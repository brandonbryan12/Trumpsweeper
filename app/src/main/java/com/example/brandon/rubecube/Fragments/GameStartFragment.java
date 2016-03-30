package com.example.brandon.rubecube.Fragments;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.brandon.rubecube.R;

public class GameStartFragment extends Fragment {
    private static final int CONTAINER = R.id.fragment_bottom_containter;

    Button bstart, bdiffs[];


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void addToolbarFragment()
    {
        Fragment fr = new GameToolbarFragment();
        FragmentChangeListener fc = (FragmentChangeListener)getActivity();
        fc.replaceToolbarFragment(fr);

        // Get the grid fragment
        Fragment gfr = getFragmentManager().findFragmentByTag("GRID");

        // Set all the buttons in grid to be usable
        if(gfr instanceof SweeperImageButtonGridFragment)
            ((SweeperImageButtonGridFragment) gfr).setEnabled(true);

    }

    public void replaceGameFragmentStart(int n) {
        Fragment fr = new SweeperImageButtonGridFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("SIZE", n);
        fr.setArguments(bundle);

        FragmentChangeListener fc = (FragmentChangeListener)getActivity();
        fc.replaceGameFragment(fr);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootview = inflater.inflate(R.layout.fragment_game_start, container, false);

        bstart = (Button) rootview.findViewById(R.id.start_button);
        bstart.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                addToolbarFragment();
            }
        });

        bdiffs = new Button[] {
                (Button) rootview.findViewById(R.id.d_beginner),
                (Button) rootview.findViewById(R.id.d_intermediate),
                (Button) rootview.findViewById(R.id.d_expert)
        };

        bdiffs[0].setPressed(true);

        bdiffs[0].setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                onBeginnerPress(bdiffs);
                return true;
            }
        });


        bdiffs[1].setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                onIntermediatePress(bdiffs);
                return true;
            }
        });


        bdiffs[2].setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                onExpertPress(bdiffs);
                return true;
            }
        });


        return rootview;
    }


    private void onBeginnerPress(Button[] bdiffs) {
        if(!bdiffs[0].isPressed())
        {
            //Change grid size above
            bdiffs[0].setPressed(true);
            bdiffs[1].setPressed(false);
            bdiffs[2].setPressed(false);

            // Create new game with size 7
            replaceGameFragmentStart(7);
        }
    }

    private void onIntermediatePress(Button[] bdiffs) {
        if(!bdiffs[1].isPressed()) {
            //Change grid size above
            bdiffs[0].setPressed(false);
            bdiffs[1].setPressed(true);
            bdiffs[2].setPressed(false);

            replaceGameFragmentStart(10);
        }
    }

    private void onExpertPress(Button[] bdiffs) {
        if(!bdiffs[2].isPressed()) {
            //Change grid size above
            bdiffs[0].setPressed(false);
            bdiffs[1].setPressed(false);
            bdiffs[2].setPressed(true);

            replaceGameFragmentStart(15);
        }
    }

    public interface FragmentChangeListener
    {
        public void replaceToolbarFragment(Fragment fragment);
        public void replaceGameFragment(Fragment fragment);
    }

}
