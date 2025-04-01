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

        barcodeView = findViewById(R.id.zxing_barcode_scanner);
        barcodeView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                String scannedContent = result.getText();
                String groupName = extractGroupName(scannedContent);
                //Toast.makeText(QRCodeScannerActivity.this, "Scanned Group Name: " + groupName, Toast.LENGTH_SHORT).show();

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
    }

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
        barcodeView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        barcodeView.resume();
    }
}