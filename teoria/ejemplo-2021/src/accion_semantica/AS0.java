package accion_semantica;

import compilador.AnalizadorLexico;

import java.io.IOException;
import java.io.Reader;

public class AS0 implements AccionSemantica {
    @Override
    public int ejecutar(Reader lector, StringBuilder token) {
        try {
            char caracter = (char) lector.read(); // Lee el siguiente caracter

            if (caracter == AnalizadorLexico.NUEVA_LINEA) {
                AnalizadorLexico.setLineaActual(AnalizadorLexico.getLineaActual() + 1);
            }
        } catch (IOException excepcion) {
            excepcion.printStackTrace();
        }

        return TOKEN_ACTIVO;
    }
}
