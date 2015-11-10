package com.foo.scenesinger.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class MainActivity extends Activity {

    private RelativeLayout topContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.Init();
    }

    private void Init() {
        this.topContainer = (RelativeLayout) this.findViewById(R.id.topContainer);

        this.InitStartText();
        this.InitTopContainerClickListener();
    }

    private void InitStartText() {
        TextView textView = (TextView) this.findViewById(R.id.startText);
        textView.setX(200);
        textView.setY(135);

    }

    private void InitTopContainerClickListener() {
        this.topContainer.setOnClickListener(OnClick_topContainer);
        this.topContainer.setFocusableInTouchMode(true);
        this.topContainer.requestFocus();
        this.topContainer.requestFocus();
    }

    /////////////////////////////////////
    //callbacks
    /////////////////////////////////////

    private View.OnClickListener OnClick_topContainer = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            RunBrain();
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /////////////////////////////////////
    //utilities
    /////////////////////////////////////

    private void RunBrain() {
        Intent Intent = new Intent(this, BrainActivity.class);
        startActivity(Intent);
    }
}