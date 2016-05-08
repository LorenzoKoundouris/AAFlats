package com.example.lorenzo.aaflats;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ActionMenuView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends Activity {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private ImageView qrCode;

    private SharedPreferences mSharedPreferences;
    public static final String MY_PREFERENCES = "MyPreferences";
    public static final String EMAIL_KEY = "StaffEmail";
    public static final String PASSWORD_KEY = "StaffPassword";
    public static final String FULL_NAME_KEY = "StaffFullName";
    public static final String STAFF_KEY = "StaffKey";

    private SharedPreferences mPreviousAccounts;
    private SharedPreferences.Editor accountEditor;
    public static final String MY_ACCOUNTS = "MyAccounts";

    private Context context;

    private ArrayList<Staff> staffSigningIn = new ArrayList<>();
    Staff loggedIn;
    InputMethodManager inputMethodManager;

    private static final int REQUEST_CAMERA = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        context = this;

        // Set up the login form.
        Firebase.setAndroidContext(this);
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        final Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mSharedPreferences = getSharedPreferences(MY_PREFERENCES, MODE_PRIVATE);
        inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        qrCode = (ImageView) findViewById(R.id.qr_code_imageview);

        mPreviousAccounts = getSharedPreferences(MY_ACCOUNTS, MODE_PRIVATE);
        accountEditor = mPreviousAccounts.edit();

        /////////////////////////////////////////////////////
        Map<String, ?> usedAccounts = mPreviousAccounts.getAll();
        final ArrayList<String> pa = new ArrayList<>();
        for (Map.Entry<String, ?> tEntry : usedAccounts.entrySet()) {
            pa.add(tEntry.getValue().toString());
        }
        System.out.println("Previous accounts: \t" + pa.toString());
        ////////////////////////////////////////


//        populateAutoComplete();
        Bundle intent = getIntent().getExtras();
        if (intent != null) {
            staffSigningIn = intent.getParcelableArrayList("parceable_staff_list");
        }

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mEmailSignInButton.setEnabled(true);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        populateAutoComplete();

        mEmailView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
//                    if (!TextUtils.isEmpty(mEmailView.getText().toString()) &&
//                            !TextUtils.isEmpty(mPasswordView.getText().toString())) {
//                        attemptLogin();
//                    }


                }
            }
        });

        qrCode.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);//mine
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    //Check if camera permission is granted
                    if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        scanQRNow();
                    } else {
                        //Camera permission not granted

                        //Provide context to the user to justify permission
                        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                            Toast.makeText(LoginActivity.this, "Camera permission needed", Toast.LENGTH_SHORT).show();
                        }
                        //Request permission
                        requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
//                        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
//                            scanQRNow();
//                        }
                    }
                } else {
                    scanQRNow();
                }
//                startActivity(new Intent(LoginActivity.this, ScanQR.class));
//                overridePendingTransition(R.anim.login_animation, R.anim.splash_animation);
            }
        });


//        final Firebase staffRef = new Firebase(getResources().getString(R.string.staff_location));
////        Query verifyCredentials = staffRef.orderByChild("username").equalTo(mEmailView);
//        staffRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                for (DataSnapshot childSnap : dataSnapshot.getChildren()) {
//                    Staff stf = childSnap.getValue(Staff.class);
//                    stf.setStaffKey(childSnap.getKey());
//                    staffSigningIn.add(stf);
//                }
//                attemptLogin();
//            }
//
//            @Override
//            public void onCancelled(FirebaseError firebaseError) {
//
//            }
//        });

    }//end of oncreate

    private void scanQRNow() {
        Animation mAnimation = new TranslateAnimation(0, 0, 0, 2000); //2000
        mAnimation.setDuration(3000); //3000
        mAnimation.setFillAfter(true);
//        mAnimation.setRepeatCount(-1);
//      mAnimation.setRepeatMode(Animation.REVERSE);
        qrCode.startAnimation(mAnimation);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Do something after 2s = 2000ms
                startActivity(new Intent(LoginActivity.this, ScanQR.class).putExtra("fromHome", false));
                finish();
                overridePendingTransition(R.anim.login_animation, R.anim.splash_animation);
            }
        }, 1500); //3000
    }

    private void populateAutoComplete() {

        String staffEmail = mSharedPreferences.getString(EMAIL_KEY, "");
        String staffPassword = mSharedPreferences.getString(PASSWORD_KEY, "");

        boolean logout = mSharedPreferences.getBoolean("logout", true);
        if (logout) {
            mEmailView.setText(staffEmail);
        } else {
            mEmailView.setText(staffEmail);
            mPasswordView.setText(staffPassword);
            attemptLogin();
        }


        /////////////////////////////////////////////////////
        Map<String, ?> mypref = mSharedPreferences.getAll();
        final ArrayList<String> mp = new ArrayList<>();
        for (Map.Entry<String, ?> tEntry : mypref.entrySet()) {
            mp.add(tEntry.getValue().toString());
        }
        System.out.println("My preferences before: \t" + mp.toString());
        ////////////////////////////////////////


        Map<String, ?> usedAccounts = mPreviousAccounts.getAll();
        final ArrayList<String> aa = new ArrayList<>();
        for (Map.Entry<String, ?> tEntry : usedAccounts.entrySet()) {
            aa.add(tEntry.getValue().toString());
        }

        System.out.println("Populate accounts: \t" + aa.toString());

//        ArrayList<String> previouslyUsedUsernames = new ArrayList<>();
//        previouslyUsedUsernames.add(staffEmail);

        addEmailsToAutoComplete(aa);//previouslyUsedUsernames
    }


    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == REQUEST_CAMERA) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                scanQRNow();
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        new Thread(new Runnable() {
            public void run() {


                if (mAuthTask != null) {
                    return;
                }

                // Reset errors.
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        mEmailView.setError(null);
                        mPasswordView.setError(null);

                    }
                });

                // Store values at the time of the login attempt.
                String email = mEmailView.getText().toString().trim();
                String password = mPasswordView.getText().toString().trim();


                boolean cancel = false;
                View focusView = null;

                // Check for a valid password, if the user entered one.
                if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mPasswordView.setError(getString(R.string.error_invalid_password));
                        }
                    });
                    focusView = mPasswordView;
                    cancel = true;
                }

                // Check for a valid email address.
                if (TextUtils.isEmpty(email)) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mEmailView.setError(getString(R.string.error_field_required));
                        }
                    });
                    focusView = mEmailView;
                    cancel = true;
                } else if (!isEmailValid(email)) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mEmailView.setError(getString(R.string.error_invalid_email));
                        }
                    });
                    focusView = mEmailView;
                    cancel = true;
                }

                if (cancel) {
                    // There was an error; don't attempt login and focus the first
                    // form field with an error.
                    final View finalFocusView = focusView;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            finalFocusView.requestFocus();
                        }
                    });
                } else if (staffSigningIn.size() > 0) {
                    // Show a progress spinner, and kick off a background task to
                    // perform the user login attempt.
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                            } catch (Exception ignore) {

                            }

                            showProgress(true);

                        }
                    });
                    mAuthTask = new UserLoginTask(email, password);
                    mAuthTask.execute((Void) null);
                }
            }
        }).start();
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_longAnimTime);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);

                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {


        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }


        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            boolean validUsername = false;

            boolean isStaff = false;
            int i;
            for (i = 0; i < staffSigningIn.size(); i++) {
                if (staffSigningIn.get(i).getUsername().matches(mEmail)) {
                    validUsername = true;
                    break;
                }
//                if (staffSigningIn.get(i).getUsername().matches(mEmail)
//                        && staffSigningIn.get(i).getPassword().matches(mPassword)) {
//                    isStaff = true;
//                    loggedIn = staffSigningIn.get(i);
//                    break;
//                } else {
//                    isStaff = false;
//                }
            }
            if (validUsername) {
                if (staffSigningIn.get(i).getPassword().matches(mPassword)) {
                    isStaff = true;
                    loggedIn = staffSigningIn.get(i);
                    accountEditor.putString(loggedIn.getUsername(), loggedIn.getUsername()).apply();
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mPasswordView.setError("Wrong password");
                        }
                    });
                }
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mEmailView.setError("Wrong username");
                    }
                });
            }


            return isStaff;


//            try {
//                // Simulate network access.
//                Thread.sleep(1000);
//
//            } catch (InterruptedException e) {
//                return false;
//            }

//            for (String credential : DUMMY_CREDENTIALS) {
//                String[] pieces = credential.split(":");
//                if (pieces[0].equals(mEmail)) {
//                    // Account exists, return true if the password matches.
//                    return pieces[1].equals(mPassword);
//                }
//            }

            // TODO: register the new account here.
//            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {


            mAuthTask = null;
            showProgress(false);

            if (success) {

                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putString(EMAIL_KEY, loggedIn.getUsername());
                editor.putString(PASSWORD_KEY, loggedIn.getPassword());
                editor.putString(STAFF_KEY, loggedIn.getStaffKey());
                editor.putBoolean("logout", false);
                String tmp = loggedIn.getForename() + " " + loggedIn.getSurname();
                editor.putString(FULL_NAME_KEY, tmp);
                editor.apply();

                /////////////////////////////////////////////////////
                Map<String, ?> usedAccounts = mSharedPreferences.getAll();
                final ArrayList<String> pa = new ArrayList<>();
                for (Map.Entry<String, ?> tEntry : usedAccounts.entrySet()) {
                    pa.add(tEntry.getValue().toString());
                }
                System.out.println("My preferences changed: \t" + pa.toString());
                ////////////////////////////////////////


                startActivity(new Intent(LoginActivity.this, Homepage.class).putExtra("parceable_staff", loggedIn)); //mine
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

