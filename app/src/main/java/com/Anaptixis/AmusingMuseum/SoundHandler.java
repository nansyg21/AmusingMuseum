package com.Anaptixis.AmusingMuseum;

import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.util.Log;

/**
 * Created by panos on 21/12/2015.
 */
public class SoundHandler {

    static int wrong_sound_id,wrong_sound_id2,wrong_sound_id3,wrong_sound_id4;
    static int correct_sound_id, correct_sound_id2, correct_sound_id3,correct_sound_id4;
    static int beep_sound_id, beep_sound_id2, beep_sound_id3,beep_sound_id4;

    static SoundPool soundPool;
    public static void InitiateSoundPool()
    {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes aa = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .build();
            soundPool = new SoundPool.Builder()
                    .setMaxStreams(10)
                    .setAudioAttributes(aa)
                    .build();

        }
        else
        {
            soundPool= new SoundPool(10, AudioManager.STREAM_MUSIC,1);

        }
        wrong_sound_id = SoundHandler.soundPool.load(MainActivity.my_context, R.raw.wrong, 1);
        wrong_sound_id2 = SoundHandler.soundPool.load(MainActivity.my_context, R.raw.wrong2, 1);
        wrong_sound_id3 = SoundHandler.soundPool.load(MainActivity.my_context, R.raw.wrong3, 1);
        wrong_sound_id4 = SoundHandler.soundPool.load(MainActivity.my_context, R.raw.wrong4, 1);
        correct_sound_id = SoundHandler.soundPool.load(MainActivity.my_context, R.raw.correct, 1);
        correct_sound_id2 = SoundHandler.soundPool.load(MainActivity.my_context, R.raw.correct2, 1);
        correct_sound_id3 = SoundHandler.soundPool.load(MainActivity.my_context, R.raw.correct3, 1);
        correct_sound_id4 = SoundHandler.soundPool.load(MainActivity.my_context, R.raw.correct4, 1);

        beep_sound_id = SoundHandler.soundPool.load(MainActivity.my_context, R.raw.beep, 1);
        beep_sound_id2 = SoundHandler.soundPool.load(MainActivity.my_context, R.raw.beep2, 1);
        beep_sound_id3 = SoundHandler.soundPool.load(MainActivity.my_context, R.raw.beep3, 1);
        beep_sound_id4 = SoundHandler.soundPool.load(MainActivity.my_context, R.raw.beep4, 1);

    }

    public static void PlaySound(int sound_id)
    {
        try {
            soundPool.play(sound_id, 1, 1, 1, 0, 1);
        }
        catch(Exception e)  //TODO: throws NullPointerException if the same mini game is restarted. The sound can't be played
        {
            Log.w("Warn","Soundpool, PlaySound Method: "+e.getMessage());
        }

    }


}
