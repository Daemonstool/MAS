package logic;

import java.util.Iterator;

import org.graphstream.graph.Node;

public class CommonKnowledge implements Formula {
	
	private Formula f;
	
	public CommonKnowledge(Formula f){
		this.f = f;
	}

	public boolean evaluate(Node n) {
		Iterator<Node> nodes = n.getBreadthFirstIterator();
		while(nodes.hasNext()){
			Node node = nodes.next();
			if(!f.evaluate(node)){
				return false;
			}
		}
		return true;
	}

	public String pprint() {
		return "C(" + f.pprint() + ")";
	}

}
