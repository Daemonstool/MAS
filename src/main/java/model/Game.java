package model;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import logic.Atom;
import logic.CommonKnowledge;

class Game {

	private Model model;
	private ArrayList<String> messages = new ArrayList<String>();
	private boolean isInitialised;
	private Socket socket;
	
	private DefaultListModel<String> dlm = new DefaultListModel<>();
	
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
			for (int idx = 1; idx != 4; ++idx)
				if ( STF(args,idx))
					ek = true;

			if (!ek){
				//only w8 is true
				Iterator<Node> nodes = model.getNodeIterator();
				while (nodes.hasNext()){
					Node n1 = nodes.next();
					ArrayList<String> atomArray = n1.getAttribute("atoms");
					if (atomArray.isEmpty())
						model.removeRelationsForAgentsExcept(args.get(0), n1);
					
				}
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
			interConnectAll(); //Throw away all knowledge.
		}

		if (type.equals("SS"))
		{
			model.setCardsleft(Integer.valueOf(args.get(0)));
			switch (model.getCardsleft())
			{
			case 1: 
				CommonKnowledge c1 = new CommonKnowledge(new Atom("c1"));
				model.getCommonKnowledge().add(c1);
				model.addToAllNodes("c1");
				model.getDLM().addElement(c1.pprint());
				
				model.setWorldCount(1);
				removeInconsistentWorlds();
				this.interConnectAll();
				break;
			case 2:
				CommonKnowledge c2 = new CommonKnowledge(new Atom("c2"));
				model.getCommonKnowledge().add(c2);
				model.addToAllNodes("c2");
				model.getDLM().addElement(c2.pprint());
				
				
				model.setWorldCount(2);
				removeInconsistentWorlds();
				break;
			case 3: 
				CommonKnowledge c3 = new CommonKnowledge(new Atom("c3"));
				model.getCommonKnowledge().add(c3);
				model.addToAllNodes("c3");
				model.getDLM().addElement(c3.pprint());
				
				model.setWorldCount(3);
				removeInconsistentWorlds();
				break;
			}
			
		}

		// We dont have to check for nopes since the message is not sent anyway when someone card(set) is noped.
		// Hence it is not processed in the model, therefore there is no gain of knowledge.
		if(type.equals("NP"))
		{
			// Nothing ... until we keep track of nope cards in kripke worlds.
		}

		if (type.equals("EK") || type.equals("DF"))
		{
			if (model.getAgents().size() == 2)
			{
				System.out.println("IC_ALL");
				this.interConnectAll();
			}
		}
		updateLabels();
	}

	private boolean STF(ArrayList<String> args, int card)
	{
		String player = args.get(0);
		boolean EK = false;
		boolean proceed = false;
		// EK in STF
		if (args.size() > card && args.get(card).equals("Explode"))
		{
			Iterator<Node> nodes = model.getNodeIterator();
			EK = true;
			while (nodes.hasNext())
			{
				// get each node
				Node n1 = nodes.next();
				ArrayList<String> atoms = n1.getAttribute("atoms");
				try
				{
					proceed = !atoms.contains("ek" + card);
				}
				catch (Exception e){
					proceed = false; // array empty, dont go in next if-statement.
				}
				if (proceed && model.isConsistent(n1)) 
				{
					//node contradicts the new information
					HashSet<String> toRemove = new HashSet<String>();
					Iterator<Edge> edges = n1.getEdgeIterator();
					while (edges.hasNext())
					{
						//search for edges that need to be removed
						Edge e = edges.next();
						toRemove.add(e.getId());
					}
					//actually remove the edges
					for (String e : toRemove)
						if (model.hasRelation(e,player))
							model.removeRelation(e, player);
				} 
				// update knowledge
				if (!proceed && model.isConsistent(n1) && !model.hasRelation(n1.getId(),  n1.getId(), player))
					model.addRelation(n1.getId(),  n1.getId(), player);
			}
		}
		return EK;
	}

	//getEmptyWorld
	private Node getEmptyWorld()
	{
		//initially this is w8, set this to keep it initialized.
		Node emptyNode = model.getNode("w8");
		Iterator<Node> nodes = model.getNodeIterator();
		// use this instead of hard coding for w8.
		while (nodes.hasNext()){
			Node n1 = nodes.next();
			ArrayList<String> atomArray = n1.getAttribute("atoms");
			if (atomArray.isEmpty())
				emptyNode = n1;
		}
		
		if(!model.isConsistent(emptyNode))
		{
			nodes = model.getNodeIterator();
			// use this instead of hard coding for w8.
			ArrayList<Node> compareNodes = new ArrayList<Node>();
			ArrayList<Integer> compareInts = new ArrayList<Integer>();
			int tmp = 0;
			while (nodes.hasNext()){
				Node n1 = nodes.next();
				if(model.isConsistent(n1))
				{
					ArrayList<String> atomArray = n1.getAttribute("atoms");
					for(String atom : atomArray)
					{
						try
						{
							compareInts.add(Character.getNumericValue(atom.charAt(2)));
							compareNodes.add(n1);
						}
						catch (Exception e) //c1 c2 c3
						{
							continue;
						}
					}
				}
			}
			tmp = Collections.max(compareInts);
			for(int i = 0; i < compareInts.size(); ++i){
				if (compareInts.get(i) == tmp)
					emptyNode = compareNodes.get(i);
			}
			
		}
		return emptyNode;
	}
	
	private void drawCard(ArrayList<String> args)
	{
		// Shift knowledge about EK to next world for each agent.
		ArrayList<Node> shiftNodes = new ArrayList<Node>();
		ArrayList<String> shiftAgents = new ArrayList<String>();
		ArrayList<String> extendAgents = new ArrayList<String>();
		//ArrayList<Integer> extendInt = new ArrayList<Integer>();
		
		//initially this is w8, set this to keep it initialized.
		Node compareWith = getEmptyWorld();

		Iterator<Node> nodes = model.getNodeIterator();
		for (String a : model.getAgents()) {
			nodes = model.getNodeIterator();
			while (nodes.hasNext()){
				Node n1 = nodes.next();
				//This is should not done for worlds where ek1 is true or all not EK (w8)
				//SS should handle these.
				ArrayList<String> atoms = n1.getAttribute("atoms");
				//Check for each node if it only has a relation to itself for the agent: is reflexive
				if(model.canAccessWorlds(a, n1) == 1 && (!n1.getId().equals("w8") && !atoms.contains("ek1"))){
					//actually shift worlds for EK
					shiftNodes.add(n1);
					shiftAgents.add(a);
				}
				// only do this for the world with no knowledge (w8) and the relations are not fully connected (and consistent).
				if (n1.equals(compareWith) && model.canAccessWorlds(a, n1) != model.getMaxConsistentWorlds())
					extendAgents.add(a);
			}
		}

		for(int i = 0; i < shiftNodes.size(); ++i)
			shiftWorldsForEK(shiftAgents.get(i), shiftNodes.get(i));
		
		for(int i = 0; i < extendAgents.size(); ++i)
			extendUncertainty(extendAgents.get(i));
	}

	//called for each agent, sets up initial relations.
	private void INIT(ArrayList<String> args){
		String agent = args.get(0);
		model.getAgents().add(agent);
		for (int w1 = 1; w1 <= model.getWorldCount(); ++w1){
			if (model.isConsistent(model.getNode("w"+w1)))
				for(int w2 = 1; w2<=model.getWorldCount(); ++w2){
					if(model.isConsistent(model.getNode("w" + w2)))
						model.addRelation("w" + w1, "w" + w2, agent);
				}
		}			
	}

	//also check inconsistency.
	// connects all consistent worlds.
	private void interConnectAll() {
		Iterator<Node> nodes = model.iterator();
		HashSet<Node> toAddNodes = new HashSet<Node>();
		while(nodes.hasNext()){
			Node n = nodes.next();
			if(model.isConsistent(n)){
				toAddNodes.add(n);
			}
		}
		
		for(Node n : toAddNodes){
			HashSet<String> toRemoveEdges = new HashSet<String>();
			Iterator<Edge> edges = n.getEdgeIterator();
			while (edges.hasNext())
			{
				//search for edges that need to be removed
				Edge e = edges.next();
				toRemoveEdges.add(e.getId());
			}
			//actually remove the edges
			for (String e : toRemoveEdges)
				for (String player : model.getAgents())
					if (!model.hasRelation(e,player))
						model.addRelation(e, player);
		}
	}
	
	// also check inconsistency.
	// connects all consistent worlds.
	private void removeInconsistentWorlds() {
		Iterator<Node> nodes = model.iterator();
		HashSet<Node> toRemoveNodes = new HashSet<Node>();
		while(nodes.hasNext()){
			Node n = nodes.next();
			if(!model.isConsistent(n)){
				toRemoveNodes.add(n);
			}
		}
		
		for(Node n : toRemoveNodes){
			HashSet<String> toRemoveEdges = new HashSet<String>();
			Iterator<Edge> edges = n.getEdgeIterator();
			while (edges.hasNext())
			{
				//search for edges that need to be removed
				Edge e = edges.next();
				toRemoveEdges.add(e.getId());
			}
			//actually remove the edges
			for (String e : toRemoveEdges)
				for (String player : model.getAgents())
					if (model.hasRelation(e,player))
						model.removeRelation(e, player);
		}
			
	}

	// after a drawcard a world that only has a relation to it self
	// shifts to a world with one EK higher, e.g.: w(not EK1, not EK2, EK3) goes to w(not EK1, EK2, not EK3)
	private void shiftWorldsForEK(String agent, Node n) {
		String nodeName = n.getId();
		// remove current relation
		model.removeRelation(nodeName, nodeName, agent);
		
		//determine next true world(s)
		Iterator<Node> nodes = model.getNodeIterator();
		ArrayList<Node> shiftToNodes = new ArrayList<Node>();
		int atomInt = 0;
		while (nodes.hasNext()){
			Node next = nodes.next();
			if (!n.equals(next) && model.isConsistent(next)){
				ArrayList<String> atoms = n.getAttribute("atoms");
				ArrayList<String> nextAtoms = next.getAttribute("atoms");
				for (int i = 0; i < atoms.size(); ++i){
					try
					{
						atomInt = Character.getNumericValue(atoms.get(i).charAt(2));
					}
					catch (Exception e) //if c1 ... c3 is set.
					{
						continue;
					}
					for (int j = 0; j < nextAtoms.size(); ++j)
					{
						// if a world contains ek such that it is one lower than the current.
						try{
							if (atomInt - 1 == Character.getNumericValue(nextAtoms.get(j).charAt(2)))
							shiftToNodes.add(next);
						}
						catch (Exception e) // c1 ... c3.
						{
							continue;
						}
							
					}
				}
				
			}
		}
		
		for(Node addNode : shiftToNodes)
			if (!model.hasRelation(addNode.getId(), addNode.getId(), agent))
				model.addRelation(addNode.getId(), addNode.getId(), agent);
	}

	//Interconnects relations of all consistent worlds such that (nConnected + 1) are connected in this step
	//Assumed is that there is at least relation to w8 but not fully connected.
	private void extendUncertainty(String agent){
		Iterator<Node> it = model.getNodeIterator();
		Node emptyNode = getEmptyWorld();
		ArrayList<String> atomArray = new ArrayList<String>();
		int lowestEK = 0;
		
		// find next world and add relations to it, this is done in 3 steps
		
		// 1. Do this by determining the highest connected ekX value 
		int tmp = 0;
		while (it.hasNext())
		{
			Node n = it.next();
			if (model.hasRelation(emptyNode.getId(), n.getId(), agent) && model.isConsistent(n))
			{
				atomArray = n.getAttribute("atoms");
				for (int i = 0; i < atomArray.size(); ++i)
				{
					try
					{
						tmp = Character.getNumericValue(atomArray.get(i).charAt(2));
					}
					catch (Exception e) //c1 c2 c3 ...
					{
						continue;
					}
					if(tmp < lowestEK  || lowestEK == 0)
						lowestEK = tmp;
				}
			}
		}
		// 2. determine the world with one EK higher.
		Node addNode = getEmptyWorld();
		it = model.getNodeIterator();

		while (it.hasNext())
		{
			Node n = it.next();
			if (model.isConsistent(n) && !model.hasRelation(emptyNode.getId(), n.getId(), agent) && !n.equals(emptyNode))
			{
				atomArray = n.getAttribute("atoms");
				for (int i = 0; i < atomArray.size(); ++i)
				{
					try
					{
						tmp = Character.getNumericValue(atomArray.get(i).charAt(2));
					}
					catch (Exception e) //c1 or c2 or c3
					{
						continue;
					}
					// found the world!
					if (lowestEK == 0 && tmp == 3)
						addNode = n;
					else if (lowestEK != 0 && tmp == lowestEK - 1)
						addNode = n;
				}
			}
		}
		
		// interconnect it with the other worlds
		it = model.getNodeIterator();
		while (it.hasNext())
		{
			Node n = it.next();
			if (model.isConsistent(n) && model.hasRelation(n.getId(), emptyNode.getId(), agent))
			{
				if (!model.hasRelation(addNode.getId(), n.getId(), agent))
					model.addRelation(addNode.getId(), n.getId(), agent);
				
				if (!model.hasRelation(n.getId(), addNode.getId(), agent))
					model.addRelation(n.getId(), addNode.getId(), agent);
					
				if (!model.hasRelation(addNode.getId(), addNode.getId(), agent))
					model.addRelation(addNode.getId(), addNode.getId(), agent);
			}
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

	public void addCommonKnowledge(CommonKnowledge c)
	{
		dlm.addElement(c.pprint());
	}
	
	public void removeCommonKnowledge(CommonKnowledge c)
	{
		dlm.removeElement(c.pprint());
	}
}

