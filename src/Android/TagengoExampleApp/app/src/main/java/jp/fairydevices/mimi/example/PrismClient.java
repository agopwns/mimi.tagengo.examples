package jp.fairydevices.mimi.example;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import ai.fd.mimi.prism.ClientComCtrl;
import ai.fd.mimi.prism.ResponseData;
import jp.fairydevices.mimi.example.db.HistoryDB;
import jp.fairydevices.mimi.example.db.HistoryDao;
import jp.fairydevices.mimi.example.model.TranslationHistory;

import static android.media.AudioTrack.MODE_STREAM;

public class PrismClient {

    // 발급 받은 ID, SECRET KEY 아래에 입력
    private static final String ID = "35acb9e68bc94ce7bd4f14574e715348";
    private static final String SECRET = "f0b0935deccf4740a1941d59352fd01570ea7411230ba48f5bf643be03f0e98d13f390189100e1a53eea8acc0c43eb999e5356612f66a68f15455d4e4aed07bd2ede8ac285b5308e34f1ff06d67c023c40836e664016f0085f8560e44e8ae3f59a1288c81e9001e941c4748e2336bb9adab8bc7286753f927714f2aa9c760fc48a99b6d614b89d726335d9f617b4a93b169ac0d2dc93331e6cf08e07a885feedee062573c96c3d559c04e31ec18ecd261612c25600b2883ed51e96a8c542ad4d30e76d0141c5755798ad9357d68b4cd14088b96b88495499a1b4feb58beec83f8e863ae76084ca3979636580da81d98e73ab15e4cbdf0290f2e8d7d479990b2f";
    private static final String SRURL = "https://sandbox-sr.mimi.fd.ai"; // 음성인식 END POINT
    private static final String SSURL = "https://sandbox-ss.mimi.fd.ai/speech_synthesis"; // 음성합성 END POINT
    private static final String MTURL = "https://sandbox-mt.mimi.fd.ai/machine_translation"; // 기계번역 END POINT

    private String accessToken = "";
    private ClientComCtrl client = null;
    private ExecutorService pool;

    private SRRecordingTask srRecordingTask = null;
    private AudioTrack ssPlayer;

    private EditText srOutputView;
    private EditText mtOutputView;

    private LinkedBlockingQueue<byte[]> recQueue = new LinkedBlockingQueue<>();
    private Recorder recorder = new Recorder(recQueue);

    // 입력, 출력 언어
    private String inputLanguage;
    private String targetLanguage;
    private boolean isDirection;

    // 기계 번역(MT)시 번역 기록을 저장하기 위한 HistoryDao
    private HistoryDao dao;
    String sr = ""; // 인식결과 db 저장을 위한 전역 변수


    public PrismClient(View srOutputView, View mtOutputView, Context context) {
        this.srOutputView = (EditText) srOutputView;
        this.mtOutputView = (EditText) mtOutputView;

        dao = HistoryDB.getInstance(context).historyDao();
        pool = Executors.newSingleThreadExecutor();

        int bufferSize = AudioTrack.getMinBufferSize(16000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            ssPlayer = new AudioTrack.Builder()
                    .setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA) // USAGE_VOICE_COMMUNICATION default
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build())
                    .setAudioFormat(new AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .setSampleRate(16000)
                            .build())
                    .setBufferSizeInBytes(8192)
                    .build();
        } else {
            ssPlayer = new AudioTrack(
                    AudioManager.STREAM_MUSIC
                    , 16000
                    , AudioFormat.CHANNEL_CONFIGURATION_MONO
                    , AudioFormat.ENCODING_PCM_16BIT
                    , bufferSize
                    , MODE_STREAM);
        }
        ssPlayer.setVolume(50);
        ssPlayer.play();
    }

    public void updateToken() {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                try {
                    accessToken = new AccessToken().getToken(ID, SECRET);
                    Log.d(getClass().getName(), "accessToken: " + accessToken);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        pool.execute(task);
    }

    public void SRInputStart(boolean isDirection) {
        client = new ClientComCtrl(accessToken);
        recQueue = new LinkedBlockingQueue<>();
        recorder = new Recorder(recQueue);
        recorder.startRecording();

        client.setTransferEncodingChunked(true); // 분할전송
        this.isDirection = isDirection; // 번역 정방향, 역방향
        srRecordingTask = new SRRecordingTask();

        pool.execute(srRecordingTask);
    }

    public void SRInputEnd() {
        if (srRecordingTask != null) {
            recorder.stopRecording();
            srRecordingTask.stopRecording();
            if (client.isTransferEncodingChunked()) {
                // recQueue.take() 가 block 하는 경우가 있으므로, 더미 음성 데이터로 block 을 해제한다.
                recQueue.add(new byte[0]);
            }
        }
        Log.d(getClass().getName(), "SEONGJUN SRInputEnd()");
    }

    // 기계 번역
    public void MT() {
        Log.d(getClass().getName(), "SEONGJUN MT() start");


        Runnable task = new Runnable() {
            @Override
            public void run() {
                try {
                    // 기계번역 요청
                    // API 요청 및 언어 변경시 해당 부분만 변경하면 됨. 나머지는 건드릴 부분 없음.
                    String text = "";
                    if(isDirection){
                        text = srOutputView.getText().toString(); // 번역할 내용
                    } else {
                        text = mtOutputView.getText().toString(); // 번역할 내용
                    }
                    Log.d(getClass().getName(), "SEONGJUN MT() text : " + text);
                    sr = text;

                    String MTRequestTemplate = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                            "<STML UtteranceID=\"0\" Version=\"1.0\">\n" +
                            "<User ID=\"N/A\"/>\n" +
                            "<MT_IN SourceLanguage=\"%s\" TargetLanguage=\"%s\">\n" +
                            "<InputTextFormat Form=\"SurfaceForm\"/>\n" +
                            "<OutputTextFormat Form=\"SurfaceForm\"/>\n" +
                            "<s>%s</s>\n" +
                            "</MT_IN>\n" +
                            "</STML>\n";
                    String requestXML = String.format(MTRequestTemplate, inputLanguage, targetLanguage, text);
                    client = new ClientComCtrl(accessToken);
                    ResponseData response = client.request(MTURL, requestXML);

                    // 번역한 결과를 view 에 표시
                    Log.d(getClass().getName(), "SEONGJUN MT result: " + response.getXML());

                    XMLSimpleParser parser = new XMLSimpleParser();
                    final String mtResult = parser.getMT_OUTSentence(response.getXML());
                    if(isDirection){
                        mtOutputView.post(new Runnable() {
                            @Override
                            public void run() {
                                mtOutputView.setText(mtResult);

                                // 인식결과 및 번역결과 DB에 저장
                                TranslationHistory th = new TranslationHistory(sr, mtResult);
                                dao.insertHistory(th);

                                Log.d(getClass().getName(), "SEONGJUN MT mtOutputView: " + mtResult);
                            }
                        });
                    } else {
                        srOutputView.post(new Runnable() {
                            @Override
                            public void run() {
                                srOutputView.setText(mtResult);

                                // 인식결과 및 번역결과 DB에 저장
                                TranslationHistory th = new TranslationHistory(sr, mtResult);
                                dao.insertHistory(th);

                                Log.d(getClass().getName(), "SEONGJUN MT srOutputView: " + mtResult);
                            }
                        });
                    }

                } catch (SAXException | XPathExpressionException | ParserConfigurationException | IOException e) {
                    e.printStackTrace();
                }
            }
        };
        pool.submit(task);
    }

    // 음성합성
    public void SS() {

        Runnable task = new Runnable() {
            @Override
            public void run() {
                try {
                    // 음성합성 API 호출
                    //  ja (일본어) en (영어) es (스페인어) fr (프랑스어), id (인도네시아어), ko (한국어) my (미얀마어), th (태국어), vi (베트남어 ) 및 zh (중국어)
                    String text = mtOutputView.getText().toString();
                    String SSRequestTemplate = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                            "<STML UtteranceID=\"0\" Version=\"1\">\n" +
                            "<User ID=\"N/A\"/>\n" +
                            "<SS_IN Language=\"%s\">\n" +
                            "<Voice Age=\"30\" Gender=\"%s\"/>\n" +
                            "<OutputAudioFormat Audio=\"RAW\" Endian=\"Little\" SamplingFrequency=\"16k\"/>\n" +
                            "<InputTextFormat Form=\"SurfaceForm\"/>\n" +
                            "<s Delimiter=\" \">%s</s>\n" +
                            "</SS_IN>\n" +
                            "</STML>";
                    String requestXML = String.format(SSRequestTemplate, inputLanguage, "Male", text);
                    client = new ClientComCtrl(accessToken);
                    ResponseData response = client.request(SSURL, requestXML);

                    // 결과를 재생
                    // 현재 음성 합성 소리가 너무 작게 나오는 문제가 있음
                    Log.d(getClass().getName(), "SS result: " + response.getXML());
                    ssPlayer.setVolume(15);
                    ssPlayer.write(response.getBinary(), 0, response.getBinary().length);
                } catch (SAXException | IOException e) {
                    e.printStackTrace();
                }
            }
        };
        pool.submit(task);
    }

    // 음성인식
    public class SRRecordingTask implements Runnable {
        private volatile boolean taskDone = false;

        @Override
        public void run() {

            if (!client.isTransferEncodingChunked()) {
                return;
            }
            //  ja (일본어) en (영어) es (스페인어) fr (프랑스어), id (인도네시아어), ko (한국어) my (미얀마어), th (태국어), vi (베트남어 ) 및 zh (중국어)
            String SRRequestTemplate = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<STML UtteranceID=\"0\" Version=\"1\">\n" +
                    "<User ID=\"N/A\"/>\n" +
                    "<SR_IN Language=\"%s\">\n" +
                    "<Voice/>\n" +
                    "<InputAudioFormat Audio=\"RAW\" Endian=\"Little\" SamplingFrequency=\"16k\"/>\n" +
                    "<OutputTextFormat Form=\"SurfaceForm\"/>\n" +
                    "</SR_IN>\n" +
                    "</STML>";
            String requestXML = String.format(SRRequestTemplate, inputLanguage);
            ResponseData response;
            try {
                client.request(SRURL, requestXML);
                // 녹음 종료까지 녹음 Queue 에서 계속 꺼내기
                while (!taskDone) {
                    try {
                        byte[] data = recQueue.take(); // blocking
                        client.request(SRURL, data);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                // 여기까지 음성인식 작업이 완료됨
                response = client.request(SRURL);

                Log.d(getClass().getName(), "SR result: " + response.getXML());
                // 응답 결과를 view 에 표시
                XMLSimpleParser parser = new XMLSimpleParser();
                final String srResult = parser.getSR_OUTSentence(response.getXML());
                if(isDirection){
                    srOutputView.post(new Runnable() {
                        @Override
                        public void run() {
                            srOutputView.setText(srResult);
                            MT();
                            Log.d(getClass().getName(), "SEONGJUN SRRecordingTask : ");
                        }
                    });
                } else {
                    mtOutputView.post(new Runnable() {
                        @Override
                        public void run() {
                            mtOutputView.setText(srResult);
                            MT();
                            Log.d(getClass().getName(), "SEONGJUN SRRecordingTask : ");
                        }
                    });
                }

            } catch (SAXException | XPathExpressionException | ParserConfigurationException | IOException e) {
                e.printStackTrace();
            }
        }

        public void stopRecording() {
            taskDone = true;
        }
    }

    public void setInputLanguage(String inputLanguage) {
        this.inputLanguage = inputLanguage;
    }

    public void setTargetLanguage(String targetLanguage) {
        this.targetLanguage = targetLanguage;
    }

    public boolean isDirection() {
        return isDirection;
    }

    public void setDirection(boolean direction) {
        this.isDirection = direction;
    }
}
