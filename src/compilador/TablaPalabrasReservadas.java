package src.compilador;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class TablaPalabrasReservadas {
    public static final int NOT_FOUND = -1;

    private static final String PATH_TABLA_PALABRAS_RESERVADAS = "src/estructuras/tabla_palabras_reservadas";
        private static final Map<String, Integer> palabras_reservadas = readMapFile(PATH_TABLA_PALABRAS_RESERVADAS);

    public static int obtenerIdentificador(String palabra_reservada) {
        switch (palabra_reservada) {
            case "IDENTIFICADOR": return Parser.IDENTIFICADOR;
            case "CONSTANTE": return Parser.CONSTANTE;
            case "CADENA": return Parser.CADENA;
            case "IF": return Parser.IF;
            case "ELSE": return Parser.ELSE;
            case "END_IF": return Parser.END_IF;
            case "BEGIN": return Parser.BEGIN;
            case "END": return Parser.END;
            case "POUT": return Parser.POUT;
            case "RET": return Parser.RET;
            case "CLASS": return Parser.CLASS;
            case "FUNCTION": return Parser.FUNCTION;
            case "SHORTINT": return Parser.SHORTINT;
            case "SINGLEF": return Parser.SINGLEF;
            case "REPEAT": return Parser.REPEAT;
            case "UNTIL": return Parser.UNTIL;
            case "AUTO": return Parser.AUTO;
            case "COMPTIME": return Parser.COMPTIME;
            case "IMPORT": return Parser.IMPORT;
            case "FROM": return Parser.FROM;
            case "EXPORT": return Parser.EXPORT;
            case "TO": return Parser.TO;
            case "EXTENDS": return Parser.EXTENDS;
            case "TOS": return Parser.TOS;
            case ":=": return Parser.ASIGNAR;
            case ">=": return Parser.MAYORIGUAL;
            case "<=": return Parser.MENORIGUAL;
            case "==": return Parser.IGUALIGUAL;
            case "!=": return Parser.DISTINTO;
            default: return palabras_reservadas.getOrDefault(palabra_reservada, NOT_FOUND);
        }
    }

    public static Map<String, Integer> readMapFile(String path) {
        Map<String, Integer> map = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String linea;
            int numero_linea = 0;

            while ((linea = br.readLine()) != null) {
                numero_linea++;
                linea = linea.trim();
                if (linea.isEmpty()) {
                    continue; // ignora líneas vacías
                }

                String[] partes = linea.split("\\s+");
                if (partes.length != 2) {
                    System.out.println("Linea " + numero_linea + " del archivo " + path
                            + " mal formada, se ignora: \"" + linea + "\"");
                    continue;
                }

                try {
                    int identificador = Integer.parseInt(partes[1]);
                    map.put(partes[0], identificador);
                } catch (NumberFormatException e) {
                    System.out.println("Linea " + numero_linea + " del archivo " + path
                            + ": \"" + partes[1] + "\" no es un numero valido, se ignora");
                }
            }
        } catch (IOException excepcion) {
            System.out.println("No se pudo leer el archivo " + path);
            excepcion.printStackTrace();
        }

        return map;
    }
}