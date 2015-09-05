package com.asus_s550cb.theo.museum;

import android.app.Activity;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

public class Insert_code extends Activity {


    EditText numCodeTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert_code);
        numCodeTxt= (EditText) findViewById(R.id.numCode);
        //Hide bars
        hideNavBar();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_insert_code, menu);
        return true;
    }


    public void OnClick(View v){
        switch (v.getId()){
            case R.id.button_num_code_Ok:
                QrCodeScanner.numCode=numCodeTxt.getText().toString();
                finish();
                break;
            case R.id.button_num_code_Back:
                QrCodeScanner.numCode=null;
                finish();
                break;
        }
    }





    public void hideNavBar() {
        if (Build.VERSION.SDK_INT >= 19) {
            View v = getWindow().getDecorView();
            v.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }

    }
}
