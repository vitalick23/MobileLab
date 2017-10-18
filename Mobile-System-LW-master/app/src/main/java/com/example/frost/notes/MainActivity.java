package com.example.frost.notes;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    ArrayList<Note> _notes = new ArrayList<Note>();
    private int _currentYear;
    private int _currentMonth;
    private int _currentDay;
    EditText editText;
    CalendarView calendarView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText = (EditText) findViewById(R.id.editText);
        editText.setText("");
        calendarView = (CalendarView) findViewById(R.id.calendarView);
        _notes = JSONHelper.ImportFromJSON(getApplicationContext());
        if (_notes == null) {
            _notes=new ArrayList<>();
        }

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                editText.setText("");
                _currentYear = year;
                _currentMonth = month;
                _currentDay = dayOfMonth;
                if (_notes != null){
                    for (Note note : _notes) {
                        if (new Date(year, month, dayOfMonth).getDate() == note.get_noteDate().getDate()) {
                            editText.setText(note.get_noteText());
                            break;
                        }
                    }
                }
            }
        });
    }

    public void Save(View view){
        Note tmpNote = new Note(new Date(_currentYear,_currentMonth,_currentDay),editText.getText().toString());
        _notes.add(tmpNote);
        boolean _result = JSONHelper.ExportToJSON(this,_notes);
        if(_result){
            Toast.makeText(this,"Note is safe",Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(this, "Note doesn't safe", Toast.LENGTH_LONG).show();
        }
    }

    public void Delete(View view){
        Note tmpNote = new Note(new Date(_currentYear,_currentMonth,_currentDay),editText.getText().toString());
        _notes.remove(tmpNote);
        boolean _result = JSONHelper.ExportToJSON(this,_notes);
        if(_result){
            Toast.makeText(this,"Note is safe",Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(this, "Note doesn't safe", Toast.LENGTH_LONG).show();
        }
    }

}
