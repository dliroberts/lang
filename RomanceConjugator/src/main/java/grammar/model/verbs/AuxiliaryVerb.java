package grammar.model.verbs;

import grammar.model.Language;
import grammar.model.MatchType;
import grammar.model.WordMatcher;
import grammar.model.Form.FormCategory;
import java.util.List;
import java.util.Map;

public class AuxiliaryVerb extends ModelVerb {
	private final boolean pronounAgreement;
	private final boolean reflexiveAuxiliary;
	private final List<WordMatcher> auxiliaryOf;

	public AuxiliaryVerb(String infinitive,
			List<ModelVerb> parents, List<WordMatcher> infinitiveMatchers,
			Map<Tense, Map<FormCategory, Map<Segment, String>>> tenseMap,
			Map<Tense, Map<FormCategory, Map<Segment, Tense>>> refTenseMap,
			Map<Tense, Map<FormCategory, Map<Segment, FormCategory>>> refPronounMap,
			Map<Tense, Map<FormCategory, Map<Segment, Tense>>> auxiliaryRefTenseMap,
			Map<Tense, Map<FormCategory, Map<Segment, FormCategory>>> auxiliaryRefPronounMap,
			List<WordMatcher> auxiliaryOf, String summary, boolean pronounAgreement,
			boolean reflexiveAuxiliary, Language language) {
		super(infinitive, parents, infinitiveMatchers, tenseMap, refTenseMap, refPronounMap,
				auxiliaryRefTenseMap, auxiliaryRefPronounMap, summary, language);
		this.auxiliaryOf = auxiliaryOf;
		this.pronounAgreement = pronounAgreement;
		this.reflexiveAuxiliary = reflexiveAuxiliary;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((auxiliaryOf == null) ? 0 : auxiliaryOf.hashCode());
		result = prime * result + (pronounAgreement ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof AuxiliaryVerb))
			return false;
		final AuxiliaryVerb other = (AuxiliaryVerb) obj;
		if (auxiliaryOf == null) {
			if (other.auxiliaryOf != null)
				return false;
		} else if (!auxiliaryOf.equals(other.auxiliaryOf))
			return false;
		if (pronounAgreement != other.pronounAgreement)
			return false;
		return true;
	}
	
	public ConjugatedVerb getConjugatedVerb() {
		return getConjugatedVerb(getName());
	}
	
	public boolean isAuxiliaryOf(String infinitive, boolean reflexive) {
		return isAuxiliaryOfInternal(infinitive, reflexive) != MatchType.NONE;
	}

	private MatchType isAuxiliaryOfInternal(String infinitive, boolean reflexive) {
		if (reflexive && isReflexiveAuxiliary())
			return MatchType.REFLEXIVE;
		
		for (WordMatcher inf : auxiliaryOf) {
			MatchType matchType = inf.getMatchType();
			MatchType match = null;
			switch (matchType) {
			case ALL:
				match = MatchType.ALL;
				break;
			case NONE:
				break;
			case FULL_NAME:
				if (infinitive.equals(inf.getMatchString()))
					match = MatchType.FULL_NAME;
				break;
			case PATTERN:
				if (infinitive.matches(inf.getMatchString()))
					match = MatchType.PATTERN;
				break;
			case SUFFIX:
				if (infinitive.endsWith(inf.getMatchString()))
					match = MatchType.SUFFIX;
				break;
			}
			if (match != null)
				return match;
		}
		return MatchType.NONE;
	}
	
	private String getAuxiliaryMatchString(String infinitive) {
		for (WordMatcher inf : auxiliaryOf) {
			MatchType matchType = inf.getMatchType();
			boolean match = false;
			switch (matchType) {
			case FULL_NAME:
				if (infinitive.equals(inf.getMatchString()))
					match = true;
				break;
			case PATTERN:
				if (infinitive.matches(inf.getMatchString()))
					match = true;
				break;
			case SUFFIX:
				if (infinitive.endsWith(inf.getMatchString()))
					match = true;
				break;
			}
			if (match)
				return inf.getMatchString();
		}
		return "";
	}
	
	public boolean isCloserMatch(String infinitive, boolean reflexive, AuxiliaryVerb other) {
		MatchType thisMt = isAuxiliaryOfInternal(infinitive, reflexive);
		MatchType otherMt = other.isAuxiliaryOfInternal(infinitive, reflexive);
		
		/*if (isReflexiveAuxiliary()) {
			System.out.println("SQUoooo");
			if (reflexive) {
				System.out.println("squOo2");
				return true;
			}
		}*/
		
		if (thisMt == MatchType.SUFFIX && otherMt == MatchType.SUFFIX) {
			return getAuxiliaryMatchString(infinitive).length() >
				other.getAuxiliaryMatchString(infinitive).length();
		}
		if (thisMt.ordinal() > otherMt.ordinal())
			return true;
		return false;
	}

	public boolean pronounAgreement() {
		return pronounAgreement;
	}

	public boolean isReflexiveAuxiliary() {
		return reflexiveAuxiliary;
	}
	
	public WordMatcher getInfinitiveMatcher(String infinitive, boolean reflexive) {
//		if (reflexive && isReflexiveAuxiliary())
//			return new WordMatcher(MatchType.REFLEXIVE, infinitive);
		return super.getInfinitiveMatcher(infinitive, reflexive);
	}
}