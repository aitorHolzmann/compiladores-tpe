package src.compilador;

public class Main {
    public static void main(String[] args) {
    
    AnalizadorLexico analizador = new AnalizadorLexico();
    var cant_estados = analizador.get_cant_estados();
    var cant_simbolos = analizador.get_cant_simbolos();
    int[][] matriz_semantica  = analizador.leerMatriz("src/estructuras/tabla_acciones_semanticas", cant_estados , cant_simbolos);

    for (int fil=0; fil< cant_estados; fil++){
        for (int col =0; col<  cant_simbolos; col++){
            System.err.print(matriz_semantica[fil][col]);
        }
        System.out.println("");
    }

    }
}