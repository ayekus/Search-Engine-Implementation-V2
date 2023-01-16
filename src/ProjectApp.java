import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class ProjectApp extends Application {

    public void start(Stage primaryStage) {
        Pane aPane = new Pane();
        ProjectTesterImp test = new ProjectTesterImp();

        // Create the view
        CrawlView view = new CrawlView();
        SearchView view2 = new SearchView();

        aPane.getChildren().addAll(view);

        primaryStage.setTitle("Search Engine Implementation");
        primaryStage.setResizable(false);
        primaryStage.setScene(new Scene(aPane));
        primaryStage.show();

        view.getToSearchButton().setOnAction(new EventHandler<>() {
            public void handle(ActionEvent actionEvent) {
                aPane.getChildren().remove(view);
                aPane.getChildren().add(view2);
            }
        });

        view2.getToCrawlButton().setOnAction(new EventHandler<>() {
            public void handle(ActionEvent actionEvent) {
                aPane.getChildren().remove(view2);
                aPane.getChildren().add(view);
            }
        });

        view2.getSearchButton().setOnAction(new EventHandler<>() {
            public void handle(ActionEvent actionEvent) {
                if (!test.isInitialized()) {
                    test.initializeSearch();
                }
                view2.update(test.search(view2.getSearchQuery(), view2.isBoost(), 10));
            }
        });

        view.getCrawlButton().setOnAction(new EventHandler<>() {
            public void handle(ActionEvent actionEvent) {
                String link = view.getCrawlLink();
                if (!link.equals("") && !link.equals("Enter link here")) {
                    test.initialize();
                    view.crawl();
                    Boolean result = test.guiCrawl(link);
                    if (result) {
                        view.doneCrawl("Crawling complete, head to search to query results.");
                    } else {
                        view.doneCrawl("Crawling failed, please check link and try again.");
                    }
                } else {
                    view.doneCrawl("Please enter a link to Crawl.");
                }
            }

        });
    }

    public static void main(String[] args) {
        launch(args);
    }

}