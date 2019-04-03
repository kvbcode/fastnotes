package com.cyber.fastnotes.view;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cyber.component.AudioPlayerComponent;
import com.cyber.fastnotes.App;
import com.cyber.fastnotes.BuildConfig;
import com.cyber.fastnotes.MakeNoteActivity;
import com.cyber.fastnotes.R;
import com.cyber.fastnotes.service.IOHelper;
import com.cyber.fastnotes.model.Article;
import com.cyber.fastnotes.model.ArticleItem;
import com.cyber.rx.ui.ObservableTextWatcher;

import java.util.concurrent.TimeUnit;

public class ArticleView extends LinearLayout{
    private static final int[] PAD = {8, 32, 8, 32};

    private static final int DEBOUNCE_VALUE = 300;

    private MakeNoteActivity activityContext;
    private Article article;
    private MediaPlayer mediaPlayer = new MediaPlayer();

    public ArticleView(Context context) {
        super(context);
        onCreate(context);
    }

    public ArticleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        onCreate(context);
    }

    public ArticleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        onCreate(context);
    }

    public void onCreate(Context context){
        if (context instanceof MakeNoteActivity){
            activityContext = (MakeNoteActivity)context;
        }
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

    public void update(int index, ArticleItem newItemData){
        newItemData.setArticleId( article.getId() );
        article.getItems().set( index, newItemData );
    }

    public View switchItemViewSupplier(ArticleItem item){
        switch(item.getType()){
            case ArticleItem.TYPE_TEXT:
                return getTextView(item);
            case ArticleItem.TYPE_IMAGE:
                return getImageView(item);
            case ArticleItem.TYPE_AUDIO:
                return getAudioView(item);
            case ArticleItem.TYPE_BARCODE:
                return getBarcodeView(item);
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
        editText.setOnLongClickListener( v -> deleteItemQuery(item) );

        return editText;
    }

    private View getImageView(ArticleItem item) {
        ImageView img = new ImageView(this.getContext());

        Bitmap th = IOHelper.getThumbnailFor(this.getContext(), item.getContentUri(), 512, 512, false);

        img.setImageBitmap(th);
        img.setAdjustViewBounds(true);
        img.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        img.setPadding(PAD[0], PAD[1], PAD[2], PAD[3]);

        img.setOnClickListener( v -> actionShowImage(this.getContext(), item.getContentUri() ) );

        img.setLongClickable(true);
        img.setOnLongClickListener( v -> deleteItemQuery(item) );

        return img;
    }

    private View getAudioView(ArticleItem item){
        AudioPlayerComponent playerComponent = new AudioPlayerComponent(getContext());
        LayoutInflater.from(getContext()).inflate(R.layout.component_audio_player, playerComponent);
        playerComponent.setMediaPlayer( mediaPlayer );
        playerComponent.setAudioSource( item.contentUri );
        playerComponent.setTitle( item.getText() );

        playerComponent.setOptionsButtonVisible(true);
        playerComponent.setOnOptionButtonClick(v -> {
            if (activityContext!=null) activityContext.actionRecordAudio( item );
        });

        playerComponent.setPadding(PAD[0], PAD[1], PAD[2], PAD[3]);

        playerComponent.setLongClickable( true );
        playerComponent.setOnLongClickListener( v -> deleteItemQuery(item) );

        return playerComponent;
    }

    private View getBarcodeView(ArticleItem item) {
        TextView textView = new TextView(this.getContext());
        textView.setText(item.getText());
        textView.setAutoLinkMask(Linkify.ALL);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18.0F);
        textView.setMovementMethod(LinkMovementMethod.getInstance());

        textView.setPadding(PAD[0], PAD[1], PAD[2], PAD[3]);

        textView.setLongClickable(true);
        textView.setOnLongClickListener( v -> deleteItemQuery(item) );

        textView.setOnClickListener(view -> actionWebSearch(getContext(), item.getText()));

        return textView;
    }

    public void actionWebSearch(Context context, String query){
        Intent intent = new Intent(Intent.ACTION_WEB_SEARCH );
        intent.putExtra(SearchManager.QUERY, query);
        context.startActivity(intent);
    }

    public void actionShowImage(Context context, Uri contentUri){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(contentUri, "image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(intent);
    }

    public boolean deleteItemQuery(ArticleItem item){
        String title = getResources().getString(R.string.query_delete_article_item);
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
