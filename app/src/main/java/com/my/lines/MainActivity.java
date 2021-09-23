package com.my.lines;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {
    Button start,save,add,mod;
    ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        save=findViewById(R.id.save);
        start=findViewById(R.id.start);
        add=findViewById(R.id.add);
        mod=findViewById(R.id.mod);
        imageView=findViewById(R.id.imageview);
    }



}