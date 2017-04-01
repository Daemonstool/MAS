package model;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import java.util.TreeSet;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.ViewerListener;
import org.graphstream.ui.view.ViewerPipe;

import logic.*;

public class Model extends MultiGraph implements ViewerListener {

	private int worldCount;

	private ArrayList<String> clickedWorlds = new ArrayList<String>();
	private ArrayList<String> agents;
	private ArrayList<Node> selectedNodes = new ArrayList<Node>();
	private TreeSet<String> atoms = new TreeSet<String>();//set of all unique atoms in the model. For each atom each node must have a truth assignment
	private ArrayList<CommonKnowledge> CK = new ArrayList<CommonKnowledge>();

	private int cardsleft = Integer.MAX_VALUE;

	public Model() {
		super("Arbitrary String #1");
		this.worldCount = 0;
		this.agents = new ArrayList<String>();
		this.atoms = new TreeSet<String>();

		this.atoms.add("ek1");
		this.atoms.add("ek2");
		this.atoms.add("ek3");

		this.CK.add(new CommonKnowledge(new If(new Atom("ek1"),new And(new Not(new Atom("ek2")),new Not(new Atom("ek3"))))));
		this.CK.add(new CommonKnowledge(new If(new Atom("ek2"),new And(new Not(new Atom("ek1")),new Not(new Atom("ek3"))))));
		this.CK.add(new CommonKnowledge(new If(new Atom("ek3"),new And(new Not(new Atom("ek1")),new Not(new Atom("ek2"))))));

		//printCommonKnowledge();
		
		initWorlds(0,new ArrayList<String>(atoms));
	}

	private void initWorlds(int idx, ArrayList<String> atoms){
		//creates all worlds to have all combinations of atoms present
		ArrayList<String> negation = new ArrayList<String>(atoms);
		negation.remove(idx);
		if(idx == atoms.size()-1){
			//base case, add worlds
			String id = addNode("").getId();
			for(String a : atoms){
				addAtom(id,a);
			}
			id = addNode("").getId();
			for(String a : negation){
				addAtom(id,a);
			}
		}else{
			initWorlds(idx+1,atoms);
			initWorlds(idx,negation);
		}
	}

	private String getNextWorldName(){
		//generate the next world id
		return "w" + ++worldCount;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Node addNode(String dump) {
		Node n = super.addNode(getNextWorldName());
		n.setAttribute("atoms", new ArrayList<String>());
		return n;
	}

	public void addAtom(String node, String atom) {
		Node n = getNode(node);
		ArrayList<String> nodeAtoms = n.getAttribute("atoms");
		nodeAtoms.add(atom);
		if(!this.atoms.contains(atom)){
			//update the set of all atoms in the model
			this.atoms.add(atom);
		}
	}

	public void constructFromFile(String s)
	{
		try {
			BufferedReader in = new BufferedReader(new FileReader(s));

			String line;
			while((line = in.readLine()) != null)
			{
				String[] args = line.split(" ");
				if (args.length == 2)
					addAtom(args[0], args[1]);
				else if (args.length == 4 && args[2].equals("B"))
				{
					addRelation(args[0], args[1], args[3]);
					addRelation(args[1], args[0], args[3]);
				}
				else if (args.length == 4 && args[2].equals("D"))
					addRelation(args[0], args[1], args[3]);
			}
			in.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public Edge addEdge(String agent, String idFrom, String idTo) {
		//Adds an edge between two world, with at least one agent
		//DIRECT USE NOT RECOMMENDED
		//This method assumes the edge doesn't already exist, you'll have to check this before
		//Using addRelation() does this for you.
		//(but we could not make this method private as it is inherited)
		Edge e = super.addEdge(idFrom + idTo, idFrom, idTo, true);
		if (getEdge(idTo + idFrom) != null) {
			// symmetric relation, do some styling
			e.setAttribute("ui.class", "symmetric");// tag only applies to one
			// side to separate the labels
		}
		if (idFrom.equals(idTo)) {
			// reflexive relation, tag it
			e.setAttribute("ui.class", "reflexive");
		}
		ArrayList<String> agents = new ArrayList<String>();
		agents.add(agent);
		e.setAttribute("agents", agents);
		return e;
	}

	public void addRelation(String idFrom, String idTo, String agent) {
		//adds a relation for an agent between two worlds
		Edge e = getEdge(idFrom+idTo);
		if(e == null){
			//need to add the edge
			e = addEdge(agent,idFrom,idTo);
		}else{
			ArrayList<String> agents = e.getAttribute("agents");
			agents.add(agent);
		}
		System.out.println("Add relation: " + idFrom + idTo + " for " + agent);
		e.addAttribute("layout.weight", 8);
	}

	public ArrayList<String> getAtoms(String node) {
		return getNode(node).getAttribute("atoms");
	}

	private String constructNodeLabel(Node n){
		ArrayList<String> nodeAtoms = n.getAttribute("atoms");
		StringBuilder ss = new StringBuilder(n.getId() + ": ");
		for(String a : atoms){
			if(nodeAtoms.contains(a)){
				ss.append(a);
			}else{
				ss.append("¬" + a);
			}
			ss.append(", ");
		}
		ss.delete(ss.length()-2,ss.length());
		return ss.toString();
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
			n.addAttribute("ui.color", new Color(0, 0, 0));
			n.setAttribute("ui.label", constructNodeLabel(n));
		}
		Iterator<Edge> edges = getEdgeIterator();
		while (edges.hasNext()) {
			Edge e = edges.next();
			e.setAttribute("ui.label", "");	
		}
		
		Viewer view = super.display();
		
		ViewerPipe viewPipe = view.newViewerPipe();
		viewPipe.addViewerListener(this);
		viewPipe.addSink(this);
		viewPipe.pump();

		while (true) {
			viewPipe.pump();
		}
	}

	public void removeRelation(String edgeId, String agent){
		//remove a relation for an agent between two worlds
		Edge e = getEdge(edgeId);
		if(e != null){
			ArrayList<String> agents = e.getAttribute("agents");
			if(agents.contains(agent)){
				//System.out.println("Removing relation " + edgeId + " for agent " + agent);
				agents.remove(agent);
				if(agents.isEmpty()){
					removeEdge(edgeId);
				}
				return;
			}
		}
		System.err.println("Tried to remove agent " + agent + "on relation " + edgeId + "while that relation wasn't there!");
	}

	public void removeRelation(String idFrom, String idTo, String agent){
		//remove a relation for an agent between two worlds
		removeRelation(idFrom+idTo,agent);
	}

	public boolean hasRelation(String edgeId, String agent){
		Edge e = getEdge(edgeId);
		if(e != null){
			ArrayList<String> agents = e.getAttribute("agents");
			return agents.contains(agent);
		}else{
			return false;
		}

	}

	public boolean hasRelation(String idFrom, String idTo, String agent){
		return hasRelation(idFrom+idTo,agent);
	}

	public boolean isConsistent(Node n){
		//returns whether a node is possible given the common knowledge rules
		for(CommonKnowledge c : CK){
			if(!c.evaluate(n)){
				return false;
			}
		}
		return true;
	}

	/* Checks whether how much worlds an agent can reach given a world */
	public int canAccessWorlds(String agent, Node n)
	{
		Iterator<Edge> it =  n.getEachLeavingEdge().iterator();

		int accessCount = 0;
		while (it.hasNext())
		{
			Edge e = it.next();
			ArrayList<String> agents = e.getAttribute("agents");

			if (agents.contains(agent))
			{
				++accessCount;
			}
		}

		return accessCount;

	}

	private void canAccessWorlds(String agent)
	{	
		Iterator<Node> nodes = getNodeIterator();
		while (nodes.hasNext())
		{
			Node n = nodes.next();
			ArrayList<String> atoms = n.getAttribute("atoms");
			StringBuilder sb = new StringBuilder();
			for (String s : atoms)
			{
				Formula f = new Knows(new Atom(s),agent);
				f.evaluate(n);
				sb.append(s);
			}
			System.out.println("Agent: " + agent + " can access " + n.getId() + " with " + sb.toString());
		}
	}
	
	private void printCommonKnowledge(){
		for(CommonKnowledge f : this.CK){
			System.out.println(f.pprint());
		}
	}

	public int getWorldCount() {
		return worldCount;
	}

	public ArrayList<String> getAgents() {
		return agents;
	}

	@Override
	public void buttonReleased(String id) {}

	@Override
	public void viewClosed(String viewName) {}

	@Override
	public void buttonPushed(String id) {
		// TODO Auto-generated method stub

		Iterator<Edge> it = getNode(id).getEachEdge().iterator();

		if (selectedNodes.contains(getNode(id)))
		{
			selectedNodes.remove(getNode(id));
			getNode(id).removeAttribute("ui.color");
			getNode(id).addAttribute("ui.color", new Color(0, 0, 0));
		}
		else
		{
			selectedNodes.add(getNode(id));
			getNode(id).removeAttribute("ui.color");
			getNode(id).addAttribute("ui.color", new Color(255, 0, 0));
		}

		Iterator<Node> it3 = super.getNodeIterator();

		while (it3.hasNext())
		{
			Node n3 = it3.next();
			Iterator<Node> it4 = super.getNodeIterator();
			while (it4.hasNext())
			{
				Node n4 = it4.next();
				if (n3 != null && n4 != null)
				{
					Edge e2 = getEdge(n3.getId() + n4.getId());
					if (e2 != null && selectedNodes.contains(n3) && selectedNodes.contains(n4))
					{
						e2.setAttribute("ui.label", e2.getAttribute("agents").toString());
					}
					else if (e2 != null)
					{
						e2.setAttribute("ui.label", "");
					}
				}
			}
		}
	}

	// e.g.: removes all but w4w4
	public void removeRelationsForAgentsExcept(String agent, Node n)
	{
		for(int w1 = 1; w1 <= worldCount; ++w1){
			for(int w2 = 1; w2 <= worldCount; ++w2){
				if (hasRelation("w" + w1, "w"+ w2, agent) && !(n.equals(getNode("w" + w1)) && n.equals(getNode("w" + w2)))) {
					removeRelation("w" + w1, "w" + w2, agent);
				}
			}
		}
	}	

	public void setCardsleft(int cardsleft) {
		this.cardsleft = cardsleft;
	}

	public int getCardsleft() {
		return cardsleft;
	}

	public void assignLabels()
	{

		Iterator<Node> nodes = getNodeIterator();

		while (nodes.hasNext()) {
			Node n = nodes.next();
			n.addAttribute("ui.color", new Color(0, 0, 0));
			n.setAttribute("ui.label", " " + n.getId() + ": " + n.getAttribute("atoms").toString());
		}
		Iterator<Edge> edges = getEdgeIterator();
		while (edges.hasNext()) {
			Edge e = edges.next();
			e.setAttribute("ui.label", "");	
		}
	}
	
	public void setWorldCount(int worldCount) {
		this.worldCount = worldCount;
	}
}
