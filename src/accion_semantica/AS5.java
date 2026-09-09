package src.accion_semantica;

import java.io.Reader;
import src.compilador.AnalizadorLexico;

public class AS5 extends Accion_Semantica{
    @Override 
    public int ejecutar(StringBuilder token_actual, Reader reader){
        try{
            int caracter_actual = reader.read();
            AnalizadorLexico.incrementarLinea();
            return -1;

        } catch (Exception e){
            e.printStackTrace();
        }
        return -1;
    }
}