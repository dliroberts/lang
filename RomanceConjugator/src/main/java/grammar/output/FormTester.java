package grammar.output;

import grammar.input.stdin.Command;
import grammar.input.stdin.Command.Action;
import grammar.input.stdin.Command.ConjugatedVerbCommand;
import grammar.input.stdin.Command.ModelVerbCommand;
import grammar.input.stdin.Command.MoodCommand;
import grammar.input.stdin.Command.TenseTagCommand;
import grammar.input.stdin.Command.TenseCommand;
import grammar.input.stdin.Command.VerbClassCommand;
import grammar.model.PersonalPronounCategory;
import grammar.model.PersonalPronounRole;
import grammar.model.Form.FormCategory;
import grammar.model.verbs.ModelVerb;
import grammar.model.verbs.Mood;
import grammar.model.verbs.Tense;
import grammar.model.verbs.TenseTag;
import grammar.model.verbs.VerbTag;
import grammar.model.verbs.ModelVerb.ConjugatedVerb;
import grammar.util.Utilities;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

public class FormTester extends AbstractTester {
	private static final String HELP_TEXT =
		"SYNTAX\t\t\t\t\t"+                     "EXAMPLES\n"+
		"<action> the verb(s) <verb...>\t\t"+   "\"add the verb manger\", \"remove the verbs aller, savoir and faire\"\n"+
		"<action> [regular] <model...> verbs\t"+"\"use ir verbs\", \"remove regular er and ir verbs\"\n"+
		"<action> <class...> class verbs\t\t"+  "\"add core class verbs\"\n"+
		"<action> <class...> class tenses\t"+   "\"add kernel class tenses\"\n"+
		"<action> the <mood...> mood(s)\t\t"+   "\"use the imperative mood\"\n"+
		"<action> the <tense...> tense(s)\t"+   "\"remove the subjunctive imperfect tense\"\n"+
		"use all verbs\t\t\t\t"+				"\"use all verbs\"\n"+
		"use all tenses\t\t\t\t"+				"\"use all tenses\"\n"+
		"help\n"+
		"exit\n"+
		"\n"+
		"(Where <action> is one of: use, add, remove)";
	
	private Set<ConjugatedVerb> verbsInScope;
	private Set<Tense> tensesInScope;
	
	public static void main(String[] args) throws IOException {
		new FormTester().play();
	}
	
	public FormTester() {
		super();
		verbsInScope = new HashSet<ConjugatedVerb>(Arrays.asList(ConjugatedVerb.values()));
		tensesInScope = new HashSet<Tense>(Arrays.asList(Tense.values()));
	}
	
	@Override
	public String getHelpText() {
		return HELP_TEXT;
	}

	@Override
	public boolean processCommand(Command command, Action action) {
		if (command instanceof ModelVerbCommand) {
			ModelVerbCommand modelVerbCommand = (ModelVerbCommand) command;
			List<ModelVerb> modelVerbs = modelVerbCommand.getModelVerbs();
			Set<ConjugatedVerb> conjugatedVerbs = new HashSet<ConjugatedVerb>();
			for (ModelVerb modelVerb : modelVerbs) {
				conjugatedVerbs.addAll(modelVerb.getConjugatedVerbs(
						modelVerbCommand.includeInherited(), modelVerbCommand.includeSelf()));
			}
			updateScope(verbsInScope, conjugatedVerbs, action);
			printScope(new TreeSet<ConjugatedVerb>(verbsInScope), "Verbs");
			return true;
		}
		else if (command instanceof ConjugatedVerbCommand) {
			ConjugatedVerbCommand conjugatedVerbCommand = (ConjugatedVerbCommand) command;
			Set<ConjugatedVerb> conjugatedVerbs = new HashSet<ConjugatedVerb>(conjugatedVerbCommand.getConjugatedVerbs());
			updateScope(verbsInScope, conjugatedVerbs, action);
			printScope(new TreeSet<ConjugatedVerb>(verbsInScope), "Verbs");
			return true;
		}
		else if (command instanceof VerbClassCommand) {
			VerbClassCommand verbClassCommand = (VerbClassCommand) command;
			Set<VerbTag> verbTags = new HashSet<VerbTag>(verbClassCommand.getVerbTags());
			Set<ConjugatedVerb> verbs = new HashSet<ConjugatedVerb>();
			for (VerbTag verbTag : verbTags) {
				verbs.addAll(verbTag.getMembers());
			}
			updateScope(verbsInScope, verbs, action);
			printScope(new TreeSet<ConjugatedVerb>(verbsInScope), "Verbs");
			return true;
		}
		else if (command instanceof MoodCommand) {
			MoodCommand moodCommand = (MoodCommand) command;
			Set<Mood> moods = new HashSet<Mood>(moodCommand.getMoods());
			Set<Tense> tenses = new HashSet<Tense>();
			for (Mood mood : moods) {
				tenses.addAll(mood.getTenses());
			}
			updateScope(tensesInScope, tenses, action);
			printScope(new TreeSet<Tense>(tensesInScope), "Tenses");
			return true;
		}
		else if (command instanceof TenseCommand) {
			TenseCommand tenseCommand = (TenseCommand) command;
			Set<Tense> tenses = new HashSet<Tense>(tenseCommand.getTenses());
			updateScope(tensesInScope, tenses, action);
			printScope(new TreeSet<Tense>(tensesInScope), "Tenses");
			return true;
		}
		else if (command instanceof TenseTagCommand) {
			TenseTagCommand tenseTagCommand = (TenseTagCommand) command;
			Set<TenseTag> tenseTags = new HashSet<TenseTag>(tenseTagCommand.getTenseTags());
			Set<Tense> tenses = new HashSet<Tense>();
			for (TenseTag tenseTag : tenseTags) {
				tenses.addAll(tenseTag.getMembers());
			}
			updateScope(tensesInScope, tenses, action);
			printScope(new TreeSet<Tense>(tensesInScope), "Tenses");
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public Question getNextQuestion() {
		return new FormQuestion();
	}

	private static final Random RANDOM = new Random();
	private static int handyAccents = 0;
	
	private class FormQuestion implements Question {
		private final Tense tense;
		private final ConjugatedVerb conjugatedVerb;
		private final ModelVerb modelVerb;
		private final FormCategory form;
		private final String correctAnswer;
		
		private FormQuestion() {
			Tense tense;
			ConjugatedVerb conjugatedVerb;
			FormCategory[] forms;
			while (true) {
				Tense[] tenses = tensesInScope.toArray(new Tense[]{});
				ConjugatedVerb[] conjugatedVerbs = verbsInScope.toArray(new ConjugatedVerb[]{});
				tense = tenses[RANDOM.nextInt(tenses.length)];
				conjugatedVerb = conjugatedVerbs[RANDOM.nextInt(conjugatedVerbs.length)];
				forms = conjugatedVerb.getModelVerb().getForms(tense);
				if (forms.length > 0)
					break;
			}
			this.conjugatedVerb = conjugatedVerb;
			this.tense = tense;
			form = forms[RANDOM.nextInt(forms.length)];
			correctAnswer = conjugatedVerb.getForm(tense, (form instanceof PersonalPronounCategory ?
					((PersonalPronounCategory) form).getForms(PersonalPronounRole.SUBJECT) :
						form.getAllForms())[0]);
			modelVerb = conjugatedVerb.getModelVerb();
		}
		
		public String getCorrectAnswer() {
			return correctAnswer;
		}

		public String getHintText() {
			StringBuilder sb = new StringBuilder();
			Set<ConjugatedVerb> conjugatedVerbs = modelVerb.getConjugatedVerbs();
			conjugatedVerbs.remove(conjugatedVerb);
			sb.append("Note: "+conjugatedVerb.getInfinitive()+" is a "+modelVerb.getName()+
					" verb.\n");
			if (conjugatedVerbs.size() > 0)
				sb.append("Other "+modelVerb.getName()+" verbs include "+Utilities.formatForPrinting(conjugatedVerbs, 6)+".\n");
			if (modelVerb.getParents().size() > 0)
				sb.append(modelVerb.getName()+" inherits rules from the model verbs "+
						Utilities.formatForPrinting(modelVerb.getParents(), 6)+".\n");
			
			if (modelVerb.getSummary() != null) {
				sb.append("In summary, the distinguishing features of "+modelVerb.getName()+" verbs are as follows:\n");
				sb.append(modelVerb.getSummary());
			}
			
			return sb.toString();
		}
		
		public String getQuestionText() {
			StringBuilder sb = new StringBuilder();
			
			if ((handyAccents++ % 4) == 0)
				sb.append("Handy accents: бвзиклопфы\n\n");
			
			sb.append("Conjugate the following... ");
			sb.append("form: ");
			sb.append(Utilities.asHumanReadableName(form.toString()));
			sb.append("; tense: ");
			sb.append(Utilities.asHumanReadableName(tense.toString()));
			sb.append("; verb: ");
			sb.append(conjugatedVerb.getInfinitive());
			
			return sb.toString();
		}

		public String getTextToRead() {
			return correctAnswer;
		}

		public boolean isCorrect(String response) {
			return correctAnswer.equals(response);
		}

		public String isPartiallyCorrect(String response) {
			if (Utilities.equalsIgnoreAccents(correctAnswer, response))
				return "Just the accents were wrong.";
			if (correctAnswer.equalsIgnoreCase(response))
				return "Just the capitalisation was wrong.";
			if (Utilities.equalsIgnoreAccentsAndCase(correctAnswer, response))
				return "Just the accents and capitalisation was wrong.";
			return null; // not partially correct - just plain wrong!
		}
	}
}