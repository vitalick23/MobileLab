package vitaly.balance.model;

import android.graphics.PointF;


public class Ball {
    private static final float DRAG_FORCE_FACTOR = 0.95f;

    private float radius, auxTime;
    private Board board;
    private PointF position, speed;
    private boolean shielded;

    public Ball(float x, float y, float radius, Board board) {
        position = new PointF(x, y);
        speed = new PointF(0,0);
        this.radius = radius;
        this.board = board;
        shielded = false;
    }

    public void move(float deltaTime) {

        if(shielded){
            auxTime+=deltaTime;
            if(auxTime>=PowerUp.DURATION)
                shielded = false;
        }

        speed.x *= DRAG_FORCE_FACTOR;
        speed.y *= DRAG_FORCE_FACTOR;
        position.x += speed.x * deltaTime;
        position.y += speed.y * deltaTime;
    }

    public float getRadius() {
        return radius;
    }

    public boolean fallsOfBoard() {
        //Границы «ослаблены» для удобства игры
        return (position.x + radius / 2 < 0 || position.x - radius / 2 > board.getWidth() ||
                position.y + radius / 2 < 0 || position.y - radius / 2 > board.getHeight());
    }

    public void applyForce(float deltaTime, float fx, float fy) {
        speed.x += fx * deltaTime;
        speed.y += fy * deltaTime;
    }

    public PointF getPosition() {
        return position;
    }

    public boolean isShielded() {
        return shielded;
    }
    // защита
    public void setShielded(boolean shielded) {
        this.shielded = shielded;
        if(shielded == true) auxTime=0;
    }
}
