package com.watchtime.fragments.account;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.app.Fragment;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.watchtime.R;
import com.watchtime.activities.AccessAccountBaseActivity;
import com.watchtime.base.ApiEndPoints;
import com.watchtime.base.WatchTimeApplication;
import com.watchtime.base.interfaces.OnDataChangeHandler;
import com.watchtime.sdk.AccessTokenWT;
import com.watchtime.sdk.LoginManagerWT;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class SignUpFragment extends Fragment {
    public static final String TAG = "SignUpFragment";
    private static final int PICK_IMAGE_REQUEST = 100;

    private EditText nameText;
    private EditText emailText;
    private EditText passwordText;
    private EditText repeatText;
    private ImageView profileImage;
    private Button signUpBtn;
    private Bitmap imageDrawable;

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

        ImageView imageLogo = (ImageView) view.findViewById(R.id.sign_up_image_view);
        nameText = (EditText) view.findViewById(R.id.input_name);
        emailText = (EditText) view.findViewById(R.id.input_email);
        passwordText = (EditText) view.findViewById(R.id.input_password);
        repeatText = (EditText) view.findViewById(R.id.input_repeat_password);
        signUpBtn = (Button) view.findViewById(R.id.btn_sign_up);
        profileImage = (ImageView) view.findViewById(R.id.profile_image);

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
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectProfileImage();
            }
        });

        profileImage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                profileImage.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_no_profile_image));
                imageDrawable = null;
                return true;
            }
        });
    }

    private void selectProfileImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
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

        serverSignUp(name, email, password, imageDrawable);
    }

    private void serverSignUp(final String name, final String email, final String password, final Bitmap image) {
        RequestBody requestBody = new FormBody.Builder()
                .add("name", name)
                .add("email", email)
                .add("password", password)
                .add("image", image != null ? toBase64(image) : "no image")
                .build();

        Request request = new Request.Builder()
                .url(ApiEndPoints.REGISTER_USER)
                .post(requestBody)
                .build();

        Call call = new OkHttpClient().newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                onSignUpFailed(getString(R.string.error_getting_response));
                Log.i(TAG, "IOEx: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    onSignUpFailed(getString(R.string.unknown_error));
                    Log.i(TAG, "Unsuccessful Response: " + response.body().string());
                } else {
                    try {
                        String strResp = response.body().string();
                        JSONObject json = new JSONObject(strResp);
                        if (json.has("error")) {
                            int error_code = json.getInt("error_code");
                            if (error_code == 0)
                                onSignUpFailed(getString(R.string.email_already_taken));
                        } else {
                            Message completeMessage = mHandler.obtainMessage(0, getString(R.string.account_created));
                            completeMessage.sendToTarget();

                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    loginToCreatedAccount(email, password);
                                }
                            }, 500);
                        }
                    } catch (JSONException e) {
                        Log.i(TAG, "JSONException: " + e.getMessage());
                        onSignUpFailed(getString(R.string.unknown_error));
                    }
                }
            }
        });
    }

    private void loginToCreatedAccount(final String email, final String password) {
        progressDialog = new ProgressDialog(getActivity(), R.style.Theme_WatchTime_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.connecting));
        progressDialog.show();

        RequestBody requestBody = new FormBody.Builder()
                .add("grant_type", "password")
                .add("username", email)
                .add("password", password)
                .add("client_id", ApiEndPoints.CLIENT_ID)
                .add("client_secret", ApiEndPoints.CLIENT_SECRET)
                .build();

        Request request = new Request.Builder()
                .url(ApiEndPoints.OAUTH2_BASE)
                .post(requestBody)
                .build();

        Call loginCall = new OkHttpClient().newCall(request);
        loginCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("Resp: ", "Failed: " + e.toString());
                onSignUpFailed(getString(R.string.error_getting_response));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    onSignUpFailed(getString(R.string.failed_to_login));
                } else {
                    JSONObject token;
                    try {
                        token = new JSONObject(response.body().string());
                        AccessTokenWT accessToken = AccessTokenWT.createFromJSON(token);
                        onSignUpSuccess(email, accessToken);
                    } catch (JSONException e) {
                        onSignUpFailed(getString(R.string.error_on_response));
                        Log.e("SignUp:Response", e.getMessage());
                    }
                }
            }
        });
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

        if (password.length() < 4 || password.length() > 16) {
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

    public void onSignUpFailed(String reason) {
        Message completeMessage = mHandler.obtainMessage(0, reason);
        completeMessage.sendToTarget();

        ((WatchTimeApplication)getActivity().getApplication()).setUser(null);

        LoginManager.getInstance().logOut();
        LoginManagerWT.getInstance().logout();
    }

    public void onSignUpSuccess(final String email, final AccessTokenWT tokenWT) {
        Message completeMessage = mHandler.obtainMessage(0, getString(R.string.logged_in));
        completeMessage.sendToTarget();

        ((WatchTimeApplication)getActivity().getApplication()).getDataChangeHandler().igniteListeners(OnDataChangeHandler.LOGIN);
        ((AccessAccountBaseActivity)getActivity()).createLoginToken(email, tokenWT);
    }

    public String toBase64(Bitmap bitmap) {
        if (bitmap == null) return "no image";

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST  && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
                profileImage.setImageBitmap(bitmap);
                imageDrawable = bitmap;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
