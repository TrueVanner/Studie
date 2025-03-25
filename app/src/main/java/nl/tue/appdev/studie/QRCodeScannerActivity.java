package nl.tue.appdev.studie;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.util.List;

public class QRCodeScannerActivity extends AppCompatActivity {

    private DecoratedBarcodeView barcodeView;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

    private boolean scannerInitialized = false;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults, int deviceId) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                loadScanner();
            } else {
                loadNoPermissionScreen();
            }
        }
    }

    public void loadNoPermissionScreen() {
        findViewById(R.id.camera_perms_warning).setVisibility(View.VISIBLE);

        Button grantButton = findViewById(R.id.camera_perms_button);
        grantButton.setVisibility(View.VISIBLE);
        Intent settingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        settingsIntent.setData(Uri.parse("package:" + getPackageName()));
        grantButton.setOnClickListener(view -> startActivity(settingsIntent));

        Button retryButton = findViewById(R.id.camera_perms_button2);
        retryButton.setVisibility(View.VISIBLE);
        retryButton.setOnClickListener(view -> startActivity(new Intent(QRCodeScannerActivity.this, QRCodeScannerActivity.class)));
    }

    public void loadScanner() {
        if(!scannerInitialized) {
            barcodeView = findViewById(R.id.zxing_barcode_scanner);
            barcodeView.decodeContinuous(new BarcodeCallback() {
                @Override
                public void barcodeResult(BarcodeResult result) {
                    String scannedContent = result.getText();
                    Toast.makeText(QRCodeScannerActivity.this, "Scanned: " + scannedContent, Toast.LENGTH_SHORT).show();
                    barcodeView.pause();

                    // Start DisplayQRCodeContentActivity with the scanned content
                    Intent intent = new Intent(QRCodeScannerActivity.this, DisplayQRCodeContentActivity.class);
                    intent.putExtra("SCANNED_CONTENT", scannedContent);
                    startActivity(intent);
                }

                @Override
                public void possibleResultPoints(List<com.google.zxing.ResultPoint> resultPoints) {
                }
            });
            scannerInitialized = true;
        }
    }

    private void checkAndRequestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Request the permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            loadScanner();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        ActivityCompat.requestPermissions(QRCodeScannerActivity.this,
//                new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);


//        Log.println(Log.DEBUG, "Permission", "shouldShowRequestPermissionRationale = false");

        setContentView(R.layout.activity_qr_code_scanner);
        checkAndRequestPermission();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(scannerInitialized) {
            barcodeView.pause();
        }
//        else {
//            startActivity(new Intent(QRCodeScannerActivity.this, QRCodeScannerActivity.class));
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(scannerInitialized) {
            barcodeView.resume();
        }
    }
}