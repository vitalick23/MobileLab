package vitaly.balance.framework;

import android.graphics.PointF;

import java.util.Random;

import vitaly.balance.model.Board;


public class MathHelper {

    //определяем внешнее заполнение, из которого будут создаваться опасности
    public static final float OBJECT_OFFBOARD_PADDING = 1;
    public static final float PI = (float) Math.PI;

    public static float randomRange(float min, float max) {
        return (float) Math.random() * (max - min + 1) + min;
    }

    public static float pointToLineDistance(PointF A, PointF B, PointF P) {
        double normalLength = Math.sqrt((B.x - A.x) * (B.x - A.x) + (B.y - A.y) * (B.y - A.y));
        return (float) (Math.abs((P.x - A.x) * (B.y - A.y) - (P.y - A.y) * (B.x - A.x)) / normalLength);
    }

    public static float moduleVector(PointF P) {
        return (float) Math.sqrt(P.x * P.x + P.y * P.y);
    }

    public static PointF makeUnitary(PointF P) {
        float module = moduleVector(P);
        return new PointF(P.x / module, P.y / module);
    }

    public static PointF placePoint(int side, Board board) {
        /*:
        *   Right = 0
        *   Top = 1
        *   Left = 2
        *   Bottom = 3*/

        Random random = new Random();

        switch (side) {
            case 0://RIGHT
                return new PointF(board.getWidth() + OBJECT_OFFBOARD_PADDING, random.nextFloat() * board.getHeight());

            case 1://TOP
                return new PointF(random.nextFloat() * board.getWidth(), -OBJECT_OFFBOARD_PADDING);

            case 2://LEFT
                return new PointF(-OBJECT_OFFBOARD_PADDING, random.nextFloat() * board.getHeight());

            case 3://BOTTOM
                return new PointF(random.nextFloat() * board.getWidth(), board.getHeight() + OBJECT_OFFBOARD_PADDING);
        }
        return new PointF(0, 0);
    }

    public static float dotProduct(PointF p1, PointF p2) {
        return p1.x * p2.x + p1.y * p2.y;
    }

    public static float toDegrees(float radians) {
        return radians * 180 / PI;
    }
}
