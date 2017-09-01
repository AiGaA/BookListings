package com.example.android.booklistings;

import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // Tag for LOG Message
    private static final String LOG_TAG = MainActivity.class.getName();

    // URL for google books data from the Google API
    private static final String BOOKS_REQUEST_URL =
            "https://www.googleapis.com/books/v1/volumes?maxResults=10&q=";

    // Google Books URL with the given key Android for the query result
    private static final String BOOK_URL_ANDROID =
            "https://www.googleapis.com/books/v1/volumes?q=android&maxResults=10";

    //Adapter for the list of books
    private BookAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find a reference to Views
        final ListView bookListView = (ListView) findViewById(R.id.list);

        TextView mEmptyView = (TextView) findViewById(R.id.empty_text_view);

        View loadingIndicator = findViewById(R.id.progress_bar);

        final EditText searchText = (EditText) findViewById(R.id.search);

        // Set empty state view on the list view with books, when there is no data.
        bookListView.setEmptyView(mEmptyView);

        // Create a new adapter that takes an empty list of books as input
        mAdapter = new BookAdapter(this, new ArrayList<Book>());

        // Set the adapter on the {@link ListView}
        // so the list can be populated in the user interface
        bookListView.setAdapter(mAdapter);

        BookAsyncTask task = new BookAsyncTask();

        //Create a ConnectivityManager and get the NetworkInfo from it
        final ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        //Create a boolean variable for the connectivity status
        final boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();

        //If the device is connected to the network
        if (isConnected) {
            Log.e(LOG_TAG, "This is called when there is an Internet connection.");
            // Start the AsyncTask to fetch the books data
            task.execute(BOOK_URL_ANDROID);
            //Hide loading indicator after content is loaded
            loadingIndicator.setVisibility(View.GONE);
            //If the device is not connected to the network
        } else {
            Log.e(LOG_TAG, "This is called when there is no Internet connection.");
            //Hide loading indicator so error will be visible
            loadingIndicator.setVisibility(View.GONE);
            //Show the empty state with no connection error message
            mEmptyView.setVisibility(View.VISIBLE);
            //Update empty state with no connection error message
            mEmptyView.setText(R.string.no_connection);
        }

        // Set a click listener on the search icon ImageView, to implement the search
        ImageView searchButton = (ImageView) findViewById(R.id.search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            // The code in this method will be executed when the the ImageView of search icon is clicked on.
            @Override
            public void onClick(View view) {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
                final boolean isConnected = activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();

                //Check for internet connection
                if (isConnected) {
                    String searchTerm = searchText.getText().toString();

                    if (searchTerm.isEmpty()) {
                        Toast.makeText(getApplicationContext(), R.string.noResults, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Searching for: " + searchTerm, Toast.LENGTH_SHORT).show();
                        String url = BOOKS_REQUEST_URL + searchTerm;

                        // Start the AsyncTask to fetch the book data
                        BookAsyncTask task = new BookAsyncTask();

                        task.execute(url);
                    }

                } else {
                    //Provide feedback about no internet connection
                    Toast.makeText(MainActivity.this, R.string.connError, Toast.LENGTH_LONG).show();
                }
            }
        });

        // Set an item click listener on the ListView, which sends an intent to a web browser
        // to open a website with more information about the selected book.
        bookListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // Find the current book that was clicked on
                Book currentBook = mAdapter.getItem(position);

                // Convert the String URL into a URI object (to pass into the Intent constructor)
                Uri bookUri = Uri.parse(currentBook.getUrl());

                // Create a new intent to view the book URI
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, bookUri);

                // Send the intent to launch a new activity
                startActivity(websiteIntent);
            }
        });
    }

    /**
     * {@link AsyncTask} to perform the network request on a background thread, and then
     * update the UI with the list of books in the response.
     * AsyncTask has three generic parameters: the input type, a type used for progress updates, and
     * an output type. Our task will take a String URL, and return a Book.
     * The doInBackground() method runs on a background thread, so it can run long-running code
     * (like network activity), without interfering with the responsiveness of the app.
     * Then onPostExecute() is passed the result of doInBackground() method, but runs on the
     * UI thread, so it can use the produced data to update the UI.
     */
    private class BookAsyncTask extends AsyncTask<String, Void, List<Book>> {
        /**
         * This method runs on a background thread and performs the network request.
         * We should not update the UI from a background thread, so we return a list of
         * {@link Book}s as the result.
         */
        ProgressDialog progress;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress = new ProgressDialog(MainActivity.this);
            progress.setMessage("Searching...");
            progress.setIndeterminate(false);
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.setCancelable(true);
            progress.show();
        }

        @Override
        protected List<Book> doInBackground(String... urls) {
            // Don't perform the request if there are no URLs, or the first URL is null
            if (urls.length < 1 || urls[0] == null) {
                return null;
            }

            List<Book> result = QueryUtils.fetchBookData(urls[0]);
            return result;
        }

        /**
         * This method runs on the main UI thread after the background work has been
         * completed. This method receives as input, the return value from the doInBackground()
         * method. First we clear out the adapter, to get rid of book data from a previous
         * query to Google Books API. Then we update the adapter with the new list of books,
         * which will trigger the ListView to re-populate its list items.
         */
        @Override
        protected void onPostExecute(List<Book> data) {
            // Clear the adapter of previous book data
            mAdapter.clear();
            progress.dismiss();
            // If there is a valid list of {@link Book}s, then add them to the adapter's
            // data set. This will trigger the ListView to update.
            if (data != null && !data.isEmpty()) {
                mAdapter.addAll(data);
            }
        }
    }
}

