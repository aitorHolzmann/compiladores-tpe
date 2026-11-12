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
    _cte_5 DB 5
    _cte_10 DB 10
    _cte_0 DB 0
    _cte_1_0 DD 1.0
    _cte_2_0 DD 2.0
    _cte_0_0 DD 0.0
    _cte_1 DB 1
    _cte_3_0 DD 3.0
    _cte_2 DB 2
    _cte_3 DB 3
    _cte_20 DB 20
    _cte_10_0 DD 10.0
    _cte_30 DB 30
    _str_0 DB "Fin de control de flujo ok", 0
    @aux8 DB ?
    @aux12 DD ?
    @aux15 DD ?
    @aux26 DB ?
    @aux30 DD ?
    @aux32 DD ?
    @aux38 DB ?
    @aux_print_short DW ?
    @aux_print_float DQ ?
    @aux_tos DW ?

.code
start:
    MOV AL, _cte_5
    MOV _main@a, AL
    MOV AL, _cte_10
    MOV _main@b, AL
    MOV AL, _cte_0
    MOV _main@c, AL
    FLD _cte_1_0
    FSTP _main@x
    FLD _cte_2_0
    FSTP _main@y
    FLD _cte_0_0
    FSTP _main@z
    MOV AL, _main@a
    CMP AL, _cte_10
    JLE Label_main_10
    MOV AL, _main@a
    ADD AL, _cte_1
    MOV @aux8, AL
    MOV AL, @aux8
    MOV _main@c, AL
Label_main_10:
    FLD _main@x
    FCOMP _main@y
    FSTSW AX
    SAHF
    JA Label_main_15
    FLD _main@x
    FMUL _cte_2_0
    FSTP @aux12
    FLD @aux12
    FSTP _main@z
    JMP Label_main_17
Label_main_15:
    FLD _main@y
    FMUL _cte_3_0
    FSTP @aux15
    FLD @aux15
    FSTP _main@z
Label_main_17:
    MOV AL, _main@a
    CMP AL, _main@b
    JNE Label_main_25
    MOV AL, _main@c
    CMP AL, _cte_0
    JE Label_main_23
    MOV AL, _cte_1
    MOV _main@a, AL
    JMP Label_main_24
Label_main_23:
    MOV AL, _cte_2
    MOV _main@a, AL
Label_main_24:
    JMP Label_main_26
Label_main_25:
    MOV AL, _cte_3
    MOV _main@a, AL
Label_main_26:
    MOV AL, _main@a
    ADD AL, _cte_1
    MOV @aux26, AL
    MOV AL, @aux26
    MOV _main@a, AL
    MOV AL, _main@a
    CMP AL, _cte_20
    JL Label_main_26
Label_main_30:
    FLD _main@x
    FADD _cte_1_0
    FSTP @aux30
    FLD @aux30
    FSTP _main@x
    FLD _main@z
    FSUB _cte_1_0
    FSTP @aux32
    FLD @aux32
    FSTP _main@z
    FLD _main@x
    FCOMP _cte_10_0
    FSTSW AX
    SAHF
    JBE Label_main_30
Label_main_36:
    MOV AL, _main@a
    CMP AL, _cte_30
    JGE Label_main_40
    MOV AL, _main@a
    ADD AL, _cte_2
    MOV @aux38, AL
    MOV AL, @aux38
    MOV _main@a, AL
Label_main_40:
    MOV AL, _main@a
    CMP AL, _cte_30
    JNE Label_main_36
    invoke printf, cfm$("%s\n"), OFFSET _str_0
    invoke ExitProcess, 0
end start
