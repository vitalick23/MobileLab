package vitaly.balance.framework;

import android.graphics.Bitmap;

import vitaly.balance.framework.TouchHandler.TouchEvent;

import java.util.List;


public interface IGameController {
    void onUpdate(float deltaTime, List<TouchEvent> touchEvents, float[] vector);
    Bitmap onDrawingRequested();
}
