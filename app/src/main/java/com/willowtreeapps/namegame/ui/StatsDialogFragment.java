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
 * Dialog fragment that displays the users stats. Contains one button that dismisses the dialog.
 * @author Keegan Smith
 */
public class StatsDialogFragment extends DialogFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.name_game_dialog_stats,container,false);

        TextView totalCorrect = (TextView) rootView.findViewById(R.id.text_view_total_correct);
        TextView totalIncorrect = (TextView) rootView.findViewById(R.id.text_view_total_incorrect);
        TextView highScore = (TextView) rootView.findViewById(R.id.text_view_high_score);
        Button doneButton = (Button) rootView.findViewById(R.id.button_stats_done);

        setupStats(totalCorrect,totalIncorrect,highScore);
        setListener(doneButton);

        return rootView;
    }

    private void setupStats(TextView totalCorrectView, TextView totalIncorrectView, TextView highScoreView){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        int correct = preferences.getInt(getResources().getString(R.string.pref_key_correct),0);
        int incorrect = preferences.getInt(getResources().getString(R.string.pref_key_incorrect),0);
        int highScore = preferences.getInt(getResources().getString(R.string.pref_key_high_score),0);

        totalCorrectView.setText(String.valueOf(correct));
        totalIncorrectView.setText(String.valueOf(incorrect));
        highScoreView.setText(String.valueOf(highScore));
    }

    private void setListener(final Button doneButton){
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });
    }
}
