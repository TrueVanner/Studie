package nl.tue.appdev.studie;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class DisplayQRCodeContentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_qr_code_content);

        TextView contentTextView = findViewById(R.id.content_text_view);

        // Get the scanned content from the intent
        String scannedContent = getIntent().getStringExtra("SCANNED_CONTENT");
        contentTextView.setText(scannedContent);
    }
}