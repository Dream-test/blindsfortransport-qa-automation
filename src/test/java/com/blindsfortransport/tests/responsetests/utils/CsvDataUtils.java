package com.blindsfortransport.tests.responsetests.utils;

import org.junit.jupiter.params.provider.Arguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class CsvDataUtils {
    private static final Logger logger = LoggerFactory.getLogger(CsvDataUtils.class);

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

    public static Stream<Arguments> readUrlsWithStatusFromCsv(String fileName) {
        InputStream inputStream = CsvDataUtils.class.getClassLoader().getResourceAsStream("testdata/" + fileName);

        if (inputStream == null) {
            logger.error("File testdata/{} not found in resources", fileName);
            return null; // или return null, если ты обрабатываешь это отдельно
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            List<Arguments> data = reader.lines()
                    .skip(1) // пропустить заголовок
                    .map(line -> line.split(","))
                    .filter(parts -> parts.length == 2)
                    .map(parts -> Arguments.of(parts[0].trim(), Integer.parseInt(parts[1].trim())))
                    .toList();

            return data.stream();
        } catch (IOException e) {
            logger.error("Failed to read CSV file: {}", fileName, e);
            return null; // либо null — но лучше пустой поток, чтобы тесты просто пропустились
        }
    }
}
