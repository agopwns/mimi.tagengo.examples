package jp.fairydevices.mimi.example.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import jp.fairydevices.mimi.example.R;
import jp.fairydevices.mimi.example.adapter.TranslationHistoryAdapter;
import jp.fairydevices.mimi.example.db.HistoryDB;
import jp.fairydevices.mimi.example.db.HistoryDao;
import jp.fairydevices.mimi.example.model.TranslationHistory;

public class TranslationHistoryActivity extends AppCompatActivity {

    private HistoryDao dao;
    private TextView modifyTextView; // 편집 버튼 대용
    private List<TranslationHistory> list;
    private RecyclerView recyclerView;
    private TranslationHistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translation_history);

        dao = HistoryDB.getInstance(this).historyDao();

        // 편집 버튼 대신 텍스트로 씀. 버튼은 배경을 투명으로 해도 그림자가 남아서..
        modifyTextView = findViewById(R.id.modifyTextView);
        modifyTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), TranslationHistoryModifyActivity.class);
                intent.addFlags (Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });

        // 리사이클러뷰에 LinearLayoutManager 객체 지정.
        recyclerView = findViewById(R.id.recyclerView_history);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadNotes() {
        this.list = new ArrayList<>();
        list = dao.getHistories(); // DB 에서 모든 노트 가져오기
        this.adapter = new TranslationHistoryAdapter(list, this);
        this.recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotes();
    }
}
