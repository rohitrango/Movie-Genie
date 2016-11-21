package com.shunya.moviegenie;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ScrollView;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.view.ViewGroup.LayoutParams;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PrefActivity extends AppCompatActivity {

    public ArrayList<String> preferences;
    public ArrayList<String> actors;
    public ViewGroup prefView;
    public ViewGroup actorView;
    public LayoutParams layoutparams;
    public LayoutParams checkboxparams;
    public Button submitButton;
    public int actorselected,genresselected;
    public String ID;
    public String fb_id;


    public class PrefGetter implements Runnable {

        public String f;

        public PrefGetter(String m) {
            f = m;
        }

        @Override
        public void run() {
            // Instantiate the RequestQueue.
            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
            String url ="http://10.196.4.62:8000"+"/MovieApp/getAllGenres";

            // Request a string response from the provided URL.
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONArray res = new JSONArray(response);

                                prefView=(ViewGroup)findViewById(R.id.prefList);
                                for(int i=0;i<res.length();i++) {
                                    CheckBox checkBox = new CheckBox(getApplicationContext());
                                    checkBox.setLayoutParams(layoutparams);
                                    checkBox.setText(((JSONObject)res.get(i)).getString("pk"));
                                    checkBox.setTextColor(Color.BLACK);
                                    checkBox.setTextSize(20);
                                    checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                        @Override
                                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                                            if(b)
                                                genresselected++;
                                            else
                                                genresselected--;
                                            Log.d(Integer.toString(genresselected),"s");
                                        }
                                    });
                                    prefView.addView(checkBox);
                                }
                            }
                            catch(Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getApplicationContext(),"Some error occured!",Toast.LENGTH_SHORT).show();
                }
            });
            // Add the request to the RequestQueue.
            queue.add(stringRequest);
            // ACTOR list

            RequestQueue q2 = Volley.newRequestQueue(getApplicationContext());
            url = "http://10.196.4.62:8000"+"/MovieApp/getAllActors";

            // Request a string response from the provided URL.
            stringRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONArray res = new JSONArray(response);
                                actorView = (ViewGroup)findViewById(R.id.actorList);
                                for(int i=0;i<res.length();i++) {
                                    CheckBox checkBox = new CheckBox(getApplicationContext());
                                    checkBox.setLayoutParams(layoutparams);
                                    checkBox.setText( (((JSONObject)res.get(i)).getJSONObject("fields")).getString("name")  );
                                    checkBox.setTextColor(Color.BLACK);
                                    checkBox.setTextSize(20);
                                    checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                        @Override
                                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                                            if(b)
                                                actorselected++;
                                            else
                                                actorselected--;
                                            Log.d(Integer.toString(actorselected),"s");
                                        }
                                    });
                                    actorView.addView(checkBox);
                                }
                            }
                            catch(Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getApplicationContext(),"Some error occured!",Toast.LENGTH_SHORT).show();
                }
            });

            q2.add(stringRequest);


            // button
            submitButton = (Button)findViewById(R.id.submitPref);
            submitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(genresselected == 0 || actorselected == 0) {
                        Toast.makeText(getApplicationContext(),"Please select at least one field!",Toast.LENGTH_SHORT).show();
                    }
                    else if(genresselected < 0 || actorselected < 0) {
                        Toast.makeText(getApplicationContext(),"Some error occured, please go back and try again!",Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Log.d("Here","Sending request");
//                    Toast.makeText(getApplicationContext(),"Preferences updated!",Toast.LENGTH_SHORT).show();
                        String genresName[] = new String[genresselected];
                        int ctr = 0;
                        for(int i=0;i<prefView.getChildCount();i++) {
                            CheckBox c = (CheckBox)prefView.getChildAt(i);
                            if(c.isSelected()) {
                                genresName[ctr] = c.getText().toString();
                                ctr++;
                            }
                        }

                        int actorsID[] = new int[actorselected];
                        ctr = 0;
                        for(int i=0;i<actorView.getChildCount();i++) {
                            CheckBox c = (CheckBox)actorView.getChildAt(i);
                            if(c.isSelected()) {
                                actorsID[ctr] = i;
                                ctr++;
                            }
                        }


//                         access token is good
                        AccessToken accessToken = AccessToken.getCurrentAccessToken();
                        if(accessToken == null || accessToken.getToken() == null  || accessToken.isExpired()) {
                            Log.e("ERROR","null token");
                            LoginManager.getInstance().logOut();
                        }
                        else {
                            Log.e("Else part! You have to come!","tag tag tag");
                            DetailsSender detailsSender = new DetailsSender(f);
                            new Thread(detailsSender,"detailsSender").start();
                        }

                    }
                }
            });

        }
    }


    @Override
    public void onBackPressed()
    {
        // code here to show dialog
        LoginManager.getInstance().logOut();
        finish();
    }

    public class DetailsSender implements Runnable {

        public String final_fbid;

        public DetailsSender(String k) {
            final_fbid = k;
        }

        @Override
        public void run() {

            ID = fb_id;

            Log.d(ID,"FB ID here -");
            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
            String url ="http://10.196.4.62:8000/MovieApp"+"/createUser";
//            Toast.makeText(getApplicationContext(),"Gonna send Create user request!",Toast.LENGTH_SHORT).show();

            // Request a string response from the provided URL.
            Log.d("URL: ",url);
            // Request a string response from the provided URL.
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            // Display the first 500 characters of the response string.
                                try {
                                    Log.d(response.toString(),"RES");
                                    JSONObject re = new JSONObject(response);
                                    String s = re.getString("status");
                                    if(s.equals("yes")) {
                                        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                    else {
                                        Log.e("ERROR","ERROR");
                                        LoginManager.getInstance().logOut();
                                        finish();
                                    }
                                }
                                catch(Exception e) {
                                    Toast.makeText(getApplicationContext(),"Error in sending!",Toast.LENGTH_SHORT).show();
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
                    Map<String,String> mParams = new HashMap<String,String>();
                    mParams.put("fb_id",final_fbid);

                    return mParams;
                }
            };
//             Add the request to the RequestQueue.
            queue.add(stringRequest);


//            JSONObject params = new JSONObject();
//            try {
//                params.put("fb_id", final_fbid);
//                Log.e("FB  ID", final_fbid);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//
//            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
//                    url, params, //Not null.
//                    new Response.Listener<JSONObject>() {
//
//                        @Override
//                        public void onResponse(JSONObject response) {
//                            Log.d("response ", response.toString());
//                        }
//                    }, new Response.ErrorListener() {
//
//                @Override
//                public void onErrorResponse(VolleyError error) {
//                    VolleyLog.d("Error: ", "Error: " + error.getMessage());
//                    //pDialog.hide();
//                }
//            });
//
//            queue.add(jsonObjReq);

        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pref);
        actorselected = 0;
        genresselected = 0;

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

                            layoutparams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                            checkboxparams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

                            PrefGetter prefGetter = new PrefGetter(fb_id);
                            new Thread(prefGetter,"p").start();

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

        // App code
//        ProfileTracker mProfileTracker = new ProfileTracker() {
//            @Override
//            protected void onCurrentProfileChanged(Profile profile, Profile profile2) {
//                Log.v("facebook - profile", profile2.getFirstName());
//                Log.v("facebook - profile", profile2.getId());
//                fb_id = profile.getId();
//                this.stopTracking();
////                        AccessToken accessToken = loginResult.getAccessToken();
////                        Profile profile = Profile.getCurrentProfile();//Or use the profile2 variable
//            }
//        };
//        mProfileTracker.startTracking();
//        fb_id = getIntent().getExtras().getString("fb_id");


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

}
