package grammar.input.stdin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import grammar.input.stdin.Command.Action;
import grammar.input.stdin.Command.ConjugatedVerbCommand;
import grammar.input.stdin.Command.ExitCommand;
import grammar.input.stdin.Command.HelpCommand;
import grammar.input.stdin.Command.ModelVerbCommand;
import grammar.input.stdin.Command.MoodCommand;
import grammar.input.stdin.Command.NounCommand;
import grammar.input.stdin.Command.TenseTagCommand;
import grammar.input.stdin.Command.TenseCommand;
import grammar.input.stdin.Command.VerbClassCommand;
import grammar.model.nouns.NounTag;
import grammar.model.nouns.NounForm.RegularityCategory;
import grammar.model.verbs.ModelVerb;
import grammar.model.verbs.Mood;
import grammar.model.verbs.Tense;
import grammar.model.verbs.TenseTag;
import grammar.model.verbs.VerbTag;
import grammar.model.verbs.ModelVerb.ConjugatedVerb;
import grammar.util.Utilities;

public class CommandFactory {
	public static Command interpret(String commandText) {
		String[] words = commandText.split(" ");
		Action action = Action.valueOf(Utilities.asConstantName(words[0]));
		
		words = Arrays.copyOfRange(words, 1, words.length);
		String[] wordsNoConj = removeConjunctions(words);
		
		List<XCommandParser> cps = Arrays.asList(new XCommandParser[] {
				new ResetCommandParser(),
				new ConjugatedVerbCommandParser(),
				new ModelVerbCommandParser(),
				new MoodCommandParser(),
				new TenseCommandParser(),
				new VerbClassCommandParser(),
				new TenseClassCommandParser(),
				new ExitCommandParser(),
				new HelpCommandParser(),
				new NounCommandParser()
		});
		try {
			for (XCommandParser cp : cps) {
				if (cp.accept(action, words)) {
					return cp.interpret(action, wordsNoConj, words);
				}
			}
		}
		catch (Exception e) {
			// invalid command
		}
		
		System.out.println("Invalid command.");
		return new HelpCommand(action);
	}

	private static String[] removeConjunctions(String[] words) {
		List<String> l = new ArrayList<String>();
		for (int i = 0; i < words.length; i++) {
			if (words[i].equals("and"))
				continue;
			if (words[i].endsWith(","))
				words[i] = words[i].substring(0, words[i].length()-1);
			l.add(words[i]);
		}
		return l.toArray(new String[] {});
	}
	
	private interface XCommandParser {
		public boolean accept(Action action, String[] words);
		public Command interpret(Action action, String[] words, String[] wordsWithConj);
	}

	private static class ConjugatedVerbCommandParser implements XCommandParser {
		public boolean accept(Action action, String[] words) {
			return words.length >= 2 && words[0].equals("the") && (words[1].equals("verb") || words[1].equals("verbs"));
		}

		public Command interpret(Action action, String[] words, String[] wordsWithConj) {
			words = Arrays.copyOfRange(words, 2, words.length);
			List<ConjugatedVerb> conjugatedVerbs = new ArrayList<ConjugatedVerb>();
			for (String word : words) {
				conjugatedVerbs.add(ConjugatedVerb.valueOf(word));
			}
			return new ConjugatedVerbCommand(action, conjugatedVerbs);
		}
	}
	
	private static class NounCommandParser implements XCommandParser {
		public boolean accept(Action action, String[] words) {
			return words.length >= 2 && words[words.length-1].equals("nouns");
		}

		public Command interpret(Action action, String[] words, String[] wordsWithConj) {
			List<NounTag> nounClasses = new ArrayList<NounTag>();
			
			words = Arrays.copyOfRange(words, 0, words.length-1);
			
			Set<RegularityCategory> regularityCategories = new HashSet<RegularityCategory>();
			if (words[0].equals("regular")) {
				regularityCategories.add(RegularityCategory.REGULAR);
				words = Arrays.copyOfRange(words, 1, words.length);
			}
			else if (words[0].equals("irregular")) {
				regularityCategories.add(RegularityCategory.EXCEPTION);
				regularityCategories.add(RegularityCategory.NOT_COVERED);
				words = Arrays.copyOfRange(words, 1, words.length);
			}
			else if (words[0].equals("exceptional")) {
				regularityCategories.add(RegularityCategory.EXCEPTION);
				words = Arrays.copyOfRange(words, 1, words.length);
			}
			else if (words[0].equals("unexceptional")) {
				regularityCategories.add(RegularityCategory.REGULAR);
				regularityCategories.add(RegularityCategory.NOT_COVERED);
				words = Arrays.copyOfRange(words, 1, words.length);
			}
			else if (words[0].equals("uncovered")) {
				regularityCategories.add(RegularityCategory.NOT_COVERED);
				words = Arrays.copyOfRange(words, 1, words.length);
			}
			else if (words[0].equals("covered")) {
				regularityCategories.add(RegularityCategory.REGULAR);
				regularityCategories.add(RegularityCategory.EXCEPTION);
				words = Arrays.copyOfRange(words, 1, words.length);
			}
			else {
				regularityCategories.addAll(Arrays.asList(RegularityCategory.values()));
			}
			
			for (String word : words) {
				if (!word.equalsIgnoreCase("class"))
					nounClasses.add(NounTag.valueOf(Utilities.asConstantName(word)));
			}
			
			return new NounCommand(action, nounClasses, regularityCategories);
		}
	}
		
	private static class ResetCommandParser implements XCommandParser {
		public boolean accept(Action action, String[] words) {
			return words.length == 2 && words[0].equals("all") &&
				(words[1].equals("verbs") || words[1].equals("tenses"));
		}

		public Command interpret(Action action, String[] words, String[] wordsWithConj) {
			if (words[1].equalsIgnoreCase("verbs"))
				return new ModelVerbCommand(action, Arrays.asList(ModelVerb.values()), false, true);
			else
				return new TenseCommand(action, Arrays.asList(Tense.values()));
		}
	}
	
	private static class ModelVerbCommandParser implements XCommandParser {
		public boolean accept(Action action, String[] words) {
			return words.length >= 2 && !words[words.length-2].equals("class") && words[words.length-1].equals("verbs");
		}

		public Command interpret(Action action, String[] words, String[] wordsWithConj) {
			words = Arrays.copyOfRange(words, 0, words.length-1);
			List<ModelVerb> modelVerbs = new ArrayList<ModelVerb>();
			boolean includeInherited = true;
			boolean includeSelf = true;
			if (words[0].equals("regular")) { 
				words = Arrays.copyOfRange(words, 1, words.length);
				includeInherited = false;
			}
			else if (words[0].equals("irregular")) {
				words = Arrays.copyOfRange(words, 1, words.length);
				includeSelf = false;
			}
			for (String word : words) {
				modelVerbs.add(ModelVerb.valueOf(word));
			}
			return new ModelVerbCommand(action, modelVerbs, includeInherited, includeSelf);
		}
	}
	
	private static class MoodCommandParser implements XCommandParser {
		public boolean accept(Action action, String[] words) {
			return words.length >= 3 && words[0].equals("the") && (words[2].equals("mood") || words[2].equals("moods"));
		}

		public Command interpret(Action action, String[] words, String[] wordsWithConj) {
			words = Arrays.copyOfRange(words, 1, words.length-1);
			List<Mood> moods = new ArrayList<Mood>();
			for (String word : words) {
				moods.add(Mood.valueOf(word));
			}
			return new MoodCommand(action, moods);
		}
	}
	
	private static class TenseCommandParser implements XCommandParser {
		public boolean accept(Action action, String[] words) {
			return words.length >= 3 && words[0].equals("the") && (words[words.length-1].equals("tense") || words[words.length-1].equals("tenses"));
		}

		public Command interpret(Action action, String[] words, String[] wordsWithConj) {
			wordsWithConj = Arrays.copyOfRange(wordsWithConj, 1, wordsWithConj.length-1);
			List<Tense> tenses = new ArrayList<Tense>();
			StringBuilder sb = new StringBuilder();
			for (String token : wordsWithConj) {
				if (token.equals("and")) {
					sb = new StringBuilder();
					tenses.add(Tense.valueOf(sb.toString().trim()));
				}
				else if (token.endsWith(",")) {
					token = token.substring(0, token.length()-1);
					sb = new StringBuilder();
					tenses.add(Tense.valueOf(sb.toString().trim()));
				}
				else {
					sb.append(' ');
					sb.append(token);
				}
			}
			tenses.add(Tense.valueOf(sb.toString().trim()));
			return new TenseCommand(action, tenses);
		}
	}

	private static class VerbClassCommandParser implements XCommandParser {
		public boolean accept(Action action, String[] words) {
			return words.length >= 3 && words[words.length-2].equals("class") && words[words.length-1].equals("verbs");
		}

		public Command interpret(Action action, String[] words, String[] wordsWithConj) {
			words = Arrays.copyOfRange(words, 0, words.length-2);
			List<VerbTag> verbClasses = new ArrayList<VerbTag>();
			for (String word : words) {
				verbClasses.add(VerbTag.valueOf(Utilities.asConstantName(word)));
			}
			return new VerbClassCommand(action, verbClasses);
		}
	}

	private static class TenseClassCommandParser implements XCommandParser {
		public boolean accept(Action action, String[] words) {
			return words.length >= 3 && words[words.length-2].equals("class") && words[words.length-1].equals("tenses");
		}

		public Command interpret(Action action, String[] words, String[] wordsWithConj) {
			words = Arrays.copyOfRange(words, 0, words.length-2);
			List<TenseTag> tenseClasses = new ArrayList<TenseTag>();
			for (String word : words) {
				tenseClasses.add(TenseTag.valueOf(Utilities.asConstantName(word)));
			}
			return new TenseTagCommand(action, tenseClasses);
		}
	}
	
	private static class ExitCommandParser implements XCommandParser {
		public boolean accept(Action action, String[] words) {
			return action.equals(Action.EXIT);
		}

		public Command interpret(Action action, String[] words, String[] wordsWithConj) {
			return new ExitCommand(action);
		}
	}
	
	private static class HelpCommandParser implements XCommandParser {
		public boolean accept(Action action, String[] words) {
			return action.equals(Action.HELP);
		}

		public Command interpret(Action action, String[] words, String[] wordsWithConj) {
			return new HelpCommand(action);
		}
	}
}