package com.asus_s550cb.theo.museum;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

public class Insert_code extends Activity {


    EditText numCodeTxt;
    String nextApp;
    int appToStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert_code);
        numCodeTxt= (EditText) findViewById(R.id.numCode);


        nextApp="nextApp";

        Bundle extras = getIntent().getExtras();
        if(extras !=null) {
            appToStart = extras.getInt("nextApp");
        }


        //Hide bars
        menu.hideNavBar(this.getWindow());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_insert_code, menu);
        return true;
    }

    //TODO: Kalese thn qrcodeScanner.class etsi wste na kaleitai o grifos, giati to quiz prepei na einai ok
    public void OnClick(View v){
        Intent itns;
        switch (v.getId()){
            case R.id.button_num_code_Ok:
                QrCodeScanner.numCode=numCodeTxt.getText().toString();
                QrCodeScanner.numCodeCheck=true;//Next time the QR Scanner will validate the num code...
                itns = new Intent(getApplicationContext(), QrCodeScanner.class);
                startActivity(itns);
                finish();
                break;
            case R.id.button_num_code_Back:
                //nothing to do
                itns = new Intent(getApplicationContext(), QrCodeScanner.class);
                itns.putExtra(nextApp, 1);
                startActivity(itns);
                finish();
                break;
        }

    }

}
