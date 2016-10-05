package com.brandonferrell.trumpsweeper.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.brandonferrell.trumpsweeper.R;
import com.brandonferrell.trumpsweeper.models.SweeperImageButton;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Brandon on 2/27/2016.
 */
public class SweeperImageButtonGridFragment extends Fragment implements Cloneable {
    private static final String SIZE = "SIZE";
    private static final String INDEX = "INDEX";
    public SweeperImageButton cells[][];
    private int index;
    private int size;
    private int flags;
    public LinearLayout rows[];
    public RelativeLayout gridLayout;
    boolean active;
    boolean moveMade;

    public void setMoveMade(boolean moveMade) { this.moveMade = moveMade; }

    public int getIndex() {
        return index;
    }

    public int getSize() {
        return size;
    }

    public int getFlags() {
        return flags;
    }

    public boolean addFlag() {
        if(flags < getMineCount(size)) {
            flags++;
            return true;
        }

        return false;
    }

    public void removeFlag() {
        if(flags - 1 >= 0)
            flags--;
    }

    public void newGrid(int n, boolean enabled) {
        size = n;
        cells = new SweeperImageButton[size][size];
        for(int i = 0; i < size; i++) {
            for(int j = 0; j < size; j++) {
                // Create a new button at the i,j coordinates
                cells[i][j] = new SweeperImageButton(getActivity(), this, i, j);
            }
        }

        setEnabled(enabled);

        setUpGame();
    }

    /**
     * Turn buttons on or off
     * @param value
     */
    public void setEnabled(boolean value) {
        for(int i = 0; i < size; i++) {
            for(int j = 0; j < size; j++) {
                cells[i][j].setEnabled(value);
            }
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        size = getArguments().getInt(SIZE);
        //index = getArguments().getInt(INDEX);

        Fragment fragment = getFragmentManager().findFragmentById(R.id.fragment_bottom_containter);
        if(fragment instanceof GameToolbarFragment)
            newGrid(size, true);
        else
            newGrid(size, false);
    }

    public static final SweeperImageButtonGridFragment newInstance(int s) {
        SweeperImageButtonGridFragment fragment = new SweeperImageButtonGridFragment();
        Bundle bundle = new Bundle(1);
        bundle.putInt(SIZE, s);
        fragment.setArguments(bundle);

        return fragment ;
    }



    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if(getFragmentManager().findFragmentById(R.id.fragment_game_end_containter) != null)
            getFragmentManager().beginTransaction().remove(getFragmentManager().findFragmentById(R.id.fragment_game_end_containter)).commit();

        RelativeLayout srl = new RelativeLayout(getActivity());
        final LinearLayout ll = new LinearLayout(getActivity());
        gridLayout = srl;
        srl.addView(ll);

        ll.setOrientation(LinearLayout.VERTICAL);
        rows = new LinearLayout[size];

        LinearLayout.LayoutParams LLParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        LLParams.weight = 1;
        LLParams.gravity = Gravity.CENTER;

        for(int i = 0; i < size; i++) {
            rows[i] = new LinearLayout(getActivity());
            rows[i].setOrientation(LinearLayout.HORIZONTAL);
            rows[i].setLayoutParams(LLParams);

            for(int j = 0; j < size; j++) {
                rows[i].addView(cells[i][j]);
                cells[i][j].setLayoutParams(LLParams);
                cells[i][j].setSoundEffectsEnabled(false);
                cells[i][j].setLongClickable(true);
            }

            ll.addView(rows[i]);
        }

        ll.setVisibility(View.INVISIBLE);



        rows[0].post(new Runnable() {
                         @Override
                         public void run() {
                 int width = cells[0][0].getWidth();
                 rows[0].getLayoutParams().height = width;
                 ll.setVisibility(View.VISIBLE);
                 rows[0].requestLayout();
             }
         }
        );

        return srl;
    }

    public void setUpGame() {
        int mineCount = getMineCount(size);
        Random r = new Random();
        int xRand;
        int yRand;
        boolean spaceIsMine;

        // Set up mines
        for(int k = 0; k < mineCount; k++) {
            spaceIsMine = true;
            do {
                // Get random x and y
                xRand = r.nextInt(size);
                yRand = r.nextInt(size);

                if(!cells[xRand][yRand].isMine)
                    spaceIsMine = false;
            } while(spaceIsMine);

            cells[xRand][yRand].isMine = true;
        }

        // Set neighbor counts
        this.setBtnMineNeighbors();

        setMoveMade(false);

        // Reset Highs list
        GameEndOverlay.newHighs = new ArrayList<>();
        GameEndOverlay.highsErrorCount++;
        GameEndOverlay.leaderboardsCompleted = 0;
    }

    public void setBtnMineNeighbors() {
        for(int i = 0; i < size; i++) {
            for(int j = 0; j < size; j++) {
                // Variable to hold the number of neighbors
                int neighborCount = 0;

                // Top left
                if(cells[i][j].x - 1 >= 0 && cells[i][j].y - 1 >= 0 && cells[cells[i][j].x - 1][cells[i][j].y - 1].isMine)
                    neighborCount++;

                // Top
                if(cells[i][j].y - 1 >= 0 && cells[cells[i][j].x][cells[i][j].y - 1].isMine)
                    neighborCount++;

                // Top right
                if(cells[i][j].x + 1 < size && cells[i][j].y - 1 >= 0 && cells[cells[i][j].x + 1][cells[i][j].y - 1].isMine)
                    neighborCount++;

                // Right
                if(cells[i][j].x + 1 < size && cells[cells[i][j].x + 1][cells[i][j].y].isMine)
                    neighborCount++;

                // Bottom right
                if(cells[i][j].x + 1 < size && cells[i][j].y + 1 < size && cells[cells[i][j].x + 1][cells[i][j].y + 1].isMine)
                    neighborCount++;

                // Bottom
                if(cells[i][j].y + 1 < size && cells[cells[i][j].x][cells[i][j].y + 1].isMine)
                    neighborCount++;

                // Bottom left
                if(cells[i][j].x - 1 >= 0 && cells[i][j].y + 1 < size && cells[cells[i][j].x - 1][cells[i][j].y + 1].isMine)
                    neighborCount++;

                // Left
                if(cells[i][j].x - 1 >= 0 && cells[cells[i][j].x - 1][cells[i][j].y].isMine)
                    neighborCount++;

                // Set the number of neighbors
                cells[i][j].mine_neighbors = neighborCount;
            }
        }
    }

    public int getMineCount(int n) {
        return (int) (4.7 * Math.pow(1.19, n) - 8);
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public boolean moveMade() {
        if(!moveMade) {
            moveMade = true;
            return true;
        }

        return false;
    }

    public void addRandomMine() {
        Random r = new Random();
        int xRand;
        int yRand;
        boolean spaceIsMine;

        // Set up mines
        for(int k = 0; k < 1; k++) {
            spaceIsMine = true;
            do {
                // Get random x and y
                xRand = r.nextInt(size);
                yRand = r.nextInt(size);

                if(!cells[xRand][yRand].isMine)
                    spaceIsMine = false;
            } while(spaceIsMine);

            cells[xRand][yRand].isMine = true;
        }

        setBtnMineNeighbors();
    }


}
