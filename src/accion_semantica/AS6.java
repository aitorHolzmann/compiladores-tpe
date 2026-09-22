package src.accion_semantica;
import java.io.PushbackReader;
import src.compilador.AnalizadorLexico;
import src.compilador.TablaPalabrasReservadas;
import src.compilador.TablaSimbolos;

public class AS6 extends Accion_Semantica{
    // Cierra el token de numeros shortint (ej: 5$s)
    @Override
    public int ejecutar(StringBuilder token_actual, PushbackReader reader, char caracter_actual){
        if (caracter_actual != 's'){
            try{
                reader.unread(caracter_actual);
            } catch (Exception e){
                e.printStackTrace();
            }
        } else {
            token_actual.append(caracter_actual);
        }
        
        String resultado = token_actual.toString();
        if (!resultado.contains("$") || !resultado.contains("s")){
            AnalizadorLexico.mostrarWarning("El numero leido fue "+ resultado + " -> Los SHORTINT terminan con $s"); 
        }
        // Limpiar sufijo $s para obtener el valor numerico
        String valor = resultado.replaceAll("[a-zA-Z\\$]+", "");
        
        int valor_int = Integer.parseInt(valor);
        // El lexico acepta hasta 128 (el -128 se resuelve con numero_negativo en la gramatica)
        if (valor_int <= Math.abs(AnalizadorLexico.ValorMinimoInt)) {
            int id = TablaSimbolos.agregarOBuscarConstante(valor);
            AnalizadorLexico.setReferenciaTablaSimbolos(id);
            return TablaPalabrasReservadas.obtenerIdentificador("CTE_SHORTINT");
        }

        AnalizadorLexico.mostrarError(": Constante shortint fuera de rango (" + valor + ")");
        return 0;
    }
}