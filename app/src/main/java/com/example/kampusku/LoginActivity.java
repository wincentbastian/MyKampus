package com.example.kampusku;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.kampusku.Admin.AdminActivity;
import com.example.kampusku.Admin.AdminLoginActivity;
import com.example.kampusku.ApiHelper.BaseApiHelper;
import com.example.kampusku.ApiHelper.UtilsApi;
import com.example.kampusku.Database.AppDatabase;
import com.example.kampusku.Database.AppExecutors;
import com.example.kampusku.Model.User;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static java.sql.Types.NULL;

public class LoginActivity extends AppCompatActivity {


    final private String FCM_API = "https://fcm.googleapis.com/fcm/send";
    final private String serverKey = "key=" + "AAAAs1TuAOU:APA91bGjZpJAiQUkR5v9nEGaov2awjFB8FU5OONt4hR_kTbUrx8zguzlMdS3bAe4VxE0jfxY4PZBJ2ceNJXgL61vagyZa_Y7CDqOhmj6nAXKqdzxWZk_3lN9XkwqWikHg13ZF-0RbrvT";
    final private String contentType = "application/json";
    final String TAG = "NOTIFICATION TAG";

    String NOTIFICATION_TITLE;
    String NOTIFICATION_MESSAGE;
    String TOPIC;



    TextView regis,to_admin;
    EditText etEmail;
    EditText etPassword;
    CircularProgressButton btnLogin;
    ProgressDialog loading;
    SharedPreferences sharedPreferences;
    SharedPreferences sharedPreferences2;
    Context mContext;
    BaseApiHelper mApiService;
    boolean session = false;
    TokenManager tokenManager;
    String token;
    Integer id_user;
    Integer admin;
    final String SHARED_PREFERENCES_NAME = "shared_preferences";
    final String SHARED_PREFERENCES_test = "shared_preferences";
    final String SESSION_STATUS = "session";
    public final static String TAG_TOKEN = "token";
    public final static Integer TAG_ID = 0;
    public final static Integer TAG_ADMIN = 0;
    AppDatabase mDb;
    User user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        mContext = this;
        mApiService = UtilsApi.getAPIService(); // meng-init yang ada di package apihelper
        sharedPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        session = sharedPreferences.getBoolean(SESSION_STATUS, false);
        token = sharedPreferences.getString(TAG_TOKEN, null);
        id_user = sharedPreferences.getInt(String.valueOf(TAG_ID),0);
        admin = sharedPreferences.getInt(String.valueOf(TAG_ADMIN),0);
        Log.d("asasa", "onCreate: admin  "+admin);

        TOPIC = "/topics/topic"; //topic must match with what the receiver subscribed to
        NOTIFICATION_TITLE = "Notifikasi";
        NOTIFICATION_MESSAGE = "isi, notifikasi";

        JSONObject notification = new JSONObject();
        JSONObject notifcationBody = new JSONObject();
        try {
            notifcationBody.put("title", NOTIFICATION_TITLE);
            notifcationBody.put("message", NOTIFICATION_MESSAGE);

            notification.put("to", TOPIC);
            notification.put("data", notifcationBody);
        } catch (JSONException e) {
            Log.e(TAG, "onCreate: " + e.getMessage() );
        }
        sendNotification(notification);

        startService(new Intent(getApplicationContext(),MyFirebaseMessagingService.class));
        FirebaseMessaging.getInstance().subscribeToTopic("topic");


        if (admin==1){
            Intent intent = new Intent(LoginActivity.this, AdminActivity.class);
            intent.putExtra(TAG_TOKEN, token);
            intent.putExtra(String.valueOf(TAG_ID),id_user);
            intent.putExtra(String.valueOf(TAG_ADMIN),admin);
            finish();
            startActivity(intent);
        }
        if(id_user!=NULL){
            Intent intent = new Intent(LoginActivity.this, BottomActivity.class);
            intent.putExtra(TAG_TOKEN, token);
            intent.putExtra(String.valueOf(TAG_ID),id_user);
            intent.putExtra(String.valueOf(TAG_ADMIN),admin);
            finish();
            startActivity(intent);
        }


        initComponents();
    }


    private void initComponents() {
        etEmail = (EditText) findViewById(R.id.iemail);
        etPassword = (EditText) findViewById(R.id.ipassword);
        btnLogin = (CircularProgressButton) findViewById(R.id.blogin);
        regis = (TextView) findViewById(R.id.regis);
        to_admin = (TextView) findViewById(R.id.pindah);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loading = ProgressDialog.show(mContext, null, "Harap Tunggu...", true, false);
                Login();
            }
        });

        regis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, RegisterActivity.class));
            }
        });

        to_admin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, AdminLoginActivity.class));
            }
        });
    }


    private void Login() {
        mApiService.loginRequest(etEmail.getText().toString(), etPassword.getText().toString())
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            loading.dismiss();
                            try {
                                JSONObject jsonRESULTS = new JSONObject(response.body().string());
                                if (jsonRESULTS.getString("status").equals("success")) {
                                    // Jika login berhasil maka data nama yang ada di response API
                                    // akan diparsing ke activity selanjutnya.

                                    String sukses = jsonRESULTS.getJSONObject("data").getString("token");
                                    Log.d("wanjay", "onResponse: "+sukses);
                                    String name = jsonRESULTS.getJSONObject("user").getString("name");
                                    int id = jsonRESULTS.getJSONObject("user").getInt("id");
                                    Log.d("id_user", "onResponse: "+id);
                                    int is_admin = jsonRESULTS.getJSONObject("user").getInt("is_admin");
                                    Log.d("admin", "onResponse: "+is_admin);
                                    token = sukses;
                                    if (id!=NULL){
                                        admin = is_admin;
                                        Log.d("admin", "onanjay "+admin);
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putBoolean(SESSION_STATUS, true);
                                        editor.putString(TAG_TOKEN, token);
                                        editor.putInt(String.valueOf(TAG_ADMIN),admin);
                                        editor.putInt(String.valueOf(TAG_ID), jsonRESULTS.getJSONObject("user").getInt("id"));
                                        editor.apply();
                                        Toast.makeText(mContext, "ID ANDA " + id, Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(mContext, BottomActivity.class);
                                        intent.putExtra("name", name);
                                        startActivity(intent);
                                        finish();
                                    }else {
                                        Toast.makeText(mContext, "Mungkin anda admin" , Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    // Jika login gagal
                                    Toast.makeText(mContext, "EEROOR", Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            loading.dismiss();
                        }
                    }
                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e("debug", "onFailure: OFFLINE > " + t.toString());
                        Toast.makeText(mContext, "OFFLINE", Toast.LENGTH_SHORT).show();
                        mDb = AppDatabase.getDatabase(getApplicationContext());
                        AppExecutors.getInstance().diskIO().execute(new Runnable() {
                            @Override
                            public void run() {
                                user = mDb.userDao().loginUser(etEmail.getText().toString(), etPassword.getText().toString());
                                int id_user = user.getId();
                                Log.e("test", "run: "+id_user);
                                if (id_user > 0 ){
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putBoolean(SESSION_STATUS, true);
                                    editor.putInt(String.valueOf(TAG_ID), user.getId());
                                    editor.putInt(String.valueOf(TAG_ADMIN),user.getIs_admin());
                                    editor.commit();
                                    Intent i = new Intent(getApplicationContext(), BottomActivity.class);
                                    startActivity(i);
                                }
                            }
                        });
                        loading.dismiss();
                    }

                });
    }
    private void sendNotification(JSONObject notification) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(FCM_API, notification,
                new com.android.volley.Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(TAG, "onResponse: " + response.toString());

                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(LoginActivity.this, "Request error", Toast.LENGTH_LONG).show();
                        Log.i(TAG, "onErrorResponse: Didn't work");
                    }
                }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", serverKey);
                params.put("Content-Type", contentType);
                return params;
            }
        };
        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest);
    }
}
