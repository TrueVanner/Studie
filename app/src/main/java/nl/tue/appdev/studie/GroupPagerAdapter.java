package nl.tue.appdev.studie;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class GroupPagerAdapter extends FragmentPagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.notes_string, R.string.flashcards_string, R.string.sets_string};
    private final Context mContext;
    int totalTabs;

    public GroupPagerAdapter(Context context, FragmentManager fm, int totalTabs) {
        super(fm);
        mContext = context;
        this.totalTabs = totalTabs;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new NotesFragment();
            case 1:
                return new FlashcardsFragment();
            case 2:
                return new SetsFragment();
            default:
                return null;
        }
    }


    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }


    @Override
    public int getCount() {
        // Show total pages.
        return this.totalTabs;
    }
}