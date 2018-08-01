package client;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javafx.scene.media.*;

public final class AudioClipFactory {
	private static final Map<String, AudioClip> cache = new HashMap<String, AudioClip>();

	static synchronized AudioClip getAudioClip(String spec) throws IOException {
		AudioClip audioClip = cache.get(spec);
		if (audioClip == null) {
			audioClip = new AudioClip(spec);
			cache.put(spec, audioClip);
		}
		return audioClip;
	}
}
