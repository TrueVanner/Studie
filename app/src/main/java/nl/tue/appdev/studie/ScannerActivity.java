package nl.tue.appdev.studie;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class ScannerActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        // Find the scan button and set an onClick listener
        ImageButton scanButton = findViewById(R.id.scan_button);
        scanButton.setOnClickListener(v -> {
            // Check if the camera permission is granted
            if (ContextCompat.checkSelfPermission(ScannerActivity.this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                // Request the camera permission if not granted
                ActivityCompat.requestPermissions(ScannerActivity.this,
                        new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
            } else {
                // Start the QR code scanner activity if permission is granted
                startScannerActivity();
            }
        });

        // Find the back button and set an onClick listener to finish the activity
        ImageButton backButton = findViewById(R.id.scan_back_button);
        backButton.setOnClickListener(v -> finish());
    }

    // Method to start the QR code scanner activity
    private void startScannerActivity() {
        Intent intent = new Intent(this, QRCodeScannerActivity.class);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            // Check if the camera permission was granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Start the QR code scanner activity if permission is granted
                startScannerActivity();
            } else {
                // Show a toast message if the camera permission is denied
                Toast.makeText(this, "Camera permission is required to scan QR codes", Toast.LENGTH_SHORT).show();
            }
        }
    }
}