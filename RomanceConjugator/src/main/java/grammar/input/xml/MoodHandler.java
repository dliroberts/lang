package grammar.input.xml;

import org.xml.sax.Attributes;
import grammar.model.verbs.Mood;

public class MoodHandler extends DataHandler<Mood> {
	private int sequence = -1;
	private Mood mood;
	private ParseMode mode = ParseMode.OFF;
	private StringBuilder sb;
	private String name;
	private boolean personal = true;
	private boolean suppressPronouns = false;
	
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		if (qName.equals("name")) {
			mode = ParseMode.NAME;
			sb = new StringBuilder();
		}
		else if (qName.equals("impersonal")) {
			personal = false;
		}
		else if (qName.equals("sequence")) {
			mode = ParseMode.SEQUENCE;
		}
		else if (qName.equals("suppress-pronouns")) {
			suppressPronouns = true;
		}
	}
	
	public void endElement(String uri, String localName, String qName) {
		if (qName.equals("name")) {
			name = sb.toString();
		}
		else if (mode.equals(ParseMode.SEQUENCE)) {
			sequence = Integer.parseInt(sb.toString());
		}
		mode = ParseMode.OFF;
		sb = new StringBuilder();
	}
	
	public void characters(char[] ch, int start, int length) {
		if (mode == ParseMode.OFF)
			return;
		String s = new String(ch).substring(start, start + length);
		sb.append(s);
	}
	
	public void endDocument() {
		if (name == null)
			throw new IllegalStateException("No name defined for this mode!");
				
		mood = new Mood(name, suppressPronouns, personal, sequence);
	}
	
	public Mood getParsedData() {
		return mood;
	}
}