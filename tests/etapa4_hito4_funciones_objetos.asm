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
    _main_res DB ?
    _main_f1 DD ?
    _main_c_base_a_atr_a DB ?
    _ret_main_c_base_a_sumar_base DB ?
    @activa_main_c_base_a_sumar_base DB 0
    _main_c_base_a_sumar_base_p DB ?
    _cte_10 DB 10
    _main_c_base_b_atr_b DD ?
    _main_c_hija_atr_propio DB ?
    _main_obj_atr_a DB ?
    _main_obj_atr_b DD ?
    _main_obj_atr_propio DB ?
    _ret_main_triplicar DB ?
    @activa_main_triplicar DB 0
    _main_triplicar_num DB ?
    _cte_3 DB 3
    _ret_main_combinar DB ?
    @activa_main_combinar DB 0
    _main_combinar_x DB ?
    _main_combinar_y DD ?
    _cte_2 DB 2
    _cte_5_0 DD 5.0
    _cte_0 DB 0
    _cte_15 DB 15
    _cte_25 DB 25
    _str_0 DB "Resultado de obj.sumar_base(2) esperado 12:", 0
    _str_1 DB "Resultado de combinar(12, 5.0) esperado 17:", 0
    _str_2 DB "Resultado de triplicar(2) esperado 6:", 0
    _str_3 DB "Hito 4 ejecutado correctamente.", 0
    @aux5 DB ?
    @aux11 DB ?
    @aux16 DB ?
    @aux_main_c_base_a_sumar_base_0 DB ?
    @aux_main_triplicar_0 DB ?
    @aux_main_combinar_0 DB ?
    @aux_main_combinar_1 DB ?
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
_proc_main_c_base_a_sumar_base:
    PUSH EBP
    MOV EBP, ESP
    CMP @activa_main_c_base_a_sumar_base, 1
    JE _err_recursion
    MOV @activa_main_c_base_a_sumar_base, 1
    MOV AL, _main_c_base_a_sumar_base_p
    ADD AL, _cte_10
    MOV @aux_main_c_base_a_sumar_base_0, AL
    MOV AL, @aux_main_c_base_a_sumar_base_0
    MOV _ret_main_c_base_a_sumar_base, AL
    JMP _fin_main_c_base_a_sumar_base
_fin_main_c_base_a_sumar_base:
    MOV @activa_main_c_base_a_sumar_base, 0
    MOV ESP, EBP
    POP EBP
    RET

_proc_main_triplicar:
    PUSH EBP
    MOV EBP, ESP
    CMP @activa_main_triplicar, 1
    JE _err_recursion
    MOV @activa_main_triplicar, 1
    MOV AL, _main_triplicar_num
    IMUL _cte_3
    JO _err_overflow_entero
    MOV @aux_main_triplicar_0, AL
    MOV AL, @aux_main_triplicar_0
    MOV _ret_main_triplicar, AL
    JMP _fin_main_triplicar
_fin_main_triplicar:
    MOV @activa_main_triplicar, 0
    MOV ESP, EBP
    POP EBP
    RET

_proc_main_combinar:
    PUSH EBP
    MOV EBP, ESP
    CMP @activa_main_combinar, 1
    JE _err_recursion
    MOV @activa_main_combinar, 1
    FLD _main_combinar_y
    FCOMP @max_shortint_f
    FSTSW AX
    SAHF
    JA _err_perdida_info
    FLD _main_combinar_y
    FCOMP @min_shortint_f
    FSTSW AX
    SAHF
    JB _err_perdida_info
    FLD _main_combinar_y
    FISTP @aux_tos
    FILD @aux_tos
    FLD _main_combinar_y
    FCOMPP
    FSTSW AX
    SAHF
    JNE _err_perdida_info
    MOV AX, @aux_tos
    MOV @aux_main_combinar_0, AL
    MOV AL, @aux_main_combinar_0
    ADD AL, _main_combinar_x
    MOV @aux_main_combinar_1, AL
    MOV AL, @aux_main_combinar_1
    MOV _ret_main_combinar, AL
    JMP _fin_main_combinar
_fin_main_combinar:
    MOV @activa_main_combinar, 0
    MOV ESP, EBP
    POP EBP
    RET

start:
    MOV AL, _cte_2
    MOV _main_a, AL
    FLD _cte_5_0
    FSTP _main_f1
    MOV AL, _cte_15
    MOV _main_obj_atr_a, AL
    MOV AL, _main_a
    CMP AL, 0
    JNE Label_skip_pos_main_3_0
    MOV AL, _cte_25
    MOV _main_obj_atr_a, AL
Label_skip_pos_main_3_0:
    MOV AL, _main_a
    CMP AL, 2
    JNE Label_skip_pos_main_3_2
    MOV AL, _cte_25
    MOV _main_obj_atr_propio, AL
Label_skip_pos_main_3_2:
    MOV AL, _main_a
    MOV _main_c_base_a_sumar_base_p, AL
    MOV AL, _main_obj_atr_a
    MOV _main_c_base_a_atr_a, AL
    FLD _main_obj_atr_b
    FSTP _main_c_base_b_atr_b
    MOV AL, _main_obj_atr_propio
    MOV _main_c_hija_atr_propio, AL
    CALL _proc_main_c_base_a_sumar_base
    MOV AL, _main_c_base_a_atr_a
    MOV _main_obj_atr_a, AL
    FLD _main_c_base_b_atr_b
    FSTP _main_obj_atr_b
    MOV AL, _main_c_hija_atr_propio
    MOV _main_obj_atr_propio, AL
    MOV AL, _ret_main_c_base_a_sumar_base
    MOV @aux5, AL
    MOV AL, @aux5
    MOV _main_b, AL
    invoke printf, cfm$("%s\n"), OFFSET _str_0
    MOV AL, _main_b
    MOVSX AX, AL
    MOV @aux_print_short, AX
    invoke printf, cfm$("%hd\n"), @aux_print_short
    MOV AL, _main_b
    MOV _main_combinar_x, AL
    FLD _main_f1
    FSTP _main_combinar_y
    CALL _proc_main_combinar
    MOV AL, _ret_main_combinar
    MOV @aux11, AL
    MOV AL, @aux11
    MOV _main_res, AL
    invoke printf, cfm$("%s\n"), OFFSET _str_1
    MOV AL, _main_res
    MOVSX AX, AL
    MOV @aux_print_short, AX
    invoke printf, cfm$("%hd\n"), @aux_print_short
    MOV AL, _main_a
    MOV _main_triplicar_num, AL
    CALL _proc_main_triplicar
    MOV AL, _ret_main_triplicar
    MOV @aux16, AL
    MOV AL, @aux16
    MOV _main_res, AL
    invoke printf, cfm$("%s\n"), OFFSET _str_2
    MOV AL, _main_res
    MOVSX AX, AL
    MOV @aux_print_short, AX
    invoke printf, cfm$("%hd\n"), @aux_print_short
    invoke printf, cfm$("%s\n"), OFFSET _str_3
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
