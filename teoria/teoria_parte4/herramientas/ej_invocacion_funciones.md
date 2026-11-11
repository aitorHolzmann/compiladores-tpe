.586
.model flat, stdcall

option casemap :none
include \masm32\include\windows.inc
include \masm32\include\kernel32.inc
include \masm32\include\user32.inc
includelib \masm32\lib\kernel32.lib
includelib \masm32\lib\user32.lib

.data
x  dd ?
p1 dd ?
msj_inicio db "Hello World!", 0
msj_fin db "Bye bye", 0
msj_f1 db "Mensaje funcion f1", 0
msj_f2 db "Mensaje funcion f2", 0
p2 dd ?


.code
f1:
invoke MessageBox, NULL, addr msj_f1, addr msj_f1, MB_OK
ret

f2:
invoke MessageBox, NULL, addr msj_f2, addr msj_f2, MB_OK
ret

start:
invoke MessageBox, NULL, addr msj_inicio, addr msj_inicio, MB_OK

call f1  ; invocacion a f1

mov eax, f2
mov p1, eax

call [p1]   ; invocacion a f2 usando un puntero a función


invoke MessageBox, NULL, addr msj_fin, addr msj_fin, MB_OK
fin:
invoke ExitProcess, 0
end start