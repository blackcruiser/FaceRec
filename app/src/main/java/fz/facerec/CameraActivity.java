package fz.facerec;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.SurfaceView;
import android.widget.Button;

import java.io.IOException;

public class CameraActivity extends Activity
{
    private Button btnBack, btnDetect;
    private SurfaceView surfaceView;

    private FaceRec faceRec;
    private Camera camera;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        faceRec = (FaceRec)bundle.getSerializable("FaceRec");

        setContentView(R.layout.camera_layout);

        surfaceView = (SurfaceView)findViewById(R.id.surfaceView);
        btnBack = (Button)findViewById(R.id.btn_Back);
        btnDetect = (Button)findViewById(R.id.btn_Detect);

        camera = Camera.open();
        try
        {
            camera.setPreviewDisplay(surfaceView.getHolder());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        camera.startPreview();
    }
}
