package vitaly.balance.framework;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


public class GameView extends SurfaceView implements Runnable {
    IGameController gameController;
    SurfaceHolder holder; // для работы с графиккой
    volatile boolean running;
    Thread renderThread;
    TouchHandler touchHandler;
    AccelerometerHandler accelHandler;

    public GameView(Context context, IGameController gameController) {
        super(context);
        this.gameController = gameController;
        holder = getHolder();
        this.touchHandler = new TouchHandler(this);
        this.accelHandler = new AccelerometerHandler(context);

        SoundHelper.initialize(context);

        running = false;
    }

    public void onResume() {
        running = true;
        renderThread = new Thread(this);
        renderThread.start();
        accelHandler.onResume();
        SoundHelper.playGameTheme();
    }

    public void onPause() {
        accelHandler.onPause();
        SoundHelper.stopGameTheme();

        running = false;
        while (true) {
            try {
                renderThread.join();
                return;
            } catch (InterruptedException e) {}
        }

    }

    @Override
    public void run() {
        Rect dstRect = new Rect();
        long startTime = System.nanoTime();

        while (running) {
            if (!holder.getSurface().isValid())
                continue;

            long now = System.nanoTime();
            float deltaTime = (now - startTime) / 1000_000_000f;
            startTime = now;

            gameController.onUpdate(deltaTime, touchHandler.getTouchEvents(), accelHandler.getVector());
            Bitmap frameBuffer = gameController.onDrawingRequested();

            Canvas canvas = holder.lockCanvas();
            canvas.getClipBounds(dstRect);
            canvas.drawBitmap(frameBuffer, null, dstRect, null);
            holder.unlockCanvasAndPost(canvas);
        }
    }
}
