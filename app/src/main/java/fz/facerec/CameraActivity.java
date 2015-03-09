package fz.facerec;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.media.Image;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class CameraActivity extends Activity
{
	private Button btnBack, btnDetect;
	private SurfaceView surfaceView;
	private ImageView imageView;

	private FaceRec faceRec;
	private Camera camera;

	private class SurfaceCallback implements SurfaceHolder.Callback
	{
		@Override
		public void surfaceCreated(SurfaceHolder holder)
		{
			camera = Camera.open();
			Camera.Parameters cameraParams = camera.getParameters();
			camera.setDisplayOrientation(90);
			camera.setPreviewCallback(new PreviewCallback());
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
			camera.stopPreview();
			camera.release();
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
		{
			Camera.Parameters cameraParams = camera.getParameters();

			//consider the display orientation
			cameraParams.setPreviewSize(width, height); // 设置预览图像大小
			camera.setParameters(cameraParams);
			camera.startPreview();// 开始预览
		}
	}

	private class PreviewCallback implements Camera.PreviewCallback
	{
		@Override
		public void onPreviewFrame(byte[] data, Camera camera)
		{
			Camera.Parameters cameraParams = camera.getParameters();
			Camera.Size size = cameraParams.getPreviewSize();

			Mat mYuv = new Mat(size.height + size.height / 2, size.width, CvType.CV_8UC1);
			mYuv.put(0, 0, data);
			Mat mRGB = new Mat();
			Imgproc.cvtColor(mYuv, mRGB, Imgproc.COLOR_YUV2BGRA_NV21, 4);
			Bitmap bitmap = Bitmap.createBitmap(size.width, size.height, Bitmap.Config.ARGB_8888);
			Utils.matToBitmap(mRGB, bitmap);
			//Mat img = new Mat();
			//Utils.bitmapToMat(bitmap, img);
			imageView.setImageBitmap(bitmap);
		}
	}


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        faceRec = (FaceRec)bundle.getSerializable("FaceRec");

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
        btnDetect = (Button)findViewById(R.id.btn_Detect);
    }


}
