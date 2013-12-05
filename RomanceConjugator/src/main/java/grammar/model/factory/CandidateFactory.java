package grammar.model.factory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CandidateFactory<C extends Candidate> {
	private static final Logger LOGGER = LoggerFactory.getLogger(CandidateFactory.class);
	
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
		
		List<C> matches = null;
		if (LOGGER.isDebugEnabled())
			matches = new ArrayList<C>();
		
		for (C current : candidateModels) {
			if (current.matches(infinitive)) {
				if (LOGGER.isDebugEnabled())
					matches.add(current);
				if (bestFit == null || current.isCloserMatch(infinitive, bestFit)) {
					bestFit = current;
				}
			}
		}
		LOGGER.debug("Matching models: {}", matches);
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