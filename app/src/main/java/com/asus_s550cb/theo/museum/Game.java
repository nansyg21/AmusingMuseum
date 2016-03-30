package com.asus_s550cb.theo.museum;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


public class Game extends AppCompatActivity {

    TextView txtV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        txtV=(TextView)findViewById(R.id.textView);
        txtV.setMovementMethod(new ScrollingMovementMethod());

        if(menu.lang.equals("uk"))
            Log.d("GLWWSA", "uk");
        else
            Log.d("GLWWSA", "EL");

        if(MainActivity.WORKING_ON_EXTERNAL_MUSEUM)
        {
            TextView txtV1=(TextView)findViewById(R.id.textView1);
            if(menu.lang.equals("uk")) {
                txtV1.setText(MainActivity.EXTERNAL_MUSEUM.museum_name);
            }
            else {
                txtV1.setText(MainActivity.EXTERNAL_MUSEUM.museum_name_gr);
            }

            TextView txtVFull=(TextView)findViewById(R.id.textView);
            txtVFull.setText(MainActivity.GetAllRoomNames());

            ImageView im = (ImageView) findViewById(R.id.imageView2);
            im.setImageBitmap( Bitmap.createScaledBitmap(MainActivity.EXTERNAL_MUSEUM.FromByteArrayToBitmap(MainActivity.EXTERNAL_MUSEUM.floor_planBmp.imageByteArray), 500, 538, true));

            Button b = (Button) findViewById(R.id.btnNext);
            b.invalidate();
        }

        menu.hideNavBar(this.getWindow());//hide...!
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_game, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void ButtonOnClick (View v){
        Button button = (Button) v;

        //Start the "Loading Screen"
        switch (v.getId()){
            case R.id.btnNext:
                startActivity(new Intent(getApplicationContext(),LoadingScreen.class));
                //Finish this screen when entering the game
                finish();
                break;
        }
    }
    //Remember to hide everything when Activity Resumes...
    @Override
    protected void onResume() {
        super.onResume();
        menu.hideNavBar(this.getWindow());
        //Update the language
        getApplicationContext().getResources().updateConfiguration( menu.setLocale(), null);

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
