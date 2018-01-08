package vitaly.balance.gameMVC;

import android.util.DisplayMetrics;

import vitaly.balance.framework.GameActivity;
import vitaly.balance.framework.IGameController;



public class gameView extends GameActivity {

    @Override
    protected IGameController buildGameController() {

        IGameController controller;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        controller = new gameController(this, displayMetrics.widthPixels, displayMetrics.heightPixels);
        return controller;
    }
}
