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
 *
 * @version 1.0
 * @author  Yongguk Go (lattechiffon@gmail.com)
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

    /**
     * 낙상사고 발생 기록을 리스트의 아이템으로 삽입하는 메서드입니다.
     *
     * @param no 낙상사고 관리 번호
     * @param datetime 낙상사고 발생 시각
     * @param result 낙상사고 처리 결과
     */
    void addItem(int no, String datetime, int result) {
        FallingRecordListViewItem item = new FallingRecordListViewItem();

        item.setNo(no);
        item.setDatetime(datetime);
        item.setResult(result);

        listViewItemList.add(item);
    }

    /**
     * 특정 낙상사고 발생 기록 아이템의 리스트 번호를 반환하는 메서드입니다.
     *
     * @param itemObject 낙상사고 발생 기록
     * @return 낙상사고 발생 기록 아이템 리스트 번호
     */
    int getItemNo(Object itemObject) {
        FallingRecordListViewItem item = (FallingRecordListViewItem) itemObject;

        return item.getNo();
    }

    /**
     * 특정 낙상사고 발생 기록 아이템의 리스트 번호를 삭제하는 메서드입니다.
     *
     * @param item 낙상사고 발생 기록
     */
    void remove(Object item) {
        listViewItemList.remove(item);
        this.notifyDataSetChanged();
    }

    /**
     * 리스트의 모든 아이템을 리스트에서 삭제하는 메서드입니다.
     * 실제로 낙상사고 발생 기록 데이터가 삭제되는 것은 아닙니다.
     */
    void clear() {
        listViewItemList.clear();
        this.notifyDataSetChanged();
    }
}
