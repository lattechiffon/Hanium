package com.lattechiffon.hanium;

/**
 * 보호자 리스트 뷰를 담당하는 아이템 클래스입니다.
 *
 * @version 1.0
 * @author  Yongguk Go (lattechiffon@gmail.com)
 */
class ContactsListViewItem {
    private int no;
    private String name;
    private String phone;
    private boolean protector;

    /**
     * 연락처 관리 번호를 설정하는 메서드입니다.
     *
     * @param no 안드로이드 주소록 시스템에서 지정한 연락처 관리 번호
     */
    void setNo(int no) { this.no = no; }

    /**
     * 연락처 이를을 설정하는 메서드입니다.
     *
     * @param name 연락처 이름
     */
    void setName(String name) {
        this.name = name;
    }

    /**
     * 연락처 전화번호를 설정하는 메서드입니다.
     *
     * @param phone 연락처 전화번호
     */
    void setPhone(String phone) { this.phone = phone; }

    /**
     * 보호자 지정 여부를 설정하는 메서드입니다.
     *
     * @param protector 보호자 지정 여부
     */
    void setProtector(boolean protector) { this.protector = protector; }

    /**
     * 연락처 관리 번호를 반환하는 메서드입니다.
     *
     * @return 안드로이드 주소록 시스템에서 지정한 연락처 관리 번호
     */
    int getNo() { return this.no; }

    /**
     * 연락처 이름을 반환하는 메서드입니다.
     *
     * @return 연락처 이름
     */
    String getName() { return this.name;}

    /**
     * 연락처 전화번호를 반환하는 메서드입니다.
     *
     * @return 연락처 전화번호
     */
    String getPhone() { return this.phone; }

    /**
     * 보호자 지정 여부를 반환하는 메서드입니다.
     *
     * @return 보호자 지정 여부
     */
    boolean getProtector() { return this.protector; }
}
