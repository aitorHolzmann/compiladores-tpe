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

%%

/* ============================================================
   PROGRAMA
   ============================================================ */
programa
    : IDENTIFICADOR sentencias_declarativas BEGIN sentencias_ejecutables END
        { System.out.println("Programa reconocido correctamente"); }
    ;

/* ============================================================
   SENTENCIAS DECLARATIVAS
   ============================================================ */
sentencias_declarativas
    : /* vacio */
    | sentencias_declarativas sentencia_declarativa
    ;

sentencia_declarativa
    : declaracion_variables
    | declaracion_funcion
    | declaracion_clase
    | declaracion_objeto
    | declaracion_comptime
    ;

tipo
    : SHORTINT
    | SINGLEF
    ;

declaracion_variables
    : tipo lista_identificadores ';'
    ;

lista_identificadores
    : IDENTIFICADOR
    | lista_identificadores ',' IDENTIFICADOR
    ;

/* Tema 22: variables en tiempo de compilacion -> comptime <tipo> <lista_variables>; */
declaracion_comptime
    : COMPTIME tipo lista_identificadores ';'
    ;

/* ---- Funciones ---- */
declaracion_funcion
    : tipo FUNCTION IDENTIFICADOR '(' parametros_formales ')'
        sentencias_declarativas
        BEGIN sentencias_ejecutables END ';'
    | AUTO FUNCTION IDENTIFICADOR '(' parametros_formales ')'
        BEGIN sentencias_ejecutables END ';'
    ;

parametros_formales
    : parametro
    | parametros_formales ',' parametro
    ;

parametro
    : tipo IDENTIFICADOR
    ;

/* ---- Clases (general + Tema 26 + Tema 30) ---- */
declaracion_clase
    : CLASS IDENTIFICADOR clausula_import BEGIN miembros_clase END ';'
    ;

clausula_import
    : /* vacio */
    | IMPORT FROM lista_identificadores
    ;

miembros_clase
    : /* vacio */
    | miembros_clase miembro_clase
    ;

miembro_clase
    : tipo IDENTIFICADOR clausula_export ';'
    | tipo IDENTIFICADOR '(' parametros_formales ')'
        BEGIN sentencias_ejecutables END ';' clausula_export_opt
    | EXTENDS lista_identificadores ';'
    ;

clausula_export
    : /* vacio */
    | EXPORT TO lista_identificadores
    ;

clausula_export_opt
    : /* vacio */
    | EXPORT TO lista_identificadores ';'
    ;

/* Declaracion de objetos: <ClaseID> <lista_variables>; */
declaracion_objeto
    : IDENTIFICADOR lista_identificadores ';'
    ;

/* ============================================================
   SENTENCIAS EJECUTABLES
   ============================================================ */
sentencias_ejecutables
    : /* vacio */
    | sentencias_ejecutables sentencia_ejecutable
    ;

sentencia_ejecutable
    : asignacion ';'
    | invocacion ';'
    | sentencia_if
    | sentencia_pout ';'
    | sentencia_ret ';'
    | sentencia_repeat_until
    ;

bloque
    : BEGIN sentencias_ejecutables END
    ;

bloque_o_sentencia
    : sentencia_ejecutable
    | bloque
    ;

asignacion
    : IDENTIFICADOR ASIGNAR expresion
    | acceso_atributo ASIGNAR expresion
    | acceso_posicional ASIGNAR expresion
    ;

/* Tema 28/29: acceso a atributos, tradicional y posicional */
acceso_atributo
    : IDENTIFICADOR '.' IDENTIFICADOR
    ;

acceso_posicional
    : IDENTIFICADOR '[' expresion ']'
    ;

invocacion
    : IDENTIFICADOR '(' lista_parametros_reales ')'
    | IDENTIFICADOR '.' IDENTIFICADOR '(' lista_parametros_reales ')'
    ;

lista_parametros_reales
    : expresion
    | lista_parametros_reales ',' expresion
    ;

sentencia_if
    : IF '(' condicion ')' bloque_o_sentencia ELSE bloque_o_sentencia END_IF ';'
    | IF '(' condicion ')' bloque_o_sentencia END_IF ';'
    ;

condicion
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

sentencia_pout
    : POUT '(' expresion ')'
    | POUT '(' CADENA ')'
    ;

sentencia_ret
    : RET '(' expresion ')'
    ;

/* Tema 12: repeat until */
sentencia_repeat_until
    : REPEAT bloque_o_sentencia UNTIL '(' condicion ')' ';'
    ;

/* ============================================================
   EXPRESIONES
   Nota: el enunciado no permite anidar expresiones entre
   parentesis salvo en los casos puntuales de invocacion,
   conversion explicita (Tema 34) y asignacion en expresion
   (Tema 17).
   ============================================================ */
expresion
    : termino
    | expresion '+' termino
    | expresion '-' termino
    ;

termino
    : factor
    | termino '*' factor
    | termino '/' factor
    ;

factor
    : IDENTIFICADOR
    | CONSTANTE
    | invocacion
    | acceso_atributo
    | acceso_posicional
    | TOS '(' expresion ')'
    | IDENTIFICADOR '=' '(' expresion ')'
    ;

%%

int yylex() {
    int token = AnalizadorLexico.yylex();
    if (token == IDENTIFICADOR || token == CONSTANTE || token == CADENA) {
        yylval = new ParserVal(AnalizadorLexico.referenciaTablaSimbolos);
    }
    return token;
}

void yyerror(String s) {
    System.out.println("Error de sintaxis en linea " + AnalizadorLexico.getLineaActual() + ": " + s);
}

public static void main(String[] args) throws Exception {
    Parser parser = new Parser();
    AnalizadorLexico.reader = new java.io.PushbackReader(
        new java.io.BufferedReader(new java.io.FileReader(args[0])));
    parser.yyparse();
}
