import unittest

from testproject.sup.module3 import blub


class Module3Test(unittest.TestCase):
    def testBlub(self):
        result = blub(1,1)
        self.assertEqual(2, result)