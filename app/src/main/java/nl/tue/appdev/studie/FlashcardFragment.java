package nl.tue.appdev.studie;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class FlashcardFragment extends Fragment {

    private static final String ARG_QUESTION_TEXT = "question_text";
    private static final String ARG_ANSWER_TEXT = "answer_text";

    private TextView flashcardContent;
    private String questionText;
    private String answerText;

    public FlashcardFragment() {
        // Required empty public constructor
    }

    public static FlashcardFragment newInstance(String question, String answer) {
        FlashcardFragment fragment = new FlashcardFragment();
        Bundle args = new Bundle();
        args.putString(ARG_QUESTION_TEXT, question);
        args.putString(ARG_ANSWER_TEXT, answer);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            questionText = getArguments().getString(ARG_QUESTION_TEXT);
            answerText = getArguments().getString(ARG_ANSWER_TEXT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_flashcard, container, false);
        flashcardContent = rootView.findViewById(R.id.flashcard_content);
        flashcardContent.setText(questionText); // Initially show the question
        return rootView;
    }

    public void updateFlashcardContent(boolean isShowingQuestion) {
        if (flashcardContent != null) {
            if (isShowingQuestion) {
                flashcardContent.setText(questionText);
            } else {
                flashcardContent.setText(answerText);
            }
        }
    }
}
