package ste.wel.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ItemProvider {

    HashMap<Integer, String> items = new HashMap<>();
    Set<SearchProduct> list = new HashSet<>();
    HashSet<Integer> filter;

    public ItemProvider(final String inputFile) throws IOException {
        // load CSV
        final Path inputPath = Paths.get(inputFile);
        final List<String> lines = Files.readAllLines(inputPath);
        lines.remove(0);
        lines.stream().forEach(string -> addItem(string));

    }

    private void addItem(final String line) {
        final String name = line.split(";")[8];
        final Integer id = Integer.parseInt(line.split(";")[4]);
        if (!items.values().contains(id)) {
            list.add(new SearchProduct(name, id));
        }
        items.put(id, name);
    }

    public List<SearchProduct> getItems(final Optional<String> term) {
        return term.map(
            presentTerm -> list.stream()
                .filter(item -> item.getName().toLowerCase().contains(presentTerm.toLowerCase()))
                .limit(50)
                .collect(Collectors.toList())).orElse(list.stream().limit(50).collect(Collectors.toList()));
    }

    public SearchProduct getItem(final Integer itemId) {
        return new SearchProduct(items.get(itemId), itemId);
    }

    public void setFilter(final HashSet<Integer> interestingProducts) {
        list = list.stream().filter(product -> interestingProducts.contains(product.getId()))
            .collect(Collectors.toSet());
    }

}
