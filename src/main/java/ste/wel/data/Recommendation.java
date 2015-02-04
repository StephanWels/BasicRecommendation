package ste.wel.data;

public class Recommendation {

    public Recommendation(final int id, final double confidence) {
        super();
        this.id = id;
        this.confidence = confidence;
    }

    private final int id;
    private final double confidence;

    public int getId() {
        return id;
    }

    public double getConfidence() {
        return confidence;
    }

}
