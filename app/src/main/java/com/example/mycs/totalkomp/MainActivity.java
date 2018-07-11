package com.example.mycs.totalkomp;

import android.Manifest;
import android.Manifest.permission;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

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

public class MainActivity extends AppCompatActivity implements DecoratedBarcodeView.TorchListener {

    private String lastResult;
    private TextView tvScanned;
    private ImageView imageView;
    private BeepManager beepManager;
    private CameraSettings cameraSettings;
    private CaptureManager captureManager;
    private EditText etName, etPlace, etPrice;
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

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        etName = findViewById(R.id.etName);
        etPrice = findViewById(R.id.etPrice);
        tvScanned = findViewById(R.id.tvScanned);
        imageView = findViewById(R.id.ivScanned);
        AdView adView = findViewById(R.id.adView);
        etPlace = findViewById(R.id.etGooglePlace);
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
            decoratedBarcodeView.pauseAndWait();
            btnToggleDecoratedBarcodeView.setImageDrawable(resumeDecoratedBarcodeView);
            btnToggleDecoratedBarcodeView.setContentDescription(stringResumeDecoratedBarcodeView);
        } else {
            decoratedBarcodeView.resume();
            btnToggleDecoratedBarcodeView.setImageDrawable(pauseDecoratedBarcodeView);
            btnToggleDecoratedBarcodeView.setContentDescription(stringPauseDecoratedBarcodeView);
        }
    }
}