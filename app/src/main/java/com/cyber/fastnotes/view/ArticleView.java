package com.cyber.fastnotes.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.cyber.model.Article;
import com.cyber.model.ArticleItem;

public class ArticleView extends LinearLayout{

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
        notifyDatasetChanged();
    }

    public void notifyDatasetChanged(){
        this.removeAllViews();
        for(int i=0; i<article.size(); i++) {
            this.addView(getItemView(i), i);
        }
    }

    public View getItemView(int index){
        ArticleItem item = article.get(index);

        switch(item.getType()){
            case ArticleItem.TYPE_TEXT:
                return getTextItemView((ArticleItem.Text)item);
            case ArticleItem.TYPE_IMAGE:
                return getImageItemView((ArticleItem.Image)item);
        }

        return getTextItemView((ArticleItem.Text)item);
    }

    private View getTextItemView(ArticleItem.Text item) {
        EditText editText = new EditText(this.getContext());
        editText.setText(item.getData());

        editText.setOnFocusChangeListener((view, hasFocus) -> item.setData(((EditText)view).getText().toString()));

        return editText;
    }

    private View getImageItemView(ArticleItem.Image item) {
        ImageView img = new ImageView(this.getContext());
        img.setImageBitmap(item.getData());

        return img;
    }


}
