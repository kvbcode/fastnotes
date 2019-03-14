package com.cyber.fastnotes;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.cyber.adapter.RowItemAdapter;
import com.cyber.fastnotes.model.Article;
import com.cyber.fastnotes.service.AppDataBase;
import com.cyber.fastnotes.service.ArticleHtmlExport;

import java.io.File;

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
        fab.setOnClickListener(v -> actionEditArticle(0, true) );

        rowsAdapter = new RowItemAdapter();

        rv = findViewById(R.id.recyclerView);
        rv.setLayoutManager(new LinearLayoutManager( this ));
        rv.setAdapter(rowsAdapter);

        rowsAdapter.setOnItemPositionClickListener( (v,i) -> actionEditArticle( rowsAdapter.get(i).getId(), false ));
        rowsAdapter.setOnItemPositionLongClickListener( (v,i) -> showArticleMenu( (Article)rowsAdapter.get(i) ));

        updateAllRows();
    }

    protected void updateAllRows(){
        Log.v(App.TAG, "updateAllRows()");
        DB.articleDao().getAll()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe( rowsList -> {
                rowsAdapter.setRowItemList(rowsList);
                rowsAdapter.notifyDataSetChanged();
            });
    }

    protected void updateRowById(long articleId){
        int pos = rowsAdapter.getIndexById(articleId);
        Log.v(App.TAG, "updateRowById: " + articleId + ", pos: " + pos);
        if (pos>=0){
            rowsAdapter.notifyItemChanged(pos);
        }else{
            updateAllRows();
        }
    }

    protected void actionEditArticle(long articleId, boolean isNew){
        Intent intent = new Intent(this, MakeNoteActivity.class);

        intent.putExtra(App.PARAM_IS_NEW, isNew);

        if (!isNew) intent.putExtra(App.PARAM_ID, articleId);

        startActivityForResult(intent, REQUEST_ARTICLE);
    }

    protected void deleteArticleQuery(Article article){
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle( getString(R.string.query_delete_article) )
                .setMessage( article.getTitle() )
                .setIcon(android.R.drawable.ic_delete)
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

    protected void showArticleMenu(Article article){
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle( R.string.title_select_action )
            .setMessage( article.getTitle() )
            .setPositiveButton( R.string.menu_open, (dialog1, which) -> actionEditArticle( article.getId(), false ) )
            .setPositiveButtonIcon(getResources().getDrawable(android.R.drawable.ic_menu_edit))

            .setNeutralButton( R.string.menu_export, (dialog1, which) -> exportArticle( article ))
            .setNeutralButtonIcon(getResources().getDrawable(android.R.drawable.ic_menu_set_as))

            .setNegativeButton( R.string.menu_delete, (dialog1, which) -> deleteArticleQuery( article ) )
            .setNegativeButtonIcon(getResources().getDrawable(android.R.drawable.ic_delete))

            .setCancelable(true)
            .show();
    }

    protected void exportArticle(Article article){
        boolean result = false;
        Log.v(App.TAG, "try export article");
        ArticleHtmlExport exporter = new ArticleHtmlExport(this);
        String dirDocuments = Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT ? "Document" : Environment.DIRECTORY_DOCUMENTS;
        File outDir = Environment.getExternalStoragePublicDirectory( dirDocuments );
        result = exporter.export( article, outDir );
        if (result){
            Toast.makeText(this, "Экспорт завершен\nсохранено в " + outDir, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode==RESULT_CANCELED) return;

        if (requestCode==REQUEST_ARTICLE) {
            long articleId = data.getLongExtra(App.PARAM_ID, Long.MIN_VALUE);
            Log.v(App.TAG, "REQUEST_ARTICLE success, article id: " + articleId);
            updateRowById(articleId);
        }

    }
}
