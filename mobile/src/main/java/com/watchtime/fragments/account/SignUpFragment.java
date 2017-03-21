package com.watchtime.fragments.account;


import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.app.Fragment;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.watchtime.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class SignUpFragment extends Fragment {
    public static final String TAG = "SignUpFragment";

    private ImageView imageLogo;
    private EditText nameText;
    private EditText emailText;
    private EditText passwordText;
    private EditText repeatText;
    private Button signUpBtn;

    private ProgressDialog progressDialog;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message message) {
            Toast.makeText(getActivity(), message.obj.toString(), Toast.LENGTH_LONG).show();
            if (progressDialog != null)
                progressDialog.dismiss();
        }
    };

    public SignUpFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        String transitionName = "";

        if (bundle != null) {
            transitionName = bundle.getString("TRANS_NAME");
        }

        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);

        imageLogo = (ImageView) view.findViewById(R.id.sign_up_image_view);
        nameText = (EditText) view.findViewById(R.id.input_name);
        emailText = (EditText) view.findViewById(R.id.input_email);
        passwordText = (EditText) view.findViewById(R.id.input_password);
        repeatText = (EditText) view.findViewById(R.id.input_repeat_password);
        signUpBtn = (Button) view.findViewById(R.id.btn_sign_up);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            imageLogo.setTransitionName(transitionName);
        }

        registerClickListeners();

        return view;
    }

    public void registerClickListeners() {
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signUp();
            }
        });
    }

    public void signUp() {
        if (!validate())
            return;

        String name = nameText.getText().toString();
        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        progressDialog = new ProgressDialog(getActivity(), R.style.Theme_WatchTime_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.creating_account));
        progressDialog.show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                onSignUpSuccess();
                progressDialog.dismiss();
            }
        }, 3000);
    }

    private boolean validate() {
        boolean valid = true;

        String name = nameText.getText().toString();
        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();
        String repeat = repeatText.getText().toString();

        if (name.trim().length() < 5) {
            nameText.setError(getString(R.string.invalid_name));
            valid = false;
        } else {
            nameText.setError(null);
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailText.setError(getString(R.string.invalid_email));
            valid = false;
        } else {
            emailText.setError(null);
        }

        if (password.length() < 4 && password.length() > 16) {
            passwordText.setError(getString(R.string.invalid_password));
            valid = false;
        } else {
            passwordText.setError(null);
        }

        if (!repeat.equals(password)) {
            repeatText.setError(getString(R.string.confirm_password_failed));
            valid = false;
        } else {
            repeatText.setError(null);
        }

        return valid;
    }

    public void onSignUpSuccess() {
        Message completeMessage = mHandler.obtainMessage(0, getString(R.string.account_created));
        completeMessage.sendToTarget();

        getFragmentManager().popBackStack();
    }

}
