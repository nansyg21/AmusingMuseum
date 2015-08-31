package com.asus_s550cb.theo.museum;

/**
 * Created by Nasia on 30/8/2015.
 */

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Creates the fragment for every page of help mode
 * Get the text view and set the apropriate text
 */
//TODO: Import screenshots of the riddles -> change the layout in the help_screen_fragment.xml
public class HelpScreenFragment extends Fragment
{
    TextView tv;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.help_screen_fragment, container, false);
        tv = (TextView) view.findViewById(R.id.adviceTextView);
        tv.setText(getArguments().getString("pageContent"));

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // Static method that updates the text on the textview of the fragment
    public static HelpScreenFragment newInstance(String page) {
        HelpScreenFragment pageFragment = new HelpScreenFragment();
        Bundle bundle = new Bundle();
        bundle.putString("pageContent", page);
        pageFragment.setArguments(bundle);

        return pageFragment;
    }


}
