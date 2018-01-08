package vitaly.balance.model;

import android.graphics.PointF;

import java.util.Random;

import vitaly.balance.framework.MathHelper;


public class Laser implements Danger {

    private static final float LIFESPAN = 6;
    private static final float FINAL_WIDTH = 0.5f;
    private static final float MIN_WIDTH = 0.05f;
    private static final float GROWING_SPEED = FINAL_WIDTH / 4;
    private static final float WIDTH_FLICKER = FINAL_WIDTH / 5;
    private static final float WIDTH_FLICKER_SPEED = WIDTH_FLICKER * 5;
    private static final float LASER_POINT_SPEED = 2;


    private dangerState state;
    private float width, auxTime;
    private PointF point1, point2;
    private int side1, side2;
    private boolean ended, mobile, speed1, speed2, stopped;//Speed = true, mobile- подвижный/не
    private Board board;

    public Laser(Board board, boolean mobile) {
        Random random = new Random();
        this.board = board;
        this.mobile = mobile;

        if (mobile) {
            speed1 = random.nextBoolean();
            speed2 = random.nextBoolean();
        }

        side1 = random.nextInt(4);
        side2 = (side1 + random.nextInt(2) + 1) % 4;

        point1 = MathHelper.placePoint(side1, board);
        point2 = MathHelper.placePoint(side2, board);

        this.state = dangerState.STARTING;
        ended = false;
        width = 0;
    }


    @Override
    public boolean isEnded() {
        return ended;
    }

    @Override
    public dangerState getState() {
        return state;
    }

    @Override
    public void onTouch(float x, float y) {
        return;
    }

    @Override
    public boolean update(float deltaTime, Ball ball) {
        // проверяйте, сталкивается ли он с мячом

        switch (state) {
            case STARTING:
                if(!stopped) {
                    if (width < FINAL_WIDTH)
                        width = Math.min(FINAL_WIDTH, width + GROWING_SPEED * deltaTime);
                    else {
                        state = dangerState.ONGOING;
                        auxTime = 0;
                    }

                    if (mobile) moveLaser(deltaTime);
                }
                break;

            case ONGOING:
                if(!stopped) {
                    auxTime += deltaTime;
                    if (auxTime >= LIFESPAN) {
                        state = dangerState.ENDING;
                        break;
                    }

                    if (mobile) moveLaser(deltaTime);

                    width = FINAL_WIDTH + (float) Math.sin(auxTime * WIDTH_FLICKER_SPEED) * WIDTH_FLICKER;
                }
                return ballBurns(ball);

            case ENDING:
                if(!stopped) {
                    if (width > MIN_WIDTH)
                        width = Math.max(width - GROWING_SPEED * deltaTime, MIN_WIDTH);
                    else ended = true;

                    if (mobile) moveLaser(deltaTime);
                }
                break;
        }
        return false;
    }

    private void moveLaser(float deltaTime) {
        speed1 = movepoint(point1, side1, speed1, deltaTime);
        speed2 = movepoint(point2, side2, speed2, deltaTime);
    }

    // движение если надо
    private boolean movepoint(PointF P, int side, boolean pointSpeed, float deltaTime) {
        switch (side) {
            case 0://Right
            case 2://Left
                if (pointSpeed) {
                    P.y += LASER_POINT_SPEED * deltaTime;
                    if (P.y > board.getHeight()) return false;
                } else {
                    P.y -= LASER_POINT_SPEED * deltaTime;
                    if (P.y < 0) return true;
                }
                break;

            case 1://Top
            case 3://Right
                if (pointSpeed) {
                    P.x += LASER_POINT_SPEED * deltaTime;
                    if (P.x > board.getWidth()) return false;
                } else {
                    P.x -= LASER_POINT_SPEED * deltaTime;
                    if (P.x < 0) return true;
                }
                break;
        }

        return pointSpeed;
    }
    //ожоги
    private boolean ballBurns(Ball ball) {
        return ball.getRadius() + width / 2 >
                MathHelper.pointToLineDistance(point1, point2, new PointF(ball.getPosition().x,
                        ball.getPosition().y));
    }


    public float getWidth() {
        return width;
    }

    public PointF getPoint1() {
        return point1;
    }

    public PointF getPoint2() {
        return point2;
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
