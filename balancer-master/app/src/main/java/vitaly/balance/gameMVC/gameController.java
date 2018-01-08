package vitaly.balance.gameMVC;

import android.content.Context;
import android.graphics.Bitmap;

import java.util.List;

import vitaly.balance.Assets;
import vitaly.balance.framework.Graphics;
import vitaly.balance.framework.IGameController;
import vitaly.balance.framework.MathHelper;
import vitaly.balance.framework.TouchHandler;
import vitaly.balance.model.ButtonModel;
import vitaly.balance.model.Danger;
import vitaly.balance.model.Hole;
import vitaly.balance.model.Laser;
import vitaly.balance.model.PowerUp;
import vitaly.balance.model.Projectile;

import static vitaly.balance.framework.TouchHandler.TouchType.TOUCH_DOWN;
import static vitaly.balance.framework.TouchHandler.TouchType.TOUCH_DRAGGED;
import static vitaly.balance.framework.TouchHandler.TouchType.TOUCH_UP;


public class gameController implements IGameController {

    private static final float BOARD_SCREEN_RATIO = 1f;
    private static final int BOARD_COLOR = 0xff_ff_ff_ff;
    private static final int BACKGROUND_COLOR = 0xff_00_00_55;
    private static final int COLOR_HOLE_FULL = 0xff_99_00_00;
    private static final int COLOR_HOLE_PARTIAL = (COLOR_HOLE_FULL & 0x00_ff_ff_ff) + 0x55_00_00_00;
    private static final float SCALE_WIND = 0.2f;
    private static final int COLOR_TIMER = 0x55_33_ff_ff;
    private static final int COLOR_RESTART_TEXT = 0xFF_55_ff_ff;
    private static final float TIMER_BOARD_PROPORTION = 0.90f;
    private static final int COLOR_LASER_FULL = 0xff_ff_00_00;
    private static final int COLOR_LASER_PARTIAL = (COLOR_LASER_FULL & 0x00_ff_ff_ff) + 0x55_00_00_00;
    private static final int BALLS_IN_WIDTH = 33;
    public static final int RESTART_BUTTON_TEXT_SIZE = 20;
    public static final int COLOR_PAUSE_FILTER = 0x55_00_00_00;
    public static final int COLOR_SHIELD = 0x55_00_FF_00;
    public static final int COLOR_FILTER_SLOW_TIME = 0x55_99_57_97;
    public static final int COLOR_FILTER_STOP_TIME = 0x55_33_ff_ff;

    private int xTouch, yTouch, xBoard, yBoard, ballRadius, boardPixelWidth, boardPixelHeight,
            screenCenterX, screenCenterY;
    private boolean touching;
    private String timer;

    private ButtonModel pauseButton, restartButton;

    private Graphics graphics;
    private gameModel model;


    public gameController(Context context, int width, int height) {

        this.touching = false;

        graphics = new Graphics(width, height);


        ballRadius = (int) (BOARD_SCREEN_RATIO * height / BALLS_IN_WIDTH);
        boardPixelWidth = width;
        boardPixelHeight = height;
        xBoard = (width - boardPixelWidth) / 2;
        yBoard = (height - boardPixelHeight) / 2;

        screenCenterX = graphics.getWidth() / 2;
        screenCenterY = graphics.getHeight() / 2;


        model = new gameModel(width / ballRadius, height / ballRadius);
        Assets.createAssets(context, ballRadius);

        pauseButton = model.getPauseButton();
        restartButton = model.getRestartButton();
    }

    @Override
    public void onUpdate(float deltaTime, List<TouchHandler.TouchEvent> touchEvents, float[] accelEvent) {
        touching = false;
        for (TouchHandler.TouchEvent event : touchEvents)
            if (event.type == TOUCH_DOWN || event.type == TOUCH_DRAGGED) {
                touching = true;
                xTouch = event.x;
                yTouch = event.y;
            } else if (event.type == TOUCH_UP)
                model.onTouch(xw2xb(event.x), yw2yb(event.y));

        model.setGravityX(accelEvent[1]);//Перевернутая ось
        model.setGravityY(accelEvent[0]);

        model.update(deltaTime);
    }

    @Override
    public Bitmap onDrawingRequested() {
        graphics.clear(BACKGROUND_COLOR);


        //BOARD
        graphics.drawRect(xBoard, yBoard, boardPixelWidth, boardPixelHeight, BOARD_COLOR);

        //TIMER
        timer = toString().valueOf(Math.round(model.getTotalTime()));
        graphics.drawTextCentered(screenCenterX, screenCenterY,
                timer, COLOR_TIMER, boardPixelHeight * TIMER_BOARD_PROPORTION);

        //DANGERS
        drawDangers();
        //WIND FORCE
        graphics.drawRotatedBitmap(Assets.arrow_wind, screenCenterX,
                screenCenterY, MathHelper.toDegrees(model.getWind().getAngle()), model.getWind().getStrength() * SCALE_WIND);

        //POWER-UPS
        drawPowerUp();

        //SLOW TIME COLOR FILTER
        if (model.isSlowingTime())
            graphics.drawRect(0, 0, boardPixelWidth, boardPixelHeight, COLOR_FILTER_SLOW_TIME);
        else if (model.isTimeStopped())
            graphics.drawRect(0, 0, boardPixelWidth, boardPixelHeight, COLOR_FILTER_STOP_TIME);

        //BALL
        graphics.drawBitmap(Assets.ball, xb2xw(model.getBall().getPosition().x - 1),
                yb2yw(model.getBall().getPosition().y - 1));

        if (model.getBall().isShielded())
            graphics.drawCircle((int) xb2xw(model.getBall().getPosition().x),
                    (int) yb2yw(model.getBall().getPosition().y), ballRadius * 2, COLOR_SHIELD);

        //PAUSE
        if (model.getState() != gameModel.gameState.PLAYING)
            graphics.drawRect(0, 0, boardPixelWidth, boardPixelHeight, COLOR_PAUSE_FILTER);

        //BUTTONS
        if (pauseButton.isVisible()) {
            if (model.getState() == gameModel.gameState.PLAYING)
                graphics.drawBitmap(Assets.pauseButton, xb2xw(pauseButton.getX()), xb2xw(pauseButton.getY()));
            else
                graphics.drawBitmap(Assets.playButton, xb2xw(pauseButton.getX()), xb2xw(pauseButton.getY()));
        }


        if (restartButton.isVisible()) {
            graphics.drawRect((int) xb2xw(restartButton.getX()), (int) xb2xw(restartButton.getY()),
                    (int) (restartButton.getWidth() * ballRadius), (int) (restartButton.getHeight() * ballRadius),
                    BACKGROUND_COLOR);

            graphics.drawTextCentered(screenCenterX, screenCenterY, "Score: " + timer, COLOR_RESTART_TEXT, RESTART_BUTTON_TEXT_SIZE);

            graphics.drawTextCentered(screenCenterX, screenCenterY + RESTART_BUTTON_TEXT_SIZE,
                    "Click anywhere in this panel to restart", COLOR_RESTART_TEXT, RESTART_BUTTON_TEXT_SIZE);
        }





        return graphics.getFrameBuffer();
    }

    private void drawPowerUp() {
        if (model.getPowerUp() != null) {
            PowerUp power = model.getPowerUp();

            switch (power.getType()) {
                case SHIELD:
                    graphics.drawBitmap(Assets.shieldPower, xb2xw(power.getPosition().x),
                            xb2xw(power.getPosition().y));
                    break;

                case SLOW_TIME:
                    graphics.drawBitmap(Assets.slowPower, xb2xw(power.getPosition().x),
                            xb2xw(power.getPosition().y));
                    break;

                case STOP_TIME:
                    graphics.drawBitmap(Assets.stopPower, xb2xw(power.getPosition().x),
                            xb2xw(power.getPosition().y));
                    break;


            }
        }
    }

    private void drawDangers() {
        for (Danger danger : model.getAllDangers()) {
            if (danger instanceof Hole) {

                Hole hole = (Hole) danger;

                int color;
                if (danger.getState() == Danger.dangerState.ONGOING)
                    color = COLOR_HOLE_FULL;
                else
                    color = COLOR_HOLE_PARTIAL;

                graphics.drawCircle((int) xb2xw(hole.getCenter().x), (int) yb2yw(hole.getCenter().y),
                        (int) (hole.getRadius() * ballRadius), color);

            } else if (danger instanceof Laser) {

                Laser laser = (Laser) danger;

                int color;
                if (laser.getState() == Danger.dangerState.ONGOING)
                    color = COLOR_LASER_FULL;
                else
                    color = COLOR_LASER_PARTIAL;

              int p1X = (int) xb2xw(laser.getPoint1().x);
                int p1Y = (int) yb2yw(laser.getPoint1().y);
                int p2X = (int) xb2xw(laser.getPoint2().x);
                int p2Y = (int) yb2yw(laser.getPoint2().y);

                graphics.drawLine(p1X, p1Y, p2X, p2Y, (int) (laser.getWidth() * ballRadius), color);

            } else if (danger instanceof Projectile) {
                Projectile projectile = (Projectile) danger;

                int projectileX = (int) xb2xw(projectile.getCenter().x);
                int projectileY = (int) yb2yw(projectile.getCenter().y);

                Bitmap bmp;
                if (projectile.isChasesBall()) bmp = Assets.missile;
                else bmp = Assets.arrow;

                graphics.drawRotatedBitmap(bmp, projectileX, projectileY,
                        MathHelper.toDegrees(projectile.getAngle()), 1); //Scale is given when creating the assets
            }
        }
    }

    private float yw2yb(float yw) {
        return (yw - yBoard) / ballRadius;
    }

    private float xw2xb(float xw) {
        return (xw - xBoard) / ballRadius;
    }

    private float yb2yw(float yb) {
        return yb * ballRadius + yBoard;
    }

    private float xb2xw(float xb) {
        return xb * ballRadius + xBoard;
    }
}
