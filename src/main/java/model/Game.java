package model;

import java.awt.GridLayout;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class Game {

	private Model model;
	private ArrayList<String> messages = new ArrayList<String>();
	
	public Game(){
		this.model = new Model();
		Socket socket;
		try {
			System.out.println("DING");
			JPanel panel = new JPanel(new GridLayout(0, 1));
			panel.add(new JLabel("You can play the game without hosting at: https://mas-ek.herokuapp.com/"));
			panel.add(new JLabel("When hosting yourself, make sure to use port 3000 on localhost."));

			String[] buttons = { "Connect to Heroku Server", "Connect Localhost (localhost:3000)", "Exit" };

			int result = JOptionPane.showOptionDialog(null, panel, "Connect to game", JOptionPane.WARNING_MESSAGE, 0,
					null, buttons, buttons[0]);

			if (result == 0) {
				socket = IO.socket("https://mas-ek.herokuapp.com/");
			} else if (result == 1) {
				socket = IO.socket("http://localhost:3000");
			} else {
				socket = IO.socket("https://mas-ek.herokuapp.com/");
				System.exit(0);
			}

			socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

				@Override
				public void call(Object... args) {
					socket.emit("connection", "hi");
					System.out.println("Connected");
				}

			}).on("message", new Emitter.Listener() {

				@Override
				public void call(Object... args) {
					String message = args[0].toString();
					messages.add(message);
					String[] substrings = message.split(" ");
					if(substrings.length > 0){
						String type = substrings[0];
						ArrayList<String> arguments = new ArrayList<String>(Arrays.asList(Arrays.copyOfRange(substrings,1,substrings.length)));
						System.out.println("New message of type " + type + " with arguments " + arguments.toString());
						update(type,arguments);
					}else{
						System.err.println("Invalid message: " + message);
					}
				}

			}).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

				@Override
				public void call(Object... args) {
					System.out.println("Disconnected");
				}

			});
			socket.connect();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		model.display();
	}
	
	
	private void update(String type, ArrayList<String> args){
		//handles an incoming message
		if(type.equals("STF")){
			STF(args,1);
			STF(args,2);
			STF(args,3);
		}else if(type.equals("INIT")){
			String agent = args.get(0);
			model.getAgents().add(agent);
			for(int w1=1;w1<=model.getWorldCount();++w1){
				for(int w2=1;w2<=model.getWorldCount();++w2){
					model.addRelation("w"+w1,"w"+w2,agent);
				}
			}
		}
	}
	
	private void STF(ArrayList<String> args, int card){
		String player = args.get(0);
		if(args.size() > card && args.get(card).equals("Explode")){
			Iterator<Node> nodes = model.iterator();
			while(nodes.hasNext()){
				Node n1 = nodes.next();
				ArrayList<String> atoms = n1.getAttribute("atoms");
				if(!atoms.contains("ek"+card)){
					//node contradicts the new information
					HashSet<String> toRemove = new HashSet<String>();
					Iterator<Edge> edges = n1.getEdgeIterator();
					while(edges.hasNext()){
						//search for edges that need to be removed
						Edge e = edges.next();
						toRemove.add(e.getId());
					}
					//actually remove the edges
					for(String e : toRemove){
						if(model.hasRelation(e,player)){
							model.removeRelation(e,player);
						}
					}
				}
			}
		}
	}
	
	public static void main(String[] args) {
		new Game();
	}
}
