package src.accion_semantica;

import java.io.Reader;
import src.compilador.AnalizadorLexico;

public class AS2 extends Accion_Semantica{
    @Override 
    public int ejecutar(StringBuilder token_actual, Reader reader){
        try{
            char caracter_actual = (char) reader.read();
            token_actual.append(caracter_actual);

            return AnalizadorLexico.token_abierto;

        } catch (Exception e){
            e.printStackTrace();
        }
        return AnalizadorLexico.token_abierto;
    }
}