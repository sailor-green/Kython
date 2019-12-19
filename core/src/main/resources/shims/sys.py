"""
sys python shim around the _kython_sys module.

This does *not* import the _kython_sys as this module is designed to be used BEFORE import is even
set up.
"""

if False:  # import _kython_sys
    _kython_sys = None


# sys.platform
platform = _kython_sys.platform

