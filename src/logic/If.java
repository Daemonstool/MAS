package logic;

import org.graphstream.graph.Node;

public class If implements Formula {
	
	private Formula condition;
	private Formula consequence;
	
	public If(Formula condition, Formula consequence) {
		this.condition = condition;
		this.consequence = consequence;
	}

	@Override
	public boolean evaluate(Node n) {
		return !condition.evaluate(n) || consequence.evaluate(n);
	}

	@Override
	public String pprint() {
		return condition.pprint() + "-->" + consequence.pprint();
	}

}
