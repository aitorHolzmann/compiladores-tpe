
    /*
package src.accion_semantica;

    import java.io.PushbackReader;
    import java.io.Reader;
    import src.compilador.AnalizadorLexico;
    import src.compilador.TablaPalabrasReservadas;

    public class AS6 extends Accion_Semantica{
        // Tiene que cerrar el token de numeros enteros, ver si esta en el rango y devolver el token de SHORTINT
        
        //POR QUE GUARDAMOS $s?
        @Override 
        public int ejecutar(StringBuilder token_actual, PushbackReader reader, char caracter_actual){
            //120$s
            token_actual.append(caracter_actual);
            String resultado = token_actual.toString();
            int posicion = resultado.indexOf('$');
            String soloDigitos = resultado.substring(0, posicion);
            int resultado_entero = Integer.parseInt(soloDigitos);
            if (resultado_entero >= AnalizadorLexico.ValorMinimoInt && resultado_entero <= AnalizadorLexico.ValorMaximoInt) {
                return TablaPalabrasReservadas.obtenerIdentificador("SHORTINT");
            }
            System.out.println("WARNING: El numero se sale de rango"); // deberia ser un error
            token_actual.setLength(0);
            return AnalizadorLexico.token_abierto;
        }
    }
Aca mi token va a tener algo del estilo "120$s"
    Como hago para que al parsear a INT solo tome el 120??

    Respuesta Claude:          
            int posicion = resultado.indexOf('$');
            String soloDigitos = resultado.substring(0, posicion);
    */

package src.accion_semantica;
import java.io.PushbackReader;
import src.compilador.AnalizadorLexico;
import src.compilador.TablaPalabrasReservadas;
import src.compilador.TablaSimbolos;
import src.token.*;

public class AS6 extends Accion_Semantica{
    // Cierra el token de numeros shortint (sufijo $s), valida el rango
    // y devuelve el token de CONSTANTE al parser.

    @Override
    public int ejecutar(StringBuilder token_actual, PushbackReader reader, char caracter_actual){
        //120$s
        token_actual.append(caracter_actual);
        String resultado = token_actual.toString();
        int posicion = resultado.indexOf('$');
        String soloDigitos = resultado.substring(0, posicion);
        int valor = Integer.parseInt(soloDigitos);
//120$s
        if (valor >= AnalizadorLexico.ValorMinimoInt && valor <= AnalizadorLexico.ValorMaximoInt) {

            //290, <LEXEMA, "120">, <TIPO, "SHORTINT">
            //291, <LEXEMA, "500">, <TIPO, "SHORTINT">
            //DUDA: TENEMOS QUE REUTILIZAR LAS CONSTANTES?
            Token nuevo_token_constante = new TokenConstante(resultado, AnalizadorLexico.getLineaActual(), valor);    
            int id = TablaSimbolos.agregarSimbolo(nuevo_token_constante);
            nuevo_token_constante.setId(id);
            
            TablaSimbolos.agregarAtributo(id, "TIPO", "SHORTINT");

            // CORRECCION: guardamos la referencia para yylval.
            AnalizadorLexico.setReferenciaTablaSimbolos(id);

            return TablaPalabrasReservadas.obtenerIdentificador("SHORTINT");
        }

        // CORRECCION: se cambia de WARNING a ERROR (el enunciado pide
        // reportar como error, con numero de linea, las constantes fuera
        // de rango: "Linea 24: Constante entera fuera del rango permitido").
        // Ademas, antes se hacia token_actual.setLength(0) pero se
        // devolvia token_abierto (-1) SIN resetear estado_actual. Como
        // analizar() solo resetea el estado a 0 cuando el token
        // devuelto != -1, el automata podia quedar "colgado" en un
        // estado intermedio esperando mas digitos que ya nunca van a
        // llegar (por ejemplo, si el siguiente caracter es una letra).
        // Reseteamos el estado explicitamente para que el compilador
        // pueda seguir compilando despues del error, tal como pide el
        // enunciado del TP2 ("cuando se detecte un error, la
        // compilacion debe continuar").
        System.out.println("ERROR - Linea " + AnalizadorLexico.getLineaActual()
                + ": Constante shortint fuera de rango (" + valor + ")");
        token_actual.setLength(0);
        AnalizadorLexico.estado_actual = 0;
        return AnalizadorLexico.token_abierto;
    }
}