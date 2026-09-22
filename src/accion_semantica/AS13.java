package src.accion_semantica;
import java.io.*;
import src.compilador.TablaPalabrasReservadas;

public class AS13 extends Accion_Semantica{

    @Override 
    //Cierra el token de . 
    public int ejecutar(StringBuilder token_actual, PushbackReader reader, char caracter_actual){
        try {
            reader.unread(caracter_actual);
        } catch (Exception e){
            e.printStackTrace();
        }
        return TablaPalabrasReservadas.obtenerIdentificador(".");
    }
}