.386
.MODEL flat, stdcall
option casemap :none
include \masm32\include\masm32rt.inc
includelib \masm32\lib\kernel32.lib
includelib \masm32\lib\masm32.lib

dll_dllcrt0 PROTO C
printf PROTO C : VARARG

.data
__cadenita__ DB "Soy un string", 10, 0
__new_line__ DB 13, 10, 0 ; CRLF

floati DQ 5.33 ; por como funciona printf, hay que si o si declararlas de 64bits
_floati DQ -5.33

inti DW 32767
_inti DW -32768

longi DD 2147483647
_longi DD -2147483648

uinti DW 65535
_uinti DW 0

ulongi DD 4294967295
_ulongi DD 0

shorti DB 127
_shorti DB -128
@varForPrintShort DW ?; por como funciona printf, los short necesitan que se haga una promoción a int

hexi  DW  0128Fh   
octi DW 077o

.code
start:
invoke StdOut, addr __cadenita__

invoke printf, ADDR __cadenita__
INVOKE printf, addr __new_line__ 

invoke printf , cfm$("%.20Lf\n"), floati
invoke printf , cfm$("%.20Lf\n"), _floati
INVOKE printf, addr __new_line__ 

invoke printf , cfm$("%hi\n"), inti
invoke printf , cfm$("%hi\n"), _inti
INVOKE printf, addr __new_line__ 

invoke printf , cfm$("%d\n"), longi
invoke printf , cfm$("%d\n"), _longi
INVOKE printf, addr __new_line__ 

invoke printf , cfm$("%u\n"), uinti
invoke printf , cfm$("%u\n"), _uinti
INVOKE printf, addr __new_line__ 

invoke printf , cfm$("%u\n"), ulongi
invoke printf , cfm$("%u\n"), _ulongi
INVOKE printf, addr __new_line__ 

MOV AL, shorti
MOVSX AX, AL 
MOV @varForPrintShort, AX 
invoke printf , cfm$("%hd\n"), @varForPrintShort

MOV AL, _shorti
MOVSX AX, AL 
MOV @varForPrintShort, AX 
invoke printf , cfm$("%hd\n"), @varForPrintShort

INVOKE printf, addr __new_line__ 

invoke printf , cfm$("%X\n"), hexi ; X o x, dependiendo si la impresión lleva mayus o minus
invoke printf , cfm$("%d\n"), hexi ; la hexa interpretada como decimal

INVOKE printf, addr __new_line__ 

invoke printf , cfm$("%o\n"), octi ; 
invoke printf , cfm$("%d\n"), octi ; la octal interpretada como decimal

INVOKE printf, addr __new_line__ 


end start
INVOKE ExitProcess, 0