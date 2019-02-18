package com.cyber.fastnotes;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.cyber.adapter.RowItemAdapter;
import com.cyber.model.RowItem;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    RecyclerView rv;

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

        RowItemAdapter rowsAdapter = new RowItemAdapter(getRows());

        rv = findViewById(R.id.recyclerView);
        rv.setLayoutManager(new LinearLayoutManager( this ));
        rv.setAdapter(rowsAdapter);

    }

    protected List<RowItem> getRows(){
        List<RowItem> rows = new ArrayList<>();
        for(int i=0;i<30; i++) {
            rows.add(new RowItem("заметка " + i + " от " + System.currentTimeMillis()));
        }
        return rows;
    }

}
