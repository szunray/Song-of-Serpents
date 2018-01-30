package com.example.mamajama.androidgame;

import android.content.ContentValues;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
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
        Log.d("QUERY", "URL is "+myURL);

        URL url = new URL(myURL);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

        try {
            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod("POST");
            //urlConnection.setDoInput(true);
            OutputStream out= new BufferedOutputStream(urlConnection.getOutputStream());


            HashMap<String,String> values = new HashMap<String, String>();
            values.put("Pawn",request.get("Pawn"));
            values.put("X",request.get("X"));
            values.put("Y", request.get("Y"));
            String data= getQuery(values);
            Log.d("HTPOST", "Sending "+data);
            out.write(data.getBytes());
            out.flush();


            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            if(in != null) {
                Log.d("HTPOST","Received "+convertInputStreamToString(in));
            }




        }
        finally {
            urlConnection.disconnect();
        }
    }


    private String HttpGet(String myUrl) throws IOException {
        Log.d("HTGET", "Made it to GET call");
        InputStream inputStream = null;
        String result = "";

        URL url = new URL(myUrl);
        Log.d("HTGET", "URL =  "+myUrl);
        // create HttpURLConnection
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        Log.d("HTGET", "Successfully opened connection");
        // make GET request to the given URL
        //conn.connect();


        // receive response as inputStream
        inputStream = conn.getInputStream();
        Log.d("HTGET", "connection had input Stream");
        // convert inputstream to string
        if(inputStream != null) {
            result = convertInputStreamToString(inputStream);
            Log.d("HTGET", "We got something");
        }
        else{
            result = "Did not work!";
            Log.d("HTGET", "We got nothing");
        }


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

        Log.d("QUERY", result.toString());
        return result.toString();
    }
}
