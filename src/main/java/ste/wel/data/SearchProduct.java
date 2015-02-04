package ste.wel.data;

public class SearchProduct {

    public SearchProduct(final String value, final int data) {
        this.name = value;
        this.id = data;
    }

    public SearchProduct(final String value, final int data, final double confidence) {
        this.name = value;
        this.id = data;
        this.confidence = confidence;
    }

    final String name;
    final int id;
    double confidence;

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public double getConfidence() {
        return confidence;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(final Object obj) {
        final SearchProduct other = (SearchProduct) obj;
        return other.getId() == id;
    }

    public SearchProduct withConfidence(final double d) {
        this.confidence = d;
        return this;
    }

}
