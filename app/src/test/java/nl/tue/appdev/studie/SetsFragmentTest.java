package nl.tue.appdev.studie;


import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class SetsFragmentTest {

    private SetsFragment fragment;
    private View mockView;
    private LinearLayout mockLinearLayout;

    @Before
    public void setup() {
        // Create an instance of the fragment
        fragment = Mockito.spy(new SetsFragment());

        // Mock the views your fragment interacts with
        mockView = Mockito.mock(View.class);
        mockLinearLayout = Mockito.mock(LinearLayout.class);

        when(mockView.findViewById(R.id.set_view_container)).thenReturn(mockLinearLayout);
    }

    @Test
    public void testNoException() {
        fragment.onViewCreated(mockView, null);

        try {
            fragment.displayFlashcardsets();
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }

        // Optionally: Verify that interactions with the views happened as expected
        //verify(mockTextView, times(1)).setText("Expected Text");
    }

}