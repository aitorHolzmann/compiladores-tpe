package src.accion_semantica;

import java.io.PushbackReader;
import src.compilador.AnalizadorLexico;
import src.compilador.TablaPalabrasReservadas;
import src.compilador.TablaSimbolos;

public class AS3 extends Accion_Semantica{
    
    @Override 
    public int ejecutar(StringBuilder token_actual, PushbackReader reader, char caracter_actual){
        String token_entregado = token_actual.toString();
        if (token_entregado.length() > 22){
            String truncado = token_entregado.substring(0, 22);
            token_entregado = truncado;
            AnalizadorLexico.mostrarWarning("El identificador tenia mas de 22 caracteres y fue truncado");
        }

        try{
            reader.unread(caracter_actual);
        } catch (Exception e){
            e.printStackTrace();
        }

        int identificador = TablaPalabrasReservadas.obtenerIdentificador(token_entregado.toUpperCase());
        
        if (identificador != TablaPalabrasReservadas.NOT_FOUND){ //es palabra reservada
            return identificador;
        }

        if (token_entregado.matches(".*[A-Z].*")) {
            AnalizadorLexico.mostrarWarning("El texto { " + token_entregado + " } contiene mayúsculas. Se convirtió a minusculas, resultado: " + token_entregado.toLowerCase());
        }
        
        identificador = TablaSimbolos.obtenerSimbolo(token_entregado);

        if  (identificador != TablaSimbolos.LEXEMA_NO_ENCONTRADO){
            AnalizadorLexico.setReferenciaTablaSimbolos(identificador);
            return TablaPalabrasReservadas.obtenerIdentificador("IDENTIFICADOR");
        }

        
        int nuevo_identificador = TablaSimbolos.agregarSimbolo(token_entregado);
        AnalizadorLexico.setReferenciaTablaSimbolos(nuevo_identificador);
        return TablaPalabrasReservadas.obtenerIdentificador("IDENTIFICADOR");

    }
}