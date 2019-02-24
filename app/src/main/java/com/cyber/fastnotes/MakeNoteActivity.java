package com.cyber.fastnotes;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ScrollView;

import com.cyber.fastnotes.service.AppDataBase;
import com.cyber.fastnotes.service.IOHelper;
import com.cyber.fastnotes.view.ArticleView;
import com.cyber.model.Article;
import com.cyber.model.ArticleItem;
import com.cyber.model.ParcelableArticleWrapper;
import com.cyber.rx.ui.ObservableTextWatcher;

import java.io.File;
import java.util.concurrent.TimeUnit;

import io.reactivex.Maybe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public class MakeNoteActivity extends AppCompatActivity {
    private static int PHOTO_REQUEST = 1101;
    private static int GALLERY_IMAGE_REQUEST = 1102;
    private static int AUDIO_REQUEST = 1103;

    private static int DEBOUNCE_VALUE = 300;

    private static final String PARAM_NAME_LAST_OUTPUT_URI = "last_uri";

    private Article article;
    private AppDataBase db;
    private Uri lastOutputFileUri;

    private Menu toolbarMenu;
    private EditText editTitle;
    private ArticleView articleView;
    private ScrollView scrollView;


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menuAddText:
                    article.add(ArticleItem.fromText(""));
                    articleView.insertLastItemView();
                    doScrollDown();
                    return true;
                case R.id.menuAddPhoto:
                    takePhoto();
                    return true;
                case R.id.menuAddGallery:
                    getGalleryImage();
                    return true;
                case R.id.menuAddAudio:
                    article.add(ArticleItem.fromText("[ AUDIO ITEM STUB ]"));
                    articleView.insertLastItemView();
                    doScrollDown();
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_note);

        db = App.getInstance().getDataBase();

        ObservableTextWatcher watcher = new ObservableTextWatcher();
        watcher.getOnChangedObservable()
                .debounce(DEBOUNCE_VALUE, TimeUnit.MILLISECONDS)
                .subscribe( str -> article.setTitle( str ) );

        editTitle = findViewById(R.id.editTitle);
        editTitle.addTextChangedListener(watcher);

        scrollView = findViewById(R.id.scrollView);
        articleView = findViewById(R.id.articleView);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setHomeButtonEnabled(true);

        if (savedInstanceState==null) parseInputParams(getIntent());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        this.toolbarMenu = menu;
        return true;
    }

    protected void parseInputParams(Intent in){
        boolean isNew = in.getBooleanExtra(App.EXTRA_IS_NEW_NAME, true);

        if (isNew) {
            Log.v(App.TAG, "create new Article");
            setArticle( new Article() );
            setTitle(R.string.title_new_record);
        }else {
            final long articleId = in.getLongExtra(App.EXTRA_ID_NAME, Long.MIN_VALUE);
            obsLoadArticle(articleId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ar -> setArticle(ar));
            setTitle(R.string.title_edit_record);
        }
    }

    protected void setArticle(Article article){
        Log.v(App.TAG, "setArticle: " + article);
        this.article = article;
        articleView.setArticle(article);
        editTitle.setText(article.getTitle());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(App.TAG, "onSaveInstanceState()");

        outState.putString(PARAM_NAME_LAST_OUTPUT_URI,
            (lastOutputFileUri!=null)? lastOutputFileUri.toString(): "");

        ParcelableArticleWrapper parc = new ParcelableArticleWrapper( article );
        outState.putParcelable( Article.class.getSimpleName(), parc );

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(App.TAG, "onRestoreInstanceState()");

        lastOutputFileUri = Uri.parse( savedInstanceState.getString( PARAM_NAME_LAST_OUTPUT_URI ) );

        ParcelableArticleWrapper parc = savedInstanceState.getParcelable( Article.class.getSimpleName() );
        setArticle( parc.getArticle() );
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        Log.v(App.TAG, "onOptionsItemSelected: " + id);

        switch(id) {
            case android.R.id.home:
                setResult(RESULT_CANCELED);
                finish();
                break;
            case R.id.menuSave:
                obsSaveArticle()
                    .subscribeOn(Schedulers.io())
                    .doFinally(() -> finish())
                    .subscribe( articleId -> {
                        Intent data = new Intent();
                        data.putExtra(App.EXTRA_ID_NAME, articleId);
                        setResult(RESULT_OK, data);
                    });
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void takePhoto(){
        String fname = IOHelper.createFilename("img_", "jpg");

        File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), fname);
        lastOutputFileUri = Uri.fromFile(file);

        Log.d(App.TAG, "takePhoto() into: " + lastOutputFileUri);

        Intent intent = new Intent( android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, lastOutputFileUri);
        startActivityForResult(intent, PHOTO_REQUEST);
    }

    protected void getGalleryImage(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_CANCELED) return;

        if (requestCode == PHOTO_REQUEST){
            Log.d(App.TAG, "success PHOTO_REQUEST, image Uri: " + lastOutputFileUri);

            ArticleItem item = ArticleItem.fromBitmap(lastOutputFileUri);
            article.add(item);
            articleView.insertLastItemView();
            doScrollDown();
        }

        if (requestCode == GALLERY_IMAGE_REQUEST){
            Uri contentURI = data.getData();
            Log.d(App.TAG, "success GALLERY_IMAGE_REQUEST, image Uri: " + contentURI);

            ArticleItem item = ArticleItem.fromBitmap(contentURI);
            article.add(item);
            articleView.insertLastItemView();
            doScrollDown();
        }
    }

    public void doScrollDown(){
        scrollView.postDelayed(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN), 1);
    }

    protected Maybe<Long> obsSaveArticle(){
        editTitle.requestFocus();
        article.setTitle(editTitle.getText().toString());

        return Maybe.fromCallable(() -> db.articleDao().saveFully(article))
            .subscribeOn(Schedulers.io())
            .doOnSuccess( id -> Log.v(App.TAG, "saved Article id: " + id))
            .doOnError( e ->  Log.e(App.TAG, "obsSaveArticle() error: " + e.getMessage()) )
        ;
    }

    protected Maybe<Article> obsLoadArticle(long articleId){
        return db.articleDao().loadFully(articleId)
            .doOnSuccess( ar -> Log.v(App.TAG, "loaded Article id: " + ar.getId()))
            .doOnError( e ->  Log.e(App.TAG, "obsLoadArticle() error: " + e.getMessage()) )
        ;
    }

}
