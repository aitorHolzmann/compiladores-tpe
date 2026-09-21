package src.accion_semantica;

import java.io.PushbackReader;
import java.io.Reader;
import src.compilador.AnalizadorLexico;

public class AS7 extends Accion_Semantica{
    @Override 
    public int ejecutar(StringBuilder token_actual, PushbackReader reader, char caracter_actual){
        token_actual.append("0.");
        return AnalizadorLexico.token_abierto;
    }
}