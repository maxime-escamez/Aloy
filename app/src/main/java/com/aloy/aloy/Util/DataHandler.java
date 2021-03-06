package com.aloy.aloy.Util;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.aloy.aloy.Adapters.InterestsAdapter;
import com.aloy.aloy.Adapters.SearchAdapter;
import com.aloy.aloy.Contracts.SearchContract;
import com.aloy.aloy.Views.Feed;
import com.aloy.aloy.Models.Answer;
import com.aloy.aloy.Models.MainUser;
import com.aloy.aloy.Models.Question;
import com.aloy.aloy.Models.QuestionHolder;
import com.aloy.aloy.Models.SpotifyItem;
import com.aloy.aloy.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Track;

import static android.content.ContentValues.TAG;

/**
 * Created by tldonne on 29/10/2017.
 */

public class DataHandler {

    private DatabaseReference refQuestionFeed;
    private DatabaseReference refUser;
    private DatabaseReference refCategories;
    private SharedPreferenceHelper sharedPreferenceHelper;
    private String profilePicture;
    private Context context;



    public DataHandler(Context context){
        sharedPreferenceHelper = new SharedPreferenceHelper(context);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        refQuestionFeed = database.getReference("questions");
        refUser = database.getReference("users");
        refCategories = database.getReference("categories");
        this.context=context;
    }

    public DatabaseReference getRefQuestionFeed(){
        return refQuestionFeed;
    }

    public DatabaseReference getRefAnswers(String questionId) {
        //return database.getReference(questionId);
        return refQuestionFeed.child(questionId).child("answers");
    }

    public DatabaseReference pushQuestionRef(){
        DatabaseReference myRef = refQuestionFeed.push();
        return myRef;
    }


    public void saveQuestion(final Question question, final HashMap<String,Track> tracks, final HashMap<String,Artist> artists, final HashMap<String,Album> albums, final HashMap<String,String> genres, DatabaseReference ref) {
        ref.setValue(question,new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                //databaseReference.child("id").setValue(databaseReference.getKey());
                int index = 0;

                for (HashMap.Entry<String, Track> entry : tracks.entrySet()) {
                    String cover = entry.getValue().album.images.get(0).url;
                    String name = entry.getValue().name;
                    String artist = entry.getValue().artists.get(0).name;
                    String spotifyId = entry.getKey();
                    String type = "track";
                    String uri = entry.getValue().uri;
                    refQuestionFeed.child(databaseReference.getKey()).child("items").child(Integer.toString(index)).setValue(new SpotifyItem(cover,name,artist,spotifyId,type,uri));
                    index++;
                }
                for (HashMap.Entry<String, Artist> artistEntry : artists.entrySet()) {
                    String cover = artistEntry.getValue().images.get(0).url;
                    String name = artistEntry.getValue().name;
                    String artist = artistEntry.getValue().name;
                    String spotifyId = artistEntry.getKey();
                    String type = "artist";
                    String uri = artistEntry.getValue().uri;
                    refQuestionFeed.child(databaseReference.getKey()).child("items").child(Integer.toString(index)).setValue(new SpotifyItem(cover,name,artist,spotifyId,type,uri));
                    index++;

                }
                for (HashMap.Entry<String, Album> album : albums.entrySet()) {
                    String cover = album.getValue().images.get(0).url;
                    String name = album.getValue().name;
                    String artist = album.getValue().artists.get(0).name;
                    String spotifyId = album.getKey();
                    String type = "album";
                    String uri = album.getValue().uri;
                    refQuestionFeed.child(databaseReference.getKey()).child("items").child(Integer.toString(index)).setValue(new SpotifyItem(cover,name,artist,spotifyId,type,uri));
                    index++;

                }
                for (HashMap.Entry<String, String> genre : genres.entrySet()) {
                    databaseReference.child("genres").child(genre.getKey()).child("cover").setValue(genre.getValue());
                    refCategories.child(genreToNumber(genre.getKey())).child("questions").child(databaseReference.getKey()).setValue("true");
                }
                refUser.child(sharedPreferenceHelper.getCurrentUserId()).child("questions").child(databaseReference.getKey()).setValue("true");
                updateAchievement("questions");
                follow(databaseReference.getKey());
            }
        });
    }

    public void updateAchievement(String achievement){
        final DatabaseReference userAchievement = refUser.child(sharedPreferenceHelper.getCurrentUserId()).child("achievements").child(achievement);
        userAchievement.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot achievement) {
                if (achievement.exists()) {
                    userAchievement.setValue(achievement.getValue(Integer.class)+1);
                }
                else {
                    userAchievement.setValue(1);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG,"addListenerForSingleValueEvent:failure",databaseError.toException());
            }
        });
    }

    public void updateVipAchievement(DatabaseReference usernameRef, final String achievement, final boolean add) {
        usernameRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot username) {
                final DatabaseReference userAchievement = refUser.child(username.getValue().toString()).child("achievements").child(achievement);
                userAchievement.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot achievement) {
                        if (achievement.exists()) {
                            if (add)
                                userAchievement.setValue(achievement.getValue(Integer.class)+1);
                            else
                                userAchievement.setValue(achievement.getValue(Integer.class)-1);
                        }
                        else {
                            userAchievement.setValue(1);
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG,"addListenerForSingleValueEvent:failure",databaseError.toException());
                    }
                });
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG,"addListenerForSingleValueEvent:failure",databaseError.toException());
            }
        });
    }

    public void updateTopAchievement (DatabaseReference usernameRef,final String achievement,final DatabaseReference countRef) {
        usernameRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot username) {
                final DatabaseReference userAchievement = refUser.child(username.getValue().toString()).child("achievements").child(achievement);
                userAchievement.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot achievement) {
                        countRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot count) {
                                if (achievement.exists()) {
                                    if ((int) count.getChildrenCount() > achievement.getValue(Integer.class))
                                        userAchievement.setValue((int) count.getChildrenCount());
                                }
                                else
                                    userAchievement.setValue(1);
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG,"addListenerForSingleValueEvent:failure",databaseError.toException());
                    }
                });
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG,"addListenerForSingleValueEvent:failure",databaseError.toException());
            }
        });

    }




    public void saveUser(MainUser user){
        refUser.child(sharedPreferenceHelper.getCurrentUserId()).setValue(user);

    }

    public void updateData(String username ){
        refUser.child(username).child("pic").setValue(sharedPreferenceHelper.getProfilePicture());
        refUser.child(username).child("name").setValue(sharedPreferenceHelper.getCurrentUserName());
        refUser.child(username).child("id").setValue(sharedPreferenceHelper.getCurrentUserId());
        refUser.child(username).child("notificationTokens").child(FirebaseInstanceId.getInstance().getToken()).setValue("true");
    }

    public DatabaseReference pushAnswerRef(String questionId) {
        DatabaseReference myRef = refQuestionFeed.child(questionId).child("answers").push();
        return myRef;
    }

    public void saveAnswer(Answer answer, final String questionID, final DatabaseReference answerRef, final LinkedHashMap<String, Track> tracksSelected, final HashMap<String,Artist> artistsSelected, final HashMap<String,Album> albumsSelected, final HashMap<String,String> genreSelected) {
        answerRef.setValue(answer,new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError,
                                   DatabaseReference databaseReference) {
                //databaseReference.child("id").setValue(databaseReference.getKey());
                int index = 0;

                for (HashMap.Entry<String, Track> entry : tracksSelected.entrySet()) {
                    String cover = entry.getValue().album.images.get(0).url;
                    String name = entry.getValue().name;
                    String artist = entry.getValue().artists.get(0).name;
                    String spotifyId = entry.getKey();
                    String type = "track";
                    String uri = entry.getValue().uri;
                    databaseReference.child("items").child(Integer.toString(index)).setValue(new SpotifyItem(cover,name,artist,spotifyId,type,uri));
                    index++;

                    //refQuestionFeed.child(databaseReference.getKey()).child("tracks").child(entry.getKey()).setValue(entry.getValue());
                }
                for (HashMap.Entry<String, Artist> artistEntry : artistsSelected.entrySet()) {
                    String cover = artistEntry.getValue().images.get(0).url;
                    String name = artistEntry.getValue().name;
                    String artist = artistEntry.getValue().name;
                    String spotifyId = artistEntry.getKey();
                    String type = "artist";
                    String uri = artistEntry.getValue().uri;
                    databaseReference.child("items").child(Integer.toString(index)).setValue(new SpotifyItem(cover,name,artist,spotifyId,type,uri));
                    index++;

                }
                for (HashMap.Entry<String, Album> album : albumsSelected.entrySet()) {
                    String cover = album.getValue().images.get(0).url;
                    String name = album.getValue().name;
                    String artist = album.getValue().artists.get(0).name;
                    String spotifyId = album.getKey();
                    String type = "album";
                    String uri = album.getValue().uri;
                    databaseReference.child("items").child(Integer.toString(index)).setValue(new SpotifyItem(cover,name,artist,spotifyId,type,uri));
                    index++;

                }
                for (HashMap.Entry<String, String> genre : genreSelected.entrySet()) {
                    databaseReference.child("genres").child(genre.getKey()).child("cover").setValue(genre.getValue());
                }
                updateAchievement("answers");
                updateVipAchievement(refQuestionFeed.child(questionID).child("username"),"answersVIP",true);
                updateTopAchievement(refQuestionFeed.child(questionID).child("username"),"answersTOP", answerRef.getParent());


                refUser.child(sharedPreferenceHelper.getCurrentUserId()).child("answers").child(questionID).setValue(databaseReference.getKey());
            }
        });
    }



    public void upvote(final DatabaseReference questionRef, final String answerId) {
        final DatabaseReference mUpvotersReference = questionRef.child(answerId).child("upvotes").child("users");
        final DatabaseReference mUpvotesReference = questionRef.child(answerId).child("upvotes").child("number");
        mUpvotesReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot votes) {
                mUpvotersReference.child(sharedPreferenceHelper.getCurrentUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot voter) {
                        if (voter.exists()) {
                            mUpvotesReference.setValue(votes.getValue(Integer.class)-1);
                            voter.getRef().removeValue();
                            updateVipAchievement(questionRef.child(answerId).child("username"),"upvotesVIP",false);

                        }else{
                            voter.getRef().setValue("voted");
                            updateVipAchievement(questionRef.child(answerId).child("username"),"upvotesVIP",true);
                            if (votes.exists()) {
                                mUpvotesReference.setValue(votes.getValue(Integer.class)+1);
                            }else{
                                mUpvotesReference.setValue(1);
                            }
                        }
                        updateTopAchievement(questionRef.child(answerId).child("username"),"upvotesTOP", mUpvotersReference);
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG,"addListenerForSingleValueEvent:failure",databaseError.toException());
                    }
                });
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG,"addListenerForSingleValueEvent:failure",databaseError.toException());
            }
        });
    }


    public void bindGenre(final SearchAdapter.ViewHolder holder, final int position, final Context context){
        refCategories.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                    holder.primaryText.setText(dataSnapshot.child(String.valueOf(position)).child("name").getValue().toString());
                    holder.secondaryText.setVisibility(View.GONE);
                    Picasso.with(context).load(dataSnapshot.child(String.valueOf(position)).child("pic").getValue().toString()).into(holder.cover);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "addListenerForSingleValueEvent:failure", databaseError.toException());
            }
        });
    }

    public void bindInterests(final InterestsAdapter.ViewHolder holder, final int position, final Context context){
        refCategories.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                holder.primaryText.setText(dataSnapshot.child(String.valueOf(position)).child("name").getValue().toString());
                holder.secondaryText.setVisibility(View.GONE);
                Picasso.with(context).load(dataSnapshot.child(String.valueOf(position)).child("pic").getValue().toString()).into(holder.cover);
                holder.cover.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "addListenerForSingleValueEvent:failure", databaseError.toException());
            }
        });
    }

    public void addGenre(final int position, final SearchContract.View searchView, final boolean add){
        refCategories.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (add) {
                        searchView.getAsk().getAskPresenter().addGenre(dataSnapshot.child(String.valueOf(position)).child("name").getValue().toString(), dataSnapshot.child(String.valueOf(position)).child("pic").getValue().toString());
                }
                else {
                        searchView.getAsk().getAskPresenter().removeGenre(dataSnapshot.child(String.valueOf(position)).child("name").getValue().toString());
                }
                searchView.updateCount("genre");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "addListenerForSingleValueEvent:failure", databaseError.toException());
            }
        });
    }



    public void request(final String target, final String questionId){
        final DatabaseReference mRequestReference = refUser.child(target).child("requests");
        mRequestReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot requests) {
                if(!target.equals(sharedPreferenceHelper.getCurrentUserId())) {
                    if (!requests.exists()) {
                        mRequestReference.child(questionId).child("requesters").child(sharedPreferenceHelper.getCurrentUserId()).setValue("requested");
                    } else {
                        for (DataSnapshot request : requests.getChildren()) {
                            if ((request.getValue().toString()).equals(questionId)) {
                                if (!((request.child("requesters").child(sharedPreferenceHelper.getCurrentUserId())).exists())) {
                                    mRequestReference.child(questionId).child("requesters").child(sharedPreferenceHelper.getCurrentUserId()).setValue("requested");
                                    updateVipAchievement(refUser.child(target),"requestsVIP",true);

                                }
                            } else {
                                //mRequestReference.setValue(questionId);
                                mRequestReference.child(questionId).child("requesters").child(sharedPreferenceHelper.getCurrentUserId()).setValue("requested");
                            }
                        }
                    }
                    updateAchievement("requests");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG,"addListenerForSingleValueEvent:failure",databaseError.toException());
            }
        });

    }

    public void follow(final String questionId){

        final DatabaseReference mFollowersReference = refQuestionFeed.child(questionId).child("following").child("users");
        final DatabaseReference mFollowingReference = refQuestionFeed.child(questionId).child("following").child("number");
        final DatabaseReference mUserFollow = refUser.child(sharedPreferenceHelper.getCurrentUserId()).child("following");
        mFollowingReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot following) {
                mFollowersReference.child(sharedPreferenceHelper.getCurrentUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot follower) {
                        if (follower.exists()) {
                            refQuestionFeed.child(questionId).child("following").child("number").setValue(following.getValue(Integer.class)-1);
                            refQuestionFeed.child(questionId).child("following").child("users").child(sharedPreferenceHelper.getCurrentUserId()).removeValue();
                            mUserFollow.child(questionId).removeValue();
                            updateVipAchievement(refQuestionFeed.child(questionId).child("username"),"followersVIP",false);
                            refQuestionFeed.child(questionId).child("following").child("notificationTokens").child(FirebaseInstanceId.getInstance().getToken()).removeValue();


                        }else{
                            refQuestionFeed.child(questionId).child("following").child("users").child(sharedPreferenceHelper.getCurrentUserId()).setValue("is following");
                            mUserFollow.child(questionId).setValue("true");
                            updateVipAchievement(refQuestionFeed.child(questionId).child("username"),"followersVIP",true);

                            if (following.exists()) {
                                refQuestionFeed.child(questionId).child("following").child("number").setValue(following.getValue(Integer.class)+1);
                            }else{
                                refQuestionFeed.child(questionId).child("following").child("number").setValue(1);
                            }
                            updateTopAchievement(refQuestionFeed.child(questionId).child("username"),"followersTOP", mFollowersReference);
                            refQuestionFeed.child(questionId).child("following").child("notificationTokens").child(FirebaseInstanceId.getInstance().getToken()).setValue("true");

                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG,"addListenerForSingleValueEvent:failure",databaseError.toException());
                    }
                });
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG,"addListenerForSingleValueEvent:failure",databaseError.toException());
            }
        });

    }

    public void getUpvote(final String questionId, final String answerId, final ImageButton upvote) {
        final DatabaseReference mUpvoterReference = refQuestionFeed.child(questionId).child("answers").child(answerId).child("upvotes").child("users").child(sharedPreferenceHelper.getCurrentUserId());
        final DatabaseReference mUpvotesReference = refQuestionFeed.child(questionId).child("answers").child(answerId).child("upvotes").child("number");
        mUpvoterReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot voter) {
                if (voter.exists()) {
                    //Log.i("votes",""+(int)votes.getValue());
                    upvote.setImageResource(R.drawable.ic_favorite);
                    upvote.setTag(R.drawable.ic_favorite);

                }else{
                    upvote.setImageResource(R.drawable.ic_favorite_border);
                    upvote.setTag(R.drawable.ic_favorite_border);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG,"addListenerForSingleValueEvent:failure",databaseError.toException());
            }
        });

    }

    public void getFollow(String questionId, final ImageButton follow) {
        final DatabaseReference mFollowingUser = refQuestionFeed.child(questionId).child("following").child("users").child(sharedPreferenceHelper.getCurrentUserId());
        mFollowingUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot follower) {
                if (follower.exists()) {
                    //Log.i("votes",""+(int)votes.getValue());
                    follow.setImageResource(R.drawable.ic_playlist_add_check);
                    follow.setTag(R.drawable.ic_playlist_add_check);

                }else{
                    follow.setImageResource(R.drawable.ic_playlist_add);
                    follow.setTag(R.drawable.ic_playlist_add);

                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG,"addListenerForSingleValueEvent:failure",databaseError.toException());
            }
        });

    }


    public void getUrl(String userId, final CircleImageView holder, final Context context) {
        refUser.child(userId).child("pic").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot userURL) {
                Picasso.with(context).load(userURL.getValue().toString()).into(holder);
                holder.setVisibility(View.VISIBLE);

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(Feed.TAG,"addListenerForSingleValueEvent:failure",databaseError.toException());
            }

        });
    }

    public String genreToNumber(String category){
        String result="";
        switch(category){
            case "Blues" :
                result="0";
                break;
            case "Chill" :
                result="1";
                break;
            case "Christmas" :
                result="2";
                break;
            case "Classical" :
                result="3";
                break;
            case "Country" :
                result="4";
                break;
            case "Electro" :
                result="5";
                break;
            case "Focus" :
                result="6";
                break;
            case "Folk" :
                result="7";
                break;
            case "Funk" :
                result="8";
                break;
            case "Gaming" :
                result="9";
                break;
            case "Hip-Hop" :
                result="10";
                break;
            case "Indie" :
                result="11";
                break;
            case "Jazz" :
                result="12";
                break;
            case "Latin" :
                result="13";
                break;
            case "Metal" :
                result="14";
                break;
            case "Mood" :
                result="15";
                break;
            case "Party" :
                result="16";
                break;
            case "Pop" :
                result="17";
                break;
            case "Punk" :
                result="18";
                break;
            case "Reggae" :
                result="19";
                break;
            case "RnB" :
                result="20";
                break;
            case "Rock" :
                result="21";
                break;
            case "Sleep" :
                result="22";
                break;
            case "Soul" :
                result="23";
                break;
            case "Travel" :
                result="24";
                break;
            case "Workout" :
                result="25";
                break;
        }
        return result;
    }

    public String NumberToGenre(String number) {
        String result = "";
        switch (number) {
            case "0":
                result = "Blues";
                break;
            case "1":
                result = "Chill";
                break;
            case "2":
                result = "Christmas";
                break;
            case "3":
                result = "Classical";
                break;
            case "4":
                result = "Country";
                break;
            case "5":
                result = "Electro";
                break;
            case "6":
                result = "Focus";
                break;
            case "7":
                result = "Folk";
                break;
            case "8":
                result = "Funk";
                break;
            case "9":
                result = "Gaming";
                break;
            case "10":
                result = "Hip-Hop";
                break;
            case "11":
                result = "Indie";
                break;
            case "12":
                result = "Jazz";
                break;
            case "13":
                result = "Latin";
                break;
            case "14":
                result = "Metal";
                break;
            case "15":
                result = "Mood";
                break;
            case "16":
                result = "Party";
                break;
            case "17":
                result = "Pop";
                break;
            case "18":
                result = "Punk";
                break;
            case "19":
                result = "Reggae";
                break;
            case "20":
                result = "RnB";
                break;
            case "21":
                result = "Rock";
                break;
            case "22":
                result = "Sleep";
                break;
            case "23":
                result = "Soul";
                break;
            case "24":
                result = "Travel";
                break;
            case "25":
                result = "Workout";
                break;
        }
        return result;
    }

    public void getItems(final String id, final QuestionHolder holder, final Context context) {
        refQuestionFeed.child(id).child("items").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot items) {
                if (items.exists()){
                    System.out.println(id + "---" + items.getChildrenCount());
                    switch ((int) items.getChildrenCount()) {
                        case 1 :
                            Picasso.with(context).load(items.child("0").child("cover").getValue().toString()).into(holder.getCover1());
                            holder.getTextCover1().setText(items.child("0").child("artist").getValue().toString());
                            holder.getCover1().setVisibility(View.VISIBLE);
                            holder.getTextCover1().setVisibility(View.VISIBLE);
                            holder.getItems().setVisibility(View.VISIBLE);
                            break;
                        case 2 :
                            Picasso.with(context).load(items.child("0").child("cover").getValue().toString()).into(holder.getCover1());
                            holder.getTextCover1().setText(items.child("0").child("artist").getValue().toString());
                            Picasso.with(context).load(items.child("1").child("cover").getValue().toString()).into(holder.getCover2());
                            holder.getTextCover2().setText(items.child("1").child("artist").getValue().toString());
                            holder.getCover1().setVisibility(View.VISIBLE);
                            holder.getTextCover1().setVisibility(View.VISIBLE);
                            holder.getCover2().setVisibility(View.VISIBLE);
                            holder.getTextCover2().setVisibility(View.VISIBLE);
                            holder.getItems().setVisibility(View.VISIBLE);

                            break;
                        case 3:
                            Picasso.with(context).load(items.child("0").child("cover").getValue().toString()).into(holder.getCover1());
                            holder.getTextCover1().setText(items.child("0").child("artist").getValue().toString());
                            Picasso.with(context).load(items.child("1").child("cover").getValue().toString()).into(holder.getCover2());
                            holder.getTextCover2().setText(items.child("1").child("artist").getValue().toString());
                            Picasso.with(context).load(items.child("2").child("cover").getValue().toString()).into(holder.getCover3());
                            holder.getTextCover3().setText(items.child("2").child("artist").getValue().toString());
                            holder.getCover1().setVisibility(View.VISIBLE);
                            holder.getTextCover1().setVisibility(View.VISIBLE);
                            holder.getCover2().setVisibility(View.VISIBLE);
                            holder.getTextCover2().setVisibility(View.VISIBLE);
                            holder.getCover3().setVisibility(View.VISIBLE);
                            holder.getTextCover3().setVisibility(View.VISIBLE);
                            holder.getItems().setVisibility(View.VISIBLE);

                            break;
                        default:
                            Picasso.with(context).load(items.child("0").child("cover").getValue().toString()).into(holder.getCover1());
                            holder.getTextCover1().setText(items.child("0").child("artist").getValue().toString());
                            Picasso.with(context).load(items.child("1").child("cover").getValue().toString()).into(holder.getCover2());
                            holder.getTextCover2().setText(items.child("1").child("artist").getValue().toString());
                            Picasso.with(context).load(items.child("2").child("cover").getValue().toString()).into(holder.getCover3());
                            holder.getTextCover3().setText(items.child("2").child("artist").getValue().toString());
                            holder.getCover1().setVisibility(View.VISIBLE);
                            holder.getTextCover1().setVisibility(View.VISIBLE);
                            holder.getCover2().setVisibility(View.VISIBLE);
                            holder.getTextCover2().setVisibility(View.VISIBLE);
                            holder.getCover3().setVisibility(View.VISIBLE);
                            holder.getTextCover3().setVisibility(View.VISIBLE);
                            holder.getMoreItems().setVisibility(View.VISIBLE);
                            holder.getItems().setVisibility(View.VISIBLE);
                            break;

                    }
                }
                else {
                    holder.getMoreItems().setVisibility(View.GONE);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void getStyles(final String id, final QuestionHolder holder, final Context context) {
        refQuestionFeed.child(id).child("genres").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot genres) {
                if (genres.exists()){
                    Iterator<DataSnapshot> iterator = genres.getChildren().iterator();
                    switch ((int) genres.getChildrenCount()) {
                        case 1 :
                            holder.getStyle1().setText(iterator.next().getKey());
                            holder.getStyle1().setVisibility(View.VISIBLE);
                            break;
                        case 2 :
                            holder.getStyle1().setText(iterator.next().getKey());
                            holder.getStyle2().setText(iterator.next().getKey());
                            holder.getStyle1().setVisibility(View.VISIBLE);
                            holder.getStyle2().setVisibility(View.VISIBLE);
                            break;
                        default:
                            holder.getStyle1().setText(iterator.next().getKey());
                            holder.getStyle2().setText(iterator.next().getKey());
                            holder.getStyle3().setText(iterator.next().getKey());
                            holder.getStyle1().setVisibility(View.VISIBLE);
                            holder.getStyle2().setVisibility(View.VISIBLE);
                            holder.getStyle3().setVisibility(View.VISIBLE);
                            break;
                    }

                }
                else {
                    //holder.getStyles().setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    public void checkItems(final RecyclerView items, String id) {
        refQuestionFeed.child(id).child("items").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists())
                    items.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
