.386
.model flat, stdcall
option casemap :none
include \masm32\include\masm32rt.inc
includelib \masm32\lib\kernel32.lib
includelib \masm32\lib\masm32.lib
dll_dllcrt0 PROTO C
printf PROTO C :VARARG

.data
    _main_caso DB ?
    _main_a DB ?
    _main_b DB ?
    _main_res_int DB ?
    _main_f1 DD ?
    _main_f2 DD ?
    _main_f_res DD ?
    _main_f_conv DD ?
    _ret_main_contar_rec DB ?
    @activa_main_contar_rec DB 0
    _main_contar_rec_n DB ?
    _cte_0 DB 0
    _cte_1 DB 1
    _cte_10 DB 10
    _cte_5 DB 5
    _str_0 DB "Producto valido 10 * 5:", 0
    _cte_10_5 DD 10.5
    _cte_2_5 DD 2.5
    _str_1 DB "Suma flotante valida 10.5 + 2.5:", 0
    _cte_42_0 DD 42.0
    _str_2 DB "Conversion tos(42.0) valida:", 0
    _str_3 DB "Llamada sin recursion contar_rec(0):", 0
    _cte_1_0E38 DD 1.0E38
    _cte_2_0 DD 2.0
    _cte_2 DB 2
    _cte_50 DB 50
    _cte_3 DB 3
    _cte_4 DB 4
    _str_4 DB "Hito 5: ejecucion finalizada sin errores.", 0
    @aux3 DB ?
    @aux9 DD ?
    @aux14 DB ?
    @aux19 DB ?
    _cte_2_5E38 DD 2.5E38
    _cte_2_0E38 DD 2.0E38
    @aux27 DD ?
    @aux34 DB ?
    @aux40 DB ?
    @aux46 DB ?
    @aux_main_contar_rec_2 DB ?
    @aux_main_contar_rec_4 DB ?
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
_proc_main_contar_rec:
    PUSH EBP
    MOV EBP, ESP
    CMP @activa_main_contar_rec, 1
    JE _err_recursion
    MOV @activa_main_contar_rec, 1
    MOV AL, _main_contar_rec_n
    CMP AL, _cte_0
    JLE Label_main_contar_rec_6
    MOV AL, _main_contar_rec_n
    SUB AL, _cte_1
    MOV @aux_main_contar_rec_2, AL
    MOV AL, @aux_main_contar_rec_2
    MOV _main_contar_rec_n, AL
    CALL _proc_main_contar_rec
    MOV AL, _ret_main_contar_rec
    MOV @aux_main_contar_rec_4, AL
    MOV AL, @aux_main_contar_rec_4
    MOV _ret_main_contar_rec, AL
    JMP _fin_main_contar_rec
Label_main_contar_rec_6:
    MOV AL, _cte_0
    MOV _ret_main_contar_rec, AL
    JMP _fin_main_contar_rec
_fin_main_contar_rec:
    MOV @activa_main_contar_rec, 0
    MOV ESP, EBP
    POP EBP
    RET

start:
    MOV AL, _cte_0
    MOV _main_caso, AL
    MOV AL, _cte_10
    MOV _main_a, AL
    MOV AL, _cte_5
    MOV _main_b, AL
    MOV AL, _main_a
    IMUL _main_b
    JO _err_overflow_entero
    MOV @aux3, AL
    MOV AL, @aux3
    MOV _main_res_int, AL
    invoke printf, cfm$("%s\n"), OFFSET _str_0
    MOV AL, _main_res_int
    MOVSX AX, AL
    MOV @aux_print_short, AX
    invoke printf, cfm$("%hd\n"), @aux_print_short
    FLD _cte_10_5
    FSTP _main_f1
    FLD _cte_2_5
    FSTP _main_f2
    FLD _main_f1
    FADD _main_f2
    FLD ST(0)
    FABS
    FCOMP @max_singlef
    FSTSW AX
    SAHF
    JA _err_overflow_float
    FSTP @aux9
    FLD @aux9
    FSTP _main_f_res
    invoke printf, cfm$("%s\n"), OFFSET _str_1
    FLD _main_f_res
    FSTP @aux_print_float
    invoke printf, cfm$("%Lf\n"), @aux_print_float
    FLD _cte_42_0
    FSTP _main_f_conv
    FLD _main_f_conv
    FCOMP @max_shortint_f
    FSTSW AX
    SAHF
    JA _err_perdida_info
    FLD _main_f_conv
    FCOMP @min_shortint_f
    FSTSW AX
    SAHF
    JB _err_perdida_info
    FLD _main_f_conv
    FISTP @aux_tos
    FILD @aux_tos
    FLD _main_f_conv
    FCOMPP
    FSTSW AX
    SAHF
    JNE _err_perdida_info
    MOV AX, @aux_tos
    MOV @aux14, AL
    MOV AL, @aux14
    MOV _main_res_int, AL
    invoke printf, cfm$("%s\n"), OFFSET _str_2
    MOV AL, _main_res_int
    MOVSX AX, AL
    MOV @aux_print_short, AX
    invoke printf, cfm$("%hd\n"), @aux_print_short
    MOV AL, _cte_0
    MOV _main_contar_rec_n, AL
    CALL _proc_main_contar_rec
    MOV AL, _ret_main_contar_rec
    MOV @aux19, AL
    MOV AL, @aux19
    MOV _main_res_int, AL
    invoke printf, cfm$("%s\n"), OFFSET _str_3
    MOV AL, _main_res_int
    MOVSX AX, AL
    MOV @aux_print_short, AX
    invoke printf, cfm$("%hd\n"), @aux_print_short
    MOV AL, _main_caso
    CMP AL, _cte_1
    JNE Label_main_30
    FLD _cte_2_5E38
    FSTP _main_f1
    FLD _cte_2_0E38
    FSTP _main_f2
    FLD _main_f1
    FADD _main_f2
    FLD ST(0)
    FABS
    FCOMP @max_singlef
    FSTSW AX
    SAHF
    JA _err_overflow_float
    FSTP @aux27
    FLD @aux27
    FSTP _main_f_res
    FLD _main_f_res
    FSTP @aux_print_float
    invoke printf, cfm$("%Lf\n"), @aux_print_float
Label_main_30:
    MOV AL, _main_caso
    CMP AL, _cte_2
    JNE Label_main_37
    MOV AL, _cte_50
    MOV _main_a, AL
    MOV AL, _cte_3
    MOV _main_b, AL
    MOV AL, _main_a
    IMUL _main_b
    JO _err_overflow_entero
    MOV @aux34, AL
    MOV AL, @aux34
    MOV _main_res_int, AL
    MOV AL, _main_res_int
    MOVSX AX, AL
    MOV @aux_print_short, AX
    invoke printf, cfm$("%hd\n"), @aux_print_short
Label_main_37:
    MOV AL, _main_caso
    CMP AL, _cte_3
    JNE Label_main_43
    FLD _cte_10_5
    FSTP _main_f_conv
    FLD _main_f_conv
    FCOMP @max_shortint_f
    FSTSW AX
    SAHF
    JA _err_perdida_info
    FLD _main_f_conv
    FCOMP @min_shortint_f
    FSTSW AX
    SAHF
    JB _err_perdida_info
    FLD _main_f_conv
    FISTP @aux_tos
    FILD @aux_tos
    FLD _main_f_conv
    FCOMPP
    FSTSW AX
    SAHF
    JNE _err_perdida_info
    MOV AX, @aux_tos
    MOV @aux40, AL
    MOV AL, @aux40
    MOV _main_res_int, AL
    MOV AL, _main_res_int
    MOVSX AX, AL
    MOV @aux_print_short, AX
    invoke printf, cfm$("%hd\n"), @aux_print_short
Label_main_43:
    MOV AL, _main_caso
    CMP AL, _cte_4
    JNE Label_main_49
    MOV AL, _cte_2
    MOV _main_contar_rec_n, AL
    CALL _proc_main_contar_rec
    MOV AL, _ret_main_contar_rec
    MOV @aux46, AL
    MOV AL, @aux46
    MOV _main_res_int, AL
    MOV AL, _main_res_int
    MOVSX AX, AL
    MOV @aux_print_short, AX
    invoke printf, cfm$("%hd\n"), @aux_print_short
Label_main_49:
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
