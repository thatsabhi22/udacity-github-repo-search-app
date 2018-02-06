/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.datafrominternet;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.datafrominternet.utilities.NetworkUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private EditText mSearchBoxEditText;

    private TextView mUrlDisplayTextView;

    private TextView mSearchResultsTextView;

    private TextView mErrorDisplayTextView;

    private ProgressBar progressBar;

    /**
     * Method to hide keyboard after the user has provided the search query
     *
     * @param activity object
     */
    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSearchBoxEditText = (EditText) findViewById(R.id.et_search_box);
        mUrlDisplayTextView = (TextView) findViewById(R.id.tv_url_display);
        mSearchResultsTextView = (TextView) findViewById(R.id.tv_github_search_results_json);
        mErrorDisplayTextView = (TextView) findViewById(R.id.tv_error_message_display);
        progressBar = (ProgressBar) findViewById(R.id.pb_loading_indicator);
        progressBar.setIndeterminate(true);
    }

    /**
     * Method that creates the search URL and displays in the url textview
     */
    void makeGithubSearchQuery() {

        if (TextUtils.isEmpty(mSearchBoxEditText.getText().toString())) {
            mSearchBoxEditText.setError("Please Enter a Search Query");
            return;
        }
        String searchQuery = mSearchBoxEditText.getText().toString();
        URL repoSearchUrl = NetworkUtils.buildUrl(searchQuery);
        mUrlDisplayTextView.setText(repoSearchUrl.toString());

        new GithubQueryTask().execute(repoSearchUrl);

        hideKeyboard(this);
    }

    void showJsonDataView() {
        mSearchResultsTextView.setVisibility(View.VISIBLE);
        mErrorDisplayTextView.setVisibility(View.INVISIBLE);
    }

    void showErrorMessage() {
        mSearchResultsTextView.setVisibility(View.INVISIBLE);
        mErrorDisplayTextView.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemThatWasClickedId = item.getItemId();
        if (itemThatWasClickedId == R.id.action_search) {
            mSearchResultsTextView.setText("");
            makeGithubSearchQuery();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class GithubQueryTask extends AsyncTask<URL, Void, String> {

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(URL... urls) {
            URL searchUrl = urls[0];
            String githubSearchResults = null;

            try {
                githubSearchResults = NetworkUtils.getResponseFromHttpUrl(searchUrl);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return githubSearchResults;
        }

        @Override
        protected void onPostExecute(String githubSearchResults) {

            String prettyString = "";
            progressBar.setVisibility(View.INVISIBLE);
            if (!TextUtils.isEmpty(githubSearchResults)) {
                showJsonDataView();
                try {
                    int spacesToIndentEachLevel = 2;
                    prettyString = new JSONObject(githubSearchResults).toString(spacesToIndentEachLevel);
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
                mSearchResultsTextView.setText(prettyString);
            } else {
                showErrorMessage();
            }
        }
    }
}
