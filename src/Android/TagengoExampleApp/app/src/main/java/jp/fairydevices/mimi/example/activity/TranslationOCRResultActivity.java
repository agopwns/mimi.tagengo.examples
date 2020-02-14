package jp.fairydevices.mimi.example.activity;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.SparseArray;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.squareup.picasso.Picasso;

import java.io.IOException;

import jp.fairydevices.mimi.example.PrismClient;
import jp.fairydevices.mimi.example.R;

public class TranslationOCRResultActivity extends AppCompatActivity {

    EditText textView, textViewResult;
    ImageView imageView;
    String filePath;
    private PrismClient prismClient = null;


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
        try {
            getTextFromImage();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 3. 번역된 텍스트 값 아래 textView 에 표시(임시)
//        prismClient = new PrismClient(textView, textViewResult);
//        prismClient.updateToken(); // 액세스 토큰 취득
//        prismClient.setInputLanguage("ko");
//        prismClient.setTargetLanguage("ja");
//
//        prismClient.MT();

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
