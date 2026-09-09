package src.accion_semantica;

import java.io.Reader;
import src.compilador.AnalizadorLexico;

public class AS4 extends Accion_Semantica{
    @Override 
    public int ejecutar(StringBuilder token_actual, Reader reader){
        try{
            char caracter_actual = (char) reader.read();
            return AnalizadorLexico.token_abierto;

        } catch (Exception e){
            e.printStackTrace();
        }
        return -1;
    }
}
