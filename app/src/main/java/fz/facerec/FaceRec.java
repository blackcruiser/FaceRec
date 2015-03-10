package fz.facerec;

import android.graphics.Bitmap;
import android.os.Environment;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.objdetect.CascadeClassifier;

public class FaceRec
{
    final private String modelPath = Environment.getExternalStorageDirectory().getPath() + "/model.xml";
    private CascadeClassifier classifier;

    public FaceRec()
    {
        classifier = new CascadeClassifier(modelPath);
    }

    public Bitmap detect(Bitmap bmSrc)
    {
        Mat img = new Mat();
        Utils.bitmapToMat(bmSrc, img);
        MatOfRect object = new MatOfRect();
        classifier.detectMultiScale(img, object);

        Rect[] rects = object.toArray();
        for (int i = 0; i < rects.length; i += 2)
            Core.rectangle(img, rects[i].tl(), rects[i].br(), new Scalar(255, 0, 0));

        Bitmap bmDst = Bitmap.createBitmap(bmSrc.getWidth(), bmSrc.getHeight(), bmSrc.getConfig());
        Utils.matToBitmap(img, bmDst);

        return bmDst;
    }
}
