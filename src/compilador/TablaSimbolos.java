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
        String mangling = nombre + ambito; //suma, shortint, funcion, :main

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
        atributos.put(LEXEMA, nombre); //Este esta repetido si estoy rehutilizando no?
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
            Map<String, String> atributos = tabla_simbolos.get(ref);
            int cant = 0;
            if (atributos.containsKey("CANT_PARAMS")) {
                try {
                    cant = Integer.parseInt(atributos.get("CANT_PARAMS"));
                } catch (NumberFormatException e) {
                    cant = 0;
                }
            }
            
            atributos.put("PARAM_TIPO_" + cant, tipoParam);
            atributos.put("PARAM_NOMBRE_" + cant, nombreParamMangled);
            atributos.put("CANT_PARAMS", String.valueOf(cant + 1));
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

    // Metodos auxiliares para COMPTIME (Tema 22)
    public static boolean esComptime(int ref) {
        return "true".equals(obtenerAtributo(ref, "COMPTIME"));
    }

    public static void guardarValorComptime(int ref, String valor) {
        agregarAtributo(ref, "VALOR_COMPTIME", valor);
    }

    public static String obtenerValorComptime(int ref) {
        String val = obtenerAtributo(ref, "VALOR_COMPTIME");
        if (ATRIBUTO_NO_ENCONTRADO.equals(val)) {
            return null;
        }
        return val;
    }

    public static boolean tieneValorComptime(int ref) {
        return obtenerValorComptime(ref) != null;
    }

    // ===== HITO 6.3: CLASES, HERENCIA Y VISIBILIDAD (Temas 26, 29, 30) =====

    public static void agregarAtributoAClase(String claseMangled, String nombreAtr, String tipoAtr) {
        int ref = buscarPorMangling(claseMangled);
        if (ref != LEXEMA_NO_ENCONTRADO) {
            Map<String, String> atributos = tabla_simbolos.get(ref);
            int cant = 0;
            if (atributos.containsKey("CANT_ATRIBUTOS")) {
                try {
                    cant = Integer.parseInt(atributos.get("CANT_ATRIBUTOS"));
                } catch (NumberFormatException e) {
                    cant = 0;
                }
            }
            atributos.put("ATR_NOMBRE_" + cant, nombreAtr);
            atributos.put("ATR_TIPO_" + cant, tipoAtr);
            atributos.put("CANT_ATRIBUTOS", String.valueOf(cant + 1));
        }
    }

    public static int obtenerCantidadAtributosClase(String claseMangled) {
        int ref = buscarPorMangling(claseMangled);
        if (ref != LEXEMA_NO_ENCONTRADO) {
            Map<String, String> atrs = tabla_simbolos.get(ref);
            if (atrs.containsKey("CANT_ATRIBUTOS")) {
                try {
                    return Integer.parseInt(atrs.get("CANT_ATRIBUTOS"));
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
        }
        return 0;
    }

    public static String obtenerNombreAtributoClase(String claseMangled, int indice) {
        int ref = buscarPorMangling(claseMangled);
        if (ref != LEXEMA_NO_ENCONTRADO) {
            return tabla_simbolos.get(ref).get("ATR_NOMBRE_" + indice);
        }
        return null;
    }

    public static String obtenerTipoAtributoClase(String claseMangled, int indice) {
        int ref = buscarPorMangling(claseMangled);
        if (ref != LEXEMA_NO_ENCONTRADO) {
            return tabla_simbolos.get(ref).get("ATR_TIPO_" + indice);
        }
        return null;
    }

    public static void registrarHerencia(String claseMangled, ArrayList<String> padres) {
        int ref = buscarPorMangling(claseMangled);
        if (ref != LEXEMA_NO_ENCONTRADO && padres != null) {
            Map<String, String> atrs = tabla_simbolos.get(ref);
            atrs.put("CANT_HERENCIA", String.valueOf(padres.size()));
            for (int i = 0; i < padres.size(); i++) {
                atrs.put("HEREDA_" + i, padres.get(i));
            }
        }
    }

    public static java.util.ArrayList<String> obtenerHerencia(String claseMangled) {
        java.util.ArrayList<String> res = new java.util.ArrayList<>();
        int ref = buscarPorMangling(claseMangled);
        if (ref != LEXEMA_NO_ENCONTRADO) {
            Map<String, String> atrs = tabla_simbolos.get(ref);
            if (atrs.containsKey("CANT_HERENCIA")) {
                try {
                    int cant = Integer.parseInt(atrs.get("CANT_HERENCIA"));
                    for (int i = 0; i < cant; i++) {
                        res.add(atrs.get("HEREDA_" + i));
                    }
                } catch (NumberFormatException e) {
                }
            }
        }
        return res;
    }

    public static void registrarImports(String claseMangled, java.util.ArrayList<String> imports) {
        int ref = buscarPorMangling(claseMangled);
        if (ref != LEXEMA_NO_ENCONTRADO && imports != null) {
            Map<String, String> atrs = tabla_simbolos.get(ref);
            atrs.put("CANT_IMPORTS", String.valueOf(imports.size()));
            for (int i = 0; i < imports.size(); i++) {
                atrs.put("IMPORT_" + i, imports.get(i));
            }
        }
    }

    public static java.util.ArrayList<String> obtenerImports(String claseMangled) {
        java.util.ArrayList<String> res = new java.util.ArrayList<>();
        int ref = buscarPorMangling(claseMangled);
        if (ref != LEXEMA_NO_ENCONTRADO) {
            Map<String, String> atrs = tabla_simbolos.get(ref);
            if (atrs.containsKey("CANT_IMPORTS")) {
                try {
                    int cant = Integer.parseInt(atrs.get("CANT_IMPORTS"));
                    for (int i = 0; i < cant; i++) {
                        res.add(atrs.get("IMPORT_" + i));
                    }
                } catch (NumberFormatException e) {
                }
            }
        }
        return res;
    }

    public static void registrarExport(int refMiembro, java.util.ArrayList<String> exports) {
        if (tabla_simbolos.containsKey(refMiembro) && exports != null) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < exports.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(exports.get(i));
            }
            tabla_simbolos.get(refMiembro).put("EXPORT_TO", sb.toString());
        }
    }

    public static boolean estaExportadoA(int refMiembro, String claseDestino) {
        String exp = obtenerAtributo(refMiembro, "EXPORT_TO");
        if (exp == null || ATRIBUTO_NO_ENCONTRADO.equals(exp)) {
            return false;
        }
        String[] partes = exp.split(",");
        for (String p : partes) {
            if (p.trim().equals(claseDestino)) {
                return true;
            }
        }
        return false;
    }

    public static int buscarMiembroEnJerarquia(String nomClase, String nombreMiembro) {
        return buscarMiembroEnJerarquia(nomClase, nombreMiembro, new java.util.HashSet<String>());
    }

    private static int buscarMiembroEnJerarquia(String nomClase, String nombreMiembro, Set<String> visitados) {
        if (nomClase == null || visitados.contains(nomClase)) {
            return LEXEMA_NO_ENCONTRADO;
        }
        visitados.add(nomClase);

        //Buscar en la propia clase
        int ref = buscarPorMangling(nombreMiembro + ":main:" + nomClase);
        if (ref != LEXEMA_NO_ENCONTRADO) {
            return ref;
        }

        //Buscar en orden en la lista EXTENDS (Tema 30: Desambiguado Opcion 1)
        java.util.ArrayList<String> padres = obtenerHerencia(nomClase + ":main");
        if (padres != null) {
            for (String padre : padres) {
                int refPadre = buscarMiembroEnJerarquia(padre, nombreMiembro, visitados);
                if (refPadre != LEXEMA_NO_ENCONTRADO) {
                    return refPadre;
                }
            }
        }
        return LEXEMA_NO_ENCONTRADO;
    }

    public static void imprimirTabla() {
        System.out.println("=== TABLA DE SIMBOLOS ===");
        for (Map.Entry<Integer, Map<String, String>> entry : tabla_simbolos.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }
}