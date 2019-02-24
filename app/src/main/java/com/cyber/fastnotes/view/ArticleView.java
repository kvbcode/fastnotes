package com.cyber.fastnotes.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.cyber.fastnotes.App;
import com.cyber.fastnotes.R;
import com.cyber.fastnotes.service.IOHelper;
import com.cyber.model.Article;
import com.cyber.model.ArticleItem;
import com.cyber.rx.ui.ObservableTextWatcher;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public class ArticleView extends LinearLayout{
    private static final int[] PAD = {8, 32, 8, 32};

    private static final int DEBOUNCE_VALUE = 300;

    Article article;


    public ArticleView(Context context) {
        super(context);
    }

    public ArticleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ArticleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setArticle(Article article) {
        this.article = article;
        resetView();
    }

    public void resetView(){
        this.removeAllViews();

        if (article!=null) {
            for (int i = 0; i < article.size(); i++) {
                ArticleItem item = article.get(i);
                if (item.isDeleted()) continue;
                addView(switchItemViewSupplier( item ));
            }
        }
    }

    public void insertLastItemView(){
        int index = article.size() - 1;
        ArticleItem item = article.get(index);
        if (item.isDeleted()) return;
        View view = switchItemViewSupplier(item);
        addView( view );
    }

    public void removeDeletedItemViews(){
        int viewIndex = 0;

        for(int i=0; i < getChildCount(); i++){
            ArticleItem item = article.get(i);
            if (item.isDeleted()) {
                removeViewAt( viewIndex );
            }else{
                viewIndex++;
            }
        }
    }


    public View switchItemViewSupplier(ArticleItem item){
        switch(item.getType()){
            case ArticleItem.TYPE_TEXT:
                return getTextView(item);
            case ArticleItem.TYPE_IMAGE:
                return getImageView(item);
        }

        return getTextView(item);
    }

    
    private View getTextView(ArticleItem item) {
        EditText editText = new EditText(this.getContext());
        editText.setText(item.getText());

        editText.setHint(R.string.hint_edit_text);
        editText.setPadding(PAD[0], PAD[1], PAD[2], PAD[3]);

        ObservableTextWatcher watcher = new ObservableTextWatcher();
        watcher.getOnChangedObservable()
            .debounce(DEBOUNCE_VALUE, TimeUnit.MILLISECONDS)
            .subscribe( str -> item.setText(str) );

        editText.addTextChangedListener(watcher);

        editText.setLongClickable(true);
        editText.setOnLongClickListener( v -> deleteItem(item) );

        return editText;
    }

    private View getImageView(ArticleItem item) {
        ImageView img = new ImageView(this.getContext());

        Bitmap th = IOHelper.getThumbnailFor(this.getContext(), item.getContentUri(), 512, 512, false);

        img.setImageBitmap(th);
        img.setAdjustViewBounds(true);
        img.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        img.setPadding(PAD[0], PAD[1], PAD[2], PAD[3]);

        img.setOnClickListener( v -> doShowImage(this.getContext(), item.getContentUri() ) );

        img.setLongClickable(true);
        img.setOnLongClickListener( v -> deleteItem(item) );

        return img;
    }

    public void doShowImage(Context context, Uri contentUri){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(contentUri, "image/*");
        context.startActivity(intent);
    }

    public boolean deleteItem(ArticleItem item){
        String title = "Удалить элемент?\n" + item;
        AlertDialog dialog = new AlertDialog.Builder(getContext())
            .setTitle(title)
            .setCancelable(true)
            .setPositiveButton(android.R.string.ok, (d, i) -> {
                Log.v(App.TAG, "mark item to delete: " + item);
                item.setDeleted();
                removeDeletedItemViews();
            })
            .setNegativeButton(android.R.string.cancel, (d, i) -> d.cancel())
            .show();

        return true;
    }


}
