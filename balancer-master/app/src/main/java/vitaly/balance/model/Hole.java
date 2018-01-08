package vitaly.balance.model;


import android.graphics.PointF;

import vitaly.balance.framework.MathHelper;


public class Hole implements Danger {
    public static final float FINAL_RADIUS = 3;
    private static final float GROWING_SPEED = FINAL_RADIUS/4;
    private static final float MIN_RADIUS = 0.1f;
    private static final float RADIUS_FLICKER = FINAL_RADIUS / 7;
    private static final float RADIUS_FLICKER_SPEED = FINAL_RADIUS/2;
    private static final float INITIAL_RADIUS = 0.3f;

    private float radius, auxTime;
    private boolean ended, touchable, stopped;
    private dangerState state;
    private PointF center;



    public Hole(float centerX, float centerY, float radius) {
        this.center = new PointF(centerX, centerY);
        this.radius = radius;
        this.state = dangerState.STARTING;
        touchable = false;
        ended = false;
        auxTime = 0;
    }

    public Hole(float centerX, float centerY) {
        this(centerX, centerY, INITIAL_RADIUS);
    }

    @Override
    public boolean isEnded() {
        return ended;
    }

    public void onTouch(float x, float y) {
        if(!touchable) return;

        float distanceX = x - this.center.x;
        float distanceY = y - this.center.y;

        float distance = (float) Math.sqrt(distanceX * distanceX + distanceY * distanceY);

        if(distance <this.radius) {
            touchable = false;
            state = dangerState.ENDING;
        }
    }

    @Override
    public boolean update(float deltaTime, Ball ball) {
        if(ball.isShielded()) return false;

        //Returns if ball falls inside the hole
        //Hole can do things (as check if ball falls inside it) when it's stopped
        switch (state) {
            case STARTING:
                if(!stopped) {
                    if (radius < FINAL_RADIUS)
                        radius = Math.min(FINAL_RADIUS, radius + GROWING_SPEED * deltaTime);
                    else {
                        state = dangerState.ONGOING;
                        touchable = true;
                        auxTime = 0;
                    }
                }
                break;
            case ONGOING:
                if(!stopped) {
                    auxTime += deltaTime;
                    radius = FINAL_RADIUS + (float) Math.sin(auxTime * RADIUS_FLICKER_SPEED) * RADIUS_FLICKER;
                }
                return ballFalls(ball);
            //break;
            case ENDING:
                if(!stopped) {
                    if (radius > MIN_RADIUS)
                        radius = Math.max(radius - GROWING_SPEED * deltaTime, MIN_RADIUS);
                    else ended = true;
                }
                break;
        }
        return false;
    }

    private boolean ballFalls(Ball ball) {

        PointF distance = new PointF(ball.getPosition().x - this.center.x,
                ball.getPosition().y - this.center.y);

        float distanceM = MathHelper.moduleVector(distance);

        return distanceM <this.radius-ball.getRadius()/2;//We make more difficult (and visual) to fall inside a hole
    }

    public PointF getCenter() {
        return center;
    }

    public float getRadius() {
        return radius;
    }


    public dangerState getState() {
        return state;
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

