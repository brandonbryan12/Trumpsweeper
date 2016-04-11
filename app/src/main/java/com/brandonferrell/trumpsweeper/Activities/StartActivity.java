package com.brandonferrell.trumpsweeper.Activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.brandonferrell.trumpsweeper.ProfileDialogFragment;
import com.brandonferrell.trumpsweeper.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.Player;

public class StartActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ProfileDialogFragment.ProfileInterface {

    ImageButton btnProfile, btnSettings, btnStart, btnLeaders;
    Player mPlayer;
    static MediaPlayer themeMediaPlayer;
    boolean stopPlayer;
    SharedPreferences prefs;

    private static final String TAG = "Trumpsweeper";

    // Client used to interact with Google APIs.
    private GoogleApiClient mGoogleApiClient;

    // Has the user clicked the sign-in button?
    private boolean mSignInClicked = false;

    // Are we currently resolving a connection failure?
    private boolean mResolvingConnectionFailure = false;

    // Set to true to automatically start the sign in flow when the Activity starts.
    // Set to false to require the user to click the button in order to sign in.
    private boolean mAutoStartSignInFlow = true;

    // Request code used to invoke sign in user interactions.
    private static final int RC_SIGN_IN = 9001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        themeMediaPlayer = MediaPlayer.create(this, R.raw.theme_music);
        themeMediaPlayer.setLooping(true);
        themeMediaPlayer.setVolume(.8f, .8f);

        if(prefs.getBoolean("music_preference", true))
            themeMediaPlayer.start();

        stopPlayer = true;

        // Hide action bar
        if(getSupportActionBar() != null)
            getSupportActionBar().hide();

        btnStart = (ImageButton) findViewById(R.id.home_start_button);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vibrate();
                Bundle b = new Bundle();
                b.putParcelable("PLAYER", mPlayer);
                stopPlayer = false;
                Intent intent = new Intent(v.getContext(), GameActivity.class);
                intent.putExtras(b);
                startActivity(intent);
            }
        });

        btnLeaders = (ImageButton) findViewById(R.id.home_leaderboards_button);
        btnLeaders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vibrate();
                if(mPlayer == null) {
                    ProfileDialogFragment profileDialogFragment = new ProfileDialogFragment();
                    profileDialogFragment.setPlayer(mPlayer);
                    profileDialogFragment.show(getSupportFragmentManager(), "Profile");
                }
                else
                    startActivityForResult(Games.Leaderboards.getAllLeaderboardsIntent(mGoogleApiClient), 1);
            }
        });

        btnSettings = (ImageButton) findViewById(R.id.home_settings_button);
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vibrate();
                stopPlayer = false;
                Intent intent = new Intent(v.getContext(), SettingsActivity.class);
                startActivity(intent);
            }
        });

        btnProfile = (ImageButton) findViewById(R.id.home_profile_button);
        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vibrate();
                ProfileDialogFragment profileDialogFragment = new ProfileDialogFragment();
                profileDialogFragment.setPlayer(mPlayer);
                profileDialogFragment.show(getSupportFragmentManager(), "Profile");
            }
        });

        // Create the Google Api Client with access to Plus and Games
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();

        mSignInClicked = true;
        mGoogleApiClient.connect();
    }

    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();

        if(themeMediaPlayer != null && prefs.getBoolean("music_preference", true))
            themeMediaPlayer.start();
    }

    protected void onResume() {
        super.onResume();

        stopPlayer = true;

        if(themeMediaPlayer != null && prefs.getBoolean("music_preference", true) && !themeMediaPlayer.isPlaying())
            themeMediaPlayer.start();
    }

    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }

        if(stopPlayer && themeMediaPlayer != null && themeMediaPlayer.isPlaying()) {
            themeMediaPlayer.pause();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected() called. Sign in successful!");
        mPlayer = Games.Players.getCurrentPlayer(mGoogleApiClient);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended() called. Trying to reconnect.");
        mPlayer = null;
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed() called, result: " + connectionResult);

        if (mResolvingConnectionFailure) {
            Log.d(TAG, "onConnectionFailed() ignoring connection failure; already resolving.");
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
            Log.d(TAG, "onActivityResult with requestCode == RC_SIGN_IN, responseCode="
                    + responseCode + ", intent=" + intent);
            mSignInClicked = false;
            mResolvingConnectionFailure = false;
            if (responseCode == RESULT_OK) {
                mGoogleApiClient.connect();
            } /*else {
                BaseGameUtils.showActivityResultError(this,requestCode,responseCode, R.string.signin_other_error);
            }*/
        }
    }

    @Override
    public void dialogSignIn() {
        mSignInClicked = true;
        mGoogleApiClient.connect();
    }

    @Override
    public void dialogSignOut() {
        if (mGoogleApiClient.isConnected()) {
            Games.signOut(mGoogleApiClient);
            mGoogleApiClient.disconnect();
        }

        mPlayer = null;

        Toast.makeText(StartActivity.this, "Signed out of Google Play Games", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void dialogAchievements() {
        startActivityForResult(Games.Achievements.getAchievementsIntent(mGoogleApiClient),
                1);
    }

    public void vibrate() {
        Vibrator v = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(25);
    }

}
