package src.token;
import src.compilador.TablaPalabrasReservadas;
public class TokenOperador extends Token {
    private int numeroASCII; //el numero que Yacc espera (ej. 255)
    //de no poder conseguir el ASCII todavia tal vez podamos juntar TokenOperador y TokenPalabraReservada en una clase TokenTemporal
    public TokenOperador(int numeroASCII, int linea) {
        super(numeroASCII);
        this.numeroASCII = numeroASCII;
        this.linea = linea;
    }

    public int getTipo() {
        return this.tipo;
    }

    public int getNumeroASCII() {
        return this.numeroASCII;
    }

   
}