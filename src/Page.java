import java.io.Serializable;
import java.util.*;

public class Page implements Serializable {
    private final ArrayList<String> incoming_links;
    private final ArrayList<String> outgoing_links;
    private final HashMap<String, Double> tf;
    private final HashMap<String, Double> tfidf;
    private String title;
    private final String url;
    private final int numberID;
    private double pagerank;
    private static final HashMap<String, Double> idf = new HashMap<>();

    Page(String initUrl, int initID) {
        this.title = "";
        this.url = initUrl;
        this.numberID = initID;
        this.tf = new HashMap<>();
        this.tfidf = new HashMap<>();
        this.incoming_links = new ArrayList<>();
        this.outgoing_links = new ArrayList<>();
    }

    public String getUrl() {
        return this.url;
    }

    public void setTitle(String t) {
        this.title = t;
    }

    public String getTitle() {
        return this.title;
    }

    public void setPagerank(double p) {
        this.pagerank = p;
    }

    public double getPagerank() {
        return this.pagerank;
    }

    public void setOutgoing_links(ArrayList<String> finalOutgoing_links) {
        this.outgoing_links.addAll(finalOutgoing_links);
    }

    public ArrayList<String> getOutgoing_links() {
        return this.outgoing_links;
    }

    public int getID() {
        return this.numberID;
    }

    public void setIncoming_links(ArrayList<String> finalIncoming_links) {
        this.incoming_links.addAll(finalIncoming_links);
    }

    public ArrayList<String> getIncoming_links() {
        return this.incoming_links;
    }

    public void addTf(String key, double val) {
        this.tf.put(key, val);
    }

    public void addTfidf(String key, double val) {
        this.tfidf.put(key, val);
    }

    public Set<String> getWords() {
        return tf.keySet();
    }

    public static void addIdf(String key, double val) {
        idf.put(key, val);
    }

    public static double getIdf(String word) {
        if (idf.containsKey(word)) {
            return idf.get(word);
        }
        return 0.0;
    }

    public static HashMap<String, Double> getFullIdf() {
        return idf;

    }

    public static void setFullIdf(HashMap<String, Double> init) {
        idf.putAll(init);
    }

    public double getTf(String word) {
        if (tf.get(word) != null) {
            return this.tf.get(word);
        }
        return 0.0;
    }

    public double getTfidf(String word) {
        if (tfidf.get(word) != null) {
            return this.tfidf.get(word);
        }
        return 0.0;
    }

    public static void resetIdf() {
        idf.clear();
    }

}
