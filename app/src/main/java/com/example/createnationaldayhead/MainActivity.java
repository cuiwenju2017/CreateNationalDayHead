package com.example.createnationaldayhead;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.createnationaldayhead.databinding.ActivityMainBinding;

import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private int REQUEST_CODE_SELECT_USER_ICON = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initView();
    }

    private void initView() {
        //保存头像
        binding.btnSave.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 201);
            } else {
                getDraw();
            }
        });

        //更换头像
        binding.btnChoose.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    //没有权限则申请权限
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 202);
                } else {
                    //有权限直接执行,docode()不用做处理
                    doCode();
                }
            } else {
                //小于6.0，不用申请权限，直接执行
                doCode();
            }
        });
    }

    private void doCode() {
        PictureSelectorUtils.ofImage(MainActivity.this, REQUEST_CODE_SELECT_USER_ICON);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_USER_ICON && resultCode == Activity.RESULT_OK) {
            String userIconPath = PictureSelectorUtils.forResult(resultCode, data);
            if (userIconPath != null) {
                Glide.with(this).load(userIconPath).into(binding.iv);
            }
        }
    }

    private void getDraw() {
        // 获取图片某布局
        binding.rl.setDrawingCacheEnabled(true);
        binding.rl.buildDrawingCache();

        mHandler.postDelayed(() -> {
            // 要在运行在子线程中
            final Bitmap bmp = binding.rl.getDrawingCache(); // 获取图片
            savePicture(bmp);// 保存图片
            binding.rl.destroyDrawingCache(); // 保存过后释放资源
        }, 100);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 201:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getDraw();
                } else {
                    Toast.makeText(this, "您拒绝了权限的申请，可能无法进行下面的操作哦~", Toast.LENGTH_LONG).show();
                }
                break;
            case 202:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    doCode();
                } else {
                    Toast.makeText(this, "您拒绝了权限的申请，可能无法进行下面的操作哦~", Toast.LENGTH_LONG).show();
                }
                break;
            default:
        }
    }

    public void savePicture(Bitmap bm) {
        File file = createImageFile();
        //重新写入文件
        try {
            // 写入文件
            FileOutputStream fos;
            fos = new FileOutputStream(file);
            //默认jpg
            bm.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
            bm.recycle();
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
            Toast.makeText(this, "保存成功,请到相册中查看!", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static File createImageFile() {
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Pic");
        if (!dir.exists()) {
            boolean isSuccess = dir.mkdirs();
            Log.i("Pic", "文件夹创建状态--->" + isSuccess);
        }
        return new File(dir.getPath() + File.separator + "img_" + System.currentTimeMillis() + ".png");
    }
}