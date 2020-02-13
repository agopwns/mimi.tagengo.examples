package jp.fairydevices.mimi.example.activity;


import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import jp.fairydevices.mimi.example.PrismClient;
import jp.fairydevices.mimi.example.R;

public class TranslationConversationActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageButton srBeforeButton; // 음성 인식
    private ImageButton srAfterButton; // 음성 인식
    private ImageButton directionButton; // 번역 문자 방향 버튼

    private EditText srOutput; // 인식 결과 - 번역할 문장
    private EditText mtOutput; // 번역 결과

    private boolean isRecording = false;
    private boolean isTransResultDirection = true;
    private PrismClient prismClient = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translation_conversation);

        checkPermission(this); // 번역 메인 화면에서 한 번만 검사하면 됨
        initView(); // 레이아웃 뷰 초기화

        prismClient = new PrismClient(srOutput, mtOutput);
        prismClient.updateToken(); // 액세스 토큰 취득
    }

    private void initView() {
        srBeforeButton = findViewById(R.id.srBeforeButton);
        srAfterButton = findViewById(R.id.srAfterButton);
        directionButton = findViewById(R.id.directionButton);
        srOutput = findViewById(R.id.srOutputText);
        mtOutput = findViewById(R.id.mtOutputText);
        directionButton.setOnClickListener(this);
        srBeforeButton.setOnClickListener(this);
        srAfterButton.setOnClickListener(this);
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
            case R.id.srBeforeButton: // 내 쪽에서  번역
                if (isRecording) {
                    // 녹음(인식)중
                    prismClient.SRInputEnd();
                    isRecording = false;
                } else {
                    // 대기중
                    directionButton.setVisibility(View.VISIBLE);
                    prismClient.setInputLanguage("ko");
                    prismClient.setTargetLanguage("ja");
                    prismClient.SRInputStart(true);
                    isRecording = true;
                    Toast.makeText(getApplicationContext(),"음성 인식중..",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.srAfterButton: // 상대방 쪽에서 번역
                if (isRecording) {
                    // 녹음(인식)중
                    prismClient.SRInputEnd();
                    isRecording = false;
                } else {
                    // 대기중
                    directionButton.setVisibility(View.VISIBLE);
                    prismClient.setInputLanguage("ja");
                    prismClient.setTargetLanguage("ko");
                    prismClient.SRInputStart(false);
                    isRecording = true;
                    Toast.makeText(getApplicationContext(),"음성 인식중..",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.ssButton: // 음성합성
                prismClient.SS();
                break;
            case R.id.directionButton: // 음성합성
                if(mtOutput.getRotationX() == 0)
                    mtOutput.setRotationX(180);
                else
                    mtOutput.setRotationX(0);

                Log.d(getClass().getName(), "SEONGJUN directionButton");
                break;
            default:
        }
    }
}
