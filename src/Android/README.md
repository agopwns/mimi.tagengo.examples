# Nova Trip
일본 총무성이 주최한 ‘다국어 번역 대회’ 최종 진출작으로 사용자의 상황을 고려하여 AI 챗봇이 맞춤형 추천과 통번역, 길찾기 기능을 제공하는 여행 어플입니다.   
  
기본 기획 및 설계는 팀원들과 함께 진행하였으며 대회측에서 제공하는 다국어 번역 API를 활용하여 통번역 기능을 개발하였습니다.  
  
작품의 상세 작동 영상은 [여기](https://youtu.be/pjqU5UYs7FQ)에서 보실 수 있습니다.     
    
## 스크린샷
<div style="float:left;">
  <img src="https://github.com/agopwns/mimi.tagengo.examples/blob/master/src/Android/TagengoExampleApp/images/1.jpg" alt="Your image title" width="290"/> 
  <img src="https://github.com/agopwns/mimi.tagengo.examples/blob/master/src/Android/TagengoExampleApp/images/2.jpg" alt="Your image title" width="290"/>
  <img src="https://github.com/agopwns/mimi.tagengo.examples/blob/master/src/Android/TagengoExampleApp/images/3.jpg" alt="Your image title" width="290"/>
  <img src="https://github.com/agopwns/mimi.tagengo.examples/blob/master/src/Android/TagengoExampleApp/images/4.jpg" alt="Your image title" width="290"/>
  <img src="https://github.com/agopwns/mimi.tagengo.examples/blob/master/src/Android/TagengoExampleApp/images/5.jpg" alt="Your image title" width="290"/>
</div>

## 기능


## 사용 기술


# 안드로이드판 샘플 앱 빌드 실행 순서
주의 : 현재는 대회가 종료되어 API를 사용하려면 유료 신청을 해서 사용해야 하므로 실행에는 어려움이 있습니다.

## API 예제 실행 방법
1. https://github.com/agopwns/mimi.tagengo.examples git download
2. mimi.tagengo.examples/src/Android/TagengoExampleApp 안드로이드 스튜디오에서 해당 디렉토리 import
3. run (추가 설정 없이 바로 사용 가능)

## API 지원 언어
ja (일본어)  
en (영어)  
es (스페인어)  
fr (프랑스어)  
id (인도네시아어)  
ko (한국어)  
my (미얀마어)  
th (태국어)  
vi (베트남어)  
zh (중국어)  

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
  String inputLanguage = "ja"; // 음성합성하여 출력할 언어
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
  String inputLanguage = "ko"; // 음성인식할 언어
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
