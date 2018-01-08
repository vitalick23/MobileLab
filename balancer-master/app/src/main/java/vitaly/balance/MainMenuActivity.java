package vitaly.balance;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import vitaly.balance.gameMVC.gameView;

public class MainMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

    }

    public void onButtonClicked(View view){
        Intent intent;
        switch(view.getId()){
            case R.id.button_play_game:
                intent = new Intent(this, gameView.class);
                startActivity(intent);
                break;
            case R.id.button_help:
                intent = new Intent(this, HelpActivity.class);
                startActivity(intent);
                break;
        }
    }


}
