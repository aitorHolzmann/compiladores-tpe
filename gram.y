%{
import java.io.*;
import java.util.ArrayList;
import java.util.Stack;
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
%token CTE_SHORTINT 286
%token CTE_SINGLEF 287

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

nombre_programa
    : IDENTIFICADOR
        {
            int ref = $1.ival;
            String nombre = TablaSimbolos.obtenerAtributo(ref, TablaSimbolos.LEXEMA);
            TablaSimbolos.declararVariable(nombre, "programa", "nombre_programa", ambitoActual);
            $$ = $1;
        }
    ;

programa
    : nombre_programa sentencias_declarativas BEGIN sentencias_ejecutables END
        { 
            if (cant_errores == 0 && cant_errores_semanticos == 0) {
                System.out.println("Programa reconocido correctamente"); 
            } else {
                System.out.println("Compilacion finalizada con " + cant_errores + " error(es) sintactico(s) y " + cant_errores_semanticos + " error(es) semantico(s)");
            }
        }
    | error { yyerror("Falta el nombre del programa al inicio"); } sentencias_declarativas BEGIN sentencias_ejecutables END
        { 
            System.out.println("Compilacion finalizada con " + cant_errores + " error(es) sintactico(s) y " + cant_errores_semanticos + " error(es) semantico(s)");
        }
    | nombre_programa sentencias_declarativas error { yyerror("Falta el delimitador BEGIN de sentencias ejecutables"); } sentencias_ejecutables END
        {
            System.out.println("Compilacion finalizada con " + cant_errores + " error(es) sintactico(s) y " + cant_errores_semanticos + " error(es) semantico(s)");
        }
    | nombre_programa sentencias_declarativas BEGIN sentencias_ejecutables
        {
            yyerror("Falta el delimitador END al final del programa");
            System.out.println("Compilacion finalizada con " + cant_errores + " error(es) sintactico(s) y " + cant_errores_semanticos + " error(es) semantico(s)");
        }
    ;

/* SENTENCIAS DECLARATIVAS                                                   */

sentencias_declarativas
    : /* vacio */
    | sentencias_declarativas sentencia_declarativa
    ;

sentencia_declarativa
    : declaracion_variables { imprimirRegla("declaracion_variables"); }
    | declaracion_funciones { imprimirRegla("declaracion_funciones"); }
    | declaracion_clase { imprimirRegla("declaracion_clase"); }
    | declaracion_objeto { imprimirRegla("declaracion_objeto"); }
    | declaracion_comptime { imprimirRegla("declaracion_comptime"); }
    ;


declaracion_variables
    : tipo lista_variables ';'
        {
            String tipoDato = $1.sval;
            ArrayList<String> variables = (ArrayList<String>) $2.obj;
            if (variables != null) {
                for (int i = 0; i < variables.size(); i++) {
                    String nombreVar = variables.get(i);
                    TablaSimbolos.declararVariable(nombreVar, tipoDato, "variable", ambitoActual);
                }
            }
        }
    | tipo error ';'
        { yyerror("Falta ';' al final de la declaracion de variables"); }
    ;

lista_variables
    : IDENTIFICADOR
        {
            ArrayList<String> lista = new ArrayList<>();
            int ref = $1.ival;
            String lexema = TablaSimbolos.obtenerAtributo(ref, TablaSimbolos.LEXEMA);
            lista.add(lexema);
            ParserVal pv = new ParserVal();
            pv.obj = lista;
            $$ = pv;
        }
    | lista_variables ',' IDENTIFICADOR
        {
            ArrayList<String> lista = (ArrayList<String>) $1.obj;
            int ref = $3.ival;
            String lexema = TablaSimbolos.obtenerAtributo(ref, TablaSimbolos.LEXEMA);
            lista.add(lexema);
            $$ = $1;
        }
    | lista_variables IDENTIFICADOR   { yyerror("Falta ',' entre los identificadores"); $$ = $1; }
    ;

lista_identificadores
    : IDENTIFICADOR
        {
            ArrayList<String> lista = new ArrayList<>();
            int ref = $1.ival;
            String lexema = TablaSimbolos.obtenerAtributo(ref, TablaSimbolos.LEXEMA);
            lista.add(lexema);
            ParserVal pv = new ParserVal();
            pv.obj = lista;
            $$ = pv;
        }
    | lista_identificadores ',' IDENTIFICADOR
        {
            ArrayList<String> lista = (ArrayList<String>) $1.obj;
            int ref = $3.ival;
            String lexema = TablaSimbolos.obtenerAtributo(ref, TablaSimbolos.LEXEMA);
            lista.add(lexema);
            $$ = $1;
        }
    ;

tipo
    : SHORTINT { $$ = new ParserVal("SHORTINT"); }
    | SINGLEF  { $$ = new ParserVal("SINGLEF"); }
    ;

/* FUNCIONES (Tema 21: AUTO FUNCTION) */

encabezado_funcion
    : tipo FUNCTION IDENTIFICADOR
        {
            int ref = $3.ival;
            String nombre = TablaSimbolos.obtenerAtributo(ref, TablaSimbolos.LEXEMA);
            int idFunc = TablaSimbolos.declararVariable(nombre, $1.sval, "funcion", ambitoActual);
            String mangling = nombre + ambitoActual;
            if (idFunc != TablaSimbolos.LEXEMA_NO_ENCONTRADO) {
                TablaSimbolos.agregarAtributo(idFunc, "TIENE_RET", "false");
            }
            entrarAmbito(nombre);
            GeneradorTerceto.crearTerceto("FUNCION", mangling, "-");
            $$ = $3;
        }
    ;

inicio_auto_funcion
    : AUTO FUNCTION IDENTIFICADOR
        {
            int ref = $3.ival;
            String nombre = TablaSimbolos.obtenerAtributo(ref, TablaSimbolos.LEXEMA);
            TablaSimbolos.declararVariable(nombre, "auto", "funcion", ambitoActual);
            String mangling = nombre + ambitoActual;
            entrarAmbito(nombre);
            GeneradorTerceto.crearTerceto("FUNCION", mangling, "-");
            $$ = $3;
        }
    ;

encabezado_auto_funcion
    : inicio_auto_funcion '(' lista_parametros_formales ')'
        { cant_retornos_auto = 0; }
    ;

declaracion_funciones
    : encabezado_funcion '(' lista_parametros_formales ')'
        sentencias_declarativas
        BEGIN sentencias_ejecutables END ';'
        {
            String mangling = obtenerFuncionActualMangled();
            if (mangling != null) {
                int refFunc = TablaSimbolos.buscarPorMangling(mangling);
                String tieneRet = TablaSimbolos.obtenerAtributo(refFunc, "TIENE_RET");
                if (!"true".equals(tieneRet)) {
                    String tipo = TablaSimbolos.obtenerAtributo(refFunc, TablaSimbolos.TIPO);
                    String valDef = "SINGLEF".equals(tipo) ? "0.0" : "0$s";
                    GeneradorTerceto.crearTerceto("RET", valDef, "-", tipo);
                }
                GeneradorTerceto.crearTerceto("FIN_FUNCION", mangling, "-");
            }
            salirAmbito();
        }
    | tipo FUNCTION error { yyerror("Falta nombre de funcion"); } '(' lista_parametros_formales ')'
        sentencias_declarativas
        BEGIN sentencias_ejecutables END ';' 
    | encabezado_funcion '(' lista_parametros_formales ')'
        sentencias_declarativas
        BEGIN sentencias_ejecutables END
        {
            String mangling = obtenerFuncionActualMangled();
            if (mangling != null) {
                GeneradorTerceto.crearTerceto("FIN_FUNCION", mangling, "-");
            }
            yyerror("Falta ';' al final de la declaracion de funcion");
            salirAmbito();
        } 
    | encabezado_auto_funcion BEGIN sentencias_ejecutables END ';'
        {
            String mangling = obtenerFuncionActualMangled();
            if (cant_retornos_auto == 0) {
                yyerror("Ausencia de sentencia de retorno 'RET' en funcion AUTO");
            }
            if (mangling != null) {
                GeneradorTerceto.crearTerceto("FIN_FUNCION", mangling, "-");
            }
            cant_retornos_auto = -1;
            salirAmbito();
        } 
    | AUTO FUNCTION error { yyerror("Falta nombre de funcion"); } '(' lista_parametros_formales ')'
        sentencias_declarativas
        BEGIN sentencias_ejecutables END ';' 
    | encabezado_auto_funcion BEGIN sentencias_ejecutables END
        {
            String mangling = obtenerFuncionActualMangled();
            if (cant_retornos_auto == 0) {
                yyerror("Ausencia de sentencia de retorno 'RET' en funcion AUTO");
            }
            if (mangling != null) {
                GeneradorTerceto.crearTerceto("FIN_FUNCION", mangling, "-");
            }
            cant_retornos_auto = -1;
            yyerror("Falta ';' al final de la declaracion de funcion");
            salirAmbito();
        } 
    ;

lista_parametros_formales
    : /* vacio */
    | parametro_formal
    | lista_parametros_formales ',' parametro_formal
    ;

parametro_formal
    : tipo IDENTIFICADOR
        {
            int ref = $2.ival;
            String nombre = TablaSimbolos.obtenerAtributo(ref, TablaSimbolos.LEXEMA);
            TablaSimbolos.declararVariable(nombre, $1.sval, "parametro", ambitoActual);
            String funcMangled = obtenerFuncionActualMangled();
            if (funcMangled != null) {
                String nombreParamMangled = nombre + ambitoActual;
                TablaSimbolos.agregarParametroAFuncion(funcMangled, $1.sval, nombreParamMangled);
            }
            imprimirRegla("parametro_formal");
        }
    | tipo error { yyerror("Falta de nombre de parametro formal en declaracion de funcion"); }
    | error IDENTIFICADOR{ yyerror("Falta de tipo del parametro formal en declaracion de funcion"); } 
    ;
    

/* CLASES (Temas 26, 29, 30) */

encabezado_clase
    : CLASS IDENTIFICADOR
        {
            int ref = $2.ival;
            String nombre = TablaSimbolos.obtenerAtributo(ref, TablaSimbolos.LEXEMA);
            TablaSimbolos.declararVariable(nombre, "clase", "nombre_clase", ambitoActual);
            entrarAmbito(nombre);
            $$ = $2;
        }
    ;

declaracion_clase
    : encabezado_clase importacion_opcional
        BEGIN herencia_opcional miembros_clase END ';'
        {
            salirAmbito();
        }
    | encabezado_clase importacion_opcional
        BEGIN herencia_opcional miembros_clase END
        {
            yyerror("Falta ';' al final de la declaracion de clase");
            salirAmbito();
        }
    ;

importacion_opcional
    : /* vacio */
    | IMPORT FROM lista_identificadores { imprimirRegla("importacion_opcional"); }
    | IMPORT lista_identificadores
        { yyerror("Falta la palabra clave 'FROM' en la declaracion IMPORT"); }
    ;

herencia_opcional
    : /* vacio */
    | EXTENDS lista_identificadores ';' { imprimirRegla("herencia_opcional"); }
    | EXTENDS ';'
        { yyerror("Falta nombre o lista de clases a heredar luego de 'EXTENDS'"); }
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
        {
            String tipoDato = $1.sval;
            ArrayList<String> variables = (ArrayList<String>) $2.obj;
            if (variables != null) {
                for (int i = 0; i < variables.size(); i++) {
                    String nombreVar = variables.get(i);
                    TablaSimbolos.declararVariable(nombreVar, tipoDato, "atributo", ambitoActual);
                }
            }
        }
    ;

metodo_clase
    : encabezado_metodo '(' lista_parametros_formales ')'
        sentencias_declarativas
        BEGIN sentencias_ejecutables END exportacion_opcional ';'
        {
            salirAmbito();
        }
    | encabezado_metodo_auto '(' lista_parametros_formales ')'
        BEGIN sentencias_ejecutables END exportacion_opcional ';'
        {
            salirAmbito();
        }
    ;

encabezado_metodo
    : tipo IDENTIFICADOR
        {
            int ref = $2.ival;
            String nombre = TablaSimbolos.obtenerAtributo(ref, TablaSimbolos.LEXEMA);
            TablaSimbolos.declararVariable(nombre, $1.sval, "metodo", ambitoActual);
            entrarAmbito(nombre);
            $$ = $2;
        }
    ;

encabezado_metodo_auto
    : AUTO IDENTIFICADOR
        {
            int ref = $2.ival;
            String nombre = TablaSimbolos.obtenerAtributo(ref, TablaSimbolos.LEXEMA);
            TablaSimbolos.declararVariable(nombre, "auto", "metodo", ambitoActual);
            entrarAmbito(nombre);
            $$ = $2;
        }
    ;

exportacion_opcional
    : /* vacio */
    | EXPORT TO lista_identificadores { imprimirRegla("exportacion_opcional"); }
    | EXPORT lista_identificadores
        { yyerror("Falta la palabra clave 'TO' en la declaracion EXPORT"); }
    ;

/* OBJETOS */

declaracion_objeto
    : IDENTIFICADOR lista_identificadores ';' { imprimirRegla("declaracion_de_objeto"); }
    | IDENTIFICADOR lista_identificadores
        { yyerror("Falta ';' al final de la declaracion de objeto"); }
    ;

/* COMPTIME (Tema 22) */

declaracion_comptime
    : COMPTIME tipo lista_identificadores ';'
        {
            String tipoDato = $2.sval;
            ArrayList<String> variables = (ArrayList<String>) $3.obj;
            if (variables != null) {
                for (int i = 0; i < variables.size(); i++) {
                    String nombreVar = variables.get(i);
                    int id = TablaSimbolos.declararVariable(nombreVar, tipoDato, "variable", ambitoActual);
                    if (id != TablaSimbolos.LEXEMA_NO_ENCONTRADO) {
                        TablaSimbolos.agregarAtributo(id, "COMPTIME", "true");
                    }
                }
            }
        }
    | COMPTIME tipo lista_identificadores
        { yyerror("Falta ';' al final de la declaracion comptime"); }
    | COMPTIME lista_identificadores ';'
        { yyerror("Falta el tipo de dato en la declaracion comptime"); }
    | COMPTIME lista_identificadores
        {
            yyerror("Falta el tipo de dato en la declaracion comptime");
            yyerror("Falta ';' al final de la declaracion comptime");
        }
    ;

/* SENTENCIAS EJECUTABLES                                                    */

sentencias_ejecutables
    : /* vacio */
    | sentencias_ejecutables sentencia_ejecutable { imprimirRegla("sentencia_ejecutable"); }
    ;

sentencia_ejecutable
    : asignacion ';'
    | expresion ';'
    | sentencia_if
    | sentencia_repeat_until
    | sentencia_pout ';'
    | sentencia_ret ';'
    | error ';'
        { yyerror("Sentencia ejecutable malformada o falta ';' previo"); }
    ;

asignacion
    : IDENTIFICADOR ASIGNAR expresion
        {
            if ($3 != null && $3.sval != null) {
                int ref = $1.ival;
                String lexema = TablaSimbolos.obtenerAtributo(ref, TablaSimbolos.LEXEMA);
                int idVar = TablaSimbolos.buscarIdentificador(lexema, ambitoActual);
                if (idVar != TablaSimbolos.LEXEMA_NO_ENCONTRADO) {
                    String mangling = TablaSimbolos.obtenerAtributo(idVar, TablaSimbolos.MANGLING);
                    String tipoVar = TablaSimbolos.obtenerAtributo(idVar, TablaSimbolos.TIPO);
                    String tipoExp = (String) $3.obj;
                    if (tipoExp != null) {
                        if (tipoVar != null && tipoVar.equals(tipoExp)) {
                            GeneradorTerceto.crearTerceto(":=", mangling, $3.sval, tipoVar);
                        } else if ("SHORTINT".equals(tipoVar) && "SINGLEF".equals(tipoExp)) {
                            agregarErrorSemantico("Incompatibilidad de tipos en la asignacion: no se puede asignar SINGLEF a la variable SHORTINT '" + lexema + "' sin conversion explicita 'tos'");
                        } else if ("SINGLEF".equals(tipoVar) && "SHORTINT".equals(tipoExp)) {
                            agregarErrorSemantico("Incompatibilidad de tipos en la asignacion: no se puede asignar SHORTINT a la variable SINGLEF '" + lexema + "'");
                        } else {
                            agregarErrorSemantico("Incompatibilidad de tipos en la asignacion a '" + lexema + "': se esperaba " + tipoVar + " pero se encontro " + tipoExp);
                        }
                    }
                } else {
                    agregarErrorSemantico("Variable '" + lexema + "' no declarada o no visible en el ambito actual (" + ambitoActual + ")");
                }
            }
        }
    | acceso_posicional ASIGNAR expresion
    ;

acceso_posicional
    : IDENTIFICADOR '[' CTE_SHORTINT ']'
    | IDENTIFICADOR '[' IDENTIFICADOR ']'
    | IDENTIFICADOR '[' CTE_SINGLEF ']'
        { yyerror("Acceso posicional invalido: solo se permite constante SHORTINT positiva o identificador"); $$ = new ParserVal(); }
    ;

/* EXPRESIONES                                                               */
expresion
    : termino
        {
            $$ = $1;
        }
    | expresion '+' termino
        {
            if ($1 != null && $1.sval != null && $3 != null && $3.sval != null) {
                String tipo1 = (String) $1.obj;
                String tipo2 = (String) $3.obj;
                if (tipo1 != null && tipo2 != null) {
                    if (tipo1.equals(tipo2)) {
                        int idTerceto = GeneradorTerceto.crearTerceto("+", $1.sval, $3.sval, tipo1);
                        ParserVal pv = new ParserVal("[" + idTerceto + "]");
                        pv.obj = tipo1;
                        $$ = pv;
                    } else {
                        agregarErrorSemantico("Incompatibilidad de tipos en operacion '+': no se puede operar " + tipo1 + " con " + tipo2 + " (requiere conversion explicita 'tos')");
                        $$ = new ParserVal();
                    }
                } else {
                    $$ = new ParserVal();
                }
            } else {
                $$ = new ParserVal();
            }
        }
    | expresion '-' termino
        {
            if ($1 != null && $1.sval != null && $3 != null && $3.sval != null) {
                String tipo1 = (String) $1.obj;
                String tipo2 = (String) $3.obj;
                if (tipo1 != null && tipo2 != null) {
                    if (tipo1.equals(tipo2)) {
                        int idTerceto = GeneradorTerceto.crearTerceto("-", $1.sval, $3.sval, tipo1);
                        ParserVal pv = new ParserVal("[" + idTerceto + "]");
                        pv.obj = tipo1;
                        $$ = pv;
                    } else {
                        agregarErrorSemantico("Incompatibilidad de tipos en operacion '-': no se puede operar " + tipo1 + " con " + tipo2 + " (requiere conversion explicita 'tos')");
                        $$ = new ParserVal();
                    }
                } else {
                    $$ = new ParserVal();
                }
            } else {
                $$ = new ParserVal();
            }
        }
    | expresion '+' error { yyerror("Falta un operando en la expresion"); $$ = new ParserVal(); }
    | expresion '-' error { yyerror("Falta un operando en la expresion"); $$ = new ParserVal(); }
    | '+' termino { yyerror("Falta un operando en la expresion"); $$ = new ParserVal(); }
    ;
termino
    : factor
        {
            $$ = $1;
        }
    | termino '*' factor
        {
            if ($1 != null && $1.sval != null && $3 != null && $3.sval != null) {
                String tipo1 = (String) $1.obj;
                String tipo2 = (String) $3.obj;
                if (tipo1 != null && tipo2 != null) {
                    if (tipo1.equals(tipo2)) {
                        int idTerceto = GeneradorTerceto.crearTerceto("*", $1.sval, $3.sval, tipo1);
                        ParserVal pv = new ParserVal("[" + idTerceto + "]");
                        pv.obj = tipo1;
                        $$ = pv;
                    } else {
                        agregarErrorSemantico("Incompatibilidad de tipos en operacion '*': no se puede operar " + tipo1 + " con " + tipo2 + " (requiere conversion explicita 'tos')");
                        $$ = new ParserVal();
                    }
                } else {
                    $$ = new ParserVal();
                }
            } else {
                $$ = new ParserVal();
            }
        }
    | termino '/' factor
        {
            if ($1 != null && $1.sval != null && $3 != null && $3.sval != null) {
                String tipo1 = (String) $1.obj;
                String tipo2 = (String) $3.obj;
                if (tipo1 != null && tipo2 != null) {
                    if (tipo1.equals(tipo2)) {
                        int idTerceto = GeneradorTerceto.crearTerceto("/", $1.sval, $3.sval, tipo1);
                        ParserVal pv = new ParserVal("[" + idTerceto + "]");
                        pv.obj = tipo1;
                        $$ = pv;
                    } else {
                        agregarErrorSemantico("Incompatibilidad de tipos en operacion '/': no se puede operar " + tipo1 + " con " + tipo2 + " (requiere conversion explicita 'tos')");
                        $$ = new ParserVal();
                    }
                } else {
                    $$ = new ParserVal();
                }
            } else {
                $$ = new ParserVal();
            }
        }
    | termino '*' error { yyerror("Falta un operando en la expresion"); $$ = new ParserVal(); }
    | termino '/' error { yyerror("Falta un operando en la expresion"); $$ = new ParserVal(); }
    | '*' factor { yyerror("Falta un operando en la expresion"); $$ = new ParserVal(); }
    | '/' factor { yyerror("Falta un operando en la expresion"); $$ = new ParserVal(); }
    | termino IDENTIFICADOR { yyerror("Falta operador en la expresion"); $$ = new ParserVal(); }
    | termino CTE_SHORTINT  { yyerror("Falta operador en la expresion"); $$ = new ParserVal(); }
    | termino CTE_SINGLEF   { yyerror("Falta operador en la expresion"); $$ = new ParserVal(); }
    ;

factor
    : IDENTIFICADOR 
        {
            int ref = $1.ival;
            String lexema = TablaSimbolos.obtenerAtributo(ref, TablaSimbolos.LEXEMA);
            int idVar = TablaSimbolos.buscarIdentificador(lexema, ambitoActual);
            if (idVar != TablaSimbolos.LEXEMA_NO_ENCONTRADO) {
                String mangling = TablaSimbolos.obtenerAtributo(idVar, TablaSimbolos.MANGLING);
                String tipo = TablaSimbolos.obtenerAtributo(idVar, TablaSimbolos.TIPO);
                ParserVal pv = new ParserVal(mangling);
                pv.ival = idVar;
                pv.obj = tipo;
                $$ = pv;
            } else {
                agregarErrorSemantico("Variable '" + lexema + "' no declarada o no visible en el ambito actual (" + ambitoActual + ")");
                $$ = new ParserVal();
            }
        }
    | CTE_SHORTINT
        {
            int id = $1.ival;
            String lexema = TablaSimbolos.obtenerAtributo(id, TablaSimbolos.LEXEMA);
            int valor = Integer.parseInt(lexema);
            if (valor > AnalizadorLexico.ValorMaximoInt) {
                yyerror("Constante shortint positiva fuera de rango (" + lexema + "). Rango permitido: [-128, 127]");
            }
            TablaSimbolos.agregarAtributo(id, TablaSimbolos.TIPO, "SHORTINT");
            ParserVal pv = new ParserVal(lexema);
            pv.ival = id;
            pv.obj = "SHORTINT";
            $$ = pv;
        }
    | CTE_SINGLEF
        {
            int id = $1.ival;
            String lexema = TablaSimbolos.obtenerAtributo(id, TablaSimbolos.LEXEMA);
            double valor = Double.parseDouble(lexema);
            if (valor != 0.0 && (valor < AnalizadorLexico.ValorMinimoFloat || valor > AnalizadorLexico.ValorMaximoFloat)) {
                yyerror("Constante singlef fuera de rango (" + lexema + ")");
            }
            TablaSimbolos.agregarAtributo(id, TablaSimbolos.TIPO, "SINGLEF");
            ParserVal pv = new ParserVal(lexema);
            pv.ival = id;
            pv.obj = "SINGLEF";
            $$ = pv;
        }
    | IDENTIFICADOR '(' ')'
        {
            int ref = $1.ival;
            String lexema = TablaSimbolos.obtenerAtributo(ref, TablaSimbolos.LEXEMA);
            int idFunc = TablaSimbolos.buscarIdentificador(lexema, ambitoActual);
            if (idFunc == TablaSimbolos.LEXEMA_NO_ENCONTRADO) {
                agregarErrorSemantico("Funcion '" + lexema + "' no declarada o no visible en el ambito actual (" + ambitoActual + ")");
                $$ = new ParserVal();
            } else {
                String uso = TablaSimbolos.obtenerAtributo(idFunc, TablaSimbolos.USO);
                if (!"funcion".equals(uso)) {
                    agregarErrorSemantico("El identificador '" + lexema + "' no es una funcion (uso actual: " + uso + ")");
                    $$ = new ParserVal();
                } else {
                    String funcMangled = TablaSimbolos.obtenerAtributo(idFunc, TablaSimbolos.MANGLING);
                    String tipoRetorno = TablaSimbolos.obtenerAtributo(idFunc, TablaSimbolos.TIPO);
                    int cantEsperada = TablaSimbolos.obtenerCantidadParametros(funcMangled);

                    if (cantEsperada != 0) {
                        agregarErrorSemantico("Cantidad incorrecta de parametros en invocacion a '" + lexema + "': se esperaban " + cantEsperada + " pero se recibieron 0");
                        $$ = new ParserVal();
                    } else {
                        int idCall = GeneradorTerceto.crearTerceto("CALL", funcMangled, "-", tipoRetorno);
                        ParserVal pv = new ParserVal("[" + idCall + "]");
                        pv.obj = tipoRetorno;
                        $$ = pv;
                    }
                }
            }
        }
    | IDENTIFICADOR '(' lista_expresiones ')'
        {
            int ref = $1.ival;
            String lexema = TablaSimbolos.obtenerAtributo(ref, TablaSimbolos.LEXEMA);
            int idFunc = TablaSimbolos.buscarIdentificador(lexema, ambitoActual);
            if (idFunc == TablaSimbolos.LEXEMA_NO_ENCONTRADO) {
                agregarErrorSemantico("Funcion '" + lexema + "' no declarada o no visible en el ambito actual (" + ambitoActual + ")");
                $$ = new ParserVal();
            } else {
                String uso = TablaSimbolos.obtenerAtributo(idFunc, TablaSimbolos.USO);
                if (!"funcion".equals(uso)) {
                    agregarErrorSemantico("El identificador '" + lexema + "' no es una funcion (uso actual: " + uso + ")");
                    $$ = new ParserVal();
                } else {
                    String funcMangled = TablaSimbolos.obtenerAtributo(idFunc, TablaSimbolos.MANGLING);
                    String tipoRetorno = TablaSimbolos.obtenerAtributo(idFunc, TablaSimbolos.TIPO);
                    int cantEsperada = TablaSimbolos.obtenerCantidadParametros(funcMangled);
                    ArrayList<ParserVal> paramsReales = ($3 != null && $3.obj != null) ? (ArrayList<ParserVal>) $3.obj : new ArrayList<>();
                    int cantRecibida = paramsReales.size();

                    if (cantRecibida != cantEsperada) {
                        agregarErrorSemantico("Cantidad incorrecta de parametros en invocacion a '" + lexema + "': se esperaban " + cantEsperada + " pero se recibieron " + cantRecibida);
                        $$ = new ParserVal();
                    } else {
                        boolean paramsOk = true;
                        for (int i = 0; i < cantEsperada; i++) {
                            String tipoFormal = TablaSimbolos.obtenerTipoParametro(funcMangled, i);
                            String nomFormal = TablaSimbolos.obtenerNombreParametro(funcMangled, i);
                            ParserVal pReal = paramsReales.get(i);
                            String tipoReal = (pReal != null) ? (String) pReal.obj : null;

                            if (tipoFormal != null && tipoReal != null && !tipoFormal.equals(tipoReal)) {
                                agregarErrorSemantico("Incompatibilidad de tipos en parametro " + (i + 1) + " al invocar a '" + lexema + "': se esperaba " + tipoFormal + " pero se recibio " + tipoReal);
                                paramsOk = false;
                            } else if (pReal != null && pReal.sval != null) {
                                GeneradorTerceto.crearTerceto(":=", nomFormal, pReal.sval, tipoFormal);
                            }
                        }

                        if (paramsOk) {
                            int idCall = GeneradorTerceto.crearTerceto("CALL", funcMangled, "-", tipoRetorno);
                            ParserVal pv = new ParserVal("[" + idCall + "]");
                            pv.obj = tipoRetorno;
                            $$ = pv;
                        } else {
                            $$ = new ParserVal();
                        }
                    }
                }
            }
        }
    | invocacion_metodo
    | unica
        {
            $$ = $1;
        }
    | numero_negativo
        {
            $$ = $1;
        }
    | conversion_tos
        {
            $$ = $1;
        }
    | acceso_posicional
    ;

invocacion_metodo
    : IDENTIFICADOR '.' IDENTIFICADOR '(' ')' { imprimirRegla("invocacion_metodo"); }
    | IDENTIFICADOR '.' IDENTIFICADOR '(' lista_expresiones ')' { imprimirRegla("invocacion_metodo"); }
    ;

lista_expresiones
    : expresion
        {
            ArrayList<ParserVal> lista = new ArrayList<>();
            lista.add($1);
            ParserVal pv = new ParserVal();
            pv.obj = lista;
            $$ = pv;
        }
    | lista_expresiones ',' expresion
        {
            ArrayList<ParserVal> lista = (ArrayList<ParserVal>) $1.obj;
            lista.add($3);
            $$ = $1;
        }
    ;

/* Tema 17: Asignacion en expresion */
unica
    : IDENTIFICADOR '=' '(' expresion ')'
        {
            if ($4 != null && $4.sval != null) {
                int ref = $1.ival;
                String lexema = TablaSimbolos.obtenerAtributo(ref, TablaSimbolos.LEXEMA);
                int idVar = TablaSimbolos.buscarIdentificador(lexema, ambitoActual);
                if (idVar != TablaSimbolos.LEXEMA_NO_ENCONTRADO) {
                    String mangling = TablaSimbolos.obtenerAtributo(idVar, TablaSimbolos.MANGLING);
                    String tipoVar = TablaSimbolos.obtenerAtributo(idVar, TablaSimbolos.TIPO);
                    String tipoExp = (String) $4.obj;
                    if (tipoExp != null) {
                        if (tipoVar != null && tipoVar.equals(tipoExp)) {
                            int idTerceto = GeneradorTerceto.crearTerceto(":=", mangling, $4.sval, tipoVar);
                            ParserVal pv = new ParserVal("[" + idTerceto + "]");
                            pv.obj = tipoVar;
                            $$ = pv;
                        } else if ("SHORTINT".equals(tipoVar) && "SINGLEF".equals(tipoExp)) {
                            agregarErrorSemantico("Incompatibilidad de tipos en la asignacion: no se puede asignar SINGLEF a la variable SHORTINT '" + lexema + "' sin conversion explicita 'tos'");
                            $$ = new ParserVal();
                        } else if ("SINGLEF".equals(tipoVar) && "SHORTINT".equals(tipoExp)) {
                            agregarErrorSemantico("Incompatibilidad de tipos en la asignacion: no se puede asignar SHORTINT a la variable SINGLEF '" + lexema + "'");
                            $$ = new ParserVal();
                        } else {
                            agregarErrorSemantico("Incompatibilidad de tipos en la asignacion a '" + lexema + "': se esperaba " + tipoVar + " pero se encontro " + tipoExp);
                            $$ = new ParserVal();
                        }
                    } else {
                        $$ = new ParserVal();
                    }
                } else {
                    agregarErrorSemantico("Variable '" + lexema + "' no declarada o no visible en el ambito actual (" + ambitoActual + ")");
                    $$ = new ParserVal();
                }
            } else {
                $$ = new ParserVal();
            }
        }
    | IDENTIFICADOR ASIGNAR '(' expresion ')'
        {
            yyerror("Uso del simbolo ':=' donde debe usarse '=' en asignacion dentro de expresion");
            $$ = new ParserVal();
        }
    ;

numero_negativo
    : '-' CTE_SHORTINT
        {
            int id_pos = $2.ival;
            String lexema_pos = TablaSimbolos.obtenerAtributo(id_pos, TablaSimbolos.LEXEMA);
            int id_neg = TablaSimbolos.convertirANegativo(lexema_pos);
            TablaSimbolos.agregarAtributo(id_neg, TablaSimbolos.TIPO, "SHORTINT");
            ParserVal pv = new ParserVal("-" + lexema_pos);
            pv.ival = id_neg;
            pv.obj = "SHORTINT";
            $$ = pv;
        }
    | '-' CTE_SINGLEF
        {
            int id_pos = $2.ival;
            String lexema_pos = TablaSimbolos.obtenerAtributo(id_pos, TablaSimbolos.LEXEMA);
            int id_neg = TablaSimbolos.convertirANegativo(lexema_pos);
            TablaSimbolos.agregarAtributo(id_neg, TablaSimbolos.TIPO, "SINGLEF");
            ParserVal pv = new ParserVal("-" + lexema_pos);
            pv.ival = id_neg;
            pv.obj = "SINGLEF";
            $$ = pv;
        }
    ;

/* Tema 34: Conversion explicita de flotante a entero corto */
conversion_tos
    : TOS '(' expresion ')'
        {
            if ($3 != null && $3.sval != null) {
                String tipoExp = (String) $3.obj;
                if ("SINGLEF".equals(tipoExp)) {
                    int idTerceto = GeneradorTerceto.crearTerceto("tos", $3.sval, "-", "SHORTINT");
                    ParserVal pv = new ParserVal("[" + idTerceto + "]");
                    pv.obj = "SHORTINT";
                    $$ = pv;
                } else if (tipoExp != null) {
                    agregarErrorSemantico("La funcion de conversion 'tos' requiere una expresion de tipo SINGLEF (se recibio: " + tipoExp + ")");
                    $$ = new ParserVal();
                } else {
                    $$ = new ParserVal();
                }
            } else {
                $$ = new ParserVal();
            }
        }
    ;

/* SENTENCIAS DE CONTROL                                                     */
encabezado_if
    : IF '(' condicion ')'
        {
            if ($3 != null && $3.sval != null) {
                int idBF = GeneradorTerceto.crearTerceto("BF", $3.sval, "?");
                pilaSaltos.push(idBF);
            }
        }
    ;

sentencia_if
    : encabezado_if bloque resto_if
    | IF condicion ')' bloque resto_if
        { yyerror("Falta '(' de apertura en condicion de seleccion"); }
    | IF error bloque resto_if
        { yyerror("Condicion de seleccion malformada o error en parentesis"); }
    ;

inicio_else
    : ELSE
        {
            if (!pilaSaltos.isEmpty()) {
                int idBF = pilaSaltos.pop();
                int idBI = GeneradorTerceto.crearTerceto("BI", "?", "-");
                GeneradorTerceto.completarSalto(idBF, GeneradorTerceto.getProximoIndice());
                pilaSaltos.push(idBI);
            }
        }
    ;

resto_if
    : END_IF ';'
        {
            if (!pilaSaltos.isEmpty()) {
                int idSalto = pilaSaltos.pop();
                GeneradorTerceto.completarSalto(idSalto, GeneradorTerceto.getProximoIndice());
            }
        }
    | inicio_else bloque END_IF ';'
        {
            if (!pilaSaltos.isEmpty()) {
                int idBI = pilaSaltos.pop();
                GeneradorTerceto.completarSalto(idBI, GeneradorTerceto.getProximoIndice());
            }
        }
    | ';'
        {
            if (!pilaSaltos.isEmpty()) {
                int idSalto = pilaSaltos.pop();
                GeneradorTerceto.completarSalto(idSalto, GeneradorTerceto.getProximoIndice());
            }
            yyerror("Falta palabra clave END_IF al final de la sentencia IF");
        }
    | inicio_else bloque ';'
        {
            if (!pilaSaltos.isEmpty()) {
                int idBI = pilaSaltos.pop();
                GeneradorTerceto.completarSalto(idBI, GeneradorTerceto.getProximoIndice());
            }
            yyerror("Falta palabra clave END_IF al final de la sentencia IF");
        }
    ;

condicion
    : expresion comparador expresion
        {
            if ($1 != null && $1.sval != null && $3 != null && $3.sval != null) {
                String tipo1 = (String) $1.obj;
                String tipo2 = (String) $3.obj;
                if (tipo1 != null && tipo2 != null) {
                    if (tipo1.equals(tipo2)) {
                        int idTerceto = GeneradorTerceto.crearTerceto($2.sval, $1.sval, $3.sval);
                        $$ = new ParserVal("[" + idTerceto + "]");
                    } else {
                        agregarErrorSemantico("Incompatibilidad de tipos en comparacion: no se puede comparar " + tipo1 + " con " + tipo2 + " (requiere conversion explicita 'tos')");
                        $$ = new ParserVal();
                    }
                } else {
                    $$ = new ParserVal();
                }
            } else {
                $$ = new ParserVal();
            }
        }
    ;

comparador
    : MAYORIGUAL { $$ = new ParserVal(">="); }
    | MENORIGUAL { $$ = new ParserVal("<="); }
    | IGUALIGUAL { $$ = new ParserVal("=="); }
    | DISTINTO   { $$ = new ParserVal("!="); }
    | '>'        { $$ = new ParserVal(">"); }
    | '<'        { $$ = new ParserVal("<"); }
    ;

bloque
    : BEGIN sentencias_ejecutables END
    | sentencia_ejecutable
    | BEGIN sentencias_ejecutables
        { yyerror("Falta el delimitador END en el bloque de sentencias"); }
    ;

/* Tema 12: Repeat-Until */
inicio_repeat
    : REPEAT
        {
            pilaSaltos.push(GeneradorTerceto.getProximoIndice());
        }
    ;

sentencia_repeat_until
    : inicio_repeat bloque UNTIL condicion_iteracion ';'
        {
            if (!pilaSaltos.isEmpty()) {
                int dirInicio = pilaSaltos.pop();
                if ($4 != null && $4.sval != null) {
                    GeneradorTerceto.crearTerceto("BF", $4.sval, "[" + dirInicio + "]");
                }
            }
        }
    | inicio_repeat UNTIL condicion_iteracion ';'
        {
            if (!pilaSaltos.isEmpty()) pilaSaltos.pop();
            yyerror("Falta el cuerpo de la iteracion REPEAT");
        }
    | inicio_repeat bloque '(' condicion ')' ';'
        {
            if (!pilaSaltos.isEmpty()) pilaSaltos.pop();
            yyerror("Falta palabra clave UNTIL en la iteracion REPEAT");
        }
    | inicio_repeat '(' condicion ')' ';'
        {
            if (!pilaSaltos.isEmpty()) pilaSaltos.pop();
            yyerror("Falta palabra clave UNTIL y cuerpo en la iteracion REPEAT");
        }
    | inicio_repeat bloque '(' condicion ';'
        {
            if (!pilaSaltos.isEmpty()) pilaSaltos.pop();
            yyerror("Falta palabra clave UNTIL y ')' en la iteracion REPEAT");
        }
    | inicio_repeat '(' condicion ';'
        {
            if (!pilaSaltos.isEmpty()) pilaSaltos.pop();
            yyerror("Falta palabra clave UNTIL, cuerpo y ')' en la iteracion REPEAT");
        }
    ;

condicion_iteracion
    : '(' condicion ')'
        {
            $$ = $2;
        }
    | condicion ')'
        {
            $$ = $1;
            yyerror("Falta '(' de apertura en condicion de iteracion");
        }
    | '(' condicion
        {
            $$ = $2;
            yyerror("Falta ')' de cierre en condicion de iteracion");
        }
    | condicion
        {
            $$ = $1;
            yyerror("Faltan parentesis en condicion de iteracion");
        }
    ;
/* ENTRADA / SALIDA                                                          */

sentencia_pout
    : POUT '(' CADENA ')'
        {
            int ref = $3.ival;
            String lexema = TablaSimbolos.obtenerAtributo(ref, TablaSimbolos.LEXEMA);
            GeneradorTerceto.crearTerceto("POUT", lexema, "-");
        }
    | POUT '(' expresion ')'
        {
            if ($3 != null && $3.sval != null) {
                GeneradorTerceto.crearTerceto("POUT", $3.sval, "-", (String) $3.obj);
            }
        }
    | POUT '(' ')'
        { yyerror("Falta argumento en sentencia pout"); }
    ;

sentencia_ret
    : RET '(' expresion ')'
        {
            String funcionActual = obtenerFuncionActualMangled();
            if (funcionActual == null) {
                agregarErrorSemantico("Sentencia RET fuera del cuerpo de una funcion");
            } else {
                int idFunc = TablaSimbolos.buscarPorMangling(funcionActual);
                String tipoDeclarado = TablaSimbolos.obtenerAtributo(idFunc, TablaSimbolos.TIPO);
                String tipoExp = ($3 != null) ? (String) $3.obj : null;
                TablaSimbolos.agregarAtributo(idFunc, "TIENE_RET", "true");

                if (cant_retornos_auto >= 0) {
                    cant_retornos_auto++;
                    if ("auto".equals(tipoDeclarado)) {
                        TablaSimbolos.agregarAtributo(idFunc, TablaSimbolos.TIPO, tipoExp);
                    } else if (tipoExp != null && !tipoDeclarado.equals(tipoExp)) {
                        TablaSimbolos.agregarAtributo(idFunc, TablaSimbolos.TIPO, "SINGLEF");
                    }
                } else {
                    if (tipoDeclarado != null && tipoExp != null && !tipoDeclarado.equals(tipoExp)) {
                        agregarErrorSemantico("Tipo de retorno incompatible en funcion '" + 
                            TablaSimbolos.obtenerAtributo(idFunc, TablaSimbolos.LEXEMA) + 
                            "': se esperaba " + tipoDeclarado + " pero se retorno " + tipoExp);
                    }
                }

                if ($3 != null && $3.sval != null) {
                    GeneradorTerceto.crearTerceto("RET", $3.sval, "-", tipoExp);
                }
            }
        }
    ;

%%

/* CODIGO DE SOPORTE                                                         */

static Parser parser;

static int cant_errores = 0;
static int cant_errores_semanticos = 0;
static int cant_retornos_auto = -1;

public static String ambitoActual = ":main";
public static Stack<Integer> pilaSaltos = new Stack<>();

public static String obtenerFuncionActualMangled() {
    if (ambitoActual == null || ambitoActual.equals(":main")) {
        return null;
    }

    int index = ambitoActual.lastIndexOf(':');
    String nombreFuncion = ambitoActual.substring(index + 1);
    String ambitoPadre = ambitoActual.substring(0, index);
    return nombreFuncion + ambitoPadre;
}

public static void entrarAmbito(String nombre) {
    ambitoActual = ambitoActual + ":" + nombre;
}

public static void salirAmbito() {
    int index = ambitoActual.lastIndexOf(':');
    if (index > 0) {
        ambitoActual = ambitoActual.substring(0, index);
    } else {
        ambitoActual = ":main";
    }
}

public static void agregarErrorSemantico(String s) {
    cant_errores_semanticos++;
    System.out.println("Error semantico (linea " + AnalizadorLexico.getLineaActual() + "): " + s);
}

// Muestra por pantalla la regla reconocida y la linea donde termino (al reducir)
static void imprimirRegla(String regla) {
    System.out.println("Regla " + regla + " en linea " + AnalizadorLexico.getLineaActual());
}

public static void main(String[] args) {
    boolean argumentosValidos = args.length == 1 || (args.length == 2 && args[1].equals("-v"));
    if (!argumentosValidos) {
        System.out.println("Uso: java -jar compilador.jar <archivo_a_compilar> [-v]");
        System.out.println("Se compila un solo archivo por ejecucion.");
        System.exit(1);
    }

    String ruta = args[0];
    boolean debug = args.length == 2;

    if (!new File(ruta).isFile()) {
        System.out.println("No se encontro el archivo a compilar: " + ruta);
        System.exit(1);
    }

    System.out.println("Compilando archivo: " + ruta);
    try {
        ambitoActual = ":main";
        pilaSaltos.clear();
        cant_retornos_auto = -1;
        cant_errores = 0;
        cant_errores_semanticos = 0;
        AnalizadorLexico.reader = new PushbackReader(new BufferedReader(
                new InputStreamReader(new FileInputStream(ruta), java.nio.charset.StandardCharsets.UTF_8)));
        parser = new Parser(debug);
        parser.yyparse(); 
        TablaSimbolos.imprimirTabla();
        GeneradorTerceto.imprimirTercetos();
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
    if (s.equals("syntax error")) {
        return; // Se omite el mensaje generico de byacc/j para usar las descripciones especificas
    }
    cant_errores++;
    System.out.println("Error sintactico (linea " + AnalizadorLexico.getLineaActual() + "): " + s);
}