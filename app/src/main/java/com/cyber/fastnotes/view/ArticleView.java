package com.cyber.fastnotes.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.cyber.fastnotes.R;
import com.cyber.fastnotes.service.IOHelper;
import com.cyber.model.Article;
import com.cyber.model.ArticleItem;

public class ArticleView extends LinearLayout{
    private static final int[] PAD = {8, 32, 8, 32};


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
                this.addView(switchItemView(i), i);
            }
        }
    }

    public void notifyItemInserted(){
        int index = article.size() - 1;
        View view = switchItemView(index);
        addView( view );
        view.requestFocus();
    }

    public View switchItemView(int index){
        ArticleItem item = article.get(index);

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

        editText.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus) item.setText(((EditText)view).getText().toString());
        });

        editText.setPadding(PAD[0], PAD[1], PAD[2], PAD[3]);

        return editText;
    }

    private View getImageView(ArticleItem item) {
        ImageView img = new ImageView(this.getContext());

        Bitmap th = IOHelper.getThumbnailFor(this.getContext(), item.getContentUri(), 512, 512, false);

        img.setImageBitmap(th);
        img.setOnClickListener( (v) -> doShowImage(this.getContext(), item.getContentUri() ) );
        img.setAdjustViewBounds(true);
        img.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        img.setPadding(PAD[0], PAD[1], PAD[2], PAD[3]);

        return img;
    }

    public void doShowImage(Context context, Uri contentUri){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(contentUri, "image/*");
        context.startActivity(intent);
    }


}
