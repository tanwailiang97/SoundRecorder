package com.example.soundrecorder;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DialogFragment extends androidx.fragment.app.DialogFragment {

    private static final String TAG = "DialogFragment";

    private EditText etUserName;
    private EditText etPassword;
    private TextView tvSave;
    private SharedPreferences mPreferences;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.diaog_fragment,container,false);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        etUserName = view.findViewById(R.id.etEnterUserName);
        etPassword = view.findViewById(R.id.etEnterPassword);
        tvSave = view.findViewById(R.id.DialogUpdate);

        String mUserName = mPreferences.getString("UserName","");
        String mPassword = mPreferences.getString("Password","");

        etUserName.setText(mUserName);
        etPassword.setText(mPassword);

        tvSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Update Pressed");
                String userName = etUserName.getText().toString();
                String password = etPassword.getText().toString();

                if(!userName.equals("")&&!password.equals("")){
                    ((MainActivity)getActivity()).preferenceEdit(userName,password);
                    Log.d(TAG, "onClick: Info Updated");
                }
                getDialog().dismiss();
            }
        });

        return view;
    }


}

