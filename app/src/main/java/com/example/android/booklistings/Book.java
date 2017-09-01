package com.example.android.booklistings;

/**
 * Created by AiGa on 2017.07.18..
 */

public class Book {

    //Book title
    private String mBookTitle;

    //Book author
    private String mBookAuthor;

    //Book thumbnail link
    private String mThumbnailLink;

    //Book url;
    private String mUrl;

    //Constructor
    public Book(String thumbnailLink, String title, String author, String url) {
        mThumbnailLink = thumbnailLink;
        mBookTitle = title;
        mBookAuthor = author;
        mUrl = url;
    }

    //getters
    public String getBookTitle() {
        return mBookTitle;
    }

    public String getBookAuthor() {
        return mBookAuthor;
    }

    public String getThumbnailLink() {
        return mThumbnailLink;
    }

    public String getUrl() {
        return mUrl;
    }

}
