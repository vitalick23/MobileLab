package vitaly.balance.model;

import vitaly.balance.gameMVC.gameModel;


public abstract class ButtonModel {

    private boolean visible;
    private gameModel model;
    private float x, y, width, height;

    public ButtonModel(gameModel model, boolean visible, float x, float y, float width, float height){
        this.model = model;
        this.visible = visible;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }


    public abstract void onTouch(float x, float y);

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public gameModel getModel() {
        return model;
    }
}
