package com.brandonferrell.trumpsweeper.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.brandonferrell.trumpsweeper.activities.GameActivity;
import com.brandonferrell.trumpsweeper.R;

/**
 * Created by Brandon on 2/28/2016.
 */
public class GameEndFragment extends Fragment {

    private static final String TIME = "TIME";
    private static final String SWEPT = "SWEPT";

    private int swept;
    private String time;

    Button difficulty, playAgain;
    TextView timeView, sweptCountView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        time = getArguments().getString(TIME);
        swept = getArguments().getInt(SWEPT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_game_end, container, false);

        difficulty = (Button) v.findViewById(R.id.btn_change_difficulty);
        playAgain = (Button) v.findViewById(R.id.btn_play_again);

        timeView = (TextView) v.findViewById(R.id.timeResult);
        timeView.setText(time);

        sweptCountView = (TextView) v.findViewById(R.id.game_end_trumps_swept);
        sweptCountView.setText("" + this.swept);

        difficulty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start Fragment
                FragmentChangeListener fc = (FragmentChangeListener)getActivity();
                Fragment startFragment = new GameStartFragment();
                fc.replaceToolbarFragment(startFragment);

                // New Grid
                Fragment gameFragment = new SweeperImageButtonGridFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("SIZE", 7);
                gameFragment.setArguments(bundle);

                // Replace Game Fragment
                fc.replaceGameFragment(gameFragment);
            }
        });

        playAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playAgain();

                // Show Interstitial ad
                ((GameActivity) getActivity()).handleIntersitial();
            }
        });

        return v;
    }

    public void playAgain() {
        // Toolbar Fragment
        Fragment toolbarFragment = new GameToolbarFragment();
        FragmentChangeListener fc = (FragmentChangeListener)getActivity();
        fc.replaceToolbarFragment(toolbarFragment);

        // New Grid
        // Get the size of the old grid and make the new one the same size
        Fragment gameFragment = new SweeperImageButtonGridFragment();
        Fragment oldFragment = getFragmentManager().findFragmentByTag("GRID");
        // Set all the buttons in grid to be usable and reset
        if(oldFragment instanceof SweeperImageButtonGridFragment) {
            Bundle bundle = new Bundle();
            bundle.putInt("SIZE", ((SweeperImageButtonGridFragment) oldFragment).getSize());
            gameFragment.setArguments(bundle);
        }

        fc.replaceGameFragment(gameFragment);
    }

    public interface FragmentChangeListener {
        public void replaceToolbarFragment(Fragment fragment);
        public void replaceGameFragment(Fragment fragment);
    }
}
