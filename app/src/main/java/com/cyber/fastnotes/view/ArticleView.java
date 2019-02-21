package com.cyber.fastnotes.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.cyber.fastnotes.App;
import com.cyber.fastnotes.R;
import com.cyber.fastnotes.service.IOHelper;
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

        return editText;
    }

    private View getImageView(ArticleItem item) {
        ImageView img = new ImageView(this.getContext());

        Bitmap bitmap = IOHelper.loadBitmap(this.getContext(), item.getContentUri());
        Bitmap th = ThumbnailUtils.extractThumbnail(bitmap, 1024, 1024);
        bitmap.recycle();

        img.setImageBitmap(th);
        img.setOnClickListener( (v) -> doShowImage(this.getContext(), item.getContentUri() ) );

        return img;
    }

    public void doShowImage(Context context, Uri contentUri){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(contentUri, "image/*");
        context.startActivity(intent);
    }


}
