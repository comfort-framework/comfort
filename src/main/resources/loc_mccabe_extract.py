import os
import re
import tokenize
from ast import NodeVisitor, iter_child_nodes, parse
from lib2to3 import refactor
from lib2to3.pgen2.parse import ParseError

import sys

pattern = re.compile("[\(\[].*?[\)\]]")

SUFFIX_RE = re.compile('/?(__init__)?\.py[cwo]?')


def delete_program_part_identifier(string):
    new_string = re.sub(pattern, "", string)
    return new_string.replace(" ", "")


# https://docs.python.org/3/library/2to3.html
def convert_2to3(file_content, file_name):
    """Quick helper function to convert python2 to python3 so that we can keep the ast buildin."""

    # all default fixers
    avail_fixes = set(refactor.get_fixers_from_package("lib2to3.fixes"))

    # create default RefactoringTool, apply to passed file_content string and return fixed string
    rt = refactor.RefactoringTool(avail_fixes)
    tmp = rt.refactor_string(file_content, file_name)
    return str(tmp)

class InvalidPythonFile(ValueError):
    pass


class Visitor(NodeVisitor):
    """
    Walk a module's ast. Build a `lines` list whose value at each
    index is a context block name for the corresponding source code line.
    """
    def __init__(self, prefix=''):
        """
        Parameters
        ----------
        prefix : str
            The name to give to the module-level context.
        """
        self.line = 1  # which line (1-based) do we populate next?

        # a stack of nested contexts
        self.context = []
        self.prefix = prefix
        self.current_context = prefix

        self.lines = []
        self.complexities = {}

    def raise_complexity(self, node):
        if hasattr(node, "name"):
            name = delete_program_part_identifier(self.current_context+"."+node.name).lstrip(".")
        else:
            name = delete_program_part_identifier(self.current_context).lstrip(".")

        current_complexity = self.complexities.get(name, (0, 0))[0]
        current_complexity += 1
        self.complexities[name] = (current_complexity, current_complexity)

    def _update_current_context(self):
        if self.context:
            self.current_context = '.'.join(self.context)
        else:
            self.current_context = ''

    def _filldown(self, lineno):
        """
        Copy current_context into `lines` down up until lineno
        """
        if self.line > lineno:
            # XXX decorated functions make us jump backwards.
            # understand this more
            return

        self.lines.extend(
            self.current_context for _ in range(self.line, lineno))
        self.line = lineno

    def _add_section(self, node, type):
        """
        Register the current node as a new context block
        """
        self._filldown(node.lineno)

        # push a new context onto stack
        self.context.append(node.name+"("+type+")")
        self._update_current_context()

        for _ in map(self.visit, iter_child_nodes(node)):
            pass

        # restore current context
        self.context.pop()
        self._update_current_context()

    def generic_visit(self, node):
        if hasattr(node, 'lineno'):
            self._filldown(node.lineno + 1)

        for _ in map(self.visit, iter_child_nodes(node)):
            pass

    def visit_Module(self, node):  # noqa
        # need to manually insert one line for empty modules like __init__.py
        if not node.body:
            self.lines = [self.current_context]
        else:
            self.generic_visit(node)

    def visit_ClassDef(self, node):
        self._add_section(node, "C")

        name = delete_program_part_identifier(self.current_context+"."+node.name)
        self.complexities[name.lstrip(".")] = (0, 0)

    def visit_FunctionDef(self, node):
        self._add_section(node, "F")
        self.raise_complexity(node)

    def visit_AsyncFunctionDef(self, node):
        self._add_section(node, "AF")
        self.raise_complexity(node)

    def visit_If(self, node):
        self.raise_complexity(node)
        self.generic_visit(node)

    def visit_Loop(self, node):
        self.raise_complexity(node)
        self.generic_visit(node)

    visit_AsyncFor = visit_For = visit_While = visit_Loop

    def visit_TryExcept(self, node):
        self.raise_complexity(node)
        self.generic_visit(node)

    def visit_Try(self, node):
        self.raise_complexity(node)
        self.generic_visit(node)

    def visit_With(self, node):
        self.raise_complexity(node)
        self.generic_visit(node)

    visit_AsyncWith = visit_With


class PythonFile(object):
    """
    A file of python source.
    """
    def __init__(self, filename, prefix=None):
        """
        Parameters
        ----------
        filename : str
            The path to the file
        prefix : str (optional)
            Name to give to the outermost context in the file.
            If not provided, will be the "." form of filename
            (ie a/b/c.py -> a.b.c)
        """
        self.filename = filename

        if prefix is None:
            self.prefix = self._module_name(filename)
        else:
            self.prefix = prefix

        self.source_lines = self._read(filename)
        self.source = ''.join(self.source_lines)
        self.module_name = self._module_name(filename)

        try:
            self.ast = parse(self.source)
        except SyntaxError:
            try:
                self.source = convert_2to3(self.source+'\n', self.filename)
                self.ast = parse(source=self.source, filename=self.filename)
                self.source_lines = self.source.split("\n")
            except (ParseError, TabError):
                raise InvalidPythonFile(self.filename)

        visitor = Visitor(prefix=self.prefix)
        visitor.visit(self.ast)
        self.lines = visitor.lines
        self.complexities = visitor.complexities

    @staticmethod
    def _read(filename):
        try:
            with open(filename, 'rb') as f:
                (encoding, _) = tokenize.detect_encoding(f.readline)
        except (LookupError, SyntaxError, UnicodeError):
            # Fall back if file encoding is improperly declared
            with open(filename, encoding='latin-1') as f:
                return f.readlines()
        with open(filename, 'r', encoding=encoding) as f:
            return f.readlines()

    def get_complexities(self):
        processed_complexities = self.complexities.copy()
        for k in sorted(processed_complexities, key=len, reverse=True):
            previous_namespace = '.'.join(k.split(".")[0:-1])
            try:
                processed_complexities[previous_namespace] = (processed_complexities[previous_namespace][0],
                                                              processed_complexities[previous_namespace][1] + processed_complexities[k][1])
            except KeyError:
                pass

            if processed_complexities[k][0] == 0:
                del processed_complexities[k]
        return processed_complexities


    @staticmethod
    def raise_ccloc(data, entity):
        (ccloc, lloc) = data.get(entity, (0, 0))
        ccloc += 1
        data[entity] = (ccloc, lloc)

    @staticmethod
    def raise_lloc(data, entity):
        (ccloc, lloc) = data.get(entity, (0, 0))
        lloc += 1
        data[entity] = (ccloc, lloc)

    def get_loc_cloc(self):
        entity_loc = {}
        comment = False

        for line in range(0, len(self.source_lines)):
            python_context = delete_program_part_identifier(self.context(line+1))

            stripped_line = self.source_lines[line].strip()

            # Empty lines are skipped
            if not stripped_line or not python_context:
                continue

            if stripped_line.startswith('#'):
                self.raise_ccloc(entity_loc, python_context)
                continue
            elif (stripped_line.startswith('"""') and stripped_line.endswith('"""') and stripped_line.count('"') == 6) or \
                    (stripped_line.startswith("'''") and stripped_line.endswith("'''") and stripped_line.count("'") == 6):
                self.raise_ccloc(entity_loc, python_context)
                continue
            elif stripped_line.startswith('"""') or stripped_line.startswith("'''"):
                comment = not comment
                self.raise_ccloc(entity_loc, python_context)
                continue
            elif comment:
                self.raise_ccloc(entity_loc, python_context)
                continue
            else:
                self.raise_lloc(entity_loc, python_context)
                continue

        for k in sorted(entity_loc, key=len, reverse=True):
            previous_namespace = '.'.join(k.split(".")[0:-1])
            try:
                entity_loc[previous_namespace] = (entity_loc[previous_namespace][0] + entity_loc[k][0],
                                                  entity_loc[previous_namespace][1] + entity_loc[k][1])
            except KeyError:
                pass

        return entity_loc

    def get_results(self):
        complexities = self.get_complexities()
        lloc_cloc = self.get_loc_cloc()

        result = {}
        for entity, mccabe in complexities.items():
            if entity:
                result[entity] = (mccabe[0], mccabe[1], lloc_cloc[entity][0], lloc_cloc[entity][1])
        return result

    @staticmethod
    def _module_name(filename):
        """
        Try to find a module name for a file path
        by stripping off a prefix found in sys.modules.
        """

        absfile = os.path.abspath(filename)
        match = filename
        for base in [''] + sys.path:
            base = os.path.abspath(base)
            if absfile.startswith(base):
                match = absfile[len(base):]
                break

        return SUFFIX_RE.sub('', match).lstrip('/').replace('/', '.')

    @property
    def line_count(self):
        return len(self.lines)

    def context(self, line):
        """
        Return the context for a given 1-offset line number.
        """
        # XXX due to a limitation in Visitor,
        # non-python code after the last python code
        # in a file is not added to self.lines, so we
        # have to guard against IndexErrors.
        idx = line - 1
        if idx >= len(self.lines):
            return self.prefix
        return self.lines[idx]


def main():
    project_dir = sys.argv[1]

    for root, dirs, files in os.walk(project_dir):
        for file in files:
            # Only look at python files and exclude setup.py
            if file.endswith('.py') and file != "setup.py":
                filepath = os.path.join(root, file)
                try:
                    python_file = PythonFile(filepath)
                    results = python_file.get_results()
                    module_name = python_file.module_name

                    for entity, result in results.items():
                        print("%s::%s.%s::%s::%s::%s::%s" % (filepath, module_name, entity, result[0],
                                                             result[1], result[2], result[3]))
                except InvalidPythonFile:
                    # non critical error
                    pass
                except Exception:
                    # critical error
                    pass


if __name__ == '__main__':
    main()
