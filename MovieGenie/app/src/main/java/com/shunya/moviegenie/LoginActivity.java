package com.shunya.moviegenie;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.Manifest.permission.READ_CONTACTS;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private LoginButton loginButton;
    AccessToken accessToken;
    CallbackManager callbackManager;
    public String fb_id;

    public class Logger implements Runnable {

        @Override
        public void run() {
            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
            String url = "http://10.196.4.62:8000/MovieApp/checkUser";
//                String url = R.string.api_url + "/MovieApp/checkUser";
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                Log.e(response,"response");
                                String status = jsonObject.getString("status");
                                Intent intent;
                                if(status.equals("yes")) {
                                    intent = new Intent(getApplicationContext(),MainActivity.class);
                                }
                                else {
                                    intent = new Intent(getApplicationContext(),PrefActivity.class);
                                }
                                intent.putExtra("fb_id",fb_id);
                                startActivity(intent);
                                finish();
                            }
                            catch(Exception e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                            Toast.makeText(getApplicationContext(),"Some error occured!",Toast.LENGTH_SHORT).show();
                        }
                    }){
                @Override
                public Map<String, String> getParams() {
                    Map<String,String> mParams = new HashMap<String, String>();
                    Log.e("FB ID inside send",fb_id);
                    mParams.put("fb_id",fb_id);
                    return mParams;
                }
            };
            // Add the request to the RequestQueue.
            requestQueue.add(stringRequest);
            Log.e("LOGGED","logged");
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        setContentView(R.layout.activity_login);

        // facebook sdk initialized!
        // check for login
        callbackManager = CallbackManager.Factory.create();
        loginButton = (LoginButton)findViewById(R.id.login_button);

        loginButton.setReadPermissions("email");
        accessToken = AccessToken.getCurrentAccessToken();

        if(accessToken != null) {
            if (accessToken.getToken() != null && !accessToken.isExpired()) {
                Log.d("STARTING MAIN:","main");
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        }

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
               if(Profile.getCurrentProfile() == null) {
                   // App code
                   ProfileTracker mProfileTracker = new ProfileTracker() {
                       @Override
                       protected void onCurrentProfileChanged(Profile profile, Profile profile2) {
                           Log.v("facebook - profile", profile2.getFirstName());
                           Log.v("facebook - profile", profile2.getId());
                           fb_id = profile2.getId();
                           this.stopTracking();
//                        AccessToken accessToken = loginResult.getAccessToken();
//                        Profile profile = Profile.getCurrentProfile();//Or use the profile2 variable
                       }
                   };
                   mProfileTracker.startTracking();
               }
                else {
                   Profile profile = Profile.getCurrentProfile();
                   fb_id = profile.getId();
                   Log.e(profile.getId(),"IDdd");

                   Logger logger = new Logger();
                   new Thread(logger,"logger").start();
               }

            }

            @Override
            public void onCancel() {
                Log.e("cancelled","cancelled");
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
            }
        });

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}

