package model;

import java.util.ArrayList;

import org.graphstream.graph.implementations.AbstractGraph;
import org.graphstream.graph.implementations.MultiNode;

import logic.Atom;

public class State extends MultiNode {
	
	private ArrayList<Atom> atoms;

	public State(AbstractGraph graph, String id) {
		super(graph, id);
		this.atoms = new ArrayList<Atom>();
	}
	
	public State(AbstractGraph graph, String id, ArrayList<Atom> atoms) {
		super(graph, id);
		this.atoms = atoms;
	}
	
	public ArrayList<Atom> getAtoms(){
		return atoms;
	}

}
