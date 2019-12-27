import java.io.Serializable;

public class GameInfo implements Serializable {
	public int oppID;
	public int ID;
	public String myPlay;
	public String oppPlay;
	public boolean gameStart;
	// w -> waiting, c -> challenging, g -> in game, x -> mark for delete, f -> game finished
	public char status;
	public int winner;
	public boolean connected;

	
	GameInfo(int id, int oppID, String myPlay, String oppPlay, char status, int winner, boolean connected) {
		this.ID = id;
		this.oppID = oppID;
		this.myPlay = myPlay;
		this.oppPlay = oppPlay;
		this.status = status;
		this.winner = winner;
		this.connected = connected;
	}
}