package com.awaitu.allen.softlrucache;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends Activity {
    private ListView mImageListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageListView = (ListView) findViewById(R.id.listview);
        String[] values = new String[]{"google", "gmail", "gtalk",
                "skype", "youtube", "ebay", "linkedin", "twitter",
                "facebook", "yahoo", "google", "gmail", "gtalk",
                "skype", "youtube", "ebay", "linkedin", "twitter",
                "facebook", "yahoo", "google", "gmail", "gtalk",
                "skype", "youtube", "ebay", "linkedin", "twitter",
                "facebook", "yahoo", "google", "gmail", "gtalk",
                "skype", "youtube", "ebay", "linkedin", "twitter",
                "skype", "youtube", "ebay", "linkedin", "twitter",
                "facebook", "yahoo", "google", "gmail", "gtalk",
                "skype", "youtube", "ebay", "linkedin", "twitter",
                "facebook", "yahoo", "google", "gmail", "gtalk",
                "skype", "youtube", "ebay", "linkedin", "twitter",
                "facebook", "yahoo", "google", "gmail", "gtalk",
                "skype", "youtube", "ebay", "linkedin", "twitter",
                "skype", "youtube", "ebay", "linkedin", "twitter",
                "facebook", "yahoo", "google", "gmail", "gtalk",
                "skype", "youtube", "ebay", "linkedin", "twitter",
                "facebook", "yahoo", "google", "gmail", "gtalk",
                "skype", "youtube", "ebay", "linkedin", "twitter",
                "facebook", "yahoo", "google", "gmail", "gtalk",
                "skype", "youtube", "ebay", "linkedin", "twitter",
                "skype", "youtube", "ebay", "linkedin", "twitter",
                "facebook", "yahoo", "google", "gmail", "gtalk",
                "skype", "youtube", "ebay", "linkedin", "twitter",
                "facebook", "yahoo", "google", "gmail", "gtalk",
                "skype", "youtube", "ebay", "linkedin", "twitter",
                "facebook", "yahoo", "google", "gmail", "gtalk",
                "skype", "youtube", "ebay", "linkedin", "twitter"};

        final ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < values.length; ++i) {
            list.add(values[i]);
        }

        final ImageArrayAdapter adapter = new ImageArrayAdapter(ctx(), R.layout.image_row, list);
        mImageListView.setAdapter(adapter);

        mImageListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                ViewGroup viewGroup = (ViewGroup) view;
                TextView txtDescription = (TextView) viewGroup.findViewById(R.id.description);
                String item = txtDescription.getText().toString();
                Toast.makeText(ctx(), item, Toast.LENGTH_SHORT).show();

            }

        });

    }

    public Context ctx() {
        return this;
    }

}
