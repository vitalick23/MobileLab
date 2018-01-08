package vitaly.balance.gameMVC;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

import vitaly.balance.model.Ball;
import vitaly.balance.model.Board;
import vitaly.balance.model.ButtonModel;
import vitaly.balance.model.Danger;
import vitaly.balance.model.Hole;
import vitaly.balance.model.Laser;
import vitaly.balance.model.PowerUp;
import vitaly.balance.model.Projectile;
import vitaly.balance.model.Wind;

import static vitaly.balance.framework.MathHelper.*;
import static vitaly.balance.model.PowerUp.*;


public class gameModel {

    public static final int BALL_RADIUS = 1;
    public static final int GRAVITY_SPEED_FACTOR = 10;
    public static final float INITIAL_DANGER_CHANCE = 0.0f;
    public static final float DANGER_GROWTH_RATE = 0.001f;
    public static final int DANGER_LEVEL_THRESHOLDS = 10;
    public static final int MAX_DANGER_LEVEL = 4;//== Различные типы опасности
    public static final float POWERUP_SPAWN_CHANCE = 0.1f;// проверить каждый итог времени, исключается опасность

    public static final float PAUSE_BUTTON_X = 1;
    public static final float PAUSE_BUTTON_Y = 1;
    public static final float PAUSE_BUTTON_DIMENSIONS = 2;

    public static final float RESTART_BUTTON_SCALE = 0.5f;

    private final float width;
    private final float height;

    private final float restartButtonX;
    private final float restartButtonY;

    public static final int MAX_HOLES = 9;
    public static final int MAX_LASERS = 5;
    public static final int MAX_ARROWS = 9;


    private Ball ball;
    private Board board;
    private gameState state;
    private float gravityX, gravityY, totalTime, dangerChance, auxTime;
    private Random random;
    private int dangerLevel, holeAmount, laserAmount, arrowAmount;
    private Wind wind;
    private List<Danger> allDangers;
    private PowerUp powerUp;
    private boolean slowingTime, timeStopped;

    private ButtonModel pauseButton, restartButton;

    enum gameState {PLAYING, PAUSE, END_GAME}

    public gameModel(float width, float height) {
        this.width = width;
        this.height = height;

        restartButtonX = (width * (1 - RESTART_BUTTON_SCALE)) / 2;
        restartButtonY = (height * (1 - RESTART_BUTTON_SCALE)) / 2;

        board = new Board(this.width, this.height);
        random = new Random();


        initializeButtons();

        ballInOrigin();
    }

    private void initializeButtons() {
        pauseButton = new ButtonModel(this, true, PAUSE_BUTTON_X, PAUSE_BUTTON_Y,
                PAUSE_BUTTON_DIMENSIONS, PAUSE_BUTTON_DIMENSIONS) {
            @Override
            public void onTouch(float x, float y) {
                if (!isVisible()) return;

                if (x >= getX() && x <= getX() + getWidth() && y >= getY() && y <= getY() + getHeight()) {

                    if (getModel().getState() == gameState.PLAYING)
                        getModel().setState(gameState.PAUSE);
                    else if (getModel().getState() == gameState.PAUSE)
                        getModel().setState(gameState.PLAYING);
                }
            }
        };

        restartButton = new ButtonModel(this, false, restartButtonX, restartButtonY,
                width * RESTART_BUTTON_SCALE, height * RESTART_BUTTON_SCALE) {
            @Override
            public void onTouch(float x, float y) {
                if (!isVisible()) return;

                if (x >= getX() && x <= getX() + getWidth() && y >= getY() && y <= getY() + getHeight()) {
                    getModel().ballInOrigin();
                }
            }
        };
    }

    public void onTouch(float x, float y) {
        if (state == state.PLAYING) {
            ListIterator<Danger> iterator = allDangers.listIterator();

            while (iterator.hasNext()) {
                Danger danger = iterator.next();
                danger.onTouch(x, y);
            }
        }

        pauseButton.onTouch(x, y);
        restartButton.onTouch(x, y);
    }

    private void ballInOrigin() {
        ball = new Ball(width / 2, height / 2, BALL_RADIUS, board);
        state = state.PLAYING;
        dangerLevel = 0;
        dangerChance = INITIAL_DANGER_CHANCE;
        totalTime = 0;
        auxTime=0;

        slowingTime = false;
        timeStopped = false;

        powerUp=null;

        holeAmount = 0;
        laserAmount = 0;
        arrowAmount = 0;

        pauseButton.setVisible(true);
        restartButton.setVisible(false);

        wind = new Wind();

        allDangers = new LinkedList<>();
        allDangers.add(wind);
    }

    public void update(float deltaTime) {
        switch (state) {
            case PLAYING:
                updatePlayElements(deltaTime);
                break;
            case PAUSE:
                break;
            case END_GAME:
                break;
        }
    }

    private void updatePlayElements(float deltaTime) {

        updateMovement(deltaTime);

        //Проверьте, включено ли время медленнее
        if (slowingTime) {
            auxTime+=deltaTime;
            if(auxTime>=PowerUp.DURATION) {
                slowingTime = false;
                auxTime=0;
            }
            deltaTime *= PowerUp.SLOW_FACTOR;
        }else if(timeStopped){
            auxTime+=deltaTime;
            if(auxTime>PowerUp.DURATION){
                timeStopped = false;
                auxTime = 0;
                for (Danger danger :allDangers){
                    danger.setStopped(false);
                }
            }
        }

        if(!timeStopped) {
            dangerLevel = Math.min((int) (Math.floor(totalTime / DANGER_LEVEL_THRESHOLDS)), MAX_DANGER_LEVEL);
            generateDangers();
        }

        updateDangers(deltaTime);
        updatePowerUp();
    }

    private void updatePowerUp() {
        if (powerUp != null)
            if (powerUp.update(this))
                powerUp = null;

    }

    private void updateDangers(float deltaTime) {
        //Эта функция выполняет все операции по обновлению и удалению
        ListIterator<Danger> iterator = allDangers.listIterator();

        while (iterator.hasNext()) {
            Danger danger = iterator.next();

            /*if(danger instanceof Hole) Log.d("Hole: ", "State: " + ((Hole) danger).getState() +
                    " || Radius : "  + ((Hole) danger).getRadius());*/

            if (danger.update(deltaTime, ball)) {
                if(!ball.isShielded())endGame();
            }
            if (danger.isEnded()) {

                if (danger instanceof Hole)
                    holeAmount--;
                else if (danger instanceof Laser)
                    laserAmount--;
                else if (danger instanceof Projectile)
                    arrowAmount--;

                iterator.remove();
                allDangers.remove(danger);

                generatePower();
            }
        }
    }

    private void generatePower() {
        if (powerUp == null && !timeStopped && !slowingTime && !ball.isShielded()) {

            if (random.nextFloat() > POWERUP_SPAWN_CHANCE) return;

            switch (random.nextInt(3)) {

                case 0://SHIELD
                    powerUp = new PowerUp(randomRange(POWER_UP_RADIUS*2, width - POWER_UP_RADIUS*2),
                            randomRange(POWER_UP_RADIUS*2, height - POWER_UP_RADIUS*2), powerType.SHIELD) {
                        @Override
                        public boolean update(gameModel model) {
                            if (this.collides(model.ball)) {
                                ball.setShielded(true);
                                return true;
                            }
                            return false;
                        }
                    };
                    break;
                case 1://SLOW TIME
                    powerUp = new PowerUp(randomRange(POWER_UP_RADIUS*2, width - POWER_UP_RADIUS*2),
                            randomRange(POWER_UP_RADIUS*2, height - POWER_UP_RADIUS*2), powerType.SLOW_TIME) {
                        @Override
                        public boolean update(gameModel model) {
                            if(this.collides(model.ball)){
                                model.slowingTime = true;
                                return true;
                            }
                            return false;
                        }
                    };
                    break;
                case 2://STOP TIME
                    powerUp = new PowerUp(randomRange(POWER_UP_RADIUS*2, width - POWER_UP_RADIUS*2),
                            randomRange(POWER_UP_RADIUS*2, height - POWER_UP_RADIUS*2), powerType.STOP_TIME) {
                        @Override
                        public boolean update(gameModel model) {
                            if(this.collides(model.ball)){
                                for (Danger danger:model.allDangers){
                                    danger.setStopped(true);
                                }
                                timeStopped = true;
                                return true;
                            }
                            return false;
                        }
                    };
            }
        }
    }

    private void generateDangers() {
        if (random.nextFloat() < dangerChance) {
            //Log.d("generateDangers", toString().valueOf(dangerLevel));
            boolean dangerProduced = false;

            switch (random.nextInt(dangerLevel + 1)) {
                case 0://HOLES
                    if (holeAmount < MAX_HOLES) {
                        allDangers.add(new Hole(randomRange(Hole.FINAL_RADIUS, width - Hole.FINAL_RADIUS),
                                randomRange(Hole.FINAL_RADIUS, height - Hole.FINAL_RADIUS)));
                        holeAmount++;
                        dangerProduced = true;
                    }
                    break;

                case 1://LINEAR ARROWS
                    if (arrowAmount < MAX_ARROWS) {
                        Projectile pr = new Projectile(false, board);
                        allDangers.add(pr);
                        arrowAmount++;
                        dangerProduced = true;
                        //Log.d("GenerateArrow", "Angle :"+pr.getAngle()+" \\Center: "+ pr.getCenter());
                    }
                    break;

                case 2://STATIC LASERS
                    if (laserAmount < MAX_LASERS) {
                        allDangers.add(new Laser(board, false));
                        laserAmount++;
                        dangerProduced = true;
                    }
                    break;

                case 4://HOMING ARROWS
                    if (arrowAmount < MAX_ARROWS) {
                        Projectile pr = new Projectile(true, board);
                        allDangers.add(pr);
                        arrowAmount++;
                        dangerProduced = true;
                    }
                    break;

                case 3: //MOBILE LASERS
                    if (laserAmount < MAX_LASERS) {
                        allDangers.add(new Laser(board, true));
                        laserAmount++;
                        dangerProduced = true;
                    }
                    break;
            }
            if (!dangerProduced) dangerChance += DANGER_GROWTH_RATE;
            else dangerChance = INITIAL_DANGER_CHANCE;

        } else dangerChance += DANGER_GROWTH_RATE;
    }


    private void updateMovement(float deltaTime) {
        totalTime += deltaTime;
        float speedModule = (float) Math.sqrt(gravityX * gravityX + gravityY * gravityY);
        float unitTime = 1 / speedModule;

        for (float time = 0; time < deltaTime; time += unitTime) {

            float timeElapsed;
            if (time + unitTime < deltaTime) timeElapsed = unitTime;
            else timeElapsed = deltaTime - time;

            ball.applyForce(deltaTime, gravityX * GRAVITY_SPEED_FACTOR, gravityY * GRAVITY_SPEED_FACTOR);
            ball.move(timeElapsed);

            if (ball.fallsOfBoard()) {
                endGame();
            }
        }
        //Log.d("updateMovement", "X: " + ball.getCenterX() + "\nY: " + ball.getCenterY() + "\n");
    }

    private void endGame() {
        state = state.END_GAME;
        pauseButton.setVisible(false);
        restartButton.setVisible(true);


    }

    public float getGravityX() {
        return gravityX;
    }

    public void setGravityX(float gravityX) {
        this.gravityX = gravityX;
    }

    public float getGravityY() {
        return gravityY;
    }

    public void setGravityY(float gravityY) {
        this.gravityY = gravityY;
    }

    public Ball getBall() {
        return ball;
    }

    public List<Danger> getAllDangers() {
        return allDangers;
    }


    public float getTotalTime() {
        return totalTime;
    }

    public Wind getWind() {
        return wind;
    }

    public float getDangerChance() {
        return dangerChance;
    }

    public ButtonModel getPauseButton() {
        return pauseButton;
    }

    public ButtonModel getRestartButton() {
        return restartButton;
    }

    public gameState getState() {
        return state;
    }

    public void setState(gameState state) {
        this.state = state;
    }

    public PowerUp getPowerUp() {
        return powerUp;
    }

    public boolean isSlowingTime() {
        return slowingTime;
    }

    public boolean isTimeStopped() {
        return timeStopped;
    }
}

