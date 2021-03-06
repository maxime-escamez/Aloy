package com.aloy.aloy.Views;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aloy.aloy.Adapters.FeedAdapter;
import com.aloy.aloy.Contracts.FeedContract;
import com.aloy.aloy.Models.Question;
import com.aloy.aloy.R;
import com.github.clans.fab.FloatingActionButton;


/**
 * A simple {@link Fragment} subclass.
 */
public class Feed extends Fragment implements FeedContract.View {

    public static final String TAG = Feed.class.getSimpleName();
    private LinearLayoutManager layoutManager;
    private FloatingActionButton addQuestionFab;
    public static final String EXTRA_QUESTION = "question";
    public static final String EXTRA_QUESTION_TRANSITION_NAME = "question_transition_name";


    public Feed() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View feedView = inflater.inflate(R.layout.fragment_feed, container, false);
        setupRecyclerView(feedView);
        addQuestionFab = (FloatingActionButton) getActivity().findViewById(R.id.main_fab);
        addQuestionFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddQuestion();
            }
        });
        return feedView;
    }



    @Override
    public void setupRecyclerView(View feedView) {
        RecyclerView recyclerView = (RecyclerView) feedView.findViewById(R.id.feedRecyclerView);
        //feedAdapter = new FeedAdapter(myRef, adapterQuestions, adapterKeys,getContext(),this);
        FeedAdapter questionAdapter = new FeedAdapter(getContext(),this);
        layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(questionAdapter.getAdapter());

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy){
                if (dy > 0)
                    addQuestionFab.hide(true);
                else if (dy < 0)
                    addQuestionFab.show(true);
            }
        });
        //recyclerView.scrollToPosition(7);

    }


    @Override
    public void showAddQuestion() {
        FragmentManager fragmentManager = getFragmentManager();
        Ask askDialog = new Ask();
        Bundle args = new Bundle();
        args.putString("questionId", "null");
        askDialog.setArguments(args);
        askDialog.show(fragmentManager,"ask");
    }

    @Override
    public void showAnswerQuestion(String questionId) {
        FragmentManager fragmentManager = getFragmentManager();
        Ask askDialog = new Ask();
        Bundle args = new Bundle();
        args.putString("questionId", questionId);
        askDialog.setArguments(args);
        askDialog.show(fragmentManager,"answer");
    }

    @Override
    public void onQuestionCLick(Question question, View itemView) {
        Intent intent = new Intent(this.getActivity(), Details.class);
        itemView.setTransitionName(EXTRA_QUESTION_TRANSITION_NAME);
        intent.putExtra(EXTRA_QUESTION, question);
        intent.putExtra(EXTRA_QUESTION_TRANSITION_NAME, ViewCompat.getTransitionName(itemView));

        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this.getActivity(), itemView,
                ViewCompat.getTransitionName(itemView));
        startActivity(intent, options.toBundle());
    }





}
