package com.bookstore.model;

/**
 * 关键字实体类，对应表：keyword。
 */
public class Keyword {

    private Long keywordId;
    private String keywordText;

    public Long getKeywordId() {
        return keywordId;
    }

    public void setKeywordId(Long keywordId) {
        this.keywordId = keywordId;
    }

    public String getKeywordText() {
        return keywordText;
    }

    public void setKeywordText(String keywordText) {
        this.keywordText = keywordText;
    }

    @Override
    public String toString() {
        return "Keyword{" +
                "keywordId=" + keywordId +
                ", keywordText='" + keywordText + '\'' +
                '}';
    }
}


