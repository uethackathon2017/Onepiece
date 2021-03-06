package chat.hdd.smartbird.whoareyou.Activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.victor.loading.rotate.RotateLoading;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

import chat.hdd.smartbird.whoareyou.Model.Account;
import chat.hdd.smartbird.whoareyou.Model.ChatMessage;
import chat.hdd.smartbird.whoareyou.Model.Friend;
import chat.hdd.smartbird.whoareyou.R;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final int SIGN_IN_REQUEST_CODE = 1;
    public static final String CHAT_ROOM = "CHAT_ROOM";
    public static final String ACCOUNT = "ACCOUNT";
    public static final String FRIEND = "FRIEND";
    private static final int SELECT_PICTURE = 3;
    private final static int GALLERY_KITKAT_INTENT_CALLED = 2;
    public static final int REQUEST_CODE_CAMERA = 10;

    public static boolean isMask = false;
    public static int chooseMask = 0;
    public static int countChildren = -1;
    private FirebaseListAdapter<ChatMessage> adapter;
    private RelativeLayout activity_main;
    private Intent intent;

    // add emojicon
    private EmojiconEditText emojiconEditText;
    private ImageView emojiButton, submitButton, imgCry, imgSendImage, imgChooseImage, imgSendCamera;
    private EmojIconActions emojIconActions;
    private TextView tvNotify, tvWaitChat, tvPleaseWait;
    private RotateLoading rotateLoading;
    private FrameLayout frameLoading, frameSendImage;
    private Button btSendImage, btCancelImage;

    private Account account, accountReceive;
    private ArrayList<Account> listAccount;
    private ArrayList<String> listKeyAccount;
    private CountDownTimer countDownTimer;
    private static String keyAccount = "";
    private int typeSendData = 0;

    private String filemanagerstring, imagePath, selectedImagePath;

    private Friend friendReceive, friendSend;
    private boolean isFriend = false;

    private FirebaseStorage storage;
    private StorageReference storageRef;
    private String idListFriend = "";
    private String arrayIDlistFriend[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        initView();


        signInAccount();

        storage = FirebaseStorage.getInstance("gs://whoareyou-29cfa.appspot.com");
        storageRef = storage.getReference();

    }

    private void signInAccount() {
        // set toolbar
        getSupportActionBar().setTitle(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
        getSupportActionBar().setSubtitle(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_chat_couple_2);

        account.setChat(false);

        if (intent.hasExtra("friend_1")) {
            idListFriend = intent.getStringExtra("friend_1");
        }
        if (intent.hasExtra("friend_2")) {
            idListFriend = intent.getStringExtra("friend_2");
            arrayIDlistFriend = idListFriend.split("-");
        }


        // push values
        FirebaseDatabase.getInstance().getReference().child(ACCOUNT).child(account.getId()).setValue(account, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    Snackbar.make(activity_main, "error", Snackbar.LENGTH_SHORT).show();
                } else {
//                    Toast.makeText(MainActivity.this, "Login success", Toast.LENGTH_SHORT).show();
                }
            }
        });

        FirebaseDatabase.getInstance().getReference().child(ACCOUNT).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                final Account accountOther = dataSnapshot.getValue(Account.class);

                if (!accountOther.isChat() && !account.isChat() && (!account.getId().equals(accountOther.getId()))) {
                    accountOther.setChat(true);
                    account.setChat(true);

                    accountReceive = new Account(accountOther.getId(), accountOther.getEmail(), accountOther.getName());
                    accountReceive.setChat(accountOther.isChat());

                    if (intent.hasExtra("friend_1")) {
                        if (idListFriend.equals(accountReceive.getId())) {
                            isFriend = true;
                        } else isFriend = false;
                    } else if (intent.hasExtra("friend_2")) {
                        for (int i = 0; i < arrayIDlistFriend.length; i++) {
                            if (arrayIDlistFriend[i].equals(accountReceive.getId())) {
                                isFriend = true;
                                return;
                            }
                        }
                    }

                    FirebaseDatabase.getInstance().getReference().child(ACCOUNT).child(account.getId()).setValue(account, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                FirebaseDatabase.getInstance().getReference().child(CHAT_ROOM).child(account.getId() + "-" + accountOther.getId()).push().setValue(new
                                        ChatMessage("hello", account.getEmail(), ""));

                                // create friend temporaty
                                createFriendTemporaty();
                                //show message
                                displayChatMessage();
                            } else {
                                Toast.makeText(MainActivity.this, "Connection fail", Toast.LENGTH_SHORT).show();
                                account.setChat(false);
                                accountOther.setChat(false);
                            }
                        }
                    });

                    FirebaseDatabase.getInstance().getReference().child(ACCOUNT).child(accountOther.getId()).setValue(accountOther, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                FirebaseDatabase.getInstance().getReference().child(CHAT_ROOM).child(account.getId() + "-" + accountOther.getId()).push().setValue(new
                                        ChatMessage("hello", accountOther.getEmail(), ""));

                            } else {
                                Toast.makeText(MainActivity.this, "Connection fail", Toast.LENGTH_SHORT).show();
                                account.setChat(false);
                                accountOther.setChat(false);
                            }
                        }
                    });
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                final Account accountOther = dataSnapshot.getValue(Account.class);
                Log.d("account", accountOther.getEmail() + " : ");

                if (!accountOther.isChat() && !account.isChat() && (!account.getId().equals(accountOther.getId()))) {
                    Log.d("account_1", accountOther.getEmail() + " : ");
                    accountOther.setChat(true);
                    account.setChat(true);

                    accountReceive = new Account(accountOther.getId(), accountOther.getEmail(), accountOther.getName());
                    accountReceive.setChat(accountOther.isChat());

                    if (intent.hasExtra("friend_1")) {
                        if (idListFriend.equals(accountReceive.getId())) {
                            isFriend = true;
                        } else isFriend = false;
                    } else if (intent.hasExtra("friend_2")) {
                        for (int i = 0; i < arrayIDlistFriend.length; i++) {
                            if (arrayIDlistFriend[i].equals(accountReceive.getId())) {
                                isFriend = true;
                                return;
                            }
                        }
                    }

                    FirebaseDatabase.getInstance().getReference().child(ACCOUNT).child(account.getId()).setValue(account, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                FirebaseDatabase.getInstance().getReference().child(CHAT_ROOM).child(account.getId() + "-" + accountOther.getId()).push().setValue(new
                                        ChatMessage("hello", account.getEmail(), ""));

                                // create friend temporaty
                                createFriendTemporaty();
                                //show message
                                displayChatMessage();
                            } else {
                                Toast.makeText(MainActivity.this, "Connection fail", Toast.LENGTH_SHORT).show();
                                account.setChat(false);
                                accountOther.setChat(false);
                            }
                        }
                    });

                    FirebaseDatabase.getInstance().getReference().child(ACCOUNT).child(accountOther.getId()).setValue(accountOther, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                FirebaseDatabase.getInstance().getReference().child(CHAT_ROOM).child(account.getId() + "-" + accountOther.getId()).push().setValue(new
                                        ChatMessage("hello", accountOther.getEmail(), ""));


                            } else {
                                Toast.makeText(MainActivity.this, "Connection fail", Toast.LENGTH_SHORT).show();
                                account.setChat(false);
                                accountOther.setChat(false);
                            }
                        }
                    });


                }
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

    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SIGN_IN_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Snackbar.make(activity_main, "Successfully signed in Welcome", Snackbar.LENGTH_SHORT).show();
                // load content
                //displayChatMessage();
                signInAccount();
            } else {
                Snackbar.make(activity_main, "We couldn't sign in. Please try again latter", Snackbar.LENGTH_SHORT).show();
                finish();
            }
        }
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                frameSendImage.setVisibility(View.VISIBLE);
                Uri selectedImageUri = data.getData();
                Log.d("chonanhdaidien", "uri " + selectedImageUri.toString());

                imgChooseImage.setImageURI(selectedImageUri);
            } else if (requestCode == GALLERY_KITKAT_INTENT_CALLED) {
                frameSendImage.setVisibility(View.VISIBLE);
                Uri originalUri = null;
                originalUri = data.getData();
                imgChooseImage.setImageURI(originalUri);

            }

        }
        // camera
        if (requestCode == REQUEST_CODE_CAMERA && resultCode == RESULT_OK && data != null) {
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            frameSendImage.setVisibility(View.VISIBLE);
            imgChooseImage.setImageBitmap(bitmap);
        }
    }


    // create friend values
    private void createFriendTemporaty() {
        if(!isFriend) {
            FirebaseDatabase.getInstance().getReference().child(FRIEND).child(account.getId()).child(accountReceive.getId()).setValue(new Friend(
                    accountReceive.getId(), accountReceive.getEmail(), accountReceive.getName(), false, false));
            FirebaseDatabase.getInstance().getReference().child(FRIEND).child(accountReceive.getId()).child(account.getId()).setValue(new Friend(
                    account.getId(), account.getEmail(), account.getName(), false, false));
        }
        FirebaseDatabase.getInstance().getReference().child(FRIEND).child(account.getId()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d("receive_2", "1");
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d("receive_2", "2");
                Friend friendOther = dataSnapshot.getValue(Friend.class);
                if (friendOther.getIdFriend().equals(accountReceive.getId())) {
                    Log.d("friendreceive", friendOther.getNameFriend());
                    friendSend = new Friend(friendOther.getIdFriend(), friendOther.getEmailFriend(), friendOther.getNameFriend(),
                            friendOther.isRequest(), friendOther.isFriend());
                    // if you received request friend
                    //if (friendSend.isRequest() && !friendSend.isFriend()) {


                       // receiveRequestFriend();


//                    } else if (friendSend.isRequest() && friendSend.isFriend()) {
//                        Toast.makeText(MainActivity.this, R.string.become_friend, Toast.LENGTH_SHORT).show();
//                        isFriend = true;
//                    }
                    //else
                        if (!friendSend.isRequest() && !friendSend.isFriend()) {
                        Toast.makeText(MainActivity.this, R.string.refuse_request_friend, Toast.LENGTH_SHORT).show();
                    }
                }

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d("receive_2", "3");
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Log.d("receive_2", "4");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("receive_2", "5");
            }
        });

        // account receive
        FirebaseDatabase.getInstance().getReference().child(FRIEND).child(accountReceive.getId()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d("receive_1", "1");
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d("receive_1", "2");
                Friend friendOther = dataSnapshot.getValue(Friend.class);
                if (friendOther.getIdFriend().equals(account.getId())) {
                    friendReceive = new Friend(friendOther.getIdFriend(), friendOther.getEmailFriend(), friendOther.getNameFriend(),
                            friendOther.isRequest(), friendOther.isFriend());
                    // if you received request friend
                    if (friendReceive.isRequest() && !friendReceive.isFriend()) {
                        receiveRequestFriend();
                    } else if (friendReceive.isRequest() && friendReceive.isFriend()) {
                        Toast.makeText(MainActivity.this, R.string.become_friend, Toast.LENGTH_SHORT).show();
                        isFriend = true;
                    }
//                    else if (!friendReceive.isRequest() && !friendReceive.isFriend()) {
//                        Toast.makeText(MainActivity.this, R.string.refuse_request_friend, Toast.LENGTH_SHORT).show();
//                    }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d("receive_1", "3");
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Log.d("receive_1", "4");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("receive_1", "5");
            }
        });

    }

    // you receive request friend
    private void receiveRequestFriend() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setIcon(R.drawable.ic_add_friend);
        builder.setTitle(R.string.add_friend);
        builder.setCancelable(false);
        builder.setMessage(R.string.receive_request_friend);
        builder.setNegativeButton(R.string.accept, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // if accept request friend
                FirebaseDatabase.getInstance().getReference().child(FRIEND).child(account.getId()).child(accountReceive.getId()).setValue(new Friend(
                        accountReceive.getId(), accountReceive.getEmail(), accountReceive.getName(), true, true
                ));
                FirebaseDatabase.getInstance().getReference().child(FRIEND).child(accountReceive.getId()).child(account.getId()).setValue(new Friend(
                        account.getId(), account.getEmail(), account.getName(), true, true), new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError == null) {
                            Toast.makeText(MainActivity.this, R.string.become_friend, Toast.LENGTH_SHORT).show();
                            isFriend = true;
                        } else {
                            Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                dialogInterface.dismiss();
            }
        });
        builder.setNeutralButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // if no accept request friend
                FirebaseDatabase.getInstance().getReference().child(FRIEND).child(accountReceive.getId()).child(account.getId()).setValue(new Friend(
                        account.getId(), account.getEmail(), account.getName(), false, false
                ));
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    // you send request friend
    private void sendRequestFriend() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.add_friend);
        builder.setIcon(R.drawable.ic_add_friend);
        builder.setMessage(R.string.add_friend_infor);
        builder.setNegativeButton(R.string.accept, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // if accept request friend
                FirebaseDatabase.getInstance().getReference().child(FRIEND).child(account.getId()).child(accountReceive.getId()).setValue(new Friend(
                        accountReceive.getId(), accountReceive.getEmail(), accountReceive.getName(), true, false
                ));


                dialogInterface.dismiss();
            }
        });
        builder.setNeutralButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.submit_button:
                // check account chat = true;
                if (account.isChat()) {
                    if (!emojiconEditText.getText().toString().equals("")) {
                        tvNotify.setText(R.string.notify_sending);
                        FirebaseDatabase.getInstance().getReference().child(CHAT_ROOM).child(account.getId() + "-" + accountReceive.getId()).push().setValue(new ChatMessage(emojiconEditText.getText().toString(),
                                FirebaseAuth.getInstance().getCurrentUser().getEmail(), ""), new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if (databaseError == null) {
                                    hideNotify(R.string.notify_sent);
                                } else {
                                    hideNotify(R.string.notify_error);
                                }
                            }
                        });
                        FirebaseDatabase.getInstance().getReference().child(CHAT_ROOM).child(accountReceive.getId() + "-" + account.getId()).push().setValue(new ChatMessage(emojiconEditText.getText().toString(),
                                FirebaseAuth.getInstance().getCurrentUser().getEmail(), ""), new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if (databaseError == null) {
                                    hideNotify(R.string.notify_sent);
                                } else {
                                    hideNotify(R.string.notify_error);
                                }
                            }
                        });

                        emojiconEditText.setText("");
                        emojiconEditText.requestFocus();
                    } else {
                        hideNotify(R.string.notify_empty);
                    }
                } else {
                    Snackbar.make(activity_main, R.string.waiting_someone, Snackbar.LENGTH_SHORT).show();
                }
                break;

            case R.id.buttonCancelImage:
                frameSendImage.setVisibility(View.INVISIBLE);

                break;

            case R.id.buttonSendImage:
                if (typeSendData == 0) {
                    frameSendImage.setVisibility(View.INVISIBLE);

                    Calendar calendar = Calendar.getInstance();

                    // Create a reference to "mountains.jpg"
                    StorageReference mountainsRef = storageRef.child("image" + calendar.getTimeInMillis() + ".png");

                    // Get the data from an ImageView as bytes
                    imgChooseImage.setDrawingCacheEnabled(true);
                    imgChooseImage.buildDrawingCache();
                    Bitmap bitmap = imgChooseImage.getDrawingCache();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                    byte[] data = baos.toByteArray();

                    UploadTask uploadTask = mountainsRef.putBytes(data);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                            Toast.makeText(MainActivity.this, "Error send image", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                            Uri downloadUrl = taskSnapshot.getDownloadUrl();
                            if (account.isChat()) {
                                if (emojiconEditText.getText().toString().equals("")) {
                                    tvNotify.setText(R.string.notify_sending);
                                    FirebaseDatabase.getInstance().getReference().child(CHAT_ROOM).child(account.getId() + "-" + accountReceive.getId()).push().setValue(new ChatMessage(emojiconEditText.getText().toString(),
                                            FirebaseAuth.getInstance().getCurrentUser().getEmail(), String.valueOf(downloadUrl)), new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                            if (databaseError == null) {
                                                hideNotify(R.string.notify_sent);
                                            } else {
                                                hideNotify(R.string.notify_error);
                                            }
                                        }
                                    });
                                    FirebaseDatabase.getInstance().getReference().child(CHAT_ROOM).child(accountReceive.getId() + "-" + account.getId()).push().setValue(new ChatMessage(emojiconEditText.getText().toString(),
                                            FirebaseAuth.getInstance().getCurrentUser().getEmail(), String.valueOf(downloadUrl)), new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                            if (databaseError == null) {
                                                hideNotify(R.string.notify_sent);
                                            } else {
                                                hideNotify(R.string.notify_error);
                                            }
                                        }
                                    });

                                    emojiconEditText.setText("");
                                    emojiconEditText.requestFocus();
                                } else {
                                    hideNotify(R.string.notify_empty);
                                }
                            } else {
                                Snackbar.make(activity_main, R.string.waiting_someone, Snackbar.LENGTH_SHORT).show();
                            }
                        }
                    });


                } else if (typeSendData == 1) {
                    frameSendImage.setVisibility(View.INVISIBLE);

                    Calendar calendar = Calendar.getInstance();

                    // Create a reference to "mountains.jpg"
                    StorageReference mountainsRef = storageRef.child("image" + calendar.getTimeInMillis() + ".png");

                    // Get the data from an ImageView as bytes
                    imgChooseImage.setDrawingCacheEnabled(true);
                    imgChooseImage.buildDrawingCache();
                    Bitmap bitmap = imgChooseImage.getDrawingCache();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                    byte[] data = baos.toByteArray();

                    UploadTask uploadTask = mountainsRef.putBytes(data);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                            Toast.makeText(MainActivity.this, "Error send image", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                            Uri downloadUrl = taskSnapshot.getDownloadUrl();
                            Toast.makeText(MainActivity.this, "Send image success", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                break;
            case R.id.imageViewSendImage:
                typeSendData = 0;
                if (Build.VERSION.SDK_INT < 19) {
                    Intent intent1 = new Intent();
                    intent1.setType("image/*");
                    intent1.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent1, "Choose Image"), SELECT_PICTURE);
                } else {
                    Intent intent2 = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent2.addCategory(Intent.CATEGORY_OPENABLE);
                    intent2.setType("image/*");
                    startActivityForResult(intent2, GALLERY_KITKAT_INTENT_CALLED);
                }

                break;
            case R.id.imageViewSendCamera:
                typeSendData = 1;
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                startActivityForResult(intent, REQUEST_CODE_CAMERA);


                break;
        }
    }

    private void displayChatMessage() {
        rotateLoading.stop();
        frameLoading.setVisibility(View.INVISIBLE);

        emojiconEditText.setEnabled(true);
        emojiButton.setEnabled(true);

        ListView listOfMessage = (ListView) findViewById(R.id.list_of_message);
        adapter = new FirebaseListAdapter<ChatMessage>(this, ChatMessage.class, R.layout.list_message_item,
                FirebaseDatabase.getInstance().getReference().child(CHAT_ROOM).child(account.getId() + "-" + accountReceive.getId())) {
            @Override
            protected void populateView(View v, ChatMessage model, int position) {
                // get references  to the  views of list item.xml
                LinearLayout lnReceiveMessage, lnReceiveMessageTotal, lnSendMessage;
                TextView messageText, messageUser, messageTime, messageSendText, messageSendUser, messageSendTime;
                ImageView imgSend, imgReceive;

                ImageView imgMask = (ImageView) v.findViewById(R.id.imageViewMask);

                if (position == 0) {
                    Log.d("posstion", position + " : " + model.getMessageText());
                }
                if (!isMask) {
                    Random random = new Random();
                    chooseMask = random.nextInt(5);
                    isMask = true;
                }
                if (chooseMask == 0) imgMask.setBackgroundResource(R.drawable.ic_mask);
                else if (chooseMask == 1) imgMask.setBackgroundResource(R.drawable.ic_mask1);
                else if (chooseMask == 2) imgMask.setBackgroundResource(R.drawable.ic_mask2);
                else if (chooseMask == 3) imgMask.setBackgroundResource(R.drawable.ic_mask3);
                else if (chooseMask == 4) imgMask.setBackgroundResource(R.drawable.ic_mask4);


                lnReceiveMessage = (LinearLayout) v.findViewById(R.id.linearLayout_receiveMessage);
                lnReceiveMessageTotal = (LinearLayout) v.findViewById(R.id.linearLayout_receiveMessage_total);
                lnSendMessage = (LinearLayout) v.findViewById(R.id.linearLayout_sendMessage);
                messageText = (TextView) v.findViewById(R.id.textView_message_text);
                messageSendText = (TextView) v.findViewById(R.id.textView_send_message_text);
                messageUser = (TextView) v.findViewById(R.id.textView_message_user);
                messageSendUser = (TextView) v.findViewById(R.id.textView_send_message_user);
                messageTime = (TextView) v.findViewById(R.id.textView_message_time);
                messageSendTime = (TextView) v.findViewById(R.id.textView_send_message_time);
                imgSend = (ImageView) v.findViewById(R.id.imageViewSend);
                imgReceive = (ImageView) findViewById(R.id.imageViewReceive);

                boolean isSend = false;

                if (FirebaseAuth.getInstance().getCurrentUser().getEmail().equals(model.getMessageUser())) {
                    isSend = true;
                } else {
                    isSend = false;
                }

                if (isSend) {
                    lnReceiveMessageTotal.setVisibility(View.GONE);
                    lnSendMessage.setVisibility(View.VISIBLE);
                    lnSendMessage.setBackgroundResource(R.drawable.in_message_bg);
                    messageSendText.setText(model.getMessageText());
                    messageSendUser.setText(model.getMessageUser());
                    messageSendUser.setVisibility(View.GONE);
                    messageSendTime.setHint(android.text.format.DateFormat.format("HH:mm", model.getMessageTime()));

                    if (!model.getMessageImage().equals(""))
                        Glide.with(MainActivity.this).load(model.getMessageImage()).
                                override(200, 200).fitCenter().into(imgSend);
                } else {
                    lnReceiveMessageTotal.setVisibility(View.VISIBLE);
                    lnReceiveMessage.setBackgroundResource(R.drawable.out_message_bg);
                    lnSendMessage.setVisibility(View.GONE);
                    messageText.setText(model.getMessageText());
                    messageUser.setText(accountReceive.getName());
                    if (!isFriend) messageUser.setVisibility(View.GONE);
                    else messageUser.setVisibility(View.VISIBLE);
                    messageTime.setHint(android.text.format.DateFormat.format("HH:mm", model.getMessageTime()));

                    if (!model.getMessageImage().equals(""))
                        Glide.with(MainActivity.this).load(model.getMessageImage()).
                                override(200, 200).fitCenter().into(imgReceive);


                }

            }
        };
        listOfMessage.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        listOfMessage.setAdapter(adapter);
        listOfMessage.setSelection(adapter.getCount() - 1);

        FirebaseDatabase.getInstance().getReference().child(CHAT_ROOM).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d("removevalue", "add");
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d("removevalue", "change");
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

                String key = dataSnapshot.getKey();
                if (key.equals(account.getId() + "-" + accountReceive.getId())) {
                    frameLoading.setVisibility(View.VISIBLE);
                    rotateLoading.setVisibility(View.INVISIBLE);
                    imgCry.setVisibility(View.VISIBLE);


                    Animation scale = AnimationUtils.loadAnimation(MainActivity.this, R.anim.scale);
                    imgCry.startAnimation(scale);
                    tvPleaseWait.setText(R.string.googbye);

                    final int[] time = {6};
                    CountDownTimer countDownTimer = new CountDownTimer(7000, 1000) {
                        @Override
                        public void onTick(long l) {
                            time[0]--;
                            tvWaitChat.setText("The other person has moved, the conversation will end in " + time[0] + " seconds");
                        }

                        @Override
                        public void onFinish() {
                            startActivity(new Intent(MainActivity.this, UserActivity.class));
                            finish();
                        }
                    }.start();
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private void exitApp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.sure);
        builder.setIcon(R.drawable.exit_chat);
        builder.setMessage(R.string.exit_chat);
        builder.setNegativeButton(R.string.accept, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                account.setChat(true);
                FirebaseDatabase.getInstance().getReference().child(ACCOUNT).child(account.getId()).setValue(account);
                if (accountReceive != null) {
                    FirebaseDatabase.getInstance().getReference().child(CHAT_ROOM).child(account.getId() + "-" + accountReceive.getId()).removeValue();
                    FirebaseDatabase.getInstance().getReference().child(CHAT_ROOM).child(accountReceive.getId() + "-" + account.getId()).removeValue();
                    if (!isFriend) {
                        FirebaseDatabase.getInstance().getReference().child(FRIEND).child(account.getId()).child(accountReceive.getId()).removeValue();
                        FirebaseDatabase.getInstance().getReference().child(FRIEND).child(accountReceive.getId()).child(account.getId()).removeValue();
                    }
                }
                dialogInterface.dismiss();
                startActivity(new Intent(MainActivity.this, UserActivity.class));
                finish();
            }
        });
        builder.setNeutralButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();


    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        exitApp();
    }

    // init view
    private void initView() {
        activity_main = (RelativeLayout) findViewById(R.id.content_main);
        listAccount = new ArrayList<Account>();
        listKeyAccount = new ArrayList<String>();
        rotateLoading = (RotateLoading) findViewById(R.id.rotateLoading);
        frameLoading = (FrameLayout) findViewById(R.id.frame_loading);
        rotateLoading.start();
        tvWaitChat = (TextView) findViewById(R.id.textViewWaitChat);
        tvPleaseWait = (TextView) findViewById(R.id.textViewPleaseWait);
        imgCry = (ImageView) findViewById(R.id.imageViewCry);
        imgCry.setVisibility(View.INVISIBLE);
        imgSendImage = (ImageView) findViewById(R.id.imageViewSendImage);
        imgSendImage.setOnClickListener(this);
        imgSendCamera = (ImageView) findViewById(R.id.imageViewSendCamera);
        imgSendCamera.setOnClickListener(this);
        btSendImage = (Button) findViewById(R.id.buttonSendImage);
        btSendImage.setOnClickListener(this);
        btCancelImage = (Button) findViewById(R.id.buttonCancelImage);
        btCancelImage.setOnClickListener(this);
        frameSendImage = (FrameLayout) findViewById(R.id.frame_sendImage);
        frameSendImage.setVisibility(View.INVISIBLE);
        imgChooseImage = (ImageView) findViewById(R.id.imageViewImageChoose);


        Animation scale = AnimationUtils.loadAnimation(MainActivity.this, R.anim.scale);
        tvPleaseWait.startAnimation(scale);


        // add emoji
        emojiButton = (ImageView) findViewById(R.id.emoji_button);
        emojiButton.setEnabled(false);
        tvNotify = (TextView) findViewById(R.id.textViewNotify);
        submitButton = (ImageView) findViewById(R.id.submit_button);
        submitButton.setOnClickListener(this);
        emojiconEditText = (EmojiconEditText) findViewById(R.id.emojicon_edit_text);
        emojiconEditText.setEnabled(false);
        emojIconActions = new EmojIconActions(MainActivity.this, activity_main, emojiButton, emojiconEditText);
        emojIconActions.ShowEmojicon();

        // get extra
        intent = getIntent();
        if (intent.hasExtra("id")) {
            account = new Account();
            account.setId(intent.getStringExtra("id"));
            account.setEmail(intent.getStringExtra("email"));
            account.setName(intent.getStringExtra("name"));
            account.setChat(intent.getBooleanExtra("ischat", false));
        }


    }

    public void hideNotify(int notify) {
        tvNotify.setText(notify);
        countDownTimer = new CountDownTimer(2000, 1000) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                tvNotify.setText("");
            }
        }.start();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_exit_chat) {
            exitApp();
        }
        if (id == R.id.action_add_friend) {
            if (isFriend)
                Toast.makeText(MainActivity.this, R.string.become_friend, Toast.LENGTH_SHORT).show();
            else sendRequestFriend();
        }

        return true;
    }
}
