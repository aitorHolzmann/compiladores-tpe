package src.accion_semantica;

import java.io.PushbackReader;
import src.compilador.AnalizadorLexico;
import src.compilador.TablaPalabrasReservadas;
import src.compilador.TablaSimbolos;

public class AS3b extends Accion_Semantica{
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
            return identificador;
        }

        identificador = TablaSimbolos.obtenerSimbolo(token_entregado);
        
        if  (identificador != TablaSimbolos.LEXEMA_NO_ENCONTRADO){
            return identificador;
        }

        TablaSimbolos.agregarSimbolo(token_entregado);
        return TablaSimbolos.obtenerSimbolo(token_entregado);

    }
}