package accion_semantica;

import compilador.AnalizadorLexico;
import compilador.Parser;
import compilador.TablaSimbolos;
import compilador.TablaTipos;

import java.io.Reader;

public class AS6 implements AccionSemantica {
    @Override
    public int ejecutar(Reader lector, StringBuilder token) {
        String simbolo = token.toString();

        try {
            double valor = Double.parseDouble(simbolo);

            if (!dentroRango(valor)) {
                Parser.agregarError(Parser.errores_lexicos, Parser.WARNING, "El numero doble " + simbolo +
                                            " fue truncado ya que no se encuentra dentro del rango aceptado");

                if (valor < AnalizadorLexico.MIN_DOUBLE_VALUE) {
                    simbolo = Double.toString(AnalizadorLexico.MIN_DOUBLE_VALUE);
                } else {
                    simbolo = Double.toString(AnalizadorLexico.MAX_DOUBLE_VALUE);
                }
            }
        } catch (NumberFormatException excepcion) {
            excepcion.printStackTrace();
        }

        if (TablaSimbolos.obtenerSimbolo(simbolo) == TablaSimbolos.NO_ENCONTRADO) {
            TablaSimbolos.agregarSimbolo(simbolo);
            int ptr_id = TablaSimbolos.obtenerSimbolo(simbolo);
            TablaSimbolos.agregarAtributo(ptr_id, "tipo", TablaTipos.DOUBLE_TYPE);
        }

        return AnalizadorLexico.CONSTANTE;
    }

    private static boolean dentroRango(double valor) {
        return valor == 0D || (AnalizadorLexico.MIN_DOUBLE_VALUE <= valor && valor <= AnalizadorLexico.MAX_DOUBLE_VALUE);
    }
}
