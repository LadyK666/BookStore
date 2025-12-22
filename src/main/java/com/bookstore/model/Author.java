package com.bookstore.model;

/**
 * 作者实体类，对应表：author。
 */
public class Author {

    private Long authorId;
    private String authorName;
    private String nationality;
    private String biography;
    /**
     * 非持久化字段：在按书目查询作者时用于承载作者顺序（author_order）。
     */
    private Integer authorOrder;

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getBiography() {
        return biography;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }

    public Integer getAuthorOrder() {
        return authorOrder;
    }

    public void setAuthorOrder(Integer authorOrder) {
        this.authorOrder = authorOrder;
    }

    @Override
    public String toString() {
        return authorName;
    }
}


