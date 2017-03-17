package logic;

import org.graphstream.graph.Node;

public class Not implements Formula{

	private Formula f;
	
	public Not(Formula f) {
		this.f = f;
	}

	@Override
	public boolean evaluate(Node n) {
		return !f.evaluate(n);
	}

	@Override
	public String pprint() {
		return "¬" + f.pprint();
	}

}
