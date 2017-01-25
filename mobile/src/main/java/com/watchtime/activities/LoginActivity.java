package com.watchtime.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.watchtime.R;
import com.watchtime.activities.base.WatchTimeBaseActivity;

import butterknife.Bind;

public class LoginActivity extends WatchTimeBaseActivity {
    private static final String TAG = "LoginActivity";
    private static final int SIGNUP = 0;

    @Bind(R.id.input_email)
    EditText emailText;
    @Bind(R.id.input_password)
    EditText passwordText;
    @Bind(R.id.btn_login)
    Button loginBtn;
    @Bind(R.id.link_signup)
    TextView signUpText;
    @Bind(R.id.login_image_view)
    ImageView loginImage;

    public static Intent startActivity(Activity activity) {
        Intent intent = new Intent(activity, LoginActivity.class);
        activity.startActivity(intent);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_login);
        loginImage.setBackgroundResource(R.drawable.app_logo_writen);

        loginBtn.setBackgroundResource(R.color.primary_dark);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });

        signUpText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signUp();
            }
        });
    }

    public void login() {
        if (!validate())
            return;

        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this, R.style.Theme_WatchTime_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.connecting));
        progressDialog.show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                onLoginSuccess();
                progressDialog.dismiss();
            }
        }, 3000);
    }

    public void signUp() {
        Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
        startActivityForResult(intent, SIGNUP);
    }

    public boolean validate() {
        boolean valid = true;

        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

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

        return valid;
    }

    public void onLoginSuccess() {
        Toast.makeText(getBaseContext(), getString(R.string.logged_in), Toast.LENGTH_LONG).show();
        finish();
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), getString(R.string.login_failed), Toast.LENGTH_LONG).show();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SIGNUP) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(getBaseContext(), getString(R.string.account_created), Toast.LENGTH_LONG).show();
                this.finish();
            }
        }
    }
}
