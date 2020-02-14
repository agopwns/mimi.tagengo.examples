package jp.fairydevices.mimi.example.activity;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequest;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import jp.fairydevices.mimi.example.PackageManagerUtils;
import jp.fairydevices.mimi.example.PrismClient;
import jp.fairydevices.mimi.example.R;

public class TranslationOCRResultActivity extends AppCompatActivity {

    EditText textView, textViewResult;
    ImageView imageView;
    String filePath;
    private PrismClient prismClient = null;

    // Google Cloud Vision API
    private static final String CLOUD_VISION_API_KEY = "AIzaSyB84oqh3MUWK5rEWwi4N1lk2jogtSq0PsU";
    public static final String FILE_NAME = "temp.jpg";
    private static final String ANDROID_CERT_HEADER = "X-Android-Cert";
    private static final String ANDROID_PACKAGE_HEADER = "X-Android-Package";
    private static final int MAX_LABEL_RESULTS = 10;
    private static final int MAX_DIMENSION = 1200;

    private static final String TAG = TranslationOCRResultActivity.class.getSimpleName();
    private static final int GALLERY_PERMISSIONS_REQUEST = 0;
    private static final int GALLERY_IMAGE_REQUEST = 1;
    public static final int CAMERA_PERMISSIONS_REQUEST = 2;
    public static final int CAMERA_IMAGE_REQUEST = 3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translation_ocrresult);

        // 1. 파일 경로 값이 넘어오면 해당 image 를 imageView 에 표시
        Intent intent = getIntent();
        filePath = intent.getStringExtra("FilePath");
        Toast.makeText(this, "filePath : " + filePath, Toast.LENGTH_SHORT).show();

        // 파일 경로 제대로 넘어오는지 확인
        textView = findViewById(R.id.textView);
        textViewResult = findViewById(R.id.textViewResult);
        textView.setText(filePath);

        // 이미지 뷰에 넣기
        imageView = findViewById(R.id.imageView);
        Picasso.get().load("file://" + filePath).into(imageView);

        // 2. image 경로 값으로 Vision OCR api 요청
//        try {
//            getTextFromImage();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
        callCloudVision(bitmap);

        // 3. 번역된 텍스트 값 아래 textView 에 표시(임시)
//        prismClient = new PrismClient(textView, textViewResult);
//        prismClient.updateToken(); // 액세스 토큰 취득
//        prismClient.setInputLanguage("ko");
//        prismClient.setTargetLanguage("ja");
//
//        prismClient.MT();

    }



    private void callCloudVision(final Bitmap bitmap) {
        // Switch text to loading
//        mImageDetails.setText(R.string.loading_message);

        // Do the real work in an async task, because we need to use the network anyway
        try {
            AsyncTask<Object, Void, String> labelDetectionTask = new LableDetectionTask(this, prepareAnnotationRequest(bitmap));
            labelDetectionTask.execute();
        } catch (IOException e) {
            Log.d(TAG, "failed to make API request because of other IOException " +
                    e.getMessage());
        }
    }

    private Vision.Images.Annotate prepareAnnotationRequest(Bitmap bitmap) throws IOException {
        HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        VisionRequestInitializer requestInitializer =
                new VisionRequestInitializer(CLOUD_VISION_API_KEY) {
                    /**
                     * We override this so we can inject important identifying fields into the HTTP
                     * headers. This enables use of a restricted cloud platform API key.
                     */
                    @Override
                    protected void initializeVisionRequest(VisionRequest<?> visionRequest)
                            throws IOException {
                        super.initializeVisionRequest(visionRequest);

                        String packageName = getPackageName();
                        visionRequest.getRequestHeaders().set(ANDROID_PACKAGE_HEADER, packageName);

                        String sig = PackageManagerUtils.getSignature(getPackageManager(), packageName);

                        visionRequest.getRequestHeaders().set(ANDROID_CERT_HEADER, sig);
                    }
                };

        Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
        builder.setVisionRequestInitializer(requestInitializer);

        Vision vision = builder.build();

        BatchAnnotateImagesRequest batchAnnotateImagesRequest =
                new BatchAnnotateImagesRequest();
        batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
            AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

            // Add the image
            Image base64EncodedImage = new Image();
            // Convert the bitmap to a JPEG
            // Just in case it's a format that Android understands but Cloud Vision
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();

            // Base64 encode the JPEG
            base64EncodedImage.encodeContent(imageBytes);
            annotateImageRequest.setImage(base64EncodedImage);

            // add the features we want
            annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
                Feature labelDetection = new Feature();
                labelDetection.setType("DOCUMENT_TEXT_DETECTION");
                /*
                TYPE_UNSPECIFIED	Unspecified feature type.
                FACE_DETECTION	Run face detection.
                LANDMARK_DETECTION	Run landmark detection.
                LOGO_DETECTION	Run logo detection.
                LABEL_DETECTION	Run label detection.
                TEXT_DETECTION	Run text detection / optical character recognition (OCR). Text detection is optimized for areas of text within a larger image; if the image is a document, use DOCUMENT_TEXT_DETECTION instead.
                DOCUMENT_TEXT_DETECTION	Run dense text document OCR. Takes precedence when both DOCUMENT_TEXT_DETECTION and TEXT_DETECTION are present.
                        SAFE_SEARCH_DETECTION	Run Safe Search to detect potentially unsafe or undesirable content.
                IMAGE_PROPERTIES	Compute a set of image properties, such as the image's dominant colors.
                CROP_HINTS	Run crop hints.
                WEB_DETECTION	Run web detection.
                PRODUCT_SEARCH	Run Product Search.
                OBJECT_LOCALIZATION	Run localizer for object detection.*/
                labelDetection.setMaxResults(MAX_LABEL_RESULTS);
                add(labelDetection);
                Feature testDetection = new Feature();
                testDetection.setType("LABEL_DETECTION");
                testDetection.setMaxResults(MAX_LABEL_RESULTS);
                add(testDetection);
            }});

            // Add the list of one thing to the request
            add(annotateImageRequest);
        }});

        Vision.Images.Annotate annotateRequest =
                vision.images().annotate(batchAnnotateImagesRequest);
        // Due to a bug: requests to Vision API containing large images fail when GZipped.
        annotateRequest.setDisableGZipContent(true);
        Log.d(TAG, "created Cloud Vision request object, sending request");

        return annotateRequest;
    }

    private static class LableDetectionTask extends AsyncTask<Object, Void, String> {
        private final WeakReference<TranslationOCRResultActivity> mActivityWeakReference;
        private Vision.Images.Annotate mRequest;

        LableDetectionTask(TranslationOCRResultActivity activity, Vision.Images.Annotate annotate) {
            mActivityWeakReference = new WeakReference<>(activity);
            mRequest = annotate;
        }

        @Override
        protected String doInBackground(Object... params) {
            try {
                Log.d(TAG, "created Cloud Vision request object, sending request");
                BatchAnnotateImagesResponse response = mRequest.execute();
                return convertResponseToString(response);

            } catch (GoogleJsonResponseException e) {
                Log.d(TAG, "failed to make API request because " + e.getContent());
            } catch (IOException e) {
                Log.d(TAG, "failed to make API request because of other IOException " +
                        e.getMessage());
            }
            return "Cloud Vision API request failed. Check logs for details.";
        }

        protected void onPostExecute(String result) {
            TranslationOCRResultActivity activity = mActivityWeakReference.get();
            if (activity != null && !activity.isFinishing()) {
                TextView imageDetail = activity.findViewById(R.id.textView);
                imageDetail.setText(result);
            }
        }
    }

    private static String convertResponseToString(BatchAnnotateImagesResponse response) {
        StringBuilder message = new StringBuilder("I found these things:\n\n");

//  List<EntityAnnotation> labels = response.getResponses().get(0).getLabelAnnotations(); // label
        List<EntityAnnotation> labels = response.getResponses().get(0).getTextAnnotations();
        if (labels != null) {
            for (EntityAnnotation label : labels) {
                message.append(String.format(Locale.US, "%.3f: %s", label.getScore(), label.getDescription()));
                message.append("\n");
            }
        } else {
            message.append("nothing");
        }

        return message.toString();
    }

    private Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {

        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }

    public void getTextFromImage() throws IOException {

        // 이미지 리소스 -> 비트맵 변환
        Bitmap bitmap = BitmapFactory.decodeFile(filePath);

        // 이미지 정방향에 맞게 90도 회전
        bitmap = getRotatedBitmap(bitmap, 90);

        // 이미지로부터 찾은 텍스트를 넣을 공간
        TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();

        // 이미지에서 텍스트를 찾을 수 없는 경우 경고 메시지
        if(!textRecognizer.isOperational()){
            Toast.makeText(this, "이미지에서 텍스트를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
        } else {
            // 비트맵을 기준으로 프레임 생성
            Frame frame = new Frame.Builder().setBitmap(bitmap).build();

            // 찾은 값들을 StringBuilder 에 할당
            SparseArray<TextBlock> items = textRecognizer.detect(frame);
            StringBuilder sb = new StringBuilder();

            for(int i = 0; i < items.size(); i++){

                TextBlock myItem = items.valueAt(i);
                sb.append(myItem.getValue());
                sb.append("\n");
            }
            textView.setText(sb.toString()); // textView 에 찾은 결과 String 할당
            bitmap.recycle(); // 안드로이드의 경우 RAM 이 적으므로 바로바로 자원 반환할 것.
        }
    }

    // 현재 이미지가 -90도로 나오고 있어 회전이 필요함
    private Bitmap getRotatedBitmap(Bitmap bitmap, int degree) {
        if (degree != 0 && bitmap != null) {
            Matrix matrix = new Matrix();
            matrix.setRotate(degree, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);

            try {
                Bitmap tmpBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

                if (bitmap != tmpBitmap) {
                    bitmap.recycle();
                    bitmap = tmpBitmap;
                }
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }



}
