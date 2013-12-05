package grammar.model;

/**
 * <p>For types whose instances are created on load only. From then on, behaviour
 * resembles an enum.</p>
 * 
 * <p>The following static methods should also be implemented (this cannot be
 * enforced in Java, as static abstract methods are disallowed):</p>
 * 
 * <ul>
 *     <li>public static T[] values();
 *     <li>public static T valueOf(String key);
 * </ul>
 * 
 * (Where T is the class name).
 * 
 * @author Duncan Roberts
 */
public interface PseudoEnum<T> extends Comparable<T> {
	public int ordinal();
	
	//public static T[] values();
	//public static T valueOf(String key);
}