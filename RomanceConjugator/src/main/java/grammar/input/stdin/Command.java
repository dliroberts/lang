package grammar.input.stdin;

import java.util.List;
import java.util.Set;
import grammar.model.nouns.NounTag;
import grammar.model.nouns.NounForm.RegularityCategory;
import grammar.model.verbs.ModelVerb;
import grammar.model.verbs.Mood;
import grammar.model.verbs.Tense;
import grammar.model.verbs.TenseTag;
import grammar.model.verbs.VerbTag;
import grammar.model.verbs.ModelVerb.ConjugatedVerb;

public abstract class Command {
	private final Action action;
	
	public Command(Action action) {
		this.action = action;
	}
	
	public Action getAction() {
		return action;
	}

	public enum Action {
		USE, ADD, REMOVE, EXIT, HELP;
	}
	
	public static class ConjugatedVerbCommand extends Command {
		private final List<ConjugatedVerb> conjugatedVerbs;
		public ConjugatedVerbCommand(Action action, List<ConjugatedVerb> conjugatedVerbs) {
			super(action);
			this.conjugatedVerbs = conjugatedVerbs;
		}
		public List<ConjugatedVerb> getConjugatedVerbs() {
			return conjugatedVerbs;
		}
	}
	public static class ModelVerbCommand extends Command {
		private final List<ModelVerb> modelVerbs;
		private final boolean includeInherited;
		private final boolean includeSelf;
		public ModelVerbCommand(Action action, List<ModelVerb> modelVerbs,
				boolean includeInherited, boolean includeSelf) {
			super(action);
			this.modelVerbs = modelVerbs;
			this.includeInherited = includeInherited;
			this.includeSelf = includeSelf;
		}
		public boolean includeInherited() {
			return includeInherited;
		}
		public boolean includeSelf() {
			return includeSelf;
		}
		public List<ModelVerb> getModelVerbs() {
			return modelVerbs;
		}
	}
	public static class MoodCommand extends Command {
		private final List<Mood> moods;
		public MoodCommand(Action action, List<Mood> moods) {
			super(action);
			this.moods = moods;
		}
		public List<Mood> getMoods() {
			return moods;
		}		
	}
	public static class TenseCommand extends Command {
		private final List<Tense> tenses;
		public TenseCommand(Action action, List<Tense> tenses) {
			super(action);
			this.tenses = tenses;
		}
		public List<Tense> getTenses() {
			return tenses;
		}
	}
	public static class TenseTagCommand extends Command {
		private final List<TenseTag> tenseClasses;
		public TenseTagCommand(Action action,
				List<TenseTag> tenseClasses) {
			super(action);
			this.tenseClasses = tenseClasses;
		}
		public List<TenseTag> getTenseTags() {
			return tenseClasses;
		}
	}
	public static class VerbClassCommand extends Command {
		private final List<VerbTag> verbClasses;
		public VerbClassCommand(Action action,
				List<VerbTag> verbClasses) {
			super(action);
			this.verbClasses = verbClasses;
		}
		public List<VerbTag> getVerbTags() {
			return verbClasses;
		}
	}
	public static class ExitCommand extends Command {
		public ExitCommand(Action action) {
			super(action);
		}
	}
	public static class HelpCommand extends Command {
		public HelpCommand(Action action) {
			super(action);
		}
	}
	public static class NounCommand extends Command {
		private final List<NounTag> nounClasses;
		private final Set<RegularityCategory> regularityCategories;
		
		public NounCommand(Action action, List<NounTag> nounClasses,
				Set<RegularityCategory> regularityCategories) {
			super(action);
			this.regularityCategories = regularityCategories;
			this.nounClasses = nounClasses;
		}

		public List<NounTag> getNounTags() {
			return nounClasses;
		}

		public boolean includeRegularNouns() {
			return regularityCategories.contains(RegularityCategory.REGULAR);
		}
		
		public boolean includeExceptionalNouns() {
			return regularityCategories.contains(RegularityCategory.EXCEPTION);
		}
		
		public boolean includeUncoveredNouns() {
			return regularityCategories.contains(RegularityCategory.NOT_COVERED);
		}
	}
}