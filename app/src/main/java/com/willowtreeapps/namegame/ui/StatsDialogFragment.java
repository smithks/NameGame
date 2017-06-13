package com.willowtreeapps.namegame.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.willowtreeapps.namegame.R;

/**
 * Dialog fragment that displays the users stats. Allows the user to clear their game stats.
 * @author Keegan Smith
 */
public class StatsDialogFragment extends DialogFragment {

    TextView totalCorrect;
    TextView totalIncorrect;
    TextView highScore;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.name_game_dialog_stats,container,false);

        totalCorrect = (TextView) rootView.findViewById(R.id.text_view_total_correct);
        totalIncorrect = (TextView) rootView.findViewById(R.id.text_view_total_incorrect);
        highScore = (TextView) rootView.findViewById(R.id.text_view_high_score);
        Button doneButton = (Button) rootView.findViewById(R.id.button_stats_done);
        Button clearButton = (Button) rootView.findViewById(R.id.button_clear_all);

        setupStats();
        setListeners(doneButton,clearButton);

        return rootView;
    }

    private void setupStats(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        int correct = preferences.getInt(getResources().getString(R.string.pref_key_correct),0);
        int incorrect = preferences.getInt(getResources().getString(R.string.pref_key_incorrect),0);
        int score = preferences.getInt(getResources().getString(R.string.pref_key_high_score),0);

        totalCorrect.setText(String.valueOf(correct));
        totalIncorrect.setText(String.valueOf(incorrect));
        highScore.setText(String.valueOf(score));
    }

    private void setListeners(final Button doneButton,final Button clearButton){
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearPreferences();
            }
        });
    }

    /**
     * Clears the user statistics that are saved in shared preferences, then updates the displayed stats.
     */
    private void clearPreferences(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        sharedPreferences.edit()
                .putInt(getResources().getString(R.string.pref_key_correct),0)
                .putInt(getResources().getString(R.string.pref_key_incorrect),0)
                .putInt(getResources().getString(R.string.pref_key_high_score),0)
                .apply();
        setupStats();
    }
}
