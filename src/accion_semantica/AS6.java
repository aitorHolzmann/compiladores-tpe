
package src.accion_semantica;
import java.io.PushbackReader;
import src.compilador.AnalizadorLexico;
import src.compilador.TablaSimbolos;

public class AS6 extends Accion_Semantica{
    // Cierra el token de numeros shortint (sufijo $s), valida el rango
    // y devuelve el token de CONSTANTE al parser.
    public int ejecutar(StringBuilder token_actual, PushbackReader reader, char caracter_actual){
        //120$s
        token_actual.append(caracter_actual);
        String resultado = token_actual.toString();
        int posicion = resultado.indexOf('$');
        String soloDigitos = resultado.substring(0, posicion);
        int valor = Integer.parseInt(soloDigitos);
        //120
        if (valor >= AnalizadorLexico.ValorMinimoInt && valor <= AnalizadorLexico.ValorMaximoInt) {

            //290, <LEXEMA, "120">, <TIPO, "SHORTINT">
            //291, <LEXEMA, "500">, <TIPO, "SHORTINT">
            //DUDA: TENEMOS QUE REUTILIZAR LAS CONSTANTES?
            int id = TablaSimbolos.agregarSimbolo("" + valor);
            TablaSimbolos.agregarAtributo(id, "TIPO", "SHORTINT");

            AnalizadorLexico.setReferenciaTablaSimbolos(id);

            return TablaSimbolos.obtenerSimbolo("SHORTINT");
        }

        //Si se fue de rango
        System.out.println("ERROR - Linea " + AnalizadorLexico.getLineaActual()
                + ": Constante shortint fuera de rango (" + valor + ")");
        token_actual.setLength(0);
        AnalizadorLexico.estado_actual = 0;
        return AnalizadorLexico.token_abierto;
    }
}