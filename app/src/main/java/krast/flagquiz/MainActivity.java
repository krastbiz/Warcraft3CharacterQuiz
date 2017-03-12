package krast.flagquiz;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.preference.PreferenceManager;
import android.widget.Toast;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Set;
public class MainActivity extends AppCompatActivity {

    public static final String CHOICES = "pref_numberOfChoices";
    public static final String RACES = "pref_raceToInclude";

    private boolean preferencesChanged = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, true);

        PreferenceManager.getDefaultSharedPreferences(this).
                registerOnSharedPreferenceChangeListener(preferencesChangeListener);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        int orientation = getResources().getConfiguration().orientation;

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {

            getMenuInflater().inflate(R.menu.menu_main, menu);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        Intent preferencesIntent = new Intent(this, SettingsActivity.class);
        startActivity(preferencesIntent);
        return super.onOptionsItemSelected(item);

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (preferencesChanged){
            MainActivityFragment quizFragment = (MainActivityFragment)getSupportFragmentManager()
                    .findFragmentById(R.id.quizFragment);
            quizFragment.updateGuessRows(PreferenceManager.getDefaultSharedPreferences(this));
            quizFragment.updateRaces(PreferenceManager.getDefaultSharedPreferences(this));
            quizFragment.loadCharacter();
            //quizFragment.resetQuiz();
            preferencesChanged = false;
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        MainActivityFragment quizFragment =
                (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.quizFragment);
        if (quizFragment.mediaPlayer != null) {
            if (quizFragment.mediaPlayer.isPlaying()) {
                quizFragment.mediaPlayer.stop();
                quizFragment.mediaPlayer.release();
                quizFragment.mediaPlayer = new MediaPlayer();
            }
        }
    }

    private OnSharedPreferenceChangeListener preferencesChangeListener =
            new OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    preferencesChanged = true;
                    MainActivityFragment quizFragment =
                            (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.quizFragment);

                    if (key.equals(CHOICES))
                        quizFragment.updateGuessRows(sharedPreferences);
                    else if (key.equals(RACES)){
                        Set<String> races = sharedPreferences.getStringSet(RACES, null);

                        if (races != null && races.size() > 0){
                            quizFragment.updateRaces(sharedPreferences);

                        } else {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            races.add(getString(R.string.default_race));
                            editor.putStringSet(RACES, races);
                            editor.apply();
                            Toast.makeText(MainActivity.this, "Setting 'elf' as default race.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            };
}
