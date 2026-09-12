package src.accion_semantica;

import java.io.PushbackReader;
import java.io.Reader;
import src.compilador.*;


public class AS11 extends Accion_Semantica{
    // Tenemos que buscar en la tabla de palabras reservadas si se encuentra la que estamos buscando. En este caso como la palabra va a ser "IF", "ELSE", o una de esas
    // Estamos seguros de eso porque hay una letra Mayuscula en el medio. Entonces no puede ser identificador
    @Override 
    public int ejecutar(StringBuilder token_actual, PushbackReader reader, char caracter_actual){
        try{
            reader.unread(caracter_actual);
        
        } catch (Exception e){
            e.printStackTrace();
        }
        String token_entregado = token_actual.toString();
        int resultado = TablaPalabrasReservadas.obtenerIdentificador(token_entregado.toUpperCase());
        if (resultado == TablaPalabrasReservadas.NOT_FOUND){
            System.out.println("ERROR: la palabra "+ token_entregado + " no es una palabra reservada");
            return 0;
        }
        return resultado;
    }
}