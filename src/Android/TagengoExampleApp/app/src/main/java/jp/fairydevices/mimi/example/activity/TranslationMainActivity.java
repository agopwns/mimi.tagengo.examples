package jp.fairydevices.mimi.example.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import jp.fairydevices.mimi.example.PrismClient;
import jp.fairydevices.mimi.example.R;

public class TranslationMainActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageButton srButton; // 음성 인식
    private ImageButton mtButton; // 기계 번역
    private ImageButton ssButton; // 음성 합성
    private ImageButton clearButton;
    private ImageButton cameraButton;
    private ImageButton conversationButton;
    private ImageButton menuButton;
    private ImageButton bookmarkButton;


    private EditText srOutput; // 인식 결과 - 번역할 문장
    private EditText mtOutput; // 번역 결과

    private TextView srTextview; // 음성 인식 텍스트

    private boolean isRecording = false;
    private PrismClient prismClient = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translation_main);

        checkPermission(this); // 번역 메인 화면에서 한 번만 검사하면 됨
        initView(); // 레이아웃 뷰 초기화

        prismClient = new PrismClient(srOutput, mtOutput, this);
        prismClient.updateToken(); // 액세스 토큰 취득
        prismClient.setInputLanguage("ko");
        prismClient.setTargetLanguage("ja");

        // 인식 결과 후에 이벤트 처리를 위한 리스너
        srOutput.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 입력되는 텍스트에 변화가 있을 때
                if(srOutput.getText().length() == 0){
                    // x 이미지 버튼 invisible
                    clearButton.setVisibility(View.INVISIBLE);
                }
            }
            @Override
            public void afterTextChanged(Editable arg0) {
                // 입력이 끝났을 때
                // x 이미지 버튼 visible
                clearButton.setVisibility(View.VISIBLE);
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // 입력하기 전에
            }
        });
    }

    private void initView() {
        srButton = findViewById(R.id.srButton);
        mtButton = findViewById(R.id.mtButton);
        ssButton = findViewById(R.id.ssButton);
        clearButton = findViewById(R.id.clearButton);
        conversationButton = findViewById(R.id.conversationButton);
        cameraButton = findViewById(R.id.cameraButton);
        menuButton = findViewById(R.id.menuButton);
        bookmarkButton = findViewById(R.id.bookmarkButton);

        srOutput = findViewById(R.id.srOutputText);
        mtOutput = findViewById(R.id.mtOutputText);
        srTextview = findViewById(R.id.srTextview);

        srButton.setOnClickListener(this);
        mtButton.setOnClickListener(this);
        ssButton.setOnClickListener(this);
        cameraButton.setOnClickListener(this);
        clearButton.setOnClickListener(this);
        conversationButton.setOnClickListener(this);
        menuButton.setOnClickListener(this);
        bookmarkButton.setOnClickListener(this);
    }

    private void checkPermission(Activity activity) {
        String[] permissions = {Manifest.permission.RECORD_AUDIO, Manifest.permission.INTERNET};
        for (String p : permissions) {
            int permissionCheck = ContextCompat.checkSelfPermission(activity, p);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                // Android 6.0 만 해당 퍼미션이 허용되지 않는 경우
                ActivityCompat.requestPermissions(activity, new String[]{p}, 1);
            } else {
                // 허가된 경우, 혹은 Android 6.0이전
                // 퍼미션 처리
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.srButton: // 음성인식
                if (isRecording) {
                    // 녹음(인식)중
                    prismClient.SRInputEnd(); // 음성 인식 종료
                    isRecording = false;
                    srTextview.setText("음성");
                } else {
                    // 대기중
                    prismClient.SRInputStart(true);
                    isRecording = true;
                    // TODO : 나중에 음성 인식 종료 시점 파악이 되면 자동으로 없어지게
                    Toast.makeText(getApplicationContext(),"음성 인식중..",Toast.LENGTH_SHORT).show();
                    //Snackbar.make(view,"음성 인식중...",Snackbar.LENGTH_LONG).show();
                    srTextview.setText("인식중..");
                }
                break;
            case R.id.mtButton: // 기계번역
                prismClient.setDirection(true);
                prismClient.MT();
                // 번역 후 음성 합성 버튼 나오게
                ssButton.setVisibility(View.VISIBLE);
                break;
            case R.id.ssButton: // 음성합성
                prismClient.setInputLanguage("ja");
                prismClient.SS();
                break;
            case R.id.clearButton: // 인식 텍스트 초기화
                srOutput.setText("");
                clearButton.setVisibility(View.INVISIBLE);
                break;
            case R.id.conversationButton: // 대화 번역 액티비티로 이동
                Intent intent = new Intent(this, TranslationConversationActivity.class);
                startActivity(intent);
                break;
            case R.id.cameraButton: // 카메라 번역 액티비티로 이동
                Intent cameraIntent = new Intent(this, TranslationCameraActivity.class);
                startActivity(cameraIntent);
                break;
            case R.id.menuButton: // 번역 기록 리스트 이동
                Intent historyIntent = new Intent(this, TranslationHistoryActivity.class);
                startActivity(historyIntent);
                break;
            case R.id.bookmarkButton: // 북마크
                Intent bookmarkIntent = new Intent(this, TranslationBookmarkActivity.class);
                startActivity(bookmarkIntent);
                break;
            default:
        }
    }


}
