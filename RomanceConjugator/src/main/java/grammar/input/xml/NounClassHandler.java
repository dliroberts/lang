package grammar.input.xml;

import java.util.ArrayList;
import java.util.List;
import org.xml.sax.Attributes;
import grammar.model.MatchType;
import grammar.model.WordMatcher;
import grammar.model.nouns.NounClass;
import grammar.util.Utilities;

public class NounClassHandler extends DataHandler<NounClass> {
	private int sequence = -1;
	private NounClass nounClass;
	private String name;
	private String abbreviation;
	private final List<WordMatcher> nounMatchers = new ArrayList<WordMatcher>();
	
	private MatchType matchType;
	
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		if (qName.equals("name")) {
			enableParsing(ParseMode.NAME);
		}
		else if (qName.equals("abbreviation")) {
			enableParsing(ParseMode.ABBREVIATION);
		}
		else if (qName.equals("sequence")) {
			enableParsing(ParseMode.SEQUENCE);
		}
		else if (qName.equals("suppress-pronouns")) {
			//suppressPronouns = true;
		}
		else {
			try {
				matchType = MatchType.valueOf(Utilities.asConstantName(qName));
				enableParsing(ParseMode.RULE);
			}
			catch (Exception e) {
				// legitimate; do nothing
			}
		}
	}
	
	public void endElement(String uri, String localName, String qName) {
		String str = getParsedString();
		ParseMode mode = getParseMode();
		
		if (qName.equals("name")) {
			name = str;
		}
		else if (mode.equals(ParseMode.ABBREVIATION)) {
			abbreviation = str;
		}
		else if (mode.equals(ParseMode.SEQUENCE)) {
			sequence = Integer.parseInt(str);
		}
		else if (mode.equals(ParseMode.RULE)) {
			nounMatchers.add(new WordMatcher(matchType, str));
		}
		disableParsing();
	}
	
	public void endDocument() {
		if (name == null)
			throw new IllegalStateException("No name defined for this mode!");
				
		nounClass = new NounClass(sequence, name, abbreviation, nounMatchers);
	}
	
	public NounClass getParsedData() {
		return nounClass;
	}
}