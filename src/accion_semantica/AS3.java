package src.accion_semantica;

import java.io.PushbackReader;
import src.compilador.AnalizadorLexico;
import src.compilador.TablaPalabrasReservadas;
import src.compilador.TablaSimbolos;
import src.token.*;

public class AS3 extends Accion_Semantica{
    // pepe_$ -> estoy en el "$" y no tiene que ser parte de la variable identificador
    
    @Override 
    public int ejecutar(StringBuilder token_actual, PushbackReader reader, char caracter_actual){
        String token_entregado = token_actual.toString();
        if (token_entregado.length() > 22){
            String truncado = token_entregado.substring(0, 22);
            token_entregado = truncado;
            AnalizadorLexico.mostrarWarning();
        }

        try{
            reader.unread(caracter_actual);
        } catch (Exception e){
            e.printStackTrace();
        }

        //al pasar a mayuscula detectamos elSE, else, ... Unicamente para palabras reservadas
        int identificador = TablaPalabrasReservadas.obtenerIdentificador(token_entregado.toUpperCase());

        if (identificador != TablaPalabrasReservadas.NOT_FOUND){
            return identificador;
        }
    
        identificador = TablaSimbolos.obtenerSimbolo(token_entregado);

        if  (identificador != TablaSimbolos.LEXEMA_NO_ENCONTRADO){
            AnalizadorLexico.setReferenciaTablaSimbolos(identificador); //Para yyval
            return TablaPalabrasReservadas.obtenerIdentificador("IDENTIFICADOR");
        }

        Token nuevo_token_identificador = new TokenIdentificador(token_entregado, AnalizadorLexico.getLineaActual());

        int nuevo_identificador = TablaSimbolos.agregarSimbolo(nuevo_token_identificador);
        nuevo_token_identificador.setId(nuevo_identificador);
        AnalizadorLexico.setReferenciaTablaSimbolos(nuevo_identificador);
        return TablaPalabrasReservadas.obtenerIdentificador("IDENTIFICADOR");

    }
}