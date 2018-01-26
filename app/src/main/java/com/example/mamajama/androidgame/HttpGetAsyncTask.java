package com.example.mamajama.androidgame;

import android.content.ContentValues;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Savaque on 1/23/2018.
 */

public class HttpGetAsyncTask extends AsyncTask<Object, Void, String> {
    @Override
    protected String doInBackground(Object... params) {

        if(params[0] instanceof String) {
            try {
                String url = (String)params[0];
                // This is getting the url from the string we passed in
                return HttpGet(url);
                // Create the urlConnection
            } catch (Exception e) {
                e.printStackTrace();
                return "Unable to retrieve network data.";

            }
        }
        if(params[0] instanceof Map) {
            HashMap<String,String> Map = (HashMap<String,String>)params[0];
            try{
                HttpPost(Map);
                return "Query Sent for Player "+Map.get("Pawn");
        }
        catch (Exception e){
                e.printStackTrace();
                return "recognized that this was a post call, but failed";
        }


    }
    else
        Log.d("FAIL", "Http function could not determine what to do");
        return "Total Failure";


    }
    @Override
    protected void onPostExecute(String result) {
        Log.d("SONG",result);
    }


    private void HttpPost(HashMap<String,String> request) throws IOException{
        String myURL = request.get("URL");
        OutputStream out=null;
        try {
            URL url = new URL(myURL);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);


            HashMap<String,String> values = new HashMap<String, String>();
            values.put("Pawn",request.get("Pawn"));
            values.put("X",request.get("X"));
            values.put("Y", request.get("Y"));
            //out = new BufferedOutputStream(urlConnection.getOutputStream());

            OutputStream os = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(getQuery(values));

            writer.flush();
            Log.d("HTPOST", "QUERY SENT");
            writer.close();
            os.close();

            urlConnection.connect();




        }
        catch(Exception e){
            e.printStackTrace();
        }
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

    // helps with HTTP Post method
    private String getQuery(HashMap<String,String> params) throws UnsupportedEncodingException
    {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (String entry: params.keySet())
        {
            if (first)
                first = false;
            else
                result.append("&");
            String key = entry;
            String value = params.get(entry);

            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value, "UTF-8"));
        }

        return result.toString();
    }
}
