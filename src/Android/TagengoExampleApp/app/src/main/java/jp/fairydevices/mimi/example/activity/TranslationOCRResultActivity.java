package jp.fairydevices.mimi.example.activity;


import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import jp.fairydevices.mimi.example.PackageManagerUtils;
import jp.fairydevices.mimi.example.PrismClient;
import jp.fairydevices.mimi.example.R;
import jp.fairydevices.mimi.example.dao.BoundingPoly;
import jp.fairydevices.mimi.example.dao.Example;
import jp.fairydevices.mimi.example.dao.Vertex;

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

    private Resources resources;
    ArrayList<Example> resultList = new ArrayList<>();
    Bitmap bitmap; // imageView 배경으로 사용하기 위한 bitmap

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
//        textView.setText(filePath);

        // 이미지 뷰에 넣기
        imageView = findViewById(R.id.imageView);
        Picasso.get().load("file://" + filePath).into(imageView);

        //Convert The Image To Bitmap
        resources = getApplicationContext().getResources();
        bitmap = BitmapFactory.decodeFile(filePath);

        // 2. image 경로 값으로 Vision OCR api 요청
//        try {
//            getTextFromImage();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
        callCloudVision(bitmap);

        // 3. 박스 그리기
//        drawBoundingBox();


    }

    // 문자 가장 자리 바운딩 박스 처리
    private void drawBoundingBox(){

        //The Color of the Rectangle to Draw on top of Text
        Paint rectPaint = new Paint();
        rectPaint.setColor(Color.WHITE);
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeWidth(4.0f);

        // 캔버스 객체 작성,
        // 예를 들어 스크린샷인 이미지를 수행하는 방법 중
        // 재래틀을 그리려면 뷰 높이 및 너비 필요
        // API가 보기에서 텍스트 위치를 감지하기 때문에
        // 그러므로 Dimesnion은 해당 텍스트에서 그리기 위한 그리기 방법이 중요하다.
        // 위치
        Bitmap tempBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(tempBitmap);
        canvas.drawBitmap(bitmap, 0, 0, null);

        for(int i = 0 ; i < resultList.size(); i++){

            List<Vertex> list = resultList.get(i).getBoundingPoly().getVertices();
            for(int j = 0; j < list.size(); j++){
                Log.d(TAG, "x : " + list.get(j).getX() + ", y : " + list.get(j).getY());
                // point xy 좌표 4개 준비
                Point first;
                Point second;
                Point third;
                Point forth;

                if(j == 0)
                    first = new Point(list.get(j).getX(), list.get(j).getY());
                else if(j == 1)
                    second = new Point(list.get(j).getX(), list.get(j).getY());
                else if(j == 2)
                    third = new Point(list.get(j).getX(), list.get(j).getY());
                else
                    forth = new Point(list.get(j).getX(), list.get(j).getY());
            }

            // 사각형 그리기
//            Rect  rect = new RectF();
//            rectPaint.setColor(Color.BLACK);
//
//            //Finally Draw Rectangle/boundingBox around word
//            canvas.drawRect(rect, rectPaint);
//
//            //Set image to the `View`
//            imageView.setImageDrawable(new BitmapDrawable(resources, tempBitmap));

        }

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
                labelDetection.setType("TEXT_DETECTION");
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
//                Feature testDetection = new Feature();
//                testDetection.setType("LABEL_DETECTION");
//                testDetection.setMaxResults(MAX_LABEL_RESULTS);
//                add(testDetection);
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

    private class LableDetectionTask extends AsyncTask<Object, Void, String> {
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
                ImageView imageView = activity.findViewById(R.id.imageView);
                //imageDetail.setText(result);

                ArrayList<String> wordList = new ArrayList<>();

                String tempWord = ""; // wordList 단어와 비교하기 위한 임시 단어
                int plusCount = 0; // tempWord 를 더한 횟수
                Point first = new Point();
                Point second = new Point();
                Point third = new Point();
                Point forth = new Point();

                // 1. 인식 결과 gson 파싱
                try {

                    Gson gson = new Gson();
                    Type listType = new TypeToken<ArrayList<Example>>() {}.getType();
                    ArrayList<Example> mList = gson.fromJson(result, listType);
                    for(int i = 0; i < mList.size(); i++){
                        Log.d(TAG, "인식 결과 " + i + " : " + mList.get(i).getDescription());

                        // 가장 첫 번째 row 에는 완전한 문장, 단어가 들어있으므로 여기서 리스트 생성.
                        if(i == 0) {

                            // 첫 번째는 줄바꿈으로 구분
                            String[] firstSplit = mList.get(i).getDescription().split("\n");
                            for(int j = 0; j < firstSplit.length; j++){

                                // 공백 있을 경우를 위해 한번 더 구분
                                String[] secondSplit = firstSplit[j].split(" ");
                                if(secondSplit.length > 1){
                                    for(String item:secondSplit){
                                        wordList.add(item);
                                    }
                                } else {
                                    wordList.add(firstSplit[j]);
                                }
                            }

                        } else {
                            // 2. 파싱값을 토대로 단어, 문장 라운딩 처리를 위한 리스트 생성
                            // 좌표와 리스트를 함께 만들어준다.
                            tempWord += mList.get(i).getDescription();

                            // 문자열이 같을 경우 해당 row(i)의 좌표값(Vertex List)을 그대로 추가한다
                            if(wordList.get(0).equals(tempWord)){
                                Example example = new Example();

                                if(plusCount == 0){
                                    example.setBoundingPoly(mList.get(i).getBoundingPoly()); // 좌표 리스트 추가
                                } else {
                                    // 순서가 다른 좌표를 만들어주기 위해..
                                    // 더럽지만 나중에 고치자 ㅠ
                                     new ArrayList<>();

                                    // 좌표 리스트 생성
                                    ArrayList<Vertex> list = setVertex(first, second, third, forth);

                                    BoundingPoly poly = new BoundingPoly();
                                    poly.setVertices(list);
                                    example.setBoundingPoly(poly);
                                }

                                example.setDescription(wordList.get(0)); // 단어, 문장 추가
                                resultList.add(example); // 최종 리스트에 추가

                                wordList.remove(0);
                                tempWord = "";
                                plusCount = 0;

                            } else {

                                // 문자열이 다를 경우 해당 row(i)를 넘기고 기존 문자열에 String 값을 더한다.
                                Example example = new Example();
                                if(plusCount == 0){
                                    // 시작 row 에서 1,4번째 좌표 값.
                                    first.set(mList.get(i).getBoundingPoly().getVertices().get(0).getX()
                                            ,mList.get(i).getBoundingPoly().getVertices().get(0).getY());
                                    forth.set(mList.get(i).getBoundingPoly().getVertices().get(3).getX()
                                            ,mList.get(i).getBoundingPoly().getVertices().get(3).getY());
                                } else {
                                    // 종료 row 에서 2,3번째 좌표 값을 가져온다.
                                    second.set(mList.get(i).getBoundingPoly().getVertices().get(1).getX()
                                            ,mList.get(i).getBoundingPoly().getVertices().get(1).getY());
                                    third.set(mList.get(i).getBoundingPoly().getVertices().get(2).getX()
                                            ,mList.get(i).getBoundingPoly().getVertices().get(2).getY());
                                }
                                plusCount++;
                            }
                        }
                    }
                    // 테스트 : wordList 개수와 새로 만들어진 리스트의 크기가 같아야 함.
                    // 좌표값은 한 row 당 4개를 가져야 함.
                    for(int i = 0; i < resultList.size(); i++){

                        Log.d(TAG, "resultList 결과 : " + resultList.get(i).getDescription());

                        List<Vertex> list = resultList.get(i).getBoundingPoly().getVertices();
                        for(int j = 0; j < list.size(); j++){
                            Log.d(TAG, "x : " + list.get(j).getX() + ", y : " + list.get(j).getY());
                        }
                    }

                } catch (Exception ex){
                    Log.e(TAG, "에러 : " + ex);
                }
                // 3. 번역 하기 위한 값 넣기
                String tempStr = "";
                for(int i = 0; i < resultList.size(); i++){
                    tempStr += resultList.get(i).getDescription() + " ";
                }
                textView.setText(tempStr);
                Log.d(TAG, "최종 문자열 : " + tempStr);

                // 4. 번역된 텍스트 값 아래 textView 에 표시(임시)
                prismClient = new PrismClient(textView, textViewResult);
                prismClient.updateToken(); // 액세스 토큰 취득
                prismClient.setInputLanguage("ja");
                prismClient.setTargetLanguage("ko");
                prismClient.setDirection(true); // 정방향

                prismClient.MT();

            }
        }
    }



    private static ArrayList<Vertex> setVertex(Point first, Point second, Point third, Point forth) {

        ArrayList<Vertex> list = new ArrayList<>();

        Vertex vertex = new Vertex();
        vertex.setX(first.x);
        vertex.setY(first.y);
        list.add(vertex);

        vertex = new Vertex();
        vertex.setX(second.x);
        vertex.setY(second.y);
        list.add(vertex);

        vertex = new Vertex();
        vertex.setX(third.x);
        vertex.setY(third.y);
        list.add(vertex);

        vertex = new Vertex();
        vertex.setX(forth.x);
        vertex.setY(forth.y);
        list.add(vertex);

        return list;
    }

    private static String convertResponseToString(BatchAnnotateImagesResponse response) {
        StringBuilder message = new StringBuilder();
//        StringBuilder testSB = new StringBuilder("I found these things:\n\n");

//  List<EntityAnnotation> labels = response.getResponses().get(0).getLabelAnnotations(); // label
        List<EntityAnnotation> labels = response.getResponses().get(0).getTextAnnotations(); // text
        if (labels != null) {
//            for (EntityAnnotation label : labels) {
////                message.append(String.format(Locale.US, "%.3f: %s", label.getScore(), label.getDescription()));
////                message.append("\n");
//                message.append(label);
//                message.append("\n");
//            }
            message.append(labels);
        } else {
            message.append("nothing");
        }
        Log.d(TAG, "인식 결과 : " + message);
//        return message.toString();
        return message.toString();
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
