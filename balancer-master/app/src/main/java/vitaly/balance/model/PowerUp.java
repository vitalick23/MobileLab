package vitaly.balance.model;

import android.graphics.PointF;

import vitaly.balance.framework.MathHelper;
import vitaly.balance.gameMVC.gameModel;


public abstract class PowerUp {
    public static final float POWER_UP_RADIUS = 1.5f;
    public static final float DURATION = 5;
    public static final float SLOW_FACTOR = 0.5f;

    public enum powerType {SHIELD, SLOW_TIME, STOP_TIME}

    private PointF position;
    private powerType type;

    public PowerUp(float x, float y, powerType type) {
        position = new PointF(x, y);
        this.type = type;
    }

    public abstract boolean update(gameModel model);//если столкнется с мячом

    protected boolean collides(Ball ball){
        PointF distance = new PointF(ball.getPosition().x-position.x,
                ball.getPosition().y-position.y);

        return POWER_UP_RADIUS+ball.getRadius()> MathHelper.moduleVector(distance);
    }

    public PointF getPosition() {
        return position;
    }

    public powerType getType() {
        return type;
    }

    }
