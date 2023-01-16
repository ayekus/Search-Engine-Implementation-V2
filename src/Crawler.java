import java.io.*;
import java.util.*;

public class Crawler {
    private Integer totalPages = 0;
    private final Queue<String> queue = new LinkedList<>();
    private final HashMap<String, ArrayList<String>> incoming_links = new HashMap<>();
    private final HashMap<String, Integer> allWords = new HashMap<>();
    private final HashSet<String> read = new HashSet<>();
    private final HashMap<Integer, Page> pagesID = new HashMap<>();
    private final HashMap<String, Page> pages = new HashMap<>();

    public void fresh_crawl() {
        delete(new File("data"));
        Page.resetIdf();
    }

    private void delete(File path) {
        if (path.isDirectory()) {
            for (File subpath : path.listFiles()) {
                delete(subpath);
            }
            path.delete();
        } else {
            path.delete();
        }
    }

    public Boolean crawl(String seed) {
        queue.offer(seed);
        int count = 0;
        File data = new File("data");
        data.mkdirs();

        while (queue.size() > 0) {
            try {
                write_info(WebRequester.readURL(queue.peek()));
                totalPages++;
                read.add(queue.poll());
                count = 0;
            } catch (IOException e) {
                System.err.println("Could not get contents from link " + queue.peek());
                count++;
            }

            if (count >= 10) {
                delete(data);
                queue.removeAll(queue);
                return false;
            }
        }
        write_final_info();
        return true;
    }

    private void write_info(String content) {
        Page temp = new Page(queue.peek(), totalPages);
        temp.setTitle(content.substring(content.indexOf("<title>") + 7, content.indexOf("</title>")));

        // for getting all the information from the page
        HashMap<String, Integer> words = new HashMap<>();
        String tempContent = content, info;
        String[] infoList;
        int totalWords = 0;
        // repeat until there aren't anymore p tags
        while (true) {
            int index = tempContent.indexOf("<p");

            // if there isn't another instance of <p, index will be -1, stopping the loop
            if (index == -1) {
                break;
            }
            tempContent = tempContent.substring(index);

            info = tempContent.substring(tempContent.indexOf('>') + 1, tempContent.indexOf("</p>"));

            if (info.contains("\n")) {
                infoList = info.split("\n");
            } else {
                infoList = info.split(" ");
            }

            for (String word : infoList) {
                if (!word.equals("")) {
                    if (words.containsKey(word)) {
                        words.put(word, words.get(word) + 1);
                    } else {
                        words.put(word, 1);
                    }
                    totalWords++;
                }
            }

            // this is to get rid of the instance of <p to make sure that in the next loop
            // it doesn't go through the same paragraph
            tempContent = tempContent.substring(1);
        }

        // to write tf values for each word in the page
        for (String word : words.keySet()) {
            double tf = words.get(word) * 1.0 / totalWords;
            temp.addTf(word, tf);

            // add words to allWords for idf
            if (allWords.containsKey(word)) {
                allWords.put(word, allWords.get(word) + 1);
            } else {
                allWords.put(word, 1);
            }

        }

        // to add new links to the queue and links_out
        String[] tempContentArray = content.split("\n");
        ArrayList<String> links_out = new ArrayList<>();
        for (String line : tempContentArray) {
            if (line.contains("href=\"")) {
                if (!line.contains("http://")) {
                    line = line.substring(line.indexOf("./") + 2, line.indexOf("\">"));
                    line = queue.peek().substring(0, queue.peek().lastIndexOf('/') + 1) + line;
                }
                links_out.add(line);
                if (!read.contains(line) && !queue.contains(line)) {
                    queue.add(line);
                }
            }
        }
        temp.setOutgoing_links(links_out);

        // to add the outgoing links and storing info for incoming links
        for (String link : links_out) {
            ArrayList<String> link_list = new ArrayList<>();
            if (incoming_links.containsKey(link)) {
                link_list.addAll(incoming_links.get(link));
            }
            link_list.add(queue.peek());
            incoming_links.put(link, link_list);
        }

        pagesID.put(temp.getID(), temp);
        pages.put(temp.getUrl(), temp);
    }

    private void write_final_info() {
        // write incoming links
        for (String link : incoming_links.keySet()) {
            pages.get(link).setIncoming_links(incoming_links.get(link));
        }

        // write idfs
        for (String word : allWords.keySet()) {
            Page.addIdf(word, (Math.log(totalPages / (1.0 + allWords.get(word) * 1.0))) / Math.log(2.0));
        }

        // write tfidf for each word in each page
        for (Page temp : pages.values()) {
            for (String word : temp.getWords()) {
                temp.addTfidf(word, (Math.log(1 + temp.getTf(word)) / Math.log(2)) * Page.getIdf(word));
            }
        }

        // write page ranks
        Double[][] matrix = new Double[totalPages][];
        for (int pageID : pagesID.keySet()) {
            ArrayList<String> links = pagesID.get(pageID).getOutgoing_links();

            Double[] temp = new Double[totalPages];
            for (String link : links) {
                temp[pages.get(link).getID()] = (1.0 / links.size()) * (1 - 0.1);
            }

            for (int num = 0; num < temp.length; num++) {
                if (temp[num] == null) {
                    temp[num] = 0.1 / totalPages;
                } else {
                    temp[num] += 0.1 / totalPages;
                }
            }
            matrix[pageID] = temp;
        }

        Double[] temp = new Double[totalPages];
        for (int d = 0; d < totalPages; d++) {
            temp[d] = (1.0 / totalPages);
        }

        Double[][] old = { temp };
        Double[][] newArr;

        while (true) {
            newArr = mult_matrix(old, matrix);
            if (euclidean_dist(newArr, old) < 0.0001) {
                break;
            }
            old = newArr;
        }

        int id = 0;
        for (Double num : newArr[0]) {
            pagesID.get(id).setPagerank(num);
            id++;
        }

        for (int page : pagesID.keySet()) {
            Page current = pagesID.get(page);
            try {
                ObjectOutputStream output = new ObjectOutputStream(
                        new FileOutputStream("data" + File.separator + page + ".dat"));
                output.writeObject(current);
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            ObjectOutputStream output = new ObjectOutputStream(
                    new FileOutputStream("data" + File.separator + "idf.dat"));
            output.writeObject(Page.getFullIdf());
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private Double[][] mult_matrix(Double[][] a, Double[][] b) {
        Double[][] newMatrix = new Double[a.length][];
        for (int i = 0; i < a.length; i++) {
            Double[] temp = new Double[b[0].length];
            for (int j = 0; j < b[0].length; j++) {
                double total = 0.0;
                for (int k = 0; k < b.length; k++) {
                    total += a[i][k] * b[k][j];
                }
                temp[j] = total;
            }
            newMatrix[i] = temp;
        }
        return newMatrix;
    }

    private Double euclidean_dist(Double[][] a, Double[][] b) {
        double distance;
        double totalDistance = 0.0;
        for (int i = 0; i < a[0].length; i++) {
            distance = Math.pow((a[0][i] - b[0][i]), 2);
            totalDistance += distance;
        }
        totalDistance = Math.sqrt(totalDistance);
        return totalDistance;
    }

}
