package grammar.model.verbs;

import grammar.model.PseudoEnum;
import grammar.util.Utilities;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class Tense implements PseudoEnum<Tense> {
	private static final Map<String, Tense> INSTANCES = new HashMap<String, Tense>();
	private static final Map<String, Set<Tense>> SETS = new HashMap<String, Set<Tense>>();
		
	private final Set<TenseTag> classifications;
	private final String name;
	private final Mood mood;
	private final int sequence;

	public Tense(String name, Mood mood, int sequence, Set<TenseTag> classifications) {
		this.name = name;
		this.mood = mood;
		this.sequence = sequence;
		this.classifications = classifications;
		
		INSTANCES.put(name.toUpperCase(), this);
		mood.addTense(this);
	}
	
	public int ordinal() {
		return sequence;
	}

	public static Set<Tense> getSet(String key) {
		return Collections.unmodifiableSet(SETS.get(key.toUpperCase().replace(' ', '_')));
	}

	protected static void addToSet(String setName, Tense tense) {
		setName = setName.toUpperCase().replace(' ', '_');
		Set<Tense> s = SETS.get(setName);
		if (s == null) {
			s = new HashSet<Tense>();
			SETS.put(setName, s);
		}
		s.add(tense);
	}

	public int compareTo(Tense o) {
		if (mood != o.mood)
			return mood.compareTo(o.mood);
		return sequence - o.sequence;
	}
	
	public static Tense[] values() {
		return new TreeSet<Tense>(INSTANCES.values()).toArray(new Tense[]{});
	}
	
	public static Tense valueOf(String name) {
		Tense tense = INSTANCES.get(name.toUpperCase().replace(' ', '_'));
		if (tense == null)
			throw new IllegalArgumentException("No such tense: "+name);
		return tense;
	}

	public String getLabel() {
		return name;
	}
	
	public Mood getMood() {
		return mood;
	}
	
	public String toString() {
		return Utilities.asConstantName(name);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mood == null) ? 0 : mood.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Tense))
			return false;
		final Tense other = (Tense) obj;
		if (mood == null) {
			if (other.mood != null)
				return false;
		} else if (!mood.equals(other.mood))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	public Set<TenseTag> getClassifications() {
		return classifications;
	}
}