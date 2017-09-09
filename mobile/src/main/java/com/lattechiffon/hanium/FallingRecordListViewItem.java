package com.lattechiffon.hanium;

/**
 * 낙상사고 기록 리스트 뷰를 담당하는 아이템 클래스입니다.
 *
 * @version 1.0
 * @author  Yongguk Go (lattechiffon@gmail.com)
 */
class FallingRecordListViewItem {
    private int no;
    private String datetime;
    private int result;

    /**
     * 낙상사고 관리 번호를 설정하는 메서드입니다.
     *
     * @param no 낙상사고 관리 번호
     */
    void setNo(int no) {
        this.no = no;
    }

    /**
     * 낙상사고 발생 시각를 설정하는 메서드입니다.
     *
     * @param datetime 낙상사고 발생 시각
     */
    void setDatetime(String datetime) {  this.datetime = datetime; }

    /**
     * 낙상사고 처리 결과를 설정하는 메서드입니다.
     *
     * @param result 낙상사고 처리 결과
     */
    void setResult(int result) { this.result = result; }

    /**
     * 낙상사고 관리 번호를 반환하는 메서드입니다.
     *
     * @return 낙상사고 관리 번호
     */
    int getNo() { return this.no;}

    /**
     * 낙상사고 발생 시각을 반환하는 메서드입니다.
     *
     * @return 낙상사고 발생 시각
     */
    String getDatetime() { return this.datetime; }

    /**
     * 낙상사고 처리 결과을 반환하는 메서드입니다.
     *
     * @return 낙상사고 처리 결과
     */
    int getResult() { return this.result; }
}
