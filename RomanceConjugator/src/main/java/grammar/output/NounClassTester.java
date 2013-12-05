package grammar.output;

import grammar.input.stdin.Command;
import grammar.input.stdin.Command.Action;
import grammar.input.stdin.Command.NounCommand;
import grammar.model.MatchType;
import grammar.model.Multiplicity;
import grammar.model.WordMatcher;
import grammar.model.factory.NounClassFactory;
import grammar.model.nouns.Noun;
import grammar.model.nouns.NounClass;
import grammar.model.nouns.NounForm;
import grammar.model.nouns.NounTag;
import grammar.util.Utilities;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

public class NounClassTester extends AbstractTester {
	private static final String HELP_TEXT =
		"SYNTAX\t\t\t\t\t\t\t"+      				 				 "EXAMPLES\n"+
		"<action> [regular|irregular] [<verb class> class] nouns\t"	+"\"use regular nouns\", \"add irregular kernel and core class nouns\"\n"+
		"help\n"+
		"exit\n"+
		"\n"+
		"(Where <action> is one of: use, add, remove)";

	private static final Random RANDOM = new Random();
	
	private static Set<NounForm> nounFormsInScope;

	public static void main(String[] args) throws IOException {
		new NounClassTester().play();
	}
	
	public NounClassTester() {
		super();
		nounFormsInScope = new HashSet<NounForm>(Arrays.asList(NounForm.getForms(null, Multiplicity.SINGULAR)));
	}
	
	public boolean processCommand(Command command, Action action) {
		if (command instanceof NounCommand) {
			NounCommand nounCommand = (NounCommand) command;
			List<NounTag> nounTags = nounCommand.getNounTags();
			Set<NounForm> nounForms = new HashSet<NounForm>();
			for (NounTag nounTag : nounTags) {
				Set<Noun> nouns = nounTag.getMembers();
				for (Noun noun : nouns)
					nounForms = addApplicableForms(noun.getForms(), nounCommand, nounForms);
			}
			if (nounTags.size() == 0) {
				nounForms = addApplicableForms(Arrays.asList(NounForm.values()), nounCommand, nounForms);
			}
			
			updateScope(nounFormsInScope, nounForms, action);
			printScope(new TreeSet<NounForm>(nounFormsInScope), "Nouns");
			
			return true;
		}
		return false;
	}
	
	private static Set<NounForm> addApplicableForms(
			Collection<NounForm> candidateNounForms, NounCommand nounCommand, Set<NounForm> nounFormsSoFar) {
		for (NounForm nf : candidateNounForms) {
			if (nf.getMultiplicity().equals(Multiplicity.SINGULAR)) {
				if (	(nf.isRegular()   && nounCommand.includeRegularNouns()    ) ||
						(nf.isException() && nounCommand.includeExceptionalNouns())	||
						(nf.isUncovered() && nounCommand.includeUncoveredNouns()  )
					) {
					nounFormsSoFar.add(nf);
				}
			}
		}
		return nounFormsSoFar;
	}
	
	@Override
	public String getHelpText() {
		return HELP_TEXT;
	}

	@Override
	public Question getNextQuestion() {
		return new NounClassQuestion();
	}

	private class NounClassQuestion implements Question {
		private NounForm nf;
		private List<NounClass> nc;
		
		private NounClassQuestion() {
			NounForm[] nfs = nounFormsInScope.toArray(new NounForm[]{});
			nf = nfs[RANDOM.nextInt(nfs.length)];
			nc = nf.getNounClasses();
		}
		
		public String getCorrectAnswer() {
			return Utilities.formatForPrinting(nc);
		}

		public String getHintText() {
			StringBuilder sb = new StringBuilder();
			
			WordMatcher wm = nf.getNounClassMatcher();
			String f = null;
			if (wm != null) {
				if (wm.getMatchType().equals(MatchType.SUFFIX))
					f = "ends with";
				else
					throw new Error("implement me"); // TODO
			}
			
			if (nf.isRegular()) {
				sb.append("Note: "+nf.getText()+" follows a pattern for "+
						Utilities.asHumanReadableName(nf.getNounClasses().get(0).getName())+
						" nouns, as it "+f+" "+wm.getMatchString()+".");
			}
			else if (wm == null) {
				sb.append("Note: "+nf.getText()+"'s gender is irregular.");
			}
			else if (nf.getNounClasses().size() > 1) {
				// do nothing
			}
			else {
				sb.append("Note: "+nf.getText()+
						"'s gender is irregular - typically, nouns ending with "+
						wm.getMatchString()+" are "+Utilities.asHumanReadableName(
								NounClassFactory.getInstance().getClosestMatch(nf.getText()).getName())+".");
			}
			
			return sb.toString();
		}

		public String getQuestionText() {
			return "Provide the noun class of "+nf.getText()+".";
		}

		public String getTextToRead() {
			return nf.getText();
		}

		public boolean isCorrect(String response) {
			boolean[] correct = new boolean[nc.size()];
			List<String> ins = Arrays.asList(Utilities.asConstantName(response.replace(" ", "")).split(","));
			int i = 0;
			for (NounClass n : nc) {
				correct[i] = ins.contains(Utilities.asConstantName(n.getName())) ||
					ins.contains(Utilities.asConstantName(n.getAbbreviation()));
				i++;
			}
			
			boolean c = true;
			for (boolean b : correct) {
				if (!b) {
					c = false;
					break;
				}
			}
			
			return c;
		}

		public String isPartiallyCorrect(String response) {
			return null;
		}
	}
}