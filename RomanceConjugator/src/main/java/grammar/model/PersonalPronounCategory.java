/**
 * 
 */
package grammar.model;

import grammar.model.Form.FormCategory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public enum PersonalPronounCategory implements FormCategory {
	FIRST_PERSON_SINGULAR (Multiplicity.SINGULAR, Person.FIRST),
	SECOND_PERSON_SINGULAR(Multiplicity.SINGULAR, Person.SECOND),
	THIRD_PERSON_SINGULAR (Multiplicity.SINGULAR, Person.THIRD),
	FIRST_PERSON_PLURAL   (Multiplicity.PLURAL,   Person.FIRST),
	SECOND_PERSON_PLURAL  (Multiplicity.PLURAL,   Person.SECOND),
	THIRD_PERSON_PLURAL   (Multiplicity.PLURAL,   Person.THIRD);
	
	private final static Map<String, SortedSet<PersonalPronounCategory>> SETS =
		new HashMap<String, SortedSet<PersonalPronounCategory>>();
	
	private static boolean PRONOUNS_LOADED = false;
	
	private final Multiplicity multiplicity;
	private final Person person;
	private final SortedSet<PersonalPronoun> pronounList = new TreeSet<PersonalPronoun>();
	
	private PersonalPronounCategory(Multiplicity multiplicity, Person person) {
		this.multiplicity = multiplicity;
		this.person = person;
	}

	public PersonalPronoun[] getForms(PersonalPronounRole role) {
		if (!PRONOUNS_LOADED) {
			// Return value ignored - just need to trigger instantiation of Pronoun's static fields. FIXME
			PersonalPronoun.getThirdPersonInstance(Multiplicity.PLURAL, SubjectGender.MASCULINE, PersonalPronounRole.SUBJECT, "");
			PRONOUNS_LOADED = true;
		}
		
		Set<Form> applicableForms = new HashSet<Form>();
		for (PersonalPronoun pronoun : pronounList) {
			if (pronoun.isApplicable(role, null, multiplicity, person))
				applicableForms.add(pronoun);
		}
		
		return applicableForms.toArray(new PersonalPronoun[]{});
	}
	
	public Form[] getAllForms() {
		if (!PRONOUNS_LOADED) {
			// Return value ignored - just need to trigger instantiation of Pronoun's static fields. FIXME
			PersonalPronoun.getThirdPersonInstance(Multiplicity.PLURAL, SubjectGender.MASCULINE, PersonalPronounRole.SUBJECT, "");
			PRONOUNS_LOADED = true;
		}
		
		return pronounList.toArray(new Form[]{});
	}

	public static SortedSet<PersonalPronounCategory> getPronounSet(String key) {
		SortedSet<PersonalPronounCategory> s = SETS.get(key.toUpperCase().replace(' ', '_'));
		if (s == null)
			throw new IllegalArgumentException("No such PronounCategory set: '"+key+"'.");
		return s;
	}
	
	public static List<PersonalPronounCategory> getPronounSet(Multiplicity multiplicity, Person person) {
		List<PersonalPronounCategory> set = new ArrayList<PersonalPronounCategory>();
		for (PersonalPronounCategory pc : values()) {
			boolean add = true;
			if (multiplicity != null)
				add &= multiplicity == pc.multiplicity;
			if (person != null)
				add &= person == pc.person;
			if (add)
				set.add(pc);
		}
		return set;
	}
	
	public static PersonalPronounCategory getPronounCategory(Multiplicity multiplicity, Person person) {
		if (multiplicity == null || person == null)
			throw new IllegalArgumentException();
		return getPronounSet(multiplicity, person).iterator().next();
	}
	
	public static void addPronounSet(String name, SortedSet<PersonalPronounCategory> pronounSet) {
		SETS.put(name.toUpperCase(), pronounSet);
	}

	public void addPronoun(PersonalPronoun pronoun) {
		if (!pronoun.getMultiplicity().equals(multiplicity) || !pronoun.getPerson().equals(person))
			throw new IllegalArgumentException();
		pronounList.add(pronoun);
	}
}