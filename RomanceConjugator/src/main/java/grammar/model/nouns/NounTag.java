package grammar.model.nouns;

import grammar.model.Tag;
import java.util.HashSet;
import java.util.Set;

public enum NounTag implements Tag<Noun> {
	KERNEL, CORE, COMMON, EXTENDED, HISTORIC, ALL;
	
	private Set<Noun> nouns = new HashSet<Noun>();
	
	public Set<Noun> getMembers() {
		return nouns;
	}
	
	public void addMember(Noun noun) {
		nouns.add(noun);
	}
}