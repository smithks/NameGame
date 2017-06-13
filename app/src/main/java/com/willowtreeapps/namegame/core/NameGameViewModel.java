package com.willowtreeapps.namegame.core;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.view.View;

import com.willowtreeapps.namegame.R;
import com.willowtreeapps.namegame.network.api.ProfilesRepository;
import com.willowtreeapps.namegame.network.api.model.Person;
import com.willowtreeapps.namegame.network.api.model.Profiles;

import java.util.List;

import javax.inject.Inject;

/**
 * The view model that is used by the Name Game. Provides access to 3 observable LiveData objects that
 * contain state information about the Name Game.
 * @author Keegan Smith
 * @since 5/24/2017
 */

public class NameGameViewModel extends AndroidViewModel implements ProfilesRepository.Listener {

    @Inject
    public ListRandomizer listRandomizer;
    @Inject
    public ProfilesRepository repository;

    private Context context;
    private Profiles profiles;
    private MutableLiveData<GameData> gameLiveData;
    private MutableLiveData<AnimationData> animationLiveData;
    private MutableLiveData<String> toastLiveData;
    private boolean initialized;

    public NameGameViewModel(Application application) {
        super(application);
        context = application;
    }

    /**
     * Initializes the fields of this viewmodel if necessary.
     * Called each time the livedata of this view model is to be observed.
     */
    public void initialize() {
        if (!initialized) {
            gameLiveData = new MutableLiveData<>();
            NameGameApplication.get(context).component().inject(this);
            if (profiles == null) {
                repository.register(this);
            }
            initialized = true;
        }
        //We don't want to persist state of animation and toast live data, always create new instances
        animationLiveData = new MutableLiveData<>();
        toastLiveData = new MutableLiveData<>();
    }

    public LiveData<GameData> getGameLiveData() {
        return gameLiveData;
    }

    public LiveData<AnimationData> getAnimationLiveData() {
        return animationLiveData;
    }

    public LiveData<String> getToastLiveData() {
        return toastLiveData;
    }

    @Override
    public void onLoadFinished(@NonNull Profiles people) {
        profiles = people;
        restartGame(); //Setup a new game when we receive the profiles
    }

    @Override
    public void onError(@NonNull Throwable error) {
        setToastData(context.getString(R.string.string_error_occurred_loading));
    }

    /**
     * Sets the properties of a new game and begins a new round.
     */
    public void restartGame() {
        GameData gameData = gameLiveData.getValue() == null ? new GameData() : gameLiveData.getValue();
        if (gameData.getCurrentMode() == null) { //If no game existed then default to normal mode.
            gameData.setCurrentMode(context.getResources().getString(R.string.key_normal_mode));
        }
        gameData.setCurrentScore(0);
        setNewRound(gameData);
    }

    /**
     * Grabs a new set of people from the profiles object and sets the observable data to alert
     * the view of the changed state.
     * @param newGameData new gamedata to update
     */
    private void setNewRound(GameData newGameData) {
        List<Person> newChoices = listRandomizer.pickN(profiles.getPeople(), 6);
        Person newGoal = listRandomizer.pickOne(newChoices);
        newGameData.setChoices(newChoices);
        newGameData.setGoal(newGoal);
        gameLiveData.setValue(newGameData);
    }

    /**
     * Called when the user changes the game mode through the menu.
     * Changes the game mode and restarts the game.
     * @param newMode the new game mode
     */
    public void changeMode(String newMode) {
        if (gameLiveData.getValue() != null) {
            gameLiveData.getValue().setCurrentMode(newMode);
        }
        restartGame();
    }

    /**
     * Called when a user taps on a portrait. Checks if the selection was correct or incorrect
     * and depending on the game mode, may restart the game or start a new round.
     * @param selectedView the imageView that was selected, contains the corresponding person object
     */
    public void onPersonSelected(View selectedView) {
        //Selected person object is stored in the imageview within the passed in viewgroup
        Person selectedPerson = (Person) selectedView.getTag();
        boolean isCorrect = checkIsCorrect(selectedPerson);
        GameData gameData = gameLiveData.getValue();
        String preferenceKey;
        if (isCorrect) {
            gameData.incrementScore();
            preferenceKey = context.getResources().getString(R.string.string_total_correct);
        } else {
            preferenceKey = context.getResources().getString(R.string.pref_key_incorrect);
        }
        updateTotalsSharedPreferences(preferenceKey);
        setAnimationData(selectedView, isCorrect);

        if (!isCorrect && isHardMode(gameData)) { //Start the game over if incorrect selection and we are in hard mode
            checkHighScore(gameData);
            restartGame();
        } else if (isCorrect) { //If the selection was correct, start a new round
            setNewRound(gameData);
        }
    }

    /**
     * Checks the given person object against the goal person to see if the selection was correct.
     * @param selection the person to compare with the goal
     * @return true if the person objects match
     */
    private boolean checkIsCorrect(Person selection) {
        boolean selectionCorrect = false;
        if (gameLiveData.getValue() != null) {
            selectionCorrect = gameLiveData.getValue().isCorrectSelection(selection);
        }
        return selectionCorrect;
    }

    /**
     * Returns true if the current mode of the given game is hard mode.
     * @param gameData the game to check
     * @return true if hard mode
     */
    private boolean isHardMode(GameData gameData) {
        return gameData.getCurrentMode().equals(context.getString(R.string.key_hard_mode));
    }

    /**
     * Updates the given shared preference value by incrementing the value currently stored.
     * @param preferenceKey the shared preference value to update
     */
    private void updateTotalsSharedPreferences(String preferenceKey) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int total = sharedPreferences.getInt(preferenceKey, 0) + 1;
        sharedPreferences.edit().putInt(preferenceKey, total).apply();
    }

    /**
     * Compares the current score with the stored highscore and updates the stored high score
     * if needed.
     * @param gameData the game to pull the current score from
     */
    private void checkHighScore(GameData gameData) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        int highScore = preferences.getInt(context.getResources().getString(R.string.pref_key_high_score), 0);
        if (gameData.getCurrentScore() > highScore) {
            preferences.edit().putInt(context.getResources().getString(R.string.pref_key_high_score), gameData.getCurrentScore()).apply();
            setToastData(context.getString(R.string.string_new_high_score));
        }
    }

    /**
     * Triggers the view to play the animation corresponding to the users selection.
     * @param selectedView     the view that was selected
     * @param selectionCorrect true if the selection was correct
     */
    private void setAnimationData(View selectedView, boolean selectionCorrect) {
        animationLiveData.setValue(new AnimationData(selectionCorrect, selectedView));
    }

    /**
     * Triggers the view to display a toast with the given string as the message.
     * @param message the message to display in the toast
     */
    private void setToastData(String message) {
        toastLiveData.setValue(message);
    }


}
