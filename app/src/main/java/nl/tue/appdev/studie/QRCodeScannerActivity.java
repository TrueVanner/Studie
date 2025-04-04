package nl.tue.appdev.studie;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.util.List;

public class QRCodeScannerActivity extends AppCompatActivity {

    private DecoratedBarcodeView barcodeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code_scanner);

        // Initialize the barcode view
        barcodeView = findViewById(R.id.zxing_barcode_scanner);
        // Set up continuous decoding for the barcode view
        barcodeView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                // Get the scanned content
                String scannedContent = result.getText();
                // Extract the group name from the scanned content
                String groupName = extractGroupName(scannedContent);

                // Pause the barcode view to stop scanning
                barcodeView.pause();

                // Start DisplayQRCodeContentActivity with the scanned content
                Intent intent = new Intent(QRCodeScannerActivity.this, DisplayQRCodeContentActivity.class);
                intent.putExtra("SCANNED_CONTENT", scannedContent);
                startActivity(intent);
                // Finish the current activity
                finish();
            }

            @Override
            public void possibleResultPoints(List<com.google.zxing.ResultPoint> resultPoints) {
                // Handle possible result points if needed
            }
        });
    }

    // Method to extract the group name from the scanned content
    private String extractGroupName(String scannedContent) {
        if (scannedContent.startsWith("Name: ")) {
            String[] parts = scannedContent.split(", ID: ");
            return parts[0].substring(6).trim();
        }
        return scannedContent;
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pause the barcode view when the activity is paused
        barcodeView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume the barcode view when the activity is resumed
        barcodeView.resume();
    }
}