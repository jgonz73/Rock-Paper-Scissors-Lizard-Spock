import java.util.HashMap;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

public class ClientGui extends Application {
	HashMap<Integer, GameInfo> players = new HashMap<Integer, GameInfo>();
	int myID;
	int oppID;
	
	boolean getID = false;
	boolean inGame = false;
	boolean gameFinished = false;
	
	Button start;
	Button play;
	Button quit;
	
	Button challBtn; 
	
	BorderPane setup;
	BorderPane main;
	BorderPane wait;
	
	VBox intro;
	VBox rWrap;
	VBox pWrap;
	VBox sWrap;
	VBox lWrap;
	VBox spWrap;
	VBox p1LastPlay;
	VBox p2LastPlay;
	VBox p1Wrap;
	VBox p2Wrap;
	VBox lobbyBox;
	VBox bottom;
	
	HBox challBox;
	HBox points;
	HBox images;
	
	Label p1;
	Label p2;
	Label lobby;
	
	Label inputPort;
	Label inputIp;
	Label r;
	Label p;
	Label s;
	Label l;
	Label sp;
	
	TextField port; 
	TextField ip; 
	TextField challField;
	
	ImageView p1Last;
	ImageView p2Last;
	ImageView rock;
	ImageView paper;
	ImageView scissors;
	ImageView lizard;
	ImageView spock;

	Client clientConnect;
	ListView<String> display; 
	ListView<String> server;
	ListView<String> serverMsg; 

	GameInfo currentInfo; 
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		launch(args);
	}

	//feel free to remove the starter code from this method
	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setTitle("Client");
		primaryStage.setScene(createSetup());
		primaryStage.show();
		
		port.setOnKeyPressed(e -> {;
			start.setDisable(false);
		});
		
		start.setOnAction(e-> {
			clientConnect = new Client(Integer.parseInt(port.getText()), ip.getText(), data -> {
				Platform.runLater(() -> {
					currentInfo = (GameInfo) data;
					
					// reads first gameinfo object, to record this clients player #
					if (!getID) {
						myID = currentInfo.ID;
						primaryStage.setTitle("Player " + myID);
						getID = true;
					}
					
					// if client tries to connect to the server while it isn't running
					if (currentInfo.ID == 0) {
						System.out.println("Server not running.");
						Platform.exit();
						System.exit(0);
					}
					
					// add each object to hash map of objects
					players.put(currentInfo.ID, (GameInfo) data);
				
					if (currentInfo.status == 'x') {
						players.remove(currentInfo.ID);
						updateDisplay();
					}
					
					if (!inGame) {
						if (currentInfo.status == 'c' && myID == currentInfo.oppID) {
							server.getItems().clear();
							server.getItems().add("Being challenged by Player " + currentInfo.ID);
							primaryStage.setScene(createMain(primaryStage));
							inGame = true;
						}
					}
					
					if ((!gameFinished) && (inGame)) {
						if ((myID == currentInfo.ID) && (!currentInfo.oppPlay.equals(""))) {
							ImageView choice = createImageView(currentInfo.oppPlay.toLowerCase() + ".jpg");
							p2Wrap.getChildren().remove(p2Last);
							p2Wrap.getChildren().add(choice);
							if (myID != currentInfo.winner) {
								if (currentInfo.winner == 0) {
									serverMsg.getItems().add("It was a tie.");
									serverMsg.setDisable(true);
									quit.setVisible(true);
									gameFinished = true;

								}
								else {
									serverMsg.getItems().add("Player " + currentInfo.winner + " won. You lose.");
									serverMsg.setDisable(true);
									quit.setVisible(true);
									gameFinished = true;
								}
							}
							else  {
								serverMsg.getItems().add("Congratulations! You win!");
								serverMsg.setDisable(true);
								quit.setVisible(true);
								gameFinished = true;
							}

						}
					}
					// clears and updates lobby with each new gameinfo
					updateDisplay();

				});
			});
			primaryStage.setScene(createLobby(primaryStage));
			primaryStage.setTitle("Player " + myID);
			clientConnect.start();
			
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
		start = new Button("Start");
		start.setDisable(true);
		
		inputPort = new Label("Input Port: ");
		port = new TextField();
		port.setMaxWidth(100);
		
		inputIp = new Label("Input IP: ");
		ip = new TextField();
		ip.setMaxWidth(100);
		
		intro = new VBox();
		intro.getChildren().addAll(inputPort, port, inputIp, ip, start);
		intro.setAlignment(Pos.CENTER);
		intro.setSpacing(20);
		
		setup = new BorderPane();
		setup.setCenter(intro);
		Scene introScene = new Scene(setup, 300, 300);
		return introScene;
	}
	
	public Scene createLobby(Stage primaryStage) {
		challField = new TextField("Enter # of player:");
		challBtn = new Button("Challenge");
		challBtn.setDisable(true);
		
		challBox = new HBox();
		challBox.setPadding(new Insets(20, 0, 0, 0));
		challBox.getChildren().addAll(challField, challBtn);
		challBox.setAlignment(Pos.CENTER);
		challBox.setSpacing(40);
		
		challField.setOnMouseClicked(event -> {
			challField.clear();
			challBtn.setDisable(false);
		});
		
		display = new ListView<String>();

		server = new ListView<String>();
		server.setPrefHeight(30);
		server.setPrefWidth(250);

		challBtn.setOnAction(event -> {
			players.forEach((k, v) -> {
				Integer id = Integer.parseInt(challField.getText());
				if (k == id) {
					if (myID == k) {
						server.getItems().clear();
						server.getItems().add("Can't challenge yourself!");
					}
					else if ((v.status == 'c') || (v.status == 'g')) {
						server.getItems().clear();
						server.getItems().add("Invalid - player doesn't exist or already in game.");
						server.getItems().add("I am! player " + myID);

					}
					else {
						server.getItems().clear();
						server.getItems().add("Challenging Player " + (id));
						oppID = id;
						players.get(myID).oppID = id;
						players.get(myID).status = 'c';
						clientConnect.send(players.get(myID));
						primaryStage.setScene(createMain(primaryStage));
					}
				}
				else {
					server.getItems().clear();
					server.getItems().add("Invalid - player doesn't exist or already in game.");

				}
			});
			challField.clear();

		});
		
		display.setStyle("-fx-border-color: black");
		
		lobby = new Label("Connected Players");
		lobbyBox = new VBox();
		lobbyBox.getChildren().add(lobby);
		lobbyBox.setAlignment(Pos.CENTER);
		lobbyBox.setPadding(new Insets(0, 0, 10, 0));
		
		bottom = new VBox();
		bottom.getChildren().addAll(server, challBox);
		bottom.setPadding(new Insets(10, 0, 0, 0));

		
		wait = new BorderPane();
		wait.setPadding(new Insets(10, 20, 20, 20));
		wait.setTop(lobbyBox);
		wait.setCenter(display);
		wait.setBottom(bottom);
		
		Scene waitScreen = new Scene(wait, 400, 300);
		return waitScreen;
	}
	
	public Scene createMain(Stage primaryStage) {
		main = new BorderPane();
		images = new HBox();
		rock = createImageView("rock.jpg");
		paper = createImageView("paper.jpg");
		scissors = createImageView("scissors.jpg");
		lizard = createImageView("lizard.jpg");
		spock = createImageView("spock.jpg");
		rWrap = borderToImage(rWrap, rock, "Rock", r);
		pWrap = borderToImage(pWrap, paper, "Paper", p);
		sWrap = borderToImage(sWrap, scissors,"Scissors", s);
		lWrap = borderToImage(lWrap, lizard,"Lizard", l);
		spWrap = borderToImage(spWrap, spock, "Spock", sp);

		images.getChildren().addAll(rWrap, pWrap, sWrap, lWrap, spWrap);
		
		quit = new Button("End Game");
		quit.setVisible(false);
		
		quit.setOnMouseClicked(event -> {
			GameInfo temp = new GameInfo(myID, 0, "", "", 'w', 0, true);
			players.put(myID, temp);
			primaryStage.setScene(createLobby(primaryStage));
			clientConnect.send(temp);
			inGame = false;
			gameFinished = false;

		});
				
		p1 = new Label("    My Play");
		p1.setMaxWidth(86);

		p2 = new Label("    Opp. Play");
		p2.setMaxWidth(86);
		
		p1Last = new ImageView();
		p1Last.setFitHeight(84);
		p1Last.setFitWidth(84);
		p1Last.setSmooth(true);
		p1Last.setCache(true);
		
		p2Last = new ImageView();
		p2Last.setFitHeight(84);
		p2Last.setFitWidth(84);
		p2Last.setSmooth(true);
		p2Last.setCache(true);
		
		p1Wrap = new VBox(p1Last);
		p1Wrap.setStyle("-fx-border-color: black");
		p2Wrap = new VBox(p2Last);
		p2Wrap.setStyle("-fx-border-color: black");

		p1LastPlay = new VBox(p1, p1Wrap);
		p1LastPlay.setAlignment(Pos.CENTER);
		
		p2LastPlay = new VBox(p2, p2Wrap);
		p2LastPlay.setAlignment(Pos.CENTER);

		points = new HBox(p1LastPlay, quit, p2LastPlay);
		points.setAlignment(Pos.CENTER);
		points.setSpacing(50);
		points.setPadding(new Insets(15, 10, 15, 10));
		
		serverMsg = new ListView<String>();
		
		main.setBottom(images);
		main.setTop(points);
		main.setCenter(serverMsg);
		
		Scene mainScreen = new Scene(main, 426, 426);
		return mainScreen;
	}
	
	public ImageView createImageView(String url) {
		ImageView temp = new ImageView(url);
		temp.setPickOnBounds(true);
		temp.setFitHeight(84);
		temp.setFitWidth(84);
		temp.setSmooth(true);
		temp.setCache(true);
		return temp;
	}
	
	public VBox borderToImage(VBox tmp, ImageView temp, String text, Label l) {
		temp.setOnMouseClicked((MouseEvent g) -> {
			rock.setDisable(true);
			paper.setDisable(true);
			scissors.setDisable(true);
			lizard.setDisable(true);
			spock.setDisable(true);

			ImageView choice = createImageView(text.toLowerCase() + ".jpg");
			serverMsg.getItems().add("You played " + text + ".");
			PauseTransition pause = new PauseTransition(Duration.seconds(1));
			pause.setOnFinished(event -> {
				p1Wrap.getChildren().remove(p1Last);
				p1Wrap.getChildren().add(choice);
				serverMsg.getItems().add("Waiting on opponent move.");
			});
			pause.play();
			
			players.get(myID).myPlay = text;
			players.get(myID).status = 'g';
			clientConnect.send(players.get(myID));
		});
		l = new Label(text);
		tmp = new VBox(l, temp);
		tmp.setAlignment(Pos.CENTER);
		tmp.setStyle("-fx-border-color: black");
		return tmp;
	}
	
	void updateDisplay() {
		display.getItems().clear();
		players.forEach((k, v) -> {
			if (v.status == 'c') {
				display.getItems().add("Player " + k + " (in-game)");
			}
			else if (v.status == 'g') {
				display.getItems().add("Player " + k + " (in-game)");
			}
			else
				display.getItems().add("Player " + k);
		});
	}
}
