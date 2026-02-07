package com.priyansh.vedicMaths;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;

public class SoundManager {

    private static SoundPool soundPool;
    private static int dialTickSound;
    private static int buttonClickSound;

    public static void init(Context context) {
        if (soundPool != null) return;

        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(attributes)
                .build();

        dialTickSound = soundPool.load(context, R.raw.dial_tick_short, 1);
        buttonClickSound = soundPool.load(context, R.raw.button_click, 1);
    }

    public static void playDialTick() {
        if (soundPool != null) {
            soundPool.play(dialTickSound, 1f, 1f, 1, 0, 1f);
        }
    }

    public static void playButtonClick() {
        if (soundPool != null) {
            soundPool.play(buttonClickSound, 1f, 1f, 1, 0, 1f);
        }
    }

    public static void release() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }
}