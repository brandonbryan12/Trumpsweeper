package com.brandonferrell.trumpsweeper.fragments;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.brandonferrell.trumpsweeper.activities.GameActivity;
import com.brandonferrell.trumpsweeper.R;
import com.brandonferrell.trumpsweeper.models.newHigh;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.google.android.gms.games.leaderboard.Leaderboards;

import java.util.ArrayList;


public class GameEndOverlay extends Fragment {
    private static final String WIN = "WIN";
    private static final String SIZE = "SIZE";
    private static final String TIME = "TIME";
    private static final String TOUPEES = "TOUPEES";

    private boolean win;
    private int size;
    private double time;
    private int toupees;
    private int ranksComputed;

    private Player mPlayer;
    private GoogleApiClient mGoogleApiClient;

    private MediaPlayer mp;
    private ImageView trumpHead;
    private ImageView youStatus, loadHead;
    private ObjectAnimator animation;
    private OnClipPlayedListener onClipPlayedListener;
    private AchieveInterface achieveInterface;
    private LinearLayout statsLayout, highHolder;
    private GameEndOverlay.ProfileInterface profileInterface;
    private TextView endText, loadPrompt;
    private HorizontalScrollView highScroll;

    public static ArrayList<newHigh> newHighs;
    public static int highsErrorCount;
    public static int leaderboardsCompleted;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        win = getArguments().getBoolean(WIN);
        size = getArguments().getInt(SIZE);
        time = getArguments().getDouble(TIME);
        toupees = getArguments().getInt(TOUPEES);

        highsErrorCount = 0;
        leaderboardsCompleted = 0;
        GameActivity.gameComplete = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_game_end_overlay, container, false);

        mPlayer = ((GameActivity) getActivity()).getPlayer();
        mGoogleApiClient = ((GameActivity) getActivity()).getGoogleApiClient();

        onClipPlayedListener = (OnClipPlayedListener) getActivity();
        achieveInterface = (AchieveInterface) getActivity();

        youStatus = (ImageView) v.findViewById(R.id.you_status);

        if(newHighs == null)
            newHighs = new ArrayList<>();

        if(win)
            youStatus.setImageDrawable(getResources().getDrawable(R.drawable.you_win));
        else
            youStatus.setImageDrawable(getResources().getDrawable(R.drawable.you_lose));

        statsLayout = (LinearLayout) v.findViewById(R.id.stats_layout);
        endText = (TextView) v.findViewById(R.id.end_game_text);
        //endText.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/PressStart2P.ttf"));

        if(mPlayer == null) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) statsLayout.getLayoutParams();
            params.topMargin = 0;

            endText.setText(R.string.no_player_game_end);
            Button signIn = (Button) v.findViewById(R.id.end_game_sign_in);
            signIn.setVisibility(View.VISIBLE);
            profileInterface = (ProfileInterface) getActivity();
            signIn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    profileInterface.endGameSignIn();
                }
            });

            ObjectAnimator animationStatsAlpha = ObjectAnimator.ofFloat(statsLayout, "alpha", 0, 1);
            animationStatsAlpha.setDuration(1500);
            animationStatsAlpha.start();
        }
        else if(!win) {
            endText.setText(getHeader());
            ObjectAnimator animationStatsAlpha = ObjectAnimator.ofFloat(statsLayout, "alpha", 0, 1);
            animationStatsAlpha.setDuration(1500);
            animationStatsAlpha.start();
        }
        else if(mGoogleApiClient.isConnected()){
            // Add stats
            // Check/add achievements
            if(newHighs.isEmpty())
                achieveInterface.handleAchievementsLeaders(win, size, time, toupees);

            highHolder = (LinearLayout) v.findViewById(R.id.highHolder);
            highScroll = (HorizontalScrollView) v.findViewById(R.id.highScroll);
            loadHead = (ImageView) v.findViewById(R.id.loadHead);
            loadHead.setVisibility(View.VISIBLE);
            loadPrompt = (TextView) v.findViewById(R.id.loadPrompt);
            loadPrompt.setVisibility(View.VISIBLE);

            Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate_around_center_point);
            loadHead.startAnimation(animation);
        }

        trumpHead = (ImageView) v.findViewById(R.id.trump_head);
        Drawable d = ContextCompat.getDrawable(getActivity(), getTrumpImage(this.win));
        trumpHead.setImageDrawable(d);

        trumpHead.post(new Runnable() {
                           @Override
                           public void run() {
                               trumpHead.setVisibility(View.VISIBLE);

                               animation = ObjectAnimator.ofFloat(trumpHead, "translationX", -trumpHead.getWidth(), trumpHead.getWidth());
                               animation.setDuration(2000);
                               animation.setInterpolator(new DecelerateInterpolator());
                               animation.start();

                               AnimatorSet animatorSet1;
                               AnimatorSet animatorSet2;

                               ObjectAnimator animationIn = ObjectAnimator.ofFloat(trumpHead, "translationX", -trumpHead.getWidth(), 0);
                               animationIn.setDuration(1500);

                               ObjectAnimator animationWait = ObjectAnimator.ofFloat(trumpHead, "translationX", 0, trumpHead.getWidth());
                               animationWait.setDuration(1000);

                               ObjectAnimator animationOut = ObjectAnimator.ofFloat(trumpHead, "alpha", 1, 1);
                               animationOut.setDuration(1500);

                               animatorSet1 = new AnimatorSet();
                               animatorSet1.play(animationIn).before(animationWait);
                               animatorSet1.play(animationOut).after(animationWait);
                               //animatorSet1.play(animationStatsAlpha).with(animationWait);
                               animatorSet1.start();

                               ObjectAnimator animationFall = ObjectAnimator.ofFloat(youStatus, "translationY", 0, 150);
                               animationFall.setDuration(3000);

                               ObjectAnimator animationRise = ObjectAnimator.ofFloat(youStatus, "translationY", 150, 100);
                               animationFall.setDuration(500);

                               ObjectAnimator animationAlpha = ObjectAnimator.ofFloat(youStatus, "alpha", 0, 1);
                               animationAlpha.setDuration(500);

                               animatorSet2 = new AnimatorSet();
                               animatorSet2.play(animationFall).with(animationAlpha);
                               animatorSet2.play(animationRise).after(animationFall);
                               animatorSet2.start();

                           }
                       }
        );

        // Get trump clip id that is not the last one played
        int trumpId = 0;
        do {
            trumpId = getTrumpClipId(this.win);
        } while(trumpId == ((GameActivity) getActivity()).getLastClipId());

        // Set the last one played to this one
        ((GameActivity) getActivity()).setLastClipId(trumpId);

        // Play clip
        onClipPlayedListener.playTrumpClip(trumpId);


        return v;
    }

    private int getTrumpClipId(boolean win) {
        int id = 0;
        int rand;

        rand = (int) (Math.random() * 6);

        if(win) {
            switch (rand) {
                case 0:
                    id = R.raw.win_not_nice_person;
                    break;
                case 1:
                    id = R.raw.win_special_guy;
                    break;
                case 2:
                    id = R.raw.win_catastrophe;
                    break;
                case 3:
                    id = R.raw.win_congratulations;
                    break;
                case 4:
                    id = R.raw.win_brilliant;
                    break;
                case 5:
                    id = R.raw.win_special_guy;
                    break;
            }
        } else {
            switch (rand) {
                case 0:
                    id = R.raw.lose_not_very_good;
                    break;
                case 1:
                    id = R.raw.lose_wall_10_feet_higher;
                    break;
                case 2:
                    id = R.raw.lose_we_have_losers;
                    break;
                case 3:
                    id = R.raw.lose_beaten_up_badly;
                    break;
                case 4:
                    id = R.raw.lose_congrats_fired;
                    break;
                case 5:
                    id = R.raw.lose_sit_down;
                    break;
            }
        }

        return id;
    }

    /**
     * Get random header from string-array resource "headers"
     * @return
     */
    private String getHeader() {
        int index = 0;

        String[] headers = getActivity().getResources().getStringArray(R.array.headers);

        index = (int) (Math.random() * headers.length);

        return headers[index];
    }

    private int getTrumpImage(boolean win) {
        int id = 0;
        int rand = (int) (Math.random() * 5);

        if(win) {
            switch (rand) {
                case 0:
                    id = R.drawable.win_trump_pointing;
                    break;
                case 1:
                    id = R.drawable.win_trump_red;
                    break;
                case 2:
                    id = R.drawable.win_trump_weird_face;
                    break;
                case 3:
                    id = R.drawable.win_trump_x;
                    break;
                case 4:
                    id = R.drawable.win_trump_yelling;
                    break;
            }
        } else {
            switch (rand) {
                case 0:
                    id = R.drawable.lose_trump_circle;
                    break;
                case 1:
                    id = R.drawable.lose_trump_smug;
                    break;
                case 2:
                    id = R.drawable.lose_trump_drake;
                    break;
                case 3:
                    id = R.drawable.lose_trump_trillary;
                    break;
                case 4:
                    id = R.drawable.lose_trump_dealwithit;
                    break;
            }
        }

        return id;
    }

    public void setHighs() {
        if(getActivity() == null)
            return;

        ranksComputed = 0;

        //Log.d("asdf", "highs last: " + highsErrorCount);

       // Log.d("asdf", "New Highs: " + newHighs.size());

        for(final newHigh highRaw : newHighs) {
            //Log.d("asdf", highRaw.getName());

            final newHigh high = highRaw;

            switch(high.getName()) {
                case "Trumps Toupee'd":
                    setRankFromLeaderboard(getString(R.string.leaderboard_trumps_toupeed), high, 0);
                    break;
                case "Apprentice Wins":
                    setRankFromLeaderboard(getString(R.string.leaderboard_apprentice_wins), high, 0);
                    break;
                case "Fastest Apprentice":
                    setRankFromLeaderboard(getString(R.string.leaderboard_fastest_apprentice), high, 0);
                    break;
                case "CEO Wins":
                    setRankFromLeaderboard(getString(R.string.leaderboard_ceo_wins), high, 0);
                    break;
                case "Fastest CEO":
                    setRankFromLeaderboard(getString(R.string.leaderboard_fastest_ceo), high, 0);
                    break;
                case "Mr. President Wins":
                    setRankFromLeaderboard(getString(R.string.leaderboard_mr__president_wins), high, 0);
                    break;
                case "Fastest President":
                    setRankFromLeaderboard(getString(R.string.leaderboard_fastest_president), high, 0);
                    break;
            }
        }
    }

    private void setRankFromLeaderboard(final String id, final newHigh high, final int trycount) {
        if(trycount > 3) {
            //Log.d("asdf", "error");
            highsErrorCount++;
            if(++ranksComputed >= newHighs.size())
                setSecondHighs();
            return;
        }
        Games.Leaderboards.loadCurrentPlayerLeaderboardScore(mGoogleApiClient,
                id, LeaderboardVariant.TIME_SPAN_ALL_TIME,
                LeaderboardVariant.COLLECTION_PUBLIC).setResultCallback(
                new ResultCallback<Leaderboards.LoadPlayerScoreResult>() {

                    @Override
                    public void onResult(Leaderboards.LoadPlayerScoreResult arg0) {
                        if(!isScoreResultValid(arg0)) {
                            //Log.d("asdf", "error " + high.getName());
                            setRankFromLeaderboard(id, high, trycount + 1);
                            return;
                        }
                        high.setRank(arg0.getScore().getRank());

                        Status status = arg0.getStatus();
                        int statusCode = status.getStatusCode();

                        if (statusCode == GamesStatusCodes.STATUS_NETWORK_ERROR_NO_DATA)
                            highsErrorCount++;

                        if(++ranksComputed >= newHighs.size())
                            setSecondHighs();
                    }
                });
    }

    public void setSecondHighs() {
        if(getActivity() == null)
            return;

        if(highsErrorCount > 0) {
            endText.setText("A network error occurred. Check your internet connection and try again.");

            ObjectAnimator animationHeadAlpha = ObjectAnimator.ofFloat(loadHead, "alpha", 1, 0);
            animationHeadAlpha.setDuration(1500);
            animationHeadAlpha.start();

            ObjectAnimator animationPromptAlpha = ObjectAnimator.ofFloat(loadPrompt, "alpha", 1, 0);
            animationPromptAlpha.setDuration(500);
            animationPromptAlpha.start();

            ObjectAnimator animationStatsAlpha = ObjectAnimator.ofFloat(statsLayout, "alpha", 0, 1);
            animationStatsAlpha.setDuration(1500);
            animationStatsAlpha.start();
        }
        else {
            endText.setText("New High Scores!");
            endText.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/PressStart2P.ttf"));

            for(newHigh high : newHighs) {
                /*if(high.getCount() > 0)
                    Log.d("count", "" + high.getName() + " " + high.getCount());
                else
                    Log.d("time", "" + high.getName() + " " + high.getTime());*/

                View v; // Creating an instance for View Object
                LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = inflater.inflate(R.layout.new_high, null);

                TextView highName = (TextView) v.findViewById(R.id.highName);
                TextView highCount = (TextView) v.findViewById(R.id.highCount);
                TextView highRank = (TextView) v.findViewById(R.id.highRank);

                //highName.setTypeface(font);
                //highCount.setTypeface(font);
                //highRank.setTypeface(font);

                highName.setText(high.getName());
                if(high.getCount() > 0)
                    highCount.setText(high.getCount() + "");
                else
                    highCount.setText(high.getTimeFormatted());
                highRank.setText("Rank #" + high.getRank());

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(20, 20, 20, 20);
                v.setLayoutParams(params);

                highHolder.addView(v);
            }

            highScroll.post(new Runnable() {
                @Override
                public void run() {
                    ObjectAnimator animationHeadAlpha = ObjectAnimator.ofFloat(loadHead, "alpha", 1, 0);
                    animationHeadAlpha.setDuration(1500);
                    animationHeadAlpha.start();

                    ObjectAnimator animationPromptAlpha = ObjectAnimator.ofFloat(loadPrompt, "alpha", 1, 0);
                    animationPromptAlpha.setDuration(500);
                    animationPromptAlpha.start();

                    ObjectAnimator animationStatsAlpha = ObjectAnimator.ofFloat(statsLayout, "alpha", 0, 1);
                    animationStatsAlpha.setDuration(1500);
                    animationStatsAlpha.start();

                    ObjectAnimator animator = ObjectAnimator.ofInt(highScroll, "scrollX", highScroll.getWidth());
                    animator.setDuration(3000);
                    animator.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            ObjectAnimator animator = ObjectAnimator.ofInt(highScroll, "scrollX", 0);
                            animator.setDuration(1000);
                            animator.start();
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });
                    animator.start();
                }
            });
        }
    }

    private boolean isScoreResultValid(final Leaderboards.LoadPlayerScoreResult scoreResult) {
        return scoreResult != null && GamesStatusCodes.STATUS_OK == scoreResult.getStatus().getStatusCode() && scoreResult.getScore() != null;
    }

    public void setPlayer(Player player) { mPlayer = player; }

    public interface OnClipPlayedListener {
        void playTrumpClip(int id);
    }

    public interface ProfileInterface {
        void endGameSignIn();
    }

    public interface AchieveInterface {
        void handleAchievementsLeaders(boolean win, int size, double time, int toupees);
    }

}
