package grammar.model.nouns;

import grammar.model.Multiplicity;
import grammar.model.PseudoEnum;
import grammar.model.SubjectGender;
import grammar.util.Utilities;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Noun implements PseudoEnum<Noun> {
	private static final Map<String, Noun> INSTANCE_MAP = new HashMap<String, Noun>();
	private static final Set<Noun> INSTANCES = new HashSet<Noun>();
	
	private static int sequenceGenerator = 0;
	
	private final int sequence;
	private final Map<NounClass, Map<Multiplicity, NounForm>> formMap = new HashMap<NounClass, Map<Multiplicity, NounForm>>();
	private final Set<NounForm> formSet;
	private final Set<NounTag> classifications;
	
	public Noun(Set<NounForm> forms, Set<NounTag> classifications) {
		for (NounForm form : forms) {
			for (NounClass nc : form.getNounClasses()) {
				Map<Multiplicity, NounForm> ms = Utilities.initialiseIfReqd(nc, formMap);
				ms.put(form.getMultiplicity(), form);
			}
			form.setNoun(this);
		}
		sequence = sequenceGenerator++;
		this.formSet = forms;
		this.classifications = classifications;
		
		INSTANCE_MAP.put(toString(), this);
		INSTANCES.add(this);
	}
	
	public Set<NounForm> getForms() {
		return formSet;
	}
	
	public boolean isRegular() {
		for (NounForm form : formSet) {
			if (!form.isRegular())
				return false;
		}
		return true;
	}
	
	public int ordinal() {
		return sequence;
	}

	public int compareTo(Noun o) {
		return ordinal() - o.ordinal();
	}
	
	public static Noun[] values() {
		return INSTANCES.toArray(new Noun[]{});
	}
	
	public static Noun valueOf(String key) {
		Noun m = INSTANCE_MAP.get(key);
		if (m == null)
			throw new IllegalArgumentException("No such Noun: '"+key+"'.");
		return m;
	}

	public String getText(SubjectGender subjectGender, Multiplicity multiplicity) {
		NounClass nc = mapSubjectGenderToNounClass(subjectGender);
		
		Map<Multiplicity, NounForm> multiplicities = formMap.get(nc);
		if (multiplicities == null)
			multiplicities = formMap.values().iterator().next();
		
		NounForm form = multiplicities.get(multiplicity);
		if (form != null)
			return form.getText();
		if (multiplicity.equals(Multiplicity.PLURAL)) {
			form = multiplicities.get(Multiplicity.SINGULAR);
			return  form + (form.getText().endsWith("s") ? "es" : "s"); // TODO this should come from some xml somewhere...
		}
		else // singular requested; only a plural form exists
			throw new IllegalArgumentException("No singular form exists for noun "+getText(subjectGender, Multiplicity.PLURAL)+".");
	}
	
	public String toString() {
		try {
			return getText(SubjectGender.MASCULINE, Multiplicity.SINGULAR);
		}
		catch (IllegalArgumentException iae) {
			return getText(SubjectGender.MASCULINE, Multiplicity.PLURAL);
		}
	}
	
	public NounClass getNounClass(SubjectGender subjectGender) {
		NounClass nc = mapSubjectGenderToNounClass(subjectGender);
		if (formMap.keySet().contains(nc))
			return nc;
		return formMap.keySet().iterator().next();
	}
	
	private static NounClass mapSubjectGenderToNounClass(SubjectGender subjectGender) {
		NounClass nc;
		if (subjectGender.equals(SubjectGender.MASCULINE)) // TODO mapping is specific to french...
			nc = NounClass.valueOf("MASCULINE");
		else if (subjectGender.equals(SubjectGender.FEMININE))
			nc = NounClass.valueOf("FEMININE");
		else
			nc = null;
		return nc;
	}

	public Set<NounTag> getClassifications() {
		return classifications;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + sequence;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Noun))
			return false;
		final Noun other = (Noun) obj;
		if (sequence != other.sequence)
			return false;
		return true;
	}
}