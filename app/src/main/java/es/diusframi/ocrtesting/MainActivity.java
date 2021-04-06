package es.diusframi.ocrtesting;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextDetector;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public Context mContext;
    public TextView myTextView;
    public ImageView imageView;
    private Uri mImageUri;
    public String pictureImagePath;
    public boolean check = true;
    static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //https://stackoverflow.com/questions/11687939/in-android-stream-camera-preview-onto-a-view Probar el surface view
        //https://firebase.google.com/docs/ml-kit/android/recognize-text ML KIT
        setContentView(R.layout.activity_main);
        mContext = this;
        imageView = (ImageView)findViewById(R.id.imageView);
        myTextView = (TextView)findViewById(R.id.TextviewOCR);
        FirebaseApp.initializeApp(mContext);
        Button buttonCamera = (Button)findViewById(R.id.button);
        Button buttonErase = (Button)findViewById(R.id.button2);
        Button buttonCambio = (Button)findViewById(R.id.button3);
        buttonCambio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(check){
                    imageView.setImageResource(R.drawable.textdrawable);
                    check=!check;
                }else {
                    imageView.setImageResource(R.drawable.textodos);
                    check=!check;
                }
            }
        });

        buttonErase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myTextView.setText("");
            }
        });

        buttonCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }

                //BitmapDrawable myDrawable = (BitmapDrawable)imageView.getDrawable();
                //Bitmap textoboleta = myDrawable.getBitmap();
                //grabImage(textoboleta);

/*
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                File photo;
                try
                {

                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                    String imageFileName = timeStamp + ".jpg";
                    File storageDir = Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_PICTURES);
                    pictureImagePath = storageDir.getAbsolutePath() + "/" + imageFileName;
                    File file = new File(pictureImagePath);
                    Uri outputFileUri = FileProvider.getUriForFile(mContext, mContext.getApplicationContext().getPackageName() + ".provider", file);

                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
                    startActivityForResult(cameraIntent, 1);
                }
                catch(Exception e)
                {
                    Log.v("TAG", "Can't create file to take picture!");
                    Toast.makeText(mContext, "Please check SD card! Image shot is impossible!", Toast.LENGTH_SHORT);
                }
*/
            }
        });


    }
    public void grabImage(Bitmap bitmap)
    {
//        this.getContentResolver().notifyChange(mImageUri, null);
        //ContentResolver cr = this.getContentResolver();
       // Bitmap bitmap;
        try
        {
            //bitmap = android.provider.MediaStore.Images.Media.getBitmap(cr, mImageUri);
            runTextRecognition(bitmap);
        }
        catch (Exception e)
        {
            Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT).show();
            Log.d("TAG", "Failed to load", e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap);
            grabImage(imageBitmap);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void runTextRecognition(Bitmap myBitmap) {
        FirebaseApp.initializeApp(mContext);
        FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(myBitmap);
        FirebaseVisionTextDetector firebaseVisionTextDetector = FirebaseVision.getInstance().getVisionTextDetector();
        firebaseVisionTextDetector.detectInImage(firebaseVisionImage).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                processExtractedText(firebaseVisionText);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("onFail= ", e.getMessage());
                Toast.makeText(mContext, "Exception = " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void processExtractedText(FirebaseVisionText firebaseVisionText) {
        List<FirebaseVisionText.Block> blockList = firebaseVisionText.getBlocks();
        if (blockList.size() == 0) {
            myTextView.setText(null);
            Toast.makeText(mContext, "No Text Found On This Image", Toast.LENGTH_SHORT).show();
        } else {
            for (FirebaseVisionText.Block block : firebaseVisionText.getBlocks()) {
                String text = block.getText();
                myTextView.setText(text);
            }
        }
    }

}