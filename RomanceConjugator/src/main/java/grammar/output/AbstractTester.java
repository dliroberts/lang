package grammar.output;

import grammar.input.stdin.Command;
import grammar.input.stdin.CommandFactory;
import grammar.input.stdin.Command.Action;
import grammar.input.stdin.Command.ExitCommand;
import grammar.input.stdin.Command.HelpCommand;
import grammar.input.xml.DataManager;
import grammar.model.Language;
import grammar.output.tts.french.FrenchVoiceSynthesiser;
import grammar.util.Utilities;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public abstract class AbstractTester {
	private static final BigDecimal CORRECT_ANSWER_SCORE_INCREMENT           = new BigDecimal(1);
	private static final BigDecimal PARTIALLY_CORRECT_ANSWER_SCORE_INCREMENT = new BigDecimal("0.5");
	private static final int        MAX_SCOPE_ITEMS_TO_PRINT                 = 6;
	
	public abstract String getHelpText();
	
	public abstract Question getNextQuestion();
	
	public interface Question {
		public String getCorrectAnswer();
		public String getQuestionText();
		public boolean isCorrect(String response);
		public String isPartiallyCorrect(String response);
		public String getHintText();
		public String getTextToRead();
	}
	
	public AbstractTester() {
		Object o = DataManager.INSTANCES; // FIXME - neater way to kickstart init of DataManager?
		
		Language language = Language.valueOf("FRENCH");
		try {
			DataManager.getInstance(language).load();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void play() throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		BigDecimal score = new BigDecimal(0);
		BigDecimal highScore = new BigDecimal(0);
		
		System.out.println("Type help for commands.");
		while (true) {
			Question q = getNextQuestion();
			
			System.out.println();
			System.out.println(q.getQuestionText());
			String correctAnswer = q.getCorrectAnswer();
			String response = br.readLine();
			
			try {
				while (true) { // loop broken by exception being thrown
					try {
						processCommand(response);
					}
					catch (IllegalStateException e) {
						System.out.println("Command ignored - it would leave nothing in scope!");
					}
					response = br.readLine();
				}
			}
			catch (IllegalArgumentException e) {
				// not a command
			}
			
			if (q.isCorrect(response)) {
				score = score.add(CORRECT_ANSWER_SCORE_INCREMENT);
				System.out.println("CORRECT! "+score.toPlainString()+
						" point"+(score.compareTo(new BigDecimal(1)) != 0?"s":"")+"!");
				if (score.compareTo(highScore) > 0)
					highScore = score;
			}
			else if (q.isPartiallyCorrect(response) != null) {
				score = score.add(PARTIALLY_CORRECT_ANSWER_SCORE_INCREMENT);
				System.out.println("ALMOST! "+q.isPartiallyCorrect(response)+"Correct answer: "+correctAnswer+". "+
						score.toPlainString()+" point"+
						(score.compareTo(new BigDecimal(1)) >= 0?"s":"")+"!");
				if (score.compareTo(highScore) > 0)
					highScore = score;
			}
			else {
				System.out.println("WRONG! Correct answer: "+correctAnswer+
					". Score this time: "+score+"; highest so far: "+highScore+".");
				score = new BigDecimal(0);
			}
			System.out.println(q.getHintText());
			
			new FrenchVoiceSynthesiser().say(q.getTextToRead());
		}
	}
	
	private void processCommand(String in) {
		Command command = CommandFactory.interpret(in);
		Action action = command.getAction();
		
		if (command instanceof ExitCommand) {
			System.exit(0);
		}
		else if (command instanceof HelpCommand) {
			System.out.println(getHelpText());
		}
		else if (processCommand(command, action)) {
			return;
		}
		else {
			throw new Error("Unimplemented command type: "+command);
		}
	}
	
	public abstract boolean processCommand(Command command, Action action);
	
	protected static <T> void updateScope(Set<T> scope, Set<T> update, Action action) {
		Set<T> scopeTmp = new HashSet<T>(scope);
		if (action.equals(Action.ADD))
			scopeTmp.addAll(update);
		else if (action.equals(Action.USE)) {
			scopeTmp.clear();
			scopeTmp.addAll(update);
		}
		else if (action.equals(Action.REMOVE))
			scopeTmp.removeAll(update);
		else
			throw new IllegalStateException();
		if (scopeTmp.size() == 0)
			throw new IllegalStateException("Nothing left in scope!");
		scope.clear();
		scope.addAll(scopeTmp);
	}
	
	protected static <S> void printScope(SortedSet<S> scope, String type) {
		StringBuilder sb = new StringBuilder();
		sb.append(type+": ");
		Set<S> s = new TreeSet<S>();
		if (scope.size() > MAX_SCOPE_ITEMS_TO_PRINT) {
			Iterator<S> iterator = scope.iterator();
			for (int i = 0; i < MAX_SCOPE_ITEMS_TO_PRINT; i++) {
				s.add(iterator.next());
			}
		}
		else {
			s.addAll(scope);
		}
		
		int sz = s.size();
		int i = 0;
		for (Object o : s) {
			sb.append(Utilities.asHumanReadableName(o.toString()));
			if (i < sz-1)
				sb.append(", ");
			i++;
		}
		if (scope.size() > MAX_SCOPE_ITEMS_TO_PRINT) {
			sb.append("... (");
			sb.append(scope.size()-MAX_SCOPE_ITEMS_TO_PRINT);
			sb.append(" more)");
		}
		String str = sb.toString();
		System.out.println(str);
	}
}