package chat.hdd.smartbird.whoareyou.Activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
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
import android.util.Base64;
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

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.victor.loading.rotate.RotateLoading;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Random;

import chat.hdd.smartbird.whoareyou.Model.Account;
import chat.hdd.smartbird.whoareyou.Model.ChatMessage;
import chat.hdd.smartbird.whoareyou.R;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final int SIGN_IN_REQUEST_CODE = 1;
    public static final String CHAT_ROOM = "CHAT_ROOM";
    public static final String ACCOUNT = "ACCOUNT";
    private static final int SELECT_PICTURE = 3;
    private final static int GALLERY_KITKAT_INTENT_CALLED = 2;

    public static boolean isMask = false;
    public static int chooseMask = 0;
    public static int countChildren = -1;
    private FirebaseListAdapter<ChatMessage> adapter;
    private RelativeLayout activity_main;

    // add emojicon
    private EmojiconEditText emojiconEditText;
    private ImageView emojiButton, submitButton, imgCry, imgSendImage, imgChooseImage;
    private EmojIconActions emojIconActions;
    private TextView tvNotify, tvWaitChat, tvPleaseWait;
    private RotateLoading rotateLoading;
    private FrameLayout frameLoading, frameSendImage;
    private Button btSendImage, btCancelImage;

    private Account account, accountReceive;
    private ArrayList<Account> listAccount;
    private ArrayList<String> listKeyAccount;
    private CountDownTimer countDownTimer;
    private boolean isDoubleBackToExit = false;
    private static String keyAccount = "";
    private static int numberZoom = 0;

    private boolean closeApp;
    private boolean isLeave;
    private String filemanagerstring, imagePath, selectedImagePath;

    private Uri imageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        closeApp = false;
        isLeave = false;

        activity_main = (RelativeLayout) findViewById(R.id.content_main);
        listAccount = new ArrayList<Account>();
        listKeyAccount = new ArrayList<String>();
        rotateLoading = (RotateLoading) findViewById(R.id.rotateLoading);
        frameLoading = (FrameLayout) findViewById(R.id.frame_loading);
        rotateLoading.start();
        tvWaitChat = (TextView)findViewById(R.id.textViewWaitChat);
        tvPleaseWait = (TextView)findViewById(R.id.textViewPleaseWait);
        imgCry = (ImageView)findViewById(R.id.imageViewCry);
        imgCry.setVisibility(View.INVISIBLE);
        imgSendImage = (ImageView)findViewById(R.id.imageViewSendImage);
        imgSendImage.setOnClickListener(this);
        btSendImage = (Button)findViewById(R.id.buttonSendImage);
        btSendImage.setOnClickListener(this);
        btCancelImage = (Button)findViewById(R.id.buttonCancelImage);
        btCancelImage.setOnClickListener(this);
        frameSendImage = (FrameLayout)findViewById(R.id.frame_sendImage);
        frameSendImage.setVisibility(View.INVISIBLE);
        imgChooseImage = (ImageView)findViewById(R.id.imageViewImageChoose);


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


        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().build(), SIGN_IN_REQUEST_CODE);

        } else {
            Snackbar.make(activity_main, "Welcome " + FirebaseAuth.getInstance().getCurrentUser().getEmail(), Snackbar.LENGTH_SHORT).show();
            // load content
            //displayChatMessage();
            signInAccount();
        }

    }

    private void signInAccount() {
        getSupportActionBar().setTitle(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
        getSupportActionBar().setSubtitle(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_chat_couple_2);

        listKeyAccount.clear();
        listAccount.clear();
        countChildren = -1;

        account = new Account(FirebaseAuth.getInstance().getCurrentUser()
                .getUid(), FirebaseAuth.getInstance().getCurrentUser().getEmail());
        account.setChat(false);

        // push values
        FirebaseDatabase.getInstance().getReference().child(ACCOUNT).child(account.getId()).setValue(account, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    Snackbar.make(activity_main, "error", Snackbar.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Login success", Toast.LENGTH_SHORT).show();
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

                    accountReceive = new Account(accountOther.getId(), accountOther.getEmail());
                    accountReceive.setChat(accountOther.isChat());

                    FirebaseDatabase.getInstance().getReference().child(ACCOUNT).child(account.getId()).setValue(account, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null) {


                                FirebaseDatabase.getInstance().getReference().child(CHAT_ROOM).child(account.getId() + "-" + accountOther.getId()).push().setValue(new
                                        ChatMessage("hello", account.getEmail(), ""));

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

                    accountReceive = new Account(accountOther.getId(), accountOther.getEmail());
                    accountReceive.setChat(accountOther.isChat());

                    FirebaseDatabase.getInstance().getReference().child(ACCOUNT).child(account.getId()).setValue(account, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                FirebaseDatabase.getInstance().getReference().child(CHAT_ROOM).child(account.getId() + "-" + accountOther.getId()).push().setValue(new
                                        ChatMessage("hello", account.getEmail(), ""));

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
                imgSend = (ImageView)v.findViewById(R.id.imageViewSend);
                imgReceive = (ImageView)findViewById(R.id.imageViewReceive);

                boolean isSend = false;

                if (FirebaseAuth.getInstance().getCurrentUser().getEmail().equals(model.getMessageUser())) {
                    isSend = true;
                } else {
                    isSend = false;
                }

                if (isSend) {
                    if(model.getMessageImage().equals("") || model.getMessageImage() == null){
                        lnReceiveMessageTotal.setVisibility(View.GONE);
                        lnSendMessage.setVisibility(View.VISIBLE);
                        lnSendMessage.setBackgroundResource(R.drawable.in_message_bg);
                        messageSendText.setText(model.getMessageText());
                        messageSendUser.setText(model.getMessageUser());
                        messageSendUser.setVisibility(View.GONE);
                        messageSendTime.setHint(android.text.format.DateFormat.format("HH:mm", model.getMessageTime()));
                    }else{
                        lnReceiveMessageTotal.setVisibility(View.GONE);
                        lnSendMessage.setVisibility(View.VISIBLE);
                        lnSendMessage.setBackgroundResource(R.drawable.in_message_bg);
                        messageSendText.setText(model.getMessageText());
                        messageSendUser.setText(model.getMessageUser());
                        messageSendUser.setVisibility(View.GONE);
                        messageSendTime.setHint(android.text.format.DateFormat.format("HH:mm", model.getMessageTime()));

                        byte[] byteArray = Base64.decode(model.getMessageImage(), Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                        imgSend.setImageBitmap(bitmap);

                    }

                } else {
                    if(model.getMessageImage().equals("") || model.getMessageImage() == null) {
                        lnReceiveMessageTotal.setVisibility(View.VISIBLE);
                        lnReceiveMessage.setBackgroundResource(R.drawable.out_message_bg);
                        lnSendMessage.setVisibility(View.GONE);
                        messageText.setText(model.getMessageText());

                        messageUser.setText(model.getMessageUser());
                        messageUser.setVisibility(View.GONE);
                        messageTime.setHint(android.text.format.DateFormat.format("HH:mm", model.getMessageTime()));
                    }else{
                        lnReceiveMessageTotal.setVisibility(View.VISIBLE);
                        lnReceiveMessage.setBackgroundResource(R.drawable.out_message_bg);
                        lnSendMessage.setVisibility(View.GONE);
                        messageText.setText(model.getMessageText());
                        messageUser.setText(model.getMessageUser());
                        messageUser.setVisibility(View.GONE);
                        messageTime.setHint(android.text.format.DateFormat.format("HH:mm", model.getMessageTime()));

                        byte[] byteArray = Base64.decode(model.getMessageImage(), Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                        imgReceive.setImageBitmap(bitmap);

                    }

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
                if (key.equals(account.getId()+"-"+accountReceive.getId())){
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
                            tvWaitChat.setText("The other person has moved, the conversation will end in "+time[0]+" seconds");
                        }

                        @Override
                        public void onFinish() {
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
        account.setChat(true);
        FirebaseDatabase.getInstance().getReference().child(ACCOUNT).child(account.getId()).setValue(account);
        if (accountReceive != null) {
            FirebaseDatabase.getInstance().getReference().child(CHAT_ROOM).child(account.getId() + "-" + accountReceive.getId()).removeValue();
            FirebaseDatabase.getInstance().getReference().child(CHAT_ROOM).child(accountReceive.getId() + "-" + account.getId()).removeValue();

        }
    }

    private void exitApp_2() {
        account.setChat(false);
        FirebaseDatabase.getInstance().getReference().child(ACCOUNT).child(account.getId()).setValue(account);

        if (accountReceive != null) {
            accountReceive.setChat(true);
            FirebaseDatabase.getInstance().getReference().child(ACCOUNT).child(accountReceive.getId()).setValue(accountReceive);
            FirebaseDatabase.getInstance().getReference().child(CHAT_ROOM).child(account.getId() + "-" + accountReceive.getId()).removeValue();
            FirebaseDatabase.getInstance().getReference().child(CHAT_ROOM).child(accountReceive.getId() + "-" + account.getId()).removeValue();
        }
        accountReceive = null;

//        recreate();
        Intent mIntent = new Intent(MainActivity.this, MainActivity.class);
        mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mIntent);
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
    }


    public byte[] imageView_To_Byte(ImageView imageView){
        BitmapDrawable drawable = (BitmapDrawable)imageView.getDrawable();
        Bitmap bitmap =     drawable.getBitmap();

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;

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

            case R.id.imageViewSendImage:
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

            case R.id.buttonCancelImage:
                frameSendImage.setVisibility(View.INVISIBLE);

                break;

            case R.id.buttonSendImage:
                frameSendImage.setVisibility(View.INVISIBLE);
                byte[] byteArray = imageView_To_Byte(imgChooseImage);
                String textImage = Base64.encodeToString(byteArray, Base64.DEFAULT);
                if (account.isChat()) {
                    if (emojiconEditText.getText().toString().equals("")) {
                        tvNotify.setText(R.string.notify_sending);
                        FirebaseDatabase.getInstance().getReference().child(CHAT_ROOM).child(account.getId() + "-" + accountReceive.getId()).push().setValue(new ChatMessage(emojiconEditText.getText().toString(),
                                FirebaseAuth.getInstance().getCurrentUser().getEmail(), textImage), new DatabaseReference.CompletionListener() {
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
                                FirebaseAuth.getInstance().getCurrentUser().getEmail(), textImage), new DatabaseReference.CompletionListener() {
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
    protected void onDestroy() {
        super.onDestroy();
        if (closeApp) exitApp_2();
        else exitApp();
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_sign_out) {
            AuthUI.getInstance().signOut(this).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Snackbar.make(activity_main, "You have been signed out", Snackbar.LENGTH_SHORT).show();
                    finish();
                }
            });
        }
        if (id == R.id.action_exit_chat) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle(R.string.sure);
            builder.setIcon(R.drawable.exit_chat);
            builder.setMessage(R.string.exit_chat);
            builder.setNegativeButton(R.string.accept, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    closeApp = true;
                    exitApp_2();

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

        return true;
    }

    @Override
    protected void onResume() {
        isDoubleBackToExit = false;
        super.onResume();
    }

    @Override
    public void onBackPressed() {

        if (isDoubleBackToExit) {
            super.onBackPressed();
        } else {
            this.isDoubleBackToExit = true;
            Toast.makeText(MainActivity.this, "double click to exit", Toast.LENGTH_SHORT).show();
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
