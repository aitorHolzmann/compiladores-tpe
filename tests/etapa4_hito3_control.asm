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
    _main@x DD ?
    _main@y DD ?
    _main@z DD ?
    _cte_10 DB 10
    _cte_5 DB 5
    _cte_0 DB 0
    _cte_1_5 DD 1.5
    _cte_3_0 DD 3.0
    _cte_0_0 DD 0.0
    _str_0 DB "1. Test IF simple shortint (a >= b):", 0
    _cte_1 DB 1
    _str_1 DB "   Valor de c esperado 1:", 0
    _str_2 DB "2. Test IF-ELSE shortint (a == b):", 0
    _cte_9 DB 9
    _cte_2 DB 2
    _str_3 DB "   Valor de c esperado 2:", 0
    _str_4 DB "3. Test IF-ELSE singlef (x < y):", 0
    _cte_10_0 DD 10.0
    _cte_20_0 DD 20.0
    _str_5 DB "   Valor de z esperado 10.0:", 0
    _str_6 DB "4. Test REPEAT UNTIL shortint:", 0
    _str_7 DB "   Valor de a esperado 5:", 0
    _str_8 DB "5. Test REPEAT UNTIL singlef con bloque BEGIN...END:", 0
    _cte_0_5 DD 0.5
    _cte_1_0 DD 1.0
    _cte_2_0 DD 2.0
    _str_9 DB "   Valor de x esperado 2.0:", 0
    _str_10 DB "   Valor de z esperado 14.0:", 0
    _str_11 DB "Hito 3: Todas las sentencias de control ejecutadas.", 0
    @aux30 DB ?
    @aux38 DD ?
    @aux40 DD ?
    @aux_print_short DW ?
    @aux_print_float DQ ?
    @aux_tos DW ?

.code
start:
    MOV AL, _cte_10
    MOV _main@a, AL
    MOV AL, _cte_5
    MOV _main@b, AL
    MOV AL, _cte_0
    MOV _main@c, AL
    FLD _cte_1_5
    FSTP _main@x
    FLD _cte_3_0
    FSTP _main@y
    FLD _cte_0_0
    FSTP _main@z
    invoke printf, cfm$("%s\n"), OFFSET _str_0
    MOV AL, _main@a
    CMP AL, _main@b
    JL Label_main_10
    MOV AL, _cte_1
    MOV _main@c, AL
Label_main_10:
    invoke printf, cfm$("%s\n"), OFFSET _str_1
    MOV AL, _main@c
    MOVSX AX, AL
    MOV @aux_print_short, AX
    invoke printf, cfm$("%hd\n"), @aux_print_short
    invoke printf, cfm$("%s\n"), OFFSET _str_2
    MOV AL, _main@a
    CMP AL, _main@b
    JNE Label_main_17
    MOV AL, _cte_9
    MOV _main@c, AL
    JMP Label_main_18
Label_main_17:
    MOV AL, _cte_2
    MOV _main@c, AL
Label_main_18:
    invoke printf, cfm$("%s\n"), OFFSET _str_3
    MOV AL, _main@c
    MOVSX AX, AL
    MOV @aux_print_short, AX
    invoke printf, cfm$("%hd\n"), @aux_print_short
    invoke printf, cfm$("%s\n"), OFFSET _str_4
    FLD _main@x
    FCOMP _main@y
    FSTSW AX
    SAHF
    JAE Label_main_25
    FLD _cte_10_0
    FSTP _main@z
    JMP Label_main_26
Label_main_25:
    FLD _cte_20_0
    FSTP _main@z
Label_main_26:
    invoke printf, cfm$("%s\n"), OFFSET _str_5
    FLD _main@z
    FSTP @aux_print_float
    invoke printf, cfm$("%Lf\n"), @aux_print_float
    invoke printf, cfm$("%s\n"), OFFSET _str_6
    MOV AL, _cte_0
    MOV _main@a, AL
Label_main_30:
    MOV AL, _main@a
    ADD AL, _cte_1
    MOV @aux30, AL
    MOV AL, @aux30
    MOV _main@a, AL
    MOV AL, _main@a
    CMP AL, _cte_5
    JNE Label_main_30
    invoke printf, cfm$("%s\n"), OFFSET _str_7
    MOV AL, _main@a
    MOVSX AX, AL
    MOV @aux_print_short, AX
    invoke printf, cfm$("%hd\n"), @aux_print_short
    invoke printf, cfm$("%s\n"), OFFSET _str_8
    FLD _cte_0_0
    FSTP _main@x
Label_main_38:
    FLD _main@x
    FADD _cte_0_5
    FSTP @aux38
    FLD @aux38
    FSTP _main@x
    FLD _main@z
    FADD _cte_1_0
    FSTP @aux40
    FLD @aux40
    FSTP _main@z
    FLD _main@x
    FCOMP _cte_2_0
    FSTSW AX
    SAHF
    JB Label_main_38
    invoke printf, cfm$("%s\n"), OFFSET _str_9
    FLD _main@x
    FSTP @aux_print_float
    invoke printf, cfm$("%Lf\n"), @aux_print_float
    invoke printf, cfm$("%s\n"), OFFSET _str_10
    FLD _main@z
    FSTP @aux_print_float
    invoke printf, cfm$("%Lf\n"), @aux_print_float
    invoke printf, cfm$("%s\n"), OFFSET _str_11
    invoke ExitProcess, 0
end start
