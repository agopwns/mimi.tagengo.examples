# 안드로이드판 샘플 앱 빌드 실행 순서

## API 예제 실행 방법
1. https://github.com/agopwns/mimi.tagengo.examples git download
2. mimi.tagengo.examples/src/Android/TagengoExampleApp 안드로이드 스튜디오에서 해당 디렉토리 import
3. run

## API 요청
app/java/jp.fairydevices.mimi/example/PrismClient 경로 이동

  1. MT - 기계번역
  ~~~
  String inputLanguage = "ko"; // 번역하는 언어
  String targetLanguage = "ja"; // 번역되는 언어
  String text = srOutputView.getText().toString(); // 번역할 내용
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
  ~~~
  2. SS - 음성합성
  ~~~
  String inputLanguage = "ja"; 
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
  ~~~                  
  3. SRRecordingTask - 음성인식
  ~~~
  String inputLanguage = "ko"; 
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

      // 중략 ...　자세한 사항은 예제 코드 참고

      } catch (SAXException | XPathExpressionException | ParserConfigurationException | IOException e) {
                e.printStackTrace();
      }
  ~~~

## 라이센스
`libmimiio-android-19-armeabi-v7a-libc++.tar.gz` 에 포함된 `libmimiio_jni.so` 는 이하의 소프트웨어를 포함하고 있습니다.

- libmimiio (Dec 12, 2018)
- OpenSSL (1.0.2q)
- Poco (1.9.0)
- FLAC （1.3.2）

(라이센스 추가시 작성 요망)
