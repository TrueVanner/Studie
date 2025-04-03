package nl.tue.appdev.studie;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;

import java.io.ByteArrayOutputStream;

public class QRCodeActivity extends AppCompatActivity implements View.OnClickListener{

    private ImageView qrCodeImageView;
    private FirebaseFirestore db;
    private TextView groupNameTextView;
    private String groupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code);

        // get group id from group screen intent
        Intent intent = getIntent();
        groupId = intent.getStringExtra("id");

        // Add back button
        ImageButton back = findViewById(R.id.qr_code_back_button);
        back.setOnClickListener(this);

        qrCodeImageView = findViewById(R.id.qr_code_image_view);
        db = FirebaseFirestore.getInstance();
        groupNameTextView = findViewById(R.id.group_name_text_view);

//        // Generate QR code with a static string
//        String staticString = "exampleStaticString";
//        generateQRCode(staticString);

        retrieveGroupUID2AndGenerateQRCode();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.qr_code_back_button) {
            Intent toManage = new Intent(QRCodeActivity.this, ManageGroupActivity.class);
            toManage.putExtra("id", groupId);
            startActivity(toManage);
        }
    }

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
                        Log.e("QRCodeActivity", "Name: " + groupName + ", ID: " + groupId);
                    });
                } else {
                    Log.e("QRCodeActivity", "Group Name is null");
                }
            } else {
                Log.e("QRCodeActivity", "ID:" + groupId);
                Log.e("QRCodeActivity", "Document does not exist");
            }
            return null;
        }).addOnFailureListener(e -> {
            Log.e("QRCodeActivity", "Transaction failed", e);
        });
    }

    private String bitmapToBase64(Bitmap bitmap, String qrCodeData) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String base64Bitmap = Base64.encodeToString(byteArray, Base64.DEFAULT);
        return qrCodeData + ":" + base64Bitmap;
    }

    private void generateQRCode(String text) {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        try {
            Bitmap bitmap = toBitmap(qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 200, 200));
            qrCodeImageView.setImageBitmap(bitmap);

            // Convert bitmap to Base64 string
            String qrCodeBase64 = bitmapToBase64(bitmap, text);

            // Store the Base64 string in Firestore
            storeQRCodeInFirestore(qrCodeBase64);

        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

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

    private void storeQRCodeInFirestore(String qrCodeBase64) {
        DocumentReference docRef = db.collection("groups").document(groupId);
        docRef.update("qrCode", qrCodeBase64)
                .addOnSuccessListener(aVoid -> Log.d("QRCodeActivity", "QR Code stored successfully"))
                .addOnFailureListener(e -> Log.e("QRCodeActivity", "Error storing QR Code", e));
    }
}