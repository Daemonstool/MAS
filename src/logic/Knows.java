package logic;

import java.util.ArrayList;
import java.util.Iterator;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;

public class Knows implements Formula {
	
	private Formula f;
	private String agent;

	public Knows(Formula f, String agent) {
		this.f = f;
		this.agent = agent;
	}

	@Override
	public boolean evaluate(Node n) {
		Iterator<Edge> worlds = n.getEachLeavingEdge().iterator();
		while (worlds.hasNext()){
			Edge e = worlds.next();
			ArrayList<String> agents = e.getAttribute("agents");
			if (agents.contains(agent)){
				//world is accessible for the agent
				Node w = e.getTargetNode();
				if (!f.evaluate(w)){
					return false;
				}
			}
		}
		//went over all worlds and all evaluated to true!
		return true;
	}

	@Override
	public String pprint() {
		return "K(" + f.pprint() + ")";
	}

}
