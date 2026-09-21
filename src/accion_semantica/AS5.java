package src.accion_semantica;

import java.io.PushbackReader;
import java.io.Reader;
import src.compilador.AnalizadorLexico;

public class AS5 extends Accion_Semantica{
    @Override 
    public int ejecutar(StringBuilder token_actual, PushbackReader reader, char caracter_actual){
        token_actual.setLength(0);
        AnalizadorLexico.incrementarLinea();
        return AnalizadorLexico.token_abierto;
    }
}