"""
This is a dummy testsuite used to test smother's testrunner plugins.
These tests are *not* meant to be invoked directly as part of
smother's own test suite (hence the nonstandard filename). Instead
they are invoked from within other tests (namely test_plugins).
"""
from tests.data import demo


def test_callDemo():
    demo.callDemo()
