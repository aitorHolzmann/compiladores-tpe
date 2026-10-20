package src.compilador;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class TablaSimbolos {
    public static final int LEXEMA_NO_ENCONTRADO = -1;
    public static final String ATRIBUTO_NO_ENCONTRADO = "NO ENCONTRADO";
    
    public static final String LEXEMA = "LEXEMA";
    public static final String MANGLING = "MANGLING";
    public static final String TIPO = "TIPO";
    public static final String USO = "USO";
    public static final String AMBITO = "AMBITO";

    // La tabla se accede con el identificador numerico (>256).
    private static final Map<Integer, Map<String, String>> tabla_simbolos = new LinkedHashMap<>();
    
    private static int siguiente_identificador = 1; 

    public static void reiniciar() {
        tabla_simbolos.clear();
        siguiente_identificador = 1;
    }
    
    public static int agregarSimbolo(String nombre_lexema){
        Map<String, String> atributos = new HashMap<>();
        atributos.put(LEXEMA, nombre_lexema);

        int id_asignado = siguiente_identificador;
        tabla_simbolos.put(id_asignado, atributos);

        siguiente_identificador++;
        return id_asignado;
    }

    public static int obtenerSimbolo(String lexema_buscado){
        for (Map.Entry<Integer, Map<String, String>> entrada_tabla_simbolos: tabla_simbolos.entrySet()) {
            String nombre_lexema_actual = entrada_tabla_simbolos.getValue().get(LEXEMA);
            if (nombre_lexema_actual != null && nombre_lexema_actual.equals(lexema_buscado)){
                return entrada_tabla_simbolos.getKey();
            }
        } 
        return LEXEMA_NO_ENCONTRADO;
    }

    public static void agregarAtributo(int clave, String atributo, String valor) {
        if (tabla_simbolos.containsKey(clave)) {
            tabla_simbolos.get(clave).put(atributo, valor);
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

    // Incrementa directamente el contador de ocurrencias de una entrada existente.
    private static void incrementarOcurrencias(int id) {
        Map<String, String> atributos = tabla_simbolos.get(id);
        String ocs = atributos.get("OCURRENCIAS");
        if (ocs != null) {
            atributos.put("OCURRENCIAS", String.valueOf(Integer.parseInt(ocs) + 1));
        } else {
            atributos.put("OCURRENCIAS", "1");
        }
    }

    // Agrega la constante si no existe, o incrementa su contador de ocurrencias.
    // El tipo no se guarda: lo determina el token devuelto por el lexico.
    public static int agregarOBuscarConstante(String lexema) {
        int id = obtenerSimbolo(lexema);
        if (id == LEXEMA_NO_ENCONTRADO) {
            id = agregarSimbolo(lexema);
            tabla_simbolos.get(id).put("OCURRENCIAS", "1");
        } else {
            incrementarOcurrencias(id);
        }
        return id;
    }

    // Convierte una constante positiva a su version negativa en la tabla.
    // No se valida rango: el lexico ya garantiza que el positivo es valido,
    // por lo que su negado tambien lo es.
    public static int convertirANegativo(String lexema_positivo) {
        int id_pos = obtenerSimbolo(lexema_positivo);

        if (id_pos != LEXEMA_NO_ENCONTRADO) {
            Map<String, String> atributos = tabla_simbolos.get(id_pos);
            String ocsStr = atributos.get("OCURRENCIAS");
            if (ocsStr != null) {
                int ocs = Integer.parseInt(ocsStr) - 1;
                if (ocs <= 0) {
                    tabla_simbolos.remove(id_pos);
                } else {
                    atributos.put("OCURRENCIAS", String.valueOf(ocs));
                }
            }
        }

        return agregarOBuscarConstante("-" + lexema_positivo);
    }

    public static int declararVariable(String nombre, String tipo, String uso, String ambito) {
        String mangling = nombre + ambito;

        //Chequeo de redeclaracion en el mismo ambito
        for (Map.Entry<Integer, Map<String, String>> entrada : tabla_simbolos.entrySet()) {
            Map<String, String> atributos = entrada.getValue();
            String m = atributos.get(MANGLING);
            if (m != null && m.equals(mangling)) {
                Parser.agregarErrorSemantico("Redeclaracion de '" + nombre + "' en el ambito " + ambito);
                return LEXEMA_NO_ENCONTRADO;
            }
        }

        //Buscar si existe una entrada sin ambito asignado (Primera declaracion con ese ID)
        int idReusar = LEXEMA_NO_ENCONTRADO; 
        for (Map.Entry<Integer, Map<String, String>> entrada : tabla_simbolos.entrySet()) {
            Map<String, String> atributos = entrada.getValue();
            if (nombre.equals(atributos.get(LEXEMA)) && atributos.get(MANGLING) == null) {
                idReusar = entrada.getKey();
                break;
            }
        }

        int id = (idReusar != LEXEMA_NO_ENCONTRADO) ? idReusar : siguiente_identificador++;
        Map<String, String> atributos = (idReusar != LEXEMA_NO_ENCONTRADO) ? tabla_simbolos.get(id) : new HashMap<>();
        atributos.put(LEXEMA, nombre);
        atributos.put(MANGLING, mangling);
        atributos.put(TIPO, tipo);
        atributos.put(USO, uso);
        atributos.put(AMBITO, ambito);
        tabla_simbolos.put(id, atributos);
        return id;
    }

    public static int buscarIdentificador(String nombre, String ambito) {
        String ambitoBusqueda = ambito;
        while (ambitoBusqueda != null && ambitoBusqueda.contains(":")) {
            String manglingBuscado = nombre + ambitoBusqueda;
            for (Map.Entry<Integer, Map<String, String>> entrada : tabla_simbolos.entrySet()) {
                Map<String, String> atributos = entrada.getValue();
                String m = atributos.get(MANGLING);
                if (m != null && m.equals(manglingBuscado)) {
                    return entrada.getKey();
                }
            }
            int ultimoSeparador = ambitoBusqueda.lastIndexOf(':');
            ambitoBusqueda = ambitoBusqueda.substring(0, ultimoSeparador);
        }
        return LEXEMA_NO_ENCONTRADO;
    }

    public static int buscarPorMangling(String manglingBuscado) {
        for (Map.Entry<Integer, Map<String, String>> entrada : tabla_simbolos.entrySet()) {
            Map<String, String> atributos = entrada.getValue();
            String m = atributos.get(MANGLING);
            if (m != null && m.equals(manglingBuscado)) {
                return entrada.getKey();
            }
        }
        return LEXEMA_NO_ENCONTRADO;
    }

    public static void actualizarTipo(String manglingBuscado, String nuevoTipo) {
        int ref = buscarPorMangling(manglingBuscado);
        if (ref != LEXEMA_NO_ENCONTRADO) {
            tabla_simbolos.get(ref).put(TIPO, nuevoTipo);
        }
    }

    public static void agregarParametroAFuncion(String funcionMangled, String tipoParam, String nombreParamMangled) {
        int ref = buscarPorMangling(funcionMangled);
        if (ref != LEXEMA_NO_ENCONTRADO) {
            Map<String, String> atrs = tabla_simbolos.get(ref);
            int cant = 0;
            if (atrs.containsKey("CANT_PARAMS")) {
                try {
                    cant = Integer.parseInt(atrs.get("CANT_PARAMS"));
                } catch (NumberFormatException e) {
                    cant = 0;
                }
            }
            atrs.put("PARAM_TIPO_" + cant, tipoParam);
            atrs.put("PARAM_NOMBRE_" + cant, nombreParamMangled);
            atrs.put("CANT_PARAMS", String.valueOf(cant + 1));
        }
    }

    public static int obtenerCantidadParametros(String funcionMangled) {
        int ref = buscarPorMangling(funcionMangled);
        if (ref != LEXEMA_NO_ENCONTRADO) {
            Map<String, String> atrs = tabla_simbolos.get(ref);
            if (atrs.containsKey("CANT_PARAMS")) {
                try {
                    return Integer.parseInt(atrs.get("CANT_PARAMS"));
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
        }
        return 0;
    }

    public static String obtenerTipoParametro(String funcionMangled, int indice) {
        int ref = buscarPorMangling(funcionMangled);
        if (ref != LEXEMA_NO_ENCONTRADO) {
            return tabla_simbolos.get(ref).get("PARAM_TIPO_" + indice);
        }
        return null;
    }

    public static String obtenerNombreParametro(String funcionMangled, int indice) {
        int ref = buscarPorMangling(funcionMangled);
        if (ref != LEXEMA_NO_ENCONTRADO) {
            return tabla_simbolos.get(ref).get("PARAM_NOMBRE_" + indice);
        }
        return null;
    }

    public static void imprimirTabla() {
        System.out.println("=== TABLA DE SIMBOLOS ===");
        for (Map.Entry<Integer, Map<String, String>> entry : tabla_simbolos.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }
}