package src.accion_semantica;
import java.io.PushbackReader;
import src.compilador.AnalizadorLexico;
import src.compilador.TablaPalabrasReservadas;
import src.compilador.TablaSimbolos;

public class AS6 extends Accion_Semantica{
    // Cierra el token de numeros shortint
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
        //Con la expresion regular limpiamos todos los caracteres ej:
        //5$, 5s, 5$s
        String valor = resultado.replaceAll("[a-zA-Z\\$]+", "");
        String lexema = valor + "$s";
        
      
        
        int valor_int = Integer.parseInt(valor);
        // Para shortint (8 bits, [-128, 127]) el lexico acepta hasta 128
        if (valor_int <= Math.abs(AnalizadorLexico.ValorMinimoInt)) {

            int id = TablaSimbolos.gestionarConstante("" + valor, "SHORTINT");
            AnalizadorLexico.setReferenciaTablaSimbolos(id);

            return TablaPalabrasReservadas.obtenerIdentificador("SHORTINT");
        }

        AnalizadorLexico.mostrarError(": Constante shortint fuera de rango (" + valor + ")");
        return 0;
    }
}