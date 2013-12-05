package grammar.model.verbs;

import grammar.model.Tag;
import grammar.model.verbs.ModelVerb.ConjugatedVerb;

import java.util.HashSet;
import java.util.Set;

public enum VerbTag implements Tag<ConjugatedVerb> {
	KERNEL, CORE, COMMON, EXTENDED, ALL;
	
	private Set<ConjugatedVerb> conjugatedVerbs = new HashSet<ConjugatedVerb>();

	public Set<ConjugatedVerb> getMembers() {
		return conjugatedVerbs;
	}
	
	public void addMember(ConjugatedVerb conjugatedVerb) {
		conjugatedVerbs.add(conjugatedVerb);
	}
}