
class NewClassTest:
    def __init__(self, name="newName"):
        self.name = name
        self.InnerClass().innerClassCall(4,2)

    def greaterThan(self, a, b):
        if a > b:
            c = a + b
            return c
        else:
            c = a - b
            return c

    def callSum(self, a, b=7):
        return sum(a, b)

    def callSum2(self, a, b=7):
        return sum(a, b)

    def callSum3(self, a, b=7):
        return sum(a, b)

    def names(self, a ,b):
        return b

    class InnerClass:
        def __init__(self):
            self.solution = sum(2,1)

        def innerClassCall(self, a, b):
            return sum(a,b)

        def selfClassCall(self):
            return self.innerClassCall(4,2)

def sum(a, b):
    return a + b


def testme(classTest):
    return classTest.callSum(5)
