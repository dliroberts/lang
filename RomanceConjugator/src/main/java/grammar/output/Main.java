package grammar.output;

import grammar.analysis.NounSuffixNounClassCorrelator;
import grammar.input.xml.DataManager;
import grammar.model.Language;
import grammar.model.verbs.Tense;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

public class Main {
	private static final String HELP_TEXT =
		"SYNTAX\t\t\t\t\t"+							"ACTION\n"+
		"test nouns\t\t\t\t"+						"Given a noun, guess its gender/noun class.\n"+
		"test verbs\t\t\t\t"+						"Given an infinitive, provide a particular inflected form.\n"+
		"conjugate [the <tense> of] <verb>\t"+		"Displays inflections of a given verb.\n"+
		"analyse noun classes\t\t\t"+				"Correlates noun suffixes with noun classes.\n"+
		"show verb tree\t\t\t\t"+					"Provides a visual representation of the model verb hierarchy.\n"+
		"help\t\t\t\t\t"+							"Displays this text.\n"+
		"exit\t\t\t\t\t"+							"Exits the program.\n";
	
	public static void main(String[] args) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		Object o = DataManager.INSTANCES; // FIXME - neater way to kickstart init of DataManager?
		Language l = Language.valueOf("FRENCH");
//		Language l = Language.valueOf("ENGLISH");
		DataManager.getInstance(l).load();
		
		System.out.println("Type help for commands.");
		
		while (true) {
			String response = br.readLine();
			
			if (response.equalsIgnoreCase("test nouns")) {
				new NounClassTester().play();
			}
			else if (response.equalsIgnoreCase("analyse noun classes")) {
				new NounSuffixNounClassCorrelator().findNounClassRules();
			}
			else if (response.equalsIgnoreCase("show verb tree")) {
				ModelVerbGraphViewer.main(null);
			}
			else if (response.equalsIgnoreCase("test verbs")) {
				new FormTester().play();
			}
			else if (response.startsWith("conjugate") && response.contains("of")) {
				response = response.substring("conjugate the ".length()).replace(" tense", "");
				String tense = response.substring(0, response.indexOf(" of"));
				String infinitive = response.substring(response.lastIndexOf(' ')+1);
				
				Tense t = Tense.valueOf(tense);
				new Conjugator().conjugate(
						infinitive, l, false, Arrays.asList(new Tense[] {t}));
			}
			else if (response.startsWith("conjugate")) {
				new Conjugator().conjugate(
						response.substring("conjugate ".length()), l, false);
			}
			else if (response.equals("help")) {
				System.out.println(HELP_TEXT);
			}
			else if (response.equals("exit")) {
				System.exit(0);
			}
		}
	}
}