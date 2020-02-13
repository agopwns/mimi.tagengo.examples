package jp.fairydevices.mimi.example.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import jp.fairydevices.mimi.example.R;
import jp.fairydevices.mimi.example.adapter.TranslationBookmarkAdapter;
import jp.fairydevices.mimi.example.model.TranslationHistory;

public class TranslationBookmarkActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translation_bookmark);

        // 리사이클러뷰 데이터 리스트 생성(임시)
        ArrayList<TranslationHistory> list = new ArrayList<>();
        for (int i=0; i<20; i++) {
            TranslationHistory th = new TranslationHistory("안녕하세요" + i, "おはようございます。" + i);
            list.add(th) ;
        }

        // 리사이클러뷰에 LinearLayoutManager 객체 지정.
        RecyclerView recyclerView = findViewById(R.id.recyclerView_bookmark);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 리사이클러뷰에 SimpleTextAdapter 객체 지정.
        TranslationBookmarkAdapter adapter = new TranslationBookmarkAdapter(list) ;
        recyclerView.setAdapter(adapter) ;
    }
}
