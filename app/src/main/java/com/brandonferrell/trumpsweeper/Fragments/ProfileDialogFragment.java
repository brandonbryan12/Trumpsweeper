package com.brandonferrell.trumpsweeper.fragments;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.brandonferrell.trumpsweeper.activities.StartActivity;
import com.brandonferrell.trumpsweeper.R;
import com.google.android.gms.common.images.ImageManager;
import com.google.android.gms.games.Player;

/**
 * Created by Brandon on 4/2/2016.
 */
public class ProfileDialogFragment extends DialogFragment {
    private static final String PLAYER = "PLAYER";

    Player mPlayer;
    ProfileInterface profileInterface;
    Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView;

        profileInterface = (ProfileInterface) getActivity();

        context = getContext();

        if(mPlayer == null) {
            rootView = inflater.inflate(R.layout.fragment_profile_dialog_logged_out, container, false);
            setUpNullPlayer(rootView);
        }
        else {
            rootView = inflater.inflate(R.layout.fragment_profile_dialog_logged_in, container, false);
            setUpExistingPlayer(rootView);
        }

        // Remove Title Bar
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        return rootView;

    }

    private void setUpNullPlayer(View v) {
        v.findViewById(R.id.logged_out_sign_in).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((StartActivity) getActivity()).vibrate();
                profileInterface.dialogSignIn();
                getDialog().dismiss();
            }
        });
    }

    private void setUpExistingPlayer(View v) {
        v.findViewById(R.id.logged_in_sign_out).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((StartActivity) getActivity()).vibrate();
                profileInterface.dialogSignOut();
                getDialog().dismiss();
            }
        });

        v.findViewById(R.id.achievements_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((StartActivity) getActivity()).vibrate();
                profileInterface.dialogAchievements();
            }
        });

        v.findViewById(R.id.rate_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((StartActivity) getActivity()).vibrate();

                Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                // To count with Play market backstack, After pressing back button,
                // to taken back to our application, we need to add following flags to intent.
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                        Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + context.getPackageName())));
                }
            }
        });

        ImageView profileImage = (ImageView) v.findViewById(R.id.profile_image);
        ImageManager im = ImageManager.create(getActivity());
        im.loadImage(profileImage, mPlayer.getIconImageUri());

        ((TextView) v.findViewById(R.id.profile_name)).setText(mPlayer.getDisplayName());
    }

    public void setPlayer(Player player) {
        mPlayer = player;
    }

    public interface ProfileInterface {
        void dialogSignIn();
        void dialogSignOut();
        void dialogAchievements();
    }

}
