package src.accion_semantica;
import java.io.*;
import src.compilador.AnalizadorLexico;
import src.compilador.TablaPalabrasReservadas;
import src.compilador.TablaSimbolos;

public class AS14 extends Accion_Semantica{

    @Override 
    public int ejecutar(StringBuilder token_actual, PushbackReader reader, char caracter_actual){
        try {
            reader.unread(caracter_actual);
        } catch (Exception e){
            e.printStackTrace();
        }
        
        String lexema = token_actual.toString();
        AnalizadorLexico.mostrarWarning("El double escrito fue: "+ lexema + " Se completo con: " + lexema + "0");
        lexema += "0";

        Double resultado = Double.parseDouble(lexema);

        if ((resultado >= AnalizadorLexico.ValorMinimoFloat && resultado <= AnalizadorLexico.ValorMaximoFloat) || resultado == 0.0){
            int id = TablaSimbolos.gestionarConstante("" + resultado, "SINGLEF");
            AnalizadorLexico.setReferenciaTablaSimbolos(id);
            return TablaPalabrasReservadas.obtenerIdentificador("SINGLEF");
        }
        AnalizadorLexico.mostrarError("El double "+ resultado + " esta fuera de rango");
        return 0;
    }
}