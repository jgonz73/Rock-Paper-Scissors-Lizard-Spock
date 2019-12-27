import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.function.Consumer;

public class Client extends Thread {
	Socket player;
	String ip;
	int port;
	ObjectOutputStream out;
	ObjectInputStream in;
	
	private Consumer<Serializable> callback;
	
	Client(int port, String ip, Consumer<Serializable> call) {
		this.ip = ip;
		this.port = port;
		this.callback = call;
	}
	
	public void run() {
		try {
			player = new Socket(ip, port);
		    out = new ObjectOutputStream(player.getOutputStream());
		    in = new ObjectInputStream(player.getInputStream());
		    player.setTcpNoDelay(true);
		}
		
		catch (Exception e) {
			callback.accept(new GameInfo(0, 0, "", "", 'w', 0, false));
		}
		
		while(true) {
			try {
				GameInfo g = (GameInfo) in.readObject();
				callback.accept(g);
			}
			catch(Exception e) {
			}
		}
	}
	
	public void send(GameInfo g) {
		try {
			out.writeObject(g);
			out.reset();
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
