package compilador;

import java.util.Map;

public class TablaPalabrasReservadas {
    public static final int PALABRA_NO_RESERVADA = -1;

    private static final String ARCHIVO_PALABRAS_RESERVADAS = "src/palabrasReservadas.txt";

    private static final Map<String, Integer> palabras_reservadas = FileHelper.readMapFile(ARCHIVO_PALABRAS_RESERVADAS);

    public static int obtenerIdentificador(String palabra_reservada) {
        return palabras_reservadas.getOrDefault(palabra_reservada, PALABRA_NO_RESERVADA);
    }
}
