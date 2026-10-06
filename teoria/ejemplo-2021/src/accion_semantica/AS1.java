package accion_semantica;

import java.io.IOException;
import java.io.Reader;

public class AS1 implements AccionSemantica {
    @Override
    public int ejecutar(Reader lector, StringBuilder token) {
        try {
            char siguiente_caracter = (char) lector.read(); // Lee el siguiente caracter
            token.append(siguiente_caracter); // Concatena el caracter actual
        } catch (IOException excepcion) {
            excepcion.printStackTrace();
        }

        return TOKEN_ACTIVO;
    }
}
