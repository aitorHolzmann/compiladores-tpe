package src.accion_semantica;

import java.io.PushbackReader;
import src.compilador.*;

public class AS8 extends Accion_Semantica{
    // Cierra token float sin exponente explicito (ej: 20.3s -> 20.3^1)
    @Override 
    public int ejecutar(StringBuilder token_actual, PushbackReader reader, char caracter_actual){
        String token_entregado = token_actual.toString(); // "20.3s"
        int posicionS = token_entregado.indexOf("s");
        String base = token_entregado.substring(0, posicionS); // "20.3"
        Double baseD = Double.parseDouble(base);
        Double resultado = Math.pow(baseD, 1);
        try{
            reader.unread(caracter_actual);
        } catch (Exception e){
            e.printStackTrace();
        }
        if ((resultado >= AnalizadorLexico.ValorMinimoFloat && resultado <= AnalizadorLexico.ValorMaximoFloat) || resultado == 0.0){
            int id = TablaSimbolos.agregarOBuscarConstante("" + resultado);
            AnalizadorLexico.setReferenciaTablaSimbolos(id);
            return TablaPalabrasReservadas.obtenerIdentificador("CTE_SINGLEF");
        }
        AnalizadorLexico.mostrarWarning("El double "+ resultado + " esta fuera de rango");
        return 0; 
    }
}
