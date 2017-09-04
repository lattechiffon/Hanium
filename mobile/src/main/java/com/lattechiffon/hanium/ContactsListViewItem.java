package com.lattechiffon.hanium;

class ContactsListViewItem {
    private int no;
    private String name;
    private String phone;
    private boolean protector;

    void setNo(int no) { this.no = no; }

    void setName(String name) {
        this.name = name;
    }

    void setPhone(String phone) { this.phone = phone; }

    void setProtector(boolean protector) { this.protector = protector; }

    int getNo() { return this.no; }

    String getName() { return this.name;}

    String getPhone() { return this.phone; }

    boolean getProtector() { return this.protector; }
}
