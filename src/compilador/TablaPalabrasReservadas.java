package src.compilador;

import java.util.Map;

import src.compilador.FileHelper;

public class TablaPalabrasReservadas {
    public static final int NOT_FOUND = -1;

    private static final String PATH_TABLA_PALABRAS_RESERVADAS = "src/estructuras/tabla_palabras_reservadas";
        private static final Map<String, Integer> palabras_reservadas = FileHelper.readMapFile(PATH_TABLA_PALABRAS_RESERVADAS);

    public static int obtenerIdentificador(String palabra_reservada) {
        return palabras_reservadas.getOrDefault(palabra_reservada, NOT_FOUND);
    }
}
