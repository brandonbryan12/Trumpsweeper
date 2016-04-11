package com.brandonferrell.trumpsweeper.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.SystemClock;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.Chronometer;

import com.brandonferrell.trumpsweeper.Fragments.GameEndFragment;
import com.brandonferrell.trumpsweeper.Fragments.GameEndOverlay;
import com.brandonferrell.trumpsweeper.Fragments.GameStartFragment;
import com.brandonferrell.trumpsweeper.Fragments.GameToolbarFragment;
import com.brandonferrell.trumpsweeper.Helper;
import com.brandonferrell.trumpsweeper.R;
import com.brandonferrell.trumpsweeper.Fragments.SweeperImageButtonGridFragment;
import com.brandonferrell.trumpsweeper.SweeperImageButton;
import com.brandonferrell.trumpsweeper.newHigh;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.leaderboard.LeaderboardScore;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.google.android.gms.games.leaderboard.Leaderboards;

import java.util.ArrayList;
import java.util.List;

public class GameActivity extends AppCompatActivity implements GameStartFragment.FragmentChangeListener, GameToolbarFragment.FragmentChangeListener, SweeperImageButton.OnGameMovePlayedListener, GameEndFragment.FragmentChangeListener, GameEndOverlay.OnClipPlayedListener, GameEndOverlay.ProfileInterface, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        GameEndOverlay.AchieveInterface {

    private static final String PLAYER = "PLAYER";

    private Player mPlayer;

    FragmentManager fragmentManager = getFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    public SharedPreferences prefs;
    SharedPreferences.Editor editor;
    MenuItem volumeUp, volumeMute;
    MediaPlayer trumpMediaPlayer, flagMediaPlayer, tileMediaPlayer, chinaMediaPlayer, neighborsMediaPlayer;
    int lastClipId;

    SweeperImageButtonGridFragment mGameFragment;
    Helper mHelper;

    // Client used to interact with Google APIs.
    private GoogleApiClient mGoogleApiClient;

    // Has the user clicked the sign-in button?
    private boolean mSignInClicked = false;

    // Are we currently resolving a connection failure?
    private boolean mResolvingConnectionFailure = false;

    // Set to true to automatically start the sign in flow when the Activity starts.
    // Set to false to require the user to click the button in order to sign in.
    private boolean mAutoStartSignInFlow = false;

    // Request code used to invoke sign in user interactions.
    private static final int RC_SIGN_IN = 9001;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        Bundle b = getIntent().getExtras();
        if(b != null)
            mPlayer  = b.getParcelable(PLAYER);

        setUpMediaPlayers();

        lastClipId = 0;

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        editor = prefs.edit();

        mHelper = new Helper(this);

        Bundle bundle = new Bundle();
        bundle.putInt("SIZE", getResources().getInteger(R.integer.size_apprentice));

        fragmentTransaction = fragmentManager.beginTransaction();

        Fragment fragmentgamestart = new GameStartFragment();

        Fragment fragmentgamegrid = new SweeperImageButtonGridFragment();
        fragmentgamegrid.setArguments(bundle);

        fragmentTransaction.add(R.id.fragment_bottom_containter, fragmentgamestart).commit();

        fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.add(R.id.fragment_game_containter, fragmentgamegrid, "GRID").commit();
        
        mGameFragment = (SweeperImageButtonGridFragment) fragmentgamegrid;

        // Create the Google Api Client with access to Plus and Games
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();
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

            if(prefs.getBoolean("volume_preference", true)) {
                flagMediaPlayer.seekTo(0);
                flagMediaPlayer.start();
            }

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

        if(prefs.getBoolean("volume_preference", true)) {
            flagMediaPlayer.seekTo(0);
            flagMediaPlayer.start();
        }

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
            // Set game outcome to win for overlay
            trumpBundle.putBoolean("WIN", true);
        }
        else if(reason.equals("time")) {
            // Set game outcome to lose for overlay
            trumpBundle.putBoolean("WIN", false);
        }

        trumpBundle.putInt("SIZE", mGameFragment.getSize());
        trumpBundle.putInt("TOUPEES", sweptCount);
        trumpBundle.putDouble("TIME", SystemClock.elapsedRealtime() - cr.getBase());

        Fragment gameEndOverlayFrag = new GameEndOverlay();
        gameEndOverlayFrag.setArguments(trumpBundle);

        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_game_end_containter, gameEndOverlayFrag).commit();


    }

    @Override
    public void setNeighborsUncovered(int x, int y) {
        SweeperImageButton cells[][] = mGameFragment.cells;
        int size = mGameFragment.getSize();

        // Top left
        if(x - 1 >= 0 && y - 1 >= 0 && !cells[x - 1][y - 1].getIsRevealed())
            if(cells[x - 1][y - 1].reveal(true))
                return;

        // Top
        if(y - 1 >= 0 && !cells[x][y - 1].getIsRevealed())
            if(cells[x][y - 1].reveal(true))
                return;

        // Top right
        if(x + 1 < size && y - 1 >= 0 && !cells[x + 1][y - 1].getIsRevealed())
            if(cells[x + 1][y - 1].reveal(true))
                return;

        // Right
        if(x + 1 < size  && !cells[x + 1][y].getIsRevealed())
            if(cells[x + 1][y].reveal(true))
                return;

        // Bottom right
        if(x + 1 < size && y + 1 < size && !cells[x + 1][y + 1].getIsRevealed())
            if(cells[x + 1][y + 1].reveal(true))
                return;

        // Bottom
        if(y + 1 < size  && !cells[x][y + 1].getIsRevealed())
            if(cells[x][y + 1].reveal(true))
                return;

        // Bottom left
        if(x - 1 >= 0 && y + 1 < size && !cells[x - 1][y + 1].getIsRevealed())
            if(cells[x - 1][y + 1].reveal(true))
                return;

        // Left
        if(x - 1 >= 0 && !cells[x - 1][y].getIsRevealed())
            if(cells[x - 1][y].reveal(true))
                return;
    }

    @Override
    public void playTileOpened() {
        if(prefs.getBoolean("volume_preference", true)) {
            flagMediaPlayer.seekTo(0);
            tileMediaPlayer.start();
        }
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

        if(win && fragmentManager.findFragmentById(R.id.fragment_game_end_containter) == null)
            endGame("win");

        return win;
    }



    @Override
    public void onBackPressed() {
        Fragment toolBarFrag = getFragmentManager().findFragmentById(R.id.fragment_bottom_containter);
        if(toolBarFrag instanceof GameToolbarFragment) {

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(GameActivity.this);

            alertDialogBuilder.setTitle("You're about to quit your game");
            alertDialogBuilder.setMessage("Are you sure?");

            // set positive button: Yes message
            alertDialogBuilder.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int id) {
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

            // set negative button: No message
            alertDialogBuilder.setNegativeButton("No",new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });

            alertDialogBuilder.show();
        }
        else {
            callBackPresssed();
        }

    }

    public void callBackPresssed() {
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.volume_control_up) {
            volumeUp.setVisible(false);
            volumeMute.setVisible(true);
            editor.putBoolean("volume_preference", false);
            editor.commit();
            trumpMediaPlayer.stop();
        }
        else if(item.getItemId() == R.id.volume_control_mute) {
            volumeUp.setVisible(true);
            volumeMute.setVisible(false);
            editor.putBoolean("volume_preference", true);
            editor.commit();
        }
        else if(item.getItemId() == android.R.id.home)
            onBackPressed();


        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        volumeUp = menu.findItem(R.id.volume_control_up);
        volumeMute = menu.findItem(R.id.volume_control_mute);

        if(prefs.getBoolean("volume_preference", true)) {
            volumeMute.setVisible(false);
        }
        else {
            volumeUp.setVisible(false);
        }

        return true;
    }

    @Override
    public void playTrumpClip(int id) {
        if(prefs.getBoolean("volume_preference", true) && trumpMediaPlayer != null) {
            trumpMediaPlayer.stop();
            trumpMediaPlayer = MediaPlayer.create(this, id);
            trumpMediaPlayer.setVolume(30f, 30f);
            trumpMediaPlayer.start();
        }
    }

    @Override
    public void playChinaClip() {
        chinaMediaPlayer.seekTo(0);
        chinaMediaPlayer.start();
    }

    @Override
    public void playNeighborsClip() {
        neighborsMediaPlayer.seekTo(0);
        neighborsMediaPlayer.start();
    }

    public void setLastClipId(int id) { lastClipId = id; }

    public int getLastClipId() { return lastClipId; }

    public Player getPlayer() { return mPlayer; }

    public GoogleApiClient getGoogleApiClient() {return mGoogleApiClient; }

    @Override
    public void endGameSignIn() {
        mSignInClicked = true;
        mGoogleApiClient.connect();
    }

    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        mPlayer = Games.Players.getCurrentPlayer(mGoogleApiClient);
    }

    @Override
    public void onConnectionSuspended(int i) {
        mPlayer = null;
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        if (mResolvingConnectionFailure) {
            return;
        }

        if (mSignInClicked || mAutoStartSignInFlow) {
            mAutoStartSignInFlow = false;
            mSignInClicked = false;
            mResolvingConnectionFailure = resolveConnectionFailure(this, mGoogleApiClient,
                    connectionResult, RC_SIGN_IN, "error");
        }
    }

    public static boolean resolveConnectionFailure(Activity activity,
                                                   GoogleApiClient client, ConnectionResult result, int requestCode,
                                                   String fallbackErrorMessage) {

        if (result.hasResolution()) {
            try {
                result.startResolutionForResult(activity, requestCode);
                return true;
            } catch (IntentSender.SendIntentException e) {
                // The intent was canceled before it was sent.  Return to the default
                // state and attempt to connect to get an updated ConnectionResult.
                client.connect();
                return false;
            }
        } else {
            // not resolvable... so show an error message
            int errorCode = result.getErrorCode();
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(errorCode,
                    activity, requestCode);
            if (dialog != null) {
                dialog.show();
            } else {
                // no built-in dialog: show the fallback error message
                //showAlert(activity, fallbackErrorMessage);
            }
            return false;
        }
    }

    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        if (requestCode == RC_SIGN_IN) {
            mSignInClicked = false;
            mResolvingConnectionFailure = false;
            if (responseCode == RESULT_OK) {
                mGoogleApiClient.connect();
            } /*else {
                BaseGameUtils.showActivityResultError(this,requestCode,responseCode, R.string.signin_other_error);
            }*/
        }
    }

    public void setUpMediaPlayers() {
        trumpMediaPlayer = new MediaPlayer();

        chinaMediaPlayer = MediaPlayer.create(this, R.raw.move_china);
        chinaMediaPlayer.setVolume(10f, 10f);

        tileMediaPlayer = MediaPlayer.create(this, R.raw.tile_open);
        tileMediaPlayer.setVolume(.8f, .8f);

        flagMediaPlayer = MediaPlayer.create(this, R.raw.tile_flag);
        flagMediaPlayer.setVolume(.8f, .8f);

        neighborsMediaPlayer = MediaPlayer.create(this, R.raw.uncover_neighbors);
        neighborsMediaPlayer.setVolume(.3f, .3f);
    }

    @Override
    protected void onPause() {
        super.onPause();
        StartActivity.themeMediaPlayer.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("music_preference", true))
            StartActivity.themeMediaPlayer.start();
    }

    @Override
    public void handleAchievementsLeaders(boolean win, int size, double t, final int toupees) {

        if(win) {
            final long time = (long) t;

            Games.Achievements.increment(mGoogleApiClient, getResources().getString(R.string.achievement_ocd_hairdresser), toupees);

            // Toupees
            Games.Leaderboards.loadCurrentPlayerLeaderboardScore(mGoogleApiClient,
                    getString(R.string.leaderboard_trumps_toupeed),
                    LeaderboardVariant.TIME_SPAN_ALL_TIME,
                    LeaderboardVariant.COLLECTION_PUBLIC).setResultCallback(
                    new ResultCallback<Leaderboards.LoadPlayerScoreResult>() {

                        @Override
                        public void onResult(Leaderboards.LoadPlayerScoreResult arg0) {
                            LeaderboardScore c = arg0.getScore();
                            int old;
                            if (c != null)
                                old = (int) c.getRawScore();
                            else
                                old = 0;
                            Games.Leaderboards.submitScore(mGoogleApiClient, getResources().getString(R.string.leaderboard_trumps_toupeed), old + toupees);
                        }
                    });

            if (size == getResources().getInteger(R.integer.size_apprentice)) {
                Games.Achievements.unlock(mGoogleApiClient, getResources().getString(R.string.achievement_chump_change));
                Games.Achievements.increment(mGoogleApiClient, getResources().getString(R.string.achievement_chump_change_v_2), 1);
                Games.Achievements.increment(mGoogleApiClient, getResources().getString(R.string.achievement_chump_change_v_3), 1);

                if(time <= 30000)
                    Games.Achievements.unlock(mGoogleApiClient, getResources().getString(R.string.achievement_thats_very_unimpressive));


                // Wins
                Games.Leaderboards.loadCurrentPlayerLeaderboardScore(mGoogleApiClient,
                        getString(R.string.leaderboard_apprentice_wins),
                        LeaderboardVariant.TIME_SPAN_ALL_TIME,
                        LeaderboardVariant.COLLECTION_PUBLIC).setResultCallback(
                        new ResultCallback<Leaderboards.LoadPlayerScoreResult>() {

                            @Override
                            public void onResult(Leaderboards.LoadPlayerScoreResult arg0) {
                                LeaderboardScore c = arg0.getScore();
                                int wins;
                                if (c != null)
                                    wins = (int) c.getRawScore();
                                else
                                    wins = 0;
                                Games.Leaderboards.submitScore(mGoogleApiClient, getResources().getString(R.string.leaderboard_apprentice_wins), wins + 1);
                            }
                        });

                // Speed
                Games.Leaderboards.loadCurrentPlayerLeaderboardScore(mGoogleApiClient,
                        getString(R.string.leaderboard_fastest_apprentice),
                        LeaderboardVariant.TIME_SPAN_ALL_TIME,
                        LeaderboardVariant.COLLECTION_PUBLIC).setResultCallback(
                        new ResultCallback<Leaderboards.LoadPlayerScoreResult>() {

                            @Override
                            public void onResult(Leaderboards.LoadPlayerScoreResult arg0) {
                                LeaderboardScore c = arg0.getScore();
                                long old_time;
                                if(c != null) {
                                    old_time = c.getRawScore();
                                    if(time < old_time) {
                                        Games.Leaderboards.submitScore(mGoogleApiClient, getResources().getString(R.string.leaderboard_fastest_apprentice), time);
                                    }
                                        Games.Leaderboards.submitScore(mGoogleApiClient, getResources().getString(R.string.leaderboard_fastest_apprentice), time);
                                }
                                else {
                                    Games.Leaderboards.submitScore(mGoogleApiClient, getResources().getString(R.string.leaderboard_fastest_apprentice), time);
                                }
                            }
                        });
            } else if (size == getResources().getInteger(R.integer.size_ceo)) {
                Games.Achievements.unlock(mGoogleApiClient, getResources().getString(R.string.achievement_whos_this_guy));
                Games.Achievements.increment(mGoogleApiClient, getResources().getString(R.string.achievement_whos_this_guy_v_2), 1);
                Games.Achievements.increment(mGoogleApiClient, getResources().getString(R.string.achievement_whos_this_guy_v_3), 1);

                if(time <= 60000)
                    Games.Achievements.unlock(mGoogleApiClient, getResources().getString(R.string.achievement_climbing_the_polls));

                Games.Leaderboards.loadCurrentPlayerLeaderboardScore(mGoogleApiClient,
                        getString(R.string.leaderboard_ceo_wins),
                        LeaderboardVariant.TIME_SPAN_ALL_TIME,
                        LeaderboardVariant.COLLECTION_PUBLIC).setResultCallback(
                        new ResultCallback<Leaderboards.LoadPlayerScoreResult>() {

                            @Override
                            public void onResult(Leaderboards.LoadPlayerScoreResult arg0) {
                                LeaderboardScore c = arg0.getScore();
                                int wins;
                                if(c != null)
                                    wins = (int) c.getRawScore();
                                else
                                    wins = 0;
                                Games.Leaderboards.submitScore(mGoogleApiClient, getResources().getString(R.string.leaderboard_ceo_wins), wins + 1);
                            }
                        });

                // Speed
                Games.Leaderboards.loadCurrentPlayerLeaderboardScore(mGoogleApiClient,
                        getString(R.string.leaderboard_fastest_ceo),
                        LeaderboardVariant.TIME_SPAN_ALL_TIME,
                        LeaderboardVariant.COLLECTION_PUBLIC).setResultCallback(
                        new ResultCallback<Leaderboards.LoadPlayerScoreResult>() {

                            @Override
                            public void onResult(Leaderboards.LoadPlayerScoreResult arg0) {
                                LeaderboardScore c = arg0.getScore();
                                long old_time;
                                if(c != null) {
                                    old_time = c.getRawScore();
                                    if(time < old_time) {
                                        Games.Leaderboards.submitScore(mGoogleApiClient, getResources().getString(R.string.leaderboard_fastest_ceo), time);
                                    }
                                }
                                else {
                                    Games.Leaderboards.submitScore(mGoogleApiClient, getResources().getString(R.string.leaderboard_fastest_ceo), time);
                                }
                            }
                        });
            } else if (size == getResources().getInteger(R.integer.size_president)) {
                Games.Achievements.unlock(mGoogleApiClient, getResources().getString(R.string.achievement_trumped_the_trump));
                Games.Achievements.increment(mGoogleApiClient, getResources().getString(R.string.achievement_trumped_the_trump_v_2), 1);
                Games.Achievements.increment(mGoogleApiClient, getResources().getString(R.string.achievement_trumped_the_trump_v_3), 1);

                if(time <= 90000)
                    Games.Achievements.unlock(mGoogleApiClient, getResources().getString(R.string.achievement_end_all_trump_all));


                Games.Leaderboards.loadCurrentPlayerLeaderboardScore(mGoogleApiClient,
                        getString(R.string.leaderboard_mr__president_wins),
                        LeaderboardVariant.TIME_SPAN_ALL_TIME,
                        LeaderboardVariant.COLLECTION_PUBLIC).setResultCallback(
                        new ResultCallback<Leaderboards.LoadPlayerScoreResult>() {

                            @Override
                            public void onResult(Leaderboards.LoadPlayerScoreResult arg0) {
                                LeaderboardScore c = arg0.getScore();
                                int wins;
                                if (c != null)
                                    wins = (int) c.getRawScore();
                                else
                                    wins = 0;
                                Games.Leaderboards.submitScore(mGoogleApiClient, getResources().getString(R.string.leaderboard_mr__president_wins), wins + 1);
                            }
                        });

                // Speed
                Games.Leaderboards.loadCurrentPlayerLeaderboardScore(mGoogleApiClient,
                        getString(R.string.leaderboard_fastest_president),
                        LeaderboardVariant.TIME_SPAN_ALL_TIME,
                        LeaderboardVariant.COLLECTION_PUBLIC).setResultCallback(
                        new ResultCallback<Leaderboards.LoadPlayerScoreResult>() {

                            @Override
                            public void onResult(Leaderboards.LoadPlayerScoreResult arg0) {
                                LeaderboardScore c = arg0.getScore();
                                long old_time;
                                if (c != null) {
                                    old_time = c.getRawScore();
                                    if (time < old_time) {
                                        Games.Leaderboards.submitScore(mGoogleApiClient, getResources().getString(R.string.leaderboard_fastest_president), time);
                                    }
                                } else {
                                    Games.Leaderboards.submitScore(mGoogleApiClient, getResources().getString(R.string.leaderboard_fastest_president), time);
                                }
                            }
                        });
            }
        }
    }
}
