package com.nightcap.previously;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.Date;

/**
 * Activity for editing events before storage to database.
 */

public class EditActivity extends AppCompatActivity {
    private String TAG = "EventActivity";

    private boolean isEditExistingEvent;
    int editId;

    DbHandler dbHandler;
//    FloatingActionButton fab;

    // Input fields
    EditText inputName;
    EditText inputDate;
    EditText inputPeriod;
    EditText inputNotes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up views
        setContentView(R.layout.activity_edit);
        inputName = (EditText) findViewById(R.id.event_name);
        inputDate = (EditText) findViewById(R.id.event_date);
        inputPeriod = (EditText) findViewById(R.id.event_period);
        inputNotes = (EditText) findViewById(R.id.event_notes);

        // Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.event_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(null);

        // Set FocusListener on date field EditText
        inputDate.setOnFocusChangeListener(focusListener);

        // Get Realm handler
        dbHandler = new DbHandler(this);

        // Check if editing
        editId = getIntent().getIntExtra("edit_id", -1);
        if (editId > 0) {
            // Editing existing event
            isEditExistingEvent = true;

            // Pre-fill existing values
            inputName.setText(dbHandler.getEventById(editId).getName());
            inputDate.setText(new DateHandler().dateToString(dbHandler.getEventById(editId).getDate()));
            int period = dbHandler.getEventById(editId).getPeriod();
            String periodStr;
            if (period <=0) {
                periodStr = "N/A";
            } else {
                periodStr = String.valueOf(period);
            }
            inputPeriod.setText(periodStr);
            inputNotes.setText(dbHandler.getEventById(editId).getNotes());
        } else {
            // Creating new event
            isEditExistingEvent = false;

            // Default date to today
            inputDate.setText(new DateHandler().dateToString(new Date()));
        }

        // Floating action button
//        this.fab = (FloatingActionButton) findViewById(R.id.event_fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                // Start new Activity to add an event
//            }
//        });

        // Change editText underline colour
//        EditText nameEditText = (EditText) findViewById(R.id.event_name);
//        nameEditText.getBackground().mutate()
//                .setColorFilter(getResources().getColor(R.color.colorEditTextUnderline),
//                        PorterDuff.Mode.SRC_ATOP);

    }

    @Override
    protected void onStart() {
        super.onStart();

        RelativeLayout header = (RelativeLayout) findViewById(R.id.detail_head_space);
        Log.i(TAG, "Header height: " + header.getHeight());
//        fab.setY(header.getHeight());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                Intent homeIntent = new Intent(this, MainActivity.class);
                homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeIntent);
                break;
            case R.id.action_save_event:
                saveEvent();
                break;
        }
        return (super.onOptionsItemSelected(menuItem));
    }

    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerDialog();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    private void saveEvent() {
        // Get data from fields
        // Name and date
        String eventName = inputName.getText().toString();
        String eventDate = inputDate.getText().toString();

        // Period
        int eventPeriod;
        try {
            eventPeriod = Integer.parseInt(inputPeriod.getText().toString());
        } catch (NumberFormatException e) {
            eventPeriod = 0;   // Use a zero value to indicate no repeats
        }

        // Notes
        String eventNotes = inputNotes.getText().toString();

        // Check inputs here
        if (eventName.length() == 0 || eventDate.length() == 0) {
            Log.d(TAG, "A required field is empty");
            Toast.makeText(getApplicationContext(), "Event name and date must be filled in", Toast.LENGTH_SHORT).show();
        } else {
            Log.d(TAG, "Attempting to save event");
            DateHandler dh = new DateHandler();

            // Save event
            Event event = new Event();

            // ID depends on new or edit
            int id;
            if (isEditExistingEvent) {
                id = dbHandler.getEventById(editId).getId();
            } else {
                id = dbHandler.getEventCount() + 1;
            }
            event.setId(id);

            event.setName(eventName);
            event.setDate(dh.stringToDate(eventDate));
            event.setPeriod(eventPeriod);
            event.setNextDue(dh.nextDueDate(dh.stringToDate(eventDate), eventPeriod));
            event.setNotes(eventNotes);
            dbHandler.saveEvent(event);

            // Return to main screen
            Intent homeIntent = new Intent(this, MainActivity.class);
            homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(homeIntent);
        }
    }

    private View.OnFocusChangeListener focusListener = new View.OnFocusChangeListener() {
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus){
                showDatePickerDialog(v);
            }
        }
    };


}