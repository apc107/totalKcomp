package com.example.mycs.totalkomp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.BeepManager;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;
import com.journeyapps.barcodescanner.camera.CameraSettings;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class MainActivity extends AppCompatActivity implements DecoratedBarcodeView.TorchListener, View.OnClickListener {

    private String lastResult;
    private TextView tvScanned;
    private ImageView imageView;
    private BeepManager beepManager;
    private CameraSettings cameraSettings;
    private CaptureManager captureManager;
    private EditText etName, etPlace, etPrice, etDate;
    private Drawable turnOnTorch, turnOffTorch;
    private DecoratedBarcodeView decoratedBarcodeView;
    private String stringTurnOnTorch, stringTurnOffTorch;
    private Collection<BarcodeFormat> formats = Arrays.asList(
            BarcodeFormat.EAN_13,
            BarcodeFormat.EAN_8,
            BarcodeFormat.UPC_EAN_EXTENSION,
            BarcodeFormat.UPC_E,
            BarcodeFormat.UPC_A);
    private ImageButton btnToggleTorch, btnToggleDecoratedBarcodeView;
    private String stringPauseDecoratedBarcodeView, stringResumeDecoratedBarcodeView;
    private Drawable pauseDecoratedBarcodeView, resumeDecoratedBarcodeView;
    //---------------------------
    private SQLiteDatabase db;
    private String TABLE_NAME = "tk1", TABLE_NAME2 = "tk2";
    private String sql, sql2;
    Button btnAdd, btnDel, btnQuery;
    String newName, newCode, newPrice, newPlace, newDate, key_code;
    int id , code;
    //---------------------------
    private BarcodeCallback barcodeCallback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {

            if (result.getText() == null || result.getText().equals(lastResult))
                return;

            lastResult = result.getText();
            tvScanned.setText(lastResult);
            decoratedBarcodeView.setStatusText(lastResult);
            imageView.setImageBitmap(result.getBitmapWithResultPoints(Color.YELLOW));
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //--------------------
        initView();
        open();
        createTable();
        createTable2();
        //--------------------

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        etName = findViewById(R.id.etName);
        etPrice = findViewById(R.id.etPrice);
        tvScanned = findViewById(R.id.tvScanned);
        imageView = findViewById(R.id.ivScanned);
        AdView adView = findViewById(R.id.adView);
        etPlace = findViewById(R.id.etPlace);
        btnToggleTorch = findViewById(R.id.btnToggleTorch);
        decoratedBarcodeView = findViewById(R.id.decoratedBarcodeView);
        btnToggleDecoratedBarcodeView = findViewById(R.id.btnToggleDecoratedBarcodeView);

        tvScanned.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                beepManager.playBeepSoundAndVibrate();
                toggleDecoratedBarcodeView(decoratedBarcodeView);
                query();
            }
        });

        cameraSettings = new CameraSettings();
        beepManager = new BeepManager(this);
        stringTurnOnTorch = getResources().getString(R.string.turnOnTorch);
        stringTurnOffTorch = getResources().getString(R.string.turnOffTorch);
        turnOnTorch = getResources().getDrawable(R.drawable.turn_on_torch, null);
        turnOffTorch = getResources().getDrawable(R.drawable.turn_off_torch, null);
        stringPauseDecoratedBarcodeView = getResources().getString(R.string.pauseDecoratedBarcodeView);
        stringResumeDecoratedBarcodeView = getResources().getString(R.string.resumeDecoratedBarcodeView);
        pauseDecoratedBarcodeView = getResources().getDrawable(R.drawable.pause_barcode_view, null);
        resumeDecoratedBarcodeView = getResources().getDrawable(R.drawable.resume_barcode_view, null);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            startActivity(new Intent(MainActivity.this, SplashPermissionActivity.class));
            finish();
        }

        MobileAds.initialize(this, getString(R.string.googleAdsKey));
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        cameraSettings.setRequestedCameraId(0);
        decoratedBarcodeView.setTorchListener(this);
        decoratedBarcodeView.getBarcodeView().setCameraSettings(cameraSettings);
        decoratedBarcodeView.getBarcodeView().setDecoderFactory(new DefaultDecoderFactory(formats));

        decoratedBarcodeView.resume();

        if (!hasTorch())
            btnToggleTorch.setVisibility(View.GONE);

        decoratedBarcodeView.decodeContinuous(barcodeCallback);
    }

    @Override
    public void onTorchOn() {
        btnToggleTorch.setImageDrawable(turnOffTorch);
    }

    @Override
    public void onTorchOff() {
        btnToggleTorch.setImageDrawable(turnOnTorch);
    }

    private boolean hasTorch() {
        return getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    public void toggleTorch(View view) {
        if (getString(R.string.turnOnTorch).contentEquals(btnToggleTorch.getContentDescription())) {
            decoratedBarcodeView.setTorchOn();
            btnToggleTorch.setContentDescription(stringTurnOffTorch);
        } else {
            decoratedBarcodeView.setTorchOff();
            btnToggleTorch.setContentDescription(stringTurnOnTorch);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return decoratedBarcodeView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }

    public void toggleDecoratedBarcodeView(View view) {
        if (getString(R.string.pauseDecoratedBarcodeView).contentEquals(btnToggleDecoratedBarcodeView.getContentDescription())) {
            decoratedBarcodeView.pause();
            btnToggleDecoratedBarcodeView.setImageDrawable(resumeDecoratedBarcodeView);
            btnToggleDecoratedBarcodeView.setContentDescription(stringResumeDecoratedBarcodeView);
        } else {
            decoratedBarcodeView.resume();
            btnToggleDecoratedBarcodeView.setImageDrawable(pauseDecoratedBarcodeView);
            btnToggleDecoratedBarcodeView.setContentDescription(stringPauseDecoratedBarcodeView);
        }
    }
    //-------------------------
    private void open() {
        String path = "/data/data/" + getPackageName() + "/contacts.db";
        db = SQLiteDatabase.openOrCreateDatabase(path, null);
        Log.i("新增資料表", path);
    }

    private void createTable() {
        sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT, code TEXT, place TEXT, price TEXT, date TEXT);";
        Log.i("建立資料表", sql);
        db.execSQL(sql);
    }

    private void createTable2(){
        sql2 = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME2 +
                "(code TEXT, name TEXT);";
        Log.i("建立資料表2", sql2);
        db.execSQL(sql2);
    }

    private void initView() {
        etDate = findViewById(R.id.etDate);
        btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(this);
        btnDel = findViewById(R.id.btnDel);
        btnDel.setOnClickListener(this);
        btnQuery = findViewById(R.id.btnQuery);
        btnQuery.setOnClickListener(this);
    }

    private void add() {
        newCode = tvScanned.getText().toString();//取得資料 轉字串
        newName = etName.getText().toString();
        newPrice = etPrice.getText().toString();
        newPlace = etPlace.getText().toString();
        newDate = etDate.getText().toString();
        sql2 = "INSERT INTO " + TABLE_NAME2 + "(code, name) VALUES (?,?)";
        try {
            db.execSQL(sql2, new Object[]{newCode, newName});//new一陣列放參數
        } catch (Exception e) {
            e.printStackTrace(); //抓錯誤訊息
        } finally {
            Toast.makeText(this, "sql2已新增成功!", Toast.LENGTH_SHORT).show();//成功則跳出訊息
        }
        sql = "INSERT INTO " + TABLE_NAME + "(code, place, price, date) VALUES (?,?,?,?)";//?是參數
        try {
            db.execSQL(sql, new Object[]{newCode, newPlace, newPrice, newDate});//new一陣列放參數
        } catch (Exception e) {
            e.printStackTrace(); //抓錯誤訊息
        } finally {
            Toast.makeText(this, "sql已新增成功!", Toast.LENGTH_SHORT).show();//成功則跳出訊息
        }
        etName.setText("");//新增完後清空欄位
        etPlace.setText("");
        etPrice.setText("");
        etDate.setText("");
    }

    private void delete() {
        sql = "DELETE FROM " + TABLE_NAME + " WHERE _id=?";
        try {
            db.execSQL(sql, new String[]{String.valueOf(id)});
        } catch (Exception e) {
            e.printStackTrace(); //抓錯誤訊息
        } finally {
            Toast.makeText(getApplicationContext(), "成功刪除sql資料!", Toast.LENGTH_SHORT).show();//成功則跳出訊息
        }
//        sql2 = "DELETE FROM " + TABLE_NAME2 + " WHERE code=?"; //code ? _id?
//        db.execSQL(sql2, new String[]{String.valueOf(code)});
//        Toast.makeText(getApplicationContext(), "成功刪除sql2資料!", Toast.LENGTH_SHORT).show();
        etName.setText("");//刪除完後清空欄位
        etPrice.setText("");
        etPlace.setText("");
        etDate.setText("");
    }

    private void query() { //關聯查詢
        key_code = lastResult; //查詢的部分取得資料 轉字串
        sql2 = "SELECT * FROM "+ TABLE_NAME2 + " Where code = ?";
        Cursor cursor = db.rawQuery(sql2, new String[]{key_code}); //cursor 抓整組資料回傳,new Object[]也行

        if(cursor.getCount()>0){ //有資料
            Cursor cursor2 = db.rawQuery("SELECT tk1._id, tk2.code, tk2.name, tk1.place, tk1.price, tk1.date FROM tk2 INNER JOIN tk1 on tk2.code = tk1.code ", new String[]{});
            while (cursor2.moveToNext()) { //列出多筆資料要用while包起來,一筆則用"cursor.moveToNext()",cursor預設-1開始,要先移動到下一筆0才不會出錯
                id = cursor2.getInt(0);
    //            tvScanned.setText(cursor2.getString(1));//獲取第一列的值,第一列的索引從0開始
                etName.setText(cursor2.getString(2));
                etPlace.setText(cursor2.getString(3));
                etPrice.setText(cursor2.getString(4));
                etDate.setText(cursor2.getString(5));
            }
            cursor2.close();
        }else{
            Toast.makeText(getApplicationContext(),"查無資料!是否新增?",Toast.LENGTH_SHORT).show();
        }
        cursor.close();
     }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnAdd:
                add();
                break;
            case R.id.btnDel:
                delete();
                break;
//            case R.id.btnQuery:
//                query();
//                break;
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
    //--------------------------
}