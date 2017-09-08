package com.lattechiffon.hanium;

import android.app.ProgressDialog;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
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
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * 보호자를 지정하는 기능을 제공하는 클래스입니다.
 * @version : 1.0
 * @author  : Yongguk Go (lattechiffon@gmail.com)
 */
public class ContactsActivity extends AppCompatActivity {
    private DatabaseHelper databaseHelper;
    private ContactsListViewAdapter contactsListViewAdapter;
    private BackgroundTask task;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.activity_contacts));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        databaseHelper = new DatabaseHelper(getApplicationContext());

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.contacts_swipe_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                contactsListViewAdapter.clear();
                task = new BackgroundTask();
                task.execute();
            }
        });

        contactsListViewAdapter = new ContactsListViewAdapter();
        ListView listView = (ListView) findViewById(R.id.listViewContacts);
        listView.setAdapter(contactsListViewAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                ContactsListViewItem item = (ContactsListViewItem) adapterView.getItemAtPosition(position);

                if (item.getNo() == 0 || item.getProtector()) {
                    return;
                }

                String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis()));

                databaseHelper.insert("INSERT INTO CONTACTS(userNo, name, phone, date, valid) VALUES (" + item.getNo() + ", '" + item.getName() + "', '" + item.getPhone() + "', '" + currentDate + "', 1);");
                item.setProtector(true);

                Toast.makeText(ContactsActivity.this, item.getName()+ " 님을 보호자로 지정하였습니다.", Toast.LENGTH_SHORT).show();
                contactsListViewAdapter.notifyDataSetChanged();
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                ContactsListViewItem item = (ContactsListViewItem) adapterView.getItemAtPosition(position);

                if (item.getNo() == 0 || !item.getProtector()) {
                    return true;
                }

                databaseHelper.insert("UPDATE CONTACTS SET valid = 0 WHERE userNo = " + item.getNo() + " AND valid = 1;");
                item.setProtector(false);

                Toast.makeText(ContactsActivity.this, item.getName()+ " 님을 보호자에서 해제하였습니다.", Toast.LENGTH_SHORT).show();
                contactsListViewAdapter.notifyDataSetChanged();

                return true;
            }
        });

        task = new BackgroundTask();
        task.execute();
    }

    /**
     * 서버로부터 보호자 리스트를 가져오는 내부 클래스입니다.
     * @version : 1.0
     * @author  : Yongguk Go (lattechiffon@gmail.com)
     */
    private class BackgroundTask extends AsyncTask<String, Integer, okhttp3.Response> {
        private ProgressDialog progressDialog = new ProgressDialog(ContactsActivity.this);
        private JSONObject jsonObject;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if (!swipeRefreshLayout.isRefreshing()) {
                if (progressDialog.getWindow() != null) {
                    progressDialog.getWindow().setGravity(Gravity.BOTTOM);
                }

                progressDialog.setMessage("이용자 데이터 처리 중입니다.");
                progressDialog.setCancelable(false);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
            }
        }

        @Override
        protected okhttp3.Response doInBackground(String... arg0) {
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            OkHttpClient client = new OkHttpClient();
            jsonObject = getContactsList();

            if (jsonObject == null) {
                return null;
            }

            RequestBody body = RequestBody.create(JSON, jsonObject.toString());
            Request request = new Request.Builder()
                    .url("http://www.lattechiffon.com/hanium/contacts_processing.php")
                    .post(body)
                    .build();

            try {
                return client.newCall(request).execute();
            } catch (IOException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(okhttp3.Response a) {
            super.onPostExecute(a);

            if (!swipeRefreshLayout.isRefreshing()) {
                progressDialog.dismiss();
            }

            if (a == null) {
                contactsListViewAdapter.addItem(0, "기기에 등록된 연락처가 없습니다.", "보호자를 등록하려면 하나 이상의 연락처를 먼저 등록하여야 합니다.", false);
                contactsListViewAdapter.notifyDataSetChanged();

                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(ContactsActivity.this, getString(R.string.main_toast_list_refresh), Toast.LENGTH_LONG).show();
                }

                return;
            }

            try {
                JSONObject json = new JSONObject(a.body().string());

                if (json.getString("count").equals("0")) {
                    contactsListViewAdapter.addItem(0, "서비스에 등록된 이용자가 없습니다.", "보호자로 등록하려면 보호자의 기기를 먼저 등록하여야 합니다.", false);
                } else {
                    JSONArray user = json.getJSONArray(("user"));

                    for (int i = 0; i < user.length(); i++) {
                        JSONObject userObject = user.getJSONObject(i);
                        boolean protector = false;

                        if (databaseHelper.checkProtector(userObject.getInt("no"))) {
                            protector = true;
                        }

                        contactsListViewAdapter.addItem(userObject.getInt("no"), userObject.getString("name"), userObject.getString("phone"), protector);
                    }

                }
            } catch (Exception e) {
                contactsListViewAdapter.addItem(0, "알 수 없는 오류가 발생했습니다.", "새로고침하여 다시 리스트를 가져와주세요.", false);
            }

            contactsListViewAdapter.notifyDataSetChanged();

            if (swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(ContactsActivity.this, getString(R.string.main_toast_list_refresh), Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * 안드로이드 내의 주소록으로부터 모든 연락처 정보를 가져오는 메서드입니다.
     * @return ; 주소록에 저장된 모든 연락처 정보 (구분 ID, 이름, 전하번호)
     */
    private JSONObject getContactsList() {
        Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, new String[] { ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME }, null, null, ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " ASC");
        int resultCount = cursor != null ? cursor.getCount() : 0;

        if (resultCount == 0) {
            return null;
        }

        JSONObject jsonObject = new JSONObject();

        try {
            JSONArray jsonArray = new JSONArray();

            while (cursor.moveToNext()) {
                String contactsId = cursor.getString(0);
                Cursor phoneCursor;

                phoneCursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER}, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactsId, null, null);

                while (phoneCursor != null && phoneCursor.moveToNext()) {
                    JSONObject jsonDataObject = new JSONObject();

                    jsonDataObject.put("id", contactsId);
                    jsonDataObject.put("name", cursor.getString(1));
                    jsonDataObject.put("phone", phoneCursor.getString(0).replaceAll("-", ""));
                    jsonArray.put(jsonDataObject);
                }

                if (phoneCursor != null) {
                    phoneCursor.close();
                }
            }

            cursor.close();
            jsonObject.put("user", jsonArray);
        } catch (JSONException e) {
            return null;
        }

        return jsonObject;
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_enter, R.anim.slide_exit);
    }
}
