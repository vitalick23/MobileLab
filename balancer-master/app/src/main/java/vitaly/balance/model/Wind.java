package vitaly.balance.model;

import static vitaly.balance.framework.MathHelper.*;

public class Wind implements Danger {
    private static final float MAX_STRENGTH = 20;
    private static final float MIN_STRENGTH = 15;
    private static final float ANGLE_VARIATION = PI/100;
    private static final float STRENGTH_VARIATION = 1;

    private float angle, strength;
    private boolean stopped;

    public Wind(float angle, float strength) {
        this.angle = angle;
        this.strength = Math.min(MAX_STRENGTH, strength);
    }

    public Wind() {
        this((float) (Math.random() * 2 * Math.PI),
                (float) Math.random() * (MAX_STRENGTH - MIN_STRENGTH + 1.0f) + MIN_STRENGTH);
    }

    public boolean update(float deltaTime, Ball ball) {

        if(!stopped) {
            angle += ((Math.random() * 2.0f) - 1.0f) * ANGLE_VARIATION;
            angle %= 2 * Math.PI;
            strength += ((Math.random() * 2.0f) - 1.0f) * STRENGTH_VARIATION;
            strength = Math.min(MAX_STRENGTH, Math.max(MIN_STRENGTH, strength));

            ball.applyForce(deltaTime, (float) Math.cos(angle) * strength,
                    (float) Math.sin(angle) * strength);
        }
        return false;//Wind never "kills" the ball
    }

    public boolean isEnded() {
        return false;
    }

    @Override
    public dangerState getState() {
        return dangerState.ONGOING;
    }

    @Override
    public void onTouch(float x, float y) {
        return;
    }

    public float getAngle() {
        return angle;
    }


    public float getStrength() {
        return strength;
    }

    @Override
    public boolean isStopped() {
        return stopped;
    }

    @Override
    public void setStopped(boolean stopped) {
        this.stopped = stopped;
    }
}
