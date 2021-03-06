/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2010 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
package ti.modules.titanium.media;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollInvocation;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.TiContext;
import org.appcelerator.titanium.TiContext.OnLifecycleEvent;
import org.appcelerator.titanium.util.Log;
import org.appcelerator.titanium.util.TiConfig;
import org.appcelerator.titanium.util.TiConvert;

import ti.modules.titanium.filesystem.FileProxy;
import android.app.Activity;

@Kroll.proxy(creatableInModule=MediaModule.class)
public class AudioPlayerProxy extends KrollProxy
	implements OnLifecycleEvent
{
	private static final String LCAT = "AudioPlayerProxy";
	private static final boolean DBG = TiConfig.LOGD;

	@Kroll.constant public static final int STATE_BUFFERING = TiSound.STATE_BUFFERING;
	@Kroll.constant public static final int STATE_INITIALIZED = TiSound.STATE_INITIALIZED;
	@Kroll.constant public static final int STATE_PAUSED = TiSound.STATE_PAUSED;
	@Kroll.constant public static final int STATE_PLAYING = TiSound.STATE_PLAYING;
	@Kroll.constant public static final int STATE_STARTING = TiSound.STATE_STARTING;
	@Kroll.constant public static final int STATE_STOPPED = TiSound.STATE_STOPPED;
	@Kroll.constant public static final int STATE_STOPPING = TiSound.STATE_STOPPING;
	@Kroll.constant public static final int STATE_WAITING_FOR_DATA = TiSound.STATE_WAITING_FOR_DATA;
	@Kroll.constant public static final int STATE_WAITING_FOR_QUEUE = TiSound.STATE_WAITING_FOR_QUEUE;
	
	protected TiSound snd;
	private String lastUrl;

	public AudioPlayerProxy(TiContext tiContext)
	{
		super(tiContext);

		tiContext.addOnLifecycleEventListener(this);
		setProperty("volume", 0.5, true);
	}

	@Override
	public void handleCreationDict(KrollDict options) {
		super.handleCreationDict(options);
		if (options.containsKey(TiC.PROPERTY_URL)) {
			setProperty(TiC.PROPERTY_URL, getTiContext().resolveUrl(null, TiConvert.toString(options, TiC.PROPERTY_URL)));
		} else if (options.containsKey(TiC.PROPERTY_SOUND)) {
			FileProxy fp = (FileProxy) options.get(TiC.PROPERTY_SOUND);
			if (fp != null) {
				String url = fp.getNativePath();
				setProperty(TiC.PROPERTY_URL, url);
			}
		}
		if (options.containsKey(TiC.PROPERTY_ALLOW_BACKGROUND)) {
			setProperty(TiC.PROPERTY_ALLOW_BACKGROUND, options.get(TiC.PROPERTY_ALLOW_BACKGROUND));
		}
		if (DBG) {
			Log.i(LCAT, "Creating audio player proxy for url: " + TiConvert.toString(getProperty("url")));
		}
	}
	
	
	@Kroll.getProperty
	public String getUrl() {
		return lastUrl;
	}
	
	@Kroll.setProperty @Kroll.method
	public void setUrl(KrollInvocation kroll, String url) {
		if (url != null) {
			url = TiConvert.toString(url);
			lastUrl = url;

			TiSound s = getSound();
			if (s != null){
				s.changeUrl(url);
			}
		}
	}
	
	@Kroll.getProperty @Kroll.method
	public int getDuration() {
		TiSound s = getSound();
		if (s != null) {
			return s.getDuration();
		}
		return 0;
	}
	
	@Kroll.getProperty @Kroll.method
	public int getTime() {
		TiSound s = getSound();
		if (s != null) {
			return s.getTime();
		}
		return 0;
	}
	
	@Kroll.setProperty @Kroll.method
	public void setTime(int time) {
		TiSound s = getSound();
		if (s != null) {
			s.setTime(time);
		}
	}
	
	@Kroll.getProperty @Kroll.method
	public float getVolume(){
		TiSound s= getSound();
		if (s != null){
			return s.getVolume();
		}
		return 0.0f;
	}
	
	@Kroll.setProperty @Kroll.method
	public void setVolume(float volume){
		TiSound s = getSound();
		if (s != null){
			s.setVolume(volume);
		}
	}
	
	@Kroll.getProperty @Kroll.method
	public boolean isPlaying() {
		TiSound s = getSound();
		if (s != null) {
			return s.isPlaying();
		}
		return false;
	}

	@Kroll.getProperty @Kroll.method
	public boolean isPaused() {
		TiSound s = getSound();
		if (s != null) {
			return s.isPaused();
		}
		return false;
	}

	// An alias for play so that
	@Kroll.method
	public void start() {
		play();
	}

	@Kroll.method
	public void play() {
		TiSound s = getSound();
		if (s != null) {
			s.play();
		}
	}

	@Kroll.method
	public void pause() {
		TiSound s = getSound();
		if (s != null) {
			s.pause();
		}
	}
	
	@Kroll.method
	public void reset(){
		TiSound s = getSound();
		if (s != null){
			s.reset();
		}
	}
	
	@Kroll.method
	public void release() {
		TiSound s = getSound();
		if (s != null) {
			s.release();
			snd = null;
		}
	}

	@Kroll.method
	public void destroy() {
		release();
	}

	@Kroll.method
	public void stop() {
		TiSound s = getSound();
		if (s != null) {
			s.stop();
		}
	}
	
	@Kroll.getProperty @Kroll.method
	public boolean isPrepared() {
		TiSound s = getSound();
		if (s != null) {
			return s.isPrepared();
		}
		return false;
	}
	
	protected TiSound getSound()
	{
		if (snd == null) {
			snd = new TiSound(this);
			setModelListener(snd);
		}
		return snd;
	}

	private boolean allowBackground() {
		boolean allow = false;
		if (hasProperty(TiC.PROPERTY_ALLOW_BACKGROUND)) {
			allow = TiConvert.toBoolean(getProperty(TiC.PROPERTY_ALLOW_BACKGROUND));
		}
		return allow;
	}

	public void onStart(Activity activity) {
	}

	public void onResume(Activity activity) {
		if (!allowBackground()) {
			if (snd != null) {
				snd.onResume();
			}
		}
	}

	public void onPause(Activity activity) {
		if (!allowBackground()) {
			if (snd != null) {
				snd.onPause();
			}
		}
	}

	public void onStop(Activity activity) {
	}

	public void onDestroy(Activity activity) {
		if (snd != null) {
			snd.onDestroy();
		}
		snd = null;
	}
}
