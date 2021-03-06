package chat.hdd.smartbird.whoareyou.Activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import chat.hdd.smartbird.whoareyou.Controller.Adapter_Friend;
import chat.hdd.smartbird.whoareyou.Model.Account;
import chat.hdd.smartbird.whoareyou.Model.Friend;
import chat.hdd.smartbird.whoareyou.R;
import de.hdodenhof.circleimageview.CircleImageView;

import static chat.hdd.smartbird.whoareyou.Activity.MainActivity.ACCOUNT;
import static chat.hdd.smartbird.whoareyou.Activity.MainActivity.FRIEND;
import static chat.hdd.smartbird.whoareyou.Activity.MainActivity.SIGN_IN_REQUEST_CODE;

public class UserActivity extends AppCompatActivity {
    private final static int SELECT_PICTURE = 100;
    private final static int GALLERY_KITKAT_INTENT_CALLED = 101;
    public static final String CHOOSE_PICTURE = "choose picture";

    @Bind(R.id.imageViewPictureUser)
    CircleImageView imgPictureUser;
    @Bind(R.id.textViewName)
    TextView tvName;
    @Bind(R.id.textViewEmailUser)
    TextView tvEmail;
    @Bind(R.id.lnuser)
    LinearLayout lnUser;
    @Bind(R.id.buttonChatNow)
    Button btChatNow;
    @Bind(R.id.imageViewChoosePicture)
    ImageView imgChoosePicture;
    @Bind(R.id.listViewFriend)
    ListView lvFriend;
    @Bind(R.id.textViewNumberFriend)
    TextView tvNumberFriend;
    @Bind(R.id.frameLayoutFindFriend)
    FrameLayout frListFriend;
    @Bind(R.id.frameLayoutFindFriend1)
    FrameLayout frFindFriend;
    @Bind(R.id.frameLayoutBackground)
    FrameLayout frBackground;


    private boolean isDoubleBackToExit = false;
    private Account account;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private String filemanagerstring, imagePath, selectedImagePath;
    private Adapter_Friend adapter_friend;
    private ArrayList<Friend> listFriend = new ArrayList<Friend>();
    private Typeface typeface, typeface1;
    private CountDownTimer countDownTimer;

    private int numberFriend = 10000;
    private boolean isFindFriend = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarUser);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.app_name);

        frListFriend.setVisibility(View.INVISIBLE);


        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().build(), SIGN_IN_REQUEST_CODE);

        } else {
            Snackbar.make(lnUser, "Welcome " + FirebaseAuth.getInstance().getCurrentUser().getEmail(), Snackbar.LENGTH_SHORT).show();
            // load content
            //displayChatMessage();
            signInAccount();
        }

        typeface = Typeface.createFromAsset(getAssets(), "miso-bold.otf");
        typeface1 = Typeface.createFromAsset(getAssets(), "miso-bold.otf");

        btChatNow.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    view.animate().scaleX(1.2f);
                    view.animate().scaleY(1.2f);
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {

                    view.animate().scaleX(1.0f);
                    view.animate().scaleY(1.0f);
                    Intent intent = new Intent(UserActivity.this, MainActivity.class);
                    intent.putExtra("id", account.getId());
                    intent.putExtra("email", account.getEmail());
                    intent.putExtra("name", account.getName());
                    intent.putExtra("ischat", account.isChat());


                    if (listFriend.size() == 1) {
                        intent.putExtra("friend_1", listFriend.get(0).getIdFriend());
                    } else if (listFriend.size() > 1) {
                        String idFriend = "";
                        for (int i = 0; i < listFriend.size(); i++) {
                            idFriend = idFriend + "-" + listFriend.get(i).getIdFriend();
                        }
                        intent.putExtra("friend_2", idFriend);
                    }

                    startActivity(intent);
                    finish();
                }
                return false;
            }

        });
        btChatNow.setTypeface(typeface);

        Animation scale = AnimationUtils.loadAnimation(UserActivity.this, R.anim.scale);
        btChatNow.setAnimation(scale);


    }

    private void signInAccount() {
        account = new Account(FirebaseAuth.getInstance().getCurrentUser()
                .getUid(), FirebaseAuth.getInstance().getCurrentUser().getEmail(), FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
        account.setChat(true);


        tvEmail.setText(account.getEmail());
        tvName.setText(account.getName());
        tvName.setTypeface(typeface1);
        tvEmail.setTypeface(typeface1);

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

        // set image
        imgPictureUser.setImageResource(R.drawable.ic_none);
        sharedPreferences = getSharedPreferences(CHOOSE_PICTURE + account.getId(), MODE_PRIVATE);
        String chooseImage = sharedPreferences.getString(CHOOSE_PICTURE, "");
        if (!chooseImage.equals("")) {
            imgPictureUser.setImageURI(Uri.parse(chooseImage));
        } else {
            imgPictureUser.setImageResource(R.drawable.ic_none);
        }

        // get friend
        listFriend.clear();
        FirebaseDatabase.getInstance().getReference().child(FRIEND).child(account.getId()).addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Friend friend = dataSnapshot.getValue(Friend.class);
                listFriend.add(friend);
                Log.d("friend", listFriend.size() + " : " + friend.getNameFriend());


            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        FirebaseDatabase.getInstance().getReference().child(FRIEND).child(account.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                tvNumberFriend.setText("You have " + dataSnapshot.getChildrenCount() + " friend");
                tvNumberFriend.setTypeface(typeface);
                numberFriend = (int) dataSnapshot.getChildrenCount();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        final int[] time = {3000};
        countDownTimer = new CountDownTimer(time[0], 1000) {
            @Override
            public void onTick(long l) {
                time[0] = time[0] - 1000;
                Log.d("numberfriend", numberFriend + " : " + listFriend.size());
            }

            @Override
            public void onFinish() {
                if (listFriend.size() != numberFriend) {
                    time[0] = 3000;
                    countDownTimer.start();
                } else {
                    adapter_friend = new Adapter_Friend(UserActivity.this, R.layout.list_friend_item, listFriend);
                    lvFriend.setAdapter(adapter_friend);
                    for (int i = 0; i < listFriend.size(); i++) {
                        Log.d("numberfriend", listFriend.get(i).getNameFriend());
                    }
                }
            }
        }.start();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SIGN_IN_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Snackbar.make(lnUser, "Successfully signed in Welcome", Snackbar.LENGTH_SHORT).show();
                // load content
                //displayChatMessage();
                signInAccount();
            } else {
                Snackbar.make(lnUser, "We couldn't sign in. Please try again latter", Snackbar.LENGTH_SHORT).show();
                finish();
            }
        }

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                Log.d("chonanhdaidien", "uri " + selectedImageUri.toString());
                // OI file Manager
                filemanagerstring = selectedImageUri.getPath();

                // Media Gallary
                selectedImagePath = getPath(selectedImageUri);
                sharedPreferences = getSharedPreferences(CHOOSE_PICTURE + account.getId(), MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.putString(CHOOSE_PICTURE, selectedImagePath);
                editor.commit();

                imgPictureUser.setImageURI(selectedImageUri);
            } else if (requestCode == GALLERY_KITKAT_INTENT_CALLED) {
                Uri originalUri = null;
                originalUri = data.getData();
                final int takeFlags = data.getFlags()
                        & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                // Check for the freshest data.
                getContentResolver().takePersistableUriPermission(originalUri, takeFlags);
                imgPictureUser.setImageURI(originalUri);
                sharedPreferences = getSharedPreferences(CHOOSE_PICTURE + account.getId(), MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.putString(CHOOSE_PICTURE, originalUri.toString());
                editor.commit();
            }

        }

    }


    private String getPath(Uri uri) {
        String[] projection = {MediaStore.MediaColumns.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        cursor.moveToFirst();
        imagePath = cursor.getString(column_index);

        return uri.toString();
    }


    @OnClick(R.id.imageViewChoosePicture)
    public void choosePicture() {

        // create a popupMenu
        PopupMenu menuMoiBanBe = new PopupMenu(UserActivity.this, imgChoosePicture);
        menuMoiBanBe.getMenuInflater().inflate(R.menu.menu_choose_picture, menuMoiBanBe.getMenu());
        menuMoiBanBe.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.menu_choose_picture) {
                    choosePictureUser();
                }
                return true;
            }
        });
        menuMoiBanBe.show();
    }

    private void choosePictureUser() {
        if (Build.VERSION.SDK_INT < 19) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Choose image your profile"), SELECT_PICTURE);
        } else {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(intent, GALLERY_KITKAT_INTENT_CALLED);
        }
    }

    @OnClick(R.id.frameLayoutFindFriend1)
    public void showListFriend() {
        frListFriend.setVisibility(View.VISIBLE);
        isFindFriend = true;
    }
    @OnClick(R.id.frameLayoutBackground)
    public void hideListFriend() {
        frListFriend.setVisibility(View.INVISIBLE);
        isFindFriend = false;
    }

    @Override
    public void onBackPressed() {
        if (isFindFriend) {
            frListFriend.setVisibility(View.INVISIBLE);
            isFindFriend = false;
        } else {
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
    }

    @Override
    protected void onResume() {
        isDoubleBackToExit = false;
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_signout) {
            AuthUI.getInstance().signOut(this).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Snackbar.make(lnUser, "You have been signed out", Snackbar.LENGTH_SHORT).show();
                    finish();
                }
            });
        }

        return true;
    }
}
