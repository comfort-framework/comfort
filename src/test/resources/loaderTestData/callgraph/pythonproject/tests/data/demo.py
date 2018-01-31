class Demo(object):
    def __init__(self):
        pass

    def foo(self):
        return 2

    def bar(self):
        return self.foo()


def callDemo():
    demo2 = Demo()
    return demo2.bar()