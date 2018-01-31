import unittest

from testproject.module1 import NewClassTest
from testproject.module2 import y, x, new, outerMethod


class Module2Test(unittest.TestCase):
    def setUp(self):
        self.newClass = NewClassTest()

    def testYWithX(self):
        result = y(x,("hello","manuel"))
        self.assertEqual("hello", result)

    def textYWithClass(self):
        result = y(self.newClass.names, ("hello", "guido"))
        self.assertEqual("guido", result)

    def testNew(self):
        result = new(5,3)
        self.assertEqual(2, result)

    def testNewNegative(self):
        result = new(2, 3)
        self.assertEqual(-1, result)

    def testOuterMethod(self):
        result = outerMethod(5, 8)
        self.assertEqual(19, result)
