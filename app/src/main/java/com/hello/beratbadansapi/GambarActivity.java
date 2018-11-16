package com.hello.beratbadansapi;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.soundcloud.android.crop.Crop;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.aprilapps.easyphotopicker.EasyImage;

public class GambarActivity extends AppCompatActivity {

    @BindView(R.id.image_original)
    ImageView imageOriginal;
    @BindView(R.id.image_grayscale)
    ImageView imageGrayscale;
    @BindView(R.id.image_biner)
    ImageView imageBiner;
    @BindView(R.id.image_panjang_badan)
    ImageView imagePanjangBadan;
    @BindView(R.id.image_tinggi_badan)
    ImageView imageTinggiBadan;
    @BindView(R.id.image_lebar_dada)
    ImageView imageLebarDada;

    @BindView(R.id.layout_original)
    LinearLayout layoutOriginal;
    @BindView(R.id.layout_grayscale)
    LinearLayout layoutGrayscale;
    @BindView(R.id.layout_biner)
    LinearLayout layoutBiner;
    @BindView(R.id.layout_panjang_badan)
    LinearLayout layoutPanjangBadan;
    @BindView(R.id.layout_tinggi_badan)
    LinearLayout layoutTinggiBadan;
    @BindView(R.id.layout_lebar_dada)
    LinearLayout layoutLebarDada;
    @BindView(R.id.layout_morfologi)
    LinearLayout layoutMorfologi;

    Intent intent;
    Uri fileUri, uriBitmap;
    String path;

    Bitmap bitmap, decoded, bitmapGrayscale, bitmapBiner, bitmapPanjangBadan, bitmapLebarBadan, bitmapTinggiBadan;

    public final int REQUEST_CAMERA = 0;
    public final int SELECT_FILE = 1;

    int request;

    int bitmap_size = 40; // image quality 1 - 100;
    int max_resolution_image = 800;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gambar);
        ButterKnife.bind(this);
        initToolbar();
    }

    @OnClick({R.id.btn_choose_image, R.id.btn_gray_scale, R.id.btn_biner, R.id.btn_panjang_badan, R.id.btn_tinggi_badan, R.id.btn_lebar_dada, R.id.btn_proses_morfologi})
    public void actionButton(View v) {
        BitmapDrawable util;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        switch (v.getId()) {
            case R.id.btn_choose_image:
                selectImage();
                break;
            case R.id.btn_gray_scale:
                imageGrayscale.setImageBitmap(convertToGrayscale(decoded));
                util = (BitmapDrawable) imageGrayscale.getDrawable();
                bitmapGrayscale = util.getBitmap();
                layoutBiner.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_biner:
                imageBiner.setImageBitmap(convertToBinary(bitmapGrayscale));
                util = (BitmapDrawable) imageBiner.getDrawable();
                bitmapBiner = util.getBitmap();
                bitmapBiner.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                layoutPanjangBadan.setVisibility(View.VISIBLE);
                convertToUri(bitmapBiner);
                break;
            case R.id.btn_panjang_badan:
                request = 0;
                beginCrop(uriBitmap);
                layoutTinggiBadan.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_tinggi_badan:
                request = 1;
                beginCrop(uriBitmap);
                layoutLebarDada.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_lebar_dada:
                request = 2;
                beginCrop(uriBitmap);
                layoutMorfologi.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_proses_morfologi:
                Toast.makeText(GambarActivity.this, "On Development !", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e("onActivityResult", "requestCode " + requestCode + ", resultCode " + resultCode);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
                try {
                    Log.e("CAMERA", fileUri.getPath());

                    bitmap = BitmapFactory.decodeFile(fileUri.getPath());
                    setToImageView(getResizedBitmap(bitmap, max_resolution_image));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (requestCode == SELECT_FILE && data != null && data.getData() != null) {
                try {
                    // mengambil gambar dari Gallery
                    Log.e("GALERI", String.valueOf(Uri.parse(data.getData().toString())));
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
                    setToImageView(getResizedBitmap(bitmap, max_resolution_image));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (requestCode == Crop.REQUEST_CROP && request == 0) {
                handleCrop(resultCode, data, imagePanjangBadan);
            } else if (requestCode == Crop.REQUEST_CROP && request == 1) {
                handleCrop(resultCode, data, imageTinggiBadan);
            } else if (requestCode == Crop.REQUEST_CROP && request == 2) {
                handleCrop(resultCode, data, imageLebarDada);
            }
        }
    }

    private void convertToUri(Bitmap bitmapBiner) {
        path = MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(), bitmapBiner, "" + Math.random(), null);
        uriBitmap = Uri.parse(path);
    }

    private void beginCrop(Uri source) {
        Uri destination = Uri.fromFile(new File(getCacheDir(), "cropped"));
        Crop.of(source, destination).asSquare().start(this);
    }

    private void handleCrop(int resultCode, Intent result, ImageView imageView) {
        if (resultCode == RESULT_OK) {
            imageView.setImageURI(Crop.getOutput(result));
        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(this, Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public Bitmap convertToGrayscale(Bitmap bmpOriginal) {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    public Bitmap convertToBinary(Bitmap src) {
        int width = src.getWidth();
        int height = src.getHeight();

        Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());

        int A, R, G, B;
        int pixel;
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                //perhitungan proses bitmap to grayscale
                pixel = src.getPixel(x, y);
                A = Color.alpha(pixel);
                R = Color.red(pixel);
                G = Color.green(pixel);
                B = Color.blue(pixel);
                int gray = (int) (0.2989 * R + 0.5870 * G + 0.1140 * B);

                // use 128 as threshold, above -> white, below -> black
                //perhitungan proses grayscale to binary
                if (gray > 128) {
                    gray = 255;
                } else {
                    gray = 0;
                }

                // set new pixel color to output bitmap
                bmOut.setPixel(x, y, Color.argb(A, gray, gray, gray));
            }
        }
        return bmOut;
    }

    private void selectImage() {
        StrictMode.VmPolicy.Builder builderCamera = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builderCamera.build());
        imageOriginal.setImageResource(0);
        final CharSequence[] items = {"Take Photo", "Choose from Library"};

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Add Photo!");
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    fileUri = getOutputMediaFileUri();
                    intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, fileUri);
                    startActivityForResult(intent, REQUEST_CAMERA);
                } else if (items[item].equals("Choose from Library")) {
                    intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_FILE);
                }
            }
        });
        builder.show();
    }

    // Untuk menampilkan bitmap pada ImageView
    private void setToImageView(Bitmap bmp) {
        //compress image
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, bitmap_size, bytes);
        decoded = BitmapFactory.decodeStream(new ByteArrayInputStream(bytes.toByteArray()));

        //menampilkan gambar yang dipilih dari camera/gallery ke ImageView
        imageOriginal.setImageBitmap(decoded);
        layoutGrayscale.setVisibility(View.VISIBLE);
    }

    // Untuk resize bitmap
    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    public Uri getOutputMediaFileUri() {
        return Uri.fromFile(getOutputMediaFile());
    }

    private static File getOutputMediaFile() {

        // External sdcard location
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "2D-OD");

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.e("Monitoring", "Oops! Failed create Monitoring directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_Detector_" + timeStamp + ".jpg");

        return mediaFile;
    }

    private void initToolbar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Halaman Gambar");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}

