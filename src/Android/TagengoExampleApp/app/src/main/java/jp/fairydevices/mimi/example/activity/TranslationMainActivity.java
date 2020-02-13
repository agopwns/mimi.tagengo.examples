package jp.fairydevices.mimi.example.activity;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import jp.fairydevices.mimi.example.PrismClient;
import jp.fairydevices.mimi.example.R;

public class TranslationMainActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageButton srButton; // 음성 인식
    private ImageButton mtButton; // 기계 번역
    private ImageButton ssButton; // 음성 합성
    private ImageButton clearButton; // 인식 결과 초기화

    private EditText srOutput; // 인식 결과 - 번역할 문장
    private EditText mtOutput; // 번역 결과

    private boolean isRecording = false;
    private PrismClient prismClient = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translation_main);

        checkPermission(this); // 번역 메인 화면에서 한 번만 검사하면 됨
        initView(); // 레이아웃 뷰 초기화

        prismClient = new PrismClient(srOutput, mtOutput);
        prismClient.updateToken(); // 액세스 토큰 취득

        // 인식 결과 이벤트
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
        srOutput = findViewById(R.id.srOutputText);
        mtOutput = findViewById(R.id.mtOutputText);
        srButton.setOnClickListener(this);
        mtButton.setOnClickListener(this);
        ssButton.setOnClickListener(this);
        clearButton.setOnClickListener(this);
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

                // 번역 후 음성 합성 버튼 나오게

                ssButton.setVisibility(View.VISIBLE);
                break;
            case R.id.ssButton: // 음성합성
                // TODO : 합성된 음성 크기가 최소인 부분 고쳐야 함.
                prismClient.SS();
                break;
            case R.id.clearButton: // 인식 텍스트 초기화
                srOutput.setText("");
                clearButton.setVisibility(View.INVISIBLE);
                break;
            default:        }
    }


}
