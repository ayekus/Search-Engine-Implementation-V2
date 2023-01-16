import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class CrawlView extends Pane {
    static private final Button crawl = new Button("Crawl");
    static private final Button toSearch = new Button("To Search");
    private final TextField input = new TextField("Enter link here");
    private final Label resultLabel = new Label("Enter a link and search to crawl");

    public Button getToSearchButton() {
        return toSearch;
    }

    public Button getCrawlButton() {
        return crawl;
    }

    public String getCrawlLink() {
        return input.getText();
    }

    public CrawlView() {
        // Create the labels
        Label linkLabel = new Label("Link:");
        linkLabel.relocate(25, 53);
        linkLabel.setFont(Font.font("Arial", FontWeight.BOLD, 15));

        resultLabel.setFont(Font.font("Arial", 15));
        resultLabel.relocate(25, 135);

        // Create input field
        input.setAlignment(Pos.BASELINE_LEFT);
        input.relocate(70, 50);
        input.setPrefSize(375, 20);

        // Create buttons
        crawl.relocate(220, 95);
        toSearch.relocate(410, 158);

        // Add all the components to the Pane
        getChildren().addAll(linkLabel, resultLabel, input, toSearch, crawl);

        setPrefSize(500, 440);
    }

    public void crawl() {
        resultLabel.setText("Crawling... ");
        crawl.setDisable(true);
    }

    public void doneCrawl(String text) {
        resultLabel.setText(text);
        crawl.setDisable(false);
    }
}