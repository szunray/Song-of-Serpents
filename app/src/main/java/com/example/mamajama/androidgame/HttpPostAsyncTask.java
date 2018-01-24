package com.example.mamajama.androidgame;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Savaque on 1/23/2018.
 */

public class HttpPostAsyncTask extends AsyncTask<String, Void, String> {
    @Override
    protected String doInBackground(String... params) {

        try {
            // This is getting the url from the string we passed in
            return HttpGet(params[0]);
            // Create the urlConnection
        } catch (Exception e) {
            return "Unable to retrieve network data.";
        }

    }

    @Override
    protected void onPostExecute(String result) {
        Log.d("SONG",result);
    }



    private String HttpGet(String myUrl) throws IOException {
        InputStream inputStream = null;
        String result = "";

        URL url = new URL(myUrl);

        // create HttpURLConnection
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        // make GET request to the given URL
        conn.connect();

        // receive response as inputStream
        inputStream = conn.getInputStream();

        // convert inputstream to string
        if(inputStream != null)
            result = convertInputStreamToString(inputStream);
        else
            result = "Did not work!";

        return result;
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }
}
