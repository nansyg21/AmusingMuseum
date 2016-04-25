package com.Anaptixis.AmusingMuseum;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class PauseMenuActivity extends Activity {

    public static Boolean pause;

    ImageView imgv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_pause_menu);

        imgv=(ImageView)findViewById(R.id.titleImageView);

        if(menu.lang.equals("uk")) {
            imgv.setImageResource(R.drawable.pause_icon_en);
        }
        else
        {
            imgv.setImageResource(R.drawable.pause_icon_v2);
        }

        menu.hideNavBar(this.getWindow());


    }

    @Override
    protected void onResume() {
        super.onResume();
        menu.hideNavBar(this.getWindow());//hide everything on Resume

        //Update the language
        getApplicationContext().getResources().updateConfiguration(menu.setLocale(), null);
    }

    public void pauseButtonOnClick(View v)
    {
        Button bt;
        bt=(Button) v;

        switch (bt.getId()) {

            case R.id.btnExit:
                finish();
                Intent itn= new Intent(getApplicationContext(), menu.class); //go to menu screen with proper flag set
                itn.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                itn.putExtra("leaving", true);
                startActivity(itn);
                break;
            case R.id.btnContinue:
                finish();
                break;
            case R.id.btnHelp:
                startActivity(new Intent(getApplicationContext(),HelpscreenSliderActivity.class));
                break;
            case R.id.btnAbout:
                startActivity(new Intent(getApplicationContext(),CreditsActivity.class));
                break;
        }
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

}
