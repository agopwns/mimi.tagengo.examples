package jp.fairydevices.mimi.example.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import jp.fairydevices.mimi.example.R;
import jp.fairydevices.mimi.example.adapter.TranslationHistoryModifyAdapter;
import jp.fairydevices.mimi.example.db.HistoryDB;
import jp.fairydevices.mimi.example.db.HistoryDao;
import jp.fairydevices.mimi.example.model.TranslationHistory;

public class TranslationHistoryModifyActivity extends AppCompatActivity {

    private HistoryDao dao;
    private List<TranslationHistory> list;
    private RecyclerView recyclerView;
    private TranslationHistoryModifyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translation_history_modify);

        dao = HistoryDB.getInstance(this).historyDao();

        // 리사이클러뷰에 LinearLayoutManager 객체 지정.
        recyclerView = findViewById(R.id.recyclerView_history);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadNotes() {
        this.list = new ArrayList<>();
        list = dao.getHistories(); // DB 에서 모든 노트 가져오기
        this.adapter = new TranslationHistoryModifyAdapter(list, this);
        this.recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotes();
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }
}
