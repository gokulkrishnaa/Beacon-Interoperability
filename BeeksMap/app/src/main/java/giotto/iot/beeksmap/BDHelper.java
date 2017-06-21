package giotto.iot.beeksmap;


import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;;
import com.bluvision.beeks.sdk.domainobjects.Beacon;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;


public class BDHelper {

    String bd_url = "https://bd-exp.andrew.cmu.edu:81";
    String url;
    String client_id = "HMGHejNnjfgAIWyHAwM3L2YXjX9CAVnIkGkCthbm";
    String client_secret = "x5vJMZtiDyt5K3NdnDrBz4DIrGMAvFjpzJQOeWopd6TOJ6nlep";
    String access_url = bd_url+"/oauth/access_token/client_id="+client_id+"/client_secret="+client_secret;

    String access_token = null;
    String post_body = null;


    Context mContext;
    JSONObject beacon_global;
    String mac;
    boolean isBDArray = false;

    public boolean isBDBeacon(Context context, Beacon beacon) throws InterruptedException, ExecutionException, IOException, JSONException {
        mContext = context;

        access_token = getAccessToken();
        postAsync post = new postAsync();
        url =  bd_url + "/api/search";

        if(beacon != null)
            post_body = "{\"Tags\":[\"mac:"+beacon.getDevice().toString()+"\"]}";
        else
            post_body = "{\"Tags\":[\"mac:"+mac+"\"]}";

        String beacon_resp = post.execute().get();
        JSONObject beacon_json = new JSONObject(beacon_resp);
        if(beacon_json.get("success").equals("True")){
            if(beacon_json.get("result").toString().equals("[]"))
                return false;
            else {
                beacon_global = beacon_json;
                return true;
            }
        }

        return false;
    }

    public String getUUID(Context context, String macAddr) throws InterruptedException, ExecutionException, JSONException, IOException {
        String uuid = null;
        mac = macAddr;
        boolean isBeacon = isBDBeacon(context, null);
        if(isBeacon)
        {
            JSONObject result = (JSONObject) beacon_global.getJSONArray("result").get(0);
            uuid = result.getString("name");
//            Log.d("building", uuid);
        }

        return uuid;
    }

    public String getLatLong(Context context, String macAddr) throws InterruptedException, ExecutionException, JSONException, IOException{
        String latlong = null;
        mac = macAddr;
        boolean isBeacon = isBDBeacon(context, null);
        if(isBeacon)
        {
            JSONObject result = (JSONObject) beacon_global.getJSONArray("result").get(0);
            JSONArray tags = result.getJSONArray("tags");
            int length = tags.length();
            for (int i = 0; i<length; i++){
                JSONObject temp = (JSONObject) tags.get(i);
                String key = temp.getString("name");
                String val = temp.getString("value");
                if(key.equals("latlong"))
                    latlong = val;
            }
        Log.d("building", latlong + "\t" + mac);
        }

        return latlong;
    }

    public void updateBeacon(Context context, JSONObject beacon_details) throws JSONException, ExecutionException, InterruptedException, IOException {
        JSONObject old_tags = null;
        String uuid = beacon_details.getString("uuid");
        mac = beacon_details.getString("mac");
        access_token = getAccessToken();

        // Get Old Tags from BD
        boolean isBeacon = isBDBeacon(context, null);

        if(isBeacon)
        {
            JSONObject result = (JSONObject) beacon_global.getJSONArray("result").get(0);
            old_tags = getOldTags(result);
        }

        postAsync post = new postAsync();
        url =  bd_url + "/api/sensor/"+beacon_details.get("uuid")+"/tags";
        beacon_details.remove("uuid");
        int length = beacon_details.length();
        isBDArray = true;

        String full = "[";

        Iterator<String> keys = beacon_details.keys();

        while (keys.hasNext()){
            String key = keys.next();
            String val = beacon_details.getString(key);
            String old_val = old_tags.getString(key);
            String temp = null;

            val = compareValues(val, old_val);

            if (length != 1) {
                temp = "{\"name\":\""+key+"\",\"value\":\""+val+"\"},";
            }
            else{
                temp = "{\"name\":\""+key+"\",\"value\":\""+val+"\"}";
            }

            length = length-1;
            full = full + temp;
        }
        full+="]";

        post_body = full;
//        Log.d("building", post_body);

        String beacon_update = post.execute().get();
//        Log.d("building", beacon_details.getString("mac") + "\n" + beacon_update);

    }

    private class getAsync extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(Void... params) {
            StringBuilder sb = new StringBuilder();
            try {
                URL requestUrl = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) requestUrl.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-length", "0");
                conn.setUseCaches(false);
                conn.setAllowUserInteraction(false);
                conn.setConnectTimeout(100000);
                conn.setReadTimeout(100000);

                conn.connect();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return String.valueOf(sb);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.d("Post", "Finished");
            return;
        }
    }

    private class postAsync extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(Void... params) {
            StringBuilder sb = new StringBuilder();
            try {
                URL requestUrl = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) requestUrl.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-type", "application/json");
                conn.setRequestProperty("charset", "utf-8");
                conn.setRequestProperty("Authorization", "Bearer "+ access_token);
                conn.setUseCaches(false);
                conn.setDoOutput(true);
                conn.setAllowUserInteraction(false);
                conn.setConnectTimeout(100000);
                conn.setReadTimeout(100000);


                JSONObject postDataParams = new JSONObject();
                if(isBDArray) {
                    isBDArray = false;
                    JSONArray tags = new JSONArray(post_body);
                    postDataParams.put("data", tags);
                }
                else {
                    JSONObject tags = new JSONObject(post_body);
                    postDataParams.put("data", tags);
                }


                PrintWriter out = new PrintWriter(conn.getOutputStream());
                out.print(postDataParams);
                out.close();

                conn.connect();

                int responseCode=conn.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {

                    Scanner inStream = new Scanner(conn.getInputStream());

                    while(inStream.hasNextLine())
                        sb.append(inStream.nextLine());
                    return sb.toString();
                }
                else {
                    return "false : " + responseCode;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return String.valueOf(sb);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.d("Post", "Finished");
            return;
        }
    }

    private String getAccessToken() throws ExecutionException, InterruptedException, JSONException {
        url = access_url;
        getAsync get = new getAsync();
        String access_response = get.execute().get();
        JSONObject access_json = new JSONObject(access_response);
        return access_json.getString("access_token");
    }

    private JSONObject getOldTags(JSONObject obj) throws JSONException {
        JSONObject json_oldTag = new JSONObject();
        JSONArray tags = obj.getJSONArray("tags");
        int length = tags.length();
        for (int i = 0; i<length; i++){
            JSONObject temp = (JSONObject) tags.get(i);
            String key = temp.getString("name");
            String val = temp.getString("value");
            json_oldTag.put(key, val);
        }
        return json_oldTag;
    }

    private String compareValues(String newVal, String oldVal){
        if(newVal.equals(""))
            return oldVal;
        if(oldVal.equals(""))
            return newVal;
        if(newVal.equals(oldVal))
            return oldVal;
        return newVal;
    }

}

