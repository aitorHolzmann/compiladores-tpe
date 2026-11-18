.386
.model flat, stdcall
option casemap :none
include \masm32\include\masm32rt.inc
includelib \masm32\lib\kernel32.lib
includelib \masm32\lib\masm32.lib
dll_dllcrt0 PROTO C
printf PROTO C :VARARG

.data
    _main_a DB ?
    _main_b DB ?
    _main_f1 DD ?
    _main_f2 DD ?
    ; variable comptime omitida: _main_c_cte
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
    @aux_tos DW ?
    @max_singlef DD 3.40282347E+38
    @min_shortint_f DD -128.0
    @max_shortint_f DD 127.0
    _msg_err_ovf_float DB "Error en tiempo de ejecucion: Overflow en suma de punto flotante (SINGLEF)", 0
    _msg_err_ovf_entero DB "Error en tiempo de ejecucion: Overflow en producto de enteros (SHORTINT)", 0
    _msg_err_perdida_tos DB "Error en tiempo de ejecucion: Perdida de informacion en conversion tos()", 0
    _msg_err_recursion DB "Error en tiempo de ejecucion: Recursion mutua o directa no permitida en funciones", 0

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

_err_overflow_float:
    invoke printf, cfm$("%s\n"), OFFSET _msg_err_ovf_float
    invoke ExitProcess, 1

_err_overflow_entero:
    invoke printf, cfm$("%s\n"), OFFSET _msg_err_ovf_entero
    invoke ExitProcess, 1

_err_perdida_info:
    invoke printf, cfm$("%s\n"), OFFSET _msg_err_perdida_tos
    invoke ExitProcess, 1

_err_recursion:
    invoke printf, cfm$("%s\n"), OFFSET _msg_err_recursion
    invoke ExitProcess, 1

end start
