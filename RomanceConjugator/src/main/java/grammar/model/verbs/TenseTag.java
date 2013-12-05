package grammar.model.verbs;

import grammar.model.Tag;
import java.util.HashSet;
import java.util.Set;

public enum TenseTag implements Tag<Tense> {
	KERNEL, COMMON, EXTENDED, HISTORIC, PERSONAL, IMPERSONAL;
	
	private Set<Tense> tenses = new HashSet<Tense>();
	
	public Set<Tense> getMembers() {
		return tenses;
	}
	
	public void addMember(Tense tense) {
		tenses.add(tense);
	}
}