package com.shunya.moviegenie;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.login.LoginManager;
import com.facebook.login.widget.LoginButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    ViewGroup.LayoutParams layoutParams,textBoxParams;

    public class MovieListGetter implements Runnable {

        public void run() {

            String json = "[]";

            try {
                ViewGroup movielist = (ViewGroup)findViewById(R.id.movieList);
                JSONArray response = new JSONArray(json);

                for(int i=0;i<response.length();i++) {

                    Log.e(Integer.toString(i),"d");
                    LayoutInflater inflater = (LayoutInflater)getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                    View childLayout = inflater.inflate(R.layout.movie_detail_box,null);

                    JSONObject responseObj = (JSONObject)(response.get(i));

                    // name
                    TextView t = (TextView)(childLayout.findViewById(R.id.movieName));
                    t.setText(  responseObj.getString("name") );
                    t.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // add movie listener here
//                            String moviename =
                        }
                    });

//                    // rating
                    t = (TextView)childLayout.findViewById(R.id.rating);
                    t.setText("Rating: " + Integer.toString(responseObj.getInt("ratings")) );
                    movielist.addView(childLayout);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    public MovieListGetter movieListGetter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // check for the accesstoken and whether its valid
        layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        textBoxParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);


        CallbackManager callbackManager = CallbackManager.Factory.create();
        AccessToken accessToken = AccessToken.getCurrentAccessToken();

        if(accessToken == null) {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            finish();
        }

        else if(accessToken.getToken() == null || accessToken.isExpired()) {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            finish();
        }

        // set the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // layout drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();


        movieListGetter = new MovieListGetter();
        new Thread(movieListGetter,"mvG").start();


        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    // on back press
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    // create the options
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    // handle
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_logout) {
            LoginManager.getInstance().logOut();
            Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
            startActivity(intent);
        }
        else if(id == R.id.chatBoxList) {
            Intent intent = new Intent(getApplicationContext(),ChatBoxListActivity.class);
            startActivity(intent);
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
