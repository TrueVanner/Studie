package nl.tue.appdev.studie;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

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
    String group_id;

    public GroupPagerAdapter(Context context, FragmentManager fm, int totalTabs, String group_id) {
        super(fm);
        mContext = context;
        this.totalTabs = totalTabs;
        this.group_id = group_id;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        Bundle bundle = new Bundle();
        bundle.putString("id", group_id);

        Fragment fragment;
        switch (position) {
            case 0:
                fragment = new NotesFragment();
                break;
            case 1:
                fragment = new FlashcardsFragment();
                break;
            case 2:
                fragment = new SetsFragment();
                break;
            default:
                return null;
        }

        fragment.setArguments(bundle);
        return fragment;
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