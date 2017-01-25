package com.watchtime.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.watchtime.R;
import com.watchtime.activities.base.WatchTimeBaseActivity;

import butterknife.Bind;

public class SignUpActivity extends WatchTimeBaseActivity {

    @Bind(R.id.sign_up_image_view)
    ImageView signUpImage;
    @Bind(R.id.input_name)
    EditText nameText;
    @Bind(R.id.input_email)
    EditText emailText;
    @Bind(R.id.input_password)
    EditText passwordText;
    @Bind(R.id.input_repeat_password)
    EditText repeatText;
    @Bind(R.id.btn_signup)
    Button signUpBtn;
    @Bind(R.id.link_login)
    TextView loginText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_sign_up);
        signUpImage.setBackgroundResource(R.drawable.app_logo_writen);

        signUpBtn.setBackgroundResource(R.color.primary_dark);

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signUp();
            }
        });

        loginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    public void signUp() {
        if (!validate())
            return;

        String name = nameText.getText().toString();
        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        final ProgressDialog progressDialog = new ProgressDialog(SignUpActivity.this, R.style.Theme_WatchTime_Dark_Dialog);
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

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailText.setError(getString(R.string.invalid_email));
            valid = false;
        } else {
            emailText.setError(null);
        }

        if (password.isEmpty() || (password.length() < 4 && password.length() > 16)) {
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
        setResult(RESULT_OK, null);
        finish();
    }

    public void onSignUpFailed() {

    }
}
