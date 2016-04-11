package com.brandonferrell.trumpsweeper.Fragments;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Interpolator;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;

import com.brandonferrell.trumpsweeper.R;

public class GameStartFragment extends Fragment {
    private static final int CONTAINER = R.id.fragment_bottom_containter;

    Button bstart, bdiffs[];
    LinearLayout arrows;


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

        getActivity().findViewById(R.id.select_difficulty).setVisibility(View.VISIBLE);

        arrows = (LinearLayout) getActivity().findViewById(R.id.arrows);

        setArrowAnimation();

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

            // Create new game with size of apprentice
            replaceGameFragmentStart(getResources().getInteger(R.integer.size_apprentice));
        }
    }

    private void onIntermediatePress(Button[] bdiffs) {
        if(!bdiffs[1].isPressed()) {
            //Change grid size above
            bdiffs[0].setPressed(false);
            bdiffs[1].setPressed(true);
            bdiffs[2].setPressed(false);

            replaceGameFragmentStart(getResources().getInteger(R.integer.size_ceo));
        }
    }

    private void onExpertPress(Button[] bdiffs) {
        if(!bdiffs[2].isPressed()) {
            //Change grid size above
            bdiffs[0].setPressed(false);
            bdiffs[1].setPressed(false);
            bdiffs[2].setPressed(true);

            replaceGameFragmentStart(getResources().getInteger(R.integer.size_president));
        }
    }

    @Override
    public void onDestroyView() {
        getActivity().findViewById(R.id.select_difficulty).setVisibility(View.INVISIBLE);
        super.onDestroy();
    }

    private void setArrowAnimation() {
        final Animation arrowAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.arrow_bounce);
        arrowAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                arrows.startAnimation(arrowAnimation);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        arrows.startAnimation(arrowAnimation);

    }

    public interface FragmentChangeListener
    {
        public void replaceToolbarFragment(Fragment fragment);
        public void replaceGameFragment(Fragment fragment);
    }

}
