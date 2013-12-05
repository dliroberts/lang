/**
 * 
 */
package grammar.model.nouns;

import grammar.model.Multiplicity;
import grammar.model.PseudoEnum;
import grammar.model.WordMatcher;
import grammar.model.factory.NounClassFactory;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NounForm implements PseudoEnum<NounForm> {
	private static final Map<String, NounForm> INSTANCE_MAP = new HashMap<String, NounForm>();
	private static final Set<NounForm> INSTANCES = new HashSet<NounForm>();
	
	private static int sequenceGenerator = 0;
	
	private final int sequence;
	private final Multiplicity multiplicity;
	private final List<NounClass> nounClasses;
	private final String text;
	private Noun noun;
	
	public NounForm(Multiplicity multiplicity, List<NounClass> nounClasses,
			String text) {
		if (nounClasses.size() == 0)
			throw new IllegalArgumentException("No noun class defined for noun form "+text+".");
		
		sequence = sequenceGenerator++;
		this.multiplicity = multiplicity;
		this.nounClasses = Collections.unmodifiableList(nounClasses);
		this.text = text;
		
		INSTANCE_MAP.put(text, this);
		INSTANCES.add(this);
	}
	
	void setNoun(Noun noun) {
		this.noun = noun;
	}
	
	public Noun getNoun() {
		return noun;
	}
	
	public static NounForm[] getForms(NounClass nounClass, Multiplicity multiplicity) {
		Set<NounForm> f = new HashSet<NounForm>();
		
		for (NounForm nounForm : INSTANCE_MAP.values()) {
			if (nounClass == null || nounForm.getNounClasses().equals(nounClass)) {
				if (multiplicity == null || nounForm.getMultiplicity().equals(multiplicity)) {
					 f.add(nounForm);
				}
			}
		}
		return f.toArray(new NounForm[]{});
	}
	
	public WordMatcher getNounClassMatcher() {
		try {
			return NounClassFactory.getInstance().getClosestMatch(text).getNounMatcher(text);
		}
		catch (IllegalArgumentException iae) {
			return null;
		}
	}
	
	public int ordinal() {
		return sequence;
	}
	
	public int compareTo(NounForm o) {
		return ordinal() - o.ordinal();
	}
	
	public static NounForm[] values() {
		return INSTANCES.toArray(new NounForm[]{});
	}

	public static NounForm valueOf(String key) {
		NounForm m = INSTANCE_MAP.get(key);
		if (m == null)
			throw new IllegalArgumentException("No such NounForm: '"+key+"'.");
		return m;
	}

	public boolean isRegular() {
		return getRegularityCategory().equals(RegularityCategory.REGULAR);
	}
	
	public boolean isException() {
		return getRegularityCategory().equals(RegularityCategory.EXCEPTION);
	}
	
	public boolean isUncovered() {
		return getRegularityCategory().equals(RegularityCategory.NOT_COVERED);
	}
	
	private RegularityCategory getRegularityCategory() {
		try {
			NounClass guess = NounClass.guessGender(getText());
			return nounClasses.size() > 1 ? RegularityCategory.EXCEPTION :
				(nounClasses.get(0).equals(guess) ? RegularityCategory.REGULAR : RegularityCategory.EXCEPTION);
		}
		catch (IllegalArgumentException e) {
			return RegularityCategory.NOT_COVERED;
		}
	}
	
	public Multiplicity getMultiplicity() {
		return multiplicity;
	}

	public List<NounClass> getNounClasses() {
		return Collections.unmodifiableList(nounClasses);
	}

	public String getText() {
		return text;
	}
	
	public String toString() {
		return getText();
	}
	
	public enum RegularityCategory {
		REGULAR, NOT_COVERED, EXCEPTION
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + sequence;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof NounForm))
			return false;
		final NounForm other = (NounForm) obj;
		if (sequence != other.sequence)
			return false;
		return true;
	}
}