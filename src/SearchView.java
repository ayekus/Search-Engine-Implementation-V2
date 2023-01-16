import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;

public class SearchView extends Pane {
    private final CheckBox boost = new CheckBox();
    private final TextField input = new TextField("Enter search here");
     private final ListView<String> resultList;
     private final Button toCrawl = new Button("To Crawl");
     private final Button search = new Button("Search");

    public Button getToCrawlButton() {
        return toCrawl;
    }
    public Button getSearchButton() {
        return search;
    }

    public String getSearchQuery() {
        return input.getText();
    }

    public Boolean isBoost() {
        return boost.isSelected();
    }

    public SearchView() {
        // Create the labels
        Label linkLabel = new Label("Search Query:");
        linkLabel.relocate(25, 53);
        linkLabel.setFont(Font.font("Arial", FontWeight.BOLD , 15));
        Label boostLabel = new Label("Boost:");
        boostLabel.relocate(25, 95);
        boostLabel.setFont(Font.font("Arial", FontWeight.BOLD , 15));

        // Create input field
        input.setAlignment(Pos.BASELINE_LEFT);
        input.relocate(135, 50);
        input.setPrefSize(335, 20);

        // Create boost box
        boost.relocate(85, 95);

        // Create buttons
        search.relocate(220, 105);
        toCrawl.relocate(410, 400);

        // Create results list
        resultList = new ListView<>();
        resultList.setPlaceholder(new Label("Please search to get results. If you would like to\n   crawl from a new link, click the Crawl button"));
        resultList.relocate(25, 150);
        resultList.setPrefSize(450, 242);


        // Add all the components to the Pane
        getChildren().addAll(linkLabel, boostLabel, input, boost, search, toCrawl, resultList);

        setPrefSize(500, 440);
    }

    public void update(List<SearchResult> newList) {
        String[] rList = new String[newList.size()];
        int count = 0;
        for (SearchResult result : newList) {
            rList[count] = result.toString();
            count++;
        }

        resultList.setItems(FXCollections.observableArrayList(rList));
    }
}