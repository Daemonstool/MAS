package logic;

import org.graphstream.graph.Node;

public interface Formula {
	public boolean evaluate(Node n);//evaluate the formula in a given state
	public String pprint();//return a string representation of the formula
}
