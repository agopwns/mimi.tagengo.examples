# 안드로이드판 샘플 앱 빌드 실행 순서

### API 예제 실행 방법
1. https://github.com/agopwns/mimi.tagengo.examples git download
2. mimi.tagengo.examples/src/Android/TagengoExampleApp 안드로이드 스튜디오에서 해당 디렉토리 import
3. run
4. app > java > jp.fairydevices.mimi > example > PrismClient 안에 주요 기능 클래스 있음
    - MT - 기계번역 
    - SS - 음성합성
    - SRRecordingTask - 음성인식

## インポート手順

1. Android Studio から mimi.tagengo.example/src/Android/TagengoExampleApp ディレクトリをインポートします。
2. https://github.com/FairyDevicesRD/mimi.tagengo.examples/releases から、`libmimiio-android-19-armeabi-v7a-libc++.tar.gz` をダウンロードします。
3. 展開した `libmimiio_jni.so` を次のように配置します。
```
mimi.tagengo.examples/src/Android/TagengoExampleApp/app/src/main/jniLibs/armeabi-v7a/libmimiio_jni.so
```
## 実行手順

1. `app/src/main/java/jp/fairydevices/mimi/example/PrismClient.java` ファイルを開き、PrismClient クラス内にある下記変数を 予め配布された`アプリケーションID` 、 `アプリケーションシークレット` で置き換えます。
```
private static final String ID = "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";
private static final String SECRET = "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";
```
2. Android Studio から `Run` -> `Run 'app'` と実行します。

## ライセンスについて
`libmimiio-android-19-armeabi-v7a-libc++.tar.gz` に含まれる `libmimiio_jni.so` は以下のソフトウェアを結合したものです。

- libmimiio (Dec 12, 2018)
- OpenSSL (1.0.2q)
- Poco (1.9.0)
- FLAC （1.3.2）

それぞれのライセンスついては同梱の LICENCE ファイルをご確認ください。
