package logic;

import model.Model;

public class CommonKnowledge {
	
	private Formula f;

	public boolean evaluate(Model m) {
		return m.getCommonKnowledge().contains(f);
	}

	public String pprint() {
		return "C(" + f.pprint() + ")";
	}

}
