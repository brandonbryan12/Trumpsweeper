package com.brandonferrell.trumpsweeper;

import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.brandonferrell.trumpsweeper.Activities.StartActivity;
import com.google.android.gms.common.images.ImageManager;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.Player;

/**
 * Created by Brandon on 4/2/2016.
 */
public class ProfileDialogFragment extends DialogFragment {
    private static final String PLAYER = "PLAYER";

    Player mPlayer;
    ProfileInterface profileInterface;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView;

        profileInterface = (ProfileInterface) getActivity();

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
