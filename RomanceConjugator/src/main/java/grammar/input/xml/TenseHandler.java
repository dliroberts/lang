package grammar.input.xml;

import java.util.HashSet;
import java.util.Set;

import org.xml.sax.Attributes;

import grammar.model.verbs.Mood;
import grammar.model.verbs.Tense;
import grammar.model.verbs.TenseTag;

public class TenseHandler extends DataHandler<Tense> {
	private Set<TenseTag> classifications = new HashSet<TenseTag>();
	private Tense tense;
	
	private String name;
	private Mood mood;
	private int sequence = -1;
	
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		if (qName.equals("name")) {
			enableParsing(ParseMode.NAME);
		}
		else if (qName.equals("mode")) {
			enableParsing(ParseMode.MOOD);
		}
		else if (qName.equals("tense")) {
			enableParsing(ParseMode.COMPOSITE);
		}
		else if (qName.equals("sequence")) {
			enableParsing(ParseMode.SEQUENCE);
		}
		else if (qName.equals("stem")) {
			enableParsing(ParseMode.STEM);
		}
		else if (qName.equals("infix")) {
			enableParsing(ParseMode.INFIX);
		}
		else if (qName.equals("suffix")) {
			enableParsing(ParseMode.SUFFIX);
		}
		else if (qName.equals("class")) {
			enableParsing(ParseMode.CLASSIFICATION);
		}
	}
	
	public void endElement(String uri, String localName, String qName) {
		String str = getParsedString();
		ParseMode mode = getParseMode();
		if (mode == ParseMode.NAME) {
			name = str;
		}
		else if (mode == ParseMode.MOOD) {
			mood = Mood.valueOf(str);
		}
		else if (mode == ParseMode.SEQUENCE) {
			sequence = Integer.parseInt(str);
		}
		else if (mode.equals(ParseMode.CLASSIFICATION)) {
			classifications.add(TenseTag.valueOf(str.toUpperCase().replace(' ', '_')));
		}
		disableParsing();
	}
	
	public void endDocument() {
		if (name == null)
			throw new IllegalStateException("No name defined for this tense!");
		if (mood == null)
			throw new IllegalStateException("No mood defined for this tense!");
		tense = new Tense(name, mood, sequence, classifications);
		for (TenseTag tc : classifications)
			tc.addMember(tense);
	}

	public Tense getParsedData() {
		return tense;
	}
}