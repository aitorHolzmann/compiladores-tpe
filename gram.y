%{
import src.compilador.*;
%}

/* ===== TOKENS ===== */
%token IDENTIFICADOR CONSTANTE CADENA

/* Palabras reservadas generales */
%token IF ELSE END_IF BEGIN END POUT RET CLASS FUNCTION

/* Tipos de datos del Grupo 20 (Tema 1 y Tema 7) */
%token SHORTINT SINGLEF

/* Temas particulares - palabras reservadas */
%token REPEAT UNTIL
%token AUTO
%token COMPTIME
%token IMPORT FROM EXPORT TO
%token EXTENDS
%token TOS

/* Operadores compuestos (no son un solo caracter ASCII) */
%token ASIGNAR          /* := */
%token MAYORIGUAL MENORIGUAL IGUALIGUAL DISTINTO   /* >= <= == != */

%left '+' '-'
%left '*' '/'



programa
    : IDENTIFICADOR sentencias_declarativas BEGIN sentencias_ejecutables END
        { System.out.println("Programa reconocido correctamente"); }
    ;

    
 ------------------------------------------------------------------------------------------------
/*SENTENCIAS DECLARATIVAS*/
 ------------------------------------------------------------------------------------------------   

lista_sentencias_declarativas
    : /* vacio */
    | sentencia_declarativa 
    | lista_sentencias_declarativas ';' sentencia_declarativa //
    ;
    
sentencia_declarativas // TIPO IDENTIFICADOR ;
    : declaracion_variables
    | declaracion_funciones
    | declaracion clase //TODO
    | declaracion objeto //TODO
    ;


declaracion_variables
    : tipo lista_identificadores ';'
    ;

lista_identificadores
    : IDENTIFICADOR
    | lista_identificadores ',' IDENTIFICADOR
    ;
    

tipo
    : SHORTINT
    | SINGLEF
    ;


/* FUNCIONES*/

declaracion_funciones
    : tipo FUNCTION IDENTIFICADOR '(' lista_parametros_formales ')'
        lista_sentencias_declarativas
        BEGIN sentencias_ejecutables END ';'
    | AUTO FUNCTION IDENTIFICADOR '(' lista_parametros_formales ')'
        BEGIN sentencias_ejecutables END ';'
    ;

lista_parametros_formales
    | parametro_formal
    | parametros_formales ',' parametro_formal
    ;

parametro_formal
    : tipo IDENTIFICADOR
    ;

------------------------------------------------------------------------------------------------
/* SENTENCIAS EJECUTABLES */
------------------------------------------------------------------------------------------------

sentencias_ejecutables
    : sentencia_ejecutable
    | sentencias_ejecutables ';' sentencia_ejecutable
    ;

sentencia_ejecutable
    : asignacion ';'
    | invocacion ';'
    | setencia_if ';' //podria no ir ';'?
    | sentencia_repeat_until ';' 
    | sentencia_pout ';'
    | sentencia_ret ';'

asignacion
    : IDENTIFICADOR ASIGNAR expresion
    | acceso_posicional ASIGNAR expresion
    | acceso_posicional '=' expresion //<--Esto es valido?
    ;

acceso_posicional
    : IDENTIFICADOR '[' SHORTINT ']'
    | IDENTIFICADOR '[' IDENTIFICADOR ']'
    ;

//a + b -->expresion + termino  --> expresion --> termino --> factor --> identificador = a
//termino --> factor --> identificador = b
x = persona.getEdad() + 4;
x = calcularTotal(porcentaje) + 5;
expresion
    : termino
    | expresion '+' termino
    | expresion '-' termino

termino:
    : factor
    | termino '*' factor
    | termino '/' factor
    ;

//a = (a = (a + b) + b) ESTO NO LO DEBO PERMITIR VERDAD? 
// IDENTIFICADOR = (expresion + termino) 
factor:
    : IDENTIFCADOR
    | SHORTINT
    | SINGLEF
    | IDENTIFICADOR '=' '(' expresion ')'
    ;    

    //persona.nombre
    IDENTIFCADOR IDENTIFCADOR
    nombre23p s || _ AS3  -> SI VIENE . AS14 -> IDENTIFICADOR.INVOCACION 
    
    //caso 1 nombre12.50
    //caso 2 nombre12.hola(x) = "aitor"
    //caso 3 nombre12 .50

sentencia_if
    : IF '('condicion ')' bloque ELSE bloque END_IF;
    | IF '(' condicion ')' bloque END_IF;
    ;

condicion //que pasa si termino es SHORTINT
    : expresion comparador expresion
    ;

comparador
    : MAYORIGUAL
    | MENORIGUAL
    | IGUALIGUAL
    | DISTINTO
    | '>'
    | '<'
    ;

bloque //Es correcto?
    : BEGIN sentencias_ejecutables END ';'
    | sentencia_ejecutable 
    ;

sentencia_repeat_until
    : REPEAT bloque UNTIL '(' condicion ') ';' //podria no estard ';'?
    ;

sentencia_pout
    : POUT '(' CADENA ')'
    | POUT '(' expresion ')'
    ;

sentencia_ret
    : RET '(' expresion ')'
    ;