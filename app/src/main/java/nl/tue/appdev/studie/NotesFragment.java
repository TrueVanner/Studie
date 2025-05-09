package nl.tue.appdev.studie;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class NotesFragment extends Fragment {

    private static final String TAG = "NotesFragment";

    private LinearLayout noteContainer;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Map<String, Object> groupDocument;
    private String groupId;
    private final ArrayList<Note> notes = new ArrayList<>();

    private boolean startedLoading = false;
    public void retrieveNotes() {
        startedLoading = true;
        // Clear the list
        notes.clear();

        // Get flashcards of the group
        db.collection("groups")
                .document(groupId)
                .get(Source.SERVER)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                            groupDocument = document.getData();
                            assert groupDocument != null;
                            List<String> noteFilenames = (List<String>) groupDocument.get("notes");
                            assert noteFilenames != null;
                            retrieveNoteData(noteFilenames);
                        } else {
                            Log.e(TAG, "No such document");
                        }
                    } else {
                        Log.e(TAG, "get failed with ", task.getException());
                    }
                });
    }
    public void retrieveNoteData(List<String> noteFilenames) {
        for (String filename : noteFilenames) {
            db.collection("notes")
                    .document(filename)
                    .get(Source.SERVER)
                    .addOnCompleteListener(requireActivity(), task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                Map<String, Object> noteDocument = document.getData();
                                assert noteDocument != null;
                                notes.add(new Note(filename, (String) noteDocument.get("title"), (String) noteDocument.get("author"), (String) noteDocument.get("groupID")));
                                displayNotes();
                            } else {
                                Log.e(TAG, "No such document");
                            }
                        }
                    });
        }
        // i know this isn't async but should be good enough
        startedLoading = false;
    }



    public void displayNotes() {
        noteContainer.removeAllViews();

//        notes.add(new Note("4df4e207d6882956", "title", "author", "groupUID1"));
        for (Note n : notes) {
//            Log.d(TAG, "got here");
            String title = n.getTitle();
            String authorId = n.getAuthorId();


            // Create a FrameLayout to act as a button container
            FrameLayout buttonContainer = new FrameLayout(getContext());
            buttonContainer.setId(View.generateViewId());
            buttonContainer.setLayoutParams(new ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.MATCH_PARENT, // Full width
                    170
            ));

            // Create a MaterialButton inside the FrameLayout
            MaterialButton customButton = new MaterialButton(getContext());
            customButton.setId(View.generateViewId());
            customButton.setLayoutParams(new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    150
            ));
            customButton.setText("");

            // Set background
            Drawable background = ContextCompat.getDrawable(getContext(), R.drawable.button_simple);
            assert background != null;
            customButton.setBackgroundTintList(null);
            customButton.setBackground(background);

            // Create a ConstraintLayout inside the FrameLayout
            ConstraintLayout buttonLayout = new ConstraintLayout(getContext());
            buttonLayout.setId(View.generateViewId());
            buttonLayout.setLayoutParams(new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            ));

            // Question text (75% width)
            TextView titleText = new TextView(getContext());
            titleText.setId(View.generateViewId());
            titleText.setText(title);
            titleText.setMaxLines(2);
            titleText.setTextColor(Color.WHITE);
            titleText.setTextSize(16);
            titleText.setTypeface(null, Typeface.BOLD);

            ConstraintLayout.LayoutParams titleTextParams = new ConstraintLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
            titleTextParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
            titleTextParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
            titleTextParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
            titleTextParams.width = 0;
            titleTextParams.matchConstraintPercentWidth = 0.75f;
            titleText.setLayoutParams(titleTextParams);
            titleText.setEllipsize(TextUtils.TruncateAt.END);

            // Author text (20% width)
            TextView authorText = new TextView(getContext());
            authorText.setId(View.generateViewId());
            authorText.setText(authorId);
            authorText.setMaxLines(1);
            authorText.setTextColor(Color.WHITE);
            authorText.setTextSize(16);

            ConstraintLayout.LayoutParams authorTextParams = new ConstraintLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
            authorTextParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
            authorTextParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
            authorTextParams.width = 0;
            authorTextParams.matchConstraintPercentWidth = 0.2f; // 20% of parent width
            authorText.setLayoutParams(authorTextParams);
            authorText.setEllipsize(TextUtils.TruncateAt.END);
            authorText.setGravity(Gravity.END);
            authorText.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);

            // Icon (Top-Right Corner)
            ImageView icon = new ImageView(getContext());
            icon.setId(View.generateViewId());
            icon.setImageResource(R.drawable.user);
            float density = getContext().getResources().getDisplayMetrics().density;
            int width = (int) (20.0 * density);
            int height = (int) (20.0 * density);
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(width, height);
            icon.setLayoutParams(params);

            // Add views to buttonLayout
            buttonLayout.addView(titleText);
            buttonLayout.addView(authorText);
            buttonLayout.addView(icon);

            // Add button and layout to FrameLayout
            buttonContainer.addView(customButton); // Button is in the background
            buttonContainer.addView(buttonLayout); // Text and Icon are on top

            // Add FrameLayout to parent layout
            noteContainer.addView(buttonContainer);

            // Set Constraints
            ConstraintSet set = new ConstraintSet();
            set.clone(buttonLayout);

            set.connect(titleText.getId(), ConstraintSet.START, buttonLayout.getId(), ConstraintSet.START, 16);
            set.connect(titleText.getId(), ConstraintSet.TOP, buttonLayout.getId(), ConstraintSet.TOP, 16);
            set.connect(titleText.getId(), ConstraintSet.BOTTOM, buttonLayout.getId(), ConstraintSet.BOTTOM, 26);
            set.constrainWidth(titleText.getId(), 0);
            set.setHorizontalWeight(titleText.getId(), 0.75f);

            set.connect(authorText.getId(), ConstraintSet.END, buttonLayout.getId(), ConstraintSet.END, 16);
            set.connect(authorText.getId(), ConstraintSet.BOTTOM, buttonLayout.getId(), ConstraintSet.BOTTOM, 26);
            set.constrainWidth(authorText.getId(), 0);
            set.setHorizontalWeight(authorText.getId(), 0.2f);

            set.connect(icon.getId(), ConstraintSet.END, buttonLayout.getId(), ConstraintSet.END, 16);
            set.connect(icon.getId(), ConstraintSet.TOP, buttonLayout.getId(), ConstraintSet.TOP, 16);

            set.applyTo(buttonLayout);

            // Set OnClickListener for the FrameLayout
            customButton.setOnClickListener(view -> loadPDF(n.getFilename()));
        }
    }

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private void loadPDF(String filename) {
        Context context = getContext();
        if (context == null) {
            Log.e(TAG, "Context was null, somehow");
            return;
        }

        File fileToWrite = new File(context.getCacheDir(), filename);
        if (!fileToWrite.exists()) {
            try {
                Toast.makeText(getContext(), "Downloading note!", Toast.LENGTH_SHORT).show();
                boolean success = fileToWrite.createNewFile();

                executor.execute(() -> {
                    FileServer.download(filename, fileToWrite);
                    Log.d(TAG, String.valueOf(fileToWrite.length()));
                    mainHandler.post(() -> viewPDF(filename));
                });
                if (!success) {
                    Log.e(TAG, "Failed to create new file");
                }
            } catch (IOException e) {
                Log.e(TAG, "Failed to create new file", e);
            }
        } else {
            viewPDF(filename);
        }
    }

    private void viewPDF(String filename) {
        Intent toPDFView = new Intent(getActivity(), PDFViewerActivity.class);
        toPDFView.putExtra("filename", filename);
        startActivity(toPDFView);
    }

//    private ActivityResultLauncher<Intent> selectDirectoryLauncher;
//
//    private void registerDirectoryLaucher() {
//        assert getActivity() != null;
//        selectDirectoryLauncher = registerForActivityResult(
//                new ActivityResultContracts.StartActivityForResult(),
//                result -> {
//                    if (result.getResultCode() == RESULT_OK) {
//                        Intent data = result.getData();
//                        if(data == null || data.getData() == null) {
//                            Log.e(TAG, "data is null");
//                            return;
//                        }
//
//                        DocumentFile selectedDirectory = DocumentFile.fromTreeUri(getActivity(), data.getData());
//
//                        if(selectedDirectory == null) {
//                            Log.e(TAG, "Selected directory is null");
//                            return;
//                        }
//
//                        // Create a new directory within the selected directory
//                        DocumentFile newDirectory = selectedDirectory.createDirectory(groupId);
//                    }
//                }
//        );
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (getArguments() != null) {
            groupId = getArguments().getString("id");
            Log.d(TAG, "Received Data: " + groupId);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        noteContainer = view.findViewById(R.id.note_view_container);
        retrieveNotes();

        Button createButton = view.findViewById(R.id.note_upload);
        createButton.setOnClickListener(v -> {
            Intent toUploadNotes = new Intent(getActivity(), UploadNotesActivity.class);
            toUploadNotes.putExtra("groupId", groupId);
            startActivity(toUploadNotes);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!startedLoading) { retrieveNotes(); }
    }
}