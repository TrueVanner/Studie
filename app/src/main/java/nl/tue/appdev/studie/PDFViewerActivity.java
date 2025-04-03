package nl.tue.appdev.studie;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.barteksc.pdfviewer.PDFView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class PDFViewerActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = "PDFViewerActivity";
    private String filename;
    private PDFView pdfView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.pdf_viewer);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.pdfView), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });

        filename = getIntent().getStringExtra("filename");
        pdfView = findViewById(R.id.pdfView);


        Log.d(TAG, filename);
        File pdfFile = new File(getCacheDir(), filename);
//        Log.d(TAG, String.valueOf(pdfFile.exists()));
//        Log.d(TAG, String.valueOf(pdfFile.length()));
        try {
            pdfView.fromStream(new FileInputStream(pdfFile))
                    .enableSwipe(true)
                    .swipeHorizontal(false)
                    .enableDoubletap(true)
                    .defaultPage(0)
                    .load();
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Pdf file not found!!", e);
            throw new RuntimeException(e);
        } catch(NullPointerException e) {
            Log.e(TAG, "Problem opening file!", e);
            pdfFile.delete();
            Toast.makeText(PDFViewerActivity.this, "Problem opening file!", Toast.LENGTH_SHORT).show();
            finish();
        } catch(IOException e) {
            Log.e(TAG, "Problem opening file!", e);
            pdfFile.delete();
            Toast.makeText(PDFViewerActivity.this, "Problem opening file!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void onClick(View v) {
    }
}
