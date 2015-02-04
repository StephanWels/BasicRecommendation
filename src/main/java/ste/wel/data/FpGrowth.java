package ste.wel.data;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Comparator;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ca.pfv.spmf.algorithms.associationrules.agrawal94_association_rules.AssocRule;
import ca.pfv.spmf.algorithms.associationrules.agrawal94_association_rules.AssocRules;
import ca.pfv.spmf.algorithms.associationrules.closedrules.AlgoClosedRules;
import ca.pfv.spmf.algorithms.frequentpatterns.fpgrowth.AlgoFPGrowth;
import ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemsets;

@Component
public class FpGrowth {

    ItemProvider itemProvider;

    AssocRules rules;

    @Autowired
    public FpGrowth(final ItemProvider itemProvider) {
        this.itemProvider = Objects.requireNonNull(itemProvider, "itemProvider must not be null");
    }

    public void run(final String inputString) throws FileNotFoundException, IOException {
        // STEP 1: Applying the FP-GROWTH algorithm to find frequent itemsets
        final double minsupp = 0.0005;
        final AlgoFPGrowth fpgrowth = new AlgoFPGrowth();
        final Itemsets patterns = fpgrowth.runAlgorithm(inputString, null, minsupp);
        final int databaseSize = fpgrowth.getDatabaseSize();

        // STEP 2: Generating all rules from the set of frequent itemsets (based on Agrawal & Srikant, 94)
        final double minconf = 0.20;
        final AlgoClosedRules algoAgrawal = new AlgoClosedRules();
        // the next line run the algorithm.
        // Note: we pass null as output file path, because we don't want
        // to save the result to a file, but keep it into memory.
        final AssocRules assocRules = algoAgrawal.runAlgorithm(patterns, null, databaseSize, minconf);
        assocRules.getRules().stream().filter(rule -> rule.getAbsoluteSupport() > 50 && rule.getConfidence() > 0.3)
            .sorted(new RuleSort()).forEach(rule -> printRule(rule));
        System.out.println("RULES: " + assocRules.getRulesCount());
        System.out.println("DATABASE SIZE " + databaseSize);
        this.rules = assocRules;
    }

    private void printRule(final AssocRule rule) {
        System.out.println("Support: " + rule.getAbsoluteSupport() + " Confidence: " + rule.getConfidence() + "  |  "
            + toItemNames(rule.getItemset1()) + " -> " + toItemNames(rule.getItemset2()));
    }

    private String toItemNames(final int[] itemset1) {
        String string = "";
        for (final int id : itemset1) {
            string += itemProvider.getItem(id).getName() + "   ";
        }
        return string;
    }

    class RuleSort implements Comparator<AssocRule> {

        @Override
        public int compare(final AssocRule o1, final AssocRule o2) {
            if (o1.getItemset1().length != o2.getItemset1().length) {
                return o2.getItemset1().length - o1.getItemset1().length;
            }
            if (o1.getAbsoluteSupport() != o2.getAbsoluteSupport()) {
                return o2.getAbsoluteSupport() - o1.getAbsoluteSupport();
            }
            return (int) Math.signum(o2.getConfidence() - o1.getConfidence());
        }

    }

    public AssocRules getRules() {
        return rules;
    }
}
