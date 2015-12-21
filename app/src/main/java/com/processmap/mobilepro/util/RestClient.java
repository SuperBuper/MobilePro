package com.processmap.mobilepro.util;

import android.util.Log;

import com.processmap.mobilepro.modules.common.ConfigurationProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.net.URL;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import javax.net.ssl.HttpsURLConnection;

public class RestClient {

    private ArrayList <KeyValueObject> params;
    private ArrayList <KeyValueObject> headers;

    public String serviceURL = null;
    public String requestData = null;

    public Map<String, List<String>> responseHeaders;

    public int responseCode = -1;
    public String responseData = null;
    public String errorMessage = null;


    public RestClient(String url) {
        serviceURL = url;

        params = new ArrayList<KeyValueObject>();
        headers = new ArrayList<KeyValueObject>();

        responseCode = -1;
        responseData = "";
        errorMessage = "";
    }

    public enum RequestMethod {
        GET, POST
    }

    public void AddParam(String name, String value) {
        params.add(new KeyValueObject(name, value));
    }

    public void AddHeader(String name, String value) {
        headers.add(new KeyValueObject(name, value));
    }

    public void Execute(RequestMethod method) throws Exception {

        switch(method) {
            case GET:
            {
                String combinedParams = "";
                if(!params.isEmpty()) {
                    combinedParams += "?";
                    for(KeyValueObject p : params) {
                        String paramString = p.name() + "=" + URLEncoder.encode(p.value(),"UTF-8");
                        if(combinedParams.length() > 1) {
                            combinedParams  +=  "&" + paramString;
                        } else {
                            combinedParams += paramString;
                        }
                    }
                }

                URL url = new URL("https://" + serviceURL + combinedParams);
                HttpsURLConnection request = (HttpsURLConnection) url.openConnection();
                request.setRequestMethod("GET");

                for(KeyValueObject h : headers) {
                    request.setRequestProperty(h.name(), h.value());
                }

                executeRequest(request);
                break;
            }
            case POST:
            {
                URL url = new URL("https://" + serviceURL);
                HttpsURLConnection request = (HttpsURLConnection) url.openConnection();
                request.setRequestMethod("POST");

                request.setDoOutput(true);

                for(KeyValueObject h : headers) {
                    request.setRequestProperty(h.name(), h.value());
                }

                if(!requestData.isEmpty()) {
                    OutputStream os = request.getOutputStream();
                    os.write(requestData.getBytes());
                    os.flush();
                }

                executeRequest(request);
                break;
            }
        }
    }

    private void executeRequest(HttpsURLConnection request)
    {
        try {
            //set timeouts to 15 seconds
            request.setConnectTimeout(15 * 1000);
            request.setReadTimeout(15 * 1000);

            responseCode = request.getResponseCode();
            responseHeaders = request.getHeaderFields();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader((request.getInputStream())));
                responseData = new Scanner(br).useDelimiter("\\A").next();
                Log.d(this.getClass().getName(), responseData);
            } else if (responseCode == HttpURLConnection.HTTP_INTERNAL_ERROR) {
                BufferedReader br = new BufferedReader(new InputStreamReader((request.getInputStream())));
                responseData = new Scanner(br).useDelimiter("\\A").next();
                Log.d(this.getClass().getName(), responseData);
            }
        } catch (IOException e) {
            responseCode = ConfigurationProvider.RESPONSE_HTTP_ERROR;
            responseData = "";
            errorMessage = e.getMessage();
            Log.d(this.getClass().getName(), errorMessage);
        } finally {
            if (request != null) {
                request.disconnect();
            }
        }
    }
}

