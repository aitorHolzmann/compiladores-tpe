%{
import java.io.*;
import src.compilador.AnalizadorLexico;
%}

/* ===== TOKENS ===== */
%token IDENTIFICADOR 257
%token IF 258
%token ELSE 259
%token END_IF 260
%token BEGIN 261
%token END 262
%token POUT 263
%token RET 264
%token CLASS 265
%token FUNCTION 266
%token ASIGNAR 267
%token MAYORIGUAL 268
%token MENORIGUAL 269
%token IGUALIGUAL 270
%token DISTINTO 271
%token SHORTINT 272
%token SINGLEF 273
%token CADENA 274
%token CONSTANTE 275

/* Temas particulares - palabras reservadas */
%token REPEAT 276
%token UNTIL 277
%token AUTO 278
%token COMPTIME 279
%token IMPORT 280
%token FROM 281
%token EXPORT 282
%token TO 283
%token EXTENDS 284
%token TOS 285

%left '+' '-'
%left '*' '/'

%start programa

%%

/* ========================================================================= */
/* PROGRAMA PRINCIPAL                                                        */
/* ========================================================================= */

programa
    : IDENTIFICADOR sentencias_declarativas BEGIN sentencias_ejecutables END
        { System.out.println("Programa reconocido correctamente"); }
    |   error sentencias_declarativas BEGIN sentencias_ejecutables END
        { yyerror("Se olvido el nombre del program"); }
    ;

/* ========================================================================= */
/* SENTENCIAS DECLARATIVAS                                                   */
/* ========================================================================= */

sentencias_declarativas
    : /* vacio */
    | sentencias_declarativas sentencia_declarativa
    ;

sentencia_declarativa
    : declaracion_variables
    | declaracion_funciones
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

/* FUNCIONES */

declaracion_funciones
    : tipo FUNCTION IDENTIFICADOR '(' lista_parametros_formales ')'
        sentencias_declarativas
        BEGIN sentencias_ejecutables END ';'
    | AUTO FUNCTION IDENTIFICADOR '(' lista_parametros_formales ')'
        BEGIN sentencias_ejecutables END ';'
    ;

lista_parametros_formales
    : /* vacio */
    | parametro_formal
    | lista_parametros_formales ',' parametro_formal
    ;

parametro_formal
    : tipo IDENTIFICADOR
    ;

/* ========================================================================= */
/* SENTENCIAS EJECUTABLES                                                    */
/* ========================================================================= */

sentencias_ejecutables
    : /* vacio */
    | sentencias_ejecutables sentencia_ejecutable
    ;

sentencia_ejecutable
    : asignacion ';'
    | invocacion ';'
    | sentencia_if
    | sentencia_repeat_until
    | sentencia_pout ';'
    | sentencia_ret ';'
    ;

asignacion
    : IDENTIFICADOR ASIGNAR expresion
    | acceso_posicional ASIGNAR expresion
    | acceso_posicional '=' expresion
    ;

acceso_posicional
    : IDENTIFICADOR '[' SHORTINT ']'
    | IDENTIFICADOR '[' IDENTIFICADOR ']'
    ;

invocacion
    : IDENTIFICADOR '(' ')'
    | IDENTIFICADOR '(' lista_expresiones ')'
    ;

lista_expresiones
    : expresion
    | lista_expresiones ',' expresion
    ;

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
    | SHORTINT
    | SINGLEF
    | unica
    | numero_negativo
    ;

unica
    : IDENTIFICADOR '=' '(' expresion ')'
    ;

numero_negativo
    : '-' SHORTINT
    | '-' SINGLEF
    ;

sentencia_if
    : IF '(' condicion ')' bloque ELSE bloque END_IF ';'
    | IF '(' condicion ')' bloque END_IF ';'
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

bloque
    : BEGIN sentencias_ejecutables END ';'
    | sentencia_ejecutable
    ;

sentencia_repeat_until
    : REPEAT bloque UNTIL '(' condicion ')' ';'
    ;

sentencia_pout
    : POUT '(' CADENA ')'
    | POUT '(' expresion ')'
    ;

sentencia_ret
    : RET '(' expresion ')'
    ;

%%

/* ========================================================================= */
/* CODIGO DE SOPORTE                                                         */
/* ========================================================================= */

static Parser parser;

public static void main(String[] args) {
    String ruta = "prueba_gramatica";
    if (args.length > 0) {
        ruta = args[0];
    }
    System.out.println("Compilando archivo: " + ruta);
    try {
        AnalizadorLexico.reader = new PushbackReader(new BufferedReader(new FileReader(ruta)));
        parser = new Parser(true);
        parser.yyparse();
    } catch (Exception e) {
        e.printStackTrace();
    }
}

int yylex() {
    int token = AnalizadorLexico.yylex();
    if (token <= 0) {
        return 0; // EOF para byacc/j
    }
    yylval = new ParserVal(AnalizadorLexico.getTokenActual().toString());
    return token;
}

void yyerror(String s) {
    System.out.println("Error sintactico (linea " + AnalizadorLexico.getLineaActual() + "): " + s);
}

