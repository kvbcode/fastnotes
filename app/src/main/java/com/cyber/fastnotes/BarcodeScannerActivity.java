package com.cyber.fastnotes;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import me.dm7.barcodescanner.zxing.ZXingScannerView;
import com.google.zxing.Result;

public class BarcodeScannerActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    public static final String PARAM_BARCODE_LIST = "barcode_list";
    private static final long DEBOUNCE_INTERVAL = 1000;

    private String mTitle;

    private ZXingScannerView mScannerView;
    private ArrayList<String> mBarcodeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTitle = getResources().getString(R.string.title_activity_barcodes);
        mBarcodeList = new ArrayList<>();

        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setHomeButtonEnabled(true);

        mScannerView = new ZXingScannerView(this);
        mScannerView.setAutoFocus(true);
        setContentView(mScannerView);

        updateTitle();
    }

    @Override
    public void onResume() {
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mScannerView.stopCamera();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList(PARAM_BARCODE_LIST, mBarcodeList);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mBarcodeList = savedInstanceState.getStringArrayList( PARAM_BARCODE_LIST );
    }

    private void updateTitle(){
        setTitle(mTitle + ": " + mBarcodeList.size());
    }

    @Override
    public void handleResult(Result rawResult) {
        mBarcodeList.add(rawResult.getText());
        String msg = rawResult.getBarcodeFormat().toString() + ": " + rawResult.getText();
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        updateTitle();

        Completable.timer(DEBOUNCE_INTERVAL, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(() -> mScannerView.resumeCameraPreview(this) );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
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
                Intent intent = new Intent();
                intent.putStringArrayListExtra(PARAM_BARCODE_LIST, mBarcodeList);
                setResult(RESULT_OK, intent);
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

}
