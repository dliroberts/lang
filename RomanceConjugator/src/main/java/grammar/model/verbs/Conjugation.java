package grammar.model.verbs;

import grammar.model.Form;
import grammar.model.Multiplicity;
import grammar.model.PersonalPronoun;
import grammar.model.PersonalPronounCategory;
import grammar.model.PersonalPronounRole;
import grammar.model.SubjectGender;
import grammar.model.verbs.ModelVerb.ConjugatedVerb;

public class Conjugation {
	private final Mood mood;
	private final Tense tense;
	private final Form form;
	private final ConjugatedVerb conjugatedVerb;
	private final ModelVerb modelVerb;
	private final boolean question;
	private final boolean suppressPronouns;
	private final String[] conjugatedAuxiliaryStr; // TODO should just be 2 strings
	private final String[] conjugatedVerbStr; // TODO should just be 2 strings
	
	public Conjugation(Mood mood, Tense tense, Form pronoun,
			ConjugatedVerb conjugatedVerb, ModelVerb modelVerb,
			boolean question, String[] conjugatedAuxiliaryStr,
			String[] conjugatedVerbStr) {
		this.mood = mood;
		this.tense = tense;
		this.form = pronoun;
		this.conjugatedVerb = conjugatedVerb;
		this.modelVerb = modelVerb;
		this.question = question;
		this.conjugatedAuxiliaryStr = conjugatedAuxiliaryStr;
		this.conjugatedVerbStr = conjugatedVerbStr;
		if (mood.isPersonal())
			suppressPronouns = mood.suppressPronouns();
		else
			suppressPronouns = false;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		if (conjugatedAuxiliaryStr != null) {
			sb.append(conjugatedAuxiliaryStr[1]);
			sb.append(' ');
		}
		if (question && !suppressPronouns && mood.isPersonal()) {
			sb.append(form);
			sb.append(' ');
		}
		sb.append(conjugatedVerbStr[1]);
		
		if (!question && !suppressPronouns && mood.isPersonal()) {
			if (conjugatedVerb.isReflexive()) {
				if (form instanceof PersonalPronoun) {
					PersonalPronounCategory ppc = ((PersonalPronoun) form).getCategory();
					PersonalPronoun f = ppc.getForms(PersonalPronounRole.REFLEXIVE)[0];
					
					String s = f.addPronoun(sb.toString());
					sb = new StringBuilder();
					sb.append(s);
				}
			}
			
			String s = ((PersonalPronoun) form).addPronoun(sb.toString());
			sb = new StringBuilder();
			sb.append(s);
		}
		
		if (conjugatedVerb.getAuxiliary().pronounAgreement() && conjugatedAuxiliaryStr != null &&
				conjugatedAuxiliaryStr[1] != null && form instanceof PersonalPronoun) {
			PersonalPronoun p = (PersonalPronoun) form;
			if (p.getGender() != null && p.getGender().equals(SubjectGender.valueOf("FEMININE")))
				sb.append('e'); // TODO configure in some xml
			if (p.getMultiplicity().equals(Multiplicity.PLURAL))
				sb.append('s'); // TODO configure in some xml
		}
		
		return conjugatedVerbStr[0] + (conjugatedAuxiliaryStr == null? "" : conjugatedAuxiliaryStr[0]) + sb.toString();
	}

	public Mood getMode() {
		return mood;
	}

	public Tense getTense() {
		return tense;
	}

	public Form getPronoun() {
		return form;
	}

	public ConjugatedVerb getConjugatedVerb() {
		return conjugatedVerb;
	}

	public ModelVerb getModelVerb() {
		return modelVerb;
	}

	public boolean isQuestion() {
		return question;
	}

	public String getConjugatedAuxiliaryStr() {
		return conjugatedAuxiliaryStr[1];
	}

	public String getConjugatedVerbStr() {
		return conjugatedVerbStr[1];
	}
	
	public String getPrefix() {
		return conjugatedVerbStr[0];
	}
}