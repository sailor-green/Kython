This is Kython version 3.9
==========================
.. image:: https://img.shields.io/codecov/c/github/sailor-green/kython?style=for-the-badge
    :alt: Codecov
    :target: https://codecov.io/gh/sailor-green/Kython
.. image:: https://img.shields.io/github/workflow/status/sailor-green/kython/Gradle%20Build%20-%20Test%20-%20Dist?style=for-the-badge
    :alt: GitHub Workflow Status
    :target: https://github.com/sailor-green/Kython/actions?query=workflow%3A%22Gradle+Build+-+Test+-+Dist%22
.. image:: https://img.shields.io/github/license/sailor-green/kython?color=0&style=for-the-badge
    :alt: GitHub
    :target: https://github.com/sailor-green/Kython/blob/master/LICENCE

Contents
========

* `General information`_
* `Setup`_
* `Testing`_
* `Contributing`_
* `Licence`_

General information
===================

Kython is a Python 3 interpreter written in Kotlin/JVM.

Setup
=====

Kython currently requires a copy of CPython to compile code from the text form to the ``kyc`` format that can be
understood by the interpreter. A version of CPython that matches the current Kython version is required

Testing
=======

To test the interpreter, run ``./gradlew test`` or ``gradle test`` in the root directory.

Contributing
============

Pull requests are always welcome. For major changes, please open an issue first to discuss what you would like to
change with the rest of the team.

| Please make sure to update tests as appropriate.

Licence
=======

Kython is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

| See the file "LICENCE" for information on the history of this software, terms & conditions for usage, and a DISCLAIMER OF ALL WARRANTIES.

All trademarks referenced herein are property of their respective holders.
