.386
.model flat, stdcall
option casemap :none
include \masm32\include\masm32rt.inc
includelib \masm32\lib\kernel32.lib
includelib \masm32\lib\masm32.lib
dll_dllcrt0 PROTO C
printf PROTO C :VARARG

.data
    _main@a DB ?
    _main@b DB ?
    _main@c DB ?
    _main@res_int DB ?
    _main@x DD ?
    _main@y DD ?
    _main@z DD ?
    _main@res_flt DD ?
    _str_0 DB "--- Prueba Hito 2: Aritmetica Entera (SHORTINT) ---", 0
    _cte_10 DB 10
    _cte_3 DB 3
    _cte_2 DB 2
    _cte_6 DB 6
    _cte_4 DB 4
    _str_1 DB "--- Prueba Hito 2: Aritmetica Flotante (SINGLEF) ---", 0
    _cte_12_5 DD 12.5
    _cte_2_5 DD 2.5
    _cte_3_0 DD 3.0
    _cte_1_5 DD 1.5
    _str_2 DB "--- Prueba Hito 2: Conversion explicita tos() (Tema 34) ---", 0
    @aux3 DB ?
    @aux4 DB ?
    @aux5 DB ?
    @aux6 DB ?
    @aux10 DB ?
    @aux17 DD ?
    @aux18 DD ?
    @aux19 DD ?
    @aux20 DD ?
    @aux24 DD ?
    @aux26 DB ?
    @aux27 DB ?
    @aux30 DB ?
    @aux_print_short DW ?
    @aux_print_float DQ ?
    @aux_tos DW ?

.code
start:
    invoke printf, cfm$("%s\n"), OFFSET _str_0
    MOV AL, _cte_10
    MOV _main@a, AL
    MOV AL, _cte_3
    MOV _main@b, AL
    MOV AL, _main@b
    IMUL _cte_2
    MOV @aux3, AL
    MOV AL, _main@a
    ADD AL, @aux3
    MOV @aux4, AL
    MOV AL, _cte_6
    CBW
    IDIV _main@b
    MOV @aux5, AL
    MOV AL, @aux4
    SUB AL, @aux5
    MOV @aux6, AL
    MOV AL, @aux6
    MOV _main@res_int, AL
    MOV AL, _main@res_int
    MOVSX AX, AL
    MOV @aux_print_short, AX
    invoke printf, cfm$("%hd\n"), @aux_print_short
    MOV AL, _cte_4
    MOV _main@b, AL
    MOV AL, _main@b
    ADD AL, _main@a
    MOV @aux10, AL
    MOV AL, @aux10
    MOV _main@c, AL
    MOV AL, _main@b
    MOVSX AX, AL
    MOV @aux_print_short, AX
    invoke printf, cfm$("%hd\n"), @aux_print_short
    MOV AL, _main@c
    MOVSX AX, AL
    MOV @aux_print_short, AX
    invoke printf, cfm$("%hd\n"), @aux_print_short
    invoke printf, cfm$("%s\n"), OFFSET _str_1
    FLD _cte_12_5
    FSTP _main@x
    FLD _cte_2_5
    FSTP _main@y
    FLD _main@x
    FDIV _main@y
    FSTP @aux17
    FLD _cte_3_0
    FMUL _main@y
    FSTP @aux18
    FLD @aux17
    FADD @aux18
    FSTP @aux19
    FLD @aux19
    FSUB _cte_1_5
    FSTP @aux20
    FLD @aux20
    FSTP _main@z
    FLD _main@z
    FSTP @aux_print_float
    invoke printf, cfm$("%Lf\n"), @aux_print_float
    invoke printf, cfm$("%s\n"), OFFSET _str_2
    FLD _main@x
    FADD _main@y
    FSTP @aux24
    FLD @aux24
    FSTP _main@res_flt
    FLD _main@res_flt
    FISTP @aux_tos
    MOV AX, @aux_tos
    MOV @aux26, AL
    MOV AL, @aux26
    ADD AL, _main@a
    MOV @aux27, AL
    MOV AL, @aux27
    MOV _main@res_int, AL
    MOV AL, _main@res_int
    MOVSX AX, AL
    MOV @aux_print_short, AX
    invoke printf, cfm$("%hd\n"), @aux_print_short
    FLD _main@x
    FISTP @aux_tos
    MOV AX, @aux_tos
    MOV @aux30, AL
    MOV AL, @aux30
    MOVSX AX, AL
    MOV @aux_print_short, AX
    invoke printf, cfm$("%hd\n"), @aux_print_short
    invoke ExitProcess, 0
end start
