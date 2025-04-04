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

        // Initialize Firestore instance
        db = FirebaseFirestore.getInstance();

        // Find views by their IDs
        TextView groupNameTextView = findViewById(R.id.group_name_text_view);
        ImageButton backButton = findViewById(R.id.join_group_back_button);
        Button noButton = findViewById(R.id.button_no);
        Button yesButton = findViewById(R.id.button_yes);

        // Get the scanned content from the intent
        String scannedContent = getIntent().getStringExtra("SCANNED_CONTENT");
        Log.d("DisplayQRCodeContentActivity", "Received SCANNED_CONTENT: " + scannedContent);
        if (scannedContent != null) {
            // Extract group name and ID from the scanned content
            groupName = extractGroupName(scannedContent);
            groupNameTextView.setText(groupName);
            groupId = extractGroupIdFromScannedContent(scannedContent);
        } else {
            groupNameTextView.setText("Group name not found");
        }

        // Set onClick listener for the back button to finish the activity
        backButton.setOnClickListener(v -> {
            finish();
        });

        // Set onClick listener for the no button to start JoinActivity
        noButton.setOnClickListener(v -> {
            Intent intent = new Intent(DisplayQRCodeContentActivity.this, JoinActivity.class);
            startActivity(intent);
        });

        // Set onClick listener for the yes button to join the group
        yesButton.setOnClickListener(v -> joinGroup());
    }

    // Method to extract the group name from the scanned content
    private String extractGroupName(String scannedContent) {
        if (scannedContent.startsWith("Name: ")) {
            String[] parts = scannedContent.split(", ID: ");
            return parts[0].substring(6).trim();
        }
        return scannedContent;
    }

    // Method to extract the group ID from the scanned content
    private String extractGroupIdFromScannedContent(String scannedContent) {
        if (scannedContent.contains(", ID: ")) {
            String[] parts = scannedContent.split(", ID: ");
            if (parts.length > 1) {
                return parts[1].trim();
            }
        }
        return null;
    }

    // Method to join the group
    private void joinGroup() {
        if (groupId != null) {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DocumentReference userRef = db.collection("users").document(userId);

            // Add the user to the group
            userRef.update("groups." + groupId, groupName)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("DisplayQRCodeContentActivity", "User added to group successfully");

                        // Redirect to the group's home view
                        Intent intent = new Intent(DisplayQRCodeContentActivity.this, GroupActivity.class);
                        intent.putExtra("id", groupId);
                        startActivity(intent);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("DisplayQRCodeContentActivity", "Error adding user to group", e);
                    });
        } else {
            Log.e("DisplayQRCodeContentActivity", "Group ID is null");
        }
    }
}