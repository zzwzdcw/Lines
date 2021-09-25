package com.my.lines;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ClipData;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private static final int IMAGE_HEAD_CODE = 1;
    private static final int IMAGE_ADD_CODE = 2;
    private static final int RESIZE_REQUEST_CODE = 3;
    private static final String IMAGE_FILE_NAME = "MOD";
    Button start,adds,mod;
    ImageView imageView;
    ArrayList<String> paths = new ArrayList<>();            //存头图和后面添加的图片的path  不包含line
    ArrayList<String> modPaths = new ArrayList<>();         //存了line的图的paht
    ArrayList<Bitmap> modBitmap = new ArrayList<>();        //存第三张开始的path
//    int originalHeight,OriginalWidth;
    int lineHeight,lineWidth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestAllPower();//获取动态权限
        start=findViewById(R.id.start);
        adds=findViewById(R.id.adds);
        mod=findViewById(R.id.mod);
        imageView=findViewById(R.id.imageview);


    }


    //请求应用所需所有权限
    public void requestAllPower() {

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 1);

    }
    public void setStart(View view) {
        //在这里跳转到手机系统相册里面
        paths.clear();
        modPaths.clear();
        modBitmap.clear();
        Intent intent = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, IMAGE_HEAD_CODE);
    }

    public void setAdds(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Picture"), IMAGE_ADD_CODE);

    }

    public void setMod(View view) {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON) //开启选择器
                .setActivityTitle("台词范围裁剪")
                .setCropShape(CropImageView.CropShape.RECTANGLE)  //选择矩形裁剪
                .setFixAspectRatio(false)
                .setMinCropResultSize(30,30)
                .setMinCropWindowSize(30,30)
                .start(MainActivity.this);
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            //头图
            case IMAGE_HEAD_CODE:
            {
                if (resultCode == RESULT_OK) {//resultcode是setResult里面设置的code值
                    try {
                        Uri selectedImage = data.getData(); //获取系统返回的照片的Uri
                        paths.add(getRealPathFromURI(selectedImage));
                        imageView.setImageURI(selectedImage);
                        Log.v("debug", "头图添加完毕");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            }
            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE: {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {
                    final Uri resultUri = result.getUri();  //获取裁减后的图片的Uri
                    imageView.setImageURI(resultUri);
                    modPaths.add(getRealPathFromURI(resultUri));
                    Bitmap bitmap= BitmapFactory.decodeFile(modPaths.get(0));
                    lineHeight=bitmap.getHeight();
                    lineWidth=bitmap.getWidth();
                    Log.v("debug", "lineWidth:"+lineWidth+"lineHeight:"+lineHeight);

                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Log.v("debug", "裁剪的模块");
                }
                break;
            }
            case IMAGE_ADD_CODE:{
                //这里拿到的是第三张图片
                if ( data != null){
                    ClipData imageNames = data.getClipData();
                    if (imageNames != null){
                        for (int i=0; i<imageNames.getItemCount(); i++){
                            Uri imageUri = imageNames.getItemAt(i).getUri();
                            paths.add(getRealPathFromURI(imageUri));
                        }
                        for (int i = 1; i< paths.size(); i++){
                            Bitmap bitmap= BitmapFactory.decodeFile(paths.get(i));
                            Log.v("debug", "构建bitmap");
                            Bitmap bitmap1=Bitmap.createBitmap(bitmap,bitmap.getWidth()-lineWidth,bitmap.getHeight()-lineHeight,lineWidth,lineHeight);
                            Log.v("debug", "新建bitmap");
                            modBitmap.add(bitmap1);
                        }
                        int height,wide;
                        Bitmap headBitmp=BitmapFactory.decodeFile(paths.get(0));
                        wide = Math.max(headBitmp.getWidth(),lineWidth);
                        height =headBitmp.getHeight()+lineHeight*paths.size();
                        Bitmap result = Bitmap.createBitmap(wide,height,Bitmap.Config.RGB_565);
                        Log.v("debug", "resultWidth:"+wide+"resultHeight:"+height);

                        int left=0,top=0;
                        Canvas canvas = new Canvas(result);
                        canvas.drawBitmap(headBitmp, 0, 0, null);
                        Log.v("debug", "添加头图进入");
                        //头图添加完成
                        top+=headBitmp.getHeight();
                        canvas.drawBitmap(BitmapFactory.decodeFile(modPaths.get(0)), 0, top, null);
                        Log.v("debug", "添加第1句台词进入");
                        //第一句台词添加完成
                        for (int i = 0 ; i <modBitmap.size();i++){
                            top+=lineHeight;
                            canvas.drawBitmap((modBitmap.get(i)), 0, top, null);
                            Log.v("debug", "添加第"+i+2+"句台词进入");
                        }
                        //全部完成
                        saveImageToGallery(result);
                        imageView.setImageBitmap(result);
                        Toast.makeText(getApplicationContext(), "已经保存到相册", Toast.LENGTH_SHORT).show();
                    }
                }
            }


        }

    }




    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) {
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    /**
     * 本段代码来自https://www.jianshu.com/p/54083466b044
     * @param bmp
     * @return
     */
    public int saveImageToGallery(Bitmap bmp) {

        //生成路径
        String root = Environment.getExternalStorageDirectory().getAbsolutePath();
        String dirName = "Lines";
        File appDir = new File(root , dirName);
        if (!appDir.exists()) {
            appDir.mkdirs();
        }

        //文件名为时间
        long timeStamp = System.currentTimeMillis();
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String sd = sdf.format(new Date(timeStamp));
        String fileName = sd + ".jpg";

        //获取文件
        File file = new File(appDir, fileName);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            Log.v("debug", "保存成功");
            fos.flush();
            //通知系统相册刷新
            MainActivity.this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    Uri.fromFile(new File(file.getPath()))));
            return 2;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }


}