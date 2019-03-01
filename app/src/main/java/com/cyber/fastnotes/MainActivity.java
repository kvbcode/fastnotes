package com.cyber.fastnotes;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.cyber.adapter.RowItemAdapter;
import com.cyber.fastnotes.model.Article;
import com.cyber.fastnotes.service.AppDataBase;

import io.reactivex.Completable;
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

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> doArticleEdit(0, true) );

        rowsAdapter = new RowItemAdapter();

        rv = findViewById(R.id.recyclerView);
        rv.setLayoutManager(new LinearLayoutManager( this ));
        rv.setAdapter(rowsAdapter);

        rowsAdapter.setOnItemPositionClickListener( (v,i) -> doArticleEdit( rowsAdapter.get(i).getId(), false ));
        rowsAdapter.setOnItemPositionLongClickListener( (v,i) -> deleteArticleQuery( (Article)rowsAdapter.get(i) ));

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

        intent.putExtra(App.PARAM_IS_NEW, isNew);

        if (!isNew) intent.putExtra(App.PARAM_ID, articleId);

        startActivityForResult(intent, REQUEST_ARTICLE);
    }

    protected void deleteArticleQuery(Article article){
        String title = "Удалить запись?\n" + article;
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setCancelable(true)
                .setPositiveButton(android.R.string.ok, (d, i) -> deleteArticle( article ) )
                .setNegativeButton(android.R.string.cancel, (d, i) -> d.cancel())
                .show();
    }

    protected void deleteArticle(Article article){
        int pos = rowsAdapter.getIndexById( article.getId() );

        Completable.fromAction( () -> DB.articleDao().delete( article ))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe( () -> {
                rowsAdapter.remove( pos );
                rowsAdapter.notifyItemRemoved( pos );
            });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode==RESULT_CANCELED) return;

        if (requestCode==REQUEST_ARTICLE) {
            long articleId = data.getLongExtra(App.PARAM_ID, Long.MIN_VALUE);
            Log.v(App.TAG, "REQUEST_ARTICLE success, article id: " + articleId);
            doUpdateRowById(articleId);
        }

    }
}
