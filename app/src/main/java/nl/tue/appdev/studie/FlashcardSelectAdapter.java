package nl.tue.appdev.studie;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Adapter for selecting flashcards within the Flashcard Set Creation RecyclerView.
 */
public class FlashcardSelectAdapter extends RecyclerView.Adapter<FlashcardSelectAdapter.ViewHolder> {

    private static final String TAG = "FlashcardSelecterAdapter";

    private final ArrayList<Flashcard> flashcardList;
    private final ArrayList<String> selectedFlashcards;

    public FlashcardSelectAdapter(ArrayList<Flashcard> flashcardList) {
        this.flashcardList = flashcardList;
        this.selectedFlashcards = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.flashcard_select, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Flashcard flashcard = flashcardList.get(position);
        holder.questionTextView.setText(flashcard.getQuestion());

        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(selectedFlashcards.contains(flashcard.getId()));

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedFlashcards.add(flashcard.getId());
                Log.d(TAG, "Added: " + flashcard.getId());
            } else {
                selectedFlashcards.remove(flashcard.getId());
                Log.d(TAG, "Removed: " + flashcard.getId());
            }
            Log.d(TAG, "Selected flashcards: " + selectedFlashcards);
        });
    }

    @Override
    public int getItemCount() {
        return flashcardList.size();
    }

    public ArrayList<String> getSelectedFlashcards() {
        return selectedFlashcards;
    }

    /**
     * ViewHolder for flashcard items.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView questionTextView;
        CheckBox checkBox;

        ViewHolder(View itemView) {
            super(itemView);
            questionTextView = itemView.findViewById(R.id.fc_select_question);
            checkBox = itemView.findViewById(R.id.flashcard_checkbox);
        }
    }
}
