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
	private boolean isInitialised;
	private Socket socket;

	public Game(){
		this.isInitialised = false;
		this.model = new Model();
		try {

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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		model.display();
	}


	private void update(String type, ArrayList<String> args){
		if(type.equals("STF")){
			boolean ek = false;
			for (int idx = 0; idx != 3; ++idx)
				if ( STF(args,idx))
					ek = true;

			if (!ek){
				//only w4 is true
				model.removeRelationsForAgentsExcept(args.get(0), model.getNode("w4"));
			}
		}

		if(type.equals("INIT")){
			if(this.isInitialised){
				this.isInitialised = false;
				this.model = new Model();

				Game g = new Game();
				g.INIT(args);
			}else{
				INIT(args);
			}
		}

		if(type.equals("INITDONE")){
			this.isInitialised = true;
		}

		if(type.equals("DC")){
			drawCard(args);
		}

		if(type.equals("SH")){
			SH(); //Throw away all knowledge.
		}

		if (type.equals("SS"))
		{
			model.setCardsleft(Integer.valueOf(args.get(0)));
			//System.out.println(cardsleft + " = cardsleft");
			switch (model.getCardsleft())
			{
			case 1: 
				try 
				{ 
					model.removeNode("w2");
				} 
				catch (Exception e)
				{
					System.err.println("Already removed");
				}
				model.setWorldCount(1);
				this.interConnectAll();
				break;
			case 2:
				try 
				{ 
					model.removeNode("w3");
				} 
				catch (Exception e)
				{
					System.err.println("Already removed");
				}
				model.setWorldCount(2);
				break;
			case 3: 
				try 
				{ 
					model.removeNode("w4");
				} 
				catch (Exception e)
				{
					System.err.println("Already removed");
				}
				model.setWorldCount(3);
				break;
			}
		}

		// We dont have to check for nopes since the message is not sent anyway when someone card(set) is noped.
		// Hence it is not processed in the model, therefore there is no gain of knowledge.
		if(type.equals("NP")){
			// Nothing ... until we keep track of nope cards in kripke worlds.
		}

		if (type.equals("EK"))
		{
			if (model.getAgents().size() == 2)
			{
				System.out.println("Game Over");
			}
		}

		if(type.equals("DF"))
		{

		}
		updateLabels();

	}

	private boolean STF(ArrayList<String> args, int card){
		String player = args.get(0);
		boolean EK = false;
		if(args.size() > card && args.get(card).equals("Explode")){
			Iterator<Node> nodes = model.getNodeIterator();
			EK = true;
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
							model.removeRelation(e, player);
						}
					}
				}
			}
		}
		return EK;
	}

	private void drawCard(ArrayList<String> args){
		// TODO: Update knowledge about hands
		// String player = args.get(0);
		// String card = args.get(1);

		// Shift knowledge about EK to next world for each agent.
		ArrayList<Node> shiftNodes = new ArrayList<Node>();
		ArrayList<String> shiftAgents = new ArrayList<String>();

		ArrayList<String> extendAgents = new ArrayList<String>();
		ArrayList<Integer> extendSize = new ArrayList<>();

		String compareWith = "w" + model.getWorldCount();

		for (String a : model.getAgents()) {
			Iterator<Node> nodes = model.getNodeIterator();


			while (nodes.hasNext()){
				//Check for each node if it only has a relation to itself for the agent: is reflexive
				Node n1 = nodes.next();

				//This is not true for w4, and w1 gets already processed by SS.
				if(model.canAccessWorlds(a, n1) == 1 && (!n1.getId().equals("w4") && !n1.getId().equals("w1"))){
					//actually shift worlds for EK
					shiftNodes.add(n1);
					shiftAgents.add(a);
				}


				for (int i = (model.getWorldCount() - 1) ; i >= 1; --i){
					if (model.canAccessWorlds(a, n1) == i && (n1.getId().equals(compareWith))){
						extendAgents.add(a);
						extendSize.add(i);
					}
				}

			}
		}

		for(int i = 0; i < shiftNodes.size(); ++i)
			shiftWorldsForEK(shiftAgents.get(i), shiftNodes.get(i));

		for(int i = 0; i < extendSize.size(); ++i)
			extendUncertainty(extendAgents.get(i), extendSize.get(i));
	}

	private void INIT(ArrayList<String> args){
		String player = args.get(0);
		model.getAgents().add(player);
		String agent = args.get(0);
		model.getAgents().add(agent);
		for(int w1=1;w1<=model.getWorldCount();++w1){
			if(model.isConsistent(model.getNode("w"+w1))){
				for(int w2=1;w2<=model.getWorldCount();++w2){
					if(model.isConsistent(model.getNode("w"+w2))){
						model.addRelation("w"+w1,"w"+w2,agent);
					}
				}
			}
		}
	}


	private void interConnectAll(){
		for(int w1 = 1; w1 <= model.getWorldCount(); ++w1){
			for(int w2 = 1; w2 <= model.getWorldCount(); ++w2){
				for(String a : model.getAgents()){
					if (!model.hasRelation("w" + w1, "w"+ w2, a)) {
						model.addRelation("w" + w1, "w" + w2, a);
					}
				}
			}
		}
	}


	private void SH(){
		interConnectAll();
	}	


	private void shiftWorldsForEK(String agent, Node n){
		String nodeName = n.getId();
		model.removeRelation(nodeName, nodeName, agent);
		nodeName = "w" + (Integer.parseInt(nodeName.substring(1,nodeName.length())) - 1);
		model.addRelation(nodeName, nodeName, agent);
	}

	//Interconnects relations of w4 up to w (nConnected + 1)
	private void extendUncertainty(String agent, int nConnected){
		for (int w1 = model.getWorldCount(); w1 >= (model.getWorldCount() - nConnected); --w1)
			for (int w2 = model.getWorldCount(); w2 >= (model.getWorldCount() - nConnected); --w2)
				if (!model.hasRelation("w" + w1, "w" + w2, agent)){
					model.addRelation("w" + w1, "w" + w2, agent);
					//System.out.println("Adding relation" + "w"+w1+"w"+w2 + "for agent " + agent);
				}
	}

	public void updateLabels() 
	{
		Iterator<Node> it = model.getNodeIterator();

		while (it.hasNext())
		{
			Node n = it.next();
			model.buttonPushed(n.getId());
			model.buttonPushed(n.getId());
		}

	}

	public static void main(String[] args) {
		new Game();
	}
}
