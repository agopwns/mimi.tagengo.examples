package jp.fairydevices.mimi.example.model;

public class TranslationHistory {


    public final String beforeString;
    public final String afterString;

    public TranslationHistory(String beforeString, String afterString) {
        this.beforeString = beforeString;
        this.afterString = afterString;
    }

    public String getBeforeString() {
        return beforeString;
    }

    public String getAfterString() {
        return afterString;
    }
}
