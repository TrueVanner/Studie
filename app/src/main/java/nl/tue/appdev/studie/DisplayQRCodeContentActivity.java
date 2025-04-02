package nl.tue.appdev.studie;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

public class DisplayQRCodeContentActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String groupId;
    private String groupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_qr_code_content);

        db = FirebaseFirestore.getInstance();

        TextView groupNameTextView = findViewById(R.id.group_name_text_view);
        ImageButton backButton = findViewById(R.id.join_group_back_button);
        Button noButton = findViewById(R.id.button_no);
        Button yesButton = findViewById(R.id.button_yes);

        // Get the scanned content from the intent
        String scannedContent = getIntent().getStringExtra("SCANNED_CONTENT");
        //Toast.makeText(this, "scannedContent: " + scannedContent, Toast.LENGTH_LONG).show();
        Log.d("DisplayQRCodeContentActivity", "Received SCANNED_CONTENT: " + scannedContent);
        if (scannedContent != null) {
            groupName = extractGroupName(scannedContent);
            groupNameTextView.setText(groupName);
            // Extract group ID from the scanned content
            groupId = extractGroupIdFromScannedContent(scannedContent);
            //Toast.makeText(this, "Retrieved Group ID: " + groupId, Toast.LENGTH_LONG).show();
        } else {
            groupNameTextView.setText("Group name not found");
        }

        backButton.setOnClickListener(v -> {
            finish();
        });

        noButton.setOnClickListener(v -> {
            Intent intent = new Intent(DisplayQRCodeContentActivity.this, JoinActivity.class);
            startActivity(intent);
        });

        yesButton.setOnClickListener(v -> joinGroup());
    }

    private String extractGroupName(String scannedContent) {
        if (scannedContent.startsWith("Name: ")) {
            String[] parts = scannedContent.split(", ID: ");
            return parts[0].substring(6).trim();
        }
        return scannedContent;
    }

    private String extractGroupIdFromScannedContent(String scannedContent) {
        if (scannedContent.contains(", ID: ")) {
            String[] parts = scannedContent.split(", ID: ");
            if (parts.length > 1) {
                return parts[1].trim();
            }
        }
        return null;
    }

    private void joinGroup() {
        if (groupId != null) {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DocumentReference userRef = db.collection("users").document(userId);

            // Add the user to the group
            userRef.update("groups." + groupId, groupName)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("DisplayQRCodeContentActivity", "User added to group successfully");
                        //Toast.makeText(DisplayQRCodeContentActivity.this, groupName + " joined!", Toast.LENGTH_SHORT).show();

                        // Redirect to the group's home view
                        Intent intent = new Intent(DisplayQRCodeContentActivity.this, GroupActivity.class);
                        intent.putExtra("id", groupId);
                        startActivity(intent);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("DisplayQRCodeContentActivity", "Error adding user to group", e);
                        //Toast.makeText(DisplayQRCodeContentActivity.this, "Something went wrong...", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Log.e("DisplayQRCodeContentActivity", "Group ID is null");
        }
    }
}