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
    _main_c DB ?
    _main_x DD ?
    _main_y DD ?
    _main_z DD ?
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
    @max_singlef DD 3.40282347E+38
    @min_shortint_f DD -128.0
    @max_shortint_f DD 127.0
    _msg_err_ovf_float DB "Error en tiempo de ejecucion: Overflow en suma de punto flotante (SINGLEF)", 0
    _msg_err_ovf_entero DB "Error en tiempo de ejecucion: Overflow en producto de enteros (SHORTINT)", 0
    _msg_err_perdida_tos DB "Error en tiempo de ejecucion: Perdida de informacion en conversion tos()", 0
    _msg_err_recursion DB "Error en tiempo de ejecucion: Recursion mutua o directa no permitida en funciones", 0

.code
start:
    MOV AL, _cte_10
    MOV _main_a, AL
    MOV AL, _cte_5
    MOV _main_b, AL
    MOV AL, _cte_0
    MOV _main_c, AL
    FLD _cte_1_5
    FSTP _main_x
    FLD _cte_3_0
    FSTP _main_y
    FLD _cte_0_0
    FSTP _main_z
    invoke printf, cfm$("%s\n"), OFFSET _str_0
    MOV AL, _main_a
    CMP AL, _main_b
    JL Label_main_10
    MOV AL, _cte_1
    MOV _main_c, AL
Label_main_10:
    invoke printf, cfm$("%s\n"), OFFSET _str_1
    MOV AL, _main_c
    MOVSX AX, AL
    MOV @aux_print_short, AX
    invoke printf, cfm$("%hd\n"), @aux_print_short
    invoke printf, cfm$("%s\n"), OFFSET _str_2
    MOV AL, _main_a
    CMP AL, _main_b
    JNE Label_main_17
    MOV AL, _cte_9
    MOV _main_c, AL
    JMP Label_main_18
Label_main_17:
    MOV AL, _cte_2
    MOV _main_c, AL
Label_main_18:
    invoke printf, cfm$("%s\n"), OFFSET _str_3
    MOV AL, _main_c
    MOVSX AX, AL
    MOV @aux_print_short, AX
    invoke printf, cfm$("%hd\n"), @aux_print_short
    invoke printf, cfm$("%s\n"), OFFSET _str_4
    FLD _main_x
    FCOMP _main_y
    FSTSW AX
    SAHF
    JAE Label_main_25
    FLD _cte_10_0
    FSTP _main_z
    JMP Label_main_26
Label_main_25:
    FLD _cte_20_0
    FSTP _main_z
Label_main_26:
    invoke printf, cfm$("%s\n"), OFFSET _str_5
    FLD _main_z
    FSTP @aux_print_float
    invoke printf, cfm$("%Lf\n"), @aux_print_float
    invoke printf, cfm$("%s\n"), OFFSET _str_6
    MOV AL, _cte_0
    MOV _main_a, AL
Label_main_30:
    MOV AL, _main_a
    ADD AL, _cte_1
    MOV @aux30, AL
    MOV AL, @aux30
    MOV _main_a, AL
    MOV AL, _main_a
    CMP AL, _cte_5
    JNE Label_main_30
    invoke printf, cfm$("%s\n"), OFFSET _str_7
    MOV AL, _main_a
    MOVSX AX, AL
    MOV @aux_print_short, AX
    invoke printf, cfm$("%hd\n"), @aux_print_short
    invoke printf, cfm$("%s\n"), OFFSET _str_8
    FLD _cte_0_0
    FSTP _main_x
Label_main_38:
    FLD _main_x
    FADD _cte_0_5
    FLD ST(0)
    FABS
    FCOMP @max_singlef
    FSTSW AX
    SAHF
    JA _err_overflow_float
    FSTP @aux38
    FLD @aux38
    FSTP _main_x
    FLD _main_z
    FADD _cte_1_0
    FLD ST(0)
    FABS
    FCOMP @max_singlef
    FSTSW AX
    SAHF
    JA _err_overflow_float
    FSTP @aux40
    FLD @aux40
    FSTP _main_z
    FLD _main_x
    FCOMP _cte_2_0
    FSTSW AX
    SAHF
    JB Label_main_38
    invoke printf, cfm$("%s\n"), OFFSET _str_9
    FLD _main_x
    FSTP @aux_print_float
    invoke printf, cfm$("%Lf\n"), @aux_print_float
    invoke printf, cfm$("%s\n"), OFFSET _str_10
    FLD _main_z
    FSTP @aux_print_float
    invoke printf, cfm$("%Lf\n"), @aux_print_float
    invoke printf, cfm$("%s\n"), OFFSET _str_11
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
