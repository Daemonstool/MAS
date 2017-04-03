package logic;

import java.util.Iterator;

import org.graphstream.graph.Node;

import model.Model;

public class CommonKnowledge implements Formula {
	
	private Formula f;
	
	public CommonKnowledge(Formula f){
		this.f = f;
	}

	public boolean evaluate(Node n) {
		Iterator<Node> nodes = n.getDepthFirstIterator();
		while(nodes.hasNext()){
			Node node = nodes.next();
			if(!f.evaluate(node)){
				System.out.println(node);
				return false;
			}
		}
		return true;
	}

	public String pprint() {
		return "C(" + f.pprint() + ")";
	}
	
	public Formula getFormula(){
		return f;
	}

}
