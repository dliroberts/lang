package grammar.model.verbs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import grammar.input.xml.DataManager;
import grammar.model.Form;
import grammar.model.Language;
import grammar.model.MatchType;
import grammar.model.PseudoEnum;
import grammar.model.WordMatcher;
import grammar.model.Form.FormCategory;
import grammar.model.factory.ModelVerbFactory;
import grammar.util.Utilities;

/**
 * <p>A means of conjugating a particular set of infinitives. ModelVerb instances
 * define (via the matcmhes() method) what infinitives this model can be used for,
 * and provide a facility for retrieving segments of a conjugation using the
 * getForm() method. ModelVerb instances are created on load using XML definition
 * files, which specify what rules should be used for conjugation, and what
 * infinitives are applicable.</p>
 * 
 * <p>ModelVerb instances can inherit rules from </p>
 * 
 * @author Duncan Roberts
 */
public class ModelVerb implements PseudoEnum<ModelVerb> {
	private static final Map<String, ModelVerb> INSTANCES = new HashMap<String, ModelVerb>();

	private final Set<ConjugatedVerb> conjugatedVerbs = new HashSet<ConjugatedVerb>();
		
	private String name;
	private final List<ModelVerb> parents = new ArrayList<ModelVerb>();
	private final List<WordMatcher> infinitiveMatchers;
	private     Map<Tense, Map<FormCategory, Map<Segment, String>>> tenseMap =
		new HashMap<Tense, Map<FormCategory, Map<Segment, String>>>();
	private     Map<Tense, Map<FormCategory, Map<Segment, Tense>>> refTenseMap =
		new HashMap<Tense, Map<FormCategory, Map<Segment, Tense>>>();
	private     Map<Tense, Map<FormCategory, Map<Segment, FormCategory>>> refPronounMap =
		new HashMap<Tense, Map<FormCategory, Map<Segment, FormCategory>>>();
	private     Map<Tense, Map<FormCategory, Map<Segment, Tense>>> auxiliaryRefTenseMap =
		new HashMap<Tense, Map<FormCategory, Map<Segment, Tense>>>();
	private     Map<Tense, Map<FormCategory, Map<Segment, FormCategory>>> auxiliaryRefPronounMap =
		new HashMap<Tense, Map<FormCategory, Map<Segment, FormCategory>>>();
	private String summary;
	private Language language;

	public ModelVerb(String name, List<ModelVerb> parents,
			List<WordMatcher> infinitiveMatchers,
			Map<Tense, Map<FormCategory, Map<Segment, String>>> tenseMap,
			Map<Tense, Map<FormCategory, Map<Segment, Tense>>> refTenseMap,
			Map<Tense, Map<FormCategory, Map<Segment, FormCategory>>> refPronounMap,
			Map<Tense, Map<FormCategory, Map<Segment, Tense>>> auxiliaryRefTenseMap,
			Map<Tense, Map<FormCategory, Map<Segment, FormCategory>>> auxiliaryRefPronounMap,
			String summary, Language language) {
		this.name = name;
		this.language = language;
		this.parents.addAll(parents);
		this.infinitiveMatchers = infinitiveMatchers;
		this.tenseMap = tenseMap;
		this.refTenseMap = refTenseMap;
		this.refPronounMap = refPronounMap;
		this.auxiliaryRefTenseMap = auxiliaryRefTenseMap;
		this.auxiliaryRefPronounMap = auxiliaryRefPronounMap;
		this.summary = summary;
		
		INSTANCES.put(Utilities.asConstantName(name), this);
	}
	
	public static ModelVerb[] values() {
		return INSTANCES.values().toArray(new ModelVerb[] {});
	}
	
	public static ModelVerb valueOf(String name) {
		ModelVerb mv = INSTANCES.get(Utilities.asConstantName(name));
		if (mv == null)
			throw new IllegalArgumentException("No ModelVerb with name '"+name+"'.");
		return mv;
	}
	
	public int ordinal() {
		return name.hashCode(); // TODO replace with sensible implementation
	}
	
	public int compareTo(ModelVerb mv) {
		return ordinal() - mv.ordinal();
	}

	public List<ModelVerb> getParents() {
		return Collections.unmodifiableList(parents);
	}
	
	public int getDepth() {
		if (parents.size() == 0)
			return 0;
		return parents.get(0).getDepth()+1;
	}
	
	public Language getLanguage() {
		return language;
	}
	
	public boolean matches(String infinitive, boolean reflexive) {
		try {
			getInfinitiveMatcher(infinitive, reflexive);
			return true;
		}
		catch (IllegalArgumentException e) {
			return false;
		}
	}
	
	public List<WordMatcher> getInfinitiveMatchers() {
		return infinitiveMatchers;
	}
	
	public WordMatcher getInfinitiveMatcher(String infinitive, boolean reflexive) {
		for (WordMatcher im : infinitiveMatchers) {
			MatchType matchType = im.getMatchType();
			switch (matchType) {
			case FULL_NAME:
				if (infinitive.equalsIgnoreCase(im.getMatchString()))
					return im;
				break;
			case SUFFIX:
				if (infinitive.endsWith(im.getMatchString()))
					return im;
				break;
			case PATTERN:
				if (infinitive.matches(im.getMatchString()))
					return im;
				break;
			case ALL:
				return im;
			case NONE:
				return im;
			default:
				throw new Error("Invalid match type: "+im.getMatchType());
			}
		}
		throw new IllegalArgumentException(infinitive+" does not match model verb "+name);
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
	
	public boolean isCloserMatch(String infinitive, boolean reflexive, ModelVerb other) {
		if (isDescendantOf(other))
			return true;
		
		WordMatcher infinitiveMatcher;
		try {
			infinitiveMatcher = getInfinitiveMatcher(infinitive, reflexive);
		}
		catch (IllegalArgumentException iae) {
			return false;
		}
		
		WordMatcher otherMatcher;
		try {
			otherMatcher = other.getInfinitiveMatcher(infinitive, reflexive);
		}
		catch (IllegalArgumentException iae) {
			return true;
		}
		
		if ((infinitiveMatcher.getMatchType() == MatchType.PATTERN || infinitiveMatcher.getMatchType() == MatchType.SUFFIX) && 
				(otherMatcher.getMatchType() == MatchType.PATTERN || otherMatcher.getMatchType() == MatchType.SUFFIX)) {
			return getMatchStringPointsValue(infinitiveMatcher) >
				getMatchStringPointsValue(otherMatcher);
		}
		if (infinitiveMatcher.getMatchType() != otherMatcher.getMatchType())
			return infinitiveMatcher.getMatchType().ordinal() > otherMatcher.getMatchType().ordinal();
		return false;
	}
	
	private boolean isDescendantOf(ModelVerb other) {
		for (ModelVerb p : parents) {
			//if (p == null || getName().equals(other.getName()))
			//	return false;
			if (p.getName().equals(other.getName()))
				return true;
			if (p.isDescendantOf(other))
				return true;
		}
		return false;
	}
	
	public FormCategory[] getForms(Tense tense) {
		Map<FormCategory, Map<Segment, Tense>> m = refTenseMap.get(tense);
		
		if (m != null)
			return new TreeSet<FormCategory>(m.keySet()).toArray(new FormCategory[]{});
		
		for (ModelVerb parent : parents) {
			FormCategory[] s = parent.getForms(tense);
			if (s != null)
				return s;
		}
		
		return new TreeSet<FormCategory>().toArray(new FormCategory[]{});
	}
	
	private static ModelVerb sourceModelVerb = null;
	private static ModelVerb sourceRedirectionModelVerb = null;
	private static ModelVerb invokedModelVerb = null;
	
	public String getForm(Tense tense, FormCategory pronounCategory, Segment segment, boolean auxiliary) {
		invokedModelVerb = this;
		return getFormRecursive(tense, pronounCategory, segment, this, auxiliary);
	}
	
	private static String getFormRecursive(Tense tense, FormCategory pronounCategory, Segment segment, ModelVerb modelVerb, boolean auxiliary) {
		String form = null;
		
		if (!auxiliary) {
			try { // 1. Check for rule for this tense.
				form = modelVerb.tenseMap.get(tense).get(pronounCategory).get(segment); // will throw NullPointerException if no rule in this model verb
				if (form != null)
					sourceModelVerb = modelVerb;
				if (form.contains("<parent />")) {
					for (ModelVerb parentModelVerb : modelVerb.parents) {
						ModelVerb mv = sourceModelVerb; // 'backup' variable
						String inheritedForm = getFormRecursive(tense, pronounCategory, segment, parentModelVerb, false);
						sourceModelVerb = mv; // restore value as it's this form (containing <parent/>) that we're interested in.
						if (inheritedForm != null) {
							form = form.replace("<parent />", inheritedForm);
							break;
						}
					}
					form = form.replace("<parent />", ""); // if there's nothing to inherit, remove this flag
				}
				return form;
			}
			catch (NullPointerException npe) {
				// Legitimate; query other sources (references to other tenses; parents) for rule instead.
			}
		}
		
		if (form == null) { // 2. Check for links to other tenses.
			try {
				Map<Tense, Map<FormCategory, Map<Segment, Tense>>> tm =
					auxiliary ? modelVerb.auxiliaryRefTenseMap : modelVerb.refTenseMap;
				Map<Tense, Map<FormCategory, Map<Segment, FormCategory>>> pm =
					auxiliary ? modelVerb.auxiliaryRefPronounMap : modelVerb.refPronounMap;
				
				Tense targetTense =                  tm.get(tense).get(pronounCategory).get(segment);
				FormCategory targetPronounCategory = pm.get(tense).get(pronounCategory).get(segment);
				
				if (targetTense == null)
					targetTense = getTargetTense(modelVerb, tense, pronounCategory, segment);
				if (targetPronounCategory == null)
					targetPronounCategory = getTargetPronounCategory(modelVerb, tense, pronounCategory, segment);
				
				if (targetTense != tense || targetPronounCategory != pronounCategory) {
					sourceRedirectionModelVerb = modelVerb;
					form = getFormRecursive(targetTense, targetPronounCategory, segment, invokedModelVerb, false);
				}
			}
			catch (NullPointerException npe) {
				// Legitimate
			}
		}
		
		if (form == null) { // 3. Recursively check parents for rules.
			ModelVerb closestMatch = null;
			ModelVerb closestRedirectionMatch = null;
			for (ModelVerb parentModelVerb : modelVerb.parents) {
				String formTmp = getFormRecursive(tense, pronounCategory, segment, parentModelVerb, auxiliary);
				if (formTmp != null) {
					boolean closer = false;
					if (sourceModelVerb == null) {
						closer = true;
					}
					else if (closestMatch == null) {
						closer = true;
					}
					else if (sourceRedirectionModelVerb != null) {
						if (closestRedirectionMatch != null) {
							if (sourceRedirectionModelVerb.isDescendantOf(closestRedirectionMatch)) {
								closer = true;
							}
							else if (sourceRedirectionModelVerb.equals(closestRedirectionMatch)) {
								if (sourceModelVerb.isDescendantOf(closestMatch))
									closer = true;								
							}
						}
						else {
							closer = true;
						}
					}
					else {
						if (closestRedirectionMatch != null) {
							closer = true;
						}
						else {
							if (sourceModelVerb.isDescendantOf(closestMatch))
								closer = true;
						}
					}

					if (closer) {
						closestRedirectionMatch = sourceRedirectionModelVerb;
						closestMatch = sourceModelVerb;
						form = formTmp;
					}
				}
			}
		}
		return form;
	}
	
	private static FormCategory getTargetPronounCategory(
			ModelVerb modelVerb, Tense tense, FormCategory pronounCategory,
			Segment segment) {
		try {
			FormCategory targetPronounCategory = modelVerb.refPronounMap.get(tense).get(pronounCategory).get(segment);
			if (targetPronounCategory != null)
				return targetPronounCategory;
		}
		catch (NullPointerException npe) {
			// Legitimate
		}
		for (ModelVerb parent : modelVerb.parents) {
			FormCategory targetPronounCategory =
				getTargetPronounCategory(parent, tense, pronounCategory, segment);
			if (targetPronounCategory != null)
				return targetPronounCategory;
		}
		
		return null;
	}
	
	private static Tense getTargetTense(
			ModelVerb modelVerb, Tense tense, FormCategory pronounCategory,
			Segment segment) {
		try {
			Tense targetTense = modelVerb.refTenseMap.get(tense).get(pronounCategory).get(segment);
			if (targetTense != null)
				return targetTense;
		}
		catch (NullPointerException npe) {
			// Legitimate
		}
		for (ModelVerb parent : modelVerb.parents) {
			Tense targetTense = getTargetTense(parent, tense, pronounCategory, segment);
			if (targetTense != null)
				return targetTense;
		}
		
		return null;
	}

	public ConjugatedVerb getConjugatedVerb(String infinitive) {
		ConjugatedVerb c = new ConjugatedVerb(infinitive, this);
		conjugatedVerbs.add(c);
		return c;
	}
	
	public ConjugatedVerb getConjugatedVerb(String infinitive, Set<VerbTag> classifications) {
		ConjugatedVerb c = new ConjugatedVerb(infinitive, this, classifications);
		conjugatedVerbs.add(c);
		return c;
	}
	
	public Set<ConjugatedVerb> getConjugatedVerbs(boolean includeInherited, boolean includeSelf) {
		Set<ConjugatedVerb> s = new HashSet<ConjugatedVerb>();
		if (includeInherited) {
			for (ModelVerb mv : INSTANCES.values()) {
				if (mv.isDescendantOf(this)) {
					s.addAll(mv.conjugatedVerbs);
				}
			}
		}
		if (includeSelf) {
			s.addAll(conjugatedVerbs);
		}
		return s;
	}
	
	public Set<ConjugatedVerb> getConjugatedVerbs() {
		return getConjugatedVerbs(false, true);
	}
	
	public static class ConjugatedVerb implements Comparable<ConjugatedVerb> {
		private static final Map<String, ConjugatedVerb> INSTANCES =
			new HashMap<String, ConjugatedVerb>();

		private final String infinitive;
		private final String verb;
		private final ModelVerb modelVerb;
		private final AuxiliaryVerb auxiliary;
		private final Set<VerbTag> classifications;
		private boolean reflexive;
		
		private ConjugatedVerb(String infinitive, ModelVerb modelVerb) {
			this(infinitive, modelVerb, new HashSet<VerbTag>());
		}
		
		private ConjugatedVerb(String infinitive, ModelVerb modelVerb, Set<VerbTag> classifications) {
			this.modelVerb = modelVerb;
			reflexive = modelVerb.getLanguage().isReflexive(infinitive);
			auxiliary = ModelVerbFactory.getInstance().getModelAuxiliaryVerb(infinitive, reflexive);
			this.classifications =  classifications;
			this.infinitive = infinitive;
			this.verb = reflexive ? modelVerb.getLanguage().stripReflexiveMarker(infinitive) : infinitive;
			
			if (!DataManager.getInstance(modelVerb.getLanguage()).loaded())
				INSTANCES.put(infinitive, this);
		}
		
		public String getVerb() {
			return verb;
		}

		public Set<VerbTag> getClassifications() {
			return classifications;
		}

		public static ConjugatedVerb[] values() {
			return INSTANCES.values().toArray(new ConjugatedVerb[] {});
		}
		
		public static ConjugatedVerb valueOf(String name) {
			ConjugatedVerb mv = INSTANCES.get(name);
			if (mv == null)
				throw new IllegalArgumentException("No conjugated verb with name '"+name+"'.");
			return mv;
		}

		public String getForm(Tense tense, Form pronoun) {
			return tense.getMood().conjugate(this, tense, pronoun, false).toString();
		}
		
		public AuxiliaryVerb getAuxiliary() {
			return auxiliary;
		}
		
		public String toString() {
			return infinitive;
		}

		public ModelVerb getModelVerb() {
			return modelVerb;
		}

		public String getInfinitive() {
			return infinitive;
		}

		@Override
		public int hashCode() {
			return infinitive.toUpperCase().hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof ModelVerb))
				return false;
			
			return infinitive.equalsIgnoreCase(((ConjugatedVerb) obj).infinitive);
		}

		public int compareTo(ConjugatedVerb o) {
			return infinitive.compareTo(o.infinitive);
		}

		public boolean isReflexive() {
			return reflexive;
		}
	}
	
	public boolean equals(ModelVerb mv) {
		return this.name.equals(mv.name);
	}

	public String getName() {
		return name;
	}
	
	public String toString() {
		return name;
	}
	
	public String getSummary() {
		return summary;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((language == null) ? 0 : language.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof ModelVerb))
			return false;
		final ModelVerb other = (ModelVerb) obj;
		if (language == null) {
			if (other.language != null)
				return false;
		} else if (!language.equals(other.language))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}