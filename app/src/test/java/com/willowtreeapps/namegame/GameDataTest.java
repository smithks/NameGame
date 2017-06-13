package com.willowtreeapps.namegame;

import com.willowtreeapps.namegame.core.GameData;
import com.willowtreeapps.namegame.network.api.model.Person;

import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Keegan Smith
 * @since 6/12/2017
 */

public class GameDataTest {

    @Test
    public void isCorrectSelection_passCorrect_returnTrue(){
        GameData gameData = new GameData();
        Person person = mock(Person.class);
        gameData.setGoal(person);
        Assert.assertTrue(gameData.isCorrectSelection(person));
    }

    @Test
    public void isCorrectSelection_passIncorrect_returnFalse(){
        GameData gameData = new GameData();
        Person person1 = mock(Person.class);
        Person person2 = mock(Person.class);
        gameData.setGoal(person1);
        Assert.assertFalse(gameData.isCorrectSelection(person2));
    }

    @Test
    public void incrementScore_incrementCorrectly(){
        GameData gameData = new GameData();
        int score = 0;
        gameData.setCurrentScore(score);
        gameData.incrementScore();
        score++;
        Assert.assertEquals(score,gameData.getCurrentScore());
    }

    @Test
    public void getGoalNameString_returnCorrect(){
        GameData gameData = new GameData();
        Person person = mock(Person.class);
        when(person.getFirstName()).thenReturn("Keegan");
        when(person.getLastName()).thenReturn("Smith");
        gameData.setGoal(person);
        Assert.assertEquals("Keegan Smith",gameData.getGoalNameString());
    }
}
