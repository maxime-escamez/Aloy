package com.aloy.aloy.Util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.aloy.aloy.Adapters.AnswersAdapter;
import com.aloy.aloy.Adapters.InterestsAdapter;
import com.aloy.aloy.Adapters.SearchAdapter;
import com.aloy.aloy.Contracts.SearchContract;
import com.aloy.aloy.Fragments.Feed;
import com.aloy.aloy.Models.Answer;
import com.aloy.aloy.Models.MainUser;
import com.aloy.aloy.Models.Question;
import com.aloy.aloy.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import kaaes.spotify.webapi.android.models.AlbumSimple;
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


    public void saveQuestion(final Question question, final HashMap<String,Track> tracks, final HashMap<String,Artist> artists, final HashMap<String,AlbumSimple> albums, final HashMap<String,String> genres,DatabaseReference ref) {
        ref.setValue(question,new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                //databaseReference.child("id").setValue(databaseReference.getKey());

                for (HashMap.Entry<String, Track> entry : tracks.entrySet()) {
                    //refQuestionFeed.child(databaseReference.getKey()).child("tracks").child(entry.getKey()).setValue(entry.getValue());
                    refQuestionFeed.child(databaseReference.getKey()).child("tracks").child(entry.getKey()).child("name").setValue(entry.getValue().name);
                    refQuestionFeed.child(databaseReference.getKey()).child("tracks").child(entry.getKey()).child("uri").setValue(entry.getValue().uri);
                    refQuestionFeed.child(databaseReference.getKey()).child("tracks").child(entry.getKey()).child("artist").setValue(entry.getValue().artists.get(0).name);
                    refQuestionFeed.child(databaseReference.getKey()).child("tracks").child(entry.getKey()).child("cover").setValue(entry.getValue().album.images.get(0).url);
                }
                for (HashMap.Entry<String, Artist> artist : artists.entrySet()) {
                    refQuestionFeed.child(databaseReference.getKey()).child("artists").child(artist.getKey()).child("name").setValue(artist.getValue().name);
                    refQuestionFeed.child(databaseReference.getKey()).child("artists").child(artist.getKey()).child("uri").setValue(artist.getValue().uri);
                    refQuestionFeed.child(databaseReference.getKey()).child("artists").child(artist.getKey()).child("cover").setValue(artist.getValue().images.get(0).url);
                }
                for (HashMap.Entry<String, AlbumSimple> album : albums.entrySet()) {
                    refQuestionFeed.child(databaseReference.getKey()).child("albums").child(album.getKey()).child("name").setValue(album.getValue().name);
                    refQuestionFeed.child(databaseReference.getKey()).child("albums").child(album.getKey()).child("uri").setValue(album.getValue().uri);
                    refQuestionFeed.child(databaseReference.getKey()).child("albums").child(album.getKey()).child("cover").setValue(album.getValue().images.get(0).url);
                }
                for (HashMap.Entry<String, String> genre : genres.entrySet()) {
                    databaseReference.child("genres").child(genre.getKey()).child("cover").setValue(genre.getValue());
                    refCategories.child(genreToNumber(genre.getKey())).child("questions").child(databaseReference.getKey()).setValue("true");
                }
                refUser.child(sharedPreferenceHelper.getCurrentUserId()).child("questions").child(databaseReference.getKey()).setValue("true");
                follow(databaseReference.getKey());
            }
        });
    }

    public void saveUser(MainUser user){
        refUser.child(sharedPreferenceHelper.getCurrentUserId()).setValue(user);

    }

    public void updateData(String username){
        refUser.child(username).child("pic").setValue(sharedPreferenceHelper.getProfilePicture());
    }

    public DatabaseReference pushAnswerRef(String questionId) {
        DatabaseReference myRef = refQuestionFeed.child(questionId).child("answers").push();
        return myRef;
    }

    public void saveAnswer(Answer answer, final String questionID,DatabaseReference answerId, final LinkedHashMap<String, Track> tracksSelected, final HashMap<String,Artist> artistsSelected, final HashMap<String,AlbumSimple> albumsSelected, final HashMap<String,String> genreSelected) {
        answerId.setValue(answer,new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError,
                                   DatabaseReference databaseReference) {
                //databaseReference.child("id").setValue(databaseReference.getKey());

                for (HashMap.Entry<String, Track> entry : tracksSelected.entrySet()) {
                    //refQuestionFeed.child(databaseReference.getKey()).child("tracks").child(entry.getKey()).setValue(entry.getValue());
                    databaseReference.child("tracks").child(entry.getKey()).child("name").setValue(entry.getValue().name);
                    databaseReference.child("tracks").child(entry.getKey()).child("uri").setValue(entry.getValue().uri);
                    databaseReference.child("tracks").child(entry.getKey()).child("artist").setValue(entry.getValue().artists.get(0).name);
                    databaseReference.child("tracks").child(entry.getKey()).child("cover").setValue(entry.getValue().album.images.get(0).url);
                }
                for (HashMap.Entry<String, Artist> artist : artistsSelected.entrySet()) {
                    databaseReference.child("artists").child(artist.getKey()).child("name").setValue(artist.getValue().name);
                    databaseReference.child("artists").child(artist.getKey()).child("uri").setValue(artist.getValue().uri);
                    databaseReference.child("artists").child(artist.getKey()).child("cover").setValue(artist.getValue().images.get(0).url);
                }
                for (HashMap.Entry<String, AlbumSimple> album : albumsSelected.entrySet()) {
                    databaseReference.child("albums").child(album.getKey()).child("name").setValue(album.getValue().name);
                    databaseReference.child("albums").child(album.getKey()).child("uri").setValue(album.getValue().uri);
                    databaseReference.child("albums").child(album.getKey()).child("cover").setValue(album.getValue().images.get(0).url);
                }
                for (HashMap.Entry<String, String> genre : genreSelected.entrySet()) {
                    databaseReference.child("genres").child(genre.getKey()).child("cover").setValue(genre.getValue());
                }
                refUser.child(sharedPreferenceHelper.getCurrentUserId()).child("answers").child(questionID).setValue(databaseReference.getKey());
            }
        });
    }

    public void upvote(DatabaseReference questionId,String answerId) {
        final DatabaseReference mUpvoterReference = questionId.child(answerId).child("upvotes").child("users").child(sharedPreferenceHelper.getCurrentUserId());
        final DatabaseReference mUpvotesReference = questionId.child(answerId).child("upvotes").child("number");
        mUpvotesReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot votes) {
                mUpvoterReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot voter) {
                        if (voter.exists()) {
                            mUpvotesReference.setValue(votes.getValue(Integer.class)-1);
                            mUpvoterReference.removeValue();
                         }else{
                            mUpvoterReference.setValue("voted");
                            if (votes.exists()) {
                                mUpvotesReference.setValue(votes.getValue(Integer.class)+1);
                            }else{
                                mUpvotesReference.setValue(1);
                            }
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
                                }
                            } else {
                                //mRequestReference.setValue(questionId);
                                mRequestReference.child(questionId).child("requesters").child(sharedPreferenceHelper.getCurrentUserId()).setValue("requested");
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG,"addListenerForSingleValueEvent:failure",databaseError.toException());
            }
        });

    }

    public void follow(final String questionId){
        final DatabaseReference mFollowersReference = refQuestionFeed.child(questionId).child("following").child("users").child(sharedPreferenceHelper.getCurrentUserId());
        final DatabaseReference mFollowingReference = refQuestionFeed.child(questionId).child("following").child("number");
        final DatabaseReference mUserFollow = refUser.child(sharedPreferenceHelper.getCurrentUserId()).child("following");
        mFollowingReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot following) {
                mFollowersReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot follower) {
                        if (follower.exists()) {
                            refQuestionFeed.child(questionId).child("following").child("number").setValue(following.getValue(Integer.class)-1);
                            refQuestionFeed.child(questionId).child("following").child("users").child(sharedPreferenceHelper.getCurrentUserId()).removeValue();
                            mUserFollow.child(questionId).removeValue();

                        }else{
                            refQuestionFeed.child(questionId).child("following").child("users").child(sharedPreferenceHelper.getCurrentUserId()).setValue("is following");
                            mUserFollow.child(questionId).setValue("true");

                            if (following.exists()) {
                                refQuestionFeed.child(questionId).child("following").child("number").setValue(following.getValue(Integer.class)+1);
                            }else{
                                refQuestionFeed.child(questionId).child("following").child("number").setValue(1);
                            }
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

    public void getUpvote(final String questionId, final String answerId, final AnswersAdapter.ViewHolder holder) {
        System.out.println(questionId + "----" + answerId);
        final DatabaseReference mUpvoterReference = refQuestionFeed.child(questionId).child("answers").child(answerId).child("upvotes").child("users").child(sharedPreferenceHelper.getCurrentUserId());
        final DatabaseReference mUpvotesReference = refQuestionFeed.child(questionId).child("answers").child(answerId).child("upvotes").child("number");
        mUpvotesReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot votes) {
                mUpvoterReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot voter) {
                        if (voter.exists()) {
                            //Log.i("votes",""+(int)votes.getValue());
                            holder.upvote.setImageResource(R.drawable.ic_favorite);

                        }else{
                            holder.upvote.setImageResource(R.drawable.ic_favorite_border);
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

    public String NumberToGenre(String number){
        String result="";
        switch(number){
            case "0" :
                result="Blues";
                break;
            case "1" :
                result="Chill";
                break;
            case "2" :
                result="Christmas";
                break;
            case "3" :
                result="Classical";
                break;
            case "4" :
                result="Country";
                break;
            case "5" :
                result="Electro";
                break;
            case "6" :
                result="Focus";
                break;
            case "7" :
                result="Folk";
                break;
            case "8" :
                result="Funk";
                break;
            case "9" :
                result="Gaming";
                break;
            case "10" :
                result="Hip-Hop";
                break;
            case "11" :
                result="Indie";
                break;
            case "12" :
                result="Jazz";
                break;
            case "13" :
                result="Latin";
                break;
            case "14" :
                result="Metal";
                break;
            case "15" :
                result="Mood";
                break;
            case "16" :
                result="Party";
                break;
            case "17" :
                result="Pop";
                break;
            case "18" :
                result="Punk";
                break;
            case "19" :
                result="Reggae";
                break;
            case "20" :
                result="RnB";
                break;
            case "21" :
                result="Rock";
                break;
            case "22" :
                result="Sleep";
                break;
            case "23" :
                result="Soul";
                break;
            case "24" :
                result="Travel";
                break;
            case "25" :
                result="Workout";
                break;
        }
        return result;
    }
}
