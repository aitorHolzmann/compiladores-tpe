package src.token;
import src.compilador.TablaPalabrasReservadas;  //esta clase es para los tokens de palabras reservadas y para los operadores
public class TokenPalabraReservada extends Token {

    public TokenPalabraReservada(int tipo, int linea) {
        super(tipo);
        this.linea = linea;
    }

    public int getTipo() {
        return this.tipo;
    }
   
}