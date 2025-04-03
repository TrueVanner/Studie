package nl.tue.appdev.studie;

import static android.view.View.VISIBLE;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UploadNotesActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "UploadNotesActivity";

    ImageButton backButton;
    EditText noteTitle;
    ImageButton selectFileButton;
    Button uploadButton;
    ProgressBar progressBar;
    TextView selectFileHint;
    private String groupId;

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private ActivityResultLauncher<Intent> selectFileLauncher;
    private Uri fileUri;
    private String filename;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.upload_notes);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_upload_notes), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        groupId = getIntent().getStringExtra("groupId");

        backButton = findViewById(R.id.upload_notes_back_button);
        noteTitle = findViewById(R.id.upload_notes_note_title);
        selectFileButton = findViewById(R.id.select_file_button);
        uploadButton = findViewById(R.id.upload_file_button);
        selectFileHint = findViewById(R.id.upload_notes_select_file_hint);
        progressBar = findViewById(R.id.upload_notes_progress_bar);

        selectFileButton.setOnClickListener(v -> selectFileToUpload());
        uploadButton.setOnClickListener(v -> {
            uploadButton.setEnabled(false);
            selectFileButton.setEnabled(false);
            sendFile(getNoteTitle());
        });
        backButton.setOnClickListener(v -> finish());

        registerFileSelectionLauncher();
    }

    /**
     * Launches built-in file selection activity.
     */
    private void selectFileToUpload() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");

        selectFileLauncher.launch(intent);
    }

    private String getFileNameFromUri(Uri uri) {
        String result = null;
        if (Objects.equals(uri.getScheme(), "content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index != -1) {
                        result = cursor.getString(index);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            if(result == null) return "NOFILENAME";

            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    /**
     * Registers the launcher for the built-in file selection activity
     */
    private void registerFileSelectionLauncher() {
        selectFileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() != Activity.RESULT_OK) {
                        Log.e(TAG, "failed to get file (getFileFromDevice result not OK)");
                        selectFileHint.setText(R.string.select_file_hint);
                        return;
                    }

                    Intent data = result.getData();
                    if(data == null || data.getData() == null) {
                        Log.e(TAG, "No data selected");
                        Toast.makeText(UploadNotesActivity.this, "No data selected!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Store file uri
                    fileUri = data.getData();
                    filename = getFileNameFromUri(fileUri);
                    selectFileButton.setImageResource(R.drawable.pdf_icon);
                    uploadButton.setVisibility(VISIBLE);
                    selectFileHint.setText(filename);
                    noteTitle.setHint(filename.replace(".pdf",""));
                }
        );
    }

    // Needed to make sure that for sending the file the most recent filename is used
    private String getNoteTitle() {
        String title = noteTitle.getText().toString();
        if(title.isEmpty()) {
            // name of the file extracted from the uri;
            // since we can only have PDFs this should be safe
            return filename.replace(".pdf", "");
        }
        return title;
    }

    /**
     * Hashes the title of the note to make sure it is unique and short
     * @param title the title of the note
     */
    private String hashTitle(String title, String userId, String groupId) {
        MessageDigest md;
        try {
            // append to the given title the groupID, userID and current timestamp.
            // ensures that the string is unique.
            StringBuilder sb = new StringBuilder(title);
            sb.append(userId);
            sb.append(groupId);
            sb.append(System.currentTimeMillis() / 1000L);

            md = MessageDigest.getInstance("SHA-256");
            md.update(sb.toString().getBytes());
            byte[] hashBytes = md.digest();
            sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString().substring(0,16);
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Failed to hash filename", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Sends a request to the FTP server to store the file.
     * @param title is hashed and used as file name
     */
    private void sendFile(String title) {
        if(fileUri == null) {
            // should never happen since the button is hidden
            handleUploadFailure(new IOException("No file selected"));
        }

        assert fileUri != null;
        try {
            Log.d(TAG, "Starting to process file");
            InputStream inputStream = getContentResolver().openInputStream(fileUri);

            assert inputStream != null;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, length);
            }

            inputStream.close();
            Log.d(TAG, "Wrote the file, closed input stream, starting upload sequence");

            // Start upload with the file data
            progressBar.setVisibility(VISIBLE);
            uploadButton.setEnabled(false);

            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // will be used as filename
            String hashedTitle = hashTitle(title, userId, groupId);

            executor.execute(() -> {
                try {
                    final boolean success = FileServer.upload(baos.toByteArray(), hashedTitle);
                    mainHandler.post(() -> {
                        if(success) {
                            recordNoteInDatabase(new Note(hashedTitle, title, userId, groupId));
                        } else {
                            handleUploadFailure(new IOException("Something went wrong when storing the file on FTP side"));
                        }
                    });
                } catch (RuntimeException e) {
                    mainHandler.post(() -> handleUploadFailure(e));
                }
            });
        } catch (IOException e) {
            Log.e(TAG, "Failed to read file", e);
            Toast.makeText(UploadNotesActivity.this, "Failed to read file", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Stores the note data in the database.
     * Note ID is used as data key
     * @param note note data to store
     */
    private void recordNoteInDatabase(Note note) {
        executor.execute(() -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("notes")
                    .document(note.getFilename())
                    .set(note)
                    .addOnSuccessListener(aVoid -> {
                        db.collection("groups").document(groupId)
                                .get(Source.SERVER)
                                .addOnSuccessListener(groupDoc -> {
                                    if (groupDoc.exists()) {
                                        List<String> notes = (List<String>) groupDoc.get("notes");
                                        if (notes == null) {
                                            notes = new ArrayList<>(); // error prevention
                                        }
                                        // add the new flashcard set ID to the flashcardsets array in the group
                                        notes.add(note.getFilename());

                                        db.collection("groups").document(groupId)
                                                .update("notes", notes)
                                                .addOnSuccessListener(documentReference -> mainHandler.post(this::handleUploadSuccess))
                                                .addOnFailureListener(e -> mainHandler.post(() -> handleUploadFailure(e)));
                                    }
                                })
                                .addOnFailureListener(e -> mainHandler.post(() -> handleUploadFailure(e)));
                    })
                    .addOnFailureListener(e -> mainHandler.post(() -> handleUploadFailure(e)));
        });
//                    .addOnFailureListener(e -> mainHandler.post((() -> handleUploadFailure(e))));
    }

    /**
     * Called if uploading the file and storing it in the database was successful
     */
    private void handleUploadSuccess() {
        Log.d(TAG, "Successfully stored filename in database");
        Toast.makeText(UploadNotesActivity.this, "Successfully uploaded file!", Toast.LENGTH_SHORT).show();
        finish();
    }

    /**
     * Called if uploading the file and storing it in the database failed
     * @param e the exception thrown
     */
    private void handleUploadFailure(Exception e) {
        Log.e(TAG, "Failed to store filename in database", e);
        progressBar.setVisibility(View.INVISIBLE);
        selectFileButton.setEnabled(true);
        uploadButton.setEnabled(true);
        Toast.makeText(UploadNotesActivity.this, "Failed to upload file, please try again!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
    }
}