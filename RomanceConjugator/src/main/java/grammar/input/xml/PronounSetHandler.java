package grammar.input.xml;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.xml.sax.Attributes;

import grammar.model.PersonalPronounCategory;

public class PronounSetHandler extends DataHandler<Map.Entry<String, Set<PersonalPronounCategory>>> {
	private final SortedSet<PersonalPronounCategory> pronounSet = new TreeSet<PersonalPronounCategory>();
	private ParseMode mode = ParseMode.OFF;
	private StringBuilder sb;
	private String multiplicity = null;
	private String name;
	
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		if (qName.equals("name")) {
			mode = ParseMode.NAME;
			sb = new StringBuilder();
		}
		else if (qName.matches("(singular|plural)")) {
			multiplicity = qName;
		}
		else if (qName.equals("all")) {
			mode = ParseMode.RULE;
			sb = new StringBuilder();
			if (multiplicity == null) {
				pronounSet.add(PersonalPronounCategory.FIRST_PERSON_SINGULAR);
				pronounSet.add(PersonalPronounCategory.SECOND_PERSON_SINGULAR);
				pronounSet.add(PersonalPronounCategory.THIRD_PERSON_SINGULAR);
				pronounSet.add(PersonalPronounCategory.FIRST_PERSON_PLURAL);
				pronounSet.add(PersonalPronounCategory.SECOND_PERSON_PLURAL);
				pronounSet.add(PersonalPronounCategory.THIRD_PERSON_PLURAL);
			}
			else if (multiplicity.equals("singular")) {
				pronounSet.add(PersonalPronounCategory.FIRST_PERSON_SINGULAR);
				pronounSet.add(PersonalPronounCategory.SECOND_PERSON_SINGULAR);
				pronounSet.add(PersonalPronounCategory.THIRD_PERSON_SINGULAR);
			}
			else if (multiplicity.equals("plural")) {
				pronounSet.add(PersonalPronounCategory.FIRST_PERSON_PLURAL);
				pronounSet.add(PersonalPronounCategory.SECOND_PERSON_PLURAL);
				pronounSet.add(PersonalPronounCategory.THIRD_PERSON_PLURAL);
			}
			else
				throw new IllegalStateException();
		}
		else if (qName.equals("first")) {
			mode = ParseMode.RULE;
			sb = new StringBuilder();
			if (multiplicity.equals("singular"))
				pronounSet.add(PersonalPronounCategory.FIRST_PERSON_SINGULAR);
			else if (multiplicity.equals("plural"))
				pronounSet.add(PersonalPronounCategory.FIRST_PERSON_PLURAL);
			else
				throw new IllegalStateException();
		}
		else if (qName.equals("second")) {
			mode = ParseMode.RULE;
			sb = new StringBuilder();
			if (multiplicity.equals("singular"))
				pronounSet.add(PersonalPronounCategory.SECOND_PERSON_SINGULAR);
			else if (multiplicity.equals("plural"))
				pronounSet.add(PersonalPronounCategory.SECOND_PERSON_PLURAL);
			else
				throw new IllegalStateException();
		}
		else if (qName.equals("third")) {
			mode = ParseMode.RULE;
			sb = new StringBuilder();
			if (multiplicity.equals("singular"))
				pronounSet.add(PersonalPronounCategory.THIRD_PERSON_SINGULAR);
			else if (multiplicity.equals("plural"))
				pronounSet.add(PersonalPronounCategory.THIRD_PERSON_PLURAL);
			else
				throw new IllegalStateException();
		}
	}
	
	public void endElement(String uri, String localName, String qName) {
		if (qName.equals("name")) {
			mode = ParseMode.OFF;
			name = sb.toString();
		}
		else if (qName.matches("(singular|plural)")) {
			multiplicity = null;
		}
	}
	
	public void characters(char[] ch, int start, int length) {
		if (mode == ParseMode.OFF)
			return;
		String s = new String(ch).substring(start, start + length);
		sb.append(s);
	}
	
	public void endDocument() {
		if (name == null)
			throw new IllegalStateException("No name defined for this pronoun set!");
		PersonalPronounCategory.addPronounSet(name, pronounSet);
	}
	
	public Map.Entry<String, Set<PersonalPronounCategory>> getParsedData() {
		return new MapEntryImpl<String, Set<PersonalPronounCategory>>(name, pronounSet);
	}
}

class MapEntryImpl<K,V> implements Map.Entry<K,V> {
	private K key;
	private V value;
	
	public MapEntryImpl(K key, V value) {
		this.key = key;
		this.value = value;
	}

	public K getKey() {
		return key;
	}
	
	public V getValue() {
		return value;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}
	
	public V setValue(V value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof MapEntryImpl))
			return false;
		MapEntryImpl<?,?> other = (MapEntryImpl<?,?>) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
}