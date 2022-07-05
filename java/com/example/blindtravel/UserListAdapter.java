package com.example.blindtravel;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.speech.tts.TextToSpeech;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.blindtravel.models.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.ViewHolder> {

    private ArrayList<String> users = new ArrayList<String>();
    private JSONArray fetched;
    private TextView name;
    private TextView mobile;
    private TextView destination;
    private ImageView profilePic;

    TextToSpeech textToSpeech;
    Context context;
    String role;

    public UserListAdapter(JSONArray fetched, Context context, String role) {
        this.fetched = fetched;
        this.context = context;
        this.role = role;
        Log.d("Adaptor Content",fetched.toString());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_card, parent, false);
        name = view.findViewById(R.id.name_card);
        mobile = view.findViewById(R.id.mobile_card);
        destination = view.findViewById(R.id.destination_card);

        profilePic = view.findViewById(R.id.picture_card);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String temp = "null";
        try {
            temp = fetched.getJSONObject(position).getString("destination");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(fetched==null || temp.equals("null") || temp.equals("")) {
            name.setText("Sample Name"+(position+1));
            mobile.setText("Mobile no. "+(position+1));
            destination.setText("Destination"+(position+1));
        }
        if(fetched!=null || !temp.equals("null") || !temp.equals("")) {
            try {
                name.setText(fetched.getJSONObject(position).getString("name"));
                JSONObject test = fetched.getJSONObject(position);
                mobile.setText(test.getString("mobile"));
                destination.setText(test.getString("destination"));

                String completeImageData = test.getString("profilePicturePath");
                String imageDataBytes = completeImageData.substring(completeImageData.indexOf(",")+1);
                InputStream stream = new ByteArrayInputStream(Base64.decode(imageDataBytes.getBytes(), Base64.DEFAULT));
                Bitmap bitmap = BitmapFactory.decodeStream(stream);
                profilePic.setImageBitmap(bitmap);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getItemCount() {
        return fetched.length();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
