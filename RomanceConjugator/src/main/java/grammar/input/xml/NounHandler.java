package grammar.input.xml;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.xml.sax.Attributes;
import grammar.model.Multiplicity;
import grammar.model.nouns.Noun;
import grammar.model.nouns.NounClass;
import grammar.model.nouns.NounForm;
import grammar.model.nouns.NounTag;
import grammar.util.Utilities;

public class NounHandler extends DataHandler<List<Noun>> {
	private List<Noun> nouns = new ArrayList<Noun>();
	
	private Set<NounForm> forms;
	private Set<NounTag> classifications;
	
	private Multiplicity affectedMultiplicity = Multiplicity.SINGULAR;
	private List<NounClass> affectedNounClasses;
	
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		if (qName.equals("text")) {
			enableParsing(ParseMode.NAME);
		}
		else if (qName.equals("noun-class")) {
			enableParsing(ParseMode.NOUN_CLASS);
		}
		else if (qName.equals("noun")) {i++;
			affectedNounClasses = new ArrayList<NounClass>();
			forms = new HashSet<NounForm>();
			classifications = new HashSet<NounTag>();
			classifications.add(NounTag.ALL);
		}
		else if (qName.equals("class")) {
			enableParsing(ParseMode.CLASSIFICATION);
		}
		else {
			try {
				affectedMultiplicity = Multiplicity.valueOf(Utilities.asConstantName(qName));
				return;
			}
			catch (Exception e) {
				// legitimate; do nothing
			}
		}
	}
	
	public void endElement(String uri, String localName, String qName) {
		String str = getParsedString();
		ParseMode mode = getParseMode();
		if (mode.equals(ParseMode.NAME)) {
			forms.add(new NounForm(affectedMultiplicity, affectedNounClasses, str));
			affectedMultiplicity = Multiplicity.SINGULAR; // reset to default
			affectedNounClasses = new ArrayList<NounClass>();
		}
		else if (mode.equals(ParseMode.NOUN_CLASS)) {
			affectedNounClasses.add(NounClass.valueOf(str));
		}
		else if (mode.equals(ParseMode.CLASSIFICATION)) {
			classifications.add(NounTag.valueOf(Utilities.asConstantName(str)));
		}
		else if (qName.equals("noun")) {
			nouns.add(new Noun(forms, classifications));
		}
		disableParsing();
	}
	
	public void endDocument() {
		for (Noun n : nouns)
			for (NounTag tc : n.getClassifications())
				tc.addMember(n);
	}

	public List<Noun> getParsedData() {
		return nouns;
	}
	int i = 0;
}