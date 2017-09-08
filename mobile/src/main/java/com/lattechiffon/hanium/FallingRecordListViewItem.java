package com.lattechiffon.hanium;

/**
 * 낙상사고 기록 리스트 뷰를 담당하는 아이템 클래스입니다.
 * @version : 1.0
 * @author  : Yongguk Go (lattechiffon@gmail.com)
 */
class FallingRecordListViewItem {
    private int no;
    private String datetime;
    private int result;

    void setNo(int no) {
        this.no = no;
    }

    void setDatetime(String datetime) {  this.datetime = datetime; }

    void setResult(int result) { this.result = result; }

    int getNo() { return this.no;}

    String getDatetime() { return this.datetime; }

    int getResult() { return this.result; }
}
