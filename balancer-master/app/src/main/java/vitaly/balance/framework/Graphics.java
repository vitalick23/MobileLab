package vitaly.balance.framework;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;

import static android.graphics.Bitmap.Config.ARGB_8888;


public class Graphics {
    private static final int TEXT_DEFAULT_SIZE = 20;
    private static final int DEFAULT_COLOR = 0xff_00_ff_ff;

    Bitmap frameBuffer;
    Canvas canvas, canvasRotation;
    Paint paint;


    public Graphics(int width, int height) {
        this.frameBuffer = Bitmap.createBitmap(width, height, ARGB_8888);
        canvas = new Canvas(frameBuffer);
        canvasRotation = new Canvas(frameBuffer);
        paint = new Paint();
        paint.setColor(DEFAULT_COLOR);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(TEXT_DEFAULT_SIZE);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
    }

    public Bitmap getFrameBuffer() {
        return frameBuffer;
    }

    public void clear(int color) {
        canvas.drawRGB((color & 0xff0000) >> 16, (color & 0xff00) >> 8, color & 0xff);
    }

    public void drawRect(int x, int y, int width, int height, int color) {
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(x, y, x + width - 1, y + height - 1, paint);
    }

    public void drawLine(int p1X, int p1Y, int p2X, int p2Y, int width, int color){
        paint.setStrokeWidth(width);
        paint.setColor(color);
        paint.setStyle((Paint.Style.FILL));

        canvas.drawLine(p1X, p1Y, p2X, p2Y, paint);
    }

    public void drawCircle(int centerX, int centerY, int radius, int color) {
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(centerX, centerY, radius, paint);
    }

    public void drawBitmap(Bitmap bitmap, float x, float y) {
        canvas.drawBitmap(bitmap, x, y, null);
    }

    public void drawRotatedBitmap(Bitmap bitmap, float centerX, float centerY, float degrees, float scale) {

        Matrix matrix = new Matrix(), matrix2 = new Matrix();

        matrix.setTranslate(-bitmap.getWidth() / 2, -bitmap.getHeight() / 2);

        matrix2.setScale(scale, scale);
        matrix.setConcat(matrix2, matrix);

        matrix2.setRotate(degrees, 0, 0);
        matrix.setConcat(matrix2, matrix);

        matrix2.setTranslate(centerX, centerY);
        matrix.setConcat(matrix2, matrix);

        canvasRotation.drawBitmap(bitmap, matrix, null);
    }



    public void drawTextCentered(float x, float y, String text, int color, float size) {
        paint.setTextSize(size);
        paint.setColor(color);
        paint.setTextAlign(Paint.Align.CENTER);

        Rect rect = new Rect();

        paint.getTextBounds(text, 0, text.length(), rect);

        canvas.drawText(text, x, y + rect.height() / 2, paint);
    }


    public int getWidth() {
        return frameBuffer.getWidth();
    }

    public int getHeight() {
        return frameBuffer.getHeight();
    }

}
