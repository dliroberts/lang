package grammar.output.tts;

import java.io.IOException;

public interface VoiceSynthesiser {
	public void say(String phrase) throws IOException;
	public void repeat() throws IOException;
}