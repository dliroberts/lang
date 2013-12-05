package grammar.model;

import java.util.Set;

public interface Tag<T> {
	public Set<T> getMembers();
	public void addMember(T member);
}