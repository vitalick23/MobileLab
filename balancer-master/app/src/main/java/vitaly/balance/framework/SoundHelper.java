package vitaly.balance.framework;

import android.content.Context;
import android.media.MediaPlayer;

import vitaly.balance.R;


public class SoundHelper {


    private static MediaPlayer mediaPlayer;

    public static void initialize(Context context) {
        mediaPlayer = MediaPlayer.create(context, R.raw.ecstasyx);
        mediaPlayer.setLooping(true);
    }

    public static void playGameTheme(){
        mediaPlayer.start();
    }

    public static void stopGameTheme(){
        mediaPlayer.stop();
    }

}
