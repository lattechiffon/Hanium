package com.lattechiffon.hanium;

class ContactsListViewItem {
    private String name;
    private String phone;
    private String push;

    void setName(String name) {
        this.name = name;
    }

    void setPhone(String phone) {  this.phone = phone; }

    void setPush(String push) { this.push = push; }

    String getName() { return this.name;}

    String getPhone() { return this.phone; }

    String getPush() { return this.push; }
}
