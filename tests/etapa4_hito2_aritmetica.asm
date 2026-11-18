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
    _main_res_int DB ?
    _main_x DD ?
    _main_y DD ?
    _main_z DD ?
    _main_res_flt DD ?
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
    MOV AL, _cte_10
    MOV _main_a, AL
    MOV AL, _cte_3
    MOV _main_b, AL
    MOV AL, _main_b
    IMUL _cte_2
    JO _err_overflow_entero
    MOV @aux3, AL
    MOV AL, _main_a
    ADD AL, @aux3
    MOV @aux4, AL
    MOV AL, _cte_6
    CBW
    IDIV _main_b
    MOV @aux5, AL
    MOV AL, @aux4
    SUB AL, @aux5
    MOV @aux6, AL
    MOV AL, @aux6
    MOV _main_res_int, AL
    MOV AL, _main_res_int
    MOVSX AX, AL
    MOV @aux_print_short, AX
    invoke printf, cfm$("%hd\n"), @aux_print_short
    MOV AL, _cte_4
    MOV _main_b, AL
    MOV AL, _main_b
    ADD AL, _main_a
    MOV @aux10, AL
    MOV AL, @aux10
    MOV _main_c, AL
    MOV AL, _main_b
    MOVSX AX, AL
    MOV @aux_print_short, AX
    invoke printf, cfm$("%hd\n"), @aux_print_short
    MOV AL, _main_c
    MOVSX AX, AL
    MOV @aux_print_short, AX
    invoke printf, cfm$("%hd\n"), @aux_print_short
    invoke printf, cfm$("%s\n"), OFFSET _str_1
    FLD _cte_12_5
    FSTP _main_x
    FLD _cte_2_5
    FSTP _main_y
    FLD _main_x
    FDIV _main_y
    FSTP @aux17
    FLD _cte_3_0
    FMUL _main_y
    FSTP @aux18
    FLD @aux17
    FADD @aux18
    FLD ST(0)
    FABS
    FCOMP @max_singlef
    FSTSW AX
    SAHF
    JA _err_overflow_float
    FSTP @aux19
    FLD @aux19
    FSUB _cte_1_5
    FSTP @aux20
    FLD @aux20
    FSTP _main_z
    FLD _main_z
    FSTP @aux_print_float
    invoke printf, cfm$("%Lf\n"), @aux_print_float
    invoke printf, cfm$("%s\n"), OFFSET _str_2
    FLD _main_x
    FADD _main_y
    FLD ST(0)
    FABS
    FCOMP @max_singlef
    FSTSW AX
    SAHF
    JA _err_overflow_float
    FSTP @aux24
    FLD @aux24
    FSTP _main_res_flt
    FLD _main_res_flt
    FCOMP @max_shortint_f
    FSTSW AX
    SAHF
    JA _err_perdida_info
    FLD _main_res_flt
    FCOMP @min_shortint_f
    FSTSW AX
    SAHF
    JB _err_perdida_info
    FLD _main_res_flt
    FISTP @aux_tos
    FILD @aux_tos
    FLD _main_res_flt
    FCOMPP
    FSTSW AX
    SAHF
    JNE _err_perdida_info
    MOV AX, @aux_tos
    MOV @aux26, AL
    MOV AL, @aux26
    ADD AL, _main_a
    MOV @aux27, AL
    MOV AL, @aux27
    MOV _main_res_int, AL
    MOV AL, _main_res_int
    MOVSX AX, AL
    MOV @aux_print_short, AX
    invoke printf, cfm$("%hd\n"), @aux_print_short
    FLD _main_res_flt
    FCOMP @max_shortint_f
    FSTSW AX
    SAHF
    JA _err_perdida_info
    FLD _main_res_flt
    FCOMP @min_shortint_f
    FSTSW AX
    SAHF
    JB _err_perdida_info
    FLD _main_res_flt
    FISTP @aux_tos
    FILD @aux_tos
    FLD _main_res_flt
    FCOMPP
    FSTSW AX
    SAHF
    JNE _err_perdida_info
    MOV AX, @aux_tos
    MOV @aux30, AL
    MOV AL, @aux30
    MOVSX AX, AL
    MOV @aux_print_short, AX
    invoke printf, cfm$("%hd\n"), @aux_print_short
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
