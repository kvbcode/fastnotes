package com.cyber.fastnotes;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
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
import com.cyber.fastnotes.model.ArticleItem;
import com.cyber.fastnotes.model.ParcelableArticleWrapper;
import com.cyber.fastnotes.service.AppDataBase;
import com.cyber.fastnotes.service.ArticleHtmlExport;
import com.cyber.fastnotes.service.IOHelper;

import java.io.File;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    static final AppDataBase DB = App.getInstance().getDataBase();

    private static final int ARTICLE_REQUEST = 101;
    private static final int EXPORT_ARTICLE_REQUEST = 102;

    private static final String[] STORAGE_PERMISSIONS = {
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };

    RecyclerView rv;
    RowItemAdapter rowsAdapter;
    Article activeArticle;

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
        if (isNew) startActivityForResult(intent, ARTICLE_REQUEST);

        DB.articleDao().loadFully( articleId )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe( ar -> {
                    ParcelableArticleWrapper parc = new ParcelableArticleWrapper(ar);
                    intent.putExtra( App.PARAM_ARTICLE, parc );
                    startActivityForResult(intent, ARTICLE_REQUEST);
                });
    }

    protected void deleteArticleQuery(Article article){
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle( getString(R.string.query_delete_article) )
                .setMessage( article.getTitle() )
                .setIcon(android.R.drawable.ic_delete)
                .setCancelable(true)
                .setPositiveButton(android.R.string.ok, (d, i) -> deleteArticle( article.getId() ) )
                .setNegativeButton(android.R.string.cancel, (d, i) -> d.cancel())
                .show();
    }

    protected void deleteArticle(Long articleId){
        Log.v(App.TAG, "deleteArticle() id: " + articleId);
        DB.articleDao().loadFully( articleId )
            .doAfterSuccess( ar -> DB.articleDao().delete( ar ) )
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe( ar -> {
                int pos = rowsAdapter.getIndexById( ar.getId() );
                rowsAdapter.remove( pos );
                rowsAdapter.notifyItemRemoved( pos );
                rowsAdapter.notifyDataSetChanged();
                for(ArticleItem item:ar.getItems()) {
                    deleteItemAttachment(item);
                }
            });

    }

    protected void deleteItemAttachment(ArticleItem item){
        if (ArticleItem.TYPE_IMAGE==item.getType() ||
            ArticleItem.TYPE_AUDIO==item.getType()) {

            if (IOHelper.isApplicationStorageFilePath( this, item.getContentUri().getPath() )){
                File attFile = new File(item.getContentUri().getPath());
                Log.v(App.TAG, "delete attachment: " + attFile);
                attFile.delete();
            }

            if (ArticleItem.TYPE_IMAGE==item.getType()){
                File thumbFile = IOHelper.getThumbnailFile(this, item.getContentUri());
                Log.v(App.TAG, "delete thumbnail: " + thumbFile);
                thumbFile.delete();
            }
        }
    }

    protected void showArticleMenu(Article article){
        activeArticle = article;

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
        if (!App.isPermissionsGranted(this, STORAGE_PERMISSIONS)){
            App.requestRuntimePermissions(this, STORAGE_PERMISSIONS, EXPORT_ARTICLE_REQUEST);
            return;
        }

        Log.v(App.TAG, "try export article");
        ArticleHtmlExport exporter = new ArticleHtmlExport(this);
        String dirDocuments = Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT ? "Document" : Environment.DIRECTORY_DOCUMENTS;
        File outDir = Environment.getExternalStoragePublicDirectory( dirDocuments );

        DB.articleDao().loadFully( article.getId() )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe( ar -> {
                if (exporter.export( ar, outDir ))
                    Toast.makeText(this, "Экспорт завершен\nсохранено в " + outDir, Toast.LENGTH_LONG).show();
            } );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (!App.isRequestedPermissionsGranted(permissions, grantResults)){
            Toast.makeText(this, R.string.status_denied, Toast.LENGTH_SHORT).show();
        }else{
            if (requestCode==EXPORT_ARTICLE_REQUEST) exportArticle(activeArticle);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode==RESULT_CANCELED) return;

        if (requestCode== ARTICLE_REQUEST) {
            ParcelableArticleWrapper parc = data.getParcelableExtra( App.PARAM_ARTICLE );

            final Article resultArticle = parc.getArticle();

            DB.articleDao().saveFully(resultArticle)
                    .doAfterSuccess( id -> {
                        for(ArticleItem item:resultArticle.getItems()){
                            if (item.isDeleted()) deleteItemAttachment(item);
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(articleId -> {
                        Log.v(App.TAG, "ARTICLE_REQUEST success, article id: " + articleId);
                        updateRowById(articleId);
                    });
        }

    }
}
