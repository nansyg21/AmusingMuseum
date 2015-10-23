package com.asus_s550cb.theo.museum;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

public class CodeActivity extends Activity {


    EditText numCodeTxt;
    String nextApp;
    int appToStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code);
        numCodeTxt= (EditText) findViewById(R.id.numCode);

        numCodeTxt.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
            /* When focus is lost check that the text field
            * has valid values.
            */
                if (hasFocus) {
                   menu.hideNavBar(getWindow());
                }
            }
        });


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
            //    QrCodeScanner.numCode=numCodeTxt.getText().toString();
            //    QrCodeScanner.numCodeCheck=true;//Next time the QR Scanner will validate the num code...
                itns = new Intent(getApplicationContext(), QrCodeScanner.class);
                itns.putExtra("nextApp",appToStart);
                finish();
                startActivity(itns);
                break;
            case R.id.button_num_code_Back:
                //nothing to do
                itns = new Intent(getApplicationContext(), QrCodeScanner.class);
                itns.putExtra("nextApp", appToStart);
                finish();
                startActivity(itns);
                break;
        }
        finish();

    }

    @Override
    public void onBackPressed() {

        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_exit_small)
                .setMessage(R.string.confirm_exit_large)
                .setNegativeButton(R.string.confirm_exit_cancel, null)
                .setPositiveButton(R.string.confirm_exit_οκ, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1)
                    {
                        finish();
                        Intent itn= new Intent(getApplicationContext(), menu.class); //go to menu screen with proper flag set
                        itn.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        itn.putExtra("leaving", true);
                        startActivity(itn);
                    }
                }).create().show();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        //Hide bars
        menu.hideNavBar(this.getWindow());
    }

}
