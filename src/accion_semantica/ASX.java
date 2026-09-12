/*
package src.accion_semantica;

import java.io.PushbackReader;
import src.compilador.AnalizadorLexico;
import src.compilador.TablaPalabrasReservadas;
import src.compilador.TablaSimbolos;

public class AS3 extends Accion_Semantica{
    // pepe_$ -> estoy en el "$" y no tiene que ser parte de la variable identificador
    
    @Override 
    public int ejecutar(StringBuilder token_actual, PushbackReader reader, char caracter_actual){
        String token_entregado = token_actual.toString();
        if (token_entregado.length() > 22){
            String truncado = token_entregado.substring(0, 22); // o 21?
            token_entregado = truncado;
            AnalizadorLexico.mostrarWarning();
        }

        try{
            reader.unread(caracter_actual);
        } catch (Exception e){
            e.printStackTrace();
        }
        
        int identificador = TablaPalabrasReservadas.obtenerIdentificador(token_entregado);
        
        if (identificador != TablaPalabrasReservadas.NOT_FOUND){
            
        }
        
            identificador = TablaSimbolos.obtenerSimbolo(token_entregado);

            if (identificador == TablaSimbolos.LEXEMA_NO_ENCONTRADO){
                TablaSimbolos.agregarSimbolo(token_entregado);
            }

        } else {
           return 
        }
        
        
        //AnalizadorLexico.setLexema(token_entregado);

//        return AnalizadorLexico.IDENTIFICADOR;
    }
}*/