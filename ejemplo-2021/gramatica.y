%{
package compilador;

import accion_semantica.AccionSemantica;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
%}
        //declaracion de tokens a recibir del Analizador Lexico
%token ID CTE CADENA IF THEN ELSE ENDIF PRINT FUNC RETURN BEGIN END
 ULONG ASSIGN COMP_MAYOR_IGUAL COMP_MENOR_IGUAL COMP_IGUAL COMP_DISTINTO
 LOGIC_AND LOGIC_OR COMMENT REPEAT UNTIL BREAK DOUBLE CONTRACT TRY CATCH

%left '+' '-'
%left '*' '/'

%start program

%%      //declaracion de la gramatica del lenguaje

program: header_program begin_prog ejecucion END ';'
        | header_program begin_prog END ';' {agregarError(errores_sintacticos, Parser.ERROR, "Se esperaban sentencias de ejecucion");}
        | header_program begin_prog ejecucion {agregarError(errores_sintacticos, Parser.ERROR, "Se esperaba un END al final del programa");}
        | header_program
        | ';' {agregarError(errores_sintacticos, Parser.ERROR, "Se esperaba un programa");}
;

begin_prog: BEGIN { agregarToken(":START");
                    TablaSimbolos.agregarSimbolo(nombreVariableContrato);
                    TablaSimbolos.agregarAtributo(TablaSimbolos.obtenerSimbolo(nombreVariableContrato), "tipo", TablaTipos.ULONG_TYPE);
                    }
;
header_program: nombre_programa { Parser.declarando = false; }
              | nombre_programa declaracion 
              | declaracion {agregarError(errores_sintacticos, Parser.ERROR, "Se esperaba nombre del programa");}
;

nombre_programa: ID {cambiarAmbito($1.sval);}
;

//reglas de declaraciones y bloques de sentencias
declaracion: declaracion_variables declaracion_funcion { Parser.declarando = false; }
        | declaracion_variables { Parser.declarando = false; }
        | declaracion_funcion { Parser.declarando = false; }
;

declaracion_variables: tipo list_var ';'
        | declaracion_variables tipo list_var ';'
        | list_var ';'
;

list_var: ID { 
                int ptr_id = TablaSimbolos.obtenerSimbolo($1.sval + Parser.ambito.toString());
                TablaSimbolos.agregarAtributo(ptr_id, "tipo", tipo);
                TablaSimbolos.agregarAtributo(ptr_id, "uso", "variable");
                }
        | list_var ',' ID { 
                int ptr_id = TablaSimbolos.obtenerSimbolo($3.sval + Parser.ambito.toString());
                TablaSimbolos.agregarAtributo(ptr_id, "tipo", tipo);
                TablaSimbolos.agregarAtributo(ptr_id, "uso", "variable"); }
;

declaracion_funcion: declaracion_funcion funcion
                | funcion
;

funcion: header_funcion '(' parametro ')' cuerpo_funcion { agregarToken(nombreFuncion());
                                                           salirAmbito();
                                                           Parser.declarando = true;
                                                           agregarToken("\\ENDP"); }
        | header_funcion '(' ')' cuerpo_funcion { agregarError(errores_sintacticos, Parser.ERROR, "Se espera al menos un parametro");}
        | tipo FUNC cuerpo_funcion { agregarError(errores_sintacticos, Parser.ERROR, "Se espera el nombre y parametros de la funcion");}
;

header_funcion: tipo FUNC ID {
                        int ptr_id = TablaSimbolos.obtenerSimbolo($3.sval + Parser.ambito.toString());
                        TablaSimbolos.agregarAtributo(ptr_id, "tipo", TablaTipos.FUNC_TYPE);
                        TablaSimbolos.agregarAtributo(ptr_id, "uso", TablaTipos.FUNC_TYPE);
                        TablaSimbolos.agregarAtributo(ptr_id, "retorno", tipo);

                        TablaSimbolos.agregarSimbolo("@ret@" + $3.sval + Parser.ambito.toString());
                        int ptr_ret = TablaSimbolos.obtenerSimbolo("@ret@" + $3.sval + Parser.ambito.toString());
                        TablaSimbolos.agregarAtributo(ptr_ret, "tipo", tipo);
                        TablaSimbolos.agregarAtributo(ptr_ret, "uso", "variable");
                        
                        cambiarAmbito($3.sval);
                        }
        | tipo FUNC {agregarError(errores_sintacticos, Parser.ERROR, "Se espera el nombre de la funcion");}
;

cuerpo_funcion: declaracion ejecucion_funcion   
        | ejecucion_funcion
;

parametro: tipo ID {    int ptr_id = TablaSimbolos.obtenerSimbolo($2.sval + Parser.ambito.toString());
                        int primerSeparador = Parser.ambito.toString().indexOf(NAME_MANGLING_CHAR);
                        int ultimoSeparador = Parser.ambito.toString().lastIndexOf(NAME_MANGLING_CHAR);
                        String nombre_funcion = Parser.ambito.substring(ultimoSeparador + 1) + Parser.ambito.substring(primerSeparador, ultimoSeparador);
                        int ptr_func = TablaSimbolos.obtenerSimbolo(nombre_funcion);
                        
                        TablaSimbolos.agregarAtributo(ptr_id, "tipo", tipo);
                        TablaSimbolos.agregarAtributo(ptr_id, "uso", "parametro");
                        TablaSimbolos.agregarAtributo(ptr_func, "tipo_parametro", tipo);
                   }
        | ID {agregarError(errores_sintacticos, Parser.ERROR, "Se espera el tipo del parametro");}
        | tipo {agregarError(errores_sintacticos, Parser.ERROR, "Se espera el nombre del parametro");}
;

tipo: DOUBLE {tipo = TablaTipos.DOUBLE_TYPE;}
        | ULONG {tipo = TablaTipos.ULONG_TYPE;}
        | FUNC {tipo = TablaTipos.FUNC_TYPE;}
;

ejecucion_funcion: begin bloque_funcion RETURN '(' expresion ')' ';' END ';' {
                                                                        int punt_funcion = TablaSimbolos.obtenerSimbolo(nombreFuncion());

                                                                        if (TablaSimbolos.obtenerAtributo(punt_funcion, "retorno").equals(TablaTipos.FUNC_TYPE)) {
                                                                                TablaSimbolos.agregarAtributo(punt_funcion, "nombre_retorno", funcion_a_asignar);
                                                                                funcion_a_asignar = "";  
                                                                        }
                                                                        
                                                                        agregarToken("@ret@" + nombreFuncion()); 
                                                                        agregarToken("\\RET");}
                                                                        
                | begin bloque_funcion RETURN '(' expresion ')' ';' bloque_funcion END ';' {agregarError(errores_sintacticos, Parser.ERROR, "El RETURN debe ser la ultima sentencia de la funcion");}
                | begin bloque_funcion RETURN '(' expresion ')' ';' {agregarError(errores_sintacticos, Parser.ERROR, "Se espera 'END;'");}
                | begin bloque_funcion RETURN '(' expresion ')' ';' bloque_funcion {agregarError(errores_sintacticos, Parser.ERROR, "El RETURN debe ser la ultima sentencia de la funcion y se espera 'END;'");}
                | begin bloque_funcion RETURN '(' expresion ')' ';' ';' {agregarError(errores_sintacticos, Parser.ERROR, "Se espera END");}
                | begin bloque_funcion RETURN '(' expresion ')' ';' END {agregarError(errores_sintacticos, Parser.ERROR, "Se espera ';' luego del END");}
                | begin bloque_funcion RETURN '(' expresion ')' ';' bloque_funcion END {agregarError(errores_sintacticos, Parser.ERROR, "El RETURN debe ser la ultima sentencia de la funcion y se espera ';' luego del END");}
                
                | begin bloque_funcion RETURN expresion ';' END { agregarError(errores_sintacticos, Parser.ERROR, "Se espera que se retorne una expresion. Se espera un ';' luego del END"); }
                | begin bloque_funcion RETURN expresion ';' bloque_funcion END { agregarError(errores_sintacticos, Parser.ERROR, "Se espera que el RETURN sea la ultima sentencia y se retorne una expresion. Se espera un ';' luego del END"); }
                | begin bloque_funcion RETURN expresion ';' END ';' { agregarError(errores_sintacticos, Parser.ERROR, "Se espera que la expresion a retornar este encerrada entre parentesis"); }
                | begin bloque_funcion RETURN expresion ';' bloque_funcion END ';' { agregarError(errores_sintacticos, Parser.ERROR, "Se espera que el RETURN sea la ultima sentencia y la expresion a retornar este encerrada entre parentesis"); }
                | begin bloque_funcion RETURN expresion ';' ';' { agregarError(errores_sintacticos, Parser.ERROR, "Se espera que la expresion a retornar este encerrada entre parentesis y un END al final"); }
                | begin bloque_funcion RETURN expresion ';' { agregarError(errores_sintacticos, Parser.ERROR, "Se espera que la expresion a retornar este encerrada entre parentesis y un END al final"); }
                | begin bloque_funcion RETURN expresion ';' bloque_funcion { agregarError(errores_sintacticos, Parser.ERROR, "Se espera que el RETURN sea la ultima sentencia y la expresion a retornar este encerrada entre parentesis y un END al final"); }

                | begin bloque_funcion RETURN '(' ')' ';' END ';' { agregarError(errores_sintacticos, Parser.ERROR, "Se espera que se retorne una expresion"); }
                | begin bloque_funcion RETURN '(' ')' ';' bloque_funcion END ';' { agregarError(errores_sintacticos, Parser.ERROR, "Se espera que el RETURN sea la ultima sentencia y que se retorne una expresion"); }
                | begin bloque_funcion RETURN '(' ')' ';' END { agregarError(errores_sintacticos, Parser.ERROR, "Se espera que se retorne una expresion y un ';' al final del END"); }
                | begin bloque_funcion RETURN '(' ')' ';' ';' { agregarError(errores_sintacticos, Parser.ERROR, "Se espera que se retorne una expresion y un END al final"); }
                | begin bloque_funcion RETURN '(' ')' ';' { agregarError(errores_sintacticos, Parser.ERROR, "Se espera que se retorne una expresion y un END al final"); }

                | begin END ';' { agregarError(errores_sintacticos, Parser.ERROR, "Se espera que la funcion tenga un bloque de sentencias"); }
                | begin END { agregarError(errores_sintacticos, Parser.ERROR, "Se espera que la funcion tenga un bloque de sentencias y un ';' luego del END"); }
                | begin ';' { agregarError(errores_sintacticos, Parser.ERROR, "Se espera que la funcion tenga un bloque de sentencias y un END al final"); }
                | begin { agregarError(errores_sintacticos, Parser.ERROR, "Se espera que la funcion tenga un bloque de sentencias y un END al final"); }
                
                | begin RETURN ';' END ';' { agregarError(errores_sintacticos, Parser.ERROR, "Se espera que se retorne algun valor"); }
                | begin RETURN ';' bloque_funcion END ';' { agregarError(errores_sintacticos, Parser.ERROR, "Se espera que el RETURN sea la ultima sentencia, se retorne algun valor"); }
                | begin RETURN ';' END { agregarError(errores_sintacticos, Parser.ERROR, "Se espera que se retorne algun valor y un ';' luego del END"); }
                | begin RETURN ';' bloque_funcion END { agregarError(errores_sintacticos, Parser.ERROR, "Se espera que el RETURN sea la ultima sentencia, se retorne algun valor y un ';' luego del END"); }
                | begin RETURN ';' { agregarError(errores_sintacticos, Parser.ERROR, "Se espera que se retorne algun valor y un END al final"); }
                | begin RETURN ';' bloque_funcion { agregarError(errores_sintacticos, Parser.ERROR, "Se espera que el RETURN sea la ultima sentencia, que se retorne algun valor y un END al final"); }
                | begin RETURN ';' ';' { agregarError(errores_sintacticos, Parser.ERROR, "Se espera que se retorne algun valor y un END al final"); }
                
                | begin RETURN '(' expresion ')' ';' END ';'
                | begin RETURN '(' expresion ')' ';' bloque_funcion END ';'{ agregarError(errores_sintacticos, Parser.ERROR, "Se espera que el RETURN sea la ultima sentencia y un ';' luego del END"); }
                | begin RETURN '(' expresion ')' ';' END { agregarError(errores_sintacticos, Parser.ERROR, "Se espera un ';' luego del END"); }
                | begin RETURN '(' expresion ')' ';' bloque_funcion END { agregarError(errores_sintacticos, Parser.ERROR, "Se espera un ';' luego del END"); }
                | begin RETURN '(' expresion ')' ';' { agregarError(errores_sintacticos, Parser.ERROR, "Se espera un END al final"); }
                | begin RETURN '(' expresion ')' ';' bloque_funcion { agregarError(errores_sintacticos, Parser.ERROR, "Se espera que el RETURN sea la ultima sentencia y un END al final"); }

                | begin RETURN '(' ')' ';' END ';' { agregarError(errores_sintacticos, Parser.ERROR, "Se espera que se retorne algun valor"); }
                | begin RETURN '(' ')' ';' END { agregarError(errores_sintacticos, Parser.ERROR, "Se espera que se retorne algun valor y un ';' luego del END"); }
                | begin RETURN '(' ')' ';' ';' { agregarError(errores_sintacticos, Parser.ERROR, "Se espera que se retorne algun valor y un END al final"); }
                | begin RETURN '(' ')' ';' { agregarError(errores_sintacticos, Parser.ERROR, "Se espera que se retorne algun valor y un END al final"); }
;

begin: BEGIN { Parser.declarando = false; agregarToken("!" + nombreFuncion().replace(':', '/')); }
;

bloque_funcion: bloque_funcion sentencia_funcion
        | sentencia_funcion
;

sentencia_funcion: sentencia
                | CONTRACT ':' '(' condicion ')' ';' { agregarToken(String.valueOf(polaca.size() + 2));
                                                       agregarToken("#BT");
                                                       agregarToken(":L" + String.valueOf(polaca.size())); }
                | CONTRACT ':'                      { agregarError(errores_sintacticos, Parser.ERROR, "Se espera condicion despues de CONTRACT"); }
                | CONTRACT ':' '(' condicion ')'     { agregarError(errores_sintacticos, Parser.ERROR, "Se espera ';' despues de la condicion de CONTRACT"); }
;

ejecucion: ejecucion sentencia
        | sentencia
;

sentencia: sentencia_ejecutable
        | try_catch
;

try_catch: TRY sentencia_try catch BEGIN bloque_sentencias_ejecutables END ';' { agregarToken("#END_TRY"); 
                                                                                 int posicion = pila.pop();
                                                                                 polaca.set(posicion, String.valueOf(polaca.size()));
                                                                                 agregarToken(":L" + String.valueOf(polaca.size())); }
        | TRY catch BEGIN bloque_sentencias_ejecutables END ';' { agregarError(errores_sintacticos, Parser.ERROR, "Se esperaba una sentencia luego del 'TRY'"); }
        | TRY sentencia_try BEGIN bloque_sentencias_ejecutables END ';' { agregarError(errores_sintacticos, Parser.ERROR, "Se esperaba 'CATCH'"); }
        | TRY sentencia_try catch bloque_sentencias_ejecutables END ';' { agregarError(errores_sintacticos, Parser.ERROR, "Se esperaba 'BEGIN' luego del 'CATCH'"); }
        | TRY sentencia_try catch BEGIN END ';' { agregarError(errores_sintacticos, Parser.ERROR, "Se esperaba el bloque de sentencias dentro del 'CATCH'"); }
        | TRY sentencia_try catch BEGIN bloque_sentencias_ejecutables END { agregarError(errores_sintacticos, Parser.ERROR, "Se esperaba ';' al final del END"); }
;

catch: CATCH { agregarToken("#CATCH"); 
               apilar();
               agregarToken("");
               agregarToken("#BT"); }
;
sentencia_try: asignacion ';'
        | asignacion { agregarError(errores_sintacticos, Parser.ERROR, "Se esperaba ';'"); }
        | seleccion_en_try ';' 
        | seleccion_en_try { agregarError(errores_sintacticos, Parser.ERROR, "Se esperaba ';'"); }
        | impresion ';' 
        | impresion { agregarError(errores_sintacticos, Parser.ERROR, "Se esperaba ';'"); }
        | repeat_until_try ';' 
        | repeat_until_try { agregarError(errores_sintacticos, Parser.ERROR, "Se esperaba ';'"); }
;

bloque_sentencias_ejecutables: bloque_sentencias_ejecutables sentencia_ejecutable
                        | sentencia_ejecutable
;

sentencia_ejecutable: asignacion ';' 
                | seleccion ';' 
                | seleccion {agregarError(errores_sintacticos, Parser.ERROR, "Se esperaba ';'");}
                | impresion ';' 
                | impresion {agregarError(errores_sintacticos, Parser.ERROR, "Se esperaba ';'");}
                | repeat_until ';' 
                | repeat_until {agregarError(errores_sintacticos, Parser.ERROR, "Se esperaba ';'");}
;

bloque_sentencias_try: BEGIN bloque_sentencias_ejecutables END ';'
                | BEGIN END ';' { agregarError(errores_sintacticos, Parser.ERROR, "Se esperaban sentencias entre BEGIN y END"); }
;

seleccion_en_try: IF condicion_salto_if then_seleccion_en_try ENDIF { 
                                                                int posicion = pila.pop();
                                                                polaca.set(posicion, String.valueOf(polaca.size()));
                                                                agregarToken(":L" + String.valueOf(polaca.size())); }
        | IF condicion_salto_if then_seleccion_en_try else_seleccion_en_try ENDIF { 
                                                        int posicion = pila.pop();
                                                        polaca.set(posicion, String.valueOf(polaca.size()));
                                                        agregarToken(":L" + String.valueOf(polaca.size())); }
        | IF condicion_salto_if bloque_sentencias_try ENDIF { agregarError(errores_sintacticos, Parser.ERROR, "Se espera 'THEN' luego de la condicion"); }
        | IF condicion_salto_if then_seleccion_en_try bloque_sentencias_try ENDIF { agregarError(errores_sintacticos, Parser.ERROR, "Se espera ELSE luego del bloque de sentencias del IF"); }
;

then_seleccion_en_try: THEN bloque_sentencias_try {
                                                int posicion = pila.pop();
                                                polaca.set(posicion, String.valueOf(polaca.size() + 2));
                                                apilar();
                                                agregarToken("");
                                                agregarToken("#BI");
                                                agregarToken(":L" + String.valueOf(polaca.size()));
                                                }
;

else_seleccion_en_try: ELSE bloque_sentencias_try
;

bloque_sentencias_repeat_en_try: BEGIN sentencia_ejecutable_repeat_en_try END ';'
        | BEGIN sentencia_repeat_en_try ';' { agregarError(errores_sintacticos, Parser.ERROR, "Se espera END al final"); }
        | sentencia_repeat_en_try END ';' { agregarError(errores_sintacticos, Parser.ERROR, "Se espera BEGIN antes de las sentencias"); }
        | sentencia_repeat_en_try { agregarError(errores_sintacticos, Parser.ERROR, "Se espera BEGIN y END, antes y despues de las sentencias"); }
;

sentencia_ejecutable_repeat_en_try: sentencia_repeat_en_try
                        | sentencia_ejecutable_repeat_en_try sentencia_repeat_en_try
;

sentencia_repeat_en_try: asignacion ';' 
                | seleccion_en_repeat_en_try ';' 
                | impresion ';' 
                | repeat_until_try ';'
                | break ';'
;

break: BREAK {
        apilarBreak();
        agregarToken("");
        agregarToken("#BI");
}
;

repeat_until_try: repeat bloque_sentencias_repeat_en_try UNTIL '(' condicion ')' {
                                                                int posicion = pila.pop();
                                                                agregarToken(String.valueOf(posicion));
                                                                agregarToken("#BF");
                                                                List<Integer> breakes = getListaBreakes();
                                                                for(int pos: breakes){
                                                                        polaca.set(pos, String.valueOf(posicion));
                                                                }
                                                        } //la iteracion dentro de un try debe utilizar sentencias aptas dentro de esa estructura, es decir, los bloque_sentencias_repeat_en_try
        | repeat UNTIL { agregarError(errores_sintacticos, Parser.ERROR, "Se espera bloque de sentencias entre REPEAT y UNTIL"); }
        | repeat bloque_sentencias_repeat_en_try UNTIL { agregarError(errores_sintacticos, Parser.ERROR, "Se espera condicion despues de UNTIL"); } 
        | repeat bloque_sentencias_repeat_en_try UNTIL '(' ')' { agregarError(errores_sintacticos, Parser.ERROR, "Se espera condicion entre los parentesis de UNTIL"); }
        | repeat bloque_sentencias_repeat_en_try { agregarError(errores_sintacticos, Parser.ERROR, "Se espera UNTIL despues de bloque de sentencias en REPEAT"); }
;

seleccion_en_repeat_en_try: IF condicion_salto_if then_seleccion_repeat_en_try ENDIF { 
                                                        int posicion = pila.pop();
                                                        polaca.set(posicion, String.valueOf(polaca.size()));
                                                        agregarToken(":L" + String.valueOf(polaca.size()));
                                                        }
                        | IF condicion_salto_if then_seleccion_repeat_en_try else_seleccion_repeat_en_try ENDIF { 
                                                        int posicion = pila.pop();
                                                        polaca.set(posicion, String.valueOf(polaca.size())); 
                                                        agregarToken(":L" + String.valueOf(polaca.size()));
                                                        }
                        | IF condicion_salto_if bloque_sentencias_repeat_en_try ENDIF  { agregarError(errores_sintacticos, Parser.ERROR, "Se espera 'THEN' luego de la condicion del IF"); }
                        | IF condicion_salto_if then_seleccion_repeat_en_try bloque_sentencias_repeat_en_try ENDIF  { agregarError(errores_sintacticos, Parser.ERROR, "Se espera ELSE"); }
                        | IF condicion_salto_if THEN ENDIF { agregarError(errores_sintacticos, Parser.ERROR, "Se espera bloque de sentencias luego del THEN"); }        
                        | IF condicion_salto_if then_seleccion_repeat_en_try ELSE ENDIF { agregarError(errores_sintacticos, Parser.ERROR, "Se espera bloque de sentencias despues del ELSE"); }
                        | IF condicion_salto_if THEN else_seleccion_repeat_en_try ENDIF { agregarError(errores_sintacticos, Parser.ERROR, "Se espera bloque de sentencias despues del THEN"); }
;

then_seleccion_repeat_en_try: THEN bloque_sentencias_repeat_en_try {
                                                int posicion = pila.pop();
                                                polaca.set(posicion, String.valueOf(polaca.size() + 2));
                                                apilar();
                                                agregarToken("");
                                                agregarToken("#BI");
                                                agregarToken(":L" + String.valueOf(polaca.size()));
                                                }
;

else_seleccion_repeat_en_try: ELSE bloque_sentencias_repeat_en_try
;

repeat_until: repeat bloque_sentencias_repeat UNTIL '(' condicion ')' {
                                                        int posicion = pila.pop();
                                                        agregarToken(String.valueOf(posicion));
                                                        agregarToken("#BF");
                                                        List<Integer> breakes = getListaBreakes();
                                                        for(int pos: breakes){
                                                                polaca.set(pos, String.valueOf(posicion));
                                                        }
                                                }
        | repeat UNTIL { agregarError(errores_sintacticos, Parser.ERROR, "Se espera bloque de sentencias entre REPEAT y UNTIL"); }
        | repeat bloque_sentencias_repeat UNTIL { agregarError(errores_sintacticos, Parser.ERROR, "Se espera condicion despues de UNTIL"); } 
        | repeat bloque_sentencias_repeat UNTIL '(' ')' { agregarError(errores_sintacticos, Parser.ERROR, "Se espera condicion entre los parentesis de UNTIL"); }
;

repeat: REPEAT { apilar();
                agregarListaBreak();
                agregarToken(":L" + String.valueOf(polaca.size()));
                }
;

bloque_sentencias_repeat: BEGIN sentencia_ejecutable_repeat END ';'
                        | BEGIN END ';' { agregarError(errores_sintacticos, Parser.ERROR, "Se espera bloque de sentencias entre REPEAT y UNTIL"); }
                        | sentencia_ejecutable_repeat END ';' { agregarError(errores_sintacticos, Parser.ERROR, "Se espera un BEGIN"); }
;

sentencia_ejecutable_repeat: sentencia_repeat
                        | sentencia_ejecutable_repeat sentencia_repeat
;

sentencia_repeat: try_catch
                | repeat_until ';'
                | repeat_until { agregarError(errores_sintacticos, Parser.ERROR, "Se espera ';'"); }
                | impresion ';'
                | impresion { agregarError(errores_sintacticos, Parser.ERROR, "Se espera ';'"); }
                | seleccion_en_repeat ';'
                | seleccion_en_repeat { agregarError(errores_sintacticos, Parser.ERROR, "Se espera ';'"); }
                | asignacion ';'
                | break ';'
                | break { agregarError(errores_sintacticos, Parser.ERROR, "Se espera ';'"); }
;

seleccion_en_repeat: IF condicion_salto_if then_seleccion_repeat ENDIF { 
                                                        int posicion = pila.pop();
                                                        polaca.set(posicion, String.valueOf(polaca.size()));
                                                        agregarToken(":L" + String.valueOf(polaca.size()));
                                                        }
        | IF condicion_salto_if then_seleccion_repeat else_seleccion_repeat ENDIF { 
                                                        int posicion = pila.pop();
                                                        polaca.set(posicion, String.valueOf(polaca.size()));
                                                        agregarToken(":L" + String.valueOf(polaca.size()));
                                                        }
        | IF condicion_salto_if bloque_sentencias_repeat ENDIF { agregarError(errores_sintacticos, Parser.ERROR, "Se espera 'THEN' luego de la condicion del IF"); }
        | IF condicion_salto_if then_seleccion_repeat bloque_sentencias_repeat ENDIF { agregarError(errores_sintacticos, Parser.ERROR, "Se espera ELSE"); }
        | IF condicion_salto_if THEN ENDIF { agregarError(errores_sintacticos, Parser.ERROR, "Se espera bloque de sentencias luego del THEN"); }        
        | IF condicion_salto_if then_seleccion_repeat ELSE ENDIF { agregarError(errores_sintacticos, Parser.ERROR, "Se espera bloque de sentencias despues del ELSE"); }
        | IF condicion_salto_if THEN else_seleccion_repeat ENDIF { agregarError(errores_sintacticos, Parser.ERROR, "Se espera bloque de sentencias despues del THEN"); }
;

then_seleccion_repeat: THEN bloque_sentencias_repeat {
                                                int posicion = pila.pop();
                                                polaca.set(posicion, String.valueOf(polaca.size() + 2));
                                                apilar();
                                                agregarToken("");
                                                agregarToken("#BI");
                                                agregarToken(":L" + String.valueOf(polaca.size()));
                                                }
;

else_seleccion_repeat: ELSE bloque_sentencias_repeat
;
 
seleccion: IF condicion_salto_if then_seleccion ENDIF {
                                                        int posicion = pila.pop();
                                                        polaca.set(posicion, String.valueOf(polaca.size()));
                                                        agregarToken(":L" + String.valueOf(polaca.size()));
                                                        }
        | IF condicion_salto_if then_seleccion else_seleccion ENDIF { 
                                                        int posicion = pila.pop();
                                                        polaca.set(posicion, String.valueOf(polaca.size()));
                                                        agregarToken(":L" + String.valueOf(polaca.size()));
                                                        }
        | IF condicion_salto_if BEGIN ejecucion END ';' else_seleccion ENDIF { agregarError(errores_sintacticos, Parser.ERROR, "Se espera THEN"); }
        | IF condicion_salto_if then_seleccion BEGIN ejecucion END ';' ENDIF { agregarError(errores_sintacticos, Parser.ERROR, "Se espera ELSE"); }
        | IF condicion_salto_if THEN ENDIF { agregarError(errores_sintacticos, Parser.ERROR, "Se espera bloque de sentencias luego del THEN"); }        
        | IF condicion_salto_if then_seleccion ELSE ENDIF { agregarError(errores_sintacticos, Parser.ERROR, "Se espera bloque de sentencias despues del ELSE"); }
        | IF condicion_salto_if THEN else_seleccion ENDIF { agregarError(errores_sintacticos, Parser.ERROR, "Se espera bloque de sentencias despues del THEN"); }
;

then_seleccion: THEN BEGIN ejecucion END ';' {
                                                int posicion = pila.pop();
                                                polaca.set(posicion, String.valueOf(polaca.size() + 2));
                                                apilar();
                                                agregarToken("");
                                                agregarToken("#BI");
                                                agregarToken(":L" + String.valueOf(polaca.size()));
                                                }
        | THEN BEGIN ejecucion  { agregarError(errores_sintacticos, Parser.ERROR, "Se espera END al final de las sentencias del THEN");}
        | THEN BEGIN END ';' { agregarError(errores_sintacticos, Parser.ERROR, "Se esperan sentencias dentro del cuerpo del THEN");}
        | THEN ejecucion END ';' { agregarError(errores_sintacticos, Parser.ERROR, "Se esperan BEGIN luego del THEN");}
        | THEN BEGIN ejecucion END { agregarError(errores_sintacticos, Parser.ERROR, "Se esperan ';' luego del END");}
;

else_seleccion: ELSE BEGIN ejecucion END ';'
        | ELSE BEGIN END ';' {agregarError(errores_sintacticos, Parser.ERROR, "Se esperan sentencias dentro del cuerpo del ELSE ");}
        | ELSE ejecucion END ';' { agregarError(errores_sintacticos, Parser.ERROR, "Se espera BEGIN luego del ELSE"); }
;


condicion_salto_if: '(' condicion ')' {
                                apilar();
                                agregarToken("");
                                agregarToken("#BF");
                                }
        | condicion ')'         { agregarError(errores_sintacticos, Parser.ERROR, "Se espera '(' al principio de la condicion");}
        | '(' ')'               { agregarError(errores_sintacticos, Parser.ERROR, "Se espera una condicion");}
;


condicion: termino_conjuntivo
        | condicion LOGIC_OR termino_conjuntivo { agregarToken($2.sval); }
        | condicion LOGIC_OR {agregarError(errores_sintacticos, Parser.ERROR, "Se espera expresion booleana despues de comparador logico OR");}
        | LOGIC_OR termino_conjuntivo {agregarError(errores_sintacticos, Parser.ERROR, "Se espera expresion booleana antes de comparador logico OR");}      
;

termino_conjuntivo: expresion_bool 
                | termino_conjuntivo LOGIC_AND expresion_bool { agregarToken($2.sval); }
                | termino_conjuntivo LOGIC_AND {agregarError(errores_sintacticos, Parser.ERROR, "Se espera expresion booleana despues de comparador logico AND");}
                | LOGIC_AND expresion_bool {agregarError(errores_sintacticos, Parser.ERROR, "Se espera expresion booleana antes de comparador logico AND");}
;

expresion_bool: expresion comparador expresion { agregarToken($2.sval); }
        | expresion
        | expresion comparador {agregarError(errores_sintacticos, Parser.ERROR, "Se espera una expresion luego del comparador");}
        | comparador expresion {agregarError(errores_sintacticos, Parser.ERROR, "Se espera una expresion antes del comparador");}
;

comparador: COMP_DISTINTO 
        | COMP_IGUAL
        | COMP_MAYOR_IGUAL
        | COMP_MENOR_IGUAL
        | '<'
        | '>'
;

asignacion: ID ASSIGN '(' expresion ')'      {  int punt1 = TablaSimbolos.obtenerSimboloAmbito($1.sval + Parser.ambito.toString());
                                                int punt4 = TablaSimbolos.obtenerSimboloAmbito($4.sval + Parser.ambito.toString());
                                                String lexema1 = TablaSimbolos.obtenerAtributo(punt1, "lexema");
                                                String lexema4 = TablaSimbolos.obtenerAtributo(punt4, "lexema");

                                                agregarToken(lexema1); 
                                                agregarToken($2.sval);
                                                crearPunteroFuncion(lexema1, lexema4); }

        | ID ASSIGN expresion                {  int punt1 = TablaSimbolos.obtenerSimboloAmbito($1.sval + Parser.ambito.toString());
                                                int punt3 = TablaSimbolos.obtenerSimboloAmbito($3.sval + Parser.ambito.toString());
                                                String lexema1 = TablaSimbolos.obtenerAtributo(punt1, "lexema");
                                                String lexema3 = TablaSimbolos.obtenerAtributo(punt3, "lexema");

                                                agregarToken(lexema1); 
                                                agregarToken($2.sval); 
                                                crearPunteroFuncion(lexema1, lexema3); }

        | ASSIGN expresion {agregarError(errores_sintacticos, Parser.ERROR, "Se espera un identificador en el lado izquierdo de la asignacion");}
        | ID expresion  {agregarError(errores_sintacticos, Parser.ERROR, "Se espera el simbolo de asignacion  entre el identificador y la expresion");}
        | ID ASSIGN  {agregarError(errores_sintacticos, Parser.ERROR, "Se espera una expresion del lado derecho de la asignacion ");}
;

expresion: expresion '+' termino_positivo {agregarToken("+");}
        | expresion '-' termino_positivo  {agregarToken("-");}
        | termino
        | tipo '(' expresion '+' termino_positivo ')'   { agregarError(errores_sintacticos, Parser.ERROR, "Conversion explicita no permitida"); }
        | tipo '(' expresion '-' termino_positivo ')'   { agregarError(errores_sintacticos, Parser.ERROR, "Conversion explicita no permitida"); }
        | tipo '(' termino ')'                          { agregarError(errores_sintacticos, Parser.ERROR, "Conversion explicita no permitida"); }
;

termino: termino '*' factor     {agregarToken("*");}
        | termino '/' factor    {agregarToken("/");}
        | factor
;

termino_positivo: termino_positivo '*' factor {agregarToken("*");}
            | termino_positivo '/' factor {agregarToken("/");}
            | factor_positivo
;

factor: ID { int punt1 = TablaSimbolos.obtenerSimboloAmbito($1.sval + Parser.ambito.toString());
             String lexema1 = TablaSimbolos.obtenerAtributo(punt1, "lexema");
             agregarToken(lexema1);
             
             if (TablaSimbolos.obtenerAtributo(punt1, "tipo").equals(TablaTipos.FUNC_TYPE))
                funcion_a_asignar = lexema1;
            }
        | CTE  {
                int ptr_id = TablaSimbolos.obtenerSimbolo($1.sval);
                TablaSimbolos.agregarAtributo(ptr_id, "uso", "constante");
                agregarToken($1.sval);
                }
        | '-' CTE {
                        int ptr_id = TablaSimbolos.obtenerSimbolo($2.sval);
                        TablaSimbolos.agregarAtributo(ptr_id, "uso", "constante");
                        String lexema = negarConstante($2.sval);
                        agregarToken(lexema);
                }
        | ID '(' ID ')' { int punt1 = TablaSimbolos.obtenerSimboloAmbito($1.sval + Parser.ambito.toString());
                          int punt3 = TablaSimbolos.obtenerSimboloAmbito($3.sval + Parser.ambito.toString());
                          String lexema1 = TablaSimbolos.obtenerAtributo(punt1, "lexema");
                          String lexema3 = TablaSimbolos.obtenerAtributo(punt3, "lexema");
                          accionSemanticaFuncion(lexema3, lexema1); }    
        | ID '(' constante ')' { int punt1 = TablaSimbolos.obtenerSimboloAmbito($1.sval + Parser.ambito.toString());
                                 String lexema1 = TablaSimbolos.obtenerAtributo(punt1, "lexema");
                                 accionSemanticaFuncion($3.sval, lexema1); } 
        | ID '(' ')' {agregarError(errores_sintacticos, Parser.ERROR, "Se espera un parametro");}
;

factor_positivo: ID { int punt1 = TablaSimbolos.obtenerSimboloAmbito($1.sval + Parser.ambito.toString());
                      String lexema1 = TablaSimbolos.obtenerAtributo(punt1, "lexema");
                      agregarToken(lexema1);

                      if (TablaSimbolos.obtenerAtributo(punt1, "tipo").equals(TablaTipos.FUNC_TYPE))
                                funcion_a_asignar = lexema1;
                       }
                | CTE {
                        int ptr_id = TablaSimbolos.obtenerSimbolo($1.sval);
                        TablaSimbolos.agregarAtributo(ptr_id, "uso", "constante");
                        agregarToken($1.sval);
                        }
                | ID '(' ID ')' { 
                                int punt1 = TablaSimbolos.obtenerSimboloAmbito($1.sval  + Parser.ambito.toString());
                                int punt3 = TablaSimbolos.obtenerSimboloAmbito($3.sval + Parser.ambito.toString());
                                String lexema1 = TablaSimbolos.obtenerAtributo(punt1, "lexema");
                                String lexema3 = TablaSimbolos.obtenerAtributo(punt3, "lexema");

                                accionSemanticaFuncion(lexema3, lexema1); }  
                | ID '(' constante ')' {int punt1 = TablaSimbolos.obtenerSimboloAmbito($1.sval + Parser.ambito.toString());
                                        String lexema1 = TablaSimbolos.obtenerAtributo(punt1, "lexema");

                                        accionSemanticaFuncion($3.sval, lexema1); }  
                | ID '(' ')' { agregarError(errores_sintacticos, Parser.ERROR, "Se espera un parametro"); }
;

constante: CTE  {
                int ptr_id = TablaSimbolos.obtenerSimbolo($1.sval);
                
                TablaSimbolos.agregarAtributo(ptr_id, "uso", "constante");
                }
        | '-' CTE {
                        int ptr_id = TablaSimbolos.obtenerSimbolo($2.sval);
                        TablaSimbolos.agregarAtributo(ptr_id, "uso", "constante");
                        String lexema = negarConstante($2.sval);
                }
;

impresion: PRINT '(' CADENA ')' {       
                                String nombre = STRING_CHAR + "cadena" + String.valueOf(contador_cadenas);
                                String valor = $3.sval;
                                String tipo = "string";
                                TablaSimbolos.agregarSimbolo(nombre);
                                int puntero = TablaSimbolos.obtenerSimbolo(nombre);
                                TablaSimbolos.agregarAtributo(puntero, "valor", valor);
                                TablaSimbolos.agregarAtributo(puntero, "tipo", tipo);
                                agregarToken(nombre);    //agregamos a la polaca el simbolo, junto identificador de cadenas, a la polaca 
                                contador_cadenas++; }

        | PRINT '(' ')'         {agregarError(errores_sintacticos, Parser.ERROR, "Se espera una cadena dentro del PRINT");}
        | PRINT                 {agregarError(errores_sintacticos, Parser.ERROR, " Se esperan ( ) con una cadena a continuacion del PRINT ");}
;


%%

public static boolean declarando = true;

public static final String ERROR = "Error";
public static final String WARNING = "Warning";
public static final String NAME_MANGLING_CHAR = "@";

public static final String nombreVariableContrato = "@contrato";
public static String funcion_a_asignar = "";

public static StringBuilder ambito = new StringBuilder();

public static final List<String> errores_lexicos = new ArrayList<>();
public static final List<String> errores_sintacticos = new ArrayList<>();
public static final List<String> errores_semanticos = new ArrayList<>();


public static final List<Integer> posicionesPolaca = new ArrayList<>();
public static final List<String> polaca = new ArrayList<>();
public static final Stack<Integer> pila = new Stack<>();
public static final Stack<List<Integer>> pilaBreak = new Stack<>();

private static boolean errores_compilacion;

private static String tipo;

private static int contador_cadenas = 0;
public static final String STRING_CHAR = "&";

void yyerror(String mensaje) {
        // funcion utilizada para imprimir errores que produce yacc
        System.out.println("Error yacc: " + mensaje);
}

int yylex() {
        int identificador_token = 0;
        Reader lector = AnalizadorLexico.lector;
        AnalizadorLexico.estado_actual = 0;

        // Leo hasta que el archivo termine
        while (true) {
                try {
                        if (FileHelper.endOfFile(lector)) {
                                break;
                        }

                        char caracter = FileHelper.getNextCharWithoutAdvancing(lector);
                        identificador_token = AnalizadorLexico.cambiarEstado(lector, caracter);

                        // Si llego a un estado final
                        if (identificador_token != AccionSemantica.TOKEN_ACTIVO) {
                                yylval = new ParserVal(AnalizadorLexico.token_actual.toString());
                                AnalizadorLexico.token_actual.delete(0, AnalizadorLexico.token_actual.length());
                                return identificador_token;
                        }
                } catch (IOException e) {
                        e.printStackTrace();
                }
        }

        return identificador_token;
}

public String negarConstante(String constante) {
        // Si la constante es un numero DOUBLE, la negamos antes de que se agrege a la tabla de simbolos
        int puntero = TablaSimbolos.obtenerSimbolo(constante);
        String nuevo_lexema;

        if (constante.contains(".")) {
                nuevo_lexema = '-' + constante;
        } else {
                agregarError(errores_sintacticos, Parser.WARNING, "El numero largo -" + constante + 
                                " fue truncado al valor minimo, ya que es menor que este mismo");
                nuevo_lexema = "0";
        }

        TablaSimbolos.agregarAtributo(puntero, TablaSimbolos.LEXEMA, nuevo_lexema);
        return nuevo_lexema;
}

public static void agregarError(List<String> errores, String tipo, String error) {
        if (tipo == Parser.ERROR) {
                errores_compilacion = true;
        }

        int linea_actual = AnalizadorLexico.getLineaActual();

        errores.add(tipo + " (Linea " + linea_actual + "): " + error);
}

public static void agregarErrorSemantico(int linea, String error){
        errores_compilacion = true;
        errores_semanticos.add(Parser.ERROR + " (Linea " + linea + "): " + error);
}

public static boolean pertenece(String simbolo) {
        // funcion recursiva para controlar si un simbolo se encuentra en la tabla de simbolos
        if (!simbolo.contains(NAME_MANGLING_CHAR)) {
                return false;
        } else if (TablaSimbolos.obtenerSimbolo(simbolo) != TablaSimbolos.NO_ENCONTRADO) {
                return true;
        } else {
                int index = simbolo.lastIndexOf(NAME_MANGLING_CHAR);
                simbolo = simbolo.substring(0, index);
                return pertenece(simbolo);
        }
}

public static void crearPunteroFuncion(String puntero_funcion, String funcion_llamada) {
        //tomo el tipo de dato de funcion_asignada y funcion de la tabla de simbolos
        int puntero_funcion_asignada = TablaSimbolos.obtenerSimbolo(puntero_funcion);
        int puntero_funcion_llamada = TablaSimbolos.obtenerSimbolo(funcion_llamada);

        String tipo_puntero = TablaSimbolos.obtenerAtributo(puntero_funcion_asignada, "tipo");
        String retorno_funcion_llamada = TablaSimbolos.obtenerAtributo(puntero_funcion_llamada, "retorno");
        
        boolean retorna_funciones = funcion_a_asignar.equals("") && retorno_funcion_llamada.equals(TablaTipos.FUNC_TYPE);
        boolean es_funcion = !funcion_llamada.equals("");
        
        //pregunto si ninguno de ellos es distinto del tipo string
        if (tipo_puntero.equals(TablaTipos.FUNC_TYPE) && (es_funcion || retorna_funciones)) {
                //verifico que el atributo 'uso' del simbolo puntero sea: PUNTERO_FUNCION
        	int puntero_funcion_a_copiar;

                if (retorna_funciones) {
                        String lexema_a_copiar = TablaSimbolos.obtenerAtributo(puntero_funcion_llamada, "nombre_retorno");
                        puntero_funcion_a_copiar = TablaSimbolos.obtenerSimbolo(lexema_a_copiar);
                } else {
                        puntero_funcion_a_copiar = TablaSimbolos.obtenerSimbolo(funcion_a_asignar);
                }

                String uso_puntero = TablaSimbolos.obtenerAtributo(puntero_funcion_asignada, "uso");
                
                if (uso_puntero.equals("variable")) {
                        //agrego a los atributos de puntero_funcion todos los atributos de funcion en la tabla de simbolos, con excepcion del atributo 'uso' y 'lexema'
                        Map<String,String> atributos = TablaSimbolos.obtenerAtributos(puntero_funcion_a_copiar);
                        assert atributos != null;

                        TablaSimbolos.agregarAtributo(puntero_funcion_asignada, "funcion_asignada", atributos.get("lexema"));

                        for (String atributo : atributos.keySet()) {
                                if (atributo.equals("uso") || atributo.equals("lexema")) continue;  //no agrego el atributo uso
                                
                                TablaSimbolos.agregarAtributo(puntero_funcion_asignada, atributo, atributos.get(atributo));
                        }
                }

                funcion_a_asignar = "";   // reiniciamos la funcion a asignar           
        }
}

private static void cambiarAmbito(String nuevo_ambito) {
        //recibe el ID de una funcion, y lo concantenac con ambito 
        ambito.append(NAME_MANGLING_CHAR).append(nuevo_ambito);
}

private static void salirAmbito() {
        //la funcion salirAmbito modifica el atributo ambito, quitandole todos los caracteres hasta el ':'
        int index = ambito.lastIndexOf(NAME_MANGLING_CHAR);
        ambito.delete(index, ambito.length());
}

public static boolean chequearParametro(String parametro, String funcion) {
        //esta funcion chequea si el tipo de un parametro es valido para una funcion
        int puntero_parametro = TablaSimbolos.obtenerSimboloAmbito(parametro);
        int puntero_funcion = TablaSimbolos.obtenerSimboloAmbito(funcion);
        
        String tipoParametro = TablaSimbolos.obtenerAtributo(puntero_parametro, "tipo");
        String tipoFuncion = TablaSimbolos.obtenerAtributo(puntero_funcion, "tipo_parametro");
        
        return tipoParametro == tipoFuncion;
}

public static void accionSemanticaFuncion(String parametro, String funcion) {
        //esta funcion se encarga de verificar el parametro dentro de la funcion, y agrega un error en caso de haberlo
        if (chequearParametro(parametro, funcion)) {
                agregarToken(funcion_a_asignar);
                agregarToken(funcion);
                agregarToken(parametro);
                agregarToken("#CALL");
        } else {
                agregarError(errores_semanticos, Parser.ERROR, "El tipo del parametro es distinto al provisto");
        }
}

public static void agregarToken(String token) {
        //la funcion agregarToken agrega tokens al final de la polaca
        polaca.add(token);
        posicionesPolaca.add(AnalizadorLexico.getLineaActual());
}

public static void apilar(){
        //agrega una nueva posicion a la pila, correspondiente a lo ultimo que se encontro en la polaca
        pila.push(polaca.size());
}

public static void apilarBreak(){
        //la funcion apilarBreak agrega una posicion a la lista de posiciones donde se encuentran los break de un REPEAT
        List<Integer> posBreakes = pilaBreak.pop(); 
        posBreakes.add(polaca.size());
        pilaBreak.push(posBreakes);
}

public static void agregarListaBreak(){
        //la funcion agregarListaBreak agrega una lista de posiciones a la pila de posiciones donde se encuentran los break de un REPEAT
        List<Integer> nuevaLista = new ArrayList<>();
        pilaBreak.push(nuevaLista);
}

public static List<Integer> getListaBreakes(){
        //la funcion getListaBreakes obtiene la lista de posiciones donde se encuentran los break de un REPEAT
        return pilaBreak.pop();
}


//--FUNCIONES DE IMPRESION Y MAIN--//

public static void imprimirErrores(List<String> errores, String cabecera) {
        // Imprimo los errores encontrados en el programa
        if (!errores.isEmpty()) {
                System.out.println();
                System.out.println(cabecera + ":");

                for (String error: errores) {
                        System.out.println(error);
                }
        }
}

public static void imprimirPolaca() {
        // Imprimo la polaca inversa generada en el programa
        if (!polaca.isEmpty()) {
                System.out.println();
                System.out.println("Polaca:");

                for (int i = 0; i < polaca.size(); ++i) {
                        System.out.println(i + " " + polaca.get(i));
                }
        }
}

private static String nombreFuncion() {
        // Ultimo name mangling char
        int ultimo_nmc = ambito.lastIndexOf(NAME_MANGLING_CHAR);
        String nombre_funcion = ambito.substring(ultimo_nmc + 1);
        return nombre_funcion + ambito.substring(0, ultimo_nmc);
}

public static void main(String[] args) {
        if (args.length > 1) {
                String archivo_a_leer = args[0];
                System.out.println("Se esta compilando el siguiente archivo: " + archivo_a_leer);

                try {
                        AnalizadorLexico.lector = new BufferedReader(new FileReader(archivo_a_leer));
                        Parser parser = new Parser();
                        parser.run();
                } catch (IOException excepcion) {
                        excepcion.printStackTrace();
                }
                
                Parser.imprimirErrores(errores_lexicos, "Errores Lexicos");
                Parser.imprimirErrores(errores_sintacticos, "Errores Sintacticos");

                if (!errores_compilacion) {
                        GeneradorCodigo.generarCodigo();
                        FileHelper.writeProgram(args[1], GeneradorCodigo.codigo.toString());
                }

                Parser.imprimirErrores(errores_semanticos, "Errores Semanticos");
                Parser.imprimirPolaca();
                TablaSimbolos.imprimirTabla();
        } else {
                System.out.println("No se especifico el archivo a compilar");
        }
}