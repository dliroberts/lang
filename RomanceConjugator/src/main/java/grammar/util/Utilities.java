package grammar.util;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Utilities {
	public static String asConstantName(String s) {
		return s.toUpperCase().replace(' ', '_').replace('-', '_');
	}
	
	public static String asHumanReadableName(String s) {
		return s.toLowerCase().replace('_', ' ');
	}

	/**
	 * Ensures that a Map instance exists in parentMap for the given key
	 * parentKey. Either such a child Map exists as a value in parentMap for the
	 * given key and is returned, or a new Map is created and inserted into
	 * parentMap prior to being returned. Useful for building up nested Map structures.
	 * 
	 * @param <L> The key type of the Map returned by the method.
	 * @param <V> The value type of the Map returned by the method.
	 * @param <P> The parent map's key type.
	 * @param parentKey
	 * @param parentMap
	 * @return If parentMap.get(parentKey) != null, then that call's return value is returned.
	 * Otherwise, a new Map is created, inserted into parentMap, and finally returned.
	 */
	public static <L, V, P> Map<L, V> initialiseIfReqd(P parentKey, Map<P, Map<L, V>> parentMap) {
		Map<L, V> q = parentMap.get(parentKey);
		if (q == null) {
			q = new HashMap<L, V>();
			parentMap.put(parentKey, q);
			return q;
		}
		return q;
	}
	
	public static <S> String formatForPrinting(Collection<S> scope) {
		return formatForPrinting(scope, scope.size());
	}
	
	public static <S> String formatForPrinting(Collection<S> scope, int nElements) {
		StringBuilder sb = new StringBuilder();
		Collection<S> s = new ArrayList<S>();
		if (scope.size() > nElements) {
			Iterator<S> iterator = scope.iterator();
			for (int i = 0; i < nElements; i++) {
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
			if (i < sz-2)
				sb.append(", ");
			else if (i == sz-2) {
				if (sz == scope.size())
					sb.append(" and ");
				else
					sb.append(", ");
			}
			i++;
		}
		if (scope.size() > nElements) {
			sb.append("... (");
			sb.append(scope.size() - nElements);
			sb.append(" more)");
		}
		return sb.toString();
	}
	
	public static boolean equalsIgnoreAccents(String s1, String s2) {
		return stripAccents(s1).equals(stripAccents(s2));
	}
	
	public static boolean equalsIgnoreAccentsAndCase(String s1, String s2) {
		return equalsIgnoreAccents(s1.toLowerCase(), s2.toLowerCase());
	}
	
	public static String stripAccents(String str) {
		return Normalizer.normalize(str, Normalizer.Form.NFD).
			replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
	}
}