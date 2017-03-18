package logic;

import java.util.ArrayList;
import java.util.Iterator;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;

public class Maybe implements Formula {

	private Formula f;
	private String agent;

	public Maybe(Formula f, String agent) {
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
				if (f.evaluate(w)){
					return true;
				}
			}
		}
		//went over all worlds and all evaluated to false!
		return false;
	}

	@Override
	public String pprint() {
		return "M_" + agent + "(" + f.pprint() + ")";
	}

}
