package grammar.model.factory;

public interface Candidate {
	public boolean isCloserMatch(String infinitive, Candidate other);
	public boolean matches(String infinitive);
}