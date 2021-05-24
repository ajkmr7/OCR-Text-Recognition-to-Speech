package com.ajay.ocrapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {


    EditText textView;
    Button captureImage,speak,uploadImage;
    ImageView imageView;

    Bitmap imageBitmap;

    TextToSpeech mTTS;

    TextRecognizer recognizer = TextRecognition.getClient();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.editText);
        captureImage = findViewById(R.id.button3);
        imageView = findViewById(R.id.imageView);
        speak = findViewById(R.id.button4);
        uploadImage = findViewById(R.id.button5);

        textView.setMovementMethod(new ScrollingMovementMethod());



        captureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });

        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");

                startActivityForResult(Intent.createChooser(intent, "Pick an image"),1);
            }
        });

        mTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i == TextToSpeech.SUCCESS){
                    int status = mTTS.setLanguage(Locale.ENGLISH);

                    if(status == TextToSpeech.LANG_MISSING_DATA || status == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.i("TTS","Language not supported!");
                    }
                    else{
                        speak.setEnabled(true);
                    }
                }
            }
        });

        speak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tts();
            }
        });
    }

    private void detectTextFromImage() {

        InputImage image = InputImage.fromBitmap(imageBitmap,0);

        Task<Text> result =
                recognizer.process(image)
                        .addOnSuccessListener(new OnSuccessListener<Text>() {
                            @Override
                            public void onSuccess(Text visionText) {
                                if(visionText.getText().isEmpty()){
                                    textView.setText("No text found!");
                                }
                                else
                                textView.setText(visionText.getText());
                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        textView.setText("No text found!");
                                    }
                                });



    }

    static final int REQUEST_IMAGE_CAPTURE = 10;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } catch (ActivityNotFoundException e) {
            // display error state to the user
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();

            imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap);
            detectTextFromImage();
        }
        if(resultCode == RESULT_OK && requestCode == 1){
            try {
                InputStream inputStream = getContentResolver().openInputStream(data.getData());

                imageBitmap = BitmapFactory.decodeStream(inputStream);
                 imageView.setImageBitmap(imageBitmap);
                 detectTextFromImage();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }
    }

    private void tts() {
        String text = textView.getText().toString();
        mTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }



    @Override
    protected void onDestroy() {
        if (mTTS != null) {
            mTTS.stop();
            mTTS.shutdown();
        }
        super.onDestroy();
    }

}
