package com.cyber.fastnotes;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.cyber.adapter.RowItemAdapter;
import com.cyber.fastnotes.service.AppDataBase;

import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    static final AppDataBase DB = App.getInstance().getDataBase();

    static final int REQUEST_ARTICLE = 101;

    RecyclerView rv;
    RowItemAdapter rowsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(v -> doArticleEdit(App.NO_VALUE) );

        rowsAdapter = new RowItemAdapter();

        rv = findViewById(R.id.recyclerView);
        rv.setLayoutManager(new LinearLayoutManager( this ));
        rv.setAdapter(rowsAdapter);

        rowsAdapter.setOnItemPositionClickListener((v,i) -> doArticleEdit( rowsAdapter.get(i).getId() ));

        doUpdateAllRows();
    }

    protected void doUpdateAllRows(){
        DB.articleDao().getAll()
            .subscribeOn(Schedulers.io())
            .subscribe( rowsList -> rowsAdapter.setRowItemList(rowsList));
    }

    protected void doUpdateRowById(long articleId){
        int pos = rowsAdapter.getIndexById(articleId);
        if (pos>=0){
            rowsAdapter.notifyItemChanged(pos);
        }else{
            doUpdateAllRows();
        }
    }

    protected void doArticleEdit(long articleId){
        Log.v(App.TAG, "doArticleEdit(), id: " + articleId);

        Intent intent = new Intent(this, MakeNoteActivity.class);
        intent.putExtra(App.EXTRA_ID_NAME, articleId);

        startActivityForResult(intent, REQUEST_ARTICLE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode==RESULT_CANCELED) return;

        if (requestCode==REQUEST_ARTICLE) {
            Log.v(App.TAG, "REQUEST_ARTICLE success, article id: " + data.getLongExtra(App.EXTRA_ID_NAME, App.NO_VALUE));
            long articleId = data.getLongExtra(App.EXTRA_ID_NAME, App.NO_VALUE);
            doUpdateRowById(articleId);
        }

    }
}
