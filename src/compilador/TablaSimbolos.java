package src.compilador;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class TablaSimbolos {
    public static final String SEPARADOR = "@";
    public static final String AMBITO_GLOBAL = "main";
    public static final String TIPO = "TIPO";
    public static final String USO = "USO";

    // Almacena simbolos indexados por su clave mangled o lexema de constante
    private static final Map<String, Map<String, Object>> tabla_simbolos = new LinkedHashMap<>();

    public static void reiniciar() {
        tabla_simbolos.clear();
    }

    // El lexico registra el identificador; al declararse pasa a tener ambito
    public static void agregarIdentificador(String lexema) {
        if (!tabla_simbolos.containsKey(lexema)) {
            tabla_simbolos.put(lexema, new HashMap<>());
        }
    }

    // Purga entradas de identificadores sin ambito que no llegaron a declararse
    public static void purgarIdentificadoresSinDeclarar() {
        Iterator<Map.Entry<String, Map<String, Object>>> it = tabla_simbolos.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Map<String, Object>> entry = it.next();
            String clave = entry.getKey();
            Map<String, Object> atr = entry.getValue();
            if (!clave.contains(SEPARADOR) && (atr == null || atr.isEmpty())) {
                it.remove();
            }
        }
    }

    // Alta o busqueda de constante registrando ocurrencias y tipo
    public static String agregarOBuscarConstante(String lexema, String tipo) {
        if (tabla_simbolos.containsKey(lexema)) {
            incrementarOcurrencias(lexema);
            if (tipo != null) {
                Map<String, Object> atr = tabla_simbolos.get(lexema);
                if (atr != null && !atr.containsKey(TIPO)) {
                    atr.put(TIPO, tipo);
                }
            }
            return lexema;
        }
        Map<String, Object> atributos = new HashMap<>();
        atributos.put("OCURRENCIAS", 1);
        if (tipo != null) {
            atributos.put(TIPO, tipo);
        }
        tabla_simbolos.put(lexema, atributos);
        return lexema;
    }

    public static String agregarOBuscarConstante(String lexema) {
        return agregarOBuscarConstante(lexema, null);
    }

    public static void incrementarOcurrencias(String clave) {
        Map<String, Object> atr = tabla_simbolos.get(clave);
        if (atr != null) {
            Integer ocs = (Integer) atr.get("OCURRENCIAS");
            int valor = (ocs != null) ? ocs + 1 : 1;
            atr.put("OCURRENCIAS", valor);
        }
    }

    // Convierte constante positiva a negativa decrementando ocurrencias
    public static String convertirANegativo(String lexemaPositivo) {
        Map<String, Object> atrPos = tabla_simbolos.get(lexemaPositivo);
        String tipoPrevio = null;
        if (atrPos != null) {
            tipoPrevio = (String) atrPos.get(TIPO);
            Integer ocs = (Integer) atrPos.get("OCURRENCIAS");
            if (ocs != null) {
                int cant = ocs - 1;
                if (cant <= 0) {
                    tabla_simbolos.remove(lexemaPositivo);
                } else {
                    atrPos.put("OCURRENCIAS", cant);
                }
            }
        }
        return agregarOBuscarConstante("-" + lexemaPositivo, tipoPrevio);
    }

    // Declara una variable o entidad en la tabla de simbolos con clave mangled
    public static String declararVariable(String nombre, String tipo, String uso, String ambito) {
        String clave = ambito + SEPARADOR + nombre;
        if (tabla_simbolos.containsKey(clave)) {
            Parser.agregarErrorSemantico("Redeclaracion de '" + nombre + "' en el ambito " + ambito);
            return null;
        }
        // Reemplaza la entrada sin ambito insertada por el lexico
        if (tabla_simbolos.containsKey(nombre) && tabla_simbolos.get(nombre).isEmpty()) {
            tabla_simbolos.remove(nombre);
        }
        Map<String, Object> atributos = new HashMap<>();
        atributos.put(TIPO, tipo);
        atributos.put(USO, uso);
        tabla_simbolos.put(clave, atributos);
        return clave;
    }

    // Busca un identificador subiendo en la jerarquia de ambitos
    public static String buscarIdentificador(String nombre, String ambito) {
        String ambitoBusqueda = ambito;
        while (ambitoBusqueda != null) {
            String clave = ambitoBusqueda + SEPARADOR + nombre;
            if (tabla_simbolos.containsKey(clave)) {
                return clave;
            }
            int ultimo = ambitoBusqueda.lastIndexOf(SEPARADOR);
            ambitoBusqueda = (ultimo > 0) ? ambitoBusqueda.substring(0, ultimo) : null;
        }
        return null;
    }

    public static boolean existeSimbolo(String clave) {
        return tabla_simbolos.containsKey(clave);
    }

    public static void eliminarSimbolo(String clave) {
        tabla_simbolos.remove(clave);
    }

    public static void agregarAtributo(String clave, String atributo, Object valor) {
        Map<String, Object> atr = tabla_simbolos.get(clave);
        if (atr != null) {
            atr.put(atributo, valor);
        }
    }

    public static Object obtenerAtributo(String clave, String atributo) {
        Map<String, Object> atr = tabla_simbolos.get(clave);
        return (atr != null) ? atr.get(atributo) : null;
    }

    public static Map<String, Object> obtenerTodosAtributos(String clave) {
        return tabla_simbolos.get(clave);
    }

    public static String obtenerTipo(String clave) {
        return (String) obtenerAtributo(clave, TIPO);
    }

    public static String obtenerUso(String clave) {
        return (String) obtenerAtributo(clave, USO);
    }

    public static String obtenerNombre(String clave) {
        if (clave == null) {
            return null;
        }
        int idx = clave.lastIndexOf(SEPARADOR);
        return (idx >= 0) ? clave.substring(idx + 1) : clave;
    }

    public static String obtenerAmbito(String clave) {
        if (clave == null) {
            return "";
        }
        int idx = clave.lastIndexOf(SEPARADOR);
        return (idx >= 0) ? clave.substring(0, idx) : "";
    }

    // Gestion de parametros de funciones (Tema 21)
    public static void agregarParametro(String claveFuncion, String lexemaParam) {
        ArrayList<String> params = (ArrayList<String>) obtenerAtributo(claveFuncion, "PARAMETROS");
        if (params == null) {
            params = new ArrayList<>();
            agregarAtributo(claveFuncion, "PARAMETROS", params);
        }
        params.add(lexemaParam);
    }

    public static ArrayList<String> obtenerParametros(String claveFuncion) {
        ArrayList<String> params = (ArrayList<String>) obtenerAtributo(claveFuncion, "PARAMETROS");
        return (params != null) ? params : new ArrayList<>();
    }

    // Gestion de atributos de clases (Tema 29)
    public static void agregarAtributoAClase(String claveClase, String lexemaAtr) {
        ArrayList<String> atrs = (ArrayList<String>) obtenerAtributo(claveClase, "ATRIBUTOS");
        if (atrs == null) {
            atrs = new ArrayList<>();
            agregarAtributo(claveClase, "ATRIBUTOS", atrs);
        }
        atrs.add(lexemaAtr);
    }

    public static ArrayList<String> obtenerAtributosClase(String claveClase) {
        ArrayList<String> atrs = (ArrayList<String>) obtenerAtributo(claveClase, "ATRIBUTOS");
        return (atrs != null) ? atrs : new ArrayList<>();
    }

    // Gestion de herencia (Tema 30)
    public static void registrarHerencia(String claveClase, ArrayList<String> padres) {
        agregarAtributo(claveClase, "HERENCIA", new ArrayList<>(padres));
    }

    public static ArrayList<String> obtenerHerencia(String claveClase) {
        ArrayList<String> h = (ArrayList<String>) obtenerAtributo(claveClase, "HERENCIA");
        return (h != null) ? h : new ArrayList<>();
    }

    // Verifica si agregar nomClasePadre como padre de nomClaseHija genera un ciclo
    public static boolean hayCicloHerencia(String nomClaseHija, String nomClasePadre) {
        if (nomClaseHija == null || nomClasePadre == null) {
            return false;
        }
        if (nomClaseHija.equals(nomClasePadre)) {
            return true;
        }
        ArrayList<String> padres = obtenerHerencia(AMBITO_GLOBAL + SEPARADOR + nomClasePadre);
        for (int i = 0; i < padres.size(); i++) {
            if (hayCicloHerencia(nomClaseHija, padres.get(i))) {
                return true;
            }
        }
        return false;
    }

    // Gestion de imports/exports entre clases (Tema 26)
    public static void registrarImports(String claveClase, ArrayList<String> imports) {
        agregarAtributo(claveClase, "IMPORTS", new ArrayList<>(imports));
    }

    public static ArrayList<String> obtenerImports(String claveClase) {
        ArrayList<String> imp = (ArrayList<String>) obtenerAtributo(claveClase, "IMPORTS");
        return (imp != null) ? imp : new ArrayList<>();
    }

    public static void registrarExport(String claveMiembro, ArrayList<String> exports) {
        agregarAtributo(claveMiembro, "EXPORT_TO", new ArrayList<>(exports));
    }

    public static boolean estaExportadoA(String claveMiembro, String claseDestino) {
        ArrayList<String> lista = (ArrayList<String>) obtenerAtributo(claveMiembro, "EXPORT_TO");
        return (lista != null && lista.contains(claseDestino));
    }

    // Variables en tiempo de compilacion (Tema 22)
    public static boolean esComptime(String clave) {
        return Boolean.TRUE.equals(obtenerAtributo(clave, "COMPTIME"));
    }

    public static void guardarValorComptime(String clave, String valor) {
        agregarAtributo(clave, "VALOR_COMPTIME", valor);
    }

    public static String obtenerValorComptime(String clave) {
        return (String) obtenerAtributo(clave, "VALOR_COMPTIME");
    }

    public static boolean tieneValorComptime(String clave) {
        return obtenerAtributo(clave, "VALOR_COMPTIME") != null;
    }

    // Resolucion de miembros en jerarquia de herencia (Tema 30)
    public static String buscarMiembroEnJerarquia(String nomClase, String nombreMiembro) {
        if (nomClase == null) {
            return null;
        }

        // Buscar en la propia clase: main@Clase@miembro
        String clavePropia = AMBITO_GLOBAL + SEPARADOR + nomClase + SEPARADOR + nombreMiembro;
        if (existeSimbolo(clavePropia)) {
            return clavePropia;
        }

        // Buscar en orden en la lista EXTENDS (Tema 30: Desambiguado Opcion 1)
        ArrayList<String> padres = obtenerHerencia(AMBITO_GLOBAL + SEPARADOR + nomClase);
        for (int i = 0; i < padres.size(); i++) {
            String padre = padres.get(i);
            String clavePadre = buscarMiembroEnJerarquia(padre, nombreMiembro);
            if (clavePadre != null) {
                return clavePadre;
            }
        }
        return null;
    }

    public static void imprimirTabla() {
        System.out.println("=== TABLA DE SIMBOLOS ===");
        for (Map.Entry<String, Map<String, Object>> entry : tabla_simbolos.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }
}
