package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button clickButton = (Button) findViewById(R.id.button);
        clickButton.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivity(cameraIntent);

            }
        });

        Button clickUploadButton = (Button) findViewById(R.id.button2);
        clickUploadButton.setOnClickListener( new View.OnClickListener() {

            @TargetApi(Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                try {

                    URL url = new URL("http://10.0.2.2:5004/generate");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setDoInput(true);
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);
                    System.setProperty("http.keepAlive", "false");
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);
                    conn.connect();
                    OutputStream output = conn.getOutputStream();
                    File path = Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_PICTURES);
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;

                    File targetDir = new File("/mnt/sdcard/DCIM/Camera");
                    File[] files = targetDir.listFiles();
                    Bitmap bitmap = BitmapFactory.decodeFile(files[files.length - 1].getAbsolutePath());
                    byte[] data = null;
                    if(bitmap!=null){
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                        data = bos.toByteArray();
                    }
                    String image_str = Base64.encodeToString(data, Base64.DEFAULT);
//                    ArrayList nameValuePairs = new ArrayList();
//
//                    nameValuePairs.add(new BasicNameValuePair("image",image_str));
//
//                        HttpClient httpclient = new DefaultHttpClient();
//                        HttpPost httppost = new HttpPost("http://10.0.2.2:5004/generate");
//                        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
//                        HttpResponse response = httpclient.execute(httppost);
                    
                    String datajson =  "{\"file\": \""+image_str.trim()+"\"}";
                    Log.e("data","json:"+datajson);
                        //String the_string_response = convertResponseToString(response);
//                    Map<String, Object> map = new HashMap<>();
//                    map.put("image", data);
                  //  output.write(data);
                  //  output.flush();
                    output.write(datajson.getBytes("UTF-8"));
                    output.close();
                    Scanner result = new Scanner(conn.getInputStream());
                    String response = result.nextLine();
//                    Log.e("ImageUploader", "Error uploading image: " + response);
//                    result.close();
                    conn.disconnect();
                } catch (IOException e) {
                    Log.e("ImageUploader", "Error uploading image", e);
                }

            }
        });
    }
}
