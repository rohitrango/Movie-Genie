package com.shunya.moviegenie;

import android.content.Intent;
import android.media.Rating;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

public class MovieDetailActivity extends AppCompatActivity {

    public String fb_id;
    public int movie_pk;

    public class Getter implements Runnable {

        @Override
        public void run() {

            String url = "http://10.196.4.62:8000"+"/MovieApp/getMovieById";

            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                Log.d("RESPONSE: ",response);
                                JSONObject json = new JSONObject(response);

                                // name
                                TextView titleView = (TextView)findViewById(R.id.movieTitle);
                                titleView.setText(json.getString("name"));

                                // rating here
                                RatingBar ratingBar = (RatingBar)findViewById(R.id.avgRating);
                                ratingBar.setRating(json.getInt("rating"));


                                // genres
                                TextView genresView = (TextView)findViewById(R.id.genres);
                                String genretext = "Genre: ";
                                JSONArray gObj = json.getJSONArray("genre");
                                for(int i=0;i<gObj.length()-1;i++) {
                                    genretext += ((JSONObject)(gObj.get(i))).getString("pk") + ", ";
                                }
                                genretext += ((JSONObject)(gObj.get(gObj.length()-1))).getString("pk");

                                genresView.setText(genretext);

                                // cast
                                TextView castview = (TextView)findViewById(R.id.cast);
                                String actorText = "Cast: ";
                                JSONArray aObj = json.getJSONArray("cast");
                                for(int i=0;i<aObj.length();i++) {
                                    JSONObject o = aObj.getJSONObject(i);
                                    actorText += (o.getJSONObject("fields")).getString("name") + ", ";
                                }
                                JSONObject o = aObj.getJSONObject(aObj.length()-1);
                                actorText += (o.getJSONObject("fields")).getString("name");

                                castview.setText(actorText);


                                Log.d(response,"res");
                            }
                            catch(Exception e) {
                                e.printStackTrace();
                                Intent i = new Intent(getApplicationContext(),MainActivity.class);
                                startActivity(i);
                                finish();
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
                    mParams.put("movie_id",Integer.toString(movie_pk));
                    return mParams;
                }
            };
            // Add the request to the RequestQueue.
            queue.add(stringRequest);

        }
    }

    @Override
    public void onBackPressed(){
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        Log.d("here","here");
        try {
            Intent intent = getIntent();
            fb_id = intent.getExtras().getString("fb_id");
            movie_pk = intent.getExtras().getInt("movie_pk",-1);
            // got the fb_id and movie_pk
            if(fb_id == null || movie_pk == -1)
            {
                Intent i = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(i);
                finish();
            }

            // fetch the details
            Log.d("here","here1");
            Getter getter = new Getter();
            new Thread(getter,"getter").start();

        }
        catch(Exception e) {
            e.printStackTrace();
            Intent i = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(i);
            finish();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

    }

}
