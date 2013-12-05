package grammar.model.factory;

import grammar.model.MatchType;
import grammar.model.WordMatcher;

public abstract class AbstractCandidate implements Candidate {

	public abstract WordMatcher getWordMatcher(String text);
	public abstract boolean isCloserMatch(String infinitive, Candidate other);

	public boolean matches(String infinitive) {
		try {
			getWordMatcher(infinitive);
			return true;
		}
		catch (IllegalArgumentException e) {
			return false;
		}
	}
	
	protected int getMatchStringPointsValue(WordMatcher infinitiveMatcher) {
		if (infinitiveMatcher.getMatchType() == MatchType.PATTERN) {
			String points = infinitiveMatcher.getMatchString().replaceAll("\\[.+\\]", "[");
			points = points.replaceAll("\\(\\.\\*\\)", "");
			points = points.replaceAll("[()+*]", "");
			return points.length();
		}
		else if (infinitiveMatcher.getMatchType() == MatchType.SUFFIX) {
			return infinitiveMatcher.getMatchString().length();
		}
		else throw new Error();
	}
}