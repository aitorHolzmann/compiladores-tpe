package src.accion_semantica;
import src.compilador.AnalizadorLexico;
import src.compilador.TablaPalabrasReservadas;
import java.io.*;

public class AS1 extends Accion_Semantica{

    @Override 
    //TODO: la AS1 tambien se ejecuta al cerrar los mensajes multilinea con " " ", o al terminar un comentario. Que hacemos? 
    public int ejecutar(StringBuilder token_actual, Reader reader){
        int numero_actual = -1;
        try{
            numero_actual = reader.read();
            char caracter_actual = (char) numero_actual;
            token_actual.append(caracter_actual);
            String token_cerrado = token_actual.toString();

            int identificador_palabra_reservada = TablaPalabrasReservadas.obtenerIdentificador(token_cerrado);
            
        } catch (Exception e){
            e.printStackTrace();
        }


        return numero_actual;
    }
}
