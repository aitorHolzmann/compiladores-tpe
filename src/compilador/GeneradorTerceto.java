package src.compilador;

import java.util.ArrayList;

public class GeneradorTerceto {
    private static ArrayList<Terceto> codigoIntermedio = new ArrayList<>();

    public static int crearTerceto(String op, String arg1, String arg2) {
        return crearTerceto(op, arg1, arg2, null);
    }

    public static int crearTerceto(String op, String arg1, String arg2, String tipo) {
        Terceto terceto = new Terceto(op, arg1, arg2, tipo);
        codigoIntermedio.add(terceto);
        return codigoIntermedio.size() - 1;
    }

    public static Terceto getTerceto(int indice) {
        return codigoIntermedio.get(indice);
    }

    public static int getProximoIndice() {
        return codigoIntermedio.size();
    }

    public static void completarSalto(int indiceTerceto, int destino) {
        if (indiceTerceto >= 0 && indiceTerceto < codigoIntermedio.size()) {
            Terceto t = codigoIntermedio.get(indiceTerceto);
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
        for (int i = 0; i < codigoIntermedio.size(); i++) {
            System.out.println(i + " " + codigoIntermedio.get(i).toString());
        }
        System.out.println("----------------------------------------\n");
    }
}