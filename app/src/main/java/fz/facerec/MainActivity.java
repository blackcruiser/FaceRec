package fz.facerec;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.io.InputStream;
import java.lang.ref.WeakReference;

public class MainActivity extends Activity
{
    final static int LOAD_MODEL_SUCCESS = 0;
    final static int FACE_DETECT_SUCCESS = 1;

    private Button btnOpenFile, btnOpenCamera, btnDetect;
    private ImageView imageView;

    private Bitmap bmImg = null, bmResult = null;
    private FaceRec faceRec = null;

    private boolean isModelLoaded = false;


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this)
    {

        @Override
        public void onManagerConnected(int status)
        {
            switch (status)
            {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Toast toast = Toast.makeText(getApplicationContext(), "Load OpenCV success!", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.BOTTOM, 0, 0);
                    toast.show();

                    btnDetect.setEnabled(true);
                    btnOpenCamera.setEnabled(true);
                    btnOpenFile.setEnabled(true);

                    new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            faceRec = new FaceRec();
                            Message msg = new Message();
                            msg.what = LOAD_MODEL_SUCCESS;
                            handler.sendMessage(msg);
                        }
                    }).start();
                }
                break;
                default:
                {
                    Toast toast = Toast.makeText(getApplicationContext(), "Fail to load OpenCV library!", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();

                    finish();
                }
                break;
            }
        }
    };

    static class MyHandler extends Handler {
        WeakReference<MainActivity> mActivity;

        MyHandler(MainActivity activity) {
            mActivity = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity theActivity = mActivity.get();
            switch (msg.what)
            {
                case MainActivity.LOAD_MODEL_SUCCESS:
                    theActivity.isModelLoaded = true;
                    break;
                case MainActivity.FACE_DETECT_SUCCESS:
                    theActivity.imageView.setImageBitmap(theActivity.bmResult);
                    break;
                default:
                    break;
            }
        }
    }
    MyHandler handler = new MyHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);
        imageView = (ImageView)findViewById(R.id.imageView);

        btnOpenFile = (Button)findViewById(R.id.Button_OpenFile);
        btnOpenFile.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("image/*");
                intent.putExtra("return-data", true);
                startActivityForResult(intent, 0);
            }
        });

        btnOpenCamera = (Button) findViewById(R.id.Button_OpenCamera);
        btnOpenCamera.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("FaceRec", faceRec);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        btnDetect = (Button) findViewById(R.id.Button_Detect);
        btnDetect.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (!isModelLoaded)
                {
                    Toast toast = Toast.makeText(getApplicationContext(), "Model is not loaded!", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.BOTTOM, 0, 0);
                    toast.show();
                }
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
	                    bmResult = Bitmap.createBitmap(bmImg.getWidth(), bmImg.getHeight(), bmImg.getConfig());
                        faceRec.detect(bmImg, bmResult);
                        Message msg = new Message();
                        msg.what = FACE_DETECT_SUCCESS;
                        handler.sendMessage(msg);
                    }
                }).start();

            }
        });

        btnDetect.setEnabled(false);
        btnOpenCamera.setEnabled(false);
        btnOpenFile.setEnabled(false);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_10, this, mLoaderCallback);
    }

    @Override
    protected void onPause()
    {
        super.onResume();

        btnDetect.setEnabled(false);
        btnOpenCamera.setEnabled(false);
        btnOpenFile.setEnabled(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == RESULT_OK)
        {
            switch (requestCode)
            {
                case 0:
                {
                    Uri uri = data.getData();
                    ContentResolver resolver = getContentResolver();
                    String fileType = resolver.getType(uri);

                    if (fileType.startsWith("image"))
                    {
                        try
                        {
                            InputStream is = resolver.openInputStream(uri);
                            bmImg = BitmapFactory.decodeStream(is);
                            is.close();
                        } catch (Exception e)
                        {
                            bmImg = null;
                            break;
                        }
                        imageView.setImageBitmap(bmImg);
                    }

                    break;
                }
                default:
                {
                    bmImg = null;
                    break;
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}

