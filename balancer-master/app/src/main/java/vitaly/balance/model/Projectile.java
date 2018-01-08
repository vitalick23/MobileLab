package vitaly.balance.model;

import android.graphics.PointF;

import java.util.Random;

import vitaly.balance.framework.MathHelper;

import static vitaly.balance.framework.MathHelper.*;

public class Projectile implements Danger {

    private static final float BASE_SPEED = 15;
    private static final float PUSH_STRENGTH = 2500;
    private static final float EASING_ROTATION_FACTOR = 0.007f;

    public static final float WIDTH = 2;
    public static final float HEIGHT = 3;

    //мы сохраняем ширину и высоту, чтобы иметь возможность изменять их извне из их исходных значений
    private float width, height, angle, speed;//angle in radians
    private boolean ended, chasesBall, stopped;
    private PointF center;
    private Board board;

    public Projectile(boolean chasesBall, Board board) {
        speed = BASE_SPEED;
        this.board = board;

        int startSide = new Random().nextInt(4);
        center = placePoint(startSide, board);
        selectAngle(startSide);

        //Угловые корректоры для негативов и более 2 * PI
        if (angle < 0) angle += 2 * PI;
        if (angle > 2 * PI) angle %= 2 * PI;

        this.width = WIDTH;
        this.height = HEIGHT;

        this.chasesBall = chasesBall;
        ended = false;
        stopped = false;
    }

    private void selectAngle(int startSide) {

        //Angle setting
        switch (startSide) {
            case 0://RIGHT
                angle = randomRange(PI / 2, 3 * PI / 2);
                break;

            case 1://TOP
                angle = randomRange(PI, 2 * PI);
                break;

            case 2://LEFT
                angle = randomRange(-PI / 2, PI / 2);
                break;

            case 3://BOTTOM
                angle = randomRange(0, PI);
                break;
        }
    }

    @Override
    public boolean isEnded() {
        return ended;
    }

    @Override
    public dangerState getState() {
        return dangerState.ONGOING;
    }

    @Override
    public void onTouch(float x, float y) {

        PointF finger = new PointF(x, y);

        if (pointInThisRectangle(finger)) {
            //вычисляем вектор от этого к пальцу и устанавливаем угол в противоположном направлении

            PointF toFinger = new PointF(finger.x - center.x, finger.y - center.y);
            float fingerAngle = (float) Math.atan(toFinger.y / toFinger.x);

            this.angle = (fingerAngle + PI) % (2 * PI);
        }

    }

    @Override
    public boolean update(float deltaTime, Ball ball) {

        PointF distanceU = makeUnitary(
                new PointF(ball.getPosition().x - center.x, ball.getPosition().y - center.y));
        //Унитарный вектор расстояния ОТ ЭТОГО К ШАРУ

        if (!stopped) {
            if (chasesBall) {
                //Вычислим угол вектора расстояния и слегка сдвинем его в направлении шара
                float ballAngle = (float) Math.atan(distanceU.y / distanceU.x);


                //Рассчитаем ход угла снаряда по направлению к шару
                if (Math.abs(angle - ballAngle) < PI)
                    if (distanceU.x > 0)
                        angle -= (angle - ballAngle) * EASING_ROTATION_FACTOR;
                    else angle += (angle - ballAngle) * EASING_ROTATION_FACTOR;
                else if (distanceU.x > 0)
                    angle += (ballAngle + 2 * PI - angle) * EASING_ROTATION_FACTOR;
                else angle -= (ballAngle + 2 * PI - angle) * EASING_ROTATION_FACTOR;
                //else angle+=(angle-ballAngle)*EASING_ROTATION_FACTOR;

                //установили поправки для угла
                if (angle < 0) angle += 2 * PI;
                if (angle > 2 * PI) angle %= 2 * PI;

            }

            center.x += speed * deltaTime * Math.cos(angle);
            center.y += speed * deltaTime * Math.sin(angle);
        }

        if (!ball.isShielded())
            if (rectangleToCircleCollission(ball)) //ball collides
                ball.applyForce(deltaTime, distanceU.x * PUSH_STRENGTH, distanceU.y * PUSH_STRENGTH);


        //Упрощенная проверка, чем pointInRectangle, чтобы проверить, не находится ли снаряд снаружи
        if (center.x < -OBJECT_OFFBOARD_PADDING || center.x > board.getWidth() + OBJECT_OFFBOARD_PADDING
                || center.y < -OBJECT_OFFBOARD_PADDING || center.y > board.getHeight() + OBJECT_OFFBOARD_PADDING)
            ended = true;

        return false;//Always returns false as it won't "kill" the ball directly
    }


    private boolean rectangleToCircleCollission(Ball ball) {



        PointF A = new PointF(center.x - (width / 2) * (float) Math.cos(angle),
                center.y + (height / 2) * (float) Math.sin(angle));//-x, +y

        PointF B = new PointF(center.x + (width / 2) * (float) Math.cos(angle),
                center.y + (height / 2) * (float) Math.sin(angle));//+x, +y

        PointF C = new PointF(center.x + (width / 2) * (float) Math.cos(angle),
                center.y - (height / 2) * (float) Math.sin(angle));//+x, -y

        PointF D = new PointF(center.x - (width / 2) * (float) Math.cos(angle),
                center.y - (height / 2) * (float) Math.sin(angle));//-x, -y


        return (pointInRectangle(ball.getPosition(), A, B, C, D) ||
                intersectsEdge(ball, A, B) ||
                intersectsEdge(ball, B, C) ||
                intersectsEdge(ball, D, C) ||
                intersectsEdge(ball, A, D)
        );
    }

    private boolean intersectsEdge(Ball ball, PointF startEdge, PointF endEdge) {



        PointF edgeVector = new PointF(endEdge.x - startEdge.x, endEdge.y - startEdge.y);
        float moduleEdgeVector = MathHelper.moduleVector(edgeVector);

        PointF distanceToBall = new PointF(ball.getPosition().x - startEdge.x, ball.getPosition().y - startEdge.y);

        //Проверим проекцию на линию края и если она выходит за пределы края

        //Pv(u) = (u·v)/|v|
        float projectionBallToEdge = (MathHelper.dotProduct(distanceToBall, edgeVector)) / moduleEdgeVector;

        if (projectionBallToEdge < 0 || projectionBallToEdge > moduleEdgeVector) //мяч находится вне координат края
            return false;


        float minDistanceBallToEdge = pointToLineDistance(startEdge, endEdge, ball.getPosition());

        return ball.getRadius() > minDistanceBallToEdge;
    }


    private boolean pointInThisRectangle(PointF P) {
        PointF A = new PointF(center.x - (width / 2) * (float) Math.cos(angle),
                center.y + (height / 2) * (float) Math.sin(angle));//-x, +y

        PointF B = new PointF(center.x + (width / 2) * (float) Math.cos(angle),
                center.y + (height / 2) * (float) Math.sin(angle));//+x, +y

        PointF C = new PointF(center.x + (width / 2) * (float) Math.cos(angle),
                center.y - (height / 2) * (float) Math.sin(angle));//+x, -y

        PointF D = new PointF(center.x - (width / 2) * (float) Math.cos(angle),
                center.y - (height / 2) * (float) Math.sin(angle));//-x, -y

        return pointInRectangle(P, A, B, C, D);
    }


    private boolean pointInRectangle(PointF P, PointF A, PointF B, PointF C, PointF D) {

        //Если расстояние до ни одного из ребер для точки P больше оно внутри.

        return (pointToLineDistance(A, B, P) <= height &&
                pointToLineDistance(C, D, P) <= height &&
                pointToLineDistance(A, D, P) <= width &&
                pointToLineDistance(B, C, P) <= width);
    }



    public PointF getCenter() {
        return center;
    }

    public float getAngle() {
        return angle;
    }

    public boolean isChasesBall() {
        return chasesBall;
    }

    public boolean isStopped() {
        return stopped;
    }

    @Override
    public void setStopped(boolean stopped) {
        this.stopped = stopped;
    }
}
