package com.example.safegallery.tabs.main;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import com.example.safegallery.Constants;
import com.example.safegallery.tabs.data.DataType;
import com.example.safegallery.tabs.fragments.ParentFragment;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {
    FragmentManager fragmentManager;

    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.fragmentManager = fm;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        DataType[] values = DataType.values();
        if (position < values.length * 2)
            return new ParentFragment(position, (position >= values.length), values[position % values.length]);

        return new ParentFragment(position, false, DataType.Gallery);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        int len = DataType.values().length;

        if (position < len * 2)
            return position < len
                    ? DataType.values()[position].name()
                    : "Safe " + DataType.values()[position % len].name();

        return "Intruders";
    }

    @Override
    public int getCount() {
        return Constants.TAB_LENGTH;
    }
}