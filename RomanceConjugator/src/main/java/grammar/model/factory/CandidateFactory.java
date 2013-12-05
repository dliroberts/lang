package grammar.model.factory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CandidateFactory<C extends Candidate> {
	private static CandidateFactory instance;
	
	private final Set<C> candidateModels;
	
	public CandidateFactory(Class<Candidate> cl) {
		if (instance != null)
			throw new IllegalStateException(
					"ModelVerbFactory cannot be instantiated more than once.");
		try {
			candidateModels = new HashSet<C>(Arrays.asList((C[]) cl.getMethod("values").invoke(null)));
		} catch (Exception e) {
			throw new Error(e);
		}
		instance = this;
	}
	
	private String lastInfinitive;
	private C lastModelVerb;
	
	public C getBestCandidate(String infinitive) {
		if (lastInfinitive != null && lastInfinitive.equals(infinitive))
			return lastModelVerb;
		
		C bestFit = null;
		
		List<C> matches = new ArrayList<C>(); // For debugging only
		
		for (C current : candidateModels) {
			if (current.matches(infinitive)) {
				matches.add(current);
				if (bestFit == null || current.isCloserMatch(infinitive, bestFit)) {
					bestFit = current;
				}
			}
		}
//		System.out.println("Matching models: "+matches);
		if (bestFit == null)
			throw new IllegalArgumentException("No gender match found for infinitive "+infinitive+".");
		
		lastInfinitive = infinitive;
		return lastModelVerb = bestFit;
	}
	
	public static CandidateFactory getInstance(Class<Candidate> cl) {
		if (instance == null)
			instance = new CandidateFactory(cl);
		return instance;
	}
}