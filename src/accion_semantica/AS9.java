package src.accion_semantica;

import java.io.PushbackReader;
import src.compilador.*;

public class AS9 extends Accion_Semantica{
    // Cierra token float con exponente explicito (ej: 20.3s2 -> 20.3^2)
    @Override 
    public int ejecutar(StringBuilder token_actual, PushbackReader reader, char caracter_actual){
        try {
            reader.unread(caracter_actual);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String token_entregado = token_actual.toString(); // "20.3s2"
        int posicionS = token_entregado.indexOf("s");
        String base = token_entregado.substring(0, posicionS); // "20.3"
        Double baseD = Double.parseDouble(base);
        String exp = token_entregado.substring(posicionS + 1); // "2"
        int exponente = Integer.parseInt(exp);
        Double resultado = Math.pow(baseD, exponente);
        if ((resultado >= AnalizadorLexico.ValorMinimoFloat && resultado <= AnalizadorLexico.ValorMaximoFloat) || resultado == 0.0){
            int id = TablaSimbolos.agregarOBuscarConstante("" + resultado);
            AnalizadorLexico.setReferenciaTablaSimbolos(id);
            return TablaPalabrasReservadas.obtenerIdentificador("CTE_SINGLEF");
        }
        AnalizadorLexico.mostrarWarning("El double "+ resultado + " esta fuera de rango");
        return 0;
    }
}
