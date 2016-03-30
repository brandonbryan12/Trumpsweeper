package com.example.brandon.rubecube;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageButton;

import com.example.brandon.rubecube.Fragments.SweeperImageButtonGridFragment;

/**
 * Created by Brandon on 2/28/2016.
 */
public class SweeperImageButton extends ImageButton implements View.OnClickListener{
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

    public boolean reveal() {
        if(isFlagged)
            return false;

        if(isMine) {
            movePlayedListener.endGame("mine");
            return true;
        }

        isRevealed = true;

        if(mine_neighbors == 0) {
            setBackgroundResource(mHelper.getTileResource(true));
            movePlayedListener.setNeighborsUncovered(this.x, this.y);
        }
        else {
            setBackgroundResource(mHelper.getCellResource(mine_neighbors));
        }

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

        this.movePlayedListener = (OnGameMovePlayedListener) context;

        init();
    }

    private void init() {
        // Set the click listener
        setOnClickListener(this);

        // Set image of unpressed button
        setBackgroundResource(mHelper.getTileResource(false));
    }

    @Override
    public void onClick(View v) {
        ImageButton btnFlag = (ImageButton) ((Activity)getContext()).findViewById(R.id.btn_flag);
        ImageButton btnShovel = (ImageButton) ((Activity)getContext()).findViewById(R.id.btn_shovel);

        // Shovel
        if(btnFlag.isEnabled()) {
            if(!isFlagged) {
                if(gridFragment.moveMade()) {
                    if(isMine) {
                        isMine = false;
                        gridFragment.addRandomMine();
                    }
                    reveal();
                    playRandomClipRandomly();
                    return;
                }

                if(isMine) {
                    movePlayedListener.endGame("mine");
                    setBackgroundResource(mHelper.getMineResource(true));
                }
                else if(!isRevealed){
                    reveal();

                    if(!movePlayedListener.tileUncovered())
                        playRandomClipRandomly();
                }
                else {
                    movePlayedListener.setNeighborsUncovered(x, y);
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
    public void playRandomClipRandomly() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        if(prefs.getBoolean("china_preference", false)) {
            int id = 0;
            int randId;
            int randPlay = (int) (Math.random() * 100);

            // 10% chance of playing
            if(randPlay < 5) {
                randId = (int) (Math.random() * 2);
                switch (randId) {
                    case 0:
                        id = R.raw.move_china;
                        break;
                    case 1:
                        id = R.raw.move_china;
                        break;
                }

                // Create a new MediaPlayer to play this sound
                MediaPlayer mp = MediaPlayer.create(getContext(), id);
                mp.start();
            }
        }

    }

    public interface OnGameMovePlayedListener {
        void endGame(String reason);
        void addFlag(SweeperImageButton button);
        void removeFlag(SweeperImageButton button);
        boolean tileUncovered();
        void setNeighborsUncovered(int x, int y);
    }
}
