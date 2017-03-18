package model;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.view.Viewer;

import logic.Atom;
import logic.Formula;
import logic.Knows;

public class Model extends MultiGraph{
	
	public Model() {
		super("Arbitrary String #1");
		
		
       
		addNode("w1");
		addNode("w2");
		addNode("w3");
		
		Iterator<Node> nodes = getNodeIterator();
		Random r = new Random();
		while (nodes.hasNext()){
			Node n = nodes.next();
			if(r.nextBoolean())
				addAtom(n.getId(),"p");
			if(r.nextBoolean())
				addAtom(n.getId(),"q");
			n.setAttribute("ui.label", n.getAttribute("atoms").toString());
		}
		
		addEdge("w1w2","w1","w2");
		addEdge("w1w1","w1","w1");
		addEdge("w1w3","w1","w3");
		addEdge("w3w1","w3","w1");
		addEdge("w2w3","w2","w3");
		addAgent("w1w2","Henk");
		addAgent("w1w2","Joost");
		addAgent("w2w3","Henry");
		
		addAgent("w1w3","Up");
		addAgent("w3w1","Down");
		addAgent("w1w1", "Henk");
		
		System.out.println(new Atom("p").evaluate(getNode("w1")));
		System.out.println(new Atom("p").evaluate(getNode("w2")));
		System.out.println(new Atom("p").evaluate(getNode("w3")));
		Formula f = new Knows(new Atom("p"),"Henk");
		System.out.println(f.evaluate(getNode("w1")));
		
		System.out.println();
		
		display();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Node addNode(String id){
		Node n = super.addNode(id);
		n.setAttribute("atoms", new ArrayList<String>());
		return n;
	}
	
	public void addAtom(String node, String atom){
		Node n = getNode(node);
		ArrayList<String> atoms = n.getAttribute("atoms");
		atoms.add(atom);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Edge addEdge(String id, String idFrom, String idTo){
		Edge e = super.addEdge(idFrom+idTo, idFrom, idTo,true);
		if(getEdge(idTo+idFrom) != null){
			//symmetric relation, do some styling
			System.out.println(e.getId());
			e.setAttribute("ui.class", "symmetric");//tag only applies to one side to separate the labels
		}
		if(idFrom.equals(idTo)){
			//reflexive relation, tag it
			e.setAttribute("ui.class", "reflexive");
		}
		e.setAttribute("agents", new ArrayList<String>());
		return e;
	}
	
	public void addAgent(String edge, String agent){
		Edge e = getEdge(edge);
		ArrayList<String> agents = e.getAttribute("agents");
		agents.add(agent);
	}
	
	public ArrayList<String> getAtoms(String node){
		return getNode(node).getAttribute("atoms");
	}
	
	@Override
	public Viewer display(){
		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		addAttribute("ui.antialias");
		addAttribute("ui.quality");//remove if real-time rendering becomes laggy
		
		String stylesheet;
		try {
			Scanner s = new Scanner(new File("graphstyle.css"));
			stylesheet = s.useDelimiter("\\Z").next();
			s.close();
		} catch (FileNotFoundException e1) {
			System.err.println("Stylesheet not found!");
			stylesheet =  "";
			e1.printStackTrace();
		}
		addAttribute("ui.stylesheet",stylesheet);
		
		Iterator<Node> nodes = getNodeIterator();
		while(nodes.hasNext()){
			Node n = nodes.next();
			n.setAttribute("ui.label", " " + n.getId() + ": " + n.getAttribute("atoms").toString());
		}
		Iterator<Edge> edges = getEdgeIterator();
		while(edges.hasNext()){
			Edge e = edges.next();
			e.setAttribute("ui.label", e.getAttribute("agents").toString());
		}
		
		return super.display();
	}
	
	

	public static void main(String[] args){
		new Model();
	}
}