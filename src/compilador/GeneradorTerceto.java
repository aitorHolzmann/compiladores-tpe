package src.compilador;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class GeneradorTerceto {
    // Tercetos almacenados por ambito (main, funciones, metodos)
    private static final Map<String, ArrayList<Terceto>> tercetosPorAmbito = new LinkedHashMap<>();

    public static void reiniciar() {
        tercetosPorAmbito.clear();
    }

    public static void iniciarAmbito(String ambito) {
        if (ambito != null && !tercetosPorAmbito.containsKey(ambito)) {
            tercetosPorAmbito.put(ambito, new ArrayList<Terceto>());
        }
    }

    private static ArrayList<Terceto> obtenerListaActual() {
        String ambito = Parser.ambitoActual;
        if (ambito == null || ambito.isEmpty()) {
            ambito = TablaSimbolos.AMBITO_GLOBAL;
        }
        iniciarAmbito(ambito);
        return tercetosPorAmbito.get(ambito);
    }

    public static int crearTerceto(String op, String arg1, String arg2) {
        return crearTerceto(op, arg1, arg2, null);
    }

    public static int crearTerceto(String op, String arg1, String arg2, String tipo) {
        Terceto terceto = new Terceto(op, arg1, arg2, tipo);
        ArrayList<Terceto> lista = obtenerListaActual();
        lista.add(terceto);
        return lista.size() - 1;
    }

    public static Terceto getTerceto(int indice) {
        ArrayList<Terceto> lista = obtenerListaActual();
        return (indice >= 0 && indice < lista.size()) ? lista.get(indice) : null;
    }

    public static int getProximoIndice() {
        return obtenerListaActual().size();
    }

    public static void completarSalto(int indiceTerceto, int destino) {
        ArrayList<Terceto> lista = obtenerListaActual();
        if (indiceTerceto >= 0 && indiceTerceto < lista.size()) {
            Terceto t = lista.get(indiceTerceto);
            if (t.getOperador().equals("BF")) {
                t.setOperando2("[" + destino + "]");
            } else if (t.getOperador().equals("BI")) {
                t.setOperando1("[" + destino + "]");
            }
        }
    }

    public static void imprimirTercetos() {
        System.out.println("\n----------------------------------------");
        System.out.println("         CODIGO INTERMEDIO (TERCETOS)   ");
        System.out.println("----------------------------------------");
        for (Map.Entry<String, ArrayList<Terceto>> entrada : tercetosPorAmbito.entrySet()) {
            String ambito = entrada.getKey();
            ArrayList<Terceto> lista = entrada.getValue();
            System.out.println("Ambito: " + ambito);
            if (lista.isEmpty()) {
                System.out.println("  (sin tercetos)");
            } else {
                for (int i = 0; i < lista.size(); i++) {
                    System.out.println(i + " " + lista.get(i).toString());
                }
            }
            System.out.println("----------------------------------------");
        }
        System.out.println();
    }
}
