package com.lattechiffon.hanium;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import java.util.ArrayList;

/**
 * 보호자 리스트 뷰를 담당하는 어댑터 클래스입니다.
 * @version : 1.0
 * @author  : Yongguk Go (lattechiffon@gmail.com)
 */
class ContactsListViewAdapter extends BaseAdapter {
    private ArrayList<ContactsListViewItem> listViewItemList = new ArrayList<>();

    @Override
    public int getCount() {
        return listViewItemList.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Context context = parent.getContext();

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_view_item_contacts, parent, false);
        }

        TextView nameTextView = (TextView) convertView.findViewById(R.id.listViewItemName);
        TextView phoneTextView = (TextView) convertView.findViewById(R.id.listViewItemPhone);

        ContactsListViewItem listViewItem = listViewItemList.get(position);

        nameTextView.setText(listViewItem.getName());
        phoneTextView.setText(listViewItem.getPhone());

        if (listViewItem.getProtector()) {
            nameTextView.setTextColor(Color.parseColor("#FFFFFF"));
            phoneTextView.setTextColor(Color.parseColor("#FFFFFF"));
            convertView.setBackgroundResource(R.color.colorAccent);
        } else {
            nameTextView.setTextColor(Color.parseColor("#000000"));
            phoneTextView.setTextColor(Color.parseColor("#666666"));
            convertView.setBackgroundResource(R.color.colorPrimary);
        }

        return convertView;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return listViewItemList.get(position);
    }

    void addItem(int no, String name, String phone, boolean protector) {
        ContactsListViewItem item = new ContactsListViewItem();

        item.setNo(no);
        item.setName(name);
        item.setPhone(phone);
        item.setProtector(protector);

        listViewItemList.add(item);
    }

    void clear() {
        listViewItemList.clear();
        this.notifyDataSetChanged();
    }
}
