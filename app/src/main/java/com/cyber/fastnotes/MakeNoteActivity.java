package com.cyber.fastnotes;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ScrollView;

import com.cyber.component.AudioPlayerComponent;
import com.cyber.fastnotes.model.ParcelableArticleItemWrapper;
import com.cyber.fastnotes.service.AppDataBase;
import com.cyber.fastnotes.service.IOHelper;
import com.cyber.fastnotes.view.ArticleView;
import com.cyber.fastnotes.model.Article;
import com.cyber.fastnotes.model.ArticleItem;
import com.cyber.fastnotes.model.ParcelableArticleWrapper;
import com.cyber.rx.ui.ObservableTextWatcher;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;


public class MakeNoteActivity extends AppCompatActivity {
    private static int PHOTO_REQUEST = 1101;
    private static int GALLERY_IMAGE_REQUEST = 1102;
    private static int AUDIO_REQUEST = 1103;
    private static int BARCODE_REQUEST = 1104;

    private Article article;
    private AppDataBase db;
    private Uri lastOutputFileUri;

    private Menu toolbarMenu;
    private EditText editTitle;
    private ArticleView articleView;
    private ScrollView scrollView;


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
                switch (item.getItemId()) {
                    case R.id.menuAddText:
                        addArticleItem( ArticleItem.fromText("") );
                        return true;
                    case R.id.menuAddPhoto:
                        lastOutputFileUri = actionTakePhoto();
                        return true;
                    case R.id.menuAddGallery:
                        actionGetGalleryImage();
                        return true;
                    case R.id.menuAddAudio:
                        actionRecordAudio(null);
                        return true;
                    case R.id.menuAddBarcode:
                        actionTakeBarcodes();
                        return true;
                }
                return false;
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_note);

        db = App.getInstance().getDataBase();

        ObservableTextWatcher watcher = new ObservableTextWatcher();
        watcher.getOnChangedObservable()
                .debounce( App.DEBOUNCE_VALUE, TimeUnit.MILLISECONDS)
                .subscribe( str -> article.setTitle( str ) );

        editTitle = findViewById(R.id.editTitle);
        editTitle.addTextChangedListener(watcher);

        scrollView = findViewById(R.id.scrollView);
        articleView = findViewById(R.id.articleView);

        BottomNavigationView navigation = findViewById(R.id.navigation);
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
        boolean isNew = in.getBooleanExtra(App.PARAM_IS_NEW, true);

        if (isNew) {
            Log.v(App.TAG, "create new Article");
            setArticle( new Article() );
            setTitle(R.string.title_new_record);
        }else {
            final long articleId = in.getLongExtra(App.PARAM_ID, Long.MIN_VALUE);
            db.articleDao().loadFully(articleId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ar -> setArticle(ar));
            setTitle(R.string.title_edit_record);

            // suppress soft keyboard to open at start
            getWindow().setSoftInputMode( WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN );
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

        outState.putString( App.PARAM_LAST_OUTPUT_URI,
            (lastOutputFileUri!=null)? lastOutputFileUri.toString(): "");

        ParcelableArticleWrapper parc = new ParcelableArticleWrapper( article );
        outState.putParcelable( Article.class.getSimpleName(), parc );

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(App.TAG, "onRestoreInstanceState()");

        lastOutputFileUri = Uri.parse( savedInstanceState.getString( App.PARAM_LAST_OUTPUT_URI ) );

        ParcelableArticleWrapper parc = savedInstanceState.getParcelable( Article.class.getSimpleName() );
        setArticle( parc.getArticle() );
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch(id) {
            case android.R.id.home:
                setResult(RESULT_CANCELED);
                finish();
                break;
            case R.id.menuSave:
                db.articleDao().saveFully( article )
                    .observeOn(AndroidSchedulers.mainThread())
                    .doFinally(() -> finish())
                    .subscribe( articleId -> {
                        Intent data = new Intent();
                        data.putExtra(App.PARAM_ID, articleId);
                        setResult(RESULT_OK, data);
                    });
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public Uri actionTakePhoto(){
        Uri outputUri = Uri.fromFile( IOHelper.createExternalFilePath( this, Environment.DIRECTORY_PICTURES, "img_", "jpg" ) );

        Log.d(App.TAG, "actionTakePhoto() into: " + outputUri);

        Intent intent = new Intent( android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
        startActivityForResult(intent, PHOTO_REQUEST);

        return outputUri;
    }

    public void actionGetGalleryImage(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_IMAGE_REQUEST);
    }

    public void actionRecordAudio(ArticleItem articleItem){
        Log.d(App.TAG, "actionRecordAudio() with " + articleItem);

        //external recording with default params
        //Intent intent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
        //startActivityForResult(intent, AUDIO_REQUEST);

        // internal recording with HQ
        Intent intent = new Intent( this, AudioRecorderActivity.class );
        intent.putExtra( App.PARAM_IS_NEW, articleItem==null );
        if (articleItem!=null) intent.putExtra( ArticleItem.class.getSimpleName(), new ParcelableArticleItemWrapper(articleItem) );

        startActivityForResult(intent, AUDIO_REQUEST);
    }

    public void actionTakeBarcodes(){
        Intent intent = new Intent(this, BarcodeScannerActivity.class);
        startActivityForResult(intent, BARCODE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_CANCELED) return;

        if (requestCode == PHOTO_REQUEST){
            Log.d(App.TAG, "success PHOTO_REQUEST, image Uri: " + lastOutputFileUri);
            addArticleItem( ArticleItem.fromBitmap(lastOutputFileUri) );
        }

        if (requestCode == GALLERY_IMAGE_REQUEST){
            Uri contentURI = data.getData();
            Log.d(App.TAG, "success GALLERY_IMAGE_REQUEST, image Uri: " + contentURI);
            addArticleItem( ArticleItem.fromBitmap(contentURI) );
        }

        if (requestCode == AUDIO_REQUEST){
            ParcelableArticleItemWrapper itemWrapper = data.getParcelableExtra( ArticleItem.class.getSimpleName() );
            ArticleItem item = itemWrapper.getArticleItem();
            Log.d(App.TAG, "success AUDIO_REQUEST: " + item);

            if (item.isNew()){
                addArticleItem( item );
            }else{
                updateArticleItem( item );
            }
        }

        if (requestCode == BARCODE_REQUEST){
            ArrayList<String> barcodeList = data.getStringArrayListExtra( BarcodeScannerActivity.PARAM_BARCODE_LIST );
            Log.d(App.TAG, "success BARCODE_REQUEST items: " + barcodeList.size());
            for(String barcode:barcodeList){
                addArticleItem( ArticleItem.fromBarcode( barcode ) );
            }
        }
    }

    public void addArticleItem(ArticleItem item){
        article.add(item);
        articleView.insertLastItemView();
        scrollView.postDelayed(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN), 1);
    }

    public void updateArticleItem(ArticleItem newData){
        ArticleItem item = article.getById( newData.getId() );
        if(item!=null){
            Log.v(App.TAG, "updateArticleItem(): " + newData);
            item.setText( newData.getText() );
            item.setContentUri( newData.getContentUri() );
        }
        articleView.resetView();
    }

}
