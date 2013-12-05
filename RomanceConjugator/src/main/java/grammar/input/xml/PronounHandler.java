package grammar.input.xml;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.xml.sax.Attributes;

import grammar.model.PersonalPronounRole;
import grammar.model.SubjectGender;
import grammar.model.Multiplicity;
import grammar.model.Person;
import grammar.model.PersonalPronoun;
import grammar.util.Utilities;

public class PronounHandler extends DataHandler<Set<PersonalPronoun>> {
	private Set<PersonalPronoun> pronouns = new HashSet<PersonalPronoun>();

	private String label;
	private String elidedLabel;
	private SubjectGender gender;
	private Multiplicity multiplicity;
	private Person person;
	private Set<PersonalPronounRole> roles = new HashSet<PersonalPronounRole>();
	
	private boolean elision = false;

	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		if (qName.equals("label")) {
			enableParsing(ParseMode.NAME);
		}
		else if (qName.equals("elided-label")) {
			enableParsing(ParseMode.NAME);
			elision = true;
		}
		else if (qName.equals("applicability")) {
			multiplicity = null;
			person = null;
			roles.clear();
		}
		else if (qName.equals("gender")) {
			enableParsing(ParseMode.GENDER);
		}
		else if (qName.equals("multiplicity")) {
			enableParsing(ParseMode.MULTIPLICITY);
		}
		else if (qName.equals("person")) {
			enableParsing(ParseMode.PERSON);
		}
	}
	
	public void endElement(String uri, String localName, String qName) {
		ParseMode parseMode = getParseMode();
		String parsedString = getParsedString();
		String parsedStringAsConstant = parsedString.toUpperCase().replace(' ', '_');
		
		if (parseMode.equals(ParseMode.NAME) && !elision) {
			label = parsedString;
		}
		else if (parseMode.equals(ParseMode.NAME) && elision) {
			elidedLabel = parsedString;
			elision = false;
		}
		else if (parseMode.equals(ParseMode.GENDER)) {
			gender = SubjectGender.valueOf(parsedStringAsConstant);
		}
		else if (parseMode.equals(ParseMode.MULTIPLICITY)) {
			multiplicity = Multiplicity.valueOf(parsedStringAsConstant);
		}
		else if (parseMode.equals(ParseMode.PERSON)) {
			person = Person.valueOf(parsedStringAsConstant);
		}
		else if (qName.equals("applicability")) {
			if (label == null)
				throw new IllegalStateException("No label defined for this pronoun!");
			
			Set<Multiplicity> ms = new HashSet<Multiplicity>();
			if (multiplicity == null)
				ms.addAll(Arrays.asList(Multiplicity.values()));
			else
				ms.add(multiplicity);

			Set<Person> ps = new HashSet<Person>();
			if (person == null)
				ps.addAll(Arrays.asList(Person.values()));
			else
				ps.add(person);

			Set<SubjectGender> gs = new HashSet<SubjectGender>();
			if (gender == null) {
				gs.addAll(Arrays.asList(SubjectGender.values()));
				gs.add(null);
			}
			else
				gs.add(gender);
			
			for (PersonalPronounRole role : roles) {
				for (Multiplicity m : ms) {
					for (Person p : ps) {
						for (SubjectGender g : gs) {
							pronouns.add(new PersonalPronoun(label, elidedLabel,
									m, p, g, role, true));
						}
					}
				}
			}
		}
		else {
			try {
				roles.add(PersonalPronounRole.valueOf(Utilities.asConstantName(qName)));
			}
			catch (IllegalArgumentException e) {
				
			}
		}
		disableParsing();
	}
	
	public Set<PersonalPronoun> getParsedData() {
		return pronouns;
	}
}