package com.lattechiffon.hanium;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * 낙상사고 기록 리스트 뷰를 담당하는 어댑터 클래스입니다.
 * @version : 1.0
 * @author  : Yongguk Go (lattechiffon@gmail.com)
 */
class FallingRecordListViewAdapter extends BaseAdapter {
    private ArrayList<FallingRecordListViewItem> listViewItemList = new ArrayList<>();

    @Override
    public int getCount() {
        return listViewItemList.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Context context = parent.getContext();

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_view_item_falling_record, parent, false);
        }

        TextView datetimeTextView = (TextView) convertView.findViewById(R.id.fallingRecordListViewItemDatetime);
        TextView resultTextView = (TextView) convertView.findViewById(R.id.fallingRecordListViewItemResult);

        FallingRecordListViewItem listViewItem = listViewItemList.get(position);

        datetimeTextView.setText(listViewItem.getDatetime());

        if (listViewItem.getResult() == 1) {
            resultTextView.setText("정상 인식");
        } else {
            resultTextView.setText("잘못된 인식");
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

    void addItem(int no, String datetime, int result) {
        FallingRecordListViewItem item = new FallingRecordListViewItem();

        item.setNo(no);
        item.setDatetime(datetime);
        item.setResult(result);

        listViewItemList.add(item);
    }

    int getItemNo(Object itemObject) {
        FallingRecordListViewItem item = (FallingRecordListViewItem) itemObject;

        return item.getNo();
    }

    void remove(Object item) {
        listViewItemList.remove(item);
        this.notifyDataSetChanged();
    }

    void clear() {
        listViewItemList.clear();
        this.notifyDataSetChanged();
    }
}
