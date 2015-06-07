package com.example.owner.fishyfish;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;


public class GalleryActivity extends ActionBarActivity {
    ImageView im;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_gallery, menu);
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

    public void biggerView(View v)

    {

        im=(ImageView)findViewById(R.id.selected);



        switch (v.getId())

        {

            case R.id.image1: im.setImageResource(R.drawable.im1);

                break;

            case R.id.image2: im.setImageResource(R.drawable.im2);

                break;

            case R.id.image3: im.setImageResource(R.drawable.im3);

                break;

            case R.id.image4: im.setImageResource(R.drawable.im4);

                break;

            case R.id.image5: im.setImageResource(R.drawable.im5);

                break;

            case R.id.image6: im.setImageResource(R.drawable.im6);

                break;

            case R.id.image7: im.setImageResource(R.drawable.im7);

                break;

        }

    }
}
