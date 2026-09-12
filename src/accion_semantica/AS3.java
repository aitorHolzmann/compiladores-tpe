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
            String truncado = token_entregado.substring(0, 22);
            token_entregado = truncado;
            AnalizadorLexico.mostrarWarning();
        }

        try{
            reader.unread(caracter_actual);
        } catch (Exception e){
            e.printStackTrace();
        }

        // CORRECCION: las palabras reservadas se pueden escribir en
        // mayusculas o minusculas ("IF, else, END_if, begin..." segun el
        // enunciado del TP1), pero los identificadores de usuario SOLO
        // pueden tener minusculas (ya validado por el propio automata).
        // Por eso es seguro normalizar a mayusculas UNICAMENTE para la
        // busqueda en la tabla de palabras reservadas, sin tocar
        // token_entregado (que es lo que se guarda como lexema real).
        // Para que esto funcione, la tabla_palabras_reservadas debe tener
        // sus claves guardadas en mayusculas (ej "BEGIN", "SHORTINT", etc).
        int identificador = TablaPalabrasReservadas.obtenerIdentificador(token_entregado.toUpperCase());

        if (identificador != TablaPalabrasReservadas.NOT_FOUND){
            return identificador;
        }

        identificador = TablaSimbolos.obtenerSimbolo(token_entregado);

        if  (identificador != TablaSimbolos.LEXEMA_NO_ENCONTRADO){
            AnalizadorLexico.setLexema(token_entregado);
            // CORRECCION: guardamos la referencia para que yylex() arme yylval.
            AnalizadorLexico.referenciaTablaSimbolos = identificador;
            return AnalizadorLexico.IDENTIFICADOR;
        }

        // CORRECCION: antes se ignoraba el valor de retorno de
        // agregarSimbolo (era void). Ahora usamos el id que devuelve.
        int nuevo_identificador = TablaSimbolos.agregarSimbolo(token_entregado);
        AnalizadorLexico.referenciaTablaSimbolos = nuevo_identificador;
        return AnalizadorLexico.IDENTIFICADOR;

    }
}