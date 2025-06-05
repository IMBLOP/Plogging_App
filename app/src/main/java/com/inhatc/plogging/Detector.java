package com.inhatc.plogging;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.SystemClock;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Detector {
    private final Context context;
    private final String modelPath;
    private final String labelPath;
    private final DetectorListener listener;

    private Interpreter interpreter;
    private List<String> labels;

    private int tensorWidth, tensorHeight, numChannel, numElements;

    private static final float INPUT_MEAN = 0f;
    private static final float INPUT_STD = 255f;
    private static final DataType INPUT_TYPE = DataType.FLOAT32;
    private static final float CONFIDENCE_THRESHOLD = 0.2f;
    private static final float IOU_THRESHOLD = 0.45f;

    private final ImageProcessor imageProcessor = new ImageProcessor.Builder()
            .add(new NormalizeOp(INPUT_MEAN, INPUT_STD))
            .build();

    public Detector(Context context, String modelPath, String labelPath, DetectorListener listener) {
        this.context = context;
        this.modelPath = modelPath;
        this.labelPath = labelPath;
        this.listener = listener;
    }

    public void setup() throws IOException {
        MappedByteBuffer modelBuffer = FileUtil.loadMappedFile(context, modelPath);
        interpreter = new Interpreter(modelBuffer, new Interpreter.Options());

        int[] inputShape = interpreter.getInputTensor(0).shape();
        int[] outputShape = interpreter.getOutputTensor(0).shape();

        tensorWidth = inputShape[1];
        tensorHeight = inputShape[2];
        numChannel = outputShape[1];
        numElements = outputShape[2];

        labels = new ArrayList<>();
        InputStream inputStream = context.getAssets().open(labelPath);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            labels.add(line);
        }
        reader.close();
        inputStream.close();
    }

    public void detect(Bitmap frame) {
        if (interpreter == null) return;

        long startTime = SystemClock.uptimeMillis();

        Bitmap resized = Bitmap.createScaledBitmap(frame, tensorWidth, tensorHeight, false);
        TensorImage tensorImage = new TensorImage(INPUT_TYPE);
        tensorImage.load(resized);
        tensorImage = imageProcessor.process(tensorImage);

        TensorBuffer outputBuffer = TensorBuffer.createFixedSize(new int[]{1, numChannel, numElements}, INPUT_TYPE);
        interpreter.run(tensorImage.getBuffer(), outputBuffer.getBuffer());

        List<BoundingBox> boxes = processOutput(outputBuffer.getFloatArray());

        long elapsed = SystemClock.uptimeMillis() - startTime;

        if (boxes == null || boxes.isEmpty()) {
            listener.onEmptyDetect();
        } else {
            listener.onDetect(boxes, elapsed);
        }
    }

    private List<BoundingBox> processOutput(float[] array) {
        List<BoundingBox> boxes = new ArrayList<>();

        for (int c = 0; c < numElements; c++) {
            float maxConf = -1f;
            int maxIdx = -1;
            for (int j = 4; j < numChannel; j++) {
                int idx = c + numElements * j;
                if (array[idx] > maxConf) {
                    maxConf = array[idx];
                    maxIdx = j - 4;
                }
            }

            if (maxConf > CONFIDENCE_THRESHOLD) {
                float cx = array[c];
                float cy = array[c + numElements];
                float w = array[c + numElements * 2];
                float h = array[c + numElements * 3];
                float x1 = cx - (w / 2f);
                float y1 = cy - (h / 2f);
                float x2 = cx + (w / 2f);
                float y2 = cy + (h / 2f);

                if (x1 < 0 || y1 < 0 || x2 > 1 || y2 > 1) continue;

                boxes.add(new BoundingBox(x1, y1, x2, y2, cx, cy, w, h, maxConf, maxIdx, labels.get(maxIdx)));
            }
        }

        return nms(boxes);
    }

    private List<BoundingBox> nms(List<BoundingBox> boxes) {
        Collections.sort(boxes, (a, b) -> Float.compare(b.getCnf(), a.getCnf()));
        List<BoundingBox> selected = new ArrayList<>();

        for (BoundingBox box : boxes) {
            boolean shouldSelect = true;
            for (BoundingBox selectedBox : selected) {
                if (iou(box, selectedBox) > IOU_THRESHOLD) {
                    shouldSelect = false;
                    break;
                }
            }
            if (shouldSelect) selected.add(box);
        }

        return selected;
    }

    private float iou(BoundingBox a, BoundingBox b) {
        float x1 = Math.max(a.getX1(), b.getX1());
        float y1 = Math.max(a.getY1(), b.getY1());
        float x2 = Math.min(a.getX2(), b.getX2());
        float y2 = Math.min(a.getY2(), b.getY2());

        float interArea = Math.max(0, x2 - x1) * Math.max(0, y2 - y1);
        float unionArea = a.getW() * a.getH() + b.getW() * b.getH() - interArea;
        return interArea / unionArea;
    }

    public void clear() {
        if (interpreter != null) {
            interpreter.close();
            interpreter = null;
        }
    }

    public interface DetectorListener {
        void onEmptyDetect();
        void onDetect(List<BoundingBox> boxes, long inferenceTime);
    }
}
