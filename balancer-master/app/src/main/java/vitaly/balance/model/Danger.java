package vitaly.balance.model;


public interface Danger {

    enum dangerState {STARTING, ONGOING, ENDING}

    boolean isEnded();

    dangerState getState();

    void onTouch(float x, float y);

    boolean update(float deltaTime, Ball ball);
    //

    void setStopped(boolean stopped);
    boolean isStopped();
}
