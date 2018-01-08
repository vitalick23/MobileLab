package vitaly.balance;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import vitaly.balance.model.PowerUp;
import vitaly.balance.model.Projectile;
import vitaly.balance.gameMVC.gameModel;


public class Assets {

    public static Bitmap ball, arrow_wind, arrow, missile, pauseButton, playButton,
            shieldPower, stopPower, slowPower;


    public static void createAssets(Context context, int ballRadius) {

        if (ball != null)
            ball.recycle();


        Resources resources = context.getResources();

        ball = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(resources, R.drawable.ball),
                ballRadius * 2, ballRadius * 2, true);

        arrow_wind = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(resources, R.drawable.arrow_wind),
                ballRadius, ballRadius, true);

        arrow = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(resources, R.drawable.arrow),
                (int) (ballRadius * Projectile.WIDTH), (int) (ballRadius * Projectile.HEIGHT), true);

        missile = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(resources, R.drawable.missile),
                (int) (ballRadius * Projectile.WIDTH), (int) (ballRadius * Projectile.HEIGHT), true);

        pauseButton = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(resources, R.drawable.pause),
                (int) (ballRadius * gameModel.PAUSE_BUTTON_DIMENSIONS),
                (int) (ballRadius * gameModel.PAUSE_BUTTON_DIMENSIONS), true);

        playButton = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(resources, R.drawable.play),
                (int) (ballRadius * gameModel.PAUSE_BUTTON_DIMENSIONS),
                (int) (ballRadius * gameModel.PAUSE_BUTTON_DIMENSIONS), true);

        shieldPower = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(resources, R.drawable.shield),
                (int) (ballRadius * PowerUp.POWER_UP_RADIUS*2), (int) (ballRadius * PowerUp.POWER_UP_RADIUS*2), true);

        slowPower = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(resources, R.drawable.hourglass),
                (int) (ballRadius * PowerUp.POWER_UP_RADIUS*2), (int) (ballRadius * PowerUp.POWER_UP_RADIUS*2), true);

        stopPower = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(resources, R.drawable.stop),
                (int) (ballRadius * PowerUp.POWER_UP_RADIUS*2), (int) (ballRadius * PowerUp.POWER_UP_RADIUS*2), true);


    }
}
