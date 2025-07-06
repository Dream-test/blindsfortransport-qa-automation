package utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Stream;

public class CsvDataUtils {

    /**
     * Считывает строки из CSV-файла из папки resources/testdata
     *
     * @param fileName имя файла внутри папки testdata, например "allpaths.csv"
     * @return поток строк без первой строки (заголовка), без пустых строк и пробелов
     */
    public static Stream<String> readPathsFromCsv(String fileName) {
        InputStream inputStream = CsvDataUtils.class.getClassLoader()
                .getResourceAsStream("testdata/" + fileName);

        if (inputStream == null) {
            throw new RuntimeException("File testdata/" + fileName + " not found in resources");
        }

        return new BufferedReader(new InputStreamReader(inputStream))
                .lines()
                .skip(1) // пропускаем заголовок
                .map(String::trim)
                .filter(line -> !line.isEmpty());
    }
}
