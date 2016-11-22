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
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.widget.LoginButton;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    ViewGroup.LayoutParams layoutParams,textBoxParams;


    // movie lister
    public class MovieListGetter implements Runnable {

        public void run() {

            String url = "http://10.196.4.62:8000/MovieApp/getRecommendations";

            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String json) {
                            // Display the response string.
                            ///////////////////////////////////////////////////////
                            Log.e("HERE in rreco","ggg");

                            try {
                                ViewGroup movielist = (ViewGroup)findViewById(R.id.movieList);
                                JSONArray response = new JSONArray(json);

                                for(int i=0;i<response.length();i++) {

                                    Log.e(Integer.toString(i),"d");
                                    LayoutInflater inflater = (LayoutInflater)getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                                    View childLayout = inflater.inflate(R.layout.movie_detail_box,null);

                                    JSONObject responseObj = (JSONObject)(response.get(i));
                                    final int moviePk = responseObj.getInt("pk");

                                    // name of movie -> clickable, send to the clickable
                                    TextView t = (TextView)(childLayout.findViewById(R.id.movieName));
                                    t.setText(  responseObj.getString("name") );
                                    t.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent intent = new Intent(getApplicationContext(),MovieDetailActivity.class);
                                            intent.putExtra("fb_id",fb_id);
                                            intent.putExtra("movie_pk",moviePk);
                                            startActivity(intent);
                                        }
                                    });

                                     //  // rating
                                    t = (TextView)childLayout.findViewById(R.id.rating);
                                    t.setText("Rating: " + Integer.toString(responseObj.getInt("ratings")) );
                                    movielist.addView(childLayout);


                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("ERROR","Error response");
                }
            }){
                @Override
                protected Map<String, String> getParams() {
                    Map<String,String> mParams = new HashMap<String, String>();
                    mParams.put("fb_id",fb_id);
                    return mParams;
                }
            };
            // Add the request to the RequestQueue.
            queue.add(stringRequest);

        }
    }





    public MovieListGetter movieListGetter;
    public String fb_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // set some layouts

        layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        textBoxParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        try {
            fb_id = getIntent().getExtras().getString("fb_id");

            // send the movies
            MovieListGetter movieListGetter = new MovieListGetter();
            new Thread(movieListGetter,"movies").start();
        }
        catch (Exception e) {
            // check for the accesstoken and whether its valid
            // check access token and get fb_id if not present
            AccessToken accessToken = AccessToken.getCurrentAccessToken();
            if (accessToken != null) {
                if(accessToken.getToken() != null && !accessToken.isExpired()) {

                    GraphRequest request = GraphRequest.newMeRequest(
                            AccessToken.getCurrentAccessToken(),
                            new GraphRequest.GraphJSONObjectCallback() {
                                @Override
                                public void onCompleted(
                                        JSONObject object,
                                        GraphResponse response) {
                                    try {
                                        Log.d("res",object.toString());
                                        fb_id = object.getString("id");
                                        Log.e("fb id ",fb_id);

                                        // send the movies
                                        MovieListGetter movieListGetter = new MovieListGetter();
                                        new Thread(movieListGetter,"movies").start();

                                    }
                                    catch(Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                    Bundle parameters = new Bundle();
                    parameters.putString("fields", "id,name,link");
                    request.setParameters(parameters);
                    request.executeAsync();
                }
                else {
                    // null access token
                    Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                    LoginManager.getInstance().logOut();
                    startActivity(intent);
                    finish();
                }
            }
            else {
                // NO access token
                Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                LoginManager.getInstance().logOut();
                startActivity(intent);
                finish();
            }
            // end of exception
        }

        // if no fb_id, go back to login
//        try {
//            fb_id = getIntent().getExtras().getString("fb_id");
//        }
//        catch(Exception e) {
//            e.printStackTrace();
//            Toast.makeText(getApplicationContext(),"Sorry, login first!",Toast.LENGTH_SHORT).show();
//            LoginManager.getInstance().logOut();
//            Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
//            startActivity(intent);
//            finish();
//        }

//        CallbackManager callbackManager = CallbackManager.Factory.create();
//        AccessToken accessToken = AccessToken.getCurrentAccessToken();
//
//        if(accessToken == null) {
//            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
//            finish();
//        }
//
//        else if(accessToken.getToken() == null || accessToken.isExpired()) {
//            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
//            finish();
//        }

        // set the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // layout drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();


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
            finish();
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
