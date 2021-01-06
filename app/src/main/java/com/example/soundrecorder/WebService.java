package com.example.soundrecorder;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.request.SimpleMultiPartRequest;

import java.io.File;

public class WebService {
    private static final String TAG = "WebService";
    private RequestQueue mRequestQueue;
    private static WebService apiRequests = null;

    public static WebService getInstance() {
        if (apiRequests == null) {
            apiRequests = new WebService();
            return apiRequests;
        }
        return apiRequests;
    }
    public void updateProfile(Context context,
                              String username,
                              String title,
                              String description,
                              File file,
                              Response.Listener<String> listener,
                              Response.ErrorListener errorListener) {
        String url = "https://a17kedjango.herokuapp.com/post/phone/" +
                "?title=" + title +
                "&content=" + description +
                "&username=" +username;
        Log.d(TAG, "updateProfile URL: " + url);
        SimpleMultiPartRequest request = new SimpleMultiPartRequest(Request.Method.POST,
                url,
                listener,
                errorListener);
//        request.setParams(data);
        mRequestQueue = RequestManager.getnstance(context);
//        request.addMultipartParam("parameter_1", "text", doc_name);
//        request.addMultipartParam("dparameter_2", "text", doc_type);
//        request.addMultipartParam("parameter_3", "text", appliance_id);
        request.addFile("audio", file.getPath());

        request.setFixedStreamingMode(true);
        mRequestQueue.add(request);
    }
}