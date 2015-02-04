package ste.wel.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ca.pfv.spmf.algorithms.associationrules.agrawal94_association_rules.AssocRule;
import ca.pfv.spmf.algorithms.associationrules.agrawal94_association_rules.AssocRules;

@Component
public class Recommender {

    private static final String BASKETS_CSV = "src/main/resources/baskets.csv";
    private static final String BASKETS_BASKET = "src/main/resources/baskets.basket";
    private static final String BASKETS_MAHOUT = "src/main/resources/baskets.mahout";
    private static final String BASKETS_MAHOUT_SET = "src/main/resources/baskets.mahoutSet";

    Map<String, Set<Integer>> recommendations = new HashMap<>();
    Map<String, Map<Integer, String>> support = new HashMap<>();
    Map<String, Map<Integer, Double>> confidence = new HashMap<>();
    HashSet<Integer> interestingProducts = new HashSet<>();

    ItemProvider itemProvider;

    FpGrowth fpGrowth;

    @Autowired
    public Recommender(final FpGrowth fpGrowth, final ItemProvider itemProvider) throws IOException {
        this.fpGrowth = Objects.requireNonNull(fpGrowth, "fpGrowth must not be null");
        this.itemProvider = Objects.requireNonNull(itemProvider, "itemProvider must not be null");

        // convert CSV
        ConvertBasketData.convertDataToSets(BASKETS_CSV, BASKETS_BASKET);
        ConvertBasketData.conertDataToMahout(BASKETS_CSV, BASKETS_MAHOUT);
        ConvertBasketData.conertDataToMahoutSet(BASKETS_CSV, BASKETS_MAHOUT_SET);

        fpGrowth.run(BASKETS_MAHOUT_SET);

        final AssocRules rules = fpGrowth.getRules();
        rules.getRules().stream().forEach(this::addRule);
        System.out.println(recommendations.size() + " rules extracted");
        System.out.println(interestingProducts.size() + " interesting products");
        itemProvider.setFilter(interestingProducts);
    }

    public List<SearchProduct> recommendForItems(final List<SearchProduct> selectedItems) {
        final Set<Integer> ids = new HashSet<>();
        selectedItems.stream().forEach(item -> ids.add(item.getId()));

        final Set<Set<Integer>> powerset = powerSet(ids);

        final Set<SearchProduct> recommendedItems = new HashSet<>();
        for (final Set<Integer> set : powerset) {
            final List<Integer> list = new LinkedList<>(set);
            Collections.sort(list);

            final String ref = Arrays.toString(list.toArray());
            if (recommendations.containsKey(ref)) {
                recommendations.get(ref).forEach(
                    id -> recommendedItems.add(itemProvider.getItem(id).withConfidence(confidence.get(ref).get(id))));
            }
        }
        ArrayList<SearchProduct> sortedRecommendations = new ArrayList<>(recommendedItems);
        sortedRecommendations.sort(new ByConfidenceComparator());
        sortedRecommendations = (ArrayList<SearchProduct>) sortedRecommendations.stream()
            .filter(product -> !selectedItems.contains(product))
            .collect(Collectors.toList());
        return sortedRecommendations;
    }

    private void addRule(final AssocRule rule) {
        final int[] antecedent = rule.getItemset1();
        final int consequent = rule.getItemset2()[0];
        Arrays.sort(antecedent);

        for (final int id : antecedent) {
            interestingProducts.add(id);
        }

        final String key = Arrays.toString(antecedent);
        if (!recommendations.containsKey(key)) {
            recommendations.put(key, new HashSet<>());
            support.put(key, new HashMap<>());
            confidence.put(key, new HashMap<>());
        }
        recommendations.get(key).add(consequent);
        support.get(key).put(consequent, String.valueOf(rule.getAbsoluteSupport()));
        confidence.get(key).put(consequent, rule.getConfidence());
    }

    public static Set<Set<Integer>> powerSet(final Set<Integer> originalSet) {
        final Set<Set<Integer>> sets = new HashSet<>();
        if (originalSet.isEmpty()) {
            sets.add(new HashSet<Integer>());
            return sets;
        }
        final List<Integer> list = new ArrayList<>(originalSet);
        final Integer head = list.get(0);
        final Set<Integer> rest = new HashSet<>(list.subList(1, list.size()));
        for (final Set<Integer> set : powerSet(rest)) {
            final Set<Integer> newSet = new HashSet<>();
            newSet.add(head);
            newSet.addAll(set);
            sets.add(newSet);
            sets.add(set);
        }
        return sets;
    }

    public List<String> explainForItem(final List<SearchProduct> selectedItems, final Integer itemId) {
        final List<String> explain = new LinkedList<>();
        final Set<Integer> ids = new HashSet<>();
        selectedItems.stream().forEach(item -> ids.add(item.getId()));

        final Set<Set<Integer>> powerset = powerSet(ids);

        for (final Set<Integer> set : powerset) {
            final List<Integer> list = new LinkedList<>(set);
            Collections.sort(list);

            final String ref = Arrays.toString(list.toArray());
            if (recommendations.containsKey(ref) && confidence.get(ref).containsKey(itemId)) {
                explain.add(list.stream()
                    .map(id -> itemProvider.getItem(id))
                    .collect(Collectors.toList())
                    .toString()
                    + " -> " + itemProvider.getItem(itemId).getName() + "     (Support: "
                    + support.get(ref).get(itemId) + ", Confidence: "
                    + String.valueOf(confidence.get(ref).get(itemId)) + ")");
            }
        }

        return explain;
    }

    class ByConfidenceComparator implements Comparator<SearchProduct> {

        @Override
        public int compare(final SearchProduct o1, final SearchProduct o2) {
            return (int) Math.signum(o2.getConfidence() - o1.getConfidence());
        }

    }
}
