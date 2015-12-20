package com.asus_s550cb.theo.museum;

/**
 * Created by Nasia on 30/8/2015.
 */

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Creates the fragment for every page of help mode
 * Get the text view and set the apropriate text
 */
public class HelpScreenFragment extends Fragment
{
    TextView tv;
    ImageView imgv;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.help_screen_fragment, container, false);
        tv = (TextView) view.findViewById(R.id.adviceTextView);
        tv.setText(getArguments().getString("pageContent"));

        imgv=(ImageView) view.findViewById(R.id.logoImageView);

        if(menu.lang.equals("uk")) {
            imgv.setImageResource(R.drawable.help_icon_en);
        }
        else
        {
            imgv.setImageResource(R.drawable.help_icon);
        }

        // Set listener on back button to close the screen
        // Cannot set on xml because it is not an Activity class
        ImageView bt=(ImageView) view.findViewById(R.id.backButton1);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

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
