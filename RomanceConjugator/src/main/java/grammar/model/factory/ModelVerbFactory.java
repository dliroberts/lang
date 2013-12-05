package grammar.model.factory;

import grammar.model.Language;
import grammar.model.verbs.AuxiliaryVerb;
import grammar.model.verbs.ModelVerb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ModelVerbFactory {
	private static ModelVerbFactory instance;
	
	private final Set<ModelVerb> candidateModels;
	private final Set<AuxiliaryVerb> modelAuxiliaries = new HashSet<AuxiliaryVerb>();
	
	public ModelVerbFactory() {
		if (instance != null)
			throw new IllegalStateException(
					"ModelVerbFactory cannot be instantiated more than once.");
		candidateModels = new HashSet<ModelVerb>(Arrays.asList(ModelVerb.values()));
		for (ModelVerb modelVerb : candidateModels) {
			if (modelVerb instanceof AuxiliaryVerb)
				modelAuxiliaries.add((AuxiliaryVerb) modelVerb);
		}
		instance = this;
	}
	
	private String lastInfinitive;
	private ModelVerb lastModelVerb;
	
	public ModelVerb getModelVerb(String infinitive, Language language) {
		return getModelVerb(infinitive, language, language.isReflexive(infinitive));
	}
	
	public ModelVerb getModelVerb(String verb, Language language, boolean reflexive) {
		if (lastInfinitive != null && lastInfinitive.equals(verb))
			return lastModelVerb;
		
		ModelVerb bestFit = null;
		
//		System.out.println("Model verbs: "+candidateModels);
		
		List<ModelVerb> matches = new ArrayList<ModelVerb>(); // For debugging only
		
		for (ModelVerb current : candidateModels) {
			if (current.matches(verb, reflexive)) {
				matches.add(current);
				if (bestFit == null || current.isCloserMatch(verb, reflexive, bestFit)) {
					bestFit = current;
				}
			}
		}
//		System.out.println("Matching models: "+matches);
		if (bestFit == null)
			throw new IllegalArgumentException("No model verb match found for infinitive "+verb+".");
		
		lastInfinitive = verb;
		return lastModelVerb = bestFit;
	}
	
	public AuxiliaryVerb getModelAuxiliaryVerb(String infinitive, boolean reflexive) {
		AuxiliaryVerb bestFit = null;
		
//		System.out.println("Model auxiliary verbs: "+modelAuxiliaries);
		
		List<AuxiliaryVerb> matches = new ArrayList<AuxiliaryVerb>(); // For debugging only
		
		for (AuxiliaryVerb current : modelAuxiliaries) {
			if (current.isAuxiliaryOf(infinitive, reflexive)) {
				matches.add(current);
				if (bestFit == null || current.isCloserMatch(infinitive, reflexive, bestFit)) {
					bestFit = current;
				}
			}
		}
//		System.out.println("Matching auxiliaries: "+matches);
		if (bestFit == null)
			throw new IllegalArgumentException("No auxiliary verb match found for infinitive "+infinitive+".");
		
		return bestFit;
	}

	public static ModelVerbFactory getInstance() {
		if (instance == null)
			instance = new ModelVerbFactory();
		return instance;
	}
}