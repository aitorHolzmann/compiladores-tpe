package compilador;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TablaSimbolos {
    public static final int NO_ENCONTRADO = -1;
    public static final String NO_ENCONTRADO_S = "No encontrado";
    public static final String LEXEMA = "lexema";

    private static final Map<Integer, Map<String, String>> simbolos = new HashMap<>();
                           
    private static int identificador_siguiente = 1;

    public static Set<Integer> obtenerConjuntoPunteros() {
        return simbolos.keySet();
    }

    // TODO Habria que verificar que el simbolo no este?
    public static void agregarSimbolo(String simbolo_nuevo) {
        Map<String, String> atributos = new HashMap<>();
        atributos.put(LEXEMA, simbolo_nuevo);
        simbolos.put(identificador_siguiente, atributos);
        ++identificador_siguiente;
    }

    public static int obtenerSimbolo(String lexema) {
        for (Map.Entry<Integer, Map<String, String>> entrada: simbolos.entrySet()) {
            String lexema_actual = entrada.getValue().get(LEXEMA);

            if (lexema_actual.equals(lexema)) {
                return entrada.getKey();
            }
        }

        return NO_ENCONTRADO;
    }

    public static int obtenerSimboloAmbito(String lexema) {
        //Dado un lexema, retorna su puntero a la tabla de simbolos
        int puntero = TablaSimbolos.obtenerSimbolo(lexema);

        if (puntero != TablaSimbolos.NO_ENCONTRADO) {
            return puntero;
        } else if (lexema.contains(Parser.NAME_MANGLING_CHAR)) {
            int index = lexema.lastIndexOf(Parser.NAME_MANGLING_CHAR);
            lexema = lexema.substring(0, index);
            return obtenerSimboloAmbito(lexema);
        } else {
            return NO_ENCONTRADO;
        }
    }

    public static int obtenerParametro(String funcion) {
        int punt_funcion = obtenerSimbolo(funcion);
        String uso = obtenerAtributo(punt_funcion, "uso");

        if (uso.equals("variable")) {
            funcion = obtenerAtributo(punt_funcion, "funcion_asignada");
        }

        int primer_nmc = funcion.indexOf(Parser.NAME_MANGLING_CHAR);
        String ambito = funcion.substring(primer_nmc + 1) + Parser.NAME_MANGLING_CHAR + funcion.substring(0, primer_nmc);

        for (Map.Entry<Integer, Map<String, String>> entrada_i: simbolos.entrySet()) {
            Map<String, String> parametros_i = entrada_i.getValue();

            if (!parametros_i.containsKey("uso")) continue;
        
            if (parametros_i.get("uso").equals("parametro") && parametros_i.get("lexema").endsWith(ambito)) {
                return entrada_i.getKey();
            }
        }

        return NO_ENCONTRADO;
    }

    public static void eliminarSimbolo(int clave) {
        simbolos.remove(clave);
    }

    public static String obtenerUso(String lexema){
        int id = obtenerSimbolo(lexema);
        return obtenerAtributo(id, "uso");
    }

    // Agregar un simbolo en simbolos dado su clave, un string 'atributo', un string 'valor', si es que pertenece al mapa
    // Si el atributo ya esta dentro del mapa de atributos del simbolo, su valor se sobreescribira
    public static void agregarAtributo(int clave, String atributo, String valor) {
        if (simbolos.containsKey(clave)) {
            Map<String, String> atributos = simbolos.get(clave);
            atributos.put(atributo, valor);
        }  
    }

    //obtengo un atributo de un simbolo, dado su clave y un string 'atributo', si es que pertenece al mapa
    public static String obtenerAtributo(int clave, String atributo) {
        if (simbolos.containsKey(clave)) {
            Map<String, String> atributos = simbolos.get(clave);

            if (atributos.containsKey(atributo)) {
                return atributos.get(atributo);
            }
        }
        return NO_ENCONTRADO_S;
    }

    //obtengo todos los atributos de un simbolo, dado su clave, si es que pertenece al mapa
    public static Map<String, String> obtenerAtributos(int clave) {
        if (simbolos.containsKey(clave)) {
            return simbolos.get(clave);
        }
        return null;
    }

    //imprimo simbolos entera
    public static void imprimirTabla() {
        System.out.println("\nTablaSimbolos:");

        for (Map.Entry<Integer, Map<String, String>> entrada: simbolos.entrySet()) {
            Map<String, String> atributos = entrada.getValue();
            System.out.print(entrada.getKey() + ": ");

            for (Map.Entry<String, String> atributo: atributos.entrySet()) {
                System.out.print("(" + atributo.getKey() + ": " + atributo.getValue() + ") ");
            }

            System.out.println();
        }
    }
}
