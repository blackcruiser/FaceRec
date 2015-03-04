package fz.facerec;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

public class MainActivity extends Activity {

    private Button btnOpenFile, btnOpenCamera, btnDetect;
    private ImageView imageView;
    private boolean isLoadLibSuccess = false;


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {

        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Toast toast = Toast.makeText(getApplicationContext(), "Load OpenCV success!", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.BOTTOM, 0, 0);
                    toast.show();

                    isLoadLibSuccess = true;
                }
                break;
                default: {
                    Toast toast = Toast.makeText(getApplicationContext(), "Fail to load OpenCV library!", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();

                    isLoadLibSuccess = false;
                    finish();
                }
                break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);

        imageView = (ImageView)findViewById(R.id.imageView);

        btnOpenFile = (Button)findViewById(R.id.Button_OpenFile);
        btnOpenFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("image/*");
                intent.putExtra("return-data", true);
                startActivityForResult(intent, 0);
            }
        });

        btnOpenCamera = (Button)findViewById(R.id.Button_OpenCamera);
        btnDetect = (Button)findViewById(R.id.Button_Detect);
    }

    @Override
    protected void onResume() {
        super.onResume();

        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_10, this, mLoaderCallback);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 0: {
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    ContentResolver resolver = getContentResolver();
                    String fileType = resolver.getType(uri);

                    if (fileType.startsWith("image"))
                    {
                        Cursor cursor = resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Images.Media.DATA}, null, null, null);
                        cursor.moveToFirst();
                        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                        Mat img = Highgui.imread(path);
                        //TODO
                        imageView.setImageURI(uri);
                    }
                }
                break;
            }
            default:
                break;

        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
