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
import com.cyber.model.Article;

import io.reactivex.android.schedulers.AndroidSchedulers;
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
        fab.setOnClickListener(v -> doArticleEdit(0, true) );

        rowsAdapter = new RowItemAdapter();

        rv = findViewById(R.id.recyclerView);
        rv.setLayoutManager(new LinearLayoutManager( this ));
        rv.setAdapter(rowsAdapter);

        rowsAdapter.setOnItemPositionClickListener((v,i) -> doArticleEdit( rowsAdapter.get(i).getId(), false ));

        doUpdateAllRows();
    }

    protected void doUpdateAllRows(){
        Log.v(App.TAG, "doUpdateAllRows()");
        DB.articleDao().getAll()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe( rowsList -> {
                rowsAdapter.setRowItemList(rowsList);
                rowsAdapter.notifyDataSetChanged();
            });
    }

    protected void doUpdateRowById(long articleId){
        int pos = rowsAdapter.getIndexById(articleId);
        Log.v(App.TAG, "doUpdateRowById: " + articleId + ", pos: " + pos);
        if (pos>=0){
            rowsAdapter.notifyItemChanged(pos);
        }else{
            doUpdateAllRows();
        }
    }

    protected void doArticleEdit(long articleId, boolean isNew){
        Intent intent = new Intent(this, MakeNoteActivity.class);

        intent.putExtra(App.EXTRA_IS_NEW_NAME, isNew);

        if (!isNew) intent.putExtra(App.EXTRA_ID_NAME, articleId);

        startActivityForResult(intent, REQUEST_ARTICLE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode==RESULT_CANCELED) return;

        if (requestCode==REQUEST_ARTICLE) {
            long articleId = data.getLongExtra(App.EXTRA_ID_NAME, Long.MIN_VALUE);
            Log.v(App.TAG, "REQUEST_ARTICLE success, article id: " + articleId);
            doUpdateRowById(articleId);
        }

    }
}
