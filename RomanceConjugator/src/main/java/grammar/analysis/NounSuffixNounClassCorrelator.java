package grammar.analysis;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import grammar.input.xml.DataManager;
import grammar.model.Language;
import grammar.model.MatchType;
import grammar.model.Multiplicity;
import grammar.model.WordMatcher;
import grammar.model.nouns.NounClass;
import grammar.model.nouns.NounForm;
import grammar.util.Utilities;

public class NounSuffixNounClassCorrelator {
	private static final double RULE_ACCEPTANCE_THRESHOLD        = 0.8d;
	private static final double SAMPLE_SIZE_ACCEPTANCE_THRESHOLD = 70;
	private static final double MAX_SUFFIX_LENGTH                = 5;
	
	public static void main(String[] args) throws IOException {
		new NounSuffixNounClassCorrelator().findNounClassRules();
	}
	
	public void findNounClassRules() throws IOException {
		Object o = DataManager.INSTANCES; // FIXME - neater way to kickstart init of DataManager?
		Language language = Language.valueOf("FRENCH");
		DataManager.getInstance(language).load();
		
		nfs = NounForm.getForms(null, Multiplicity.SINGULAR);
		
		SortedSet<NounClassRule> rules = new TreeSet<NounClassRule>();
		
		Map<NounClass, StringBuilder> sbs = new HashMap<NounClass, StringBuilder>();
		for (NounClass nc : NounClass.values()) {
			sbs.put(nc, new StringBuilder());
		}
		
		for (int chars = 1; chars <= MAX_SUFFIX_LENGTH; chars++) {
			SortedSet<NounMatcher> suffixSet = getSuffixes(chars);
			for (NounMatcher nm : suffixSet) {
				for (NounClass nc : NounClass.values()) {
					Set<NounForm> matchingNouns = findMatches(nm);
					int total = matchingNouns.size();
					
					int matches = 0;
					for (NounForm nf : matchingNouns) {
						if (nf.getNounClasses().contains(nc))
							matches++;
					}
					
					NounClassRule ncr = new NounClassRule(nm.getMatchType(), nm.getMatchString(), nc, matches, total);
					
					if (ncr.getReliability() >= RULE_ACCEPTANCE_THRESHOLD && total >= SAMPLE_SIZE_ACCEPTANCE_THRESHOLD) {
						if (!isRuleRedundant(rules, ncr))
							rules.add(ncr);
					}
				}
				
			}
		}
		
		int total = nfs.length;
		int coverage = 0;
		Set<NounClassRule> soFar = new HashSet<NounClassRule>();
		for (NounClassRule ncr : rules) {
			boolean increasesCoverage = true;
			for (NounClassRule previous : soFar) {
				if (ncr.getMatchString().endsWith(previous.getMatchString()))
					increasesCoverage = false;
			}
			if (increasesCoverage)
				coverage += ncr.getMatches();
			soFar.add(ncr);
			
			sbs.get(ncr.getNounClass()).append(
					ncr.getMatchString()+
					" is "+Utilities.asHumanReadableName(ncr.getNounClass().getName())+" "+
					Math.round(ncr.getReliability()*100)+"% of the time. "+
					"Sample size: "+ncr.getInstances()+".\n");
		}
		
		for (StringBuilder sb : sbs.values()) {
			System.out.println(sb.toString());
		}
		
		System.out.println("These rules cover "+Math.round(((double) coverage/(double) total)*100)+"% of the total ("+total+").");
	}
	
	private boolean isRuleRedundant(Collection<NounClassRule> rules, NounClassRule ncr) {
		String matchString = ncr.getMatchString();
		
		for (NounClassRule existing : rules) {
			if (matchString.endsWith(existing.getMatchString()) && existing.getNounClass().equals(ncr.getNounClass()))
				return true;
		}
		
		return false;
	}
	
	private NounForm[] nfs;

	private Set<NounForm> findMatches(NounMatcher nm) {
		//nfs = NounForm.values();
		
		Set<NounForm> matchingForms = new HashSet<NounForm>();
		
		for (NounForm nf : nfs) {
			if (nf.getText().endsWith(nm.getMatchString()))
				matchingForms.add(nf);
		}
		return matchingForms;
	}
	
	private SortedSet<NounMatcher> getSuffixes(int length) {
		SortedMap<String, NounMatcher> strs = new TreeMap<String, NounMatcher>();
		NounForm[] nounForms = NounForm.values();
		
		for (NounForm nf : nounForms) {
			String text = nf.getText();
			if (text.length() < length)
				continue;
			String suffix = text.substring(text.length()-length);
			
			if (strs.keySet().contains(suffix))
				strs.get(suffix).incrementInstances();
			else
				strs.put(suffix, new NounMatcher(MatchType.SUFFIX, suffix));
		}
		
		return new TreeSet<NounMatcher>(strs.values());
	}
}

class NounMatcher extends WordMatcher implements Comparable<NounMatcher> {
	private int instances = 1;

	public NounMatcher(MatchType matchType, String matchString) {
		super(matchType, matchString);
	}
	
	public NounMatcher(MatchType matchType, String matchString, int total) {
		this(matchType, matchString);
		this.instances = total;
	}

	public int getInstances() {
		return instances;
	}

	public void incrementInstances() {
		instances++;
	}

	public int compareTo(NounMatcher o) {
		if (o.instances == instances)
			return o.getMatchString().length() - getMatchString().length();
			
		return o.instances - instances;
	}
}

class NounClassRule extends NounMatcher {
	private final NounClass nounClass;
	private final int matches;
	
	public NounClassRule(MatchType matchType, String matchString,
			NounClass nounClass, int matches, int total) {
		super(matchType, matchString, total);
		this.nounClass = nounClass;
		this.matches = matches;
	}

	public NounClass getNounClass() {
		return nounClass;
	}

	public int getMatches() {
		return matches;
	}
	
	public double getReliability() {
		return (double) matches / (double) getInstances();
	}
	
	public int compareTo(NounClassRule o) {
		return o.matches - matches;
	}
}