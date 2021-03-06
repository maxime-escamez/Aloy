package com.aloy.aloy.Util;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import com.aloy.aloy.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.ContentValues.TAG;

/**
 * Created by tldonne on 02/12/2017.
 */

public class AchievementsHandler {

    private Context context;
    private DatabaseReference achievementsRef;

    public AchievementsHandler(Context context,String username) {
        this.context=context;
        this.achievementsRef= FirebaseDatabase.getInstance().getReference("users").child(username).child("achievements");
    }

    public void setProfilePicBorder(final CircleImageView pic){
        achievementsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot achievements) {
                int score = 0;
                if (achievements.exists()){
                    for (DataSnapshot achievement : achievements.getChildren()) {
                        score += achievement.getValue(Integer.class);
                    }
                }
                if (score<10)
                    pic.setBorderColor(context.getResources().getColor(R.color.level_0_Aloy));
                else if (score>9 && score < 25)
                    pic.setBorderColor(context.getResources().getColor(R.color.level_1_Aloy));
                else if (score>24 && score < 50)
                    pic.setBorderColor(context.getResources().getColor(R.color.level_2_Aloy));
                else if (score>49 && score < 150 )
                    pic.setBorderColor(context.getResources().getColor(R.color.level_3_Aloy));
                else if (score>149 )
                    pic.setBorderColor(context.getResources().getColor(R.color.level_4_Aloy));

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void getScore(final FragmentActivity activity){
        achievementsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot achievements) {
                int score = 0;
                if (achievements.exists()){
                    for (DataSnapshot achievement : achievements.getChildren()) {
                        score += achievement.getValue(Integer.class);
                    }
                }
                if (score<10) {
                    Toast toast = Toast.makeText(activity, "You have a score of " + score + ", try to reach 10 !", Toast.LENGTH_LONG);
                    TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
                    if (v != null) v.setGravity(Gravity.CENTER);
                    toast.show();
                }
                else if (score>9 && score < 50) {
                    Toast toast = Toast.makeText(activity, "You have a score of " + score + ", try to reach 50 !", Toast.LENGTH_LONG);
                    TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
                    if (v != null) v.setGravity(Gravity.CENTER);
                    toast.show();
                }
                else if (score>49 && score < 100){
                    Toast toast = Toast.makeText(activity, "You have a score of " + score + ", try to reach 100 !", Toast.LENGTH_LONG);
                    TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
                    if (v != null) v.setGravity(Gravity.CENTER);
                    toast.show();
                }
                else if (score>99 && score < 200 ) {
                    Toast toast = Toast.makeText(activity, "You have a score of " + score + ", try to reach 200 !", Toast.LENGTH_LONG);
                    TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
                    if (v != null) v.setGravity(Gravity.CENTER);
                    toast.show();
                }
                else if (score>199 ) {
                    Toast toast = Toast.makeText(activity, "You have a score of " + score + " ! You're a master !", Toast.LENGTH_LONG);
                    TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
                    if (v != null) v.setGravity(Gravity.CENTER);
                    toast.show();

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void displayAchievement(final FragmentActivity activity, final String achievement) {
        final DatabaseReference userAchievements = achievementsRef.child(achievement);
        userAchievements.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot achievementSnapshot) {
                switch (achievement) {
                    case "questions" :
                        if (achievementSnapshot.exists() && achievementSnapshot.getValue(Integer.class)>0 ) {
                            if (achievementSnapshot.getValue(Integer.class)==1)
                                Toast.makeText(activity, "You asked " + achievementSnapshot.getValue(Integer.class) + " question", Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(activity, "You asked " + achievementSnapshot.getValue(Integer.class) + " questions", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(activity, "You never asked a question", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case "answers" :
                        if (achievementSnapshot.exists() && achievementSnapshot.getValue(Integer.class)>0 ) {
                            if (achievementSnapshot.getValue(Integer.class)==1)
                                Toast.makeText(activity, "You published " + achievementSnapshot.getValue(Integer.class) + " answer", Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(activity, "You published " + achievementSnapshot.getValue(Integer.class) + " answers", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(activity, "You never answered a question", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case "requests" :
                        if (achievementSnapshot.exists() && achievementSnapshot.getValue(Integer.class)>0) {
                            if (achievementSnapshot.getValue(Integer.class)==1)
                                Toast.makeText(activity, "You requested " + achievementSnapshot.getValue(Integer.class) + " person", Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(activity, "You requested " + achievementSnapshot.getValue(Integer.class) + " persons", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(activity, "You never requested someone", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case "answersVIP" :
                        if (achievementSnapshot.exists() && achievementSnapshot.getValue(Integer.class)>0) {
                            if (achievementSnapshot.getValue(Integer.class)==1)
                                Toast.makeText(activity, "You got answered " + achievementSnapshot.getValue(Integer.class) + " time", Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(activity, "You got answered " + achievementSnapshot.getValue(Integer.class) + " times", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(activity, "You never got answered", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case "upvotesVIP" :
                        if (achievementSnapshot.exists() && achievementSnapshot.getValue(Integer.class)>0) {
                            if (achievementSnapshot.getValue(Integer.class)==1)
                                Toast.makeText(activity, "You got upvoted " + achievementSnapshot.getValue(Integer.class) + " time", Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(activity, "You got upvoted " + achievementSnapshot.getValue(Integer.class) + " times", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(activity, "You never got upvoted", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case "requestsVIP" :
                        if (achievementSnapshot.exists() && achievementSnapshot.getValue(Integer.class)>0) {
                            if (achievementSnapshot.getValue(Integer.class)==1)
                                Toast.makeText(activity, "You got requested " + achievementSnapshot.getValue(Integer.class) + " time", Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(activity, "You got requested " + achievementSnapshot.getValue(Integer.class) + " times", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(activity, "You never got requested", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case "followersVIP" :
                        if (achievementSnapshot.exists() && achievementSnapshot.getValue(Integer.class)>0) {
                            if (achievementSnapshot.getValue(Integer.class)==1)
                                Toast.makeText(activity, "Your questions got followed " + achievementSnapshot.getValue(Integer.class) + " time", Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(activity, "Your questions got followed " + achievementSnapshot.getValue(Integer.class) + " times", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(activity, "Your questions never got followed", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case "followersTOP" :
                        if (achievementSnapshot.exists() && achievementSnapshot.getValue(Integer.class)>0) {
                            if (achievementSnapshot.getValue(Integer.class)==1)
                                Toast.makeText(activity, "Your most followed question has " + achievementSnapshot.getValue(Integer.class) + " follower", Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(activity, "Your most followed question has " + achievementSnapshot.getValue(Integer.class) + " followers", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(activity, "Your questions never got followed", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case "answersTOP" :
                        if (achievementSnapshot.exists() && achievementSnapshot.getValue(Integer.class)>0) {
                            if (achievementSnapshot.getValue(Integer.class)==1)
                                Toast.makeText(activity, "Your most answered question has " + achievementSnapshot.getValue(Integer.class) + " answer", Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(activity, "Your most answered question has " + achievementSnapshot.getValue(Integer.class) + " answers", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(activity, "Your questions never got answered", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case "upvotesTOP" :
                        if (achievementSnapshot.exists() && achievementSnapshot.getValue(Integer.class)>0) {
                            if (achievementSnapshot.getValue(Integer.class)==1)
                                Toast.makeText(activity, "Your most upvoted answer has " + achievementSnapshot.getValue(Integer.class) + " upvote", Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(activity, "Your most upvoted answer has " + achievementSnapshot.getValue(Integer.class) + " upvotes", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(activity, "Your answers never got upvoted", Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG,"addListenerForSingleValueEvent:failure",databaseError.toException());
            }
        });
    }


    public void getAchievements(final CircleImageView achievementView, String type) {
        achievementsRef.child(type).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot achievements) {
                if ((!achievements.exists()) || achievements.getValue(Integer.class)<1)
                    achievementView.setBorderColor(context.getResources().getColor(R.color.level_0_Aloy));
                else if (achievements.getValue(Integer.class)>0 && achievements.getValue(Integer.class)<5)
                    achievementView.setBorderColor(context.getResources().getColor(R.color.level_1_Aloy));
                else if (achievements.getValue(Integer.class)>4 && achievements.getValue(Integer.class)<15)
                    achievementView.setBorderColor(context.getResources().getColor(R.color.level_2_Aloy));
                else if (achievements.getValue(Integer.class)>14 && achievements.getValue(Integer.class)<30)
                    achievementView.setBorderColor(context.getResources().getColor(R.color.level_3_Aloy));
                else if (achievements.getValue(Integer.class)>29 && achievements.getValue(Integer.class)<50)
                    achievementView.setBorderColor(context.getResources().getColor(R.color.level_4_Aloy));
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }
}
