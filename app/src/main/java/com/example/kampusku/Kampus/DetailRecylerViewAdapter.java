package com.example.kampusku.Kampus;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.kampusku.ApiHelper.BaseApiHelper;
import com.example.kampusku.ApiHelper.UtilsApi;
import com.example.kampusku.BottomActivity;
import com.example.kampusku.LoginActivity;
import com.example.kampusku.MyFirebaseMessagingService;
import com.example.kampusku.MySingleton;
import com.example.kampusku.R;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailRecylerViewAdapter extends RecyclerView.Adapter<DetailRecylerViewAdapter.ViewHolder> {

    final private String FCM_API = "https://fcm.googleapis.com/fcm/send";
    final private String serverKey = "key=" + "AAAAs1TuAOU:APA91bGjZpJAiQUkR5v9nEGaov2awjFB8FU5OONt4hR_kTbUrx8zguzlMdS3bAe4VxE0jfxY4PZBJ2ceNJXgL61vagyZa_Y7CDqOhmj6nAXKqdzxWZk_3lN9XkwqWikHg13ZF-0RbrvT";
    final private String contentType = "application/json";
    final String TAG = "NOTIFICATION TAG";

    String NOTIFICATION_TITLE;
    String NOTIFICATION_MESSAGE;
    String TOPIC;


    private Context context;
    private List<ResultDetail> results;
    BaseApiHelper Apihelper;
    int id_user;
    final String SHARED_PREFERENCES_NAME = "shared_preferences";
    public final static Integer TAG_ID = 0;
    SharedPreferences sharedPreferences;

    public DetailRecylerViewAdapter(Context context,List<ResultDetail> results) {
        this.context = context;
        this.results = results;

    }
    @NonNull
    @Override
    public DetailRecylerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_detail, viewGroup, false);
        DetailRecylerViewAdapter.ViewHolder holder = new DetailRecylerViewAdapter.ViewHolder(v);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull DetailRecylerViewAdapter.ViewHolder holder, final int position) {
        final ResultDetail result = results.get(position);
        holder.Prodi.setText(result.getNama_prodi());
        holder.Deskripsi.setText(result.getTentang_prodi());
        holder.Fakultas.setText(result.getNama_fakultas());
        holder.UKT.setText(String.valueOf(result.getBiaya()));


    }


    @Override
    public int getItemCount() {
        return results.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView Prodi;
        TextView Deskripsi;
        TextView Fakultas;
        TextView UKT;


        public ViewHolder(View itemView) {
            super(itemView);
            Prodi = itemView.findViewById(R.id.prodi);
            Deskripsi = itemView.findViewById(R.id.tentang_prodi);
            Fakultas = itemView.findViewById(R.id.fakultas);
            UKT = itemView.findViewById(R.id.biaya);
            Apihelper = UtilsApi.getAPIService();
            sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
            id_user = sharedPreferences.getInt(String.valueOf(TAG_ID),0);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final SweetAlertDialog sDialog = new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE);
                    sDialog.setTitle("Daftar via Kampusku");
                    sDialog.setContentText("Ingin Daftar di " + results.get(getAdapterPosition()).getNama_prodi() + " ?");
                    sDialog.setConfirmButton("Ya", new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            Apihelper.Daftar(results.get(getAdapterPosition()).getNama_univ(),20000,id_user,
                                    results.get(getAdapterPosition()).getId_prodi()).
                                    enqueue(new Callback<ResponseBody>() {
                                        @Override
                                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                            if (response.isSuccessful()) {
                                                JSONObject jsonRESULTS = null;
                                                try {
                                                    jsonRESULTS = new JSONObject(response.body().string());
                                                    if (jsonRESULTS.getString("status").equals("success")) {
                                                        Log.i("debug", "onResponse: BERHASIL");
                                                        Toast.makeText(context, "BERHASIL DAFTAR KAMPUS", Toast.LENGTH_SHORT).show();


                                                        TOPIC = "/topics/topic"; //topic must match with what the receiver subscribed to
                                                        NOTIFICATION_TITLE = "Pengumuman";
                                                        NOTIFICATION_MESSAGE = "Ada Yang Tersesat";


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



                                                        Intent mIntent = new Intent(context, BottomActivity.class);
                                                        context.startActivity(mIntent);
                                                    }else if(jsonRESULTS.getString("status").equals("error")){
                                                        Toast.makeText(context, "Anda Sudah Terdaftar", Toast.LENGTH_SHORT).show();
                                                    }
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }

                                            } else {
                                                Toast.makeText(context, "Gagal", Toast.LENGTH_SHORT).show();
                                                Log.i("debug", "onResponse: GA BERHASIL");
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                                            Log.e("debug", "onFailure: ERROR > " + t.getMessage());
                                            Toast.makeText(context, "Koneksi Internet Bermasalah", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                            sDialog.dismissWithAnimation();
                        }
                    });
                    sDialog.setCancelButton("Tidak", new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sDialog.dismissWithAnimation();
                        }
                    });
                    sDialog.show();
                }
            });
        }
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
                        Toast.makeText(context, "Request error", Toast.LENGTH_LONG).show();
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
        MySingleton.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }
}
