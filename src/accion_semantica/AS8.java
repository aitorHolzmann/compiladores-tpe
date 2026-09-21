package src.accion_semantica;

import java.io.PushbackReader;
import java.io.Reader;
import src.compilador.*;

public class AS8 extends Accion_Semantica{
    // tenemos por ejemplo 20.3sa , estamos en la letra "a" es decir que hay que cerrar token float. poner exponente 0.
    // Para esto tenemos que transformar el string "20.3s" a algo como 20.30^0 , Luego chequear rango y devolver token "SINGLEF"
    @Override 
    public int ejecutar(StringBuilder token_actual, PushbackReader reader, char caracter_actual){
        String token_entregado = token_actual.toString(); //"20.3s" como string
        int posicionS = token_entregado.indexOf("s"); // Me fijo en que posicion esta la "s"
        String base = token_entregado.substring(0, posicionS); // Me quedo con "20.3"
        Double baseD = Double.parseDouble(base); // "20.3" -> 20.3 en double
        Double resultado = Math.pow(baseD, 0); // Eleba la base a la potencia 0 -> 20.3^0
        try{
            reader.unread(caracter_actual);
        } catch (Exception e){
            e.printStackTrace();
        }
        if ((resultado >= AnalizadorLexico.ValorMinimoFloat && resultado <= AnalizadorLexico.ValorMaximoFloat) || resultado == 0.0){
            int id = TablaSimbolos.gestionarConstante("" + resultado, "SINGLEF");
            AnalizadorLexico.setReferenciaTablaSimbolos(id);
            return TablaPalabrasReservadas.obtenerIdentificador("SINGLEF");
        }
        AnalizadorLexico.mostrarWarning("El double "+ resultado + " esta fuera de rango");
        return 0; 
    }
}

