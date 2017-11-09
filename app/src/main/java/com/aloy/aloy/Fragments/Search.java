package com.aloy.aloy.Fragments;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;

import com.aloy.aloy.Adapters.MyRecyclerAdapter;
import com.aloy.aloy.Adapters.SearchAdapter;
import com.aloy.aloy.Contracts.SearchContract;
import com.aloy.aloy.MainActivity;
import com.aloy.aloy.Models.SearchResult;
import com.aloy.aloy.Presenters.SearchPresenter;
import com.aloy.aloy.R;
import com.aloy.aloy.Util.DataHandler;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import kaaes.spotify.webapi.android.models.AlbumSimple;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Track;

/**
 * A simple {@link Fragment} subclass.
 */
public class Search extends DialogFragment implements SearchContract.View {

    private SearchPresenter searchPresenter;
    private String type;
    private EditText searchField;
    private String searchQuery;
    private Button validateSearch;
    private SearchAdapter searchAdapter;
    private TextView itemsSelected;
    private Ask callingFragment;
    private boolean searchTrack = false;



    public Search() {
        // Required empty public constructor
    }

    public void hideKeyboardFrom(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }



    @Override
    public void onStart()
    {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null)
        {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View searchView = inflater.inflate(R.layout.fragment_search, container, false);
        searchPresenter = new SearchPresenter(this, MainActivity.getDataHandler(), MainActivity.getSpotifyHandler());
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        validateSearch = (Button) searchView.findViewById(R.id.validateSearch);
        searchField = (EditText) searchView.findViewById(R.id.searchSpotify);
        Bundle args = getArguments();
        type = args.getString("type");
        if(type.equals("genre")){
            searchField.setVisibility(View.GONE);
        }
        itemsSelected = (TextView) searchView.findViewById(R.id.elementsSelected);
        callingFragment = (Ask) getTargetFragment();
        if(type.equals("genre")){
            setupRecyclerView(searchView, searchQuery, type);
        }
        searchField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    searchQuery = searchField.getText().toString();
                    setupRecyclerView(searchView, searchQuery, type);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        hideKeyboardFrom(getContext(),searchView);
                    }
                    return true;
                }
                return false;
            }
        });


        validateSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (searchTrack==true)
                    callingFragment.update();
                hideSearch();
            }
        });


        return searchView;
    }

    @Override
    public void hideSearch() {
        this.dismiss();

    }

    @Override
    public Ask getAsk() {
        return callingFragment;
    }


    @Override
    public void setupRecyclerView(View searchView, String searchQuery, String type) {
        searchTrack = true;
        RecyclerView recyclerView = (RecyclerView) searchView.findViewById(R.id.searchGrid);
        if(getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        }
        else{
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 5));
        }
        searchPresenter.setupSearchRecycler(recyclerView, searchView, getContext(), searchQuery, type);
        /*searchPresenter.updateCount(searchQuery);
        searchAdapter = new SearchAdapter(searchView,getContext(), searchPresenter, searchQuery, type, searchPresenter.getitemCount());
        recyclerView.setAdapter(searchAdapter);*/
    }

    @Override
    public void updateCount(String type) {
        switch (type) {
            case "track" :
                itemsSelected.setText(callingFragment.getAskPresenter().getTracks().size() + " Tracks Selected");
                break;
            case "artist" :
                itemsSelected.setText(callingFragment.getAskPresenter().getArtists().size() + " Artists Selected");
                break;
            case "album" :
                itemsSelected.setText(callingFragment.getAskPresenter().getAlbums().size() + " Albums Selected");
                break;
            case "genre" :
                itemsSelected.setText(callingFragment.getAskPresenter().getGenres().size() + " Genres Selected");
        }
    }



}
