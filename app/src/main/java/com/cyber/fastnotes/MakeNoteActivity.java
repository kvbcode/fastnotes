package com.cyber.fastnotes;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.cyber.fastnotes.view.ArticleView;
import com.cyber.model.Article;

import static com.cyber.model.Article.*;

public class MakeNoteActivity extends AppCompatActivity {

    private Article article;
    private ArticleView articleView;
    private ScrollView scrollView;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menuAddText:
                    article.add(new TextItem("TEXT ITEM"));
                    articleView.notifyDatasetChanged();
                    scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
                    return true;
                case R.id.menuAddImage:
                    article.add(new TextItem("IMAGE ITEM"));
                    articleView.notifyDatasetChanged();
                    scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
                    return true;
                case R.id.menuAddAudio:
                    article.add(new TextItem("AUDIO ITEM"));
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
        article.add(new TextItem("TEXT ITEM"));
        article.add(new TextItem("IMAGE ITEM"));
        article.add(new TextItem("AUDIO ITEM"));
        article.add(new TextItem("TEXT ITEM"));
        article.add(new TextItem("IMAGE ITEM"));
        article.add(new TextItem("AUDIO ITEM"));
        article.add(new TextItem("TEXT ITEM"));
        article.add(new TextItem("IMAGE ITEM"));
        article.add(new TextItem("AUDIO ITEM"));

        articleView = findViewById(R.id.articleView);
        articleView.setArticle(article);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

}
