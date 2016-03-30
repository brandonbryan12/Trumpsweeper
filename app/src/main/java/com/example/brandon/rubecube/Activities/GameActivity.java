package com.example.brandon.rubecube.Activities;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.LinearLayout;

import com.desarrollodroide.libraryfragmenttransactionextended.SlidingRelativeLayout;
import com.example.brandon.rubecube.Fragments.GameEndFragment;
import com.example.brandon.rubecube.Fragments.GameEndOverlay;
import com.example.brandon.rubecube.Fragments.GameStartFragment;
import com.example.brandon.rubecube.Fragments.GameToolbarFragment;
import com.example.brandon.rubecube.Helper;
import com.example.brandon.rubecube.R;
import com.example.brandon.rubecube.Fragments.SweeperImageButtonGridFragment;
import com.example.brandon.rubecube.SweeperImageButton;

public class GameActivity extends AppCompatActivity implements GameStartFragment.FragmentChangeListener, GameToolbarFragment.FragmentChangeListener, SweeperImageButton.OnGameMovePlayedListener, GameEndFragment.FragmentChangeListener {

    FragmentManager fragmentManager = getFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    SharedPreferences prefs;

    SweeperImageButtonGridFragment mGameFragment;
    Helper mHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        mHelper = new Helper(this);

        Bundle bundle = new Bundle();
        bundle.putInt("SIZE", 7);

        fragmentTransaction = fragmentManager.beginTransaction();

        Fragment fragmentgamestart = new GameStartFragment();

        Fragment fragmentgamegrid = new SweeperImageButtonGridFragment();
        fragmentgamegrid.setArguments(bundle);

        fragmentTransaction.add(R.id.fragment_bottom_containter, fragmentgamestart).commit();

        fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.add(R.id.fragment_game_containter, fragmentgamegrid, "GRID").commit();
        
        mGameFragment = (SweeperImageButtonGridFragment) fragmentgamegrid;
        
    }

    @Override
    public void replaceToolbarFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        Fragment oldFrag = getFragmentManager().findFragmentById(R.id.fragment_bottom_containter);

        fragmentTransaction.setCustomAnimations(R.anim.card_flip_right_in,
                R.anim.card_flip_right_out,
                R.anim.card_flip_right_in,
                R.anim.card_flip_left_out);

        fragmentTransaction.replace(R.id.fragment_bottom_containter, fragment, fragment.toString());

        // When the old fragment is the start fragment, add grid to backstack
        // TODO: When back is pressed during game, ask user if they're sure they want to quit
        if(oldFrag instanceof GameStartFragment)
            fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    /**
     *
     * @param newFragment   Fragment replacing old one
     */
    @Override
    public void replaceGameFragment(Fragment newFragment) {
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_game_containter, newFragment, "GRID");
        fragmentTransaction.commit();
        mGameFragment = (SweeperImageButtonGridFragment) newFragment;
    }

    @Override
    public void addFlag(SweeperImageButton button) {
        if(mGameFragment.addFlag()) {
            button.setIsFlagged(true);

            checkIfWin();

            // Update toolbar with new flag numbers
            Fragment toolBarFrag = (GameToolbarFragment) getFragmentManager().findFragmentById(R.id.fragment_bottom_containter);
            if(toolBarFrag instanceof GameToolbarFragment)
                ((GameToolbarFragment) toolBarFrag).incFlagCount();
        }
    }

    @Override
    public void removeFlag(SweeperImageButton button) {
        mGameFragment.removeFlag();
        button.setIsFlagged(false);


        // Update toolbar with new flag numbers
        Fragment toolBarFrag = (GameToolbarFragment) getFragmentManager().findFragmentById(R.id.fragment_bottom_containter);
        if(toolBarFrag instanceof GameToolbarFragment)
            ((GameToolbarFragment) toolBarFrag).decFlagCount();
    }

    @Override
    public boolean tileUncovered() {
       return checkIfWin();
    }

    @Override
    public void endGame(String reason) {
        Bundle trumpBundle = new Bundle();
        Bundle gameEndBundle = new Bundle();

        fragmentTransaction = fragmentManager.beginTransaction();

        Fragment gameEndFragment = new GameEndFragment();

        int sweptCount = 0;
        SweeperImageButton cells[][] = mGameFragment.cells;

        for(int i = 0; i < mGameFragment.getSize(); i++) {
            for(int j = 0; j < mGameFragment.getSize(); j++) {
                if(cells[i][j].isMine && cells[i][j].getIsFlagged())
                    sweptCount++;
            }
        }

        Chronometer cr = (Chronometer) findViewById(R.id.timeCount);

        gameEndBundle.putInt("SWEPT", sweptCount);

        gameEndBundle.putString("TIME", "" + cr.getText());

        gameEndFragment.setArguments(gameEndBundle);

        fragmentTransaction.replace(R.id.fragment_bottom_containter, gameEndFragment, gameEndFragment.toString()).commit();

        mGameFragment.setEnabled(false);

        if(reason.equals("mine")) {
            // show all mines
            for(int i = 0; i < mGameFragment.getSize(); i++) {
                for(int j = 0; j < mGameFragment.getSize(); j++) {
                    if(mGameFragment.cells[i][j].isMine)
                        mGameFragment.cells[i][j].setBackgroundResource(mHelper.getMineResource(false));

                }
            }

            AnimationSet animationSet = new AnimationSet(true);

            Animation shake = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shake);

            animationSet.addAnimation(shake);


            View view = findViewById(R.id.fragment_game_containter);

            view.startAnimation(animationSet);

            if(prefs.getBoolean("vibrate_preference", true)) {
                Vibrator vibr = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibr.vibrate(600);
            }

            // Set game outcome to lose for overlay
            trumpBundle.putBoolean("WIN", false);
        }
        else if(reason.equals("win")) {
            Log.d("DEBUG", "WIN");
            // Set game outcome to win for overlay
            trumpBundle.putBoolean("WIN", true);
        }
        else if(reason.equals("time")) {
            // Set game outcome to lose for overlay
            trumpBundle.putBoolean("WIN", false);
        }

        Fragment gameEndOverlayFrag = new GameEndOverlay();
        gameEndOverlayFrag.setArguments(trumpBundle);

        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment_game_end_containter, gameEndOverlayFrag).commit();

    }

    @Override
    public void setNeighborsUncovered(int x, int y) {
        SweeperImageButton cells[][] = mGameFragment.cells;
        int size = mGameFragment.getSize();

        // Top left
        if(x - 1 >= 0 && y - 1 >= 0 && !cells[x - 1][y - 1].getIsRevealed())
            if(cells[x - 1][y - 1].reveal())
                return;

        // Top
        if(y - 1 >= 0 && !cells[x][y - 1].getIsRevealed())
            if(cells[x][y - 1].reveal())
                return;

        // Top right
        if(x + 1 < size && y - 1 >= 0 && !cells[x + 1][y - 1].getIsRevealed())
            if(cells[x + 1][y - 1].reveal())
                return;

        // Right
        if(x + 1 < size  && !cells[x + 1][y].getIsRevealed())
            if(cells[x + 1][y].reveal())
                return;

        // Bottom right
        if(x + 1 < size && y + 1 < size && !cells[x + 1][y + 1].getIsRevealed())
            if(cells[x + 1][y + 1].reveal())
                return;

        // Bottom
        if(y + 1 < size  && !cells[x][y + 1].getIsRevealed())
            if(cells[x][y + 1].reveal())
                return;

        // Bottom left
        if(x - 1 >= 0 && y + 1 < size && !cells[x - 1][y + 1].getIsRevealed())
            if(cells[x - 1][y + 1].reveal())
                return;

        // Left
        if(x - 1 >= 0 && !cells[x - 1][y].getIsRevealed())
            if(cells[x - 1][y].reveal())
                return;
    }

    public boolean checkIfWin() {
        // Check if there is a win
        boolean win = true;
        SweeperImageButton cells[][] = mGameFragment.cells;

        for(int i = 0; i < mGameFragment.getSize(); i++) {
            for(int j = 0; j < mGameFragment.getSize(); j++) {
                if(cells[i][j].isMine && !cells[i][j].getIsFlagged() || (!cells[i][j].isMine && !cells[i][j].getIsRevealed()))
                    win = false;
            }
        }

        if(win)
            endGame("win");

        return win;
    }

    @Override
    public void onBackPressed() {
        Fragment toolBarFrag = getFragmentManager().findFragmentById(R.id.fragment_bottom_containter);
        if(toolBarFrag instanceof GameToolbarFragment) {
            // Create Dialog
            final Dialog dialog = new Dialog(this);

            //setting custom layout to dialog
            dialog.setContentView(R.layout.dialog_confirm_back);

            dialog.setTitle(R.string.quit_confirm);

            Button yes = (Button) dialog.findViewById(R.id.dialog_btn_yes);
            Button no = (Button) dialog.findViewById(R.id.dialog_btn_no);

            yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Start Fragment
                    Fragment startFragment = new GameStartFragment();
                    replaceToolbarFragment(startFragment);

                    // New Grid
                    Fragment gameFragment = new SweeperImageButtonGridFragment();
                    Bundle bundle = new Bundle();
                    bundle.putInt("SIZE", 7);
                    gameFragment.setArguments(bundle);

                    // Replace Game Fragment
                    replaceGameFragment(gameFragment);

                    dialog.dismiss();
                }
            });

            no.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            dialog.show();
        }
        else {
            callBackPresssed();
        }

    }

    public void callBackPresssed() {
        super.onBackPressed();
    }
}
