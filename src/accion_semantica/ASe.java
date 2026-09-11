package src.accion_semantica;

import java.io.PushbackReader;
import java.io.Reader;

public class ASe extends Accion_Semantica{
    @Override 
    public int ejecutar(StringBuilder token_actual, PushbackReader reader, char caracter_actual){
        return 0;
    }
}