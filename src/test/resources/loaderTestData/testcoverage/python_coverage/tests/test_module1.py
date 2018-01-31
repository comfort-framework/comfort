import unittest

import testproject.sup.module3 as nix
import testproject.module2
import testproject.module1
from testproject.module1 import NewClassTest


class Module1Test(unittest.TestCase):
    def testInModule2(self):
        result = testproject.module2.inModule2(2, 4)
        self.assertEqual(6, result)

    def testBlub(self):
        result = nix.blub(4,2)
        self.assertEqual(6, result)

    def testTestMe(self):
        result = testproject.module1.testme(NewClassTest())
        self.assertEqual(12, result)

    def testSum(self):
        result = testproject.module1.sum(2,2)
        self.assertEqual(4, result)

    def testInnerClass(self):
        result = NewClassTest().InnerClass().solution
        self.assertEqual(3, result)

    def testGreaterThanFalse(self):
        self.assertEqual(-1, NewClassTest().greaterThan(2, 3))

    def testGreaterThanTrue(self):
        self.assertEqual(5, NewClassTest().greaterThan(3,2))
