package grammar.output.tts.french;

import grammar.output.tts.VoiceSynthesiser;

import java.io.IOException;
import t2s.son.LecteurTexte;

public class FrenchVoiceSynthesiser implements VoiceSynthesiser {
	private LecteurTexte synthesiserDelegate;

	public void repeat() throws IOException {
		if (synthesiserDelegate == null)
			throw new IllegalArgumentException("Nothing to repeat!");
		synthesiserDelegate.playAll();
	}
	
	public void say(String phrase) throws IOException {
	//		if (synthesiserDelegate == null)
	//		synthesiserDelegate = new LecteurTexte();
	//	synthesiserDelegate.setTexte(phrase);
	//	synthesiserDelegate.playAll();
		
		synthesiserDelegate = new LecteurTexte(phrase);
		synthesiserDelegate.playAll();
	}
}