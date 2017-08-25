package com.lattechiffon.hanium;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class ContactsActivity extends AppCompatActivity {
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    BackgroundTask task;

    ListView listView;
    ContactsListViewAdapter adapter;

    public ContactsActivity() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.activity_contacts));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        adapter = new ContactsListViewAdapter();

        listView = (ListView) findViewById(R.id.listViewContacts);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(listener);
        listView.setOnItemLongClickListener(longClickListener);

        pref = getSharedPreferences("UserData", Activity.MODE_PRIVATE);
        editor = pref.edit();

        task = new BackgroundTask();
        task.execute();
    }

    private class BackgroundTask extends AsyncTask<String, Integer, okhttp3.Response> {

        ProgressDialog progressDialog = new ProgressDialog(ContactsActivity.this);
        JSONObject jsonObject;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog.getWindow().setGravity(Gravity.BOTTOM);
            progressDialog.setMessage("이용자 데이터 처리 중입니다.");
            progressDialog.show();
        }

        @Override
        protected okhttp3.Response doInBackground(String... arg0) {
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            OkHttpClient client = new OkHttpClient();
            jsonObject = getContactsList();

            RequestBody body = RequestBody.create(JSON, jsonObject.toString());

            Request request = new Request.Builder()
                    .url("http://www.lattechiffon.com/hanium/contacts_processing.php")
                    .post(body)
                    .build();

            try {
                return client.newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(okhttp3.Response a) {
            super.onPostExecute(a);

            progressDialog.dismiss();

            try {
                JSONObject json = new JSONObject(a.body().string());

                if (json.getString("count").equals("0")) {
                    adapter.addItem("서비스에 등록된 이용자가 없습니다.", "보호자로 등록하려면 보호자의 기기를 먼저 등록하여야 합니다.");
                } else {
                    JSONArray user = json.getJSONArray(("user"));

                    for (int i = 0; i < user.length(); i++) {
                        JSONObject userObject = user.getJSONObject(i);
                        adapter.addItem(userObject.getString("name"), userObject.getString("phone"));
                    }

                }
            } catch (Exception e) {
                adapter.addItem("알 수 없는 오류가 발생했습니다.", "새로고침하여 다시 리스트를 가져와주세요.");
            }

            adapter.notifyDataSetChanged();
        }
    }

    private JSONObject getContactsList() {
        Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, new String[] { ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME }, null, null, ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " ASC");

        int resultCount = cursor.getCount();

        if (resultCount == 0) {
            return null;
        }

        JSONObject jsonObject = new JSONObject();

        try {
            JSONArray jsonArray = new JSONArray();
            while (cursor.moveToNext()) {
                String contactsId = cursor.getString(0);
                Cursor phoneCursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER}, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactsId, null, null);

                while (phoneCursor.moveToNext()) {
                    JSONObject jsonDataObject = new JSONObject();

                    jsonDataObject.put("id", contactsId);
                    jsonDataObject.put("name", cursor.getString(1));
                    jsonDataObject.put("phone", phoneCursor.getString(0).replaceAll("-", ""));
                    jsonArray.put(jsonDataObject);
                }
            }

            jsonObject.put("user", jsonArray);
        } catch (JSONException e) {
            return null;
        }

        return jsonObject;
    }

    AdapterView.OnItemLongClickListener longClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
            ContactsListViewItem item = (ContactsListViewItem) adapterView.getItemAtPosition(position);

            String name = item.getName();
            String phone = item.getPhone();

            Toast.makeText(ContactsActivity.this, "이렇게 길게 누르면 " + name + " 님을 응급 알림 보호자에서 해제할 지의 여부를 묻습니다.", Toast.LENGTH_LONG).show();

            return true;
        }
    };

    AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            ContactsListViewItem item = (ContactsListViewItem) adapterView.getItemAtPosition(position);

            String name = item.getName();
            String phone = item.getPhone();

            Toast.makeText(ContactsActivity.this, "이렇게 한 번 누르면 " + name + " 님을 낙상 응급 알림 보호자로 지정합니다.", Toast.LENGTH_LONG).show();
            //Intent intent = new Intent(getContext(), UserInfoScrollingActivity.class);
            //startActivity(intent);
        }
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_enter, R.anim.slide_exit);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.slide_enter, R.anim.slide_exit);

                break;
        }

        return true;
    }
}
