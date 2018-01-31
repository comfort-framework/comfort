import ast
import os
import sys
from lib2to3 import refactor, pgen2


class ParserException(Exception):
    pass


def convert_2to3(file_content, file_name):
    """Quick helper function to convert python2 to python3 so that we can keep the ast buildin."""

    # all default fixers
    avail_fixes = set(refactor.get_fixers_from_package("lib2to3.fixes"))

    # create default RefactoringTool, apply to passed file_content string and return fixed string
    rt = refactor.RefactoringTool(avail_fixes)
    tmp = rt.refactor_string(file_content, file_name)
    return str(tmp)


class ImportNodeVisitor(ast.NodeVisitor):
    """Used to find imports"""

    def __init__(self):
        self.imports = []
        super().__init__()

    def generic_visit(self, node):
        type_name = type(node).__name__

        if type_name == 'Import':
            names = getattr(node, 'names', [])
            for n in names:
                self.imports.append(n.name)

        # from datetime import date -> import datetime.date
        if type_name == 'ImportFrom':
            names = getattr(node, 'names', [])
            module = getattr(node, 'module', None)
            for n in names:
                self.imports.append('{}.{}'.format(module, n.name))

        super().generic_visit(node)


class ExtractAstPython(object):
    """Extracts the AST from .py Files.
    Uses the build in ast and the visitor pattern."""

    def __init__(self, filename):
        self.astdata = None
        self.filename = filename

    def load(self):
        """Read the AST.
        We add a \n at the end because 2to3 dies otherwise.
        """
        try:
            with open(self.filename, 'r', encoding='latin-1') as f:
                self.astdata = ast.parse(source=convert_2to3(f.read() + '\n', self.filename), filename=self.filename)

            assert self.astdata is not None

            self.nt = ImportNodeVisitor()
            self.nt.visit(self.astdata)
        except pgen2.parse.ParseError as e:
            err = 'Parser Error in file: {}'.format(self.filename)
            raise ParserException(err)

    @property
    def imports(self):
        return self.nt.imports


def get_module_name_for_path(path):
    """ Method that gets the module name from a path """
    module_name = path.replace("/", ".")
    if module_name.endswith(".__init__.py"):
        return module_name[0:-12]
    return module_name[0:-3]


def get_module_name_for_import(import_name, module_names):
    """ Method that gets the module name for a specific import name """
    if not import_name:
        return None
    if import_name in module_names:
        return import_name
    else:
        return get_module_name_for_import(".".join(import_name.split(".")[0:-1]), module_names)


def get_all_imported_modules_for_star_import(import_name, module_names):
    """ Tries to get all imported modules if we have a star import """
    imported_modules = set([])
    without_star = import_name[0:-2]
    for module_name in module_names:
        if (module_name.startswith(without_star) and len(module_name.split(".")) == len(import_name.split("."))) \
                or module_name == without_star:
            imported_modules.add(module_name)
            imported_modules.update(get_init_for_imported_module(module_name, module_names))
    return imported_modules


def get_init_for_imported_module(imported_module, module_names):
    """ Get the init modules for an imported module, e.g., if foo.bar.mod1 is imported, than also foo and foo.bar """
    init_modules = set()
    parts = imported_module.split(".")[0:-1]
    i = 1
    for import_name in range(0, len(parts)):
        init_module = ".".join(parts[0:i])
        if init_module in module_names:
            init_modules.add(init_module)
        i += 1
    return init_modules


def main():
    project_dir = sys.argv[1].rstrip("/")
    module_names = []
    ignored_paths = sys.argv[2:]

    # First, get list of available modules
    for root, dirs, files in os.walk(project_dir):
        for file in files:
            if file.endswith('.py'):
                filepath = os.path.join(root, file).replace(project_dir+"/", "")
                module_names.append(get_module_name_for_path(filepath))

    # Go through the whole project directory
    for root, dirs, files in os.walk(sys.argv[1]):
        for file in files:
            # Only look at python files and exclude setup.py
            if file.endswith('.py') and file != "setup.py":
                if root.replace(project_dir+"/", "") in ignored_paths:
                    continue

                filepath = os.path.join(root, file)
                all_imports = set()

                try:
                    e = ExtractAstPython(filepath)
                    e.load()
                    for import_name in e.imports:
                        if '*' in import_name:
                            all_imports.update(get_all_imported_modules_for_star_import(import_name, module_names))
                        else:
                            imported_module = get_module_name_for_import(import_name, module_names)
                            if imported_module is not None:
                                all_imports.add(imported_module)
                                all_imports.update(get_init_for_imported_module(imported_module, module_names))
                except ParserException:
                    continue
                # this is critical
                except Exception:
                    continue

                for import_name in all_imports:
                    print("%s,%s" % (get_module_name_for_path(filepath.replace(project_dir+"/", "")), import_name))


if __name__ == '__main__':
    main()