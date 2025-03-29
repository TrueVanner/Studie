package nl.tue.appdev.studie;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class DisplayQRCodeContentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_qr_code_content);

        TextView groupNameTextView = findViewById(R.id.group_name_text_view);
        ImageButton backButton = findViewById(R.id.join_group_back_button);
        Button noButton = findViewById(R.id.button_no);

        // Get the scanned content from the intent
        String scannedGroupName = getIntent().getStringExtra("SCANNED_CONTENT");
        Log.d("DisplayQRCodeContentActivity", "Received SCANNED_CONTENT: " + scannedGroupName);
        if (scannedGroupName != null) {
            groupNameTextView.setText(scannedGroupName);
        } else {
            groupNameTextView.setText("Group name not found");
        }

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(DisplayQRCodeContentActivity.this, ScannerActivity.class);
            startActivity(intent);
        });

        noButton.setOnClickListener(v -> {
            Intent intent = new Intent(DisplayQRCodeContentActivity.this, JoinActivity.class);
            startActivity(intent);
        });
    }
}