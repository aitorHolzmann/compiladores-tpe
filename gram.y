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
            String nombre = $1.sval;
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
            lista.add($1.sval);
            ParserVal pv = new ParserVal();
            pv.obj = lista;
            $$ = pv;
        }
    | lista_variables ',' IDENTIFICADOR
        {
            ArrayList<String> lista = (ArrayList<String>) $1.obj;
            lista.add($3.sval);
            $$ = $1;
        }
    | lista_variables IDENTIFICADOR   { yyerror("Falta ',' entre los identificadores"); $$ = $1; }
    ;

lista_identificadores
    : IDENTIFICADOR
        {
            ArrayList<String> lista = new ArrayList<>();
            lista.add($1.sval);
            ParserVal pv = new ParserVal();
            pv.obj = lista;
            $$ = pv;
        }
    | lista_identificadores ',' IDENTIFICADOR
        {
            ArrayList<String> lista = (ArrayList<String>) $1.obj;
            lista.add($3.sval);
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
            String nombre = $3.sval;
            String claveFunc = TablaSimbolos.declararVariable(nombre, $1.sval, "funcion", ambitoActual);
            if (claveFunc != null) {
                TablaSimbolos.agregarAtributo(claveFunc, "TIENE_RET", Boolean.FALSE);
            }
            entrarAmbito(nombre);
            GeneradorTerceto.iniciarAmbito(ambitoActual);
            $$ = $3;
        }
    ;

inicio_auto_funcion
    : AUTO FUNCTION IDENTIFICADOR
        {
            String nombre = $3.sval;
            String claveFunc = TablaSimbolos.declararVariable(nombre, "auto", "funcion", ambitoActual);
            if (claveFunc != null) {
                TablaSimbolos.agregarAtributo(claveFunc, "TIENE_RET", Boolean.FALSE);
            }
            entrarAmbito(nombre);
            GeneradorTerceto.iniciarAmbito(ambitoActual);
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
                Boolean tieneRet = (Boolean) TablaSimbolos.obtenerAtributo(mangling, "TIENE_RET");
                if (!Boolean.TRUE.equals(tieneRet)) {
                    String tipo = TablaSimbolos.obtenerTipo(mangling);
                    String valDef = "SINGLEF".equals(tipo) ? "0.0" : "0$s";
                    GeneradorTerceto.crearTerceto("RET", valDef, "-", tipo);
                }
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
                Boolean tieneRet = (Boolean) TablaSimbolos.obtenerAtributo(mangling, "TIENE_RET");
                if (!Boolean.TRUE.equals(tieneRet)) {
                    String tipo = TablaSimbolos.obtenerTipo(mangling);
                    String valDef = "SINGLEF".equals(tipo) ? "0.0" : "0$s";
                    GeneradorTerceto.crearTerceto("RET", valDef, "-", tipo);
                }
            }
            yyerror("Falta ';' al final de la declaracion de funcion");
            salirAmbito();
        } 
    | encabezado_auto_funcion BEGIN sentencias_ejecutables END ';'
        {
            if (cant_retornos_auto == 0) {
                yyerror("Ausencia de sentencia de retorno 'RET' en funcion AUTO");
            }
            cant_retornos_auto = -1;
            salirAmbito();
        } 
    | AUTO FUNCTION error { yyerror("Falta nombre de funcion"); } '(' lista_parametros_formales ')'
        sentencias_declarativas
        BEGIN sentencias_ejecutables END ';' 
    | encabezado_auto_funcion BEGIN sentencias_ejecutables END
        {
            if (cant_retornos_auto == 0) {
                yyerror("Ausencia de sentencia de retorno 'RET' en funcion AUTO");
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
            String nombre = $2.sval;
            TablaSimbolos.declararVariable(nombre, $1.sval, "parametro", ambitoActual);
            String funcMangled = obtenerFuncionActualMangled();
            if (funcMangled != null) {
                TablaSimbolos.agregarParametro(funcMangled, nombre);
            }
            imprimirRegla("parametro_formal");
        }
    | IDENTIFICADOR IDENTIFICADOR
        {
            String nomClase = $1.sval;
            String claveClase = TablaSimbolos.AMBITO_GLOBAL + TablaSimbolos.SEPARADOR + nomClase;
            if (!TablaSimbolos.existeSimbolo(claveClase) || !"nombre_clase".equals(TablaSimbolos.obtenerUso(claveClase))) {
                agregarErrorSemantico("Tipo de parametro formal '" + nomClase + "' no declarado o no es una clase valida");
            }
            String nombre = $2.sval;
            TablaSimbolos.declararVariable(nombre, nomClase, "parametro", ambitoActual);
            String funcMangled = obtenerFuncionActualMangled();
            if (funcMangled != null) {
                TablaSimbolos.agregarParametro(funcMangled, nombre);
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
            String nombre = $2.sval;
            String claveClase = TablaSimbolos.declararVariable(nombre, "clase", "nombre_clase", ambitoActual);
            claseActualValida = (claveClase != null);
            entrarAmbito(nombre);
            $$ = $2;
        }
    ;

declaracion_clase
    : encabezado_clase importacion_opcional
        BEGIN herencia_opcional miembros_clase END ';'
        {
            claseActualValida = false;
            salirAmbito();
        }
    | encabezado_clase importacion_opcional
        BEGIN herencia_opcional miembros_clase END
        {
            yyerror("Falta ';' al final de la declaracion de clase");
            claseActualValida = false;
            salirAmbito();
        }
    ;

importacion_opcional
    : /* vacio */
        {
            $$ = new ParserVal();
        }
    | IMPORT FROM lista_identificadores
        {
            String nomClase = obtenerClaseDeAmbito(ambitoActual);
            if (nomClase != null) {
                ArrayList<String> imports = (ArrayList<String>) $3.obj;
                if (imports != null) {
                    ArrayList<String> importsValidos = new ArrayList<>();
                    for (int i = 0; i < imports.size(); i++) {
                        String imp = imports.get(i);
                        String claveImp = TablaSimbolos.AMBITO_GLOBAL + TablaSimbolos.SEPARADOR + imp;
                        if (!TablaSimbolos.existeSimbolo(claveImp) || !"nombre_clase".equals(TablaSimbolos.obtenerUso(claveImp))) {
                            agregarErrorSemantico("Clase '" + imp + "' especificada en IMPORT FROM no declarada");
                        } else {
                            importsValidos.add(imp);
                        }
                    }
                    if (claseActualValida) {
                        String claveClase = TablaSimbolos.AMBITO_GLOBAL + TablaSimbolos.SEPARADOR + nomClase;
                        TablaSimbolos.registrarImports(claveClase, importsValidos);
                    }
                }
            }
            imprimirRegla("importacion_opcional");
            $$ = $3;
        }
    | IMPORT lista_identificadores
        {
            yyerror("Falta la palabra clave 'FROM' en la declaracion IMPORT");
            $$ = $2;
        }
    ;

herencia_opcional
    : /* vacio */
        {
            $$ = new ParserVal();
        }
    | EXTENDS lista_identificadores ';'
        {
            String nomClase = obtenerClaseDeAmbito(ambitoActual);
            if (nomClase != null) {
                ArrayList<String> padres = (ArrayList<String>) $2.obj;
                if (padres != null) {
                    ArrayList<String> padresValidos = new ArrayList<>();
                    for (int i = 0; i < padres.size(); i++) {
                        String padre = padres.get(i);
                        String clavePadre = TablaSimbolos.AMBITO_GLOBAL + TablaSimbolos.SEPARADOR + padre;
                        if (!TablaSimbolos.existeSimbolo(clavePadre) || !"nombre_clase".equals(TablaSimbolos.obtenerUso(clavePadre))) {
                            agregarErrorSemantico("Clase base '" + padre + "' especificada en EXTENDS no declarada");
                        } else if (TablaSimbolos.hayCicloHerencia(nomClase, padre)) {
                            agregarErrorSemantico("Herencia circular detectada entre la clase '" + nomClase + "' y '" + padre + "'");
                        } else if (padresValidos.contains(padre)) {
                            agregarErrorSemantico("Clase base '" + padre + "' duplicada en EXTENDS de la clase '" + nomClase + "'");
                        } else {
                            padresValidos.add(padre);
                        }
                    }
                    if (claseActualValida) {
                        String claveClase = TablaSimbolos.AMBITO_GLOBAL + TablaSimbolos.SEPARADOR + nomClase;
                        TablaSimbolos.registrarHerencia(claveClase, padresValidos);
                    }
                }
            }
            imprimirRegla("herencia_opcional");
            $$ = $2;
        }
    | EXTENDS ';'
        {
            yyerror("Falta nombre o lista de clases a heredar luego de 'EXTENDS'");
            $$ = new ParserVal();
        }
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
            ArrayList<String> exportTo = ($3 != null && $3.obj != null) ? (ArrayList<String>) $3.obj : null;
            String nomClase = obtenerClaseDeAmbito(ambitoActual);
            String claseClave = (nomClase != null) ? TablaSimbolos.AMBITO_GLOBAL + TablaSimbolos.SEPARADOR + nomClase : null;

            if (variables != null) {
                for (int i = 0; i < variables.size(); i++) {
                    String nombreVar = variables.get(i);
                    String claveAtr = TablaSimbolos.declararVariable(nombreVar, tipoDato, "atributo", ambitoActual);
                    if (claveAtr != null) {
                        if (claseClave != null) {
                            TablaSimbolos.agregarAtributoAClase(claseClave, nombreVar);
                        }
                        if (exportTo != null) {
                            TablaSimbolos.registrarExport(claveAtr, exportTo);
                        }
                    }
                }
            }
        }
    ;

metodo_clase
    : encabezado_metodo '(' lista_parametros_formales ')'
        sentencias_declarativas
        BEGIN sentencias_ejecutables END exportacion_opcional ';'
        {
            String mangling = obtenerFuncionActualMangled();
            if (mangling != null) {
                Boolean tieneRet = (Boolean) TablaSimbolos.obtenerAtributo(mangling, "TIENE_RET");
                if (!Boolean.TRUE.equals(tieneRet)) {
                    String tipo = TablaSimbolos.obtenerTipo(mangling);
                    String valDef = "SINGLEF".equals(tipo) ? "0.0" : "0$s";
                    GeneradorTerceto.crearTerceto("RET", valDef, "-", tipo);
                }
                if ($9 != null && $9.obj != null) {
                    ArrayList<String> exports = (ArrayList<String>) $9.obj;
                    TablaSimbolos.registrarExport(mangling, exports);
                }
            }
            salirAmbito();
        }
    | encabezado_metodo_auto '(' lista_parametros_formales ')'
        BEGIN sentencias_ejecutables END exportacion_opcional ';'
        {
            String mangling = obtenerFuncionActualMangled();
            if (mangling != null) {
                if ($8 != null && $8.obj != null) {
                    ArrayList<String> exports = (ArrayList<String>) $8.obj;
                    TablaSimbolos.registrarExport(mangling, exports);
                }
            }
            salirAmbito();
        }
    ;

encabezado_metodo
    : tipo IDENTIFICADOR
        {
            String nombre = $2.sval;
            String claveMet = TablaSimbolos.declararVariable(nombre, $1.sval, "metodo", ambitoActual);
            if (claveMet != null) {
                TablaSimbolos.agregarAtributo(claveMet, "TIENE_RET", Boolean.FALSE);
            }
            entrarAmbito(nombre);
            GeneradorTerceto.iniciarAmbito(ambitoActual);
            $$ = $2;
        }
    ;

encabezado_metodo_auto
    : AUTO IDENTIFICADOR
        {
            String nombre = $2.sval;
            String claveMet = TablaSimbolos.declararVariable(nombre, "auto", "metodo", ambitoActual);
            if (claveMet != null) {
                TablaSimbolos.agregarAtributo(claveMet, "TIENE_RET", Boolean.FALSE);
            }
            entrarAmbito(nombre);
            GeneradorTerceto.iniciarAmbito(ambitoActual);
            $$ = $2;
        }
    ;

exportacion_opcional
    : /* vacio */
        {
            $$ = new ParserVal();
        }
    | EXPORT TO lista_identificadores
        {
            imprimirRegla("exportacion_opcional");
            $$ = $3;
        }
    | EXPORT lista_identificadores
        {
            yyerror("Falta la palabra clave 'TO' en la declaracion EXPORT");
            $$ = $2;
        }
    ;

/* OBJETOS */

declaracion_objeto
    : IDENTIFICADOR lista_identificadores ';'
        {
            String nomClase = $1.sval;
            String claveClase = TablaSimbolos.AMBITO_GLOBAL + TablaSimbolos.SEPARADOR + nomClase;
            if (!TablaSimbolos.existeSimbolo(claveClase) || !"nombre_clase".equals(TablaSimbolos.obtenerUso(claveClase))) {
                agregarErrorSemantico("Clase '" + nomClase + "' no declarada o no es una clase valida");
            } else {
                ArrayList<String> variables = (ArrayList<String>) $2.obj;
                if (variables != null) {
                    for (int i = 0; i < variables.size(); i++) {
                        String nombreObj = variables.get(i);
                        TablaSimbolos.declararVariable(nombreObj, nomClase, "objeto", ambitoActual);
                    }
                }
            }
            imprimirRegla("declaracion_de_objeto");
        }
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
                    String clave = TablaSimbolos.declararVariable(nombreVar, tipoDato, "variable", ambitoActual);
                    if (clave != null) {
                        TablaSimbolos.agregarAtributo(clave, "COMPTIME", Boolean.TRUE);
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
                String lexema = $1.sval;
                String claveVar = TablaSimbolos.buscarIdentificador(lexema, ambitoActual);
                if (claveVar != null) {
                    boolean esComp = TablaSimbolos.esComptime(claveVar);
                    String tipoVar = TablaSimbolos.obtenerTipo(claveVar);
                    String tipoExp = (String) $3.obj;

                    if (esComp) {
                        if (!esConstante($3)) {
                            agregarErrorSemantico("La variable comptime '" + lexema + "' no puede recibir una expresion dependiente de tiempo de ejecucion");
                        } else {
                            if (tipoExp != null) {
                                if (tipoVar != null && tipoVar.equals(tipoExp)) {
                                    TablaSimbolos.guardarValorComptime(claveVar, $3.sval);
                                } else if ("SHORTINT".equals(tipoVar) && "SINGLEF".equals(tipoExp)) {
                                    agregarErrorSemantico("Incompatibilidad de tipos en la asignacion: no se puede asignar SINGLEF a la variable SHORTINT '" + lexema + "' sin conversion explicita 'tos'");
                                } else if ("SINGLEF".equals(tipoVar) && "SHORTINT".equals(tipoExp)) {
                                    agregarErrorSemantico("Incompatibilidad de tipos en la asignacion: no se puede asignar SHORTINT a la variable SINGLEF '" + lexema + "'");
                                } else {
                                    agregarErrorSemantico("Incompatibilidad de tipos en la asignacion a '" + lexema + "': se esperaba " + tipoVar + " pero se encontro " + tipoExp);
                                }
                            }
                        }
                    } else {
                        if (tipoExp != null) {
                            if (tipoVar != null && tipoVar.equals(tipoExp)) {
                                GeneradorTerceto.crearTerceto(":=", claveVar, $3.sval, tipoVar);
                            } else if ("SHORTINT".equals(tipoVar) && "SINGLEF".equals(tipoExp)) {
                                agregarErrorSemantico("Incompatibilidad de tipos en la asignacion: no se puede asignar SINGLEF a la variable SHORTINT '" + lexema + "' sin conversion explicita 'tos'");
                            } else if ("SINGLEF".equals(tipoVar) && "SHORTINT".equals(tipoExp)) {
                                agregarErrorSemantico("Incompatibilidad de tipos en la asignacion: no se puede asignar SHORTINT a la variable SINGLEF '" + lexema + "'");
                            } else {
                                agregarErrorSemantico("Incompatibilidad de tipos en la asignacion a '" + lexema + "': se esperaba " + tipoVar + " pero se encontro " + tipoExp);
                            }
                        }
                    }
                } else {
                    agregarErrorSemantico("Variable '" + lexema + "' no declarada o no visible en el ambito actual (" + ambitoActual + ")");
                }
            }
        }
    | acceso_posicional ASIGNAR expresion
        {
            if ($1 != null && $1.sval != null && $3 != null && $3.sval != null) {
                String tipoVar = (String) $1.obj;
                String tipoExp = (String) $3.obj;

                if (tipoExp != null && tipoVar != null) {
                    if (tipoVar.equals(tipoExp)) {
                        GeneradorTerceto.crearTerceto(":=", $1.sval, $3.sval, tipoVar);
                    } else if ("SHORTINT".equals(tipoVar) && "SINGLEF".equals(tipoExp)) {
                        agregarErrorSemantico("Incompatibilidad de tipos en la asignacion a '" + $1.sval + "': no se puede asignar SINGLEF a SHORTINT sin conversion explicita 'tos'");
                    } else if ("SINGLEF".equals(tipoVar) && "SHORTINT".equals(tipoExp)) {
                        agregarErrorSemantico("Incompatibilidad de tipos en la asignacion a '" + $1.sval + "': no se puede asignar SHORTINT a SINGLEF");
                    } else {
                        agregarErrorSemantico("Incompatibilidad de tipos en la asignacion a '" + $1.sval + "': se esperaba " + tipoVar + " pero se encontro " + tipoExp);
                    }
                }
            }
        }
    ;

acceso_posicional
    : IDENTIFICADOR '[' CTE_SHORTINT ']'
        {
            String nomObj = $1.sval;
            String claveObj = TablaSimbolos.buscarIdentificador(nomObj, ambitoActual);

            if (claveObj == null) {
                agregarErrorSemantico("Objeto '" + nomObj + "' no declarado o no visible en el ambito actual (" + ambitoActual + ")");
                $$ = new ParserVal();
            } else {
                String uso = TablaSimbolos.obtenerUso(claveObj);
                String nomClase = TablaSimbolos.obtenerTipo(claveObj);
                String claveClase = TablaSimbolos.AMBITO_GLOBAL + TablaSimbolos.SEPARADOR + nomClase;
                boolean esObjeto = ("objeto".equals(uso) || "parametro".equals(uso)) && 
                    (TablaSimbolos.existeSimbolo(claveClase) && "nombre_clase".equals(TablaSimbolos.obtenerUso(claveClase)));

                if (!esObjeto) {
                    agregarErrorSemantico("El identificador '" + nomObj + "' no es un objeto (uso actual: " + uso + ")");
                    $$ = new ParserVal();
                } else {
                    String lexConst = $3.sval;
                    int idx = Integer.parseInt(lexConst);
                    ArrayList<String> atrs = TablaSimbolos.obtenerAtributosClase(claveClase);
                    int cantAtr = atrs.size();

                    if (idx < 0 || idx >= cantAtr) {
                        agregarErrorSemantico("Indice posicional [" + idx + "$s] fuera de rango para la clase '" + nomClase + "' (cantidad de atributos: " + cantAtr + ")");
                        $$ = new ParserVal();
                    } else {
                        String nomAtr = atrs.get(idx);
                        String claveAtr = claveClase + TablaSimbolos.SEPARADOR + nomAtr;
                        String tipoAtr = TablaSimbolos.obtenerTipo(claveAtr);

                        // Chequeo de visibilidad (Tema 26)
                        String claseActual = obtenerClaseDeAmbito(ambitoActual);
                        boolean accesoValido = true;
                        if (claseActual != null && !claseActual.equals(nomClase)) {
                            String claveClaseActual = TablaSimbolos.AMBITO_GLOBAL + TablaSimbolos.SEPARADOR + claseActual;
                            ArrayList<String> imports = TablaSimbolos.obtenerImports(claveClaseActual);
                            if (imports == null || !imports.contains(nomClase)) {
                                agregarErrorSemantico("La clase '" + claseActual + "' no importa a la clase '" + nomClase + "'");
                                accesoValido = false;
                            } else if (!TablaSimbolos.estaExportadoA(claveAtr, claseActual)) {
                                agregarErrorSemantico("El atributo '" + nomAtr + "' de la clase '" + nomClase + "' no esta exportado a la clase '" + claseActual + "'");
                                accesoValido = false;
                            }
                        }

                        if (accesoValido) {
                            ParserVal pv = new ParserVal(claveObj + "[" + idx + "$s]");
                            pv.obj = tipoAtr;
                            $$ = pv;
                        } else {
                            $$ = new ParserVal();
                        }
                    }
                }
            }
        }
    | IDENTIFICADOR '[' IDENTIFICADOR ']'
        {
            String nomObj = $1.sval;
            String claveObj = TablaSimbolos.buscarIdentificador(nomObj, ambitoActual);

            if (claveObj == null) {
                agregarErrorSemantico("Objeto '" + nomObj + "' no declarado o no visible en el ambito actual (" + ambitoActual + ")");
                $$ = new ParserVal();
            } else {
                String uso = TablaSimbolos.obtenerUso(claveObj);
                String nomClase = TablaSimbolos.obtenerTipo(claveObj);
                String claveClase = TablaSimbolos.AMBITO_GLOBAL + TablaSimbolos.SEPARADOR + nomClase;
                boolean esObjeto = ("objeto".equals(uso) || "parametro".equals(uso)) && 
                    (TablaSimbolos.existeSimbolo(claveClase) && "nombre_clase".equals(TablaSimbolos.obtenerUso(claveClase)));

                if (!esObjeto) {
                    agregarErrorSemantico("El identificador '" + nomObj + "' no es un objeto (uso actual: " + uso + ")");
                    $$ = new ParserVal();
                } else {
                    String nomIdx = $3.sval;
                    String claveVarIdx = TablaSimbolos.buscarIdentificador(nomIdx, ambitoActual);

                    if (claveVarIdx == null) {
                        agregarErrorSemantico("Variable de indice '" + nomIdx + "' no declarada");
                        $$ = new ParserVal();
                    } else {
                        String tipoIdx = TablaSimbolos.obtenerTipo(claveVarIdx);
                        if (!"SHORTINT".equals(tipoIdx)) {
                            agregarErrorSemantico("El indice de acceso posicional debe ser de tipo SHORTINT (se encontro: " + tipoIdx + ")");
                            $$ = new ParserVal();
                        } else {
                            ArrayList<String> atrs = TablaSimbolos.obtenerAtributosClase(claveClase);
                            String tipoDefecto = (!atrs.isEmpty()) ? TablaSimbolos.obtenerTipo(claveClase + TablaSimbolos.SEPARADOR + atrs.get(0)) : "SHORTINT";
                            if (tipoDefecto == null) tipoDefecto = "SHORTINT";

                            ParserVal pv = new ParserVal(claveObj + "[" + claveVarIdx + "]");
                            pv.obj = tipoDefecto;
                            $$ = pv;
                        }
                    }
                }
            }
        }
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
                        if (esConstante($1) && esConstante($3)) {
                            String res = evaluarOperacionEstatica("+", $1.sval, $3.sval, tipo1);
                            ParserVal pv = new ParserVal(res);
                            pv.obj = tipo1;
                            $$ = pv;
                        } else {
                            int idTerceto = GeneradorTerceto.crearTerceto("+", $1.sval, $3.sval, tipo1);
                            ParserVal pv = new ParserVal("[" + idTerceto + "]");
                            pv.obj = tipo1;
                            $$ = pv;
                        }
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
                        if (esConstante($1) && esConstante($3)) {
                            String res = evaluarOperacionEstatica("-", $1.sval, $3.sval, tipo1);
                            ParserVal pv = new ParserVal(res);
                            pv.obj = tipo1;
                            $$ = pv;
                        } else {
                            int idTerceto = GeneradorTerceto.crearTerceto("-", $1.sval, $3.sval, tipo1);
                            ParserVal pv = new ParserVal("[" + idTerceto + "]");
                            pv.obj = tipo1;
                            $$ = pv;
                        }
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
                        if (esConstante($1) && esConstante($3)) {
                            String res = evaluarOperacionEstatica("*", $1.sval, $3.sval, tipo1);
                            ParserVal pv = new ParserVal(res);
                            pv.obj = tipo1;
                            $$ = pv;
                        } else {
                            int idTerceto = GeneradorTerceto.crearTerceto("*", $1.sval, $3.sval, tipo1);
                            ParserVal pv = new ParserVal("[" + idTerceto + "]");
                            pv.obj = tipo1;
                            $$ = pv;
                        }
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
                        if (esConstante($1) && esConstante($3)) {
                            String res = evaluarOperacionEstatica("/", $1.sval, $3.sval, tipo1);
                            ParserVal pv = new ParserVal(res);
                            pv.obj = tipo1;
                            $$ = pv;
                        } else {
                            int idTerceto = GeneradorTerceto.crearTerceto("/", $1.sval, $3.sval, tipo1);
                            ParserVal pv = new ParserVal("[" + idTerceto + "]");
                            pv.obj = tipo1;
                            $$ = pv;
                        }
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
            String lexema = $1.sval;
            String claveVar = TablaSimbolos.buscarIdentificador(lexema, ambitoActual);
            if (claveVar != null) {
                String tipo = TablaSimbolos.obtenerTipo(claveVar);
                boolean esComp = TablaSimbolos.esComptime(claveVar);
                if (esComp) {
                    if (TablaSimbolos.tieneValorComptime(claveVar)) {
                        String val = TablaSimbolos.obtenerValorComptime(claveVar);
                        ParserVal pv = new ParserVal(val);
                        pv.obj = tipo;
                        $$ = pv;
                    } else {
                        agregarErrorSemantico("La variable comptime '" + lexema + "' se utiliza antes de ser inicializada con un valor estatico");
                        $$ = new ParserVal();
                    }
                } else {
                    ParserVal pv = new ParserVal(claveVar);
                    pv.obj = tipo;
                    $$ = pv;
                }
            } else {
                agregarErrorSemantico("Variable '" + lexema + "' no declarada o no visible en el ambito actual (" + ambitoActual + ")");
                $$ = new ParserVal();
            }
        }
    | CTE_SHORTINT
        {
            String lexema = $1.sval;
            int valor = Integer.parseInt(lexema);
            if (valor > AnalizadorLexico.ValorMaximoInt) {
                yyerror("Constante shortint positiva fuera de rango (" + lexema + "). Rango permitido: [-128, 127]");
            }
            TablaSimbolos.agregarAtributo(lexema, TablaSimbolos.TIPO, "SHORTINT");
            ParserVal pv = new ParserVal(lexema);
            pv.obj = "SHORTINT";
            $$ = pv;
        }
    | CTE_SINGLEF
        {
            String lexema = $1.sval;
            double valor = Double.parseDouble(lexema);
            if (valor != 0.0 && (valor < AnalizadorLexico.ValorMinimoFloat || valor > AnalizadorLexico.ValorMaximoFloat)) {
                yyerror("Constante singlef fuera de rango (" + lexema + ")");
            }
            TablaSimbolos.agregarAtributo(lexema, TablaSimbolos.TIPO, "SINGLEF");
            ParserVal pv = new ParserVal(lexema);
            pv.obj = "SINGLEF";
            $$ = pv;
        }
    | IDENTIFICADOR '(' ')'
        {
            String lexema = $1.sval;
            String claveFunc = TablaSimbolos.buscarIdentificador(lexema, ambitoActual);
            if (claveFunc == null) {
                agregarErrorSemantico("Funcion '" + lexema + "' no declarada o no visible en el ambito actual (" + ambitoActual + ")");
                $$ = new ParserVal();
            } else {
                String uso = TablaSimbolos.obtenerUso(claveFunc);
                if (!"funcion".equals(uso)) {
                    agregarErrorSemantico("El identificador '" + lexema + "' no es una funcion (uso actual: " + uso + ")");
                    $$ = new ParserVal();
                } else {
                    String tipoRetorno = TablaSimbolos.obtenerTipo(claveFunc);
                    ArrayList<String> paramsFormales = TablaSimbolos.obtenerParametros(claveFunc);
                    int cantEsperada = paramsFormales.size();

                    if (cantEsperada != 0) {
                        agregarErrorSemantico("Cantidad incorrecta de parametros en invocacion a '" + lexema + "': se esperaban " + cantEsperada + " pero se recibieron 0");
                        $$ = new ParserVal();
                    } else {
                        int idCall = GeneradorTerceto.crearTerceto("CALL", claveFunc, "-", tipoRetorno);
                        ParserVal pv = new ParserVal("[" + idCall + "]");
                        pv.obj = tipoRetorno;
                        $$ = pv;
                    }
                }
            }
        }
    | IDENTIFICADOR '(' lista_expresiones ')'
        {
            String lexema = $1.sval;
            String claveFunc = TablaSimbolos.buscarIdentificador(lexema, ambitoActual);
            if (claveFunc == null) {
                agregarErrorSemantico("Funcion '" + lexema + "' no declarada o no visible en el ambito actual (" + ambitoActual + ")");
                $$ = new ParserVal();
            } else {
                String uso = TablaSimbolos.obtenerUso(claveFunc);
                if (!"funcion".equals(uso)) {
                    agregarErrorSemantico("El identificador '" + lexema + "' no es una funcion (uso actual: " + uso + ")");
                    $$ = new ParserVal();
                } else {
                    String tipoRetorno = TablaSimbolos.obtenerTipo(claveFunc);
                    ArrayList<String> paramsFormales = TablaSimbolos.obtenerParametros(claveFunc);
                    int cantEsperada = paramsFormales.size();
                    ArrayList<ParserVal> paramsReales = ($3 != null && $3.obj != null) ? (ArrayList<ParserVal>) $3.obj : new ArrayList<>();
                    int cantRecibida = paramsReales.size();

                    if (cantRecibida != cantEsperada) {
                        agregarErrorSemantico("Cantidad incorrecta de parametros en invocacion a '" + lexema + "': se esperaban " + cantEsperada + " pero se recibieron " + cantRecibida);
                        $$ = new ParserVal();
                    } else {
                        boolean paramsOk = true;
                        for (int i = 0; i < cantEsperada; i++) {
                            String nomFormal = paramsFormales.get(i);
                            String claveFormal = claveFunc + TablaSimbolos.SEPARADOR + nomFormal;
                            String tipoFormal = TablaSimbolos.obtenerTipo(claveFormal);
                            ParserVal pReal = paramsReales.get(i);
                            String tipoReal = (pReal != null) ? (String) pReal.obj : null;

                            if (tipoFormal != null && tipoReal != null && !tipoFormal.equals(tipoReal)) {
                                agregarErrorSemantico("Incompatibilidad de tipos en parametro " + (i + 1) + " al invocar a '" + lexema + "': se esperaba " + tipoFormal + " pero se recibio " + tipoReal);
                                paramsOk = false;
                            } else if (pReal != null && pReal.sval != null) {
                                GeneradorTerceto.crearTerceto(":=", claveFormal, pReal.sval, tipoFormal);
                            }
                        }

                        if (paramsOk) {
                            int idCall = GeneradorTerceto.crearTerceto("CALL", claveFunc, "-", tipoRetorno);
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
        {
            $$ = $1;
        }
    ;

invocacion_metodo
    : IDENTIFICADOR '.' IDENTIFICADOR '(' ')'
        {
            String nomObj = $1.sval;
            String claveObj = TablaSimbolos.buscarIdentificador(nomObj, ambitoActual);

            if (claveObj == null) {
                agregarErrorSemantico("Objeto '" + nomObj + "' no declarado o no visible en el ambito actual (" + ambitoActual + ")");
                $$ = new ParserVal();
            } else {
                String uso = TablaSimbolos.obtenerUso(claveObj);
                String nomClase = TablaSimbolos.obtenerTipo(claveObj);
                String claveClase = TablaSimbolos.AMBITO_GLOBAL + TablaSimbolos.SEPARADOR + nomClase;
                boolean esObjeto = ("objeto".equals(uso) || "parametro".equals(uso)) && 
                    (TablaSimbolos.existeSimbolo(claveClase) && "nombre_clase".equals(TablaSimbolos.obtenerUso(claveClase)));

                if (!esObjeto) {
                    agregarErrorSemantico("El identificador '" + nomObj + "' no es un objeto (uso actual: " + uso + ")");
                    $$ = new ParserVal();
                } else {
                    String nomMetodo = $3.sval;

                    // Buscar metodo con herencia multiple (Tema 30 - Desambiguado Opcion 1)
                    String claveMet = TablaSimbolos.buscarMiembroEnJerarquia(nomClase, nomMetodo);
                    if (claveMet == null) {
                        agregarErrorSemantico("Metodo '" + nomMetodo + "' no encontrado en la clase '" + nomClase + "' ni en su jerarquia de herencia");
                        $$ = new ParserVal();
                    } else {
                        // Chequeo de visibilidad (Tema 26)
                        String claseActual = obtenerClaseDeAmbito(ambitoActual);
                        boolean accesoValido = true;
                        String claseDueniaMetodo = obtenerClaseDeAmbito(TablaSimbolos.obtenerAmbito(claveMet));
                        if (claseActual != null && claseDueniaMetodo != null && !claseActual.equals(claseDueniaMetodo)) {
                            String claveClaseActual = TablaSimbolos.AMBITO_GLOBAL + TablaSimbolos.SEPARADOR + claseActual;
                            ArrayList<String> imports = TablaSimbolos.obtenerImports(claveClaseActual);
                            if (imports == null || !imports.contains(claseDueniaMetodo)) {
                                agregarErrorSemantico("La clase '" + claseActual + "' no importa a la clase '" + claseDueniaMetodo + "'");
                                accesoValido = false;
                            } else if (!TablaSimbolos.estaExportadoA(claveMet, claseActual)) {
                                agregarErrorSemantico("El metodo '" + nomMetodo + "' de la clase '" + claseDueniaMetodo + "' no esta exportado a la clase '" + claseActual + "'");
                                accesoValido = false;
                            }
                        }

                        if (accesoValido) {
                            String tipoRetorno = TablaSimbolos.obtenerTipo(claveMet);
                            ArrayList<String> paramsFormales = TablaSimbolos.obtenerParametros(claveMet);
                            int cantEsperada = paramsFormales.size();

                            if (cantEsperada != 0) {
                                agregarErrorSemantico("Cantidad incorrecta de parametros en invocacion a metodo '" + nomMetodo + "': se esperaban " + cantEsperada + " pero se recibieron 0");
                                $$ = new ParserVal();
                            } else {
                                int idCall = GeneradorTerceto.crearTerceto("CALL", claveMet, "-", tipoRetorno);
                                ParserVal pv = new ParserVal("[" + idCall + "]");
                                pv.obj = tipoRetorno;
                                $$ = pv;
                            }
                        } else {
                            $$ = new ParserVal();
                        }
                    }
                }
            }
        }
    | IDENTIFICADOR '.' IDENTIFICADOR '(' lista_expresiones ')'
        {
            String nomObj = $1.sval;
            String claveObj = TablaSimbolos.buscarIdentificador(nomObj, ambitoActual);

            if (claveObj == null) {
                agregarErrorSemantico("Objeto '" + nomObj + "' no declarado o no visible en el ambito actual (" + ambitoActual + ")");
                $$ = new ParserVal();
            } else {
                String uso = TablaSimbolos.obtenerUso(claveObj);
                String nomClase = TablaSimbolos.obtenerTipo(claveObj);
                String claveClase = TablaSimbolos.AMBITO_GLOBAL + TablaSimbolos.SEPARADOR + nomClase;
                boolean esObjeto = ("objeto".equals(uso) || "parametro".equals(uso)) && 
                    (TablaSimbolos.existeSimbolo(claveClase) && "nombre_clase".equals(TablaSimbolos.obtenerUso(claveClase)));

                if (!esObjeto) {
                    agregarErrorSemantico("El identificador '" + nomObj + "' no es un objeto (uso actual: " + uso + ")");
                    $$ = new ParserVal();
                } else {
                    String nomMetodo = $3.sval;

                    String claveMet = TablaSimbolos.buscarMiembroEnJerarquia(nomClase, nomMetodo);
                    if (claveMet == null) {
                        agregarErrorSemantico("Metodo '" + nomMetodo + "' no encontrado en la clase '" + nomClase + "' ni en su jerarquia de herencia");
                        $$ = new ParserVal();
                    } else {
                        // Chequeo de visibilidad (Tema 26)
                        String claseActual = obtenerClaseDeAmbito(ambitoActual);
                        boolean accesoValido = true;
                        String claseDueniaMetodo = obtenerClaseDeAmbito(TablaSimbolos.obtenerAmbito(claveMet));
                        if (claseActual != null && claseDueniaMetodo != null && !claseActual.equals(claseDueniaMetodo)) {
                            String claveClaseActual = TablaSimbolos.AMBITO_GLOBAL + TablaSimbolos.SEPARADOR + claseActual;
                            ArrayList<String> imports = TablaSimbolos.obtenerImports(claveClaseActual);
                            if (imports == null || !imports.contains(claseDueniaMetodo)) {
                                agregarErrorSemantico("La clase '" + claseActual + "' no importa a la clase '" + claseDueniaMetodo + "'");
                                accesoValido = false;
                            } else if (!TablaSimbolos.estaExportadoA(claveMet, claseActual)) {
                                agregarErrorSemantico("El metodo '" + nomMetodo + "' de la clase '" + claseDueniaMetodo + "' no esta exportado a la clase '" + claseActual + "'");
                                accesoValido = false;
                            }
                        }

                        if (accesoValido) {
                            String tipoRetorno = TablaSimbolos.obtenerTipo(claveMet);
                            ArrayList<String> paramsFormales = TablaSimbolos.obtenerParametros(claveMet);
                            int cantEsperada = paramsFormales.size();
                            ArrayList<ParserVal> paramsReales = ($5 != null && $5.obj != null) ? (ArrayList<ParserVal>) $5.obj : new ArrayList<>();
                            int cantRecibida = paramsReales.size();

                            if (cantRecibida != cantEsperada) {
                                agregarErrorSemantico("Cantidad incorrecta de parametros en invocacion a metodo '" + nomMetodo + "': se esperaban " + cantEsperada + " pero se recibieron " + cantRecibida);
                                $$ = new ParserVal();
                            } else {
                                boolean paramsOk = true;
                                for (int i = 0; i < cantEsperada; i++) {
                                    String nomFormal = paramsFormales.get(i);
                                    String claveFormal = claveMet + TablaSimbolos.SEPARADOR + nomFormal;
                                    String tipoFormal = TablaSimbolos.obtenerTipo(claveFormal);
                                    ParserVal pReal = paramsReales.get(i);
                                    String tipoReal = (pReal != null) ? (String) pReal.obj : null;

                                    if (tipoFormal != null && tipoReal != null && !tipoFormal.equals(tipoReal)) {
                                        agregarErrorSemantico("Incompatibilidad de tipos en parametro " + (i + 1) + " al invocar metodo '" + nomMetodo + "': se esperaba " + tipoFormal + " pero se recibio " + tipoReal);
                                        paramsOk = false;
                                    } else if (pReal != null && pReal.sval != null) {
                                        GeneradorTerceto.crearTerceto(":=", claveFormal, pReal.sval, tipoFormal);
                                    }
                                }

                                if (paramsOk) {
                                    int idCall = GeneradorTerceto.crearTerceto("CALL", claveMet, "-", tipoRetorno);
                                    ParserVal pv = new ParserVal("[" + idCall + "]");
                                    pv.obj = tipoRetorno;
                                    $$ = pv;
                                } else {
                                    $$ = new ParserVal();
                                }
                            }
                        } else {
                            $$ = new ParserVal();
                        }
                    }
                }
            }
        }
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
                String lexema = $1.sval;
                String claveVar = TablaSimbolos.buscarIdentificador(lexema, ambitoActual);
                if (claveVar != null) {
                    boolean esComp = TablaSimbolos.esComptime(claveVar);
                    String tipoVar = TablaSimbolos.obtenerTipo(claveVar);
                    String tipoExp = (String) $4.obj;

                    if (esComp) {
                        if (!esConstante($4)) {
                            agregarErrorSemantico("La variable comptime '" + lexema + "' no puede recibir una expresion dependiente de tiempo de ejecucion");
                            $$ = new ParserVal();
                        } else {
                            if (tipoExp != null) {
                                if (tipoVar != null && tipoVar.equals(tipoExp)) {
                                    TablaSimbolos.guardarValorComptime(claveVar, $4.sval);
                                    ParserVal pv = new ParserVal($4.sval);
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
                        }
                    } else {
                        if (tipoExp != null) {
                            if (tipoVar != null && tipoVar.equals(tipoExp)) {
                                int idTerceto = GeneradorTerceto.crearTerceto(":=", claveVar, $4.sval, tipoVar);
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
            String lexemaPos = $2.sval;
            String claveNeg = TablaSimbolos.convertirANegativo(lexemaPos);
            TablaSimbolos.agregarAtributo(claveNeg, TablaSimbolos.TIPO, "SHORTINT");
            ParserVal pv = new ParserVal("-" + lexemaPos);
            pv.obj = "SHORTINT";
            $$ = pv;
        }
    | '-' CTE_SINGLEF
        {
            String lexemaPos = $2.sval;
            String claveNeg = TablaSimbolos.convertirANegativo(lexemaPos);
            TablaSimbolos.agregarAtributo(claveNeg, TablaSimbolos.TIPO, "SINGLEF");
            ParserVal pv = new ParserVal("-" + lexemaPos);
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
                    if (esConstante($3)) {
                        double d = Double.parseDouble($3.sval);
                        int entero = (int) d;
                        if (entero < AnalizadorLexico.ValorMinimoInt || entero > AnalizadorLexico.ValorMaximoInt) {
                            agregarErrorSemantico("Resultado de conversion 'tos' fuera de rango SHORTINT [-128, 127]: " + entero);
                        }
                        ParserVal pv = new ParserVal(String.valueOf(entero));
                        pv.obj = "SHORTINT";
                        $$ = pv;
                    } else {
                        int idTerceto = GeneradorTerceto.crearTerceto("tos", $3.sval, "-", "SHORTINT");
                        ParserVal pv = new ParserVal("[" + idTerceto + "]");
                        pv.obj = "SHORTINT";
                        $$ = pv;
                    }
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
            GeneradorTerceto.crearTerceto("POUT", $3.sval, "-");
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
                String claveFunc = funcionActual;
                String tipoDeclarado = TablaSimbolos.obtenerTipo(claveFunc);
                String tipoExp = ($3 != null) ? (String) $3.obj : null;
                TablaSimbolos.agregarAtributo(claveFunc, "TIENE_RET", Boolean.TRUE);

                if (cant_retornos_auto >= 0) {
                    cant_retornos_auto++;
                    if ("auto".equals(tipoDeclarado)) {
                        TablaSimbolos.agregarAtributo(claveFunc, TablaSimbolos.TIPO, tipoExp);
                    } else if (tipoExp != null && !tipoDeclarado.equals(tipoExp)) {
                        TablaSimbolos.agregarAtributo(claveFunc, TablaSimbolos.TIPO, "SINGLEF");
                    }
                } else {
                    if (tipoDeclarado != null && tipoExp != null && !tipoDeclarado.equals(tipoExp)) {
                        agregarErrorSemantico("Tipo de retorno incompatible en funcion '" + 
                            TablaSimbolos.obtenerNombre(claveFunc) + 
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
static boolean claseActualValida = false;

public static String ambitoActual = TablaSimbolos.AMBITO_GLOBAL;
public static Stack<Integer> pilaSaltos = new Stack<>();

public static String obtenerFuncionActualMangled() {
    if (ambitoActual == null || ambitoActual.equals(TablaSimbolos.AMBITO_GLOBAL)) {
        return null;
    }
    return ambitoActual;
}

public static String obtenerClaseDeAmbito(String ambito) {
    if (ambito == null) return null;
    String[] partes = ambito.split(TablaSimbolos.SEPARADOR);
    if (partes.length >= 2 && TablaSimbolos.AMBITO_GLOBAL.equals(partes[0])) {
        String posibleClase = partes[1];
        String claveClase = TablaSimbolos.AMBITO_GLOBAL + TablaSimbolos.SEPARADOR + posibleClase;
        if ("nombre_clase".equals(TablaSimbolos.obtenerUso(claveClase))) {
            return posibleClase;
        }
    }
    return null;
}

public static void entrarAmbito(String nombre) {
    ambitoActual = ambitoActual + TablaSimbolos.SEPARADOR + nombre;
}

public static void salirAmbito() {
    pilaSaltos.clear();
    int index = ambitoActual.lastIndexOf(TablaSimbolos.SEPARADOR);
    if (index > 0) {
        ambitoActual = ambitoActual.substring(0, index);
    } else {
        ambitoActual = TablaSimbolos.AMBITO_GLOBAL;
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

 // Soporte para variables COMPTIME y evaluacion estatica (Tema 22)
public static boolean esConstante(ParserVal pv) {
    if (pv == null || pv.sval == null) {
        return false;
    }
    return !pv.sval.startsWith("[") && !pv.sval.contains(TablaSimbolos.SEPARADOR);
}

public static String evaluarOperacionEstatica(String op, String val1, String val2, String tipo) {
    try {
        if ("SHORTINT".equals(tipo)) {
            int v1 = Integer.parseInt(val1);
            int v2 = Integer.parseInt(val2);
            int res = 0;
            if (op.equals("+")) res = v1 + v2;
            else if (op.equals("-")) res = v1 - v2;
            else if (op.equals("*")) res = v1 * v2;
            else if (op.equals("/")) {
                if (v2 == 0) {
                    agregarErrorSemantico("Division por cero en tiempo de compilacion");
                    return "0";
                }
                res = v1 / v2;
            }
            if (res < AnalizadorLexico.ValorMinimoInt || res > AnalizadorLexico.ValorMaximoInt) {
                agregarErrorSemantico("Resultado de operacion en tiempo de compilacion fuera de rango SHORTINT [-128, 127]: " + res);
            }
            return String.valueOf(res);
        } else if ("SINGLEF".equals(tipo)) {
            double v1 = Double.parseDouble(val1);
            double v2 = Double.parseDouble(val2);
            double res = 0.0;
            if (op.equals("+")) res = v1 + v2;
            else if (op.equals("-")) res = v1 - v2;
            else if (op.equals("*")) res = v1 * v2;
            else if (op.equals("/")) {
                if (v2 == 0.0) {
                    agregarErrorSemantico("Division por cero en tiempo de compilacion");
                    return "0.0";
                }
                res = v1 / v2;
            }
            double absRes = Math.abs(res);
            if (absRes != 0.0 && (absRes < AnalizadorLexico.ValorMinimoFloat || absRes > AnalizadorLexico.ValorMaximoFloat)) {
                agregarErrorSemantico("Resultado de operacion en tiempo de compilacion fuera de rango SINGLEF: " + res);
            }
            return String.valueOf(res);
        }
    } catch (NumberFormatException e) {
        return val1;
    }
    return val1;
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
        ambitoActual = TablaSimbolos.AMBITO_GLOBAL;
        TablaSimbolos.reiniciar();
        GeneradorTerceto.reiniciar();
        GeneradorTerceto.iniciarAmbito(ambitoActual);
        claseActualValida = false;
        pilaSaltos.clear();
        cant_retornos_auto = -1;
        cant_errores = 0;
        cant_errores_semanticos = 0;
        AnalizadorLexico.reader = new PushbackReader(new BufferedReader(
                new InputStreamReader(new FileInputStream(ruta), java.nio.charset.StandardCharsets.UTF_8)));
        parser = new Parser(debug);
        parser.yyparse(); 
        TablaSimbolos.purgarIdentificadoresSinDeclarar();
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
    if (AnalizadorLexico.referenciaTablaSimbolos != null) {
        yylval = new ParserVal(AnalizadorLexico.referenciaTablaSimbolos);
    } else {
        yylval = new ParserVal();
    }
    return token;
}

void yyerror(String s) {
    if (s.equals("syntax error")) {
        return; // Se omite el mensaje generico de byacc/j para usar las descripciones especificas
    }
    cant_errores++;
    System.out.println("Error sintactico (linea " + AnalizadorLexico.getLineaActual() + "): " + s);
}