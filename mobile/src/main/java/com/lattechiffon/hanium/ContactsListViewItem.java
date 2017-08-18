package com.lattechiffon.hanium;

/**
 * Created by yongguk on 2017. 2. 20..
 */

class ContactsListViewItem {
    private String name;
    private String phone;

    void setName(String name) {
        this.name = name;
    }

    void setPhone(String phone) {
        this.phone = phone;
    }

    String getName() { return this.name;}

    String getPhone() { return this.phone; }
}
