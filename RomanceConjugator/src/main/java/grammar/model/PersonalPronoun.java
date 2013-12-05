package grammar.model;

public class PersonalPronoun implements PseudoEnum<PersonalPronoun>, Form {
	private final String name;
	private final String elidedName;
	private final PersonalPronounRole role;
	private final Multiplicity multiplicity;
	private final Person person;
	private final SubjectGender gender;
	private PersonalPronounCategory category;
	
	public PersonalPronoun(String label, String elidedLabel,
			Multiplicity multiplicity, Person person,
			SubjectGender gender, PersonalPronounRole role,
			boolean addToList) {
		this.name = label;
		this.multiplicity = multiplicity;
		this.person = person;
		this.gender = gender;
		this.elidedName = elidedLabel;
		this.role = role;
		
		if (addToList) {
			switch (multiplicity) {
			case SINGULAR:
				switch (person) {
				case FIRST:  category = PersonalPronounCategory.FIRST_PERSON_SINGULAR;  break;
				case SECOND: category = PersonalPronounCategory.SECOND_PERSON_SINGULAR; break;
				case THIRD:  category = PersonalPronounCategory.THIRD_PERSON_SINGULAR;  break;
				default: throw new Error();
				}
				break;
			case PLURAL:
				switch (person) {
				case FIRST:  category = PersonalPronounCategory.FIRST_PERSON_PLURAL;  break;
				case SECOND: category = PersonalPronounCategory.SECOND_PERSON_PLURAL; break;
				case THIRD:  category = PersonalPronounCategory.THIRD_PERSON_PLURAL;  break;
				default: throw new Error();
				}
				break;
			default: throw new Error();
			}
			category.addPronoun(this);
		}
//		System.out.println(label + ";" + person + ";" + multiplicity + ";" + gender + ";" + role);
	}
	
	public boolean isApplicable(PersonalPronounRole role, SubjectGender gender, Multiplicity multiplicity, Person person) {
		boolean match = true;
		
		if (role != null)
			match &= getRole()         != null && getRole().equals(role);
		if (gender != null)
			match &= getGender()       != null && getGender().equals(gender);
		if (multiplicity != null)
			match &= getMultiplicity() != null && getMultiplicity().equals(multiplicity);
		if (person != null)
			match &= getPerson()       != null && getPerson().equals(person);
		
		return	match;
	}
	
	public PersonalPronounCategory getCategory() {
		return category;
	}

	public int compareTo(PersonalPronoun o) {
		if (!multiplicity.equals(o.multiplicity))
			return multiplicity.ordinal() - o.multiplicity.ordinal();
		if (!person.equals(o.person))
			return person.ordinal() - o.person.ordinal();
		if (!role.equals(o.role))
			return role.ordinal() - o.role.ordinal();
		if (gender != null && o.gender != null)
			return gender.ordinal() - o.gender.ordinal();
		return 0;
	}
	
	public int ordinal() {
		return
			multiplicity.ordinal() * 1000000 +
			person.ordinal()       * 1000 +
			gender.ordinal()       * 1;
	}

	public String toString() {
		return name;
	}
		
	public static PersonalPronoun getThirdPersonInstance(
			Multiplicity multiplicity, SubjectGender gender, PersonalPronounRole role, String label) {
		return new PersonalPronoun(label, null, multiplicity, Person.THIRD, gender, role, false);
	}
	
	public String getLabel(String conjugation) {
		if (elidedName == null)
			return name;
		if (conjugation.matches("^[aeéiou].*"))
			return elidedName;
		else
			return name;
	}
	
	public String addPronoun(String conjugation) {
		String p = getLabel(conjugation);
		return p + (p.endsWith("'") ? "" : " ") + conjugation;
	}
	
	public String getLabel() {
		return name;
	}

	public String getElidedLabel() {
		return elidedName;
	}

	public Multiplicity getMultiplicity() {
		return multiplicity;
	}

	public Person getPerson() {
		return person;
	}

	public SubjectGender getGender() {
		return gender;
	}

	public PersonalPronounRole getRole() {
		return role;
	}
}