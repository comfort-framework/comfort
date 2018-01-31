def new(a,b):
    return a - b

def x(a,b):
    return a

def y(z,t):
    return z(*t)

def inModule2(a,b):
    return a+b

def outerMethod(a,b):
    def innerMethod(a,b):
        if a < b:
            return a+b
        else:
            return a-b
    return innerMethod(a+2, b+4)
    
