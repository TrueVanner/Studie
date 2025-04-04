package nl.tue.appdev.studie;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;

public class QRCodeActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView qrCodeImageView;
    private FirebaseFirestore db;
    private TextView groupNameTextView;
    private String groupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code);

        // Get group id from group screen intent
        Intent intent = getIntent();
        groupId = intent.getStringExtra("id");

        // Add back button and set its click listener
        ImageButton back = findViewById(R.id.qr_code_back_button);
        back.setOnClickListener(this);

        // Initialize views and Firestore instance
        qrCodeImageView = findViewById(R.id.qr_code_image_view);
        db = FirebaseFirestore.getInstance();
        groupNameTextView = findViewById(R.id.group_name_text_view);

        // Retrieve group ID and generate QR code
        retrieveGroupUID2AndGenerateQRCode();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.qr_code_back_button) {
            // Navigate to ManageGroupActivity
            Intent toManage = new Intent(QRCodeActivity.this, ManageGroupActivity.class);
            toManage.putExtra("id", groupId);
            startActivity(toManage);
        }
    }

    // Method to retrieve group ID and generate QR code
    private void retrieveGroupUID2AndGenerateQRCode() {
        DocumentReference docRef = db.collection("groups").document(groupId);
        db.runTransaction((Transaction.Function<Void>) transaction -> {
            DocumentSnapshot snapshot = transaction.get(docRef);
            if (snapshot.exists()) {
                String groupName = snapshot.getString("name");
                if (groupName != null) {
                    String qrCodeData = "Name: " + groupName + ", ID: " + groupId;
                    runOnUiThread(() -> {
                        generateQRCode(qrCodeData);
                        groupNameTextView.setText(groupName);
                    });
                } else {
                    Log.e("QRCodeActivity", "Group Name is null");
                }
            } else {
                Log.e("QRCodeActivity", "Document does not exist");
            }
            return null;
        }).addOnFailureListener(e -> {
            Log.e("QRCodeActivity", "Transaction failed", e);
        });
    }

    // Method to generate QR code
    private void generateQRCode(String text) {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        try {
            Bitmap bitmap = toBitmap(qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 200, 200));
            qrCodeImageView.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    // Method to convert BitMatrix to Bitmap
    private Bitmap toBitmap(com.google.zxing.common.BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bitmap.setPixel(x, y, matrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }
        return bitmap;
    }
}