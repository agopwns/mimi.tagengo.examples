package jp.fairydevices.mimi.example.activity;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import jp.fairydevices.mimi.example.PrismClient;
import jp.fairydevices.mimi.example.R;

public class TranslationMainActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageButton srButton;
    private ImageButton mtButton;
    private EditText srOutput;
    private EditText mtOutput;

    private boolean isRecording = false;
    private PrismClient prismClient = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translation_main);

        checkPermission(this); // 번역 메인 화면에서 딱 한번만 검사하면 됨
        initView(); // 레이아웃 뷰 초기화

        prismClient = new PrismClient(srOutput, mtOutput);
        prismClient.updateToken(); // 액세스 토큰 취득
    }

    private void initView() {
        srButton = findViewById(R.id.srButton);
        mtButton = findViewById(R.id.mtButton);
//        Button ssButton = findViewById(R.id.ssButton);
        srOutput = findViewById(R.id.srOutputText);
        mtOutput = findViewById(R.id.mtOutputText);
        srButton.setOnClickListener(this);
        mtButton.setOnClickListener(this);
//        ssButton.setOnClickListener(this);
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
                    prismClient.SRInputEnd();
                    isRecording = false;
//                    srButton.setText(R.string.sr_button_off);
                } else {
                    // 대기중
                    prismClient.SRInputStart();
                    isRecording = true;
//                    srButton.setText(R.string.sr_button_on);
                }
                break;
            case R.id.mtButton: // 기계번역
                prismClient.MT();
                break;
//            case R.id.ssButton: // 음성합성
//                prismClient.SS();
//                break;
            default:
        }
    }
}
