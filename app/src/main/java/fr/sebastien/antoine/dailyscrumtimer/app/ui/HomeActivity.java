package fr.sebastien.antoine.dailyscrumtimer.app.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import fr.sebastien.antoine.dailyscrumtimer.app.R;
import fr.sebastien.antoine.dailyscrumtimer.app.utils.CircularSeekBar;
import fr.sebastien.antoine.dailyscrumtimer.app.utils.PreferencesManager;
import fr.sebastien.antoine.dailyscrumtimer.app.utils.Utils;


/**
 * Choose number of participants
 */
public class HomeActivity extends Activity {

    private CircularSeekBar mCircularSeekBar;
    private TextSwitcher mNumberParticipantsTextSwitcher;
    private TextView mParticipantsTextView;
    private Button mGoButton;
    private ImageButton mInfoButton;

    private LinearLayout mButtonLayout;
    private FrameLayout mLayoutCircularSeekBar;
    private LinearLayout mLayoutHome;

    private Typeface mTypefaceRobotoThin;

    private static int mMaxParticipants = 10;
    private int mNumberParticipants = 2;

    private int mScreenHeight;

    private float initialPositionGoButtonY;
    final int timeAnimationGo = 500;
    final int timeAnimationAppLaunch = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Point size = Utils.size(this);
        mScreenHeight = size.y;

        mTypefaceRobotoThin = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Thin.ttf"); // Create font

        // TextSwitcher for number of participants
        mNumberParticipantsTextSwitcher = (TextSwitcher) findViewById(R.id.textViewNumber);
        // Set the ViewFactory of the TextSwitcher that will create TextView object when asked
        mNumberParticipantsTextSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            public View makeView() {
                TextView myText = new TextView(HomeActivity.this);
                myText.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                myText.setTextSize(getResources().getDimension(R.dimen.number_text_size));
                myText.setTextColor(getResources().getColor(R.color.white));
                myText.setTypeface(mTypefaceRobotoThin);
                return myText;
            }
        });

        mInfoButton = (ImageButton) findViewById(R.id.infoButton);
        mInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToInfo();
            }
        });
        // Declare the in and out animations and initialize them
        Animation in = AnimationUtils.loadAnimation(this, R.anim.fast_fade_in);
        Animation out = AnimationUtils.loadAnimation(this, R.anim.fast_fade_out);
        // Set the animation type of textSwitcher
        mNumberParticipantsTextSwitcher.setInAnimation(in);
        mNumberParticipantsTextSwitcher.setOutAnimation(out);
        mNumberParticipantsTextSwitcher.setText("" + mNumberParticipants);

        mParticipantsTextView = (TextView) findViewById(R.id.textViewParticipants);
        mParticipantsTextView.setTypeface(mTypefaceRobotoThin);

        mCircularSeekBar = (CircularSeekBar) findViewById(R.id.circularSeekBarParticipants);
        mCircularSeekBar.setMaxProgress(mMaxParticipants - 2);
        mCircularSeekBar.setSeekBarChangeListener(new CircularSeekBar.OnSeekChangeListener() {
            @Override
            public void onProgressChange(CircularSeekBar view, int newProgress) {
                mNumberParticipants = view.getProgress() + 2;
                mNumberParticipantsTextSwitcher.setText("" + mNumberParticipants);
            }
        });

        mLayoutCircularSeekBar = (FrameLayout) findViewById(R.id.layoutCircularSeekBar);
        mButtonLayout = (LinearLayout) findViewById(R.id.buttonLayout);

        mGoButton = (Button) findViewById(R.id.buttonGo);
        mGoButton.setTypeface(mTypefaceRobotoThin);
        mGoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mNumberParticipants != 0) {
                    // Animate GO button
                    mButtonLayout.animate().translationY((mLayoutCircularSeekBar.getY() + mLayoutCircularSeekBar.getHeight()/2) - (mButtonLayout.getY() + mButtonLayout.getHeight()/2)).setDuration(timeAnimationGo);
                    initialPositionGoButtonY = mButtonLayout.getY();

                    // FadeOut Slider (and text)
                    mCircularSeekBar.animate().alpha(0).setDuration(timeAnimationGo);
                    mNumberParticipantsTextSwitcher.animate().alpha(0).setDuration(timeAnimationGo);
                    mParticipantsTextView.animate().alpha(0).setDuration(timeAnimationGo);

                    // Go to timer
                    final Intent intentTimer = new Intent(getApplicationContext(), StandupActivity.class);

                    new Thread(new Runnable() {
                        public void run() {
                            try {
                                Thread.sleep(timeAnimationGo);
                                mButtonLayout.animate().scaleX(0).scaleY(0).setDuration(timeAnimationGo); // scale bouton GO
                                new Thread(new Runnable() {
                                    public void run() {
                                        try {
                                            Thread.sleep(timeAnimationGo);
                                        } catch (InterruptedException e) {
                                        }
                                        startActivity(intentTimer);
                                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                        finish();
                                    }
                                }).start();
                            }
                            catch (InterruptedException e) {
                            }

                        }
                    }).start();
                }
            }
        });

        // Launch animation
        mLayoutHome = (LinearLayout) findViewById(R.id.layoutHome);
        mLayoutHome.animate().alpha(1).setDuration(timeAnimationAppLaunch);
    }

    @Override
    public void onPause() {
        super.onPause();

        PreferencesManager preferencesManager = new PreferencesManager(getApplicationContext());
        preferencesManager.putDataInt("nbParticipants", mNumberParticipants);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("nbParticipants", mNumberParticipants);
        Log.d("nb", "" + mNumberParticipants);

        savedInstanceState.putInt("progressPercent", mCircularSeekBar.getProgressPercent());
        savedInstanceState.putInt("progress", mCircularSeekBar.getProgress());

        savedInstanceState.putFloat("X", mCircularSeekBar.getCx());
        savedInstanceState.putFloat("Y", mCircularSeekBar.getCy());

        savedInstanceState.putFloat("positionButtonGo", initialPositionGoButtonY);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            mNumberParticipants = savedInstanceState.getInt("nbParticipants");
            Log.d("nbRestore", "" + mNumberParticipants);
            mNumberParticipantsTextSwitcher.setText("" + mNumberParticipants);

            mCircularSeekBar.setProgressPercent(savedInstanceState.getInt("progressPercent"));
            mCircularSeekBar.setProgress(savedInstanceState.getInt("progress"));

            mCircularSeekBar.setCx(savedInstanceState.getFloat("X"));
            mCircularSeekBar.setCy(savedInstanceState.getFloat("Y"));
            mCircularSeekBar.invalidate();

            initialPositionGoButtonY = savedInstanceState.getFloat("positionButtonGo");
            mButtonLayout.setY(initialPositionGoButtonY);
        }
    }

    public void goToInfo() {
        Intent intentInfo = new Intent(this, InfoActivity.class);
        startActivity(intentInfo);
        overridePendingTransition(R.anim.flip_out, R.anim.flip_in);
    }
}