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
    _main_f1 DD ?
    ; variable comptime omitida: _main_limite_estatico
    _main_c_base_a_atr_a DB ?
    _ret_main_c_base_a_metodo_a DB ?
    @activa_main_c_base_a_metodo_a DB 0
    _main_c_base_a_metodo_a_p DB ?
    _cte_1 DB 1
    _main_c_base_b_atr_b DD ?
    _main_c_hija_atr_propio DB ?
    _main_obj_atr_a DB ?
    _main_obj_atr_b DD ?
    _main_obj_atr_propio DB ?
    _ret_main_duplicar DB ?
    @activa_main_duplicar DB 0
    _main_duplicar_num DB ?
    _cte_2 DB 2
    _ret_main_calcular DB ?
    @activa_main_calcular DB 0
    _main_calcular_x DB ?
    _main_calcular_y DD ?
    _cte_5 DB 5
    _cte_10_5 DD 10.5
    _cte_0 DB 0
    _cte_20 DB 20
    _cte_30 DB 30
    _str_0 DB "Integracion Grupo 20finalizada exitosamente", 0
    @aux2 DB ?
    @aux3 DB ?
    @aux9 DB ?
    @aux13 DB ?
    @aux18 DB ?
    @aux22 DB ?
    @aux_main_c_base_a_metodo_a_0 DB ?
    @aux_main_duplicar_0 DB ?
    @aux_main_calcular_0 DB ?
    @aux_main_calcular_1 DB ?
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
_proc_main_c_base_a_metodo_a:
    PUSH EBP
    MOV EBP, ESP
    CMP @activa_main_c_base_a_metodo_a, 1
    JE _err_recursion
    MOV @activa_main_c_base_a_metodo_a, 1
    MOV AL, _main_c_base_a_metodo_a_p
    ADD AL, _cte_1
    MOV @aux_main_c_base_a_metodo_a_0, AL
    MOV AL, @aux_main_c_base_a_metodo_a_0
    MOV _ret_main_c_base_a_metodo_a, AL
    JMP _fin_main_c_base_a_metodo_a
_fin_main_c_base_a_metodo_a:
    MOV @activa_main_c_base_a_metodo_a, 0
    MOV ESP, EBP
    POP EBP
    RET

_proc_main_duplicar:
    PUSH EBP
    MOV EBP, ESP
    CMP @activa_main_duplicar, 1
    JE _err_recursion
    MOV @activa_main_duplicar, 1
    MOV AL, _main_duplicar_num
    IMUL _cte_2
    JO _err_overflow_entero
    MOV @aux_main_duplicar_0, AL
    MOV AL, @aux_main_duplicar_0
    MOV _ret_main_duplicar, AL
    JMP _fin_main_duplicar
_fin_main_duplicar:
    MOV @activa_main_duplicar, 0
    MOV ESP, EBP
    POP EBP
    RET

_proc_main_calcular:
    PUSH EBP
    MOV EBP, ESP
    CMP @activa_main_calcular, 1
    JE _err_recursion
    MOV @activa_main_calcular, 1
    FLD _main_calcular_y
    FCOMP @max_shortint_f
    FSTSW AX
    SAHF
    JA _err_perdida_info
    FLD _main_calcular_y
    FCOMP @min_shortint_f
    FSTSW AX
    SAHF
    JB _err_perdida_info
    FLD _main_calcular_y
    FISTP @aux_tos
    FILD @aux_tos
    FLD _main_calcular_y
    FCOMPP
    FSTSW AX
    SAHF
    JNE _err_perdida_info
    MOV AX, @aux_tos
    MOV @aux_main_calcular_0, AL
    MOV AL, @aux_main_calcular_0
    ADD AL, _main_calcular_x
    MOV @aux_main_calcular_1, AL
    MOV AL, @aux_main_calcular_1
    MOV _ret_main_calcular, AL
    JMP _fin_main_calcular
_fin_main_calcular:
    MOV @activa_main_calcular, 0
    MOV ESP, EBP
    POP EBP
    RET

start:
    MOV AL, _cte_2
    MOV _main_a, AL
    FLD _cte_10_5
    FSTP _main_f1
    FLD _main_f1
    FCOMP @max_shortint_f
    FSTSW AX
    SAHF
    JA _err_perdida_info
    FLD _main_f1
    FCOMP @min_shortint_f
    FSTSW AX
    SAHF
    JB _err_perdida_info
    FLD _main_f1
    FISTP @aux_tos
    FILD @aux_tos
    FLD _main_f1
    FCOMPP
    FSTSW AX
    SAHF
    JNE _err_perdida_info
    MOV AX, @aux_tos
    MOV @aux2, AL
    MOV AL, @aux2
    ADD AL, _main_a
    MOV @aux3, AL
    MOV AL, @aux3
    MOV _main_c, AL
    MOV AL, _main_c
    MOV _main_b, AL
    MOV AL, _cte_20
    MOV _main_obj_atr_a, AL
    MOV AL, _main_a
    CMP AL, 0
    JNE Label_skip_pos_main_7_0
    MOV AL, _cte_30
    MOV _main_obj_atr_a, AL
Label_skip_pos_main_7_0:
    MOV AL, _main_a
    CMP AL, 2
    JNE Label_skip_pos_main_7_2
    MOV AL, _cte_30
    MOV _main_obj_atr_propio, AL
Label_skip_pos_main_7_2:
    MOV AL, _main_a
    MOV _main_c_base_a_metodo_a_p, AL
    MOV AL, _main_obj_atr_a
    MOV _main_c_base_a_atr_a, AL
    FLD _main_obj_atr_b
    FSTP _main_c_base_b_atr_b
    MOV AL, _main_obj_atr_propio
    MOV _main_c_hija_atr_propio, AL
    CALL _proc_main_c_base_a_metodo_a
    MOV AL, _main_c_base_a_atr_a
    MOV _main_obj_atr_a, AL
    FLD _main_c_base_b_atr_b
    FSTP _main_obj_atr_b
    MOV AL, _main_c_hija_atr_propio
    MOV _main_obj_atr_propio, AL
    MOV AL, _ret_main_c_base_a_metodo_a
    MOV @aux9, AL
    MOV AL, @aux9
    MOV _main_c, AL
    MOV AL, _main_c
    MOV _main_calcular_x, AL
    FLD _main_f1
    FSTP _main_calcular_y
    CALL _proc_main_calcular
    MOV AL, _ret_main_calcular
    MOV @aux13, AL
    MOV AL, @aux13
    MOV _main_a, AL
    MOV AL, _main_a
    CMP AL, _cte_5
    JLE Label_main_21
    MOV AL, _main_a
    MOV _main_duplicar_num, AL
    CALL _proc_main_duplicar
    MOV AL, _ret_main_duplicar
    MOV @aux18, AL
    MOV AL, @aux18
    MOV _main_b, AL
    JMP Label_main_22
Label_main_21:
    MOV AL, _cte_0
    MOV _main_b, AL
Label_main_22:
    MOV AL, _main_b
    SUB AL, _cte_1
    MOV @aux22, AL
    MOV AL, @aux22
    MOV _main_b, AL
    MOV AL, _main_b
    CMP AL, _cte_0
    JG Label_main_22
    invoke printf, cfm$("%s\n"), OFFSET _str_0
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
