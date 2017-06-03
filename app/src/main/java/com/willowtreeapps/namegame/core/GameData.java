package com.willowtreeapps.namegame.core;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import com.willowtreeapps.namegame.R;
import com.willowtreeapps.namegame.network.api.model.Person;

import java.util.List;

/**
 * @author Keegan Smith
 * @since 5/24/2017
 */

public class GameData  {
    private List<Person> choices;
    private Person goal;
    private String currentMode;
    private int currentScore;
    private Context context;

    public GameData(Context context, List<Person> choices, Person goal, String currentMode, int currentScore){
        this.context = context;
        this.choices = choices;
        this.goal = goal;
        this.currentMode = currentMode;
        this.currentScore = currentScore;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public GameData(){}

    public void setChoices(List<Person> choices) {
        this.choices = choices;
    }

    public List<Person> getChoices() {
        return choices;
    }

    public void setGoal(Person goal) {
        this.goal = goal;
    }

    public Person getGoal() {
        return goal;
    }

    public String getCurrentMode(){
        return currentMode;
    }

    public void setCurrentMode(String currentMode) {
        this.currentMode = currentMode;
    }

    public int getCurrentScore() {
        return currentScore;
    }

    public void setCurrentScore(int currentScore) {
        this.currentScore = currentScore;
    }

    public String getUserScoreString(){
        String score = context.getResources().getString(R.string.string_score);
        score = score + " " + String.valueOf(currentScore);
        int scoreLength = score.length();
        SpannableStringBuilder builder = new SpannableStringBuilder(score);
        builder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context,R.color.colorAccent)),scoreLength,builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return builder.toString();
    }

    public String getGoalQuestionString(){
        String question = context.getResources().getString(R.string.string_question);
        int startIndex = question.length();

        question = question + " " +goal.getFirstName() + " " +goal.getLastName();
        SpannableStringBuilder builder = new SpannableStringBuilder(question);

        builder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context,R.color.colorAccent)),startIndex,builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append("?");
        return builder.toString();
    }
}
