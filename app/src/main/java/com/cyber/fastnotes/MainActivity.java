package com.cyber.fastnotes;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import com.cyber.adapter.RowItemAdapter;
import com.cyber.fastnotes.service.AppDataBase;
import com.cyber.model.RowItem;

import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    static final AppDataBase DB = App.getInstance().getDataBase();
    static final String TAG = "CYBER";

    RecyclerView rv;
    RowItemAdapter rowsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            Intent intentMakeNote = new Intent(this, MakeNoteActivity.class);
            startActivity(intentMakeNote);
        });

        rowsAdapter = new RowItemAdapter();

        rv = findViewById(R.id.recyclerView);
        rv.setLayoutManager(new LinearLayoutManager( this ));
        rv.setAdapter(rowsAdapter);
        
        doUpdateRows();
    }

    protected void doUpdateRows(){
        rowsAdapter.clear();

        DB.articleDao().getAll()
            .flatMapIterable( items -> items )
            .doOnNext(a -> Log.v(TAG, "get article: " + a.id + ", " + a.title + ", date: " + a.date ))
            .subscribeOn(Schedulers.io())
            .doOnComplete( () -> rowsAdapter.notifyDataSetChanged() )
            .subscribe( rowItem -> rowsAdapter.add(rowItem));

    }

}
