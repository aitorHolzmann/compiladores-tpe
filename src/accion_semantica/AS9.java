package src.accion_semantica;

import java.io.PushbackReader;
import java.io.Reader;
import src.compilador.*;
import src.token.*;

public class AS9 extends Accion_Semantica{
    @Override 
    public int ejecutar(StringBuilder token_actual, PushbackReader reader, char caracter_actual){
        
        String token_entregado = token_actual.toString(); //"20.3s" como string
        int posicionS = token_entregado.indexOf("s"); // Me fijo en que posicion esta la "s"
        String base = token_entregado.substring(0, posicionS); // Me quedo con "20.3"
        Double baseD = Double.parseDouble(base); // "20.3" -> 20.3 en double
        String exp = token_entregado.substring(posicionS + 1); // Me quedo con lo que venga despues de la "s"
        int exponente = Integer.parseInt(exp); // parseo a int el exponente -> "2" = 2
        Double resultado = Math.pow(baseD, exponente); // Eleva la base a la potencia 0 -> 20.3^2
        System.out.println("El resultado es: " + resultado);
        if (resultado < AnalizadorLexico.ValorMinimoFloat && resultado > AnalizadorLexico.ValorMaximoFloat){

            
            System.out.println("WARNING: El double "+ resultado + " esta fuera de rango");
            return 0; // Manejar el error
        }

        Token token_float = new TokenFloat(token_entregado, AnalizadorLexico.getLineaActual());

        int nuevo_identificador = TablaSimbolos.agregarSimbolo(token_float);
        token_float.setId(nuevo_identificador);
        AnalizadorLexico.setReferenciaTablaSimbolos(nuevo_identificador);
        return TablaPalabrasReservadas.obtenerIdentificador("SINGLEF");
    }
}
