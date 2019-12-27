import javafx.util.Duration;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class ServerGui extends Application {
	boolean firstConnect = false;
	Button start;
	BorderPane setup;
	BorderPane main;
	BorderPane wait;
	VBox intro;
	TextField port; 
	Label inputPort;
	Server serverConnect;
	ListView<String> display; 
	GameInfo currentInfo;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		launch(args);
	}

	//feel free to remove the starter code from this method
	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setTitle("Connect");
		primaryStage.setScene(createSetup());
		primaryStage.show();
		
		port.setOnKeyPressed(e -> {;
			start.setDisable(false);
		});
		
		start.setOnAction(e-> {
			primaryStage.setScene(createWait());
			primaryStage.setTitle("Waiting for Players");
			serverConnect = new Server(Integer.parseInt(port.getText()), data -> {
				Platform.runLater(() -> {
					currentInfo = (GameInfo) data;
					primaryStage.setTitle("Server");
					if (currentInfo.status == 'w') {
						if (currentInfo.connected == false) {
							display.getItems().add("Player " + currentInfo.ID + " has connected.");
							currentInfo.connected = true;
						}
					}
					else if (currentInfo.status == 'x')
						display.getItems().add("Player " + currentInfo.ID + " has disconnected.");
					else if (currentInfo.status == 'c')
						display.getItems().add("Player " + currentInfo.ID + " is challenging Player " + currentInfo.oppID + ".");
					else if (currentInfo.status == 'g')
						display.getItems().add("Player " + currentInfo.ID + " played " + currentInfo.myPlay + ".");
				});
			});
		});
		
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });
		
	}

	public Scene createSetup() {
		intro = new VBox();
		start = new Button("Start");
		start.setDisable(true);
		inputPort = new Label("Input Port: ");
		port = new TextField();
		port.setMaxWidth(100);
		intro.getChildren().addAll(inputPort, port, start);
		intro.setAlignment(Pos.CENTER);
		intro.setSpacing(20);
		setup = new BorderPane();
		setup.setCenter(intro);
		Scene introScene = new Scene(setup, 300, 300);
		return introScene;
	}
	
	public Scene createWait() {
		wait = new BorderPane();
		display = new ListView<String>();
		wait.setCenter(display);
		Scene waitScreen = new Scene(wait, 300, 300);
		return waitScreen;
	}
	
	public Scene createMain() {
		main = new BorderPane();
		Scene mainScreen = new Scene(main, 400, 400);
		return mainScreen;
	}
}
