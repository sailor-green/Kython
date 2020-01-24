#  This file is part of kython.
#
#  kython is free software: you can redistribute it and/or modify
#  it under the terms of the GNU General Public License as published by
#  the Free Software Foundation, either version 3 of the License, or
#  (at your option) any later version.
#
#  kython is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU General Public License for more details.
#
#  You should have received a copy of the GNU General Public License
#  along with kython.  If not, see <https://www.gnu.org/licenses/>.

"""
Kython reflection helper wrappers.
"""
from __future__ import annotations

# noinspection PyUnreachableCode
if False:
    from typing import Any

# __kython_internal is provided by the Kython interpreter
# and consists of actual native functions
import __kython_internal


def kotlin_type_name(thing: Any) -> str:
    """
    Gets the Kotlin type name of the specified object.

    :param thing: The object to inspect.
    :return: Effectively, ``thing::class.java.simpleName``.
    """
    return __kython_internal.kotlin_type_name(thing)
