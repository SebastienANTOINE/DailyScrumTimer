package fr.sebastien.antoine.dailyscrumtimer.app.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import java.io.IOException;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import fr.sebastien.antoine.dailyscrumtimer.app.R;
import fr.sebastien.antoine.dailyscrumtimer.app.utils.PreferencesManager;
import fr.sebastien.antoine.dailyscrumtimer.app.utils.Utils;

/**
 * StandUp Timer
 *
 * Created by Sebastien on 28/02/2014.
 */
public class StandupActivity extends Activity {

    //private static String TAG = "StandUP";

    private TextView mTimeTextView;
    private ImageButton mNextButton;
    private ImageView mCircleView;
    private TextView mOpenDiscussionTextView;

    private Typeface mTypefaceRobotoThin;

    private PowerManager.WakeLock mWakeLock;
    private Vibrator mVibrator;

    private long standUpTime = 15 * 60 * 1000; // Time of the StandUp in ms : 15 minutes
    private long turnTime;

    private Counter mTimer;
    private int mNumberParticipants;
    private int mMaxParticipants;

    private static int REFRESH = 15;
    private static int ANIMATION_LENGTH = 150;
    private static int MAX_ANIM_ITER = ANIMATION_LENGTH / REFRESH;
    private int animationIterations = MAX_ANIM_ITER;

    String[] colors;
    String[] negativeColors;

    private int sizeWidth;
    private int maxRadius;
    private Bitmap circleBitmap;
    private Canvas canvas;

    private Timer timerVibrate;
    private MediaPlayer mMediaPlayer;

    private PreferencesManager man;

    private long[] stats;

    private long timeAnimationBeginning = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_standup);

        colors = getResources().getStringArray(R.array.circle_colors);
        negativeColors = getResources().getStringArray(R.array.circle_colors_inverse);

        mVibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        mTypefaceRobotoThin = Typeface.createFromAsset(this.getAssets(), "fonts/Roboto-Thin.ttf");
        mTimeTextView = (TextView) findViewById(R.id.textViewTime);
        mCircleView = (ImageView) findViewById(R.id.circleView);
        mNextButton = (ImageButton) findViewById(R.id.buttonNext);
        mOpenDiscussionTextView = (TextView) findViewById(R.id.textViewOpenDiscussion);
        mTimeTextView.setTypeface(mTypefaceRobotoThin, Typeface.ITALIC);
        mOpenDiscussionTextView.setTypeface(mTypefaceRobotoThin);

        Point size = Utils.size(this);
        sizeWidth = size.x;

        // diameters = 80% of width, so radius 40%
        maxRadius = (sizeWidth * 40) / 100;
        circleBitmap = Bitmap.createBitmap(maxRadius * 2, maxRadius * 2, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(circleBitmap);

        mCircleView.setImageBitmap(circleBitmap);

        // Animation
        mCircleView.animate().scaleX(1).scaleY(1).setDuration(timeAnimationBeginning); // scale timer
        mNextButton.animate().scaleX(1).scaleY(1).setDuration(timeAnimationBeginning); // scale next
        mTimeTextView.animate().scaleX(1).scaleY(1).setDuration(timeAnimationBeginning); // scale text
        new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(timeAnimationBeginning);
                } catch (InterruptedException e) {
                }
            }
        }).start();

        man = new PreferencesManager(this);

        int minuteTime = man.getDataInt("minuteTime");
        if(minuteTime<1)minuteTime=15;
        standUpTime=minuteTime*60*1000;

        mNumberParticipants = man.getDataInt("nbParticipants");

        mMaxParticipants = mNumberParticipants;
        stats = new long [mMaxParticipants];
        Arrays.fill(stats,standUpTime);
        turnTime = standUpTime / mNumberParticipants;

        mTimer = new Counter(standUpTime, REFRESH);
        mTimer.start();
        turnTime = standUpTime / mNumberParticipants;

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "StandupWakeLock");
    }

    @Override
    public void onPause() {
        super.onPause();
        //mWakeLock.release();
        timerVibrate.cancel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mWakeLock.acquire();
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("nbParticipants", mNumberParticipants);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            mNumberParticipants = savedInstanceState.getInt("nbParticipants");
        }
    }

    /**
     * Draw Circle permit to draw a circle  or an arc with color, radius position and angle.
     *
     * @param canvas    Canvas to draw
     * @param color     Color of circle
     * @param angle     Angle in degrees
     * @param radius    radius of circle
     * @param positionX X&Y coordinates of center of view;
     * @param positionY
     */
    private void drawCircle(Canvas canvas, int color, float angle, int radius, int positionX, int positionY) {
        Paint paint = new Paint();

        final RectF rect = new RectF();

        /**
         * @param left   The X coordinate of the left side of the rectangle
         * @param top    The Y coordinate of the top of the rectangle
         * @param right  The X coordinate of the right side of the rectangle
         * @param bottom The Y coordinate of the bottom of the rectangle
         */
        rect.set(positionX - radius, positionY - radius, positionX + radius, positionY + radius);
        paint.setColor(color);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);

 
        /**
         * The arc is drawn clockwise. An angle of 0 degrees correspond to the
         * geometric angle of 0 degrees (3 o'clock on a watch.)
         *
         * @param oval       The bounds of oval used to define the shape and size
         *                   of the arc
         * @param startAngle Starting angle (in degrees) where the arc begins
         * @param sweepAngle Sweep angle (in degrees) measured clockwise
         * @param useCenter If true, include the center of the oval in the arc, and
         * close it if it is being stroked. This will draw a wedge
         * @param paint      The paint used to draw the arc
         */
        canvas.drawArc(rect, -90 + angle, 360 - angle, true, paint);
    }


    private void clearCircle(Canvas canvas, float angle, int radius, int positionX, int positionY) {
        Paint paint = new Paint();

        final RectF rect = new RectF();

        /**
         * @param left   The X coordinate of the left side of the rectangle
         * @param top    The Y coordinate of the top of the rectangle
         * @param right  The X coordinate of the right side of the rectangle
         * @param bottom The Y coordinate of the bottom of the rectangle
         */
        rect.set(positionX - radius, positionY - radius, positionX + radius, positionY + radius);
        paint.setAntiAlias(true);

        paint.setColor(Color.TRANSPARENT); // Set default background color to black transparent
        paint.setAlpha(0xFF);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        paint.setStyle(Paint.Style.FILL);
        paint.setDither(true);

        /**
         * The arc is drawn clockwise. An angle of 0 degrees correspond to the
         * geometric angle of 0 degrees (3 o'clock on a watch.)
         *
         * @param oval       The bounds of oval used to define the shape and size
         *                   of the arc
         * @param startAngle Starting angle (in degrees) where the arc begins
         * @param sweepAngle Sweep angle (in degrees) measured clockwise
         * @param useCenter If true, include the center of the oval in the arc, and
         * close it if it is being stroked. This will draw a wedge
         * @param paint      The paint used to draw the arc
         */
        canvas.drawArc(rect, -90 + angle, 360 - angle, true, paint);
    }

    private void drawAllCircle(Canvas canvas, float angleCurrentParticipant, int nbParticipant, int radiusMax, int positionX, int positionY, int centerOffset, boolean negativeColor) {
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        if (nbParticipant > 0) {
            int radiusUnit = (radiusMax - centerOffset) / nbParticipant;

            if (isAnimated()) {
                int lastRadiusUnit = (radiusMax - centerOffset) / (nbParticipant + 1);
                radiusUnit = lastRadiusUnit + ((radiusUnit - lastRadiusUnit) * animationIterations) / MAX_ANIM_ITER;
                animationIterations++;
            }

            int currentCircleColor = Color.parseColor(negativeColor ? negativeColors[mMaxParticipants - nbParticipant] : colors[mMaxParticipants - nbParticipant]);
            drawCircle(canvas, currentCircleColor, angleCurrentParticipant, radiusMax, positionX, positionY);

            for (int i = nbParticipant - 1; i > 0; i--) {
                drawCircle(canvas, Color.parseColor(colors[mMaxParticipants - i]), 0, (i * radiusUnit) + centerOffset, positionX, positionY);
            }
        }
        else {
            drawCircle(canvas, getGradientColorWhiteToRed(angleCurrentParticipant,180), angleCurrentParticipant, (radiusMax*75)/100, positionX, positionY);
            clearCircle(canvas,  0, ((radiusMax*75)/100)-20, positionX, positionY);
        }
    }

    private int getGradientColorWhiteToRed(float angle, float startAngle) {
        if (angle < startAngle) return Color.WHITE;

        //normalize between 0.0 and 1.0/
        float normalisedAngle = ((angle - startAngle) / (360 - startAngle)) % 1;

        return Color.rgb(255, (int) (255 * (1 - normalisedAngle)), (int) (255 * (1 - normalisedAngle)));
    }

    private boolean isAnimated(){
        return animationIterations<MAX_ANIM_ITER;
    }

    private void playAnimation(){
        animationIterations=0;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent homeIntent = new Intent(this, HomeActivity.class);
        startActivity(homeIntent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
        mTimer.cancel();
    }

    private float getAngleOfCurrentParticipant(long milliSecondUntilFinish) {
        float angle = ((float) milliSecondUntilFinish / (float) turnTime) * 360;
        return (360 - angle) % 720;
    }

    private void playSound() {

        try {

            mMediaPlayer = new MediaPlayer();
            String uri = man.getDataString("sound");
            if (uri == null) {
                mMediaPlayer = MediaPlayer.create(this, R.raw.finish);
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            } else {
                mMediaPlayer = new MediaPlayer();
                mMediaPlayer.setDataSource(this, Uri.parse("content://media"+uri));
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
                mMediaPlayer.setLooping(false);
                mMediaPlayer.prepare();
            }

            mMediaPlayer.start();
        }catch (IOException e){
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        }
    }

    public class Counter extends CountDownTimer {

        private long currentStartTime = 0;
        private long currentParticipantTimeAvailable = 0;
        private long FifteenSeconds = 15000;
        private long openTime;

        private VibrateTask vibrateTask;
        private VibratePatternTask vibratePatternTask;
        private boolean isOpenTimeEnd = false;

        public Counter(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);

            timerVibrate = new Timer();
            initVibrator();

            mNextButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    currentStartTime = 0;
                    mNumberParticipants--;

                    if (vibrateTask != null) {
                        vibrateTask.cancel();
                        timerVibrate.purge();
                    }
                    if (vibratePatternTask != null) {
                        vibratePatternTask.cancel();
                        timerVibrate.purge();
                    }
                    playAnimation();

                    if (mNumberParticipants == 0) { // No more turn = Free time, Open discussion
                        turnTime = 0;
                        mNextButton.setImageResource(R.drawable.ic_stop);
                        mOpenDiscussionTextView.setVisibility(View.VISIBLE);
                    }
                    else {
                        initVibrator();
                    }

                    if (mNumberParticipants < 0) { // Stop and finish the timer
                        isOpenTimeEnd = true;
                        openTime = standUpTime - currentParticipantTimeAvailable;
                        mTimer.cancel();
                        mTimer.onFinish();
                    }
                }
            });
        }

        @Override
        public void onTick(long millisUntilFinished) {
            if (mNumberParticipants > 0) {
                if (currentStartTime == 0) {
                    if (mNumberParticipants < mMaxParticipants) {
                        stats[mMaxParticipants - (mNumberParticipants + 1)] = standUpTime - millisUntilFinished;
                    }
                    currentStartTime = millisUntilFinished;
                }
                currentParticipantTimeAvailable = turnTime - (currentStartTime - millisUntilFinished);
            }
            else {
                if (turnTime == 0) {
                    stats[mMaxParticipants - 1] = standUpTime - millisUntilFinished;
                    turnTime = millisUntilFinished;
                }
                currentParticipantTimeAvailable = millisUntilFinished;
            }

            int centerOffset = mNextButton.getHeight() / 2;

            long millis = currentParticipantTimeAvailable;
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;

            if (millis < 0) {
                if (seconds < 0) seconds = seconds * -1;
                if (minutes < 0) minutes = minutes * -1;
                mTimeTextView.setTextColor(getResources().getColor(R.color.red));
                mTimeTextView.setText(String.format("-%02d'%02d''", minutes, seconds));
            } else {
                mTimeTextView.setTextColor(getResources().getColor(R.color.white));
                mTimeTextView.setText(String.format("%02d'%02d''", minutes, seconds));
            }

            float angle = getAngleOfCurrentParticipant(currentParticipantTimeAvailable);
            drawAllCircle(canvas, angle, mNumberParticipants, maxRadius, maxRadius, maxRadius, centerOffset, millis < 0);
            mCircleView.invalidate();
        }

        @Override
        public void onFinish() {
            playSound(); // Play a alarm sound when time is up

            long[] timeElapsed = stats.clone();

            for (int p = 1; p < mMaxParticipants; p++) {
                stats[p] = timeElapsed[p] - timeElapsed[p - 1];
            }

            for (long l : stats) {
                Log.d("Time : ", l + "sec");
            }

            Intent summaryIntent = new Intent(getApplicationContext(), SummaryActivity.class);
            summaryIntent.putExtra("stats", stats);
            summaryIntent.putExtra("turnTime", standUpTime / mMaxParticipants);
            summaryIntent.putExtra("openTime", isOpenTimeEnd ? openTime - timeElapsed[mMaxParticipants - 1] : standUpTime - timeElapsed[mMaxParticipants - 1]);
            startActivity(summaryIntent);
            overridePendingTransition(R.anim.flip_out, R.anim.flip_in);
            finish();
        }

        public void initVibrator() {
            // Vibrate : at 15 seconds and after 0
            if (timerVibrate != null) {
                long TimeToVibrate = turnTime - FifteenSeconds;
                vibrateTask = new VibrateTask();
                if (TimeToVibrate > 0) {
                    timerVibrate.schedule(vibrateTask, TimeToVibrate);
                }

                vibratePatternTask = new VibratePatternTask();
                timerVibrate.scheduleAtFixedRate(vibratePatternTask, turnTime, 1000);
            }
        }
    }

    class VibrateTask extends TimerTask {
        @Override
        public void run() {
            mVibrator.vibrate(1000);
        }
    }

    class VibratePatternTask extends TimerTask {
        @Override
        public void run() {
            mVibrator.vibrate(200);
        }
    }
}