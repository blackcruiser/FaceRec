package fz.facerec;

import android.app.Activity;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.Button;

import java.io.IOException;

public class CameraActivity extends Activity
{
	private Button btnBack, btnDetect;
	private SurfaceView surfaceView;

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
			cameraParams.setPreviewSize(480, 640); // 设置预览图像大小
			cameraParams.setPreviewFormat(ImageFormat.JPEG);
			camera.setParameters(cameraParams);// 设置相机参数
			camera.startPreview();// 开始预览
		}
	}

	private class PreviewCallback implements Camera.PreviewCallback
	{
		@Override
		public void onPreviewFrame(byte[] data, Camera camera)
		{

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
