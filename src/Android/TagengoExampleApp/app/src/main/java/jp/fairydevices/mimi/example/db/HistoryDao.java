package jp.fairydevices.mimi.example.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import jp.fairydevices.mimi.example.model.TranslationHistory;

@Dao
public interface HistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertHistory(TranslationHistory history);

    @Delete
    void deleteTh(TranslationHistory history);

    @Query("UPDATE History SET isBookmark = :isBookMark WHERE id = :id")
    void updateBookMark(int id, boolean isBookMark);

    @Query("SELECT * FROM History ORDER BY id DESC")
    List<TranslationHistory> getHistories();

    @Query("SELECT * FROM History WHERE isBookmark = 1 ORDER BY id DESC")
    List<TranslationHistory> getBookmarks();

    @Query("SELECT * FROM History WHERE id LIKE :id") // 수정, 삭제시 필요
    TranslationHistory getHistoryById(int id);

    @Query("DELETE FROM History WHERE id LIKE :id")
    void deleteHistoryById(int id);
}
