package com.example.android.booklistings;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by AiGa on 2017.07.18..
 */

public class BookAdapter extends ArrayAdapter<Book> {

    public static final String LOG_TAG = BookAdapter.class.getName();

    public BookAdapter(Activity context, ArrayList<Book> books) {
        // Here, we initialize the ArrayAdapter's internal storage for the context and the list.
        // the second argument is used when the ArrayAdapter is populating a single TextView.
        // Because this is a custom adapter for two TextViews and an ImageView, the adapter is not
        // going to use this second argument, so it can be any value. Here, we used 0.
        super(context, 0, books);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        ViewHolder holder;
        View booksList = convertView;

        if (booksList == null) {
            booksList = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
            holder = new ViewHolder(booksList);
            booksList.setTag(holder);
        } else {
            holder = (ViewHolder) booksList.getTag();
        }
        // Find the book at the given position in the list of books
        Book currentBook = getItem(position);

        // Format the title of the current book in that TextView
        holder.titleView.setText(currentBook.getBookTitle());

        // Format the author at the given position in the list of books
        holder.authorView.setText(currentBook.getBookAuthor());

        //this shows an image representing the book using an AsyncTask for downloading the images
        ImageView bookCoverView = (ImageView) booksList.findViewById(R.id.book_cover);
        new imageAsyncTask(bookCoverView).execute(currentBook.getThumbnailLink());

        // Return the view
        return booksList;
    }

    static class ViewHolder {
        @BindView(R.id.book_title)
        TextView titleView;
        @BindView(R.id.book_author)
        TextView authorView;
        @BindView(R.id.book_cover)
        ImageView bookCoverView;


        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    /**
     * {@link AsyncTask} to perform the network request on a background thread,
     * and then update the UI allowing to display the image.
     */
    private class imageAsyncTask extends AsyncTask<String, Void, Bitmap> {
        ImageView mImage;

        //This method is called on a background thread.
        public imageAsyncTask(ImageView bmImage) {
            this.mImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String url = urls[0];
            Bitmap image = null;
            try {
                InputStream in = new java.net.URL(url).openStream();
                image = BitmapFactory.decodeStream(in); //this is used for decode the InputStream to image
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage());
            }
            return image;
        }

        //This method is invoked on the main UI thread after the background work has been completed.
        protected void onPostExecute(Bitmap result) {
            //set the image obtained
            mImage.setImageBitmap(result);
        }
    }
}
