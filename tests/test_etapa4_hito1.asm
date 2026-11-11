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
    _main@f1 DD ?
    _main@f2 DD ?
    ; variable comptime omitida: _main@c_cte
    _cte_42 DB 42
    _str_0 DB "--- Inicio prueba Hito 1 Etapa 4 ---", 0
    _str_1 DB "Imprimiendo constantes enteras:", 0
    _cte_10 DB 10
    _cte_neg_5 DB -5
    _str_2 DB "Imprimiendo constantes flotantes:", 0
    _cte_3_14 DD 3.14
    _cte_neg_0_5 DD -0.5
    _cte_2_25 DD 2.25
    _str_3 DB "Imprimiendo variable comptime:", 0
    _str_4 DB "--- Fin prueba Hito 1 ---", 0
    @aux_print_short DW ?
    @aux_print_float DQ ?

.code
start:
    invoke printf, cfm$("%s\n"), OFFSET _str_0
    invoke printf, cfm$("%s\n"), OFFSET _str_1
    MOV AL, _cte_10
    MOVSX AX, AL
    MOV @aux_print_short, AX
    invoke printf, cfm$("%hd\n"), @aux_print_short
    MOV AL, _cte_neg_5
    MOVSX AX, AL
    MOV @aux_print_short, AX
    invoke printf, cfm$("%hd\n"), @aux_print_short
    invoke printf, cfm$("%s\n"), OFFSET _str_2
    FLD _cte_3_14
    FSTP @aux_print_float
    invoke printf, cfm$("%Lf\n"), @aux_print_float
    FLD _cte_neg_0_5
    FSTP @aux_print_float
    invoke printf, cfm$("%Lf\n"), @aux_print_float
    FLD _cte_2_25
    FSTP @aux_print_float
    invoke printf, cfm$("%Lf\n"), @aux_print_float
    invoke printf, cfm$("%s\n"), OFFSET _str_3
    MOV AL, _cte_42
    MOVSX AX, AL
    MOV @aux_print_short, AX
    invoke printf, cfm$("%hd\n"), @aux_print_short
    invoke printf, cfm$("%s\n"), OFFSET _str_4
    invoke ExitProcess, 0
end start
