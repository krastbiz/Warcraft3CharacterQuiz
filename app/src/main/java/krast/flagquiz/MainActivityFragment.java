package krast.flagquiz;

import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.media.MediaPlayer;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import  java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import  java.util.List;
import java.util.Set;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.support.v4.app.DialogFragment;
import android.os.Handler;
import android.util.Log;
import android.view.View.OnClickListener;
import android.view.ViewAnimationUtils;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private static final int CHARACTERS_IN_QUIZ = 10;
    private static final String FAIL_IMAGE_PATH = "Images/Answers/fail.png";
    private static final String SUCCESS_IMAGE_PATH = "Images/Answers/success.png";

    private List<PossibleAnswer> fileNameList;
    private List<PossibleAnswer> quizCharactersList;
    private Set<String> racesSet;

    private PossibleAnswer correctAnswer;
    private int totalGuesses = 0;
    private int correctAnswers = 0;
    private int guessesRows;
    private SecureRandom random;
    private Handler handler;

    private LinearLayout quizLinearLayout;
    private LinearLayout finalLinearLayout;

    private TextView txt;

    private TextView questionNumberTextView;
    private ImageView answerImageView;
    private LinearLayout[] guessLinearLayouts;

    public MediaPlayer mediaPlayer = new MediaPlayer();

    private View view;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.fragment_main, container, false);
        fileNameList = new ArrayList<>();
        quizCharactersList = new ArrayList<>();
        random = new SecureRandom();
        handler = new Handler();

        quizLinearLayout = (LinearLayout) view.findViewById(R.id.quizLinearLayout);
        finalLinearLayout = (LinearLayout) view.findViewById(R.id.finalLinearLayout);
        finalLinearLayout.setVisibility(View.GONE);

        questionNumberTextView = (TextView) view.findViewById(R.id.questionNumberTextView);
        answerImageView = (ImageView) view.findViewById(R.id.answerImageView);

        guessLinearLayouts = new LinearLayout[3];
        guessLinearLayouts[0] = (LinearLayout) view.findViewById(R.id.row1LinearLayout);
        guessLinearLayouts[1] = (LinearLayout) view.findViewById(R.id.row2LinearLayout);
        guessLinearLayouts[2] = (LinearLayout) view.findViewById(R.id.row3LinearLayout);

        Button btnResetQuiz = (Button) view.findViewById(R.id.btnResetQuiz);
        btnResetQuiz.setOnClickListener(resetQuizListener);

        answerImageView.setOnClickListener(playerSoundListener);
        for (LinearLayout row: guessLinearLayouts){
            for (int column = 0; column < row.getChildCount(); column++){
                ImageButton button = (ImageButton) row.getChildAt(column);
                button.setOnClickListener(guessButtonListener);
            }
        }
        return view;
    }

    public void updateGuessRows(SharedPreferences sharedPreferences) {
        String choices =
                sharedPreferences.getString(MainActivity.CHOICES, null);
        guessesRows = Integer.parseInt(choices)/ 3;

        for (LinearLayout layout : guessLinearLayouts)
            layout.setVisibility(View.GONE);

        for (int row = 0; row < guessesRows; row++)
            guessLinearLayouts[row].setVisibility(View.VISIBLE);
    }

    public void updateRaces(SharedPreferences sharedPreferences){
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        String[] strings = new String[] {"undead", "elf"};
//        Set<String> races = new HashSet<String>(Arrays.asList(strings));
//        //races.add(getString(R.string.default_race));
//        editor.putStringSet("pref_raceToInclude", races);
//        editor.apply();
        racesSet = sharedPreferences.getStringSet(MainActivity.RACES, null);
    }

    public void updateQuestion(){
        fileNameList.clear();
        quizCharactersList.clear();
        AssetManager assets = getActivity().getAssets();
        try {
            for (String race: racesSet){
                String[] paths = assets.list("Images" + File.separator + race);
                for (String path : paths)
                    fileNameList.add(new PossibleAnswer(race, path.replace(".jpg", "")));
            }
        } catch (IOException ex) {}

        int charCount = 0;
        int numberOfCharacters = fileNameList.size();

        while (charCount <= guessesRows * 3) {
            int randomIndex = random.nextInt(numberOfCharacters);

            PossibleAnswer possibleAnswer = fileNameList.get(randomIndex);
            if (!quizCharactersList.contains(possibleAnswer)){
                quizCharactersList.add(possibleAnswer);
                charCount++;
            }
        }
        quizCharactersList.remove(quizCharactersList.size() - 1);
        quizCharactersList.add(correctAnswer);

        Collections.shuffle(quizCharactersList);

        for (int row = 0; row < guessesRows; row ++){
            for (int column = 0; column < guessLinearLayouts[row].getChildCount(); column++){
                ImageButton guessButton = (ImageButton) guessLinearLayouts[row].getChildAt(column);
                guessButton.setEnabled(true);
                String fileName = "Images" + File.separator + quizCharactersList.get(column + row * 3).race +
                        File.separator + quizCharactersList.get(column + row * 3).character + ".jpg";
                guessButton.setTag(quizCharactersList.get(column + row * 3).character);
                guessButton.setImageBitmap(getBitmapFromAssets(fileName));
            }
        }

        playSound();
    }
    public void loadCharacter(){
        fileNameList.clear();
        quizCharactersList.clear();
        AssetManager assets = getActivity().getAssets();

        try {
            for (String race: racesSet){
                String[] paths = assets.list("Images" + File.separator + race);
                for (String path : paths)
                    fileNameList.add(new PossibleAnswer(race, path.replace(".jpg", "")));
            }
        } catch (IOException ex) {}

        int charCount = 1;
        int numberOfCharacters = fileNameList.size();

        while (charCount <= guessesRows * 3) {
            int randomIndex = random.nextInt(numberOfCharacters);

            PossibleAnswer possibleAnswer = fileNameList.get(randomIndex);
            if (!quizCharactersList.contains(possibleAnswer)){
                quizCharactersList.add(possibleAnswer);
                charCount++;
            }
        }

        //Collections.shuffle(quizCharactersList);
        correctAnswer = quizCharactersList.get(0);

        questionNumberTextView.setText(getString(R.string.question, (totalGuesses + 1), CHARACTERS_IN_QUIZ));
        String race = correctAnswer.race;

        int randomIndex = 1;
        AssetFileDescriptor descriptor = null;
        try (InputStream stream = assets.open("Images/Answers/faq.png")){
            Drawable answerImage = Drawable.createFromStream(stream, "faq.png");
            answerImageView.setImageDrawable(answerImage);
        } catch (IOException ex){}

        //TODO Shuffle

        Collections.shuffle(quizCharactersList);

        for (int row = 0; row < guessesRows; row ++){
            for (int column = 0; column < guessLinearLayouts[row].getChildCount(); column++){
                ImageButton guessButton = (ImageButton) guessLinearLayouts[row].getChildAt(column);
                guessButton.setEnabled(true);
                String fileName = "Images" + File.separator + quizCharactersList.get(column + row * 3).race +
                        File.separator + quizCharactersList.get(column + row * 3).character + ".jpg";
                guessButton.setTag(quizCharactersList.get(column + row * 3).character);
                guessButton.setImageBitmap(getBitmapFromAssets(fileName));
            }
        }

        playSound();
    }

    private Bitmap getBitmapFromAssets(String fileName){
        AssetManager assets = getActivity().getAssets();
        InputStream is = null;
        try{
            is = assets.open(fileName);
        }catch (IOException e){
            e.printStackTrace();
        }

        Bitmap bitmap = BitmapFactory.decodeStream(is);
        return bitmap;
    }

    private void playSound() {
        try {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = new MediaPlayer();
            }

            mediaPlayer = new MediaPlayer();

            AssetManager assets = getActivity().getAssets();
            String soundsDirectory = "Sounds" + File.separator + correctAnswer.race + File.separator + correctAnswer.character;
            String[] fileList = assets.list(soundsDirectory);
            int randomIndex = random.nextInt(fileList.length);
            AssetFileDescriptor descriptor = assets.openFd(soundsDirectory + File.separator + fileList[randomIndex]);
            mediaPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
            descriptor.close();
            mediaPlayer.prepare();
            mediaPlayer.setVolume(1f, 1f);
            mediaPlayer.setLooping(false);
            mediaPlayer.start();
        } catch (Exception ex) {
        }
    }

    private OnClickListener playerSoundListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            playSound();
        }
    };
    private OnClickListener resetQuizListener = new OnClickListener() {
        @Override
        public void onClick(View v) { //TODO BUIDLOKOD ISPRAVIT
            view.findViewById(R.id.questionNumberTextView).setVisibility(View.VISIBLE);
            view.findViewById(R.id.answerImageView).setVisibility(View.VISIBLE);
            view.findViewById(R.id.guessCharacterTxtView).setVisibility(View.VISIBLE);

            view.findViewById(R.id.row1LinearLayout).setVisibility(View.VISIBLE);
            view.findViewById(R.id.row2LinearLayout).setVisibility(View.VISIBLE);
            view.findViewById(R.id.row3LinearLayout).setVisibility(View.VISIBLE);

            finalLinearLayout.setVisibility(View.GONE);

            totalGuesses = 0;
            correctAnswers = 0;
            loadCharacter();
        }
    };

    private OnClickListener guessButtonListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            totalGuesses++;
            ImageButton guessButton = ((ImageButton) v);
            String charName = (String) guessButton.getTag();
            if (charName.equals(correctAnswer.character)){
                correctAnswers++;

                try (InputStream stream = getActivity().getAssets().open(SUCCESS_IMAGE_PATH)){
                    Drawable answerImage = Drawable.createFromStream(stream, "success.png");
                    answerImageView.setImageDrawable(answerImage);
                } catch (IOException ex){}

            } else {
                try (InputStream stream = getActivity().getAssets().open(FAIL_IMAGE_PATH)){
                    Drawable answerImage = Drawable.createFromStream(stream, "fail.png");
                    answerImageView.setImageDrawable(answerImage);
                } catch (IOException ex){}
            }

            if (totalGuesses == CHARACTERS_IN_QUIZ){
                //quizLinearLayout.setVisibility(View.INVISIBLE);
                hideQuizBody();
                finalLinearLayout.setVisibility(View.VISIBLE);
                ((TextView) view.findViewById(R.id.textView)).setText(getString(R.string.results, (correctAnswers), CHARACTERS_IN_QUIZ));
                //txt.setVisibility(View.VISIBLE);
            } else {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadCharacter();
                    }
                }, 500);
            }
        }
    };

    public void hideQuizBody(){ //TODO BUIDLOKOD ISPRAVIT
        view.findViewById(R.id.questionNumberTextView).setVisibility(View.GONE);
        view.findViewById(R.id.answerImageView).setVisibility(View.GONE);
        view.findViewById(R.id.guessCharacterTxtView).setVisibility(View.GONE);

        view.findViewById(R.id.row1LinearLayout).setVisibility(View.GONE);
        view.findViewById(R.id.row2LinearLayout).setVisibility(View.GONE);
        view.findViewById(R.id.row3LinearLayout).setVisibility(View.GONE);
    }
}
