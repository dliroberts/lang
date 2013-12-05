package grammar.model;

import grammar.util.Utilities;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Language implements PseudoEnum<Language> {
	private static final Map<String, Language> INSTANCE_MAP = new HashMap<String, Language>();
	private static final Set<Language> INSTANCES = new HashSet<Language>();
	
	private static int sequenceGenerator = 0;
	
	private final int sequence;
	private final String identifier;
	private final String nativeName;
	private final Pattern reflexiveVerbInfinitiveCompiledPattern;
	
	public Language(String identifier, String nativeName, String reflexiveVerbInfinitivePattern) {
		this.identifier = identifier;
		this.nativeName = nativeName;
		reflexiveVerbInfinitiveCompiledPattern = Pattern.compile(reflexiveVerbInfinitivePattern);
		
		sequence = sequenceGenerator++;
		INSTANCES.add(this);
		INSTANCE_MAP.put(Utilities.asConstantName(identifier), this);
	}

	public boolean isReflexive(String infinitive) {
		return reflexiveVerbInfinitiveCompiledPattern.matcher(infinitive).matches();
	}
	
	public String stripReflexiveMarker(String infinitive) {
		Matcher m = reflexiveVerbInfinitiveCompiledPattern.matcher(infinitive);
		if (!m.matches())
			throw new IllegalArgumentException();
		return m.group(1);
	}

	public String getIdentifier() {
		return identifier;
	}

	public String getNativeName() {
		return nativeName;
	}

	public static Language[] values() {
		return INSTANCES.toArray(new Language[] {});
	}
	
	public static Language valueOf(String key) {
		return INSTANCE_MAP.get(Utilities.asConstantName(key));
	}
	
	public int ordinal() {
		return sequence;
	}

	public int compareTo(Language o) {
		return ordinal() - o.ordinal();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((identifier == null) ? 0 : identifier.hashCode());
		result = prime * result
				+ ((nativeName == null) ? 0 : nativeName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Language))
			return false;
		final Language other = (Language) obj;
		if (identifier == null) {
			if (other.identifier != null)
				return false;
		} else if (!identifier.equals(other.identifier))
			return false;
		if (nativeName == null) {
			if (other.nativeName != null)
				return false;
		} else if (!nativeName.equals(other.nativeName))
			return false;
		return true;
	}
	
	public String toString() {
		return nativeName;
	}
}