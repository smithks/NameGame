package com.willowtreeapps.namegame.ui;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.arch.lifecycle.LifecycleFragment;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.willowtreeapps.namegame.R;
import com.willowtreeapps.namegame.core.GameData;
import com.willowtreeapps.namegame.core.NameGameApplication;
import com.willowtreeapps.namegame.core.NameGameViewModel;
import com.willowtreeapps.namegame.network.api.model.Person;
import com.willowtreeapps.namegame.util.CircleBorderTransform;
import com.willowtreeapps.namegame.util.Ui;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * The name game is hosted within this fragment. Uses callbacks from the ProfilesRepository.Listener
 * interface to receive person information from a ProfilesRepository object which grabs network data
 * using Retrofit.
 * @author Keegan Smith
 */
public class NameGameFragment extends LifecycleFragment {

    private final String STATS_TAG = "STATS_TAG";

    @Inject
    Picasso picasso;

    private Menu menu;
    private ViewGroup portraitLayout;
    private RelativeLayout loadingLayout;
    private TextView textViewTitle;
    private TextView textViewScore;
    private List<ImageView> portraitImageList = new ArrayList<>(6);
    private List<TextView> portraitTextList = new ArrayList<>(6); //A list for the portrait names

    private NameGameViewModel viewModel;

    /**
     * Sets this class as an injection target to populate injected fields.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NameGameApplication.get(getActivity()).component().inject(this);
        viewModel = ViewModelProviders.of(this).get(NameGameViewModel.class);
        viewModel.initialize();
        viewModel.getGameData().observe(this, new Observer<GameData>() {
            @Override
            public void onChanged(@Nullable GameData gameData) {
                refreshGame(gameData);
            }
        });
        setHasOptionsMenu(true);
    }

    /**
     * Inflates this fragment's layout.
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.name_game_fragment, container, false);
    }

    /**
     * Assigns all member level views and initializes the imageViews containing headshots. Restores
     * from saved state if one exists, otherwise initiates a new game.
     * @param view the parent view of this fragment
     * @param savedInstanceState bundle of saved information if this fragment is being recreated
     */
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        textViewTitle = (TextView) view.findViewById(R.id.text_view_title);
        textViewScore = (TextView) view.findViewById(R.id.text_view_score);
        portraitLayout = (ViewGroup) view.findViewById(R.id.face_container);
        loadingLayout = (RelativeLayout) view.findViewById(R.id.loading_layout);

        //Populate the portraitImageList array with the 6 imageviews.
        int n = portraitLayout.getChildCount();
        for (int i = 0; i < n; i++) {
            ViewGroup group = (ViewGroup) portraitLayout.getChildAt(i);
            for (int j = 0; j < group.getChildCount(); j++) { //Grab the ImageViews and corresponding TextViews
                ViewGroup portraitCell = (ViewGroup) group.getChildAt(j);
                ImageView face = (ImageView) portraitCell.getChildAt(0);
                TextView faceText = (TextView) portraitCell.getChildAt(1);
                portraitCell.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //viewModel.onPersonSelected((Person)view.getTag());
                        onPersonSelected(view);
                    }
                });
                portraitImageList.add(face);
                portraitTextList.add(faceText);
            }
        }
        setImageViewSize();

        restoreOrInitiateGame(savedInstanceState);
    }

//    /**
//     * Saves the member objects to be restored if this fragment is destroyed due to system constraint.
//     * @param outState bundle of saved information to restore
//     */
//    @Override
//    public void onSaveInstanceState(Bundle outState) {
//        outState.putInt(KEY_SCORE, currentScore);
//        outState.putString(KEY_MODE, currentMode);
//        outState.putParcelable(KEY_GOAL, currentGoal);
//        outState.putParcelable(KEY_PROFILES, profiles);
//        outState.putParcelableArrayList(KEY_PEOPLE, new ArrayList<Parcelable>(currentPeople));
//        super.onSaveInstanceState(outState);
//    }

    /**
     * Inflates the menu from resources and displays only the options we want.
     * @param menu the parent menu
     * @param inflater used to inflate the menu
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.name_game_menu, menu);
        this.menu = menu;
        //Display either play normal or play hard menu option
        //if (currentMode == null || currentMode.equals(MODE_NORMAL)) {
        //    showMenuItem(R.id.menu_action_play_hard);
        //} else {
        //    showMenuItem(R.id.menu_action_play_normal);
       // }
    }

    /**
     * Handles the different available options from the menu.
     * @param item the menu item that was pressed
     * @return true if the item was handled
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_action_restart: //Restart the current game
                //currentScore = 0;
                //refreshGame(currentMode);
                return true;
            case R.id.menu_action_play_normal: //Switch to normal mode
                //currentMode = MODE_NORMAL;
                //refreshGame(currentMode);
                showMenuItem(R.id.menu_action_play_hard);
                return true;
            case R.id.menu_action_play_hard: //Switch to hard mode
                //currentMode = MODE_HARD;
                //currentScore = 0;
                //refreshGame(currentMode);
                showMenuItem(R.id.menu_action_play_normal);
                return true;
            case R.id.menu_action_stats: //Display the stats dialog
                openStatsDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Sets the given menu item's visibility to true. Used to make sure both
     * play normal and play hard menu options are not both visible.
     * @param id id of the menu item to show
     */
    private void showMenuItem(int id) {
        menu.findItem(R.id.menu_action_play_normal).setVisible(false);
        menu.findItem(R.id.menu_action_play_hard).setVisible(false);

        menu.findItem(id).setVisible(true);
    }

    /**
     * Sets the given views visibilty to visible. Used when showing or
     * hiding the loading layout or portrait layout.
     * @param view the view to display.
     */
    private void showLayout(View view){
        portraitLayout.setVisibility(View.GONE);
        loadingLayout.setVisibility(View.GONE);

        view.setVisibility(View.VISIBLE);
    }

    /**
     * Opens an instance of a StatsDialogFragment that displays the users
     * statistics.
     */
    private void openStatsDialog(){
        DialogFragment dialogFragment = new StatsDialogFragment();
        dialogFragment.show(getFragmentManager(),STATS_TAG);
    }

    /**
     * Restores the previous state of the game if an instance of savedInstanceState is available,
     * otherwise we request data from the ProfilesRepository to begin a new game.
     * @param savedInstanceState saved bundle of state information
     */
    private void restoreOrInitiateGame(Bundle savedInstanceState){
        if (savedInstanceState != null) {
            //currentPeople = savedInstanceState.getParcelableArrayList(KEY_PEOPLE);
            //currentGoal = savedInstanceState.getParcelable(KEY_GOAL);
            //currentMode = savedInstanceState.getString(KEY_MODE);
            //currentScore = savedInstanceState.getInt(KEY_SCORE);
            //profiles = savedInstanceState.getParcelable(KEY_PROFILES);
            //setGameProperties(currentMode,currentGoal,currentPeople); //Continue the same game
        } else { //If not restoring, game is setup by callback
            //currentMode = MODE_NORMAL;
            //currentScore = 0;
            //repository.register(this);
        }
    }

    private void refreshGame(GameData gameData){
        showLayout(loadingLayout);
        //setGameProperties(gameData.getCurrentMode(),gameData.getGoal(),gameData.getChoices());
        setDisplayScore(gameData.getCurrentMode(),gameData.getUserScoreString());
        setQuestion(gameData.getGoalQuestionString());
        setImages(gameData.getChoices());
        showLayout(portraitLayout);
    }

    /**
     * Displays and updates the score if we are in hard mode.
     * @param gameMode current game mode
     */
    private void setDisplayScore(String gameMode, String score){
        if (gameMode.equals(getResources().getString(R.string.key_normal_mode))) { //Hide score if not hard mode
            textViewScore.setVisibility(View.GONE);
        } else {
            textViewScore.setVisibility(View.VISIBLE);
            textViewScore.setText(score);
        }
    }

    /**
     * Grabs the name from the given Person object and uses it to update the question.
     * @param question the person to fetch name information from.
     */
    private void setQuestion(String question){
        textViewTitle.setText(question);
    }

    /**
     * Grabs the headshots of a list of person objects and places them into their associated imageviews.
     * @param people list of person objects to populate views.
     */
    private void setImages(List<Person> people) {
        int imageSize = (int) Ui.convertDpToPixel(100, getContext());
        int n = portraitImageList.size();
        String scheme = "http:";

        for (int i = 0; i < n; i++) {
            ImageView face = portraitImageList.get(i);
            //Use default Willowtree image if no url exists
            String url = "//images.contentful.com/3cttzl4i3k1h/5ZUiD3uOByWWuaSQsayAQ6/c630e7f851d5adb1876c118dc4811aed/featured-image-TEST1.png";
            if (people.get(i).getHeadshot().getUrl() != null) {
                url = people.get(i).getHeadshot().getUrl();
            }
            picasso.load(scheme.concat(url))
                    .placeholder(R.drawable.ic_face_white_48dp)
                    .resize(imageSize, imageSize)
                    .transform(new CircleBorderTransform())
                    .into(face);
            face.setTag(people.get(i)); //Store the person object in this views tag
            TextView portraitName = portraitTextList.get(i);
            portraitName.setText(people.get(i).getFirstName() + " " + people.get(i).getLastName());
            portraitName.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Sets the size of each image view to make sure all imageviews are uniform size.
     */
    private void setImageViewSize(){
        int imageSize = (int) Ui.convertDpToPixel(100, getContext());
        for (ImageView view: portraitImageList){
            ViewGroup.LayoutParams params = view.getLayoutParams();
            params.height = imageSize;
            params.width = imageSize;
            view.setLayoutParams(params);
        }
    }

    /**
     * Called when a portrait is clicked. Compares the associated person object with
     * the goal and progress the game as appropriate.
     * @param view The view that was selected, the associated person is stored in the tag
     */
    private void onPersonSelected(@NonNull View view) {
        ImageView portrait = (ImageView) ((ViewGroup) view).getChildAt(0);
        TextView portraitName = (TextView) ((ViewGroup) view).getChildAt(1);
        Person person = (Person) portrait.getTag();

        portraitName.setVisibility(View.VISIBLE); //Display this person's name

        String totalKey;
        int color;

//        if (person.equals(currentGoal)) { //Correct portrait selected
//            color = ContextCompat.getColor(getContext(), R.color.alphaGreen);
//            totalKey = getResources().getString(R.string.pref_key_correct);
//            if (currentMode.equals(MODE_HARD)) {
//                currentScore++;
//            }
//            pauseAndRefresh();
//        } else { //Incorret portrait selected
//            color = ContextCompat.getColor(getContext(), R.color.alphaRed);
//            totalKey = getResources().getString(R.string.pref_key_incorrect);
//            if (currentMode.equals(MODE_HARD)) {
//                compareHighScore(currentScore); //Update highscore if needed
//                currentScore = 0;
//                pauseAndRefresh();
//            }
//        }

        //incrementTotals(totalKey); //Update statistics
        //Animate the questionTextView and portrait that was selected
        //animateCorrectOrIncorrect(portrait,"colorFilter",color);
        //animateCorrectOrIncorrect(textViewTitle,"backgroundColor",color);
        viewModel.setNewRound();
    }

    /**
     * Pause for a second before restarting the game so user can see correct portrait and
     * corresponding name.
     */
    private void pauseAndRefresh() {
        setPortraitsEnabled(false);
        Handler handler = new Handler(); //Wait one second before starting the new round
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (getActivity() != null) { //Make sure we are still attached to activity after time elapses
                    setPortraitsEnabled(true);
                    viewModel.setNewRound();
                }
            }
        }, 1000);
    }

    /**
     * Called when a portrait is pressed, updates the count of correct or incorrect guesses.
     * @param key the preference to update
     */
    private void incrementTotals(String key){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        int total = preferences.getInt(key,0)+1;
        preferences.edit().putInt(key,total).apply();
    }

    /**
     * Compares the user's score with the saved highscore. Alerts user and stores new high score.
     * @param newScore the score from the last game
     */
    private void compareHighScore(int newScore){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        int oldScore = preferences.getInt(getResources().getString(R.string.pref_key_high_score),0);
        if (newScore > oldScore){
            Toast.makeText(getContext(),getResources().getString(R.string.string_new_high_score),Toast.LENGTH_SHORT).show();
            preferences.edit().putInt(getResources().getString(R.string.pref_key_high_score),newScore).apply();
        }
    }

    /**
     * Enables or disables the portrait views so their listeners aren't called while
     * processing new views.
     * @param enabled new state of the view
     */
    private void setPortraitsEnabled(final boolean enabled) {
        for (int i = 0; i < portraitLayout.getChildCount(); i++) {
            ViewGroup group = (ViewGroup) portraitLayout.getChildAt(i);
            for (int j = 0; j < group.getChildCount(); j++) {
                ViewGroup portraitCell = (ViewGroup) group.getChildAt(j);
                portraitCell.setClickable(enabled);
            }
        }
    }

    /**
     * Animates the given view to indicate if the user's selection was correct or incorrect.
     * @param view the view to animate
     * @param propertyName the property of the view to modify
     * @param toColor the color to animate with
     */
    private void animateCorrectOrIncorrect(View view, String propertyName, int toColor){
        ValueAnimator valueAnimator = ObjectAnimator.ofInt(view,propertyName,toColor);
        valueAnimator.setRepeatCount(1);
        valueAnimator.setEvaluator(new ArgbEvaluator());
        valueAnimator.setDuration(500);
        valueAnimator.setRepeatMode(ValueAnimator.REVERSE);
        valueAnimator.start();
    }
}
