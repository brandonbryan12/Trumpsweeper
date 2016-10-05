package com.brandonferrell.trumpsweeper.models;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageButton;

import com.brandonferrell.trumpsweeper.R;
import com.brandonferrell.trumpsweeper.activities.GameActivity;
import com.brandonferrell.trumpsweeper.fragments.SweeperImageButtonGridFragment;
import com.brandonferrell.trumpsweeper.util.Helper;
import com.plattysoft.leonids.ParticleSystem;

/**
 * Created by Brandon on 2/28/2016.
 */
public class SweeperImageButton extends ImageButton implements View.OnClickListener, View.OnLongClickListener{
    public int x;
    public int y;
    public int mine_neighbors;
    public boolean isMine;
    boolean isRevealed;
    private boolean isFlagged;
    private OnGameMovePlayedListener movePlayedListener;
    private SweeperImageButtonGridFragment gridFragment;
    private Helper mHelper;

    public boolean getIsFlagged(){
        return isFlagged;
    }

    public void setIsFlagged(boolean value){
        isFlagged = value;

        if(isFlagged)
            setBackgroundResource(mHelper.getFlagResource(false));
        else {
            setBackgroundResource(mHelper.getTileResource(false));
        }
    }

    public boolean getIsRevealed() {
        return isRevealed;
    }

    public boolean reveal(boolean neighborCall) {
        if(isFlagged)
            return false;

        if(isMine) {
            movePlayedListener.endGame("mine");
            setBackgroundResource(mHelper.getMineResource(true, isFlagged));
            return true;
        }

        isRevealed = true;

        if(((GameActivity) gridFragment.getActivity()).prefs.getBoolean("particle_preference", true) && neighborCall)
            new ParticleSystem(gridFragment.getActivity(), 20, R.drawable.sparkle, 150)
                .setSpeedRange(0.2f, 0.7f)
                .oneShot(this, 20);

        if(mine_neighbors == 0) {
            setBackgroundResource(mHelper.getTileResource(true));
            movePlayedListener.setNeighborsUncovered(this.x, this.y);
        }
        else {
            setBackgroundResource(mHelper.getCellResource(mine_neighbors));
        }

        movePlayedListener.tileUncovered();

        return false;
    }

    public SweeperImageButton(Context context, SweeperImageButtonGridFragment fragment, int x, int y) {
        super(context);
        this.x = x;
        this.y = y;
        this.isMine = false;
        this.mine_neighbors = 0;
        gridFragment = fragment;
        mHelper = new Helper(context);

        this.setSoundEffectsEnabled(false);

        this.movePlayedListener = (OnGameMovePlayedListener) context;

        init();
    }

    private void init() {
        // Set the click listener
        setOnClickListener(this);

        setOnLongClickListener(this);

        // Set image of unpressed button
        setBackgroundResource(mHelper.getTileResource(false));
    }

    @Override
    public void onClick(View v) {
        ImageButton btnToupee = (ImageButton) ((Activity)getContext()).findViewById(R.id.toupee_button);
        //ImageButton btnShovel = (ImageButton) ((Activity)getContext()).findViewById(R.id.btn_shovel);

        // Shovel
        //if(btnFlag.isEnabled()) {
        if(!btnToupee.isSelected()) {
            if(!isFlagged) {
                if(gridFragment.moveMade()) {
                    if(isMine) {
                        isMine = false;
                        gridFragment.addRandomMine();
                    }
                    reveal(false);
                    playChina();

                    movePlayedListener.playTileOpened();

                    return;
                }

                if(isMine) {
                    movePlayedListener.endGame("mine");
                    setBackgroundResource(mHelper.getMineResource(true, isFlagged));
                }
                else if(!isRevealed){
                    reveal(false);

                    movePlayedListener.playTileOpened();

                    if(!movePlayedListener.tileUncovered())
                        playChina();
                }
                else {
                    movePlayedListener.setNeighborsUncovered(x, y);
                    movePlayedListener.playNeighborsClip();
                    return;
                }
            }
        }
        // Flag
        else {
            if(isFlagged) {
                movePlayedListener.removeFlag(this);
            }
            else if(!isRevealed){
                movePlayedListener.addFlag(this);
                setEnabled(true);
            }
            else {
                return;
            }
        }

        Vibrator vibr = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        vibr.vibrate(15);
    }

    /**
     * Play a random clip (type:move) of Trump with 5% chance
     */
    public void playChina() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        if(prefs.getBoolean("china_preference", false) && prefs.getBoolean("volume_preference", true)) {
            int id = 0;
            int randId;
            int randPlay = (int) (Math.random() * 100);

            // 10% chance of playing
            if(randPlay < prefs.getInt("china_slider_preference", 5)) {
                movePlayedListener.playChinaClip();
            }
        }

    }

    @Override
    public boolean onLongClick(View v) {
        if(isFlagged) {
            movePlayedListener.removeFlag(this);
        }
        else if(!isRevealed){
            movePlayedListener.addFlag(this);
            setEnabled(true);
        }
        return true;
    }

    public interface OnGameMovePlayedListener {
        void endGame(String reason);
        void addFlag(SweeperImageButton button);
        void removeFlag(SweeperImageButton button);
        boolean tileUncovered();
        void setNeighborsUncovered(int x, int y);
        void playTileOpened();
        void playTrumpClip(int id);
        void playChinaClip();
        void playNeighborsClip();
    }
}
