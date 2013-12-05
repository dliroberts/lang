package grammar.input.xml;

import java.util.ArrayList;
import java.util.List;
import org.xml.sax.Attributes;
import grammar.model.Language;

public class LanguageHandler extends DataHandler<List<Language>> {
	private List<Language> languages = new ArrayList<Language>();
	
	private String identifier;
	private String nativeName;
	private String reflexiveVerbInfinitivePattern;
	
	private boolean nativeContext;
	
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		if (qName.equals("identifier")) {
			enableParsing(ParseMode.NAME);
			nativeContext = false;
		}
		else if (qName.equals("native-name")) {
			enableParsing(ParseMode.NAME);
			nativeContext = true;
		}
		else if (qName.equals("pattern")) {
			enableParsing(ParseMode.PATTERN);
		}
	}
	
	public void endElement(String uri, String localName, String qName) {
		String str = getParsedString();
		ParseMode mode = getParseMode();
		if (mode.equals(ParseMode.NAME) && !nativeContext) {
			identifier = str;
		}
		else if (mode.equals(ParseMode.NAME) && nativeContext) {
			nativeName = str;
		}
		else if (mode.equals(ParseMode.PATTERN)) {
			reflexiveVerbInfinitivePattern = str;
		}
		else if (qName.equals("language")) {
			if (identifier == null)
				throw new IllegalStateException("Language's identifier not defined.");
			if (nativeName == null)
				throw new IllegalStateException("Language's native name not defined.");
			
			languages.add(new Language(identifier, nativeName, reflexiveVerbInfinitivePattern));
		}
		disableParsing();
	}

	public List<Language> getParsedData() {
		return languages;
	}
}