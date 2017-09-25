package com.lachesis.appupgradeservice.modules.upgrade.model;

/**
 * Created by boxue.hao on 2017/9/25.
 */

public class UpgradeTipEvent {
    private String note; //升级日志

    public UpgradeTipEvent(String note) {
        this.note = note;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public String toString() {
        return "UpgradeTipEvent{" +
                "note='" + note + '\'' +
                '}';
    }
}
