package com.aloy.aloy.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aloy.aloy.Views.IndexedFeed;
import com.aloy.aloy.Models.Question;
import com.aloy.aloy.Models.QuestionHolder;
import com.aloy.aloy.R;
import com.aloy.aloy.Util.AchievementsHandler;
import com.aloy.aloy.Util.DataHandler;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;


/**
 * Created by tldonne on 09/11/2017.
 */

public class IndexedFeedAdapter {

    private static DatabaseReference keyRef;
    private Query q;
    private static DatabaseReference dataRef;
    private static FirebaseRecyclerOptions<Question> options;
    private Context context;
    private IndexedFeed indexedFeed;
    private DataHandler dataHandler;

    public IndexedFeedAdapter(String userId, String type, Context context, final IndexedFeed indexedFeed) {
        dataHandler = new DataHandler(context);
        dataRef = FirebaseDatabase.getInstance().getReference("questions");
        if(!(type.equals("requests")||type.equals("following")||type.equals("questions")||type.equals("answers"))){
            keyRef = FirebaseDatabase.getInstance().getReference().child("categories").child(type).child("questions");
        }else {
            keyRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId).child(type);
        }
        options = new FirebaseRecyclerOptions.Builder<Question>()
                .setIndexedQuery(keyRef, dataRef, Question.class)
                .setLifecycleOwner(indexedFeed.getActivity())
                .build();
        this.context = context;
        this.indexedFeed = indexedFeed;
    }


    /**
     *This method is used to create a FirebaseRecyclerAdapter, loading specific questions from the
     *feed based on a indexed list specified by variable keyRef.
     *Binds every data stored in a Question into an QuestionHolder.
     * @return the FirebaseRecyclerAdapter
     */

    public RecyclerView.Adapter getAdapter(){
        return new FirebaseRecyclerAdapter<Question,QuestionHolder>(options) {

            @Override
            public int getItemViewType(int position) {
                return position;
            }

            @Override
            protected void onBindViewHolder(final QuestionHolder holder, int position, final Question model) {
                //final Question question = getItem(position);
                holder.getQuestionBody().setText(model.getBody());

                if((model.getName()).equals("")){
                    holder.getQuestionUsername().setText(model.getUsername());
                }else{
                    System.out.println(model.getName());
                    holder.getQuestionUsername().setText(model.getName());
                }

                //Picasso.with(context).load(user.getPhotoUrl().toString()).into(holder.profilePic);
                //Picasso.with(context).load(model.getPic()).into(holder.profilePic);
                dataHandler.getItems(model.getId(), holder,context);
                dataHandler.getStyles(model.getId(),holder,context);
                dataHandler.getUrl(model.getUsername(),holder.getProfilePic(),context);
                AchievementsHandler achievementsHandler = new AchievementsHandler(context,model.getUsername());
                achievementsHandler.setProfilePicBorder(holder.getProfilePic());
                dataHandler.getFollow(model.getId(),holder.getFollowButton());


                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        indexedFeed.onQuestionCLick(model,holder.itemView);
                    }
                });

                holder.getAnswerButton().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        indexedFeed.onAnswerClick(model.getId());
                    }
                });

                holder.getFollowButton().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dataHandler.follow(model.getId());
                    }
                });


            }


            @Override
            public QuestionHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View questionView = LayoutInflater.from(parent.getContext()).inflate(R.layout.question, parent, false);
                //questionView.findViewById(R.id.followButton).setVisibility(View.INVISIBLE);
                return new QuestionHolder(questionView);
            }
        };

    }

}
