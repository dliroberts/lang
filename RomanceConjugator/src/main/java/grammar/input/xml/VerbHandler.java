package grammar.input.xml;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.xml.sax.Attributes;
import grammar.model.factory.ModelVerbFactory;
import grammar.model.verbs.VerbTag;
import grammar.model.verbs.ModelVerb.ConjugatedVerb;

/**
 * Loads a verb 'instance', rather than a model verb.
 * 
 * @author Duncan Roberts
 */
public class VerbHandler extends DataHandler<List<ConjugatedVerb>> {
	private List<ConjugatedVerb> verbs = new ArrayList<ConjugatedVerb>();
	
	private String infinitive;
	private Set<VerbTag> classifications;
	
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		if (qName.equals("infinitive")) {
			enableParsing(ParseMode.NAME);
		}
		else if (qName.equals("verb")) {
			infinitive = null;
			classifications = new HashSet<VerbTag>();
			classifications.add(VerbTag.ALL);
		}
		else if (qName.equals("class")) {
			enableParsing(ParseMode.CLASSIFICATION);
		}
	}
	
	public void endElement(String uri, String localName, String qName) {
		String str = getParsedString();
		ParseMode mode = getParseMode();
		if (mode.equals(ParseMode.NAME)) {
			infinitive = str;
			disableParsing();
		}
		else if (qName.equals("verb")) {
			if (infinitive == null)
				throw new IllegalStateException("No name defined for this verb!");
			ModelVerbFactory mvf = ModelVerbFactory.getInstance();
			verbs.add(mvf.getModelVerb(infinitive, DataManager.languageBeingLoaded())
					.getConjugatedVerb(infinitive, classifications));
		}
		else if (mode.equals(ParseMode.CLASSIFICATION)) {
			classifications.add(VerbTag.valueOf(str.toUpperCase().replace(' ', '_')));
		}
		disableParsing();
	}
	
	public void endDocument() {
		for (ConjugatedVerb cv : verbs)
			for (VerbTag tc : cv.getClassifications())
				tc.addMember(cv);
	}

	public List<ConjugatedVerb> getParsedData() {
		return verbs;
	}
}