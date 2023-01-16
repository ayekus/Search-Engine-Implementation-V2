public class Result implements SearchResult {
    private final String title;
    private final double score;

    public Result(String t, double s) {
        this.title = t;
        this.score = s;
    }

    public String getTitle() {
        return this.title;
    }

    public double getScore() {
        return this.score;
    }

    public String toString() {
        return this.title + ": " + this.score;
    }

}
