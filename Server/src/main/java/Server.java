import java.net.ServerSocket;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.control.ListView;
import javafx.util.Duration;

public class Server {
	theServer server;
	int port; 
	int count = 1;
	int disconnectOffset = 0;
	private Consumer<Serializable> callback;
	ArrayList<ClientThread> lobby = new ArrayList<ClientThread>();
	HashMap<Integer, GameInfo> players = new HashMap<Integer, GameInfo>();
	HashMap<Integer, ClientThread> playerThread = new HashMap<Integer, ClientThread>();
	
	
	Server(int a, Consumer<Serializable> call) {
		callback = call;
		server = new theServer();
		port = a;
		server.start();
	}
	
	public class theServer extends Thread {	
		public void run() {
			try(ServerSocket mysocket = new ServerSocket(port);) {
//				callback.accept("Server started on port " + port + ".");
		    	while(true) {
					ClientThread c = new ClientThread(mysocket.accept(), count);
					lobby.add(c);
					c.start();	
					count++;
		    	}
			}
			catch(Exception e) {
				callback.accept("Server did not launch.");
			}
		}
	}
	
	class ClientThread extends Thread {
		Socket player;
		int whichPlayer; 
		ObjectInputStream in;
		ObjectOutputStream out;
		
		ClientThread(Socket s, int count) {
			this.player = s;
			this.whichPlayer = count + disconnectOffset;
			playerThread.put(whichPlayer, this);
		}
		
		public void updateLobby() {
			for (int i = 0; i < lobby.size(); i++) {
				ClientThread t = lobby.get(i);
				players.forEach((k, v) -> {
					try { 
						t.out.writeObject(v);
						t.out.reset();
					}
					catch(Exception e) {	
						System.out.println("Stream not open.");
					}
				});
			}
		}
		
		public void updateAllClients(GameInfo g) {
			for (int i = 0; i < lobby.size(); i++) {
				ClientThread t = lobby.get(i);
				try {
					t.out.writeObject(g);	
					t.out.reset();
				}
				catch(Exception e) {
					System.out.println("Stream not open.");
				}
			}
		}
		
		
		/*synchronized*/public void sendChallenge(GameInfo g, int opp) {
				ClientThread t = playerThread.get(opp);
				try {
					t.out.writeObject(g);	
					t.out.reset();
				}
				catch(Exception e) {
					System.out.println("Stream not open.");
				}
		}
		
		public void run() {
			try {
				in = new ObjectInputStream(player.getInputStream());
				out = new ObjectOutputStream(player.getOutputStream());
				player.setTcpNoDelay(true);	
			}
			catch(Exception e) {
				System.out.println("Stream not open.");
			}
		
			GameInfo g = new GameInfo(whichPlayer, 0, "", "", 'w', 0, false);
			callback.accept(g);
			players.put(whichPlayer, g);
			updateAllClients(g);
			updateLobby();
		
			while(true) {
				try {
					GameInfo m = (GameInfo) in.readObject();
					players.put(m.ID, m);
					callback.accept(m);
					if (m.status == 'w') {
						updateLobby();
					}
					
					else if (m.status == 'c') {
						sendChallenge(m, m.oppID);
						GameInfo temp = new GameInfo(m.oppID, m.ID, "", "", 'c', 0, true);
						players.put(temp.ID, temp);
						updateLobby();
					}
					
					else if (m.status == 'g') {					
						if (players.get(m.oppID).status == 'g') {
							players.get(m.oppID).oppPlay = m.myPlay;
							m.oppPlay = players.get(m.oppID).myPlay;
							int winner = evaluateWinner(m.oppID, m.ID, players.get(m.oppID).myPlay, players.get(m.ID).myPlay);
							players.get(m.oppID).winner = winner;
							m.winner = winner;
							PauseTransition pause = new PauseTransition(Duration.seconds(1));
							pause.setOnFinished(event -> {
								updateLobby();
							});
							pause.play();
						}
					}
				}	
				
				catch(Exception e) {
			    	lobby.remove(this);
			    	count--;
			    	disconnectOffset++;
			    	players.get(whichPlayer).status = 'x';
			    	updateAllClients(players.get(whichPlayer));
			    	callback.accept(players.get(whichPlayer));
				  	break;   
				}
			}
		}
	}

	int evaluateWinner(int opp, int me, String oppPlay, String myPlay) {
		// tie
		if (myPlay.equals(oppPlay))
			return 0;
		
		if (myPlay.equals("Paper") && (oppPlay.equals("Rock") || oppPlay.equals("Spock")))
			 return me;
		else if (myPlay.equals("Rock") && (oppPlay.equals("Lizard") || oppPlay.equals("Scissors")))
			 return me;
		else if (myPlay.equals("Spock") && (oppPlay.equals("Scissors") || oppPlay.equals("Rock")))
			 return me;
		else if (myPlay.equals("Scissors") && (oppPlay.equals("Paper") || oppPlay.equals("Lizard")))
			 return me;
		else if (myPlay.equals("Lizard") && (oppPlay.equals("Spock") || oppPlay.equals("Paper")))
			 return me;
		else
			return opp;
	}
}
