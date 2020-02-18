package jp.fairydevices.mimi.example.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "History")
public class TranslationHistory {

    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "beforeString")
    private String beforeString;
    @ColumnInfo(name = "afterString")
    private String afterString;
    @ColumnInfo(name = "translationScore")
    private int translationScore;
    @ColumnInfo(name = "isBookmark")
    private boolean isBookmark;

    public TranslationHistory(String beforeString, String afterString) {
        this.beforeString = beforeString;
        this.afterString = afterString;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setBeforeString(String beforeString) {
        this.beforeString = beforeString;
    }

    public void setAfterString(String afterString) {
        this.afterString = afterString;
    }

    public String getBeforeString() {
        return beforeString;
    }

    public String getAfterString() {
        return afterString;
    }

    public int getTranslationScore() {
        return translationScore;
    }

    public void setTranslationScore(int translationScore) {
        this.translationScore = translationScore;
    }

    public boolean isBookmark() {
        return isBookmark;
    }

    public void setBookmark(boolean bookmark) {
        isBookmark = bookmark;
    }
}
