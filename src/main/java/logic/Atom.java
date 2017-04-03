package logic;

import java.util.ArrayList;

import org.graphstream.graph.Node;

public class Atom implements Formula{

	private String name;
	
	public Atom(String name){
		this.name = name;
	}
	
	@Override
	public boolean evaluate(Node n) {
		ArrayList<String> atoms = n.getAttribute("atoms");
		return atoms.contains(name);
	}

	@Override
	public String pprint() {
		return name;
	}

}
