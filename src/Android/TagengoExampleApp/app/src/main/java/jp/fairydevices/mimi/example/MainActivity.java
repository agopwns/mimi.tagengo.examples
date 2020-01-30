package jp.fairydevices.mimi.example;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button srButton;
    private EditText srOutput;
    private EditText mtOutput;

    private boolean isRecording = false;
    private PrismClient prismClient = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermission(this);
        initView();

        prismClient = new PrismClient(srOutput, mtOutput);
        prismClient.updateToken(); // 액세스 토큰 취득
    }

    private void initView() {
        srButton = findViewById(R.id.srButton);
        Button mtButton = findViewById(R.id.mtButton);
        Button ssButton = findViewById(R.id.ssButton);
        srOutput = findViewById(R.id.srOutputText);
        mtOutput = findViewById(R.id.mtOutputText);
        srButton.setOnClickListener(this);
        mtButton.setOnClickListener(this);
        ssButton.setOnClickListener(this);
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
            case R.id.srButton:
                if (isRecording) {
                    // 녹음(인식)중
                    prismClient.SRInputEnd();
                    isRecording = false;
                    srButton.setText(R.string.sr_button_off);
                } else {
                    // 대기중
                    prismClient.SRInputStart();
                    isRecording = true;
                    srButton.setText(R.string.sr_button_on);
                }
                break;
            case R.id.mtButton:
                prismClient.MT();
                break;
            case R.id.ssButton:
                prismClient.SS();
                break;
            default:
        }
    }
}
