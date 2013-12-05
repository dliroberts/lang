package grammar.input.xml;

import org.xml.sax.helpers.DefaultHandler;

public abstract class DataHandler<D> extends DefaultHandler {
	private ParseMode mode = ParseMode.OFF;
	private StringBuilder sb;
	
	public abstract D getParsedData();
	
	protected void enableParsing(ParseMode parseMode) {
		mode = parseMode;
		sb = new StringBuilder();
	}
	
	protected void disableParsing() {
		mode = ParseMode.OFF;
	}
	
	protected ParseMode getParseMode() {
		return mode;
	}
	
	protected String getParsedString() {
		if (sb == null)
			return null;
		
		return sb.toString();
	}

	public void characters(char[] ch, int start, int length) {
		characters(new String(ch).substring(start, start + length));
	}
	
	protected void characters(String s) {
		if (mode == ParseMode.OFF)
			return;
		sb.append(s);
	}
}