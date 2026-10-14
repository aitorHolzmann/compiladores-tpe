package src.compilador;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class FileHelper {

    public static Map<String, Integer> readMapFile(String path) {
        Map<String, Integer> map = new HashMap<>();

        InputStream recurso = FileHelper.class.getResourceAsStream(path);
        if (recurso == null) {
            System.out.println("No se encontro la tabla " + path + " dentro del programa");
            return map;
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(recurso, StandardCharsets.UTF_8))) {
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