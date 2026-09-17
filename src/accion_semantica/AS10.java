package src.accion_semantica;

import java.io.PushbackReader;
import java.io.Reader;
import src.compilador.*;


public class AS10 extends Accion_Semantica{ 
    // tenemos por ejemplo "0.3 " , estamos en la letra " " es decir que hay que cerrar token float.
    // Para esto tenemos que transformar el string "0.3 " a algo como 0.3 , Luego chequear rango y devolver token "SINGLEF"
    @Override 
    public int ejecutar(StringBuilder token_actual, PushbackReader reader, char caracter_actual){
        String token_entregado = token_actual.toString(); //"0.3s" como string"
        Double resultado = Double.parseDouble(token_entregado); // "20.3" -> 20.3 en double
        System.out.println("El resultado es: "+ resultado);
        try{
            reader.unread(caracter_actual);
        } catch (Exception e){
            e.printStackTrace();
        }
        if ((resultado >= AnalizadorLexico.ValorMinimoFloat && resultado <= AnalizadorLexico.ValorMaximoFloat) || resultado == 0.0){
            int id = TablaSimbolos.gestionarConstante("" + resultado, "SINGLEF");
            AnalizadorLexico.setReferenciaTablaSimbolos(id);
            return TablaPalabrasReservadas.obtenerIdentificador("CONSTANTE");
        }
        System.out.println("WARNING: El double "+ resultado + " esta fuera de rango");
        return 0; // Manejar el error
    }
}
