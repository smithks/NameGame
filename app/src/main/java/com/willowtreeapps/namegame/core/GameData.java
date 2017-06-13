package com.willowtreeapps.namegame.core;

import com.willowtreeapps.namegame.network.api.model.Person;

import java.util.List;

/**
 * The GameData class contains state information about the current game of Name Game.
 * @author Keegan Smith
 * @since 5/24/2017
 */
public class GameData {
    private List<Person> choices;
    private Person goal;
    private String currentMode;
    private int currentScore;

    public void setChoices(List<Person> choices) {
        this.choices = choices;
    }

    public List<Person> getChoices() {
        return choices;
    }

    public boolean isCorrectSelection(Person selected){
        return selected.equals(goal);
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

    public void incrementScore(){
        currentScore ++;
    }

    public String getGoalNameString(){
        return goal.getFirstName() + " " + goal.getLastName();
    }
}
