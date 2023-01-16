import java.io.*;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

public class ProjectTesterImp implements ProjectTester {
    private final Crawler crawl = new Crawler();
    private final HashMap<String, Page> pages = new HashMap<>();
    private boolean initialized = false;

    public void initialize() {
        crawl.fresh_crawl();
    }

    public void crawl(String seedURL) {
        this.guiCrawl(seedURL);
    }

    public Boolean guiCrawl(String seedURL) {
        this.initialized = false;
        Boolean result = crawl.crawl(seedURL);
        if (!result) {
            return false;
        }

        this.initializeSearch();
        return true;
    }

    public List<String> getOutgoingLinks(String url) {
        if (pages.containsKey(url)) {
            return pages.get(url).getOutgoing_links();
        }
        return null;
    }

    public List<String> getIncomingLinks(String url) {
        if (pages.containsKey(url)) {
            return pages.get(url).getIncoming_links();
        }
        return null;
    }

    public double getPageRank(String url) {
        if (pages.containsKey(url)) {
            return pages.get(url).getPagerank();
        }
        return -1;
    }

    public double getIDF(String word) {
        return Page.getIdf(word);
    }

    public double getTF(String url, String word) {
        if (pages.containsKey(url)) {
            return pages.get(url).getTf(word);
        }
        return 0.0;
    }

    public double getTFIDF(String url, String word) {
        if (pages.containsKey(url)) {
            return pages.get(url).getTfidf(word);
        }
        return 0.0;
    }

    public List<SearchResult> search(String query, boolean boost, int X) {
        if (!initialized) {
            this.initializeSearch();
        }

        int numWords = 0;
        HashMap<String, Integer> words = new HashMap<>();

        query = query.toLowerCase();
        String[] search = query.split(" ");

        for (String word : search) {
            if (!word.equals("")) {
                if (words.containsKey(word)) {
                    words.put(word, words.get(word) + 1);
                } else {
                    words.put(word, 1);
                }
                numWords++;
            }
        }

        String[] queryOrder = new String[numWords];
        Double[] queryVector = new Double[numWords];
        int count = 0;
        for (String word : words.keySet()) {
            queryOrder[count] = word;
            queryVector[count] = Math.log((words.get(word) / (double) numWords) + 1.0) * Page.getIdf(word);
            count++;
        }

        double denomLeft = 0.0;
        for (Double num : queryVector) {
            if (num != null) {
                denomLeft += Math.pow(num, 2);
            }
        }
        denomLeft = Math.sqrt(denomLeft);

        List<SearchResult> allResults = new ArrayList<>(pages.size());
        for (String url : pages.keySet()) {
            double score;

            Double[] docVector = new Double[queryVector.length];
            int index = 0;
            for (String word : queryOrder) {
                docVector[index++] = this.getTFIDF(url, word);
            }

            double denomRight = 0.0;
            double numerator = 0.0;
            double cosineSimilarity;
            for (int i = 0; i < queryVector.length; i++) {
                if (queryVector[i] != null) {
                    numerator += queryVector[i] * docVector[i];
                    denomRight += Math.pow(docVector[i], 2);
                }
            }
            if (denomRight == 0.0) {
                cosineSimilarity = 0.0;
            } else {
                denomRight = Math.sqrt(denomRight);
                cosineSimilarity = numerator / (denomLeft * denomRight);
            }

            if (boost) {
                score = cosineSimilarity * (this.getPageRank(url));
            } else {
                score = cosineSimilarity;
            }
            allResults.add(new Result(this.getTitle(url), score));

        }

        List<SearchResult> top = new ArrayList<>();

        for (int i = 0; i < X; i++) {
            int index = 0;
            if (allResults.isEmpty()) {
                break;
            }
            for (int j = 0; j < allResults.size(); j++) {
                if (index != j) {
                    int result = compareTo(allResults.get(index), allResults.get(j));
                    if (result <= -1) {
                        index = j;
                    }
                }
            }
            top.add(allResults.get(index));
            allResults.remove(index);
        }

        return top;
    }

    private String getTitle(String url) {
        if (pages.containsKey(url)) {
            return pages.get(url).getTitle();
        }
        return "";
    }

    private int compareTo(SearchResult one, SearchResult two) {
        DecimalFormat df = new DecimalFormat("#.###");
        df.setRoundingMode(RoundingMode.HALF_UP);

        if (Double.parseDouble(df.format(one.getScore())) != Double.parseDouble(df.format(two.getScore()))) {
            if (Double.parseDouble(df.format(one.getScore())) > Double.parseDouble(df.format(two.getScore()))) {
                return 1;
            } else {
                return -1;
            }
        }

        return two.getTitle().compareTo(one.getTitle());
    }

    public void initializeSearch() {
        File data = new File("data");
        for (String filename : data.list()) {
            if (filename != null && !filename.equals("idf.dat")) {
                try {
                    ObjectInputStream input = new ObjectInputStream(
                            new FileInputStream("data" + File.separator + filename));
                    Page current = (Page) input.readObject();
                    input.close();
                    pages.put(current.getUrl(), current);
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            ObjectInputStream input = new ObjectInputStream(
                    new FileInputStream("data" + File.separator + "idf.dat"));
            Page.setFullIdf((HashMap<String, Double>) input.readObject());
            input.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        this.initialized = true;
    }

    public boolean isInitialized() {
        return this.initialized;
    }

}
