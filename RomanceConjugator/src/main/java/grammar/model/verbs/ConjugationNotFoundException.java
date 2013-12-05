package grammar.model.verbs;

import grammar.model.Form;

public class ConjugationNotFoundException extends RuntimeException {
	private static final long serialVersionUID = 7584948619026010620L;
	
	private Tense tense;
	private Form pronoun;
	private Segment segment;
	private String infinitive;
	
	public ConjugationNotFoundException(Tense tense, Form pronoun, String infinitive) {
		this.tense = tense;
		this.pronoun = pronoun;
		this.infinitive = infinitive;
		this.segment = null; // unknown
	}
	
	public ConjugationNotFoundException(Tense tense, Form pronoun, String infinitive, Segment segment) {
		this(tense, pronoun, infinitive);
		this.segment = segment;
	}
	
	public String getMessage() {
		return "Conjugation rules incomplete for verb "+infinitive+
		". Cannot find "+(segment == null ? "rules" : segment.toString())+
		" for tense "+tense+" and pronoun "+pronoun+".";
	}
}