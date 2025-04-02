package nl.tue.appdev.studie;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.text.InputFilter;

import androidx.appcompat.app.AppCompatActivity;

public class CreateFlashcardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_flashcard);

        ImageButton backButton = findViewById(R.id.create_fc_back_button);
        backButton.setOnClickListener(v -> finish());

        Spinner spinnerSets = findViewById(R.id.spinner_sets);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.spinner_choices, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSets.setAdapter(adapter);
    }
}