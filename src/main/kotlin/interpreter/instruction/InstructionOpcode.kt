/*
 * This file is part of kython.
 *
 * kython is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * kython is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with kython.  If not, see <https://www.gnu.org/licenses/>.
 *
 */
package green.sailor.kython.interpreter.instruction

/**
 * Instructions enum. This is automatically generated, and is correct as of Python 3.7.
 */
enum class InstructionOpcode(val opcode: Int) {
    POP_TOP(1),
    ROT_TWO(2),
    ROT_THREE(3),
    DUP_TOP(4),
    DUP_TOP_TWO(5),
    ROT_FOUR(6),
    NOP(9),
    UNARY_POSITIVE(10),
    UNARY_NEGATIVE(11),
    UNARY_NOT(12),
    UNARY_INVERT(15),
    BINARY_MATRIX_MULTIPLY(16),
    INPLACE_MATRIX_MULTIPLY(17),
    BINARY_POWER(19),
    BINARY_MULTIPLY(20),
    BINARY_MODULO(22),
    BINARY_ADD(23),
    BINARY_SUBTRACT(24),
    BINARY_SUBSCR(25),
    BINARY_FLOOR_DIVIDE(26),
    BINARY_TRUE_DIVIDE(27),
    INPLACE_FLOOR_DIVIDE(28),
    INPLACE_TRUE_DIVIDE(29),
    GET_AITER(50),
    GET_ANEXT(51),
    BEFORE_ASYNC_WITH(52),
    INPLACE_ADD(55),
    INPLACE_SUBTRACT(56),
    INPLACE_MULTIPLY(57),
    INPLACE_MODULO(59),
    STORE_SUBSCR(60),
    DELETE_SUBSCR(61),
    BINARY_LSHIFT(62),
    BINARY_RSHIFT(63),
    BINARY_AND(64),
    BINARY_XOR(65),
    BINARY_OR(66),
    INPLACE_POWER(67),
    GET_ITER(68),
    GET_YIELD_FROM_ITER(69),
    PRINT_EXPR(70),
    LOAD_BUILD_CLASS(71),
    YIELD_FROM(72),
    GET_AWAITABLE(73),
    INPLACE_LSHIFT(75),
    INPLACE_RSHIFT(76),
    INPLACE_AND(77),
    INPLACE_XOR(78),
    INPLACE_OR(79),
    BREAK_LOOP(80),
    WITH_CLEANUP_START(81),
    WITH_CLEANUP_FINISH(82),
    RETURN_VALUE(83),
    IMPORT_STAR(84),
    SETUP_ANNOTATIONS(85),
    YIELD_VALUE(86),
    POP_BLOCK(87),
    END_FINALLY(88),
    POP_EXCEPT(89),
    STORE_NAME(90),
    DELETE_NAME(91),
    UNPACK_SEQUENCE(92),
    FOR_ITER(93),
    UNPACK_EX(94),
    STORE_ATTR(95),
    DELETE_ATTR(96),
    STORE_GLOBAL(97),
    DELETE_GLOBAL(98),
    LOAD_CONST(100),
    LOAD_NAME(101),
    BUILD_TUPLE(102),
    BUILD_LIST(103),
    BUILD_SET(104),
    BUILD_MAP(105),
    LOAD_ATTR(106),
    COMPARE_OP(107),
    IMPORT_NAME(108),
    IMPORT_FROM(109),
    JUMP_FORWARD(110),
    JUMP_IF_FALSE_OR_POP(111),
    JUMP_IF_TRUE_OR_POP(112),
    JUMP_ABSOLUTE(113),
    POP_JUMP_IF_FALSE(114),
    POP_JUMP_IF_TRUE(115),
    LOAD_GLOBAL(116),
    CONTINUE_LOOP(119),
    SETUP_LOOP(120),
    SETUP_EXCEPT(121),
    SETUP_FINALLY(122),
    LOAD_FAST(124),
    STORE_FAST(125),
    DELETE_FAST(126),
    RAISE_VARARGS(130),
    CALL_FUNCTION(131),
    MAKE_FUNCTION(132),
    BUILD_SLICE(133),
    LOAD_CLOSURE(135),
    LOAD_DEREF(136),
    STORE_DEREF(137),
    DELETE_DEREF(138),
    CALL_FUNCTION_KW(141),
    CALL_FUNCTION_EX(142),
    SETUP_WITH(143),
    EXTENDED_ARG(144),
    LIST_APPEND(145),
    SET_ADD(146),
    MAP_ADD(147),
    LOAD_CLASSDEREF(148),
    BUILD_LIST_UNPACK(149),
    BUILD_MAP_UNPACK(150),
    BUILD_MAP_UNPACK_WITH_CALL(151),
    BUILD_TUPLE_UNPACK(152),
    BUILD_SET_UNPACK(153),
    SETUP_ASYNC_WITH(154),
    FORMAT_VALUE(155),
    BUILD_CONST_KEY_MAP(156),
    BUILD_STRING(157),
    BUILD_TUPLE_UNPACK_WITH_CALL(158),
    LOAD_METHOD(160),
    CALL_METHOD(161);

    companion object {

        fun get(opcode: Int): InstructionOpcode {
            return when (opcode) {
                1 -> POP_TOP
                2 -> ROT_TWO
                3 -> ROT_THREE
                4 -> DUP_TOP
                5 -> DUP_TOP_TWO
                6 -> ROT_FOUR
                9 -> NOP
                10 -> UNARY_POSITIVE
                11 -> UNARY_NEGATIVE
                12 -> UNARY_NOT
                15 -> UNARY_INVERT
                16 -> BINARY_MATRIX_MULTIPLY
                17 -> INPLACE_MATRIX_MULTIPLY
                19 -> BINARY_POWER
                20 -> BINARY_MULTIPLY
                22 -> BINARY_MODULO
                23 -> BINARY_ADD
                24 -> BINARY_SUBTRACT
                25 -> BINARY_SUBSCR
                26 -> BINARY_FLOOR_DIVIDE
                27 -> BINARY_TRUE_DIVIDE
                28 -> INPLACE_FLOOR_DIVIDE
                29 -> INPLACE_TRUE_DIVIDE
                50 -> GET_AITER
                51 -> GET_ANEXT
                52 -> BEFORE_ASYNC_WITH
                55 -> INPLACE_ADD
                56 -> INPLACE_SUBTRACT
                57 -> INPLACE_MULTIPLY
                59 -> INPLACE_MODULO
                60 -> STORE_SUBSCR
                61 -> DELETE_SUBSCR
                62 -> BINARY_LSHIFT
                63 -> BINARY_RSHIFT
                64 -> BINARY_AND
                65 -> BINARY_XOR
                66 -> BINARY_OR
                67 -> INPLACE_POWER
                68 -> GET_ITER
                69 -> GET_YIELD_FROM_ITER
                70 -> PRINT_EXPR
                71 -> LOAD_BUILD_CLASS
                72 -> YIELD_FROM
                73 -> GET_AWAITABLE
                75 -> INPLACE_LSHIFT
                76 -> INPLACE_RSHIFT
                77 -> INPLACE_AND
                78 -> INPLACE_XOR
                79 -> INPLACE_OR
                80 -> BREAK_LOOP
                81 -> WITH_CLEANUP_START
                82 -> WITH_CLEANUP_FINISH
                83 -> RETURN_VALUE
                84 -> IMPORT_STAR
                85 -> SETUP_ANNOTATIONS
                86 -> YIELD_VALUE
                87 -> POP_BLOCK
                88 -> END_FINALLY
                89 -> POP_EXCEPT
                90 -> STORE_NAME
                91 -> DELETE_NAME
                92 -> UNPACK_SEQUENCE
                93 -> FOR_ITER
                94 -> UNPACK_EX
                95 -> STORE_ATTR
                96 -> DELETE_ATTR
                97 -> STORE_GLOBAL
                98 -> DELETE_GLOBAL
                100 -> LOAD_CONST
                101 -> LOAD_NAME
                102 -> BUILD_TUPLE
                103 -> BUILD_LIST
                104 -> BUILD_SET
                105 -> BUILD_MAP
                106 -> LOAD_ATTR
                107 -> COMPARE_OP
                108 -> IMPORT_NAME
                109 -> IMPORT_FROM
                110 -> JUMP_FORWARD
                111 -> JUMP_IF_FALSE_OR_POP
                112 -> JUMP_IF_TRUE_OR_POP
                113 -> JUMP_ABSOLUTE
                114 -> POP_JUMP_IF_FALSE
                115 -> POP_JUMP_IF_TRUE
                116 -> LOAD_GLOBAL
                119 -> CONTINUE_LOOP
                120 -> SETUP_LOOP
                121 -> SETUP_EXCEPT
                122 -> SETUP_FINALLY
                124 -> LOAD_FAST
                125 -> STORE_FAST
                126 -> DELETE_FAST
                130 -> RAISE_VARARGS
                131 -> CALL_FUNCTION
                132 -> MAKE_FUNCTION
                133 -> BUILD_SLICE
                135 -> LOAD_CLOSURE
                136 -> LOAD_DEREF
                137 -> STORE_DEREF
                138 -> DELETE_DEREF
                141 -> CALL_FUNCTION_KW
                142 -> CALL_FUNCTION_EX
                143 -> SETUP_WITH
                144 -> EXTENDED_ARG
                145 -> LIST_APPEND
                146 -> SET_ADD
                147 -> MAP_ADD
                148 -> LOAD_CLASSDEREF
                149 -> BUILD_LIST_UNPACK
                150 -> BUILD_MAP_UNPACK
                151 -> BUILD_MAP_UNPACK_WITH_CALL
                152 -> BUILD_TUPLE_UNPACK
                153 -> BUILD_SET_UNPACK
                154 -> SETUP_ASYNC_WITH
                155 -> FORMAT_VALUE
                156 -> BUILD_CONST_KEY_MAP
                157 -> BUILD_STRING
                158 -> BUILD_TUPLE_UNPACK_WITH_CALL
                160 -> LOAD_METHOD
                161 -> CALL_METHOD
                else -> error("Unsupported opcode $opcode")
            }
        }
    }
}
