package fr.sebastien.antoine.dailyscrumtimer.app.ui;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.util.Linkify;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import java.io.IOException;

import fr.sebastien.antoine.dailyscrumtimer.app.R;
import fr.sebastien.antoine.dailyscrumtimer.app.utils.PreferencesManager;

/**
 * App Info
 * Created by Sebastien on 04/03/2014.
 */
public class InfoActivity extends Activity {

    private TextView mTextVersion;
    private TextView mTextLink;
    private TextView mTextFeedback;
    private TextView mTextRate;
    private Button params;
    private EditText timerParam;
    private PreferencesManager preferencesManager;
    private LinearLayout secretZone;
    private MediaPlayer mPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        mTextVersion = (TextView) findViewById(R.id.version);


        params = (Button) findViewById(R.id.buttonParams);
        timerParam = (EditText) findViewById(R.id.timerParam);
        secretZone = (LinearLayout) findViewById(R.id.secretZone);
        preferencesManager = new PreferencesManager(this);

        try {
            mTextVersion.setText("Version : " + getPackageManager().getPackageInfo("fr.sebastien.antoine.dailyscrumtimer.app", 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            mTextVersion.setText("Version : NA");
        }

        params.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                secretZone.setVisibility(secretZone.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            }
        });

        timerParam.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                int minuteTime = 0;
                try {
                    minuteTime = new Integer(timerParam.getText().toString());
                }
                catch (NumberFormatException e) {
                }
                preferencesManager.putDataInt("minuteTime", minuteTime);
            }
        });

        final RingtoneManager ringtoneManager = new RingtoneManager(this); //adds ringtonemanager
        ringtoneManager.setType(RingtoneManager.TYPE_NOTIFICATION); //sets the type to ringtones
        ringtoneManager.setIncludeDrm(true); //get list of ringtones to include DRM

        Cursor cursorRingtone = ringtoneManager.getCursor(); //appends my cursor to the ringtonemanager

        startManagingCursor(cursorRingtone); //starts the cursor query

        String[] title = {cursorRingtone.getColumnName(RingtoneManager.TITLE_COLUMN_INDEX)}; // get the list items for the listadapter could be TITLE or URI

        int[] to = {android.R.id.text1};

        // create simple cursor adapter
        SimpleCursorAdapter adapter =
                new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, cursorRingtone, title, to);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // get reference to our spinner
        Spinner s = (Spinner) findViewById(R.id.spinner1);
        s.setAdapter(adapter);
        s.setSelection(preferencesManager.getDataInt("soundPos"));
        s.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String uri = ringtoneManager.getRingtoneUri(position).getPath();
                preferencesManager.putDataString("sound", uri);
                preferencesManager.putDataInt("soundPos", position);
                try {
                    disablePlayer();
                    mPlayer = new MediaPlayer();
                    mPlayer.setDataSource(InfoActivity.this, Uri.parse("content://media" + uri));
                    mPlayer.setAudioStreamType(AudioManager.STREAM_RING);
                    mPlayer.setLooping(false);
                    mPlayer.prepare();

                    mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            disablePlayer();
                        }
                    });

                    mPlayer.start();
                } catch (IOException e) {
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        mTextFeedback = (TextView) findViewById(R.id.feedback);
        mTextRate = (TextView) findViewById(R.id.rate);

        mTextFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto","sebastien.antoine.pro@gmail.com", null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.send_feedback)+" "+getString(R.string.app_name));
                startActivity(Intent.createChooser(emailIntent, getString(R.string.send_feedback)));
            }
        });
        mTextRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String appName = getPackageName();
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appName)));
                } catch (ActivityNotFoundException exception) {
                    Toast.makeText(getApplicationContext(), "Play Store not found", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        disablePlayer();
        finish();
        overridePendingTransition(R.anim.flip_out, R.anim.flip_in);
    }

    public void disablePlayer() {
        if (mPlayer == null) return;
        mPlayer.stop();
        mPlayer.release();
        mPlayer = null;
    }
}