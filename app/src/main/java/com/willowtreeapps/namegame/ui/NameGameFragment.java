package com.willowtreeapps.namegame.ui;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.arch.lifecycle.LifecycleFragment;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
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
import com.willowtreeapps.namegame.core.AnimationData;
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
 * The name game is hosted within this fragment. A ViewModel is used to observe live data objects
 * that reflect the current state of the Name Game. When these objects change, we modify the fragment's
 * interface to reflect the new state.
 * @author Keegan Smith
 */
public class NameGameFragment extends LifecycleFragment {

    private final String STATS_TAG = "STATS_TAG";

    @Inject
    Picasso picasso;

    private ViewGroup portraitLayout;
    private RelativeLayout loadingLayout;
    private TextView textViewQuestion;
    private TextView textViewScore;
    private List<ImageView> portraitImageList = new ArrayList<>(6);
    private List<TextView> portraitTextList = new ArrayList<>(6); //A list for the portrait names

    private NameGameViewModel viewModel;
    private String currentMode; //Used only to show the correct menu option depending on current game mode

    /**
     * Sets this class as an injection target to populate injected fields.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        NameGameApplication.get(getActivity()).component().inject(this);
    }

    /**
     * Inflates this fragment's layout and initializes the viewmodel.
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.name_game_fragment, container, false);
        viewModel = ViewModelProviders.of(this).get(NameGameViewModel.class);
        viewModel.initialize();
        //Observe the GameData liveData, the meat of the game state
        viewModel.getGameLiveData().observe(this, new Observer<GameData>() {
            @Override
            public void onChanged(@Nullable GameData gameData) {
                onGameDataChanged(gameData);
            }
        });
        //Observe the animation LiveData, alerts views in this fragment when an animation needs to play
        viewModel.getAnimationLiveData().observe(this, new Observer<AnimationData>() {
            @Override
            public void onChanged(@Nullable AnimationData animationData) {
                animateSelection(animationData);
            }
        });
        //Observe the toast LiveData, alerts this fragment when a toast needs to be displayed to the user.
        viewModel.getToastLiveData().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String message) {
                displayToast(message);
            }
        });
        return rootView;

    }

    /**
     * Assigns all member level views and initializes the imageViews containing headshots.
     * @param view               the parent view of this fragment
     * @param savedInstanceState bundle of saved information if this fragment is being recreated
     */
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        textViewQuestion = (TextView) view.findViewById(R.id.text_view_title);
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
                        ((ViewGroup) view).getChildAt(1).setVisibility(View.VISIBLE); //Display this person's name
                        viewModel.onPersonSelected(((ViewGroup) view).getChildAt(0));
                    }
                });
                portraitImageList.add(face);
                portraitTextList.add(faceText);
            }
        }
        setImageViewSize();
        showLayout(loadingLayout);
    }

    /**
     * Inflates the menu from resources and displays only the options we want.
     * @param menu     the parent menu
     * @param inflater used to inflate the menu
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.name_game_menu, menu);
    }

    /**
     * Called when the user opens the menu. Only show either play hard or play normal depending on
     * the current mode.
     * @param menu the menu in this fragment
     */
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (currentMode != null){
            if (currentMode.equals(getResources().getString(R.string.key_normal_mode))){
                showMenuItem(menu, R.id.menu_action_play_hard);
            }else {
                showMenuItem(menu, R.id.menu_action_play_normal);
            }
        }
    }

    /**
     * Handles the different available options in the menu.
     * @param item the menu item that was pressed
     * @return true if the item was handled
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_action_restart: //Restart the current game
                viewModel.restartGame();
                return true;
            case R.id.menu_action_play_normal: //Switch to normal mode
                viewModel.changeMode(getResources().getString(R.string.key_normal_mode));
                return true;
            case R.id.menu_action_play_hard: //Switch to hard mode
                viewModel.changeMode(getResources().getString(R.string.key_hard_mode));
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
    private void showMenuItem(Menu menu, int id) {
            menu.findItem(R.id.menu_action_play_normal).setVisible(false);
            menu.findItem(R.id.menu_action_play_hard).setVisible(false);

            menu.findItem(id).setVisible(true);
    }

    /**
     * Sets the given views visibilty to visible. Used when showing or
     * hiding the loading layout or portrait layout.
     * @param view the view to display.
     */
    private void showLayout(View view) {
        portraitLayout.setVisibility(View.GONE);
        loadingLayout.setVisibility(View.GONE);

        view.setVisibility(View.VISIBLE);
    }

    /**
     * Opens an instance of a StatsDialogFragment that displays the users
     * statistics.
     */
    private void openStatsDialog() {
        DialogFragment dialogFragment = new StatsDialogFragment();
        dialogFragment.show(getFragmentManager(), STATS_TAG);
    }

    /**
     * Called when the observed GameData object changes. We use
     * this new gamedata object to set the properties of a new
     * game. The GameData observable can be changed by the user
     * tapping on a correct portrait (or incorrect portrait if
     * in hard mode), switching the game mode, or restarting the
     * current game.
     *
     * This method is also called if a gameData
     * object exists when we begin observing it, this is the case
     * when restoring state after being destroyed by a system constraint.
     * @param gameData the new state of the game
     */
    private void onGameDataChanged(final GameData gameData) {
        if (gameData.getCurrentScore() != 0) { //Pause before starting the new round to let the select animation play
            setPortraitsEnabled(false);
            Handler handler = new Handler(); //Wait one second before starting the new round
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (getActivity() != null) { //Make sure we are still attached to activity after time elapses
                        setGameProperties(gameData);
                    }
                }
            }, 1000);
        }else { //On new game immediately set properties
            setGameProperties(gameData);
        }
    }

    /**
     * Uses the observed gameData object to set the properties of this game.
     * @param gameData contains information about the current game
     */
    private void setGameProperties(final GameData gameData) {
        setPortraitsEnabled(true);
        showLayout(loadingLayout);
        setDisplayScore(gameData.getCurrentMode(), gameData.getCurrentScore());
        setQuestion(gameData.getGoalNameString());
        setImages(gameData.getChoices());
        showLayout(portraitLayout);
    }

    /**
     * Displays and updates the score if we are in hard mode.
     * @param gameMode current game mode
     */
    private void setDisplayScore(String gameMode, int currentScore) {
        if (gameMode.equals(getResources().getString(R.string.key_normal_mode))) { //Hide score if not hard mode
            textViewScore.setVisibility(View.GONE);
            currentMode = getResources().getString(R.string.key_normal_mode);
        } else {
            textViewScore.setVisibility(View.VISIBLE);
            String score = getResources().getString(R.string.string_score);
            score = score + " " + String.valueOf(currentScore);
            int scoreLength = score.length();
            SpannableStringBuilder builder = new SpannableStringBuilder(score);
            builder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getActivity(), R.color.colorAccent)), scoreLength, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            textViewScore.setText(builder);
            currentMode = getResources().getString(R.string.key_hard_mode);
        }
    }

    /**
     * Updates the question text field with the name of the new person goal.
     * @param goalName the person to fetch name information from.
     */
    private void setQuestion(String goalName) {
        String question = getResources().getString(R.string.string_question);
        int startIndex = question.length();
        question = question + " " + goalName;
        SpannableStringBuilder builder = new SpannableStringBuilder(question);
        builder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getActivity(), R.color.colorAccent)), startIndex, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append("?");
        textViewQuestion.setText(builder);
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
     * Sets the size of each image view to make sure all imageviews are of uniform size.
     */
    private void setImageViewSize() {
        int imageSize = (int) Ui.convertDpToPixel(100, getContext());
        for (ImageView view : portraitImageList) {
            ViewGroup.LayoutParams params = view.getLayoutParams();
            params.height = imageSize;
            params.width = imageSize;
            view.setLayoutParams(params);
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
     * Called when the animationData observable updates. Indicated the user made a selection
     * and we need to animate whether that selection was correct or incorrect.
     * @param animationData describes the selected view and whether the selection was correct
     */
    private void animateSelection(AnimationData animationData) {
        int color;
        if (animationData.isSelectionCorrect()) {
            color = ContextCompat.getColor(getContext(), R.color.alphaGreen);
        } else {
            color = ContextCompat.getColor(getContext(), R.color.alphaRed);
        }
        animateCorrectOrIncorrect(animationData.getSelectedView(), "colorFilter", color);
        animateCorrectOrIncorrect(textViewQuestion, "backgroundColor", color);
    }

    /**
     * Displays a toast to the user with the given string as the message.
     * @param message the message to display in the toast
     */
    private void displayToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Animates the given view to indicate if the user's selection was correct or incorrect.
     * @param view         the view to animate
     * @param propertyName the property of the view to modify
     * @param toColor      the color to animate with
     */
    private void animateCorrectOrIncorrect(View view, String propertyName, int toColor) {
        ValueAnimator valueAnimator = ObjectAnimator.ofInt(view, propertyName, toColor);
        valueAnimator.setRepeatCount(1);
        valueAnimator.setEvaluator(new ArgbEvaluator());
        valueAnimator.setDuration(500);
        valueAnimator.setRepeatMode(ValueAnimator.REVERSE);
        valueAnimator.start();
    }
}
