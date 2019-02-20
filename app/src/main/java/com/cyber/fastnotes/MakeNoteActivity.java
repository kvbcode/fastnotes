package com.cyber.fastnotes;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ScrollView;
import android.widget.Toast;

import com.cyber.fastnotes.service.DataStorage;
import com.cyber.fastnotes.view.ArticleView;
import com.cyber.model.Article;
import com.cyber.model.ArticleItem;

import java.io.IOException;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;


public class MakeNoteActivity extends AppCompatActivity {
    private static int PHOTO_REQUEST = 1101;
    private static int GALLERY_IMAGE_REQUEST = 1102;
    private static int AUDIO_REQUEST = 1103;

    private static String TAG = "CYBER";

    private Article article;
    private ArticleView articleView;
    private ScrollView scrollView;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menuAddText:
                    article.add("TEXT ITEM");
                    articleView.notifyDatasetChanged();
                    scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
                    return true;
                case R.id.menuAddPhoto:
                    takePhoto();
                    return true;
                case R.id.menuAddGallery:
                    getGalleryImage();
                    return true;
                case R.id.menuAddAudio:
                    article.add("AUDIO ITEM");
                    articleView.notifyDatasetChanged();
                    scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_note);

        scrollView = findViewById(R.id.scrollView);

        article = new Article();
        articleView = findViewById(R.id.articleView);
        articleView.setArticle(article);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setHomeButtonEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == android.R.id.home){
            doSaveArticle();
            Intent data = new Intent();
            setResult(RESULT_OK, data);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void takePhoto(){
        Intent intentGetPhoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intentGetPhoto, PHOTO_REQUEST);
    }

    public void getGalleryImage(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_IMAGE_REQUEST);
    }

    public Bitmap resizeBitmap(Bitmap bitmap){
        Point screenSize = new Point();
        getWindowManager().getDefaultDisplay().getSize(screenSize);

        float ar = (float)bitmap.getWidth() / bitmap.getHeight();

        int width = Math.min( screenSize.x, screenSize.y );
        int height = Math.round(width / ar);

        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_CANCELED) return;

        if (requestCode == PHOTO_REQUEST){
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            Bitmap thumb = resizeBitmap(photo);
            Uri contenUri = DataStorage.getInstance().saveBitmap(this, photo);
            photo.recycle();

            Log.v(TAG, "photo saved: " + contenUri);

            ArticleItem.Image item = article.add( thumb );
            item.setContentUri(contenUri);
        }

        if (requestCode == GALLERY_IMAGE_REQUEST){
            Uri contentURI = data.getData();
            Log.v(TAG, "success GALLERY_IMAGE_REQUEST, image Uri: " + contentURI);

            try {
                Bitmap image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentURI);
                Bitmap thumb = resizeBitmap(image);
                image.recycle();

                ArticleItem.Image item = article.add(thumb);
                item.setContentUri(contentURI);
            }catch(IOException e){
                Toast.makeText(this, "Failed file reading from gallery", Toast.LENGTH_LONG);
            }

        }

        articleView.notifyDatasetChanged();
        scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
    }

    public void doSaveArticle(){
        Single.fromCallable(() -> App.getInstance().getDataBase().articleDao().saveFully(article))
            .subscribeOn(Schedulers.io())
            .subscribe(
                id -> Log.v("TAG", "new Article id: " + id),
                e ->  Log.e("TAG", "saveWork() error: " + e.getMessage()) );
    }

}
