package fz.facerec;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.lang.ref.WeakReference;

public class CameraActivity extends Activity
{
	final static int FACE_DETECT_SUCCESS = 0;

	private Button btnBack, btnDetect;
	private SurfaceView surfaceView;
	private ImageView imageView;

	private FaceRec faceRec;
	private Camera camera;

	private Mat mYuv, mRGB;
	private Bitmap bmSrc, bmDst;

	private class SurfaceCallback implements SurfaceHolder.Callback
	{
		@Override
		public void surfaceCreated(SurfaceHolder holder)
		{
			camera = Camera.open();
			Camera.Parameters cameraParams = camera.getParameters();
			camera.setDisplayOrientation(90);
			camera.setOneShotPreviewCallback(new PreviewCallback());
			cameraParams.setPreviewFormat(ImageFormat.NV21);

			camera.setParameters(cameraParams);// 设置相机参数

			try
			{
				camera.setPreviewDisplay(holder);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder)
		{
			camera.setOneShotPreviewCallback(null);
			camera.stopPreview();
			camera.release();
            camera = null;
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
		{
			Camera.Parameters cameraParams = camera.getParameters();
			//consider the display orientation
			cameraParams.setPreviewSize(height, width); // 设置预览图像大小
			camera.setParameters(cameraParams);

			Camera.Size s = cameraParams.getPreviewSize();
			mYuv = new Mat(s.height + s.height / 2, s.width, CvType.CV_8UC1);
			mRGB = new Mat();
			bmSrc = Bitmap.createBitmap(s.width, s.height, Bitmap.Config.ARGB_8888);
			bmDst = Bitmap.createBitmap(bmSrc);

			camera.startPreview();// 开始预览
		}
	}

	static class MyHandler extends Handler {
		WeakReference<CameraActivity> mActivity;

		MyHandler(CameraActivity activity) {
			mActivity = new WeakReference<CameraActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			CameraActivity theActivity = mActivity.get();
			switch (msg.what)
			{
				case CameraActivity.FACE_DETECT_SUCCESS:
					theActivity.imageView.setImageBitmap(theActivity.bmDst);
					break;
				default:
					break;
			}
		}
	}
	MyHandler handler = new MyHandler(this);

	//Anonymous class can not access local varaible of parent class
	private class FaceRecRunnable implements Runnable
	{
		@Override
		public void run()
		{
			Imgproc.cvtColor(mYuv, mRGB, Imgproc.COLOR_YUV2RGBA_NV21, 4);
			Utils.matToBitmap(mRGB, bmSrc);
			bmDst = faceRec.detect(bmSrc);

			Message msg = new Message();
			msg.what = FACE_DETECT_SUCCESS;
			handler.sendMessage(msg);

            if (camera != null)
			    camera.setOneShotPreviewCallback(new PreviewCallback());
		}
	}

	private class PreviewCallback implements Camera.PreviewCallback
	{
		@Override
		public void onPreviewFrame(byte[] data, Camera camera)
		{
			mYuv.put(0, 0, data);
			new Thread(new FaceRecRunnable()).start();
		}
	}

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this)
    {

        @Override
        public void onManagerConnected(int status)
        {
            switch (status)
            {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Toast toast = Toast.makeText(getBaseContext(), "Load OpenCV success!", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.BOTTOM, 0, 0);
                    toast.show();

                    faceRec = new FaceRec();
                }
                break;
                default:
                {
                    Toast toast = Toast.makeText(getBaseContext(), "Fail to load OpenCV library!", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();

                    finish();
                }
                break;
            }
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.camera_layout);

	    imageView = (ImageView)findViewById(R.id.imageView);

        surfaceView = (SurfaceView)findViewById(R.id.surfaceView);
	    SurfaceHolder holder = surfaceView.getHolder();
	    //deprecated setting, but required on Android versions prior to 3.0
	    holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	    holder.addCallback(new SurfaceCallback());

	    ViewGroup.LayoutParams layoutParams = surfaceView.getLayoutParams();
	    layoutParams.width = 320;
	    layoutParams.height = 240;
	    surfaceView.setLayoutParams(layoutParams);

        btnBack = (Button)findViewById(R.id.btn_Back);
	    btnBack.setOnClickListener(new View.OnClickListener()
	    {
	        @Override
	        public void onClick(View view)
	        {
		        finish();
	        }
        });

        btnDetect = (Button)findViewById(R.id.btn_Detect);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_10, this, mLoaderCallback);
    }
}
