package com.lattechiffon.hanium;

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
