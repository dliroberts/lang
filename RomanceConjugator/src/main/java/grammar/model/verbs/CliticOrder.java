package grammar.model.verbs;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CliticOrder {
	private final String name;
	private final If predicate;
	private final List<Condition> conditions;
	
	private CliticOrder(String name, If predicate, List<Condition> conditions) {
		this.name = name;
		this.predicate = predicate;
		this.conditions = conditions;
	}

	public boolean matches(Conjugation conjugation)
			throws IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		return predicate.eval(conjugation);
	}
	
	public String inflect(Conjugation conjugation) {
		for (Condition condition : conditions) {
			
		}
		
		return null; // FIXME
	}
	
	public static class If {
		private final boolean conditional;
		private final String conditionStr;
		
		private If() {
			conditional = false;
			this.conditionStr = null;
		}
		
		private If(String conditionStr) {
			conditional = true;
			this.conditionStr = conditionStr.trim().replaceAll("\\s+", " ");
		}
		
		public boolean eval(Conjugation conjugation)
				throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
			if (!conditional)
				return true;
			
			String[] ors = conditionStr.split(" or ");
			if (ors.length > 1) {
				boolean rtn = false;
				for (String or : ors) {
					rtn |= new If(or).eval(conjugation);
				}
				return rtn;
			}
			String[] ands = conditionStr.split(" and ");
			if (ands.length > 1) {
				boolean rtn = false;
				for (String and : ands) {
					rtn &= new If(and).eval(conjugation);
				}
				return rtn;
			}
			
			Pattern p = Pattern.compile("^([a-zA-Z]+)\\(\\)$");
			Matcher m = p.matcher(conditionStr);
			if (m.matches()) {
				String methodName = m.group(1);
				Method method = conjugation.getClass().getMethod(methodName);
				return method.invoke(conjugation).equals(Boolean.TRUE);
			}
			p = Pattern.compile("^([a-zA-Z]+)\\(\\) (==|!=) ([a-zA-Z]+)$");
			m = p.matcher(conditionStr);
			if (m.matches()) {
				String methodName = m.group(1);
				Method method = conjugation.getClass().getMethod(methodName);
				String result = (String) method.invoke(conjugation);
				
				String operation = m.group(2);
				String str = m.group(3);
				
				if (operation.equals("=="))
					return result.equals(str);
				else if (operation.equals("!="))
					return !result.equals(str);
				else
					throw new IllegalArgumentException("Invalid operation in clitic order condition: "+operation);
			}
			
			throw new IllegalArgumentException("Could not parse condition: "+conditionStr);
		}
	}
	
	public static class Then {
		private String thenClause;
		
		public String eval(Conjugation conjugation) {
			String str = thenClause
				.replace("{prefix}", conjugation.getPrefix())
				.replace("{auxiliary}", conjugation.getConjugatedAuxiliaryStr())
				.replace("{verb}", conjugation.getConjugatedVerbStr());
			
			return str; // FIXME
		}
	}
	
	public static class Condition {
		private final If ifObj;
		private final Then then;
		
		private Condition(If ifObj, Then then) {
			this.ifObj = ifObj;
			this.then = then;
		}

		private Condition(If ifObj) {
			this.ifObj = ifObj;
			this.then = null;
		}

		public If getIf() {
			return ifObj;
		}

		public Then getThen() {
			return then;
		}
	}
}