package grammar.input.xml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.xml.sax.Attributes;
import grammar.model.MatchType;
import grammar.model.Multiplicity;
import grammar.model.Person;
import grammar.model.PersonalPronounCategory;
import grammar.model.WordMatcher;
import grammar.model.Form.FormCategory;
import grammar.model.ImpersonalForm.ImpersonalFormCategory;
import grammar.model.verbs.AuxiliaryVerb;
import grammar.model.verbs.ModelVerb;
import grammar.model.verbs.Mood;
import grammar.model.verbs.Segment;
import grammar.model.verbs.Tense;
import grammar.util.Utilities;

class ModelVerbHandler extends DataHandler<ModelVerb> {
	private ModelVerb parsedVerb;
	
	private boolean auxiliaryVerb = false;
	private Map<Tense, Map<FormCategory, Map<Segment, String>>> tenseMap =
		new HashMap<Tense, Map<FormCategory, Map<Segment, String>>>();
	
	private Map<Tense, Map<FormCategory, Map<Segment, Tense>>> refTenseMap =
		new HashMap<Tense, Map<FormCategory, Map<Segment, Tense>>>();
	private Map<Tense, Map<FormCategory, Map<Segment, FormCategory>>> refPronounMap =
		new HashMap<Tense, Map<FormCategory, Map<Segment, FormCategory>>>();
	private Map<Tense, Map<FormCategory, Map<Segment, Tense>>> auxiliaryRefTenseMap =
		new HashMap<Tense, Map<FormCategory, Map<Segment, Tense>>>();
	private Map<Tense, Map<FormCategory, Map<Segment, FormCategory>>> auxiliaryRefPronounMap =
		new HashMap<Tense, Map<FormCategory, Map<Segment, FormCategory>>>();
	private MatchType auxiliaryMatchType;
	private List<WordMatcher> infinitiveMatchers = new ArrayList<WordMatcher>();
	private boolean pronounAgreement = false;
	private boolean reflexiveAuxiliary = false;
	private String name;
	private String summary;
	private MatchType matchType = MatchType.NONE;
	private final List<WordMatcher> matchTypes = new ArrayList<WordMatcher>();
	private final List<ModelVerb> parents = new ArrayList<ModelVerb>();
	
	// Scope of rule
	private final List<Mood> affectedMoods               = new ArrayList<Mood>();
	private final List<Tense> affectedTenses             = new ArrayList<Tense>();
	private final List<FormCategory> affectedPronouns    = new ArrayList<FormCategory>();
	private final List<Segment> affectedSegments         = new ArrayList<Segment>();
	private Multiplicity affectedMultiplicity            = null;
	
	// Target of rule references
	private Tense targetTense = null;
	
	// Context
	private boolean infinitiveContext    = false;
	private boolean auxiliaryOfContext   = false;
	private boolean ruleContext          = false;
	private boolean targetContext        = false;
	private boolean formContext          = false;
	private boolean auxiliaryFormContext = false;
	private boolean allFormsContext      = false;
	
	private boolean inheritTense = false;
	private boolean inheritForm  = false;
	
	int ruleNum = 0;

	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		if (qName.equals("name")) {
			enableParsing(ParseMode.NAME);
		}
		else if (qName.equals("infinitive")) {
			infinitiveContext = true;
		}
		else if (qName.equals("target")) {
			targetContext = true;
		}
		else if (qName.equals("forms")) {
			formContext = true;
			for (Tense t : affectedTenses) {
				Utilities.initialiseIfReqd(t, refTenseMap);
				Utilities.initialiseIfReqd(t, refPronounMap);
			}
		}
		else if (qName.equals("auxiliary-forms")) {
			auxiliaryFormContext = true;
			for (Tense t : affectedTenses) {
				Utilities.initialiseIfReqd(t, auxiliaryRefTenseMap);
				Utilities.initialiseIfReqd(t, auxiliaryRefPronounMap);
			}
		}
		else if (qName.equals("segment")) {
			enableParsing(ParseMode.SEGMENT);
		}
		else if (qName.equals("auxiliary-pronoun-agreement")) {
			pronounAgreement = true;
		}
		else if (qName.equals("parent") && !ruleContext) {
			enableParsing(ParseMode.PARENT);
		}
		else if (qName.equals("summary")) {
			enableParsing(ParseMode.SUMMARY);
		}
		else if (qName.equals("auxiliary-of")) {
			auxiliaryVerb = true;
			auxiliaryOfContext = true;
		}
		else if (qName.equals("rule")) {
			ruleContext = true;
			affectedTenses.clear();
			affectedMoods.clear();
			affectedPronouns.clear();
			affectedSegments.clear();
			affectedMultiplicity = null;
			inheritForm = false;
			inheritTense = false;
		}
		else if (qName.equals("tense")) {
			enableParsing(ParseMode.TENSE);
		}
		else if (qName.equals("mood")) {
			enableParsing(ParseMode.MOOD);
		}
		else if (infinitiveContext || auxiliaryOfContext) {
			try {
				MatchType mt = MatchType.valueOf(qName.toUpperCase().replace('-', '_'));
				enableParsing(ParseMode.MATCH_TYPE);
				if (infinitiveContext)
					matchType = mt;
				else // auxiliaryOfContext
					auxiliaryMatchType = mt;
				return;
			}
			catch (Exception e) {
				// Legitimate; do nothing
			}
		}
		else if (qName.equals("all") && !auxiliaryOfContext) {
			affectedPronouns.clear();
			enableParsing(ParseMode.RULE);
			if (formContext || auxiliaryFormContext || affectedMultiplicity != null)
				affectedPronouns.addAll(PersonalPronounCategory.getPronounSet(affectedMultiplicity, null));
			else
				allFormsContext = true;
		}
		else {
			try {
				if (qName.equals("parent")) {
					return;
				}
				if (affectedTenses.get(0).getMood().isPersonal()) {
					Person person = Person.valueOf(qName.toUpperCase().replace(' ', '_'));
					affectedPronouns.clear();
					enableParsing(ParseMode.RULE);
					affectedPronouns.addAll(PersonalPronounCategory.getPronounSet(affectedMultiplicity, person));
				}
				else { // impersonal
					affectedPronouns.clear();
					enableParsing(ParseMode.RULE);
					affectedPronouns.add(new ImpersonalFormCategory(qName.toUpperCase().replace(' ', '_')));
				}
				//return;
			}
			catch (Exception e) {
				// Legitimate; do nothing
			}
			try {
				affectedMultiplicity = Multiplicity.valueOf(qName.toUpperCase());
				return;
			}
			catch (Exception e) {
				// legitimate; do nothing
			}
			try {
				Segment seg = Segment.valueOf(qName.toUpperCase());
				affectedSegments.clear();
				affectedSegments.add(seg);
			}
			catch (IllegalArgumentException iae) {
				// legitimate; do nothing.
			}
			
			try {
				Set<PersonalPronounCategory> set = PersonalPronounCategory.getPronounSet(qName);
				affectedPronouns.clear();
				enableParsing(ParseMode.RULE);
				affectedPronouns.addAll(set);
			}
			catch (Exception e) {
				// not a pronoun set
			}
		}
	}
	
	public void endElement(String uri, String localName, String qName) {
		String parsedString = getParsedString();
		String parsedStringAsConstant = parsedString.toUpperCase().replace(' ', '_');
		ParseMode parseMode = getParseMode();
		
		if (qName.equals("reflexive-verbs")) {
			reflexiveAuxiliary = true;
		}
		else if (qName.equals("infinitive")) {
			infinitiveContext = false;
		}
		else if (qName.equals("target")) {
			targetContext = false;
		}
		else if (qName.equals("forms")) {
			formContext = false;
		}
		else if (qName.equals("auxiliary-forms")) {
			auxiliaryFormContext = false;
		}
		else if (qName.equals("auxiliary-of")) {
			auxiliaryOfContext = false;
		}
		else if (qName.equals("rule")) {
			ruleContext = false;
			targetTense = null;
			affectedTenses.clear();
		}
		else if (parseMode == ParseMode.NAME) {
			disableParsing();
			name = parsedString;
		}
		else if (parseMode == ParseMode.SUMMARY) {
			disableParsing();
			summary = parsedString;
		}
		else if (parseMode == ParseMode.SEGMENT) {
			disableParsing();
			affectedSegments.add(Segment.valueOf(parsedStringAsConstant));
		}
		else if (parseMode == ParseMode.MATCH_TYPE) {
			disableParsing();
			if (infinitiveContext)
				matchTypes.add(new WordMatcher(matchType, parsedString));
			else if (auxiliaryOfContext)
				infinitiveMatchers.add(new WordMatcher(auxiliaryMatchType, parsedString));
		}
		else if (parseMode == ParseMode.PARENT) {
			disableParsing();
			try {
				ModelVerb p = ModelVerb.valueOf(parsedString);
				parents.add(p);
			}
			catch (Exception e) {
				throw new IllegalLoadOrderException("Model verb "+name+" must be loaded after model verb "+parsedString+".");
			}
		}
		else if (qName.equals("parent") && ruleContext) {
			if (targetContext)
				inheritTense = true;
			else if (formContext)
				inheritForm = true;
			else
				characters("<parent />");
		}
		else if (parseMode == ParseMode.MOOD) {
			disableParsing();
			if (parsedString.equals("all")) {
				affectedMoods.addAll(Arrays.asList(Mood.values()));
				affectedTenses.addAll(Arrays.asList(Tense.values()));
			}
			else {
				affectedMoods.add(Mood.valueOf(parsedStringAsConstant));
				affectedTenses.addAll(Mood.valueOf(parsedStringAsConstant).getTenses());
			}
		}
		else if (parseMode == ParseMode.TENSE) {
			disableParsing();
			if (parsedString.equals("all")) {
				affectedTenses.addAll(Arrays.asList(Tense.values()));
			}
			else if (parsedString.startsWith("@")) {
				parsedString = parsedString.substring(1); // Chop off @
				affectedTenses.addAll(Tense.getSet(parsedString));
			}
			else {
				if (targetContext && inheritTense) {
					return;
				}
				else {
					Tense t = Tense.valueOf(parsedStringAsConstant);
					
					if (targetContext)
						targetTense = t;
					else
						affectedTenses.add(t);
				}
			}
		}
		else if (parseMode == ParseMode.RULE && !qName.equals("parent")) {
			disableParsing();
			for (Tense affectedTense : affectedTenses) {
				List<FormCategory> forms = affectedPronouns;
				if (allFormsContext) {
					Map<FormCategory, Map<Segment, Tense>> m = refTenseMap.get(affectedTense);
					Set<FormCategory> s;
					if (m == null) {
						s = new HashSet<FormCategory>(Arrays.asList(parents.get(0).getForms(affectedTense)));
					}
					else
						s = m.keySet();
					forms = new ArrayList<FormCategory>(s);
				}
				
				for (FormCategory affectedPronoun : forms) {
					if (formContext || auxiliaryFormContext) {
						if (affectedSegments.size() == 0)
							affectedSegments.addAll(Arrays.asList(Segment.values()));
						
						FormCategory targetPronoun;
						if (inheritForm)
							targetPronoun = null; // no rule; refer to parent
						else if (parsedStringAsConstant.equals(""))
							targetPronoun = affectedPronoun;
						else if (targetTense != null && targetTense.getMood().isPersonal())
							targetPronoun = PersonalPronounCategory.valueOf(parsedStringAsConstant);
						else
							targetPronoun = new ImpersonalFormCategory(parsedStringAsConstant);
						
						Tense targetTense2;
						if (inheritTense)
							targetTense2 = null; // no rule; refer to parent
						else if (targetTense == null)
							targetTense2 = affectedTense;
						else 
							targetTense2 = targetTense;
						
						for (Segment affectedSegment : affectedSegments) {
							Map<FormCategory, Map<Segment, Tense>> pcMap =
								Utilities.initialiseIfReqd(affectedTense,   formContext ? refTenseMap   : auxiliaryRefTenseMap);
							Map<FormCategory, Map<Segment, FormCategory>> pcMap2 =
								Utilities.initialiseIfReqd(affectedTense,   formContext ? refPronounMap : auxiliaryRefPronounMap);
							Map<Segment, Tense> segMap =
								Utilities.initialiseIfReqd(affectedPronoun, pcMap);
							Map<Segment, FormCategory> segMap2 =
								Utilities.initialiseIfReqd(affectedPronoun, pcMap2);
							
							segMap.put(affectedSegment, targetTense2);
							segMap2.put(affectedSegment, targetPronoun);
						}
					}
					else {
						Map<FormCategory, Map<Segment, String>> pronounMap =
							Utilities.initialiseIfReqd(affectedTense,   tenseMap);
						Map<Segment, String> segmentMap =
							Utilities.initialiseIfReqd(affectedPronoun, pronounMap);
						
						String rule = parsedString;
						segmentMap.put(affectedSegments.get(0), rule);
						tenseMap.put(affectedTense, pronounMap);
					}
				}
			}
		}
		else {
			try {
				PersonalPronounCategory.getPronounSet(qName);
				affectedPronouns.clear();
			}
			catch (Exception e) {
				// not a pronoun set
			}
			try {
				Multiplicity.valueOf(qName);
				affectedMultiplicity = null;
			}
			catch (Exception e) {
				// not a pronoun set
			}
			try {
				Segment.valueOf(qName.toUpperCase());
				affectedSegments.clear();
			}
			catch (Exception e) {
				// legitimate; do nothing.
			}
		}
		if (!qName.equals("parent"))
			allFormsContext = false;
	}
	
	public void endDocument() {
		if (name == null)
			throw new IllegalStateException("No name defined for this model verb!");
		
		if (auxiliaryVerb) {
			parsedVerb = new AuxiliaryVerb(name, parents,
					matchTypes,
					tenseMap, refTenseMap, refPronounMap,
					auxiliaryRefTenseMap, auxiliaryRefPronounMap,
					infinitiveMatchers, summary, pronounAgreement, reflexiveAuxiliary,
					DataManager.languageBeingLoaded());
		}
		else {
			parsedVerb = new ModelVerb(name, parents,
					matchTypes,
					tenseMap, refTenseMap, refPronounMap,
					auxiliaryRefTenseMap, auxiliaryRefPronounMap, summary,
					DataManager.languageBeingLoaded());
		}
	}
	
	public ModelVerb getParsedData() {
		return parsedVerb;
	}
}