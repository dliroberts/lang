package grammar.model.factory;

import grammar.model.nouns.NounClass;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NounClassFactory {
	private static NounClassFactory instance;
	
	private final Set<NounClass> candidateModels;
	
	public NounClassFactory() {
		if (instance != null)
			throw new IllegalStateException(
					"ModelVerbFactory cannot be instantiated more than once.");
		candidateModels = new HashSet<NounClass>(Arrays.asList(NounClass.values()));
		instance = this;
	}
	
	private String lastInfinitive;
	private NounClass lastModelVerb;
	
	public NounClass getClosestMatch(String noun) {
		if (lastInfinitive != null && lastInfinitive.equals(noun))
			return lastModelVerb;
		
		NounClass bestFit = null;
		
//		System.out.println("Model verbs: "+candidateModels);
		
		List<NounClass> matches = new ArrayList<NounClass>(); // For debugging only
		
		for (NounClass current : candidateModels) {
			if (current.matches(noun)) {
				matches.add(current);
				if (bestFit == null || current.isCloserMatch(noun, bestFit)) {
					bestFit = current;
				}
			}
		}
//		System.out.println("Matching models: "+matches);
		if (bestFit == null)
			throw new IllegalArgumentException("No noun class match found for infinitive "+noun+".");
		
		lastInfinitive = noun;
		return lastModelVerb = bestFit;
	}
	
	public static NounClassFactory getInstance() {
		if (instance == null)
			instance = new NounClassFactory();
		return instance;
	}
}