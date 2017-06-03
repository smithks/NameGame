package com.willowtreeapps.namegame.core;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import com.willowtreeapps.namegame.R;
import com.willowtreeapps.namegame.network.api.ProfilesRepository;
import com.willowtreeapps.namegame.network.api.model.Person;
import com.willowtreeapps.namegame.network.api.model.Profiles;

import java.util.List;

import javax.inject.Inject;

/**
 * @author Keegan Smith
 * @since 5/24/2017
 */

public class NameGameViewModel extends AndroidViewModel implements ProfilesRepository.Listener{

    @Inject
    public ListRandomizer listRandomizer;
    @Inject
    public ProfilesRepository repository;
    private Context context;

    private Profiles profiles;
    private MutableLiveData<GameData> gameData;

    public NameGameViewModel(Application application) {
        super(application);
        context = application;
    }

//    @Inject
//    public NameGameViewModel(ProfilesRepository repository, ListRandomizer randomizer, Context context){
//        this.listRandomizer = randomizer;
//        this.repository = repository;
//        this.context = context;
//    }

    public void initialize(){
        gameData = new MutableLiveData<>();
        NameGameApplication.get(context).component().inject(this);
        if (profiles == null){
            repository.register(this);
        }
    }

    public LiveData<GameData> getGameData() {
        return gameData;
    }

    @Override
    public void onLoadFinished(@NonNull Profiles people) {
        profiles = people;
        setNewRound();
    }

    @Override
    public void onError(@NonNull Throwable error) {

    }

    public void onPersonSelected(Person selected){
        Person goalPerson = gameData.getValue().getGoal();

        String totalKey;
        int color;

        if (selected.equals(goalPerson)){
            color = ContextCompat.getColor(context,R.color.alphaGreen);
            totalKey = context.getResources().getString(R.string.pref_key_correct);
        }else{
            color = ContextCompat.getColor(context,R.color.alphaRed);
            totalKey = context.getResources().getString(R.string.pref_key_incorrect);
        }
    }

    public void setNewRound(){
        GameData newGameData = gameData.getValue() != null ? gameData.getValue() : new GameData();
        newGameData.setContext(context);
        List<Person> newChoices = listRandomizer.pickN(profiles.getPeople(),6);
        Person newGoal = listRandomizer.pickOne(newChoices);

        if (gameData.getValue() == null) {
            newGameData.setCurrentMode(context.getResources().getString(R.string.key_normal_mode));
            newGameData.setCurrentScore(0);
        }

        newGameData.setChoices(newChoices);
        newGameData.setGoal(newGoal);
        gameData.setValue(newGameData);
    }
}
