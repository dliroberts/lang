package grammar.input.xml;

import grammar.model.Language;
import grammar.model.PersonalPronoun;
import grammar.model.PersonalPronounCategory;
import grammar.model.nouns.Noun;
import grammar.model.nouns.NounClass;
import grammar.model.verbs.ModelVerb;
import grammar.model.verbs.Mood;
import grammar.model.verbs.Tense;
import grammar.model.verbs.ModelVerb.ConjugatedVerb;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DataManager {
	public static final String DATA_FOLDER = "./src/main/resources";
	
	public static final Map<Language, DataManager> INSTANCES =
		new HashMap<Language, DataManager>();
	
	static {
		DataLoader<List<Language>> languageLoader =
			new DataLoader<List<Language>>(new File(DATA_FOLDER+"/"), "Languages.xml", new LanguageHandler());
		try {
			List<Language> languages = languageLoader.loadDataItem();
			for (Language language : languages) {
				INSTANCES.put(language, new DataManager(language));
			}
		}
		catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	private DataManager(Language language) {
		this.language = language;
	}
	
	private final Language language;
	private boolean loaded = false;
	
	public boolean loaded() {
		return loaded;
	}
	
	public static DataManager getInstance(Language language) {
		DataManager dm = INSTANCES.get(language);
		
		if (dm == null) {
			dm = new DataManager(language);
			INSTANCES.put(language, dm);
		}
		return dm;
	}
	
	public Language getLanguage() {
		return language;
	}

	public void load() throws IOException {
		
		if (loaded())
			return;
		
		languageBeingLoaded = language;
		
		DataLoader<ModelVerb> modelVerbLoader = new DataLoader<ModelVerb>(
				new File(
						DATA_FOLDER+"/"+language.getIdentifier()+"/model-verbs"),
						"Verb.xml",       new ModelVerbHandler());
		DataLoader<Mood> moodLoader = new DataLoader<Mood>(
				new File(
						DATA_FOLDER+"/"+language.getIdentifier()+"/moods"),
						"Mood.xml",       new MoodHandler());
		DataLoader<Tense> tenseLoader = new DataLoader<Tense>(
				new File(
						DATA_FOLDER+"/"+language.getIdentifier()+"/tenses"),
						"Tense.xml",      new TenseHandler());
		DataLoader<Set<PersonalPronoun>> pronounLoader = new DataLoader<Set<PersonalPronoun>>(
				new File(
						DATA_FOLDER+"/"+language.getIdentifier()+"/pronouns"),
						"Pronoun.xml", new PronounHandler());
		DataLoader<Map.Entry<String, Set<PersonalPronounCategory>>> pronounSetLoader = new DataLoader<Map.Entry<String, Set<PersonalPronounCategory>>>(
				new File(
						DATA_FOLDER+"/"+language.getIdentifier()+"/pronoun-sets"),
						"PronounSet.xml", new PronounSetHandler());
		DataLoader<List<ConjugatedVerb>> verbLoader = new DataLoader<List<ConjugatedVerb>>(
				new File(
						DATA_FOLDER+"/"+language.getIdentifier()+"/verbs"),
						"Verbs.xml", new VerbHandler());
		DataLoader<NounClass> nounClassLoader = new DataLoader<NounClass>(
				new File(
						DATA_FOLDER+"/"+language.getIdentifier()+"/noun-classes"),
						"NounClass.xml", new NounClassHandler());
		DataLoader<List<Noun>> nounLoader = new DataLoader<List<Noun>>(
				new File(
						DATA_FOLDER+"/"+language.getIdentifier()+"/nouns"),
						".xml", new NounHandler());
		
		pronounLoader.loadData();
		nounClassLoader.loadData();
		nounLoader.loadData(); // has a dependency on noun classes
		moodLoader.loadData();
		tenseLoader.loadData(); // has a dependency on moods
		pronounSetLoader.loadData(); // has a dependency on pronouns
		modelVerbLoader.loadData(); // depends on tenses and pronoun sets
		verbLoader.loadData(); // depends on model verbs
		loaded = true;
		languageBeingLoaded = null;
	}
	
	private static Language languageBeingLoaded;
	public static Language languageBeingLoaded() {
		return languageBeingLoaded;
	}
}