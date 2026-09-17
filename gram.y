%{
import java.io.*;
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

/* PROGRAMA PRINCIPAL                                                        */

programa
    : IDENTIFICADOR sentencias_declarativas BEGIN sentencias_ejecutables END
        { System.out.println("Programa reconocido correctamente"); }
    ;

/* SENTENCIAS DECLARATIVAS                                                   */

sentencias_declarativas
    : /* vacio */
    | sentencias_declarativas sentencia_declarativa
    ;

sentencia_declarativa
    : declaracion_variables
    | declaracion_funciones
    | declaracion_clase
    | declaracion_objeto
    | declaracion_comptime
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

/* FUNCIONES (Tema 21: AUTO FUNCTION) */

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

/* CLASES (Temas 26, 29, 30) */

declaracion_clase
    : CLASS IDENTIFICADOR importacion_opcional
        BEGIN herencia_opcional miembros_clase END ';'
    ;

importacion_opcional
    : /* vacio */
    | IMPORT FROM lista_identificadores
    ;

herencia_opcional
    : /* vacio */
    | EXTENDS lista_identificadores ';'
    ;

miembros_clase
    : /* vacio */
    | miembros_clase miembro_clase
    ;

miembro_clase
    : atributo_clase
    | metodo_clase
    ;

atributo_clase
    : tipo lista_identificadores exportacion_opcional ';'
    ;

metodo_clase
    : tipo IDENTIFICADOR '(' lista_parametros_formales ')'
        sentencias_declarativas
        BEGIN sentencias_ejecutables END exportacion_opcional ';'
    | AUTO IDENTIFICADOR '(' lista_parametros_formales ')'
        BEGIN sentencias_ejecutables END exportacion_opcional ';'
    ;

exportacion_opcional
    : /* vacio */
    | EXPORT TO lista_identificadores
    ;

/* OBJETOS */

declaracion_objeto
    : IDENTIFICADOR lista_identificadores ';'
    ;

/* COMPTIME (Tema 22) */

declaracion_comptime
    : COMPTIME tipo lista_identificadores ';'
    ;

/* SENTENCIAS EJECUTABLES                                                    */

sentencias_ejecutables
    : /* vacio */
    | sentencias_ejecutables sentencia_ejecutable
    ;

sentencia_ejecutable
    : asignacion ';'
    | expresion ';'
    | sentencia_if
    | sentencia_repeat_until
    | sentencia_pout ';'
    | sentencia_ret ';'
    ;

asignacion
    : IDENTIFICADOR ASIGNAR expresion
    | acceso_posicional ASIGNAR expresion
    ;

acceso_posicional
    : IDENTIFICADOR '[' CONSTANTE ']'
    | IDENTIFICADOR '[' IDENTIFICADOR ']'
    ;

/* EXPRESIONES                                                               */

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
        {
            int id = $1.ival;
            String lexema = TablaSimbolos.obtenerAtributo(id, TablaSimbolos.LEXEMA);
            String tipo = TablaSimbolos.obtenerAtributo(id, "TIPO");
            if (tipo.equals("SHORTINT")) {
                int valor = Integer.parseInt(lexema);
                if (valor > AnalizadorLexico.ValorMaximoInt) {
                    yyerror("Constante shortint positiva fuera de rango (" + lexema + "). Rango permitido: [-128, 127]");
                }
            } else if (tipo.equals("SINGLEF")) {
                double valor = Double.parseDouble(lexema);
                if (valor != 0.0 && (valor < AnalizadorLexico.ValorMinimoFloat || valor > AnalizadorLexico.ValorMaximoFloat)) {
                    yyerror("Constante singlef fuera de rango (" + lexema + ")");
                }
            }
        }
    | IDENTIFICADOR '(' ')'
    | IDENTIFICADOR '(' lista_expresiones ')'
    | invocacion_metodo
    | unica
    | numero_negativo
    | conversion_tos
    | acceso_posicional
    ;

invocacion_metodo
    : IDENTIFICADOR '.' IDENTIFICADOR '(' ')'
    | IDENTIFICADOR '.' IDENTIFICADOR '(' lista_expresiones ')'
    ;

lista_expresiones
    : expresion
    | lista_expresiones ',' expresion
    ;

/* Tema 17: Asignacion en expresion */
unica
    : IDENTIFICADOR '=' '(' expresion ')'
    ;

numero_negativo
    : '-' CONSTANTE
        {
            int id_pos = $2.ival;
            String lexema_pos = TablaSimbolos.obtenerAtributo(id_pos, TablaSimbolos.LEXEMA);
            int id_neg = TablaSimbolos.convertirANegativo(lexema_pos);
            $$.ival = id_neg;
        }
    ;

/* Tema 34: Conversion explicita de flotante a entero corto */
conversion_tos
    : TOS '(' expresion ')'
    ;

/* SENTENCIAS DE CONTROL                                                     */

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
    : BEGIN sentencias_ejecutables END
    | sentencia_ejecutable
    ;

/* Tema 12: Repeat-Until */
sentencia_repeat_until
    : REPEAT bloque UNTIL '(' condicion ')' ';'
    ;

/* ENTRADA / SALIDA                                                          */

sentencia_pout
    : POUT '(' CADENA ')'
    | POUT '(' expresion ')'
    ;

sentencia_ret
    : RET '(' expresion ')'
    ;

%%

/* CODIGO DE SOPORTE                                                         */

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
        parser.yyparse(); TablaSimbolos.imprimirTabla();
    } catch (Exception e) {
        e.printStackTrace();
    }
}

int yylex() {
    int token = AnalizadorLexico.yylex();
    if (token <= 0) {
        return 0; // EOF para byacc/j
    }
    yylval = new ParserVal(AnalizadorLexico.referenciaTablaSimbolos);
    return token;
}

void yyerror(String s) {
    System.out.println("Error sintactico (linea " + AnalizadorLexico.getLineaActual() + "): " + s);
}