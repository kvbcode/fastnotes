package com.cyber.fastnotes.service;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.cyber.fastnotes.BuildConfig;
import com.cyber.fastnotes.model.Article;
import com.cyber.fastnotes.model.ArticleItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class ArticleHtmlExport {
    private static String TAG = "ArticleHtmlExport";
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    private final static String HTML_HEADER =
        "<!doctype html>" +
        "<html lang='ru'>" +
        "<head>" +
        "<meta charset='utf-8' />" +
        "<title>#TITLE</title>" +
        "</head><body><center>"+
        "<h1>#TITLE</h1><h3>#DATE</h3>";

    private final static String HTML_FOOTER =
        "<center></body></html>";

    private final static String HTML_TEXT = "<p>#TEXT</p>\n";
    private final static String HTML_IMAGE = "<p><img src='files/#FILENAME' width='90%'></p>\n";
    private final static String HTML_AUDIO = "<p>#TEXT</p>\n<audio src='files/#FILENAME' controls></audio>\n";

    private final Context context;

    public ArticleHtmlExport(Context context){
        this.context = context;
    }

    public boolean export(final Article article, File outDir){
        if (article==null || article.isNew()) return false;

        Log.d(TAG, "export " + article + "to " + outDir.toString());

        String articleName = String.format("%s (%s)", article.getTitle(), article.getId());
        File outArticleDir = new File(outDir, articleName);
        File outArticleFilesDir = new File(outArticleDir, "files");
        outArticleFilesDir.mkdirs();

        // start html header
        StringBuilder sb = new StringBuilder();
        Map<String,String> articleData = new HashMap<>();

        articleData.put("#TITLE", article.getTitle());
        articleData.put("#DATE", dateFormat.format( article.getDate()));
        sb.append( renderTemplate(HTML_HEADER, articleData) );

        // article items
        for(ArticleItem item:article.getItems()){
            Map<String,String> itemData = new HashMap<>();
            itemData.put("#ID", String.valueOf( item.getId() ) );
            itemData.put("#TEXT", item.getText());
            String htmlItemTemplate = "";

            switch(item.type){
                case ArticleItem.TYPE_TEXT:
                    htmlItemTemplate = HTML_TEXT;
                    break;
                case ArticleItem.TYPE_IMAGE:
                    htmlItemTemplate = HTML_IMAGE;
                    itemData.put("#FILENAME", saveUriContent( item.getContentUri(), outArticleFilesDir) );
                    break;
                case ArticleItem.TYPE_AUDIO:
                    htmlItemTemplate = HTML_AUDIO;
                    itemData.put("#FILENAME", saveUriContent( item.getContentUri(), outArticleFilesDir) );
                    break;
            }

            sb.append( renderTemplate( htmlItemTemplate, itemData ) );
        }

        // ends with footer
        sb.append( renderTemplate( HTML_FOOTER, articleData ) );

        Log.v(TAG, sb.toString());
        return saveHtml(outArticleDir, articleName + ".html", sb.toString());
    }

    private boolean saveHtml(File outArticleDir, String articleName, String htmlData){
        File outHtmlFile = new File(outArticleDir, articleName);

        try(FileOutputStream fout = new FileOutputStream(outHtmlFile)){
            fout.write( htmlData.getBytes() );
        }catch(IOException e){
            Log.e(TAG, "error saving article html: " + e.getMessage());
            return false;
        }
        return true;
    }

    private String renderTemplate(String template, Map<String, String> data){
        String out = template;
        for(Map.Entry<String,String> e:data.entrySet()){
            out = out.replaceAll(e.getKey(), e.getValue());
        }
        return out;
    }

    private String saveUriContent(Uri uri, File outFileDir){
        if (BuildConfig.DEBUG) Log.v(TAG, "saveUriContent(): '" + uri + "', to '" + outFileDir + "'");

        File outFile;
        String fileName;
        if (uri.getLastPathSegment().contains(".")){
            fileName = uri.getLastPathSegment();
        }else{
            String extensionType = context.getContentResolver().getType(uri).split("/")[1];
            fileName = IOHelper.createFilename("", extensionType);
        }
        outFile = new File( outFileDir, fileName);


        try(
            InputStream in = context.getContentResolver().openInputStream(uri);
            FileOutputStream out = new FileOutputStream(outFile)
        ) {
            IOHelper.transfer(in, out);
        }catch(IOException e){
            Log.e(TAG, "copying error from '" + uri + "', to '" + outFile + "' msg: " + e.getMessage());
        }

        return fileName;
    }


}
