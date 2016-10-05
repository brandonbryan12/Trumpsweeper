package com.brandonferrell.trumpsweeper.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.brandonferrell.trumpsweeper.fragments.ProfileDialogFragment;
import com.brandonferrell.trumpsweeper.R;
import com.brandonferrell.trumpsweeper.fragments.ShopDialogFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.Player;

import hotchemi.android.rate.AppRate;
import hotchemi.android.rate.OnClickButtonListener;

public class StartActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ProfileDialogFragment.ProfileInterface, BillingProcessor.IBillingHandler {

    ImageButton btnProfile, btnSettings, btnStart, btnLeaders, btnCoins;
    Player mPlayer;
    static MediaPlayer themeMediaPlayer;
    boolean stopPlayer;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    public static BillingProcessor bp;
    MediaPlayer.OnCompletionListener onCompletionListener;

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

        AppRate.with(this)
                .setInstallDays(1) // default 10, 0 means install day.
                .setLaunchTimes(3) // default 10
                .setRemindInterval(2) // default 1
                .setShowLaterButton(true) // default true
                .setDebug(false) // default false
                .setOnClickButtonListener(new OnClickButtonListener() { // callback listener.
                    @Override
                    public void onClickButton(int which) {
                        Log.d(StartActivity.class.getName(), Integer.toString(which));
                    }
                })
                .monitor();

        // Show a dialog if meets conditions
        AppRate.showRateDialogIfMeetsConditions(this);

        bp = new BillingProcessor(this, getResources().getString(R.string.billing_license), this);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        editor = prefs.edit();

        themeMediaPlayer = MediaPlayer.create(this, R.raw.theme_music);
        themeMediaPlayer.setLooping(false);
        themeMediaPlayer.setVolume(.8f, .8f);

        onCompletionListener = new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if(!themeMediaPlayer.isPlaying()) {
                    themeMediaPlayer.seekTo(0);
                    themeMediaPlayer.start();
                }
            }
        };

        themeMediaPlayer.setOnCompletionListener(onCompletionListener);

        if(prefs.getBoolean("music_preference", true))
            themeMediaPlayer.start();

        stopPlayer = true;

        // Hide action bar
        if(getSupportActionBar() != null)
            getSupportActionBar().hide();

        View.OnTouchListener darkenOnTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        ((ImageButton)v).setColorFilter(Color.parseColor("#AA000000"));
                        break;
                    case MotionEvent.ACTION_UP:
                        ((ImageButton)v).setColorFilter(Color.parseColor("#00000000"));
                        break;
                }
                return false;
            }
        };

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
        btnStart.setOnTouchListener(darkenOnTouchListener);

        btnCoins = (ImageButton) findViewById(R.id.home_coins_button);
        btnCoins.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vibrate();
                ShopDialogFragment shopDialogFragment = new ShopDialogFragment();
                //shopDialogFragment.setPlayer(mPlayer);
                shopDialogFragment.show(getSupportFragmentManager(), "Profile");
            }
        });
        btnCoins.setOnTouchListener(darkenOnTouchListener);

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
        btnLeaders.setOnTouchListener(darkenOnTouchListener);

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
        btnSettings.setOnTouchListener(darkenOnTouchListener);

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
        btnProfile.setOnTouchListener(darkenOnTouchListener);

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

        if(themeMediaPlayer != null && prefs.getBoolean("music_preference", true)) {
            themeMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                }
            });
        }
    }

    protected void onResume() {
        super.onResume();

        stopPlayer = true;

        if(themeMediaPlayer != null && prefs.getBoolean("music_preference", true) && !themeMediaPlayer.isPlaying()) {
            themeMediaPlayer.start();
        }
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
        if (!bp.handleActivityResult(requestCode, responseCode, intent))
            super.onActivityResult(requestCode, responseCode, intent);
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

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {
        /*
         * Called when BillingProcessor was initialized and it's ready to purchase
         */
        //Log.d("asdf", productId);
        //productId = "no_ads";
        switch (productId) {
            case "no_ads":
                editor.putBoolean("no_ads", true);
                editor.commit();
                break;
            case "china_variable":
                editor.putBoolean("china_variable", true);
                editor.commit();
                break;
        }

        Toast.makeText(this, "Thank you for your purchase!", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPurchaseHistoryRestored() {
        /*
         * Called when requested PRODUCT ID was successfully purchased
         */

    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
        if(error != null)
            Log.d("asdf", error.getMessage());

    }

    @Override
    public void onBillingInitialized() {
        /*
         * Called when purchase history was restored and the list of all owned PRODUCT ID's
         * was loaded from Google Play
         */
    }

    @Override
    protected void onDestroy() {
        if (bp != null)
            bp.release();

        super.onDestroy();
    }
}
