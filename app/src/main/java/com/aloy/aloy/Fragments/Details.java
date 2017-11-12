package com.aloy.aloy.Fragments;

import android.content.Intent;
import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.aloy.aloy.Adapters.AnswersAdapter;
import com.aloy.aloy.Contracts.QuestionDetailsContract;
import com.aloy.aloy.MainActivity;
import com.aloy.aloy.Models.Answer;
import com.aloy.aloy.Models.Question;
import com.aloy.aloy.Presenters.QuestionDetailsPresenter;
import com.aloy.aloy.R;
import com.aloy.aloy.Util.SharedPreferenceHelper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import de.hdodenhof.circleimageview.CircleImageView;
import static android.content.ContentValues.TAG;

public class Details extends AppCompatActivity implements QuestionDetailsContract.View {
    private QuestionDetailsPresenter questionDetailsPresenter;
    private ArrayList<Answer> adapterAnswer;
    private ArrayList<String> adapterKeys;
    private CircleImageView profilePic;
    private TextView username;
    private Button request;
    private TextView body ;
    private ImageView cover1 ;
    private ImageView cover2 ;
    private View questionView;
    public static final String USERNAME_TRANSITION_NAME = "usernameTransitionName" ;
    public static final String PROFILE_PIC_TRANSITION_NAME = "profilePicTransitionName" ;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_question_details);
        supportPostponeEnterTransition();
        questionDetailsPresenter = new QuestionDetailsPresenter(this, MainActivity.getDataHandler());
        SharedPreferenceHelper mSharedPreferenceHelper = new SharedPreferenceHelper(this);
        Bundle extras = getIntent().getExtras();
        final Question question = extras.getParcelable(Feed.EXTRA_QUESTION);
        String transitionName = extras.getString(Feed.EXTRA_QUESTION_TRANSITION_NAME);

        setupQuestion(question,transitionName,this);
        setupRecyclerView(question.getId());

        supportStartPostponedEnterTransition();


        if (!(question.getUsername().equals(mSharedPreferenceHelper.getCurrentUserId()))) {
            username.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openProfile(question);
                }
            });
        }
        request.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                showRequest(question.getId());
            }
        });

    }

    private void openProfile(Question question) {
        Intent intent = new Intent(this, Profile.class);
        ViewCompat.setTransitionName(username,question.getUsername());
        ViewCompat.setTransitionName(profilePic,question.getPic());
        Pair usernamePair = Pair.create(username,ViewCompat.getTransitionName(username));
        Pair picturePair = Pair.create(profilePic,ViewCompat.getTransitionName(profilePic));
        intent.putExtra(USERNAME_TRANSITION_NAME, ViewCompat.getTransitionName(username));
        intent.putExtra(PROFILE_PIC_TRANSITION_NAME, ViewCompat.getTransitionName(profilePic));

        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this,
                usernamePair,
                picturePair);
        startActivity(intent, options.toBundle());

    }

    @Override
    public void setupRecyclerView(String questionId) {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.answerRecyclerView);
        AnswersAdapter answerRecyclerAdapter = new AnswersAdapter(questionDetailsPresenter.getRef(questionId), adapterAnswer, adapterKeys, this, questionDetailsPresenter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(answerRecyclerAdapter);
    }

    @Override
    public void setupQuestion(Question question, String transitionName,final Context context){
        profilePic = (CircleImageView) findViewById(R.id.questionDetailProfilePic);
        username = (TextView) findViewById(R.id.questionDetailUsername);
        body = (TextView) findViewById(R.id.questionDetailBody);
        cover1 = (ImageView) findViewById(R.id.questionDetailCover1);
        cover2 = (ImageView) findViewById(R.id.questionDetailCover2);
        request = (Button) findViewById(R.id.requestButton);

        body.setText(question.getBody());
        username.setText(question.getUsername());

        FirebaseDatabase.getInstance().getReference("users").child(question.getUsername()).child("pic").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot userURL) {
                Picasso.with(context).load(userURL.getValue().toString()).into(profilePic);
                profilePic.setVisibility(View.VISIBLE);

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG,"addListenerForSingleValueEvent:failure",databaseError.toException());
            }
        });
        Picasso.with(this).load(question.getPic()).into(profilePic);
        Picasso.with(this).load(question.getCover1()).into(cover1);
        Picasso.with(this).load(question.getCover2()).into(cover2);
        questionView = findViewById(R.id.questionDetail);
        questionView.setTransitionName(transitionName);
    }

    @Override
    public void showRequest(String questionId) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        RequestDialog requestDialog = new RequestDialog();
        Bundle args = new Bundle();
        args.putString("questionId", questionId);
        requestDialog.setArguments(args);
        requestDialog.show(fragmentManager,"request");
    }

    @Override
    public QuestionDetailsPresenter getPresenter() {
        return this.questionDetailsPresenter;
    }

    @Override
    public void onBackPressed() {
         //finishAfterTransition();
        finish();
    }




}



