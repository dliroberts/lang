package grammar.model.nouns;

import grammar.model.MatchType;
import grammar.model.PseudoEnum;
import grammar.model.WordMatcher;
import grammar.model.factory.NounClassFactory;
import grammar.util.Utilities;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * The category of a particular noun. In many languages, adjective and verbs
 * change their forms according to the class of the noun. For instance, in
 * Romance languages, there are typically two noun classes, referred to as
 * 'masculine' and 'feminine'.
 * 
 * @author Duncan Roberts
 */
public class NounClass implements PseudoEnum<NounClass> {
	private static final SortedMap<String, NounClass> INSTANCES = new TreeMap<String, NounClass>();
	
	private final int sequence;
	private final String name;
	private final String abbreviation;
	private final List<WordMatcher> nounMatchers;
	
	public NounClass(int sequence, String name, String abbreviation,
			List<WordMatcher> nounMatchers) {
		this.sequence = sequence;
		this.name = name;
		this.abbreviation = abbreviation;
		this.nounMatchers = Collections.unmodifiableList(nounMatchers);
		addGender(this);
	}
	
	public int ordinal() {return sequence;}
	public String getName()  {return name;}
	public String toString() {return Utilities.asConstantName(name);}
	
	public static NounClass guessGender(String noun) {
		return NounClassFactory.getInstance().getClosestMatch(noun);
	}

	public boolean matches(String infinitive) {
		try {
			getNounMatcher(infinitive);
			return true;
		}
		catch (IllegalArgumentException e) {
			return false;
		}
	}
	
	public WordMatcher getNounMatcher(String noun) {
		for (WordMatcher nm : nounMatchers) {
			MatchType matchType = nm.getMatchType();
			switch (matchType) {
			case FULL_NAME:
				if (noun.equalsIgnoreCase(nm.getMatchString()))
					return nm;
				break;
			case SUFFIX:
				if (noun.endsWith(nm.getMatchString()))
					return nm;
				break;
			case PATTERN:
				if (noun.matches(nm.getMatchString()))
					return nm;
				break;
			case ALL:
				return nm;
			case NONE:
				return nm;
			default:
				throw new Error("Invalid match type: "+nm.getMatchType());
			}
		}
		throw new IllegalArgumentException("Word does not match this gender.");
	}

	public boolean isCloserMatch(String infinitive, NounClass other) {
		WordMatcher infinitiveMatcher = getNounMatcher(infinitive);
		WordMatcher otherMatcher = other.getNounMatcher(infinitive);
		if ((infinitiveMatcher.getMatchType() == MatchType.PATTERN || infinitiveMatcher.getMatchType() == MatchType.SUFFIX) && 
				(otherMatcher.getMatchType() == MatchType.PATTERN || otherMatcher.getMatchType() == MatchType.SUFFIX)) {
			return getMatchStringPointsValue(infinitiveMatcher) >
				getMatchStringPointsValue(otherMatcher);
		}
		if (infinitiveMatcher.getMatchType() != otherMatcher.getMatchType())
			return infinitiveMatcher.getMatchType().ordinal() > otherMatcher.getMatchType().ordinal();
		return false;
	}

	private int getMatchStringPointsValue(WordMatcher infinitiveMatcher) {
		if (infinitiveMatcher.getMatchType() == MatchType.PATTERN) {
			String points = infinitiveMatcher.getMatchString().replaceAll("\\[.+\\]", "[");
			points = points.replaceAll("\\(\\.\\*\\)", "");
			points = points.replaceAll("[()+*]", "");
			return points.length();
		}
		else if (infinitiveMatcher.getMatchType() == MatchType.SUFFIX) {
			return infinitiveMatcher.getMatchString().length();
		}
		else throw new Error();
	}
	
	// PSEUDO-ENUM METHODS
	
	private static void addGender(NounClass gender) {
		INSTANCES.put(Utilities.asConstantName(gender.name), gender);
	}
	
	public static NounClass[] values() {
		return INSTANCES.values().toArray(new NounClass[]{});
	}
	
	public static NounClass valueOf(String key) {
		NounClass g = INSTANCES.get(Utilities.asConstantName(key));
		if (g == null)
			throw new IllegalArgumentException("No such Gender: '"+key+"'.");
		return g;
	}
	
	// BOILERPLATE
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((nounMatchers == null) ? 0 : nounMatchers.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof NounClass))
			return false;
		final NounClass other = (NounClass) obj;
		if (nounMatchers == null) {
			if (other.nounMatchers != null)
				return false;
		} else if (!nounMatchers.equals(other.nounMatchers))
			return false;
		return true;
	}

	public int compareTo(NounClass o) {
		return sequence - o.sequence;
	}

	public String getAbbreviation() {
		return abbreviation;
	}
}