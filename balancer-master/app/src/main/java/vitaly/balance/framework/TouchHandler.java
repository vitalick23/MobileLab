package vitaly.balance.framework;

import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;


public class TouchHandler implements View.OnTouchListener {
    public enum TouchType {
        TOUCH_DOWN,
        TOUCH_UP,
        TOUCH_DRAGGED
    }

    public static class TouchEvent {
        public TouchType type;
        public int x, y;
        public int pointer;
    }


    public static final int MAX_TOUCHPOINTS = 10;

    private boolean[] isTouched = new boolean[MAX_TOUCHPOINTS];
    private int[] touchX = new int[MAX_TOUCHPOINTS];
    private int[] touchY = new int[MAX_TOUCHPOINTS];
    private int[] id = new int[MAX_TOUCHPOINTS];
    private Pool<TouchEvent> touchEventPool;
    private List<TouchEvent> touchEvents = new ArrayList<>();
    private List<TouchEvent> touchEventsBuffer = new ArrayList<>();

    public TouchHandler(View view) {
        Pool.PoolObjectFactory<TouchEvent> factory = new Pool.PoolObjectFactory<TouchEvent>() {
            @Override
            public TouchEvent createObject() {
                return new TouchEvent();
            }
        };

        touchEventPool = new Pool<>(factory, 100);
        view.setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        synchronized (this) {
            int action = event.getActionMasked();
            int pointerIndex = event.getActionIndex();
            int pointerCount = event.getPointerCount();

            int pointerId;
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN:
                    pointerId = event.getPointerId(pointerIndex);
                    registerEvent(event, pointerIndex, pointerId, TouchType.TOUCH_DOWN);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                    pointerId = event.getPointerId(pointerIndex);
                    registerEvent(event, pointerIndex, pointerId, TouchType.TOUCH_UP);
                    break;
                case MotionEvent.ACTION_MOVE:
                    for (int i = 0 ; i < pointerCount ; i++ ) {
                        pointerId = event.getPointerId(i);
                        registerEvent(event, i, pointerId, TouchType.TOUCH_DRAGGED);
                    }
                    break;
            }

            for (int i = pointerCount ; i < MAX_TOUCHPOINTS ; i++ ) {
                isTouched[i] = false;
                id[i] = -1;
            }
            return true;
        }
    }

    private void registerEvent(MotionEvent event, int i, int pointerId, TouchType type) {
        TouchEvent touchEvent = touchEventPool.newObject();
        touchEvent.type = type;
        touchEvent.pointer = pointerId;
        touchEvent.x = touchX[i] = (int)event.getX();
        touchEvent.y = touchY[i] = (int)event.getY();
        isTouched[i] = true;
        id[i] = pointerId;
        touchEventsBuffer.add(touchEvent);
    }

    public boolean isTouchDown(int pointer) {
        synchronized (this) {
            int index = getIndex(pointer);
            return index >= 0 && index < MAX_TOUCHPOINTS && isTouched[index];
        }
    }

    public int getTouchX(int pointer) {
        synchronized (this) {
            int index = getIndex(pointer);
            if (index < 0 || index >= MAX_TOUCHPOINTS)
                return 0;
            else
                return touchX[index];
        }
    }

    public int getTouchY(int pointer) {
        synchronized (this) {
            int index = getIndex(pointer);
            if (index < 0 || index >= MAX_TOUCHPOINTS)
                return 0;
            else
                return touchY[index];
        }
    }

    public List<TouchEvent> getTouchEvents() {
        synchronized (this) {
            for (TouchEvent touchEvent : touchEvents)
                touchEventPool.free(touchEvent);
            touchEvents.clear();
            touchEvents.addAll(touchEventsBuffer);
            touchEventsBuffer.clear();
            return touchEvents;
        }
    }

    private int getIndex(int pointerId) {
        for (int i = 0; i < MAX_TOUCHPOINTS; i++) {
            if (id[i] == pointerId)
                return i;
        }
        return -1;
    }
}
