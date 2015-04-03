/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package browser;

/**
 *
 * @author mschavinda
 */
import javafx.scene.control.Menu;
import javafx.application.Platform;
import javafx.scene.control.MenuBar;
import javafx.geometry.*;
import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker.State;
import javafx.scene.image.Image;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Label;
import javafx.scene.web.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.input.*;
import javafx.beans.value.ChangeListener;

public class Browser extends Application{
	final private ProgressBar progress = new ProgressBar();
	final private WebView browser = new WebView();
	final private WebEngine webEngine = browser.getEngine();
        final WebHistory history = webEngine.getHistory();
	private Button go;
        private Button back;
        private Button forward;
	private TextField webAdd;
	private Label status;
	private Scene scene;
	private Pane pane;
	
	@Override 
	public void init(){
            pane =  new FlowPane();
            webAdd = new TextField();
            status = new Label();

            HBox hbox = new HBox(3);
            HBox menu = new HBox();
            VBox vbox =  new VBox();


            go = new Button("Go");
            back =  new Button("<-");
            back.setDisable(true);
            forward =  new Button("->");
            forward.setDisable(true);

            progress.setVisible(false);

            browser.prefWidthProperty().bind(pane.widthProperty());
            browser.prefHeightProperty().bind(pane.heightProperty().subtract(90));

            MenuBar menuBar = new MenuBar();

            // --- Menu File
            Menu menuFile = new Menu("File");

            // --- Menu Edit
            Menu menuEdit = new Menu("Edit");

            // --- Menu View
            Menu menuView = new Menu("View");

            Menu menuTools = new Menu("Tools");

            Menu menuHelp = new Menu("Help");

            Menu menuWindow = new Menu("Window");
 
            menuBar.getMenus().addAll(menuFile, menuEdit, menuView, menuTools, menuWindow, menuHelp);
            menu.getChildren().add(menuBar);
            menu.prefWidthProperty().bind(pane.widthProperty());


            HBox bottom =  new HBox(20);
            bottom.setPadding(new Insets(5));
            bottom.getChildren().addAll(status,progress);

            go.setOnAction(new EventHandler<ActionEvent>() {
                @Override public void handle(ActionEvent e) {
                        goToAddress();
                }
            });

            back.setOnAction(new EventHandler<ActionEvent>() {
                @Override public void handle(ActionEvent e) {
                    
                        goBack();
                }
            });

            forward.setOnAction(new EventHandler<ActionEvent>() {
                    @Override public void handle(ActionEvent e) {
                            back.setDisable(false);
                            goForward();
                    }
            });

            webAdd.setOnKeyPressed(new EventHandler<KeyEvent>(){
                    @Override
                    public void handle(KeyEvent ke){
                            if (ke.getCode().equals(KeyCode.ENTER)){
                                    goToAddress();
                                    //System.out.println(webEngine.getHistory().toString());
                            }
                    }
            });

            webAdd.prefColumnCountProperty().bind(pane.widthProperty().divide(14));
            hbox.getChildren().addAll(back, forward, webAdd, go);
            hbox.setPadding(new Insets(5, 5, 5, 5));
            vbox.getChildren().add(browser);
            pane.getChildren().addAll(menu, hbox, vbox, bottom);
            pane.setPrefSize(1000,500);		
	}

	@Override
	public void start(Stage primaryStage){
		webEngine.load("http://www.google.com");
		scene = new Scene(pane);
		
                if (history.getMaxSize() != 0) back.setDisable(false);
                else { back.setDisable(true); }
                
		webEngine.getLoadWorker().stateProperty().addListener(
                    new ChangeListener<State>() {
                        @Override
                        public void changed(ObservableValue ov, State oldState, State newState) {
                            if (newState == State.SUCCEEDED) {
                                primaryStage.setTitle(webEngine.getTitle());
                                status.setText(webEngine.getLocation());
                                progress.progressProperty().bind(webEngine.getLoadWorker().progressProperty());
                                progress.visibleProperty().bind(progress.progressProperty().lessThan(1));
                                webAdd.setText(webEngine.getLocation());
                            }
                        }
                    });

		primaryStage.setScene(scene);
		primaryStage.setTitle("Chav's Browser");
		primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("c_blue.png")));
		primaryStage.show();
	}
	
	public void goToAddress(){
            progress.progressProperty().bind(webEngine.getLoadWorker().progressProperty());
            progress.visibleProperty().bind(progress.progressProperty().lessThan(1));
            
            StringBuilder webText = new StringBuilder();
            int dotCount = 0;
            webText.append(webAdd.getText());
            String webScan = webAdd.getText();
            boolean search = false;
            int len = webScan.length();
            
            for(int i = 0; i < len; i++){
                if (webScan.charAt(i) == '.') dotCount++;

                if (webScan.charAt(i) == ' '){
                        webText.replace(i, i + 1, "+");
                        search = true;
                }
            }

            if (webText.toString().substring(0, 4).equals("http") == false){
                if (webText.toString().substring(0, 3).equals("www") == false && dotCount < 3 && !search)
                        webText.insert(0, "www.");

                if (search){
                        webText.insert(0,"http://www.google.com/search?hl=en&source=hp&q=");
                }else{
                        webText.insert(0, "http://");
                }
            }

            webEngine.load(webText.toString());
            if (!webAdd.isFocused()) webAdd.setText(webEngine.getLocation());
	}
        
        public void goBack(){    
            ObservableList<WebHistory.Entry> entryList = history.getEntries();
            int currentIndex=history.getCurrentIndex();
            Platform.runLater(new Runnable() { public void run() { history.go(-1); } });
            webEngine.load(entryList.get(currentIndex>0?currentIndex-1:currentIndex).getUrl());
        }

        public void goForward(){    
            ObservableList<WebHistory.Entry> entryList=history.getEntries();
            int currentIndex=history.getCurrentIndex();
            Platform.runLater(new Runnable() { public void run() { history.go(1); } });
            webEngine.load(entryList.get(currentIndex<entryList.size()-1?currentIndex+1:currentIndex).getUrl());
        }
}
