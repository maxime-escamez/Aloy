package com.aloy.aloy;

/**
 * Created by Max on 29/09/2017.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.aloy.aloy.Models.MainUser;
import com.aloy.aloy.Util.CredentialsHandler;
import com.aloy.aloy.Util.DataHandler;
import com.aloy.aloy.Util.SharedPreferenceHelper;
import com.aloy.aloy.Util.SpotifyHandler;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.HttpsURLConnection;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyCallback;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.UserPrivate;
import retrofit.client.Response;

import static com.aloy.aloy.MainActivity.service;

public class LoginActivity extends Activity {

    private static final String TAG = LoginActivity.class.getSimpleName();
    @SuppressWarnings("SpellCheckingInspection")
    private static final String CLIENT_ID = "5d7de5e2301441bcba0277763af5c7fe";
    @SuppressWarnings("SpellCheckingInspection")
    private static final String REDIRECT_URI = "aloyprotocol://callback";
    @SuppressWarnings("SpellCheckingInspection")
    private static String AUTHCODE = "";
    private static String refresh_token = null;
    public static String access_token = null;
    public static String firebase_token=null;
    private FirebaseDatabase database;
    private FirebaseAuth mAuth;
    private static final int REQUEST_CODE = 1337;
    private static int a=1;
    private SpotifyHandler spotifyHandler;
    private SharedPreferenceHelper mSharedPreferenceHelper;

    public LoginActivity() {
    }


    public static Intent createIntent(Context context) {
        return new Intent(context, MainActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        mSharedPreferenceHelper = new SharedPreferenceHelper(this);
        refresh_token = mSharedPreferenceHelper.getCurrentSpotifyToken();
        String token = CredentialsHandler.getAccessToken(this);

        if (token==null||a==1) {
            if(refresh_token==null||a==1) {
                Log.i("Token State","Never logged in");
                setContentView(R.layout.activity_login);
            }else{
                try {
                    Log.i("Token State","Expired");
                    new refreshToAccess().execute().get();
                    CredentialsHandler.setAccessToken(this, access_token, 3600, TimeUnit.SECONDS);
                    /*mAuth.signInWithCustomToken(firebase_token)
                            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.d(TAG, "signInWithCustomToken:success");
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        startMainActivity(CredentialsHandler.getAccessToken(LoginActivity.this), CredentialsHandler.getExpiresAt(LoginActivity.this));
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w(TAG, "signInWithCustomToken:failure", task.getException());
                                    }
                                }
                            });*/
                    startMainActivity(CredentialsHandler.getAccessToken(LoginActivity.this), CredentialsHandler.getExpiresAt(LoginActivity.this));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        } else {
            Log.i("Token State","Not yet expired");
            startMainActivity(token,CredentialsHandler.getExpiresAt(this));
        }
    }

    public void onButton2Clicked(View view) {
        final AuthenticationRequest request = new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.CODE, REDIRECT_URI)
                .setScopes(new String[]{"user-read-currently-playing"})
                .build();
        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            switch (response.getType()) {
                // Response was successful and contains auth code
                case CODE:
                    AUTHCODE=response.getCode();
                    try {
                        new codeToRefresh().execute().get();
                        mSharedPreferenceHelper.saveSpotifyToken(refresh_token);
                        new refreshToAccess().execute().get();
                        CredentialsHandler.setAccessToken(this, access_token, 3600, TimeUnit.SECONDS);
                        spotifyHandler = new SpotifyHandler(CredentialsHandler.getAccessToken(this),this);
                        mAuth.signInWithCustomToken(firebase_token)
                                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            DatabaseReference mTheReference = FirebaseDatabase.getInstance().getReference();
                                            DatabaseReference mUsersReference = mTheReference.child("users").child(mAuth.getCurrentUser().getUid());
                                            mUsersReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    spotifyHandler.getMyInfo();
                                                    if (!dataSnapshot.exists()) {
                                                        spotifyHandler.createMainUser();
                                                    }
                                                    startMainActivity(CredentialsHandler.getAccessToken(LoginActivity.this),CredentialsHandler.getExpiresAt(LoginActivity.this));
                                                }
                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {
                                                        Log.w(TAG,"addListenerForSingleValueEvent:failure",databaseError.toException());
                                                }
                                            });
                                        } else {
                                            Log.w(TAG, "signInWithCustomToken:failure", task.getException());
                                        }
                                    }
                                });
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                    break;
                // Auth flow returned an error
                case ERROR:
                    logError("Auth error: " + response.getError());
                    break;
                // Most likely auth flow was cancelled
                default:
                    logError("Auth result: " + response.getType());
            }
        }
    }

    protected void startMainActivity(String token,long expires_at) {
        Intent intent = MainActivity.createIntent(this);
        intent.putExtra(MainActivity.EXTRA_TOKEN, token);
        intent.putExtra(MainActivity.EXTRA_EXPIRES_AT, expires_at);
        startActivity(intent);
        finish();
    }

    private void logError(String msg) {
        Toast.makeText(this, "Error: " + msg, Toast.LENGTH_SHORT).show();
        Log.e(TAG, msg);
    }


    //Asyncronous tasks
    public static class codeToRefresh extends AsyncTask<String, Void, String> {

        private String getPostDataString(JSONObject params) throws Exception {

            StringBuilder result = new StringBuilder();
            boolean first = true;

            Iterator<String> itr = params.keys();

            while(itr.hasNext()){

                String key= itr.next();
                Object value = params.get(key);

                if (first)
                    first = false;
                else
                    result.append("&");

                result.append(URLEncoder.encode(key, "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(value.toString(), "UTF-8"));

            }
            return result.toString();
        }

        protected void onPreExecute(){}

        protected String doInBackground(String... arg0) {
            try {
                URL url = new URL("https://us-central1-aloy-1995.cloudfunctions.net/refreshTokenDispenser");
                JSONObject postDataParams = new JSONObject();
                postDataParams.put("message", AUTHCODE);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(postDataParams));
                writer.flush();
                writer.close();
                os.close();

                int responseCode=conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {

                    BufferedReader in=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuffer sb = new StringBuffer("");
                    String line="";

                    while((line = in.readLine()) != null) {
                        sb.append(line);
                        break;
                    }

                    in.close();
                    refresh_token=sb.toString().replaceAll("^\"|\"$", "");
                    return null;
                }else{
                    Log.i("codeToRefresh error",responseCode+"");
                    return null;

                }

            } catch (Exception e) {
                Log.i("codeToRefresh Exception",e.getMessage());
                return null;
            }
        }
    }




    public static class refreshToAccess extends AsyncTask<String, Void, String> {


        private String getPostDataString(JSONObject params) throws Exception {

            StringBuilder result = new StringBuilder();
            boolean first = true;

            Iterator<String> itr = params.keys();

            while(itr.hasNext()){

                String key= itr.next();
                Object value = params.get(key);

                if (first)
                    first = false;
                else
                    result.append("&");

                result.append(URLEncoder.encode(key, "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(value.toString(), "UTF-8"));

            }
            return result.toString();
        }

        protected void onPreExecute(){}

        protected String doInBackground(String... arg0) {
            try {
                URL url = new URL("https://us-central1-aloy-1995.cloudfunctions.net/refresher");
                JSONObject postDataParams = new JSONObject();
                postDataParams.put("message", refresh_token);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(postDataParams));
                writer.flush();
                writer.close();
                os.close();

                int responseCode=conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {

                    BufferedReader in=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuffer sb = new StringBuffer("");
                    String line="";

                    while((line = in.readLine()) != null) {

                        sb.append(line);
                        break;
                    }

                    in.close();
                    JSONObject reader = new JSONObject(sb.toString());
                    access_token=reader.getString("spotify");
                    firebase_token=reader.getString("firebase");
                    access_token=access_token.substring(2, access_token.length()-2);
                    firebase_token=firebase_token.substring(2, firebase_token.length()-2);
                    return null;
                }else{
                    Log.i("refreshToAccess error",responseCode+"");
                    return null;

                }

            } catch (Exception e) {
                Log.i("rToAccess Exception",e.getMessage());
                return null;

            }

        }

    }

}