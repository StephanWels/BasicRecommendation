package ste.wel.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

public class ConvertBasketData {

    private static Map<String, List<Integer>> basketItems = new HashMap<>();

    public static void conertDataToMahout(final String inputFile, final String outputFile) throws IOException {
        // load CSV
        final Path inputPath = Paths.get(inputFile);
        final List<String> lines = Files.readAllLines(inputPath);
        lines.remove(0);
        lines.stream().forEach(string -> addItem(string));
        final List<String> outputLines = lines.stream().map(line -> toMahoutLine(line)).collect(Collectors.toList());

        final OpenOption[] openOptions = { StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.CREATE };
        Files.write(Paths.get(outputFile), outputLines, openOptions);
    }

    private static String toMahoutLine(final String line) {
        return line.split(";")[1] + "," + line.split(";")[4] + "1.0";
    }

    public static void convertDataToSets(final String inputFile, final String outputFile) throws IOException {
        collectBaskets(inputFile);

        writeToBasketFormat(outputFile);

    }

    private static void collectBaskets(final String inputFile) throws IOException {
        basketItems.clear();
        // load CSV
        final Path inputPath = Paths.get(inputFile);
        final List<String> lines = Files.readAllLines(inputPath);
        lines.remove(0);
        lines.stream().forEach(string -> addItem(string));
    }

    private static void writeToBasketFormat(final String outputFile) throws IOException {

        final List<String> basketLines = basketItems.values().stream().map(items -> StringUtils.join(items, ","))
            .collect(Collectors.toList());
        final OpenOption[] openOptions = { StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.CREATE };
        Files.write(Paths.get(outputFile), basketLines, openOptions);
    }

    private static void addItem(final String string) {
        // if (counter++ >= 2000) {
        // return;
        // }
        final String basketId = string.split(";")[1];
        final int itemId = Integer.parseInt(string.split(";")[4]);
        if (!basketItems.containsKey(basketId)) {
            basketItems.put(basketId, new LinkedList<>());
        }
        basketItems.get(basketId).add(itemId);
    }

    public static void conertDataToMahoutSet(final String inputFile, final String outputFile) throws IOException {
        collectBaskets(inputFile);
        writeToMahoutSetFormat(outputFile);
    }

    private static void writeToMahoutSetFormat(final String outputFile) throws IOException {
        final List<String> basketLines = basketItems.values().stream()
            .map(items -> StringUtils.join(items, " "))
            .collect(Collectors.toList());
        final OpenOption[] openOptions = { StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.CREATE };
        Files.write(Paths.get(outputFile), basketLines, openOptions);
    }
}
