package src.compilador;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FileHelper {

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