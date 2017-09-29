package com.dudu.upgrade.bt;

/**
 * Created by raise.yang on 2016/05/09.
 */
public class BtContract {

    interface View<T> {

        void set_presenter(T presenter);

        void can_find_bt();

        void send_progress(int progress);

        void connect_device(String deviceName);

        void show_error(String e);

        void connect_state_change(int state);

        void notify_view_change(int status);

        void finish();
    }

    interface Presenter {

        void start();

        void discoverable_bt();

        void on_result(int requestCode, int resultCode);

    }
}
