"""
Bootstrap file for Kython.

This will call certain fake functions that have been provided in the globals of this module,
in order to set up importlib.
"""

# __load_kython_sys -> green.sailor.kython.interpreter.bootstrap.LoadSys
sys = __load_kython_sys()
# __load_imp -> green.sailor.kython.interpreter.bootstrap.LoadImp
_imp = __load_imp()
# __load_bootstrap_external -> green.sailor.kython.interpreter.bootstrap.LoadImpBootstrapExternal
_bootstrap_external = __load_bootstrap_external()
# __load_bootstrap -> green.sailor.kython.interpreter.bootstrap.LoadBootstrap
_bootstrap = __load_bootstrap()

_bootstrap._setup(sys, _imp)
_bootstrap_external._setup(_bootstrap)

# copied from
__bootstrap_import = _bootstrap.__import__
