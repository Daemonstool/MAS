package model;

import java.awt.GridLayout;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.view.Viewer;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import logic.Atom;
import logic.Formula;
import logic.Knows;

public class Model extends MultiGraph {
	
	private int worldCount;

	private ArrayList<String> messages = new ArrayList<>();

	public Model() {
		super("Arbitrary String #1");
		this.worldCount = 0;

		Socket socket;
		try {

			JTextField field1 = new JTextField("");
			JTextField field2 = new JTextField("");
			JPanel panel = new JPanel(new GridLayout(0, 1));
			panel.add(new JLabel("IP (default localhost):"));
			panel.add(field1);
			panel.add(new JLabel("port (default 3000):"));
			panel.add(field2);

			String ip = "localhost";
			String port = "3000";

			String[] buttons = { "Connect", "Exit" };

			int result = JOptionPane.showOptionDialog(null, panel, "Connect to game", JOptionPane.WARNING_MESSAGE, 0,
					null, buttons, buttons[0]);

			System.out.println(result);

			if (result == 0) {
				ip = (field1.getText().isEmpty()) ? "localhost" : field1.getText();
				port = (field2.getText().isEmpty()) ? "3000" : field2.getText();
			} else if (result == 1) {
				System.exit(0);
			}

			socket = IO.socket("http://" + ip + ":" + port);
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

		addNode(getWorldName());
		addNode(getWorldName());
		addNode(getWorldName());

		Iterator<Node> nodes = getNodeIterator();
		Random r = new Random();
		while (nodes.hasNext()) {
			Node n = nodes.next();
			if (r.nextBoolean())
				addAtom(n.getId(), "p");
			if (r.nextBoolean())
				addAtom(n.getId(), "q");
			n.setAttribute("ui.label", n.getAttribute("atoms").toString());
		}

		addEdge("w1w2", "w1", "w2");
		addEdge("w1w1", "w1", "w1");
		addEdge("w1w3", "w1", "w3");
		addEdge("w3w1", "w3", "w1");
		addEdge("w2w3", "w2", "w3");
		addAgent("w1w2", "Henk");
		addAgent("w1w2", "Joost");
		addAgent("w2w3", "Henry");

		addAgent("w1w3", "Up");
		addAgent("w3w1", "Down");
		addAgent("w1w1", "Henk");

		System.out.println(new Atom("p").evaluate(getNode("w1")));
		System.out.println(new Atom("p").evaluate(getNode("w2")));
		System.out.println(new Atom("p").evaluate(getNode("w3")));
		Formula f = new Knows(new Atom("p"), "Henk");
		System.out.println(f.evaluate(getNode("w1")));

		System.out.println();

		display();
	}
	
	private String getWorldName(){
		//generate the next world id
		return "w" + ++worldCount;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Node addNode(String id) {
		Node n = super.addNode(id);
		n.setAttribute("atoms", new ArrayList<String>());
		return n;
	}

	public void addAtom(String node, String atom) {
		Node n = getNode(node);
		ArrayList<String> atoms = n.getAttribute("atoms");
		atoms.add(atom);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Edge addEdge(String id, String idFrom, String idTo) {
		Edge e = super.addEdge(idFrom + idTo, idFrom, idTo, true);
		if (getEdge(idTo + idFrom) != null) {
			// symmetric relation, do some styling
			System.out.println(e.getId());
			e.setAttribute("ui.class", "symmetric");// tag only applies to one
													// side to separate the
													// labels
		}
		if (idFrom.equals(idTo)) {
			// reflexive relation, tag it
			e.setAttribute("ui.class", "reflexive");
		}
		e.setAttribute("agents", new ArrayList<String>());
		return e;
	}

	public void addAgent(String edge, String agent) {
		Edge e = getEdge(edge);
		ArrayList<String> agents = e.getAttribute("agents");
		agents.add(agent);
	}

	public ArrayList<String> getAtoms(String node) {
		return getNode(node).getAttribute("atoms");
	}

	@Override
	public Viewer display() {
		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		addAttribute("ui.antialias");
		addAttribute("ui.quality");// remove if real-time rendering becomes laggy

		String stylesheet;
		try {
			Scanner s = new Scanner(new File("graphstyle.css"));
			stylesheet = s.useDelimiter("\\Z").next();
			s.close();
		} catch (FileNotFoundException e1) {
			System.err.println("Stylesheet not found!");
			stylesheet = "";
			e1.printStackTrace();
		}
		addAttribute("ui.stylesheet", stylesheet);

		Iterator<Node> nodes = getNodeIterator();
		while (nodes.hasNext()) {
			Node n = nodes.next();
			n.setAttribute("ui.label", " " + n.getId() + ": " + n.getAttribute("atoms").toString());
		}
		Iterator<Edge> edges = getEdgeIterator();
		while (edges.hasNext()) {
			Edge e = edges.next();
			e.setAttribute("ui.label", e.getAttribute("agents").toString());
		}

		return super.display();
	}
	
	private void update(String type, ArrayList<String> args){
		switch(type){
			case "STF":
				
				break;
			case "BS":
				
				break;
			case "NS":
				
				break;
			case "DS":
				
				break;
			case "NP":
				
				break;
			case "SH":
				
				break;
			case "EK":
				
				break;
			case "ATT":
				
				break;
			case "FV":
				
				break;
			case "S1":
				addNode("w4");
				addAtom(getWorldName(),"c1");
				break;
			case "S3":
				
				break;
			case "AF":
				
				break;
			case "SP":
				
				break;
			case "DC":
				
				break;
			default:
				
				break;
		}
	}

	public static void main(String[] args) {
		new Model();
	}
}