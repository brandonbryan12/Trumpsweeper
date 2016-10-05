package com.brandonferrell.trumpsweeper.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageButton;

import com.brandonferrell.trumpsweeper.R;

import java.util.List;

/**
 * Created by Brandon on 5/4/2016.
 */
public class CreditsActivity extends Activity {

    MediaPlayer greenAcresMediaPlayer;
    SharedPreferences prefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credits);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        if(prefs.getBoolean("volume_preference", true)) {
            greenAcresMediaPlayer = MediaPlayer.create(this, R.raw.green_acres);
            greenAcresMediaPlayer.setVolume(.8f, .8f);
            greenAcresMediaPlayer.start();
        }

        final Context context = this;

        ((ImageButton) findViewById(R.id.instagram)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vibrate();

                Uri uri = Uri.parse("http://instagram.com/_u/masterswzord");
                Intent insta = new Intent(Intent.ACTION_VIEW, uri);
                insta.setPackage("com.instagram.android");

                if (isIntentAvailable(context, insta)){
                    startActivity(insta);
                } else{
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://instagram.com/masterswzord")));
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(greenAcresMediaPlayer != null && greenAcresMediaPlayer.isPlaying())
            greenAcresMediaPlayer.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(greenAcresMediaPlayer != null && PreferenceManager.getDefaultSharedPreferences(this).getBoolean("music_preference", true))
            greenAcresMediaPlayer.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(greenAcresMediaPlayer == null)
            return;

        greenAcresMediaPlayer.stop();
        greenAcresMediaPlayer.release();
    }

    private boolean isIntentAvailable(Context ctx, Intent intent) {
        final PackageManager packageManager = ctx.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    public void vibrate() {
        Vibrator v = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(25);
    }
}
