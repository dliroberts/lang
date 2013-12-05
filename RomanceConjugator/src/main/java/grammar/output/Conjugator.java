package grammar.output;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import grammar.input.xml.DataManager;
import grammar.model.Form;
import grammar.model.Language;
import grammar.model.PersonalPronounCategory;
import grammar.model.PersonalPronounRole;
import grammar.model.Form.FormCategory;
import grammar.model.factory.ModelVerbFactory;
import grammar.model.verbs.ModelVerb;
import grammar.model.verbs.Tense;
import grammar.model.verbs.ModelVerb.ConjugatedVerb;

public class Conjugator {
	public static void main(String[] args) throws IOException {
		//String infinitive = args[0].toLowerCase();
		//бвзиклопфы
		String infinitive = "savoir".toLowerCase();
//		String infinitive = "think".toLowerCase();
		Object o = DataManager.INSTANCES; // FIXME - neater way to kickstart init of DataManager?
		Language l = Language.valueOf("FRENCH");
//		Language l = Language.valueOf("ENGLISH");
		new Conjugator().conjugate(infinitive, l, false);
	}
	
	public void conjugate(String infinitive, Language language, boolean allForms) throws IOException {
		DataManager.getInstance(language).load();
		conjugate(infinitive, language, allForms, Arrays.asList(Tense.values()));
	}
	
	public void conjugate(String infinitive, Language language, boolean allForms, List<Tense> tenses) throws IOException {
		DataManager.getInstance(language).load();
		ModelVerbFactory mvf = ModelVerbFactory.getInstance();
		System.out.println("Infinitive: "+infinitive);
		ModelVerb mv = mvf.getModelVerb(infinitive, language);
		System.out.println("Selected model verb: " + mv.toString());
		ConjugatedVerb cv = mv.getConjugatedVerb(infinitive);
		System.out.println("Auxiliary verb: "+cv.getAuxiliary().toString());
		
 		for (Tense t : tenses) {
			int i = 0; i++;
			for (FormCategory p : mv.getForms(t)) {
				Form[] l;
				l = p instanceof PersonalPronounCategory ?
						((PersonalPronounCategory) p).getForms(PersonalPronounRole.SUBJECT) :
							p.getAllForms();
				if (!allForms) {
					
					l = new Form[] {new TreeSet<Form>(Arrays.asList(l)).iterator().next()};
				}
				
				for (Form f : l) {
					System.out.println(
							"Mood: "+t.getMood().toString().replace('_', ' ').toLowerCase()+
							"; tense: "+t.toString().replace('_', ' ').toLowerCase()+
							"; form: "+p.toString().replace('_', ' ').toLowerCase()+
							//"; conjugation: "+cv.getForm(t, p.getForms().get(0)));
							"; conjugation: "+cv.getForm(t, f));
				}
			}
		}
	}
}