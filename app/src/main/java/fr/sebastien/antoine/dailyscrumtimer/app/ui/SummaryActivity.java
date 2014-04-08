package fr.sebastien.antoine.dailyscrumtimer.app.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.PowerManager;
import android.widget.TextView;

import java.util.ArrayList;

import fr.sebastien.antoine.dailyscrumtimer.app.R;
import fr.sebastien.antoine.dailyscrumtimer.app.utils.Bar;
import fr.sebastien.antoine.dailyscrumtimer.app.utils.BarGraph;

/**
 * Summary of times of the Stand Up
 * Created by Sebastien on 04/03/2014.
 */

public class SummaryActivity extends Activity {

    String[] colors;
    private TextView summaryTextView;
    private TextView statsTextView;
    private BarGraph barGraph;

    private long[] stats;
    private long turnTime;
    private long mAverageTime;
    private long mOpenTime;

    private Typeface mTypefaceRobotoThin;

    private PowerManager.WakeLock mWakeLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);
        colors = getResources().getStringArray(R.array.circle_colors);

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "StandupWakeLock");

        mTypefaceRobotoThin = Typeface.createFromAsset(this.getAssets(), "fonts/Roboto-Thin.ttf");

        summaryTextView = (TextView) findViewById(R.id.summaryTextView);
        statsTextView = (TextView) findViewById(R.id.statsTextView);
        summaryTextView.setTypeface(mTypefaceRobotoThin);
        statsTextView.setTypeface(mTypefaceRobotoThin);

        Intent intent = getIntent();
        Bundle b = intent.getExtras();
        stats = b.getLongArray("stats");
        turnTime = b.getLong("turnTime");
        mOpenTime = b.getLong("openTime");

        String statsText = new String();

        ArrayList<Bar> points = new ArrayList<Bar>();

        for (int s = 0; s < stats.length; s++) {
            long millis = stats[s];
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;
            String time = String.format("%02d'%02d''", minutes, seconds);

            String timeExceed = null;
            long millisTurnTime = stats[s] - turnTime; // temps parlé - temps normal du tour
            if (millisTurnTime > 0) { // positif
                int secondsTurnTime = (int) (millisTurnTime / 1000);
                int minutesTurnTime = secondsTurnTime / 60;
                secondsTurnTime = secondsTurnTime % 60;
                if (minutesTurnTime > 0) {
                    timeExceed = String.format("+%02d'%02d''", minutesTurnTime, secondsTurnTime);
                } else {
                    timeExceed = String.format("+%02d''", secondsTurnTime);
                }
                //statsText += "Participant " + (s + 1) + ".\t" + time + " (" + timeExceed + ")\n";
            } else {
                //statsText += "Participant " + (s + 1) + ".\t" + time + "\n";
            }

            Bar d = new Bar();
            d.setColor(Color.parseColor(colors[s]));
            d.setName("" + (s + 1));
            d.setValue(stats[s]);
            if (millisTurnTime > 0) {
                d.setValueString("" + time + "\n(" + timeExceed + ")");
            } else {
                d.setValueString("" + time);
            }
            points.add(d);

            mAverageTime += stats[s];
        }

        // Display open time
        int secondsOpenTime = (int) (mOpenTime / 1000);
        int minutesOpenTime = secondsOpenTime / 60;
        secondsOpenTime = secondsOpenTime % 60;
        String openTimeString = String.format("%02d'%02d''", minutesOpenTime, secondsOpenTime);

        statsText += "" + getString(R.string.open_discussion) + " : " + openTimeString;

        // Display average time
        mAverageTime /= stats.length;

        int secondsAverage = (int) (mAverageTime / 1000);
        int minutesAverage = secondsAverage / 60;
        secondsAverage = secondsAverage % 60;
        String averageTimeString = String.format("%02d'%02d''", minutesAverage, secondsAverage);

        statsText += "\n" + getString(R.string.average_time) + " : " + averageTimeString;
        statsTextView.setText(statsText);

        barGraph = (BarGraph) findViewById(R.id.bargraph);
        barGraph.setBars(points);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent homeIntent = new Intent(this, HomeActivity.class);
        startActivity(homeIntent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}