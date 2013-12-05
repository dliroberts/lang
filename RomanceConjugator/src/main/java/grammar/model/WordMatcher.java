/**
 * 
 */
package grammar.model;


public class WordMatcher {
	private MatchType matchType;
	private String matchString;
	
	public WordMatcher(MatchType matchType, String matchString) {
		this.matchType = matchType;
		this.matchString = matchString;
	}
	
	public String toString() {
		return "Type: "+matchType+"; string: "+matchString;
	}

	public MatchType getMatchType() {
		return matchType;
	}

	public String getMatchString() {
		return matchString;
	}
}