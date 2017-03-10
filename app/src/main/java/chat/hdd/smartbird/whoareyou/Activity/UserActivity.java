package chat.hdd.smartbird.whoareyou.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.Bind;
import butterknife.ButterKnife;
import chat.hdd.smartbird.whoareyou.Model.Account;
import chat.hdd.smartbird.whoareyou.R;
import de.hdodenhof.circleimageview.CircleImageView;

import static chat.hdd.smartbird.whoareyou.Activity.MainActivity.ACCOUNT;
import static chat.hdd.smartbird.whoareyou.Activity.MainActivity.SIGN_IN_REQUEST_CODE;

public class UserActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 8888;

    @Bind(R.id.imageViewPictureUser)
    CircleImageView imgPictureUser;
    @Bind(R.id.textViewName)
    TextView tvName;
    @Bind(R.id.textViewEmailUser)
    TextView tvEmail;
    @Bind(R.id.lnuser)
    LinearLayout lnUser;


    private boolean isDoubleBackToExit = false;
    private Account account;
    private String urlPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbarUser);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.app_name);


        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().build(), SIGN_IN_REQUEST_CODE);

        } else {
            Snackbar.make(lnUser, "Welcome " + FirebaseAuth.getInstance().getCurrentUser().getEmail(), Snackbar.LENGTH_SHORT).show();
            // load content
            //displayChatMessage();
            signInAccount();
        }

    }

    private void signInAccount() {


        account = new Account(FirebaseAuth.getInstance().getCurrentUser()
                .getUid(), FirebaseAuth.getInstance().getCurrentUser().getEmail(), FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
        account.setChat(true);

        tvEmail.setText(account.getEmail());
        tvName.setText(account.getName());

        // push values
        FirebaseDatabase.getInstance().getReference().child(ACCOUNT).child(account.getId()).setValue(account, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    Snackbar.make(lnUser, "error", Snackbar.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(UserActivity.this, "Login success", Toast.LENGTH_SHORT).show();
                }
            }
        });

        for (UserInfo profile : FirebaseAuth.getInstance().getCurrentUser().getProviderData()) {
            System.out.println(profile.getProviderId());
            // check if the provider id matches "facebook.com"
           if (profile.getProviderId().equals("google.com")) {
                urlPhoto = profile.getProviderId();
               loadGoogleUserDetails();
            }
        }

    }


    public void loadGoogleUserDetails() {
        try {
            // Configure sign-in to request the user's ID, email address, and basic profile. ID and
            // basic profile are included in DEFAULT_SIGN_IN.
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build();

            // Build a GoogleApiClient with access to GoogleSignIn.API and the options above.
            GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                            System.out.println("onConnectionFailed");
                        }
                    })
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build();

            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
            startActivityForResult(signInIntent, RC_SIGN_IN);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onBackPressed() {
        if (isDoubleBackToExit) {
            super.onBackPressed();
        } else {
            this.isDoubleBackToExit = true;
            Toast.makeText(UserActivity.this, "double click to exit", Toast.LENGTH_SHORT).show();
            CountDownTimer countDownTimer = new CountDownTimer(2000, 1000) {
                @Override
                public void onTick(long l) {

                }

                @Override
                public void onFinish() {
                    isDoubleBackToExit = false;
                }
            }.start();
        }
    }

    @Override
    protected void onResume() {
        isDoubleBackToExit = false;
        super.onResume();
    }
}
