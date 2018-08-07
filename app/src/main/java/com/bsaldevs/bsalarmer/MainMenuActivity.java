package com.bsaldevs.bsalarmer;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.IOException;

public class MainMenuActivity extends AppCompatActivity {
    private Button b1;
    private Button b2;
    private MediaPlayer mediaPlayer;
    public static final int ABC=222;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        b1=findViewById(R.id.button);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent in = new Intent(MainMenuActivity.this, MapsActivity.class);
                startActivity(in);
            }
        });
        b2=findViewById(R.id.button2);

        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_PICK);

                i.setType("*/*");
                startActivityForResult(i, ABC);
            }
        });

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if ((requestCode==ABC) && (resultCode == RESULT_OK ) && (data!=null))
        {
            Uri myFile = data.getData();

            mediaPlayer=MediaPlayer.create(this,myFile);
            mplayergo(myFile);




        }
    }
    public void mplayergo (Uri sound)
    {
        try {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = MediaPlayer.create(this, sound);
             mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
