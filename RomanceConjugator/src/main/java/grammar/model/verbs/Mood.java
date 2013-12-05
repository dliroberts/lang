package grammar.model.verbs;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import grammar.input.xml.DataManager;
import grammar.model.Form;
import grammar.model.Language;
import grammar.model.MatchType;
import grammar.model.PseudoEnum;
import grammar.model.factory.ModelVerbFactory;
import grammar.model.verbs.ModelVerb.ConjugatedVerb;
import grammar.output.ModelVerbGraphViewer;
import grammar.util.Utilities;

public class Mood implements PseudoEnum<Mood> {
	private static final Logger LOGGER = LoggerFactory.getLogger(Mood.class);

	private static final Map<String, Mood> INSTANCES = new HashMap<String, Mood>();
	
	private final String name;
	private final SortedSet<Tense> tenses = new TreeSet<Tense>();
	private ModelVerbFactory modelVerbFactory;
	private final boolean suppressPronouns;
	private final boolean personal;
	private final int sequence;
	private final Language language;

	public Mood(String name, boolean suppressPronouns, boolean personal, int sequence) {
		this.name = name;
		this.suppressPronouns = suppressPronouns;
		this.personal = personal;
		this.sequence = sequence;
		language = DataManager.languageBeingLoaded();
		
		addMood(name, this);
	}
	
	public int ordinal() {
		return sequence;
	}
	
	public boolean isPersonal() {
		return personal;
	}

	public boolean suppressPronouns() {
		return suppressPronouns;
	}
	
	public Conjugation conjugate(ConjugatedVerb conjugatedVerb, Tense tense, Form pronoun, boolean question) {
		return new Conjugation(this, tense, pronoun,
				conjugatedVerb, conjugatedVerb.getModelVerb(),
				question,
				conjugateVerb(
						conjugatedVerb.getAuxiliary(),
						conjugatedVerb.getAuxiliary().getName().toLowerCase(),
						tense, pronoun, true, conjugatedVerb.isReflexive()),
				conjugateVerb(conjugatedVerb.getModelVerb(), conjugatedVerb.getVerb(), tense, pronoun, false, conjugatedVerb.isReflexive()));
	}

	public String[] conjugateVerb(ModelVerb modelVerb, String verb, Tense tense, Form pronoun, boolean auxiliary, boolean reflexive) {
		String prefix, stem, infix, suffix;
		
		prefix = modelVerb.getForm(tense, pronoun.getCategory(), Segment.PREFIX, auxiliary);
		stem   = modelVerb.getForm(tense, pronoun.getCategory(), Segment.STEM,   auxiliary);
		infix  = modelVerb.getForm(tense, pronoun.getCategory(), Segment.INFIX,  auxiliary);
		suffix = modelVerb.getForm(tense, pronoun.getCategory(), Segment.SUFFIX, auxiliary);
		
		if (auxiliary && suffix == null)
			return null;
		
		if (prefix == null)
			prefix = "";
		if (infix == null)
			infix = "";
		if (suffix == null)
			throw new ConjugationNotFoundException(tense, pronoun, verb);
		
		if (stem == null) {
			stem = verb.substring(0, verb.length()-2);
		}
		else {
			Pattern p1 = Pattern.compile(".*?(-?[0-9]+).*");
			Matcher m1 = p1.matcher(stem);
			
			if (modelVerb.getInfinitiveMatcher(verb, reflexive).getMatchType() == MatchType.PATTERN) {
				String regex = modelVerb.getInfinitiveMatcher(verb, reflexive).getMatchString();
				stem = modelVerb.getForm(tense, pronoun.getCategory(), Segment.STEM, auxiliary);
				
				Pattern p2 = Pattern.compile(regex);
				Matcher m2 = p2.matcher(verb);
				
				if (!m2.matches())
					throw new IllegalArgumentException("Infinitive doesn't match model verb pattern!");
				
				for (int i = 1; i <= m2.groupCount(); i++) {
					String g = m2.group(i);
					stem = stem.replace("\\"+i, g);
					if (infix != null)
						infix = infix.replace("\\"+i, g);
				}
			}
			if (m1.matches()) {
				String infinitiveSegment = m1.group(1);
				
				int stemSize = Integer.parseInt(infinitiveSegment);
				String replacement;
				if (stemSize > 0)
					replacement = verb.substring(0, stemSize);
				else if (stemSize < 0)
					replacement = verb.substring(0, verb.length()+stemSize);
				else // stemSize == 0
					replacement = verb;
				stem = stem.replace(infinitiveSegment, replacement);
			}
		}
		
		return new String[] {prefix, stem + (infix == null ? "" : infix) + suffix};
	}

	public int compareTo(Mood o) {
		return sequence - o.sequence;
	}
	
	public String toString() {
		return name;
	}
	
	public static Mood[] values() {
		return INSTANCES.values().toArray(new Mood[]{});
	}
	
	public static Mood valueOf(String key) {
		Mood m = INSTANCES.get(Utilities.asConstantName(key));
		if (m == null)
			throw new IllegalArgumentException("No such Mood: '"+key+"'.");
		return m;
	}
	
	private static void addMood(String key, Mood mood) {
		INSTANCES.put(Utilities.asConstantName(key), mood);
	}
	
	public String getName() {
		return name;
	}
	
	public SortedSet<Tense> getTenses() {
		return tenses;
	}
	
	public void addTense(Tense tense) {
		tenses.add(tense);
	}
	
	public Conjugation conjugate(ConjugatedVerb conjugatedVerb, Tense tense, Form pronoun, boolean question, boolean reflexive) {
		if (modelVerbFactory == null)
			modelVerbFactory = ModelVerbFactory.getInstance();
		
		//ConjugatedVerb conjugatedVerb = modelVerbFactory.getModelVerb(verb, language, reflexive).getConjugatedVerb(verb, reflexive);

		LOGGER.debug("AUX2:"+reflexive+conjugatedVerb.getAuxiliary());
		
		return conjugate(conjugatedVerb, tense, pronoun, question);
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
		if (!(obj instanceof Mood))
			return false;
		final Mood other = (Mood) obj;
		if (sequence != other.sequence)
			return false;
		return true;
	}
}