package src.compilador;

import java.util.HashMap;
import java.util.Map;

import src.token.Token;
import src.token.TokenIdentificador;

public class TablaSimbolos {
    public static final int LEXEMA_NO_ENCONTRADO = -1;
    public static final String ATRIBUTO_NO_ENCONTRADO = "NO ENCONTRADO";
    
    public static final String LEXEMA = "LEXEMA";
    //La tabla de simbolos se accede con el numero de identifcador (despuse de ASCII ej 256).
    //Y tiene mapa de atributos. Por ejemplo si es entero o flotante.
    private static final Map<Integer, Token> tabla_simbolos = new HashMap<>();
    
    
    //TODO: hay que ver cuantas palabras reservadas tenemos. Despues de la ultima palabra reservada podemos empezar a contar simbolos.
    private static int siguiente_identificador = 1; 
    
    
    // CORRECCION: agregarSimbolo ahora DEVUELVE el identificador asignado.
    // Antes era void, y no habia forma de saber en que fila de la tabla
    // quedo guardado el simbolo recien creado. Esto es necesario para
    // poder setear AnalizadorLexico.referenciaTablaSimbolos (yylval) desde
    // las acciones semanticas (ver AS3 / AS6 corregidos).
    public static int agregarSimbolo(Token nuevo_token){
        int id_asignado = siguiente_identificador;
        tabla_simbolos.put(id_asignado, nuevo_token);

        siguiente_identificador++;
        return id_asignado;
    }

    //Recorro cada fila de la tabla. Cada fila es un par <Integer, Map<String, String>>.
    //                                                                 LEXEMA, ACA
    //Si el lexema fue agregado, su nombre va a estar en la marca "ACA".
    public static int obtenerSimbolo(String lexema_buscado){
        for (Map.Entry<Integer, Token> entrada_tabla_simbolos: tabla_simbolos.entrySet()) {
            String nombre_lexema_actual = entrada_tabla_simbolos.getValue().getLexema();

            if (nombre_lexema_actual != null && nombre_lexema_actual.equals(lexema_buscado)){
                return entrada_tabla_simbolos.getKey();
            }
        } 

        return LEXEMA_NO_ENCONTRADO;
    }

    public static void agregarAtributo(int clave, String atributo, String valor) {
        if (tabla_simbolos.containsKey(clave)) {
            Map<String, String> atributos = tabla_simbolos.get(clave);
            atributos.put(atributo, valor);
        }  
    }

    public static Map<String, String> obtenerTodosAtributos(int clave) {
        if (tabla_simbolos.containsKey(clave)) {
            return tabla_simbolos.get(clave);
        }
        return null;
    }

    public static String obtenerAtributo(int clave, String atributo) {
        if (tabla_simbolos.containsKey(clave)) {
            Map<String, String> atributos = tabla_simbolos.get(clave);

            if (atributos.containsKey(atributo)) {
                return atributos.get(atributo);
            }
        }
        return ATRIBUTO_NO_ENCONTRADO;
    }
}