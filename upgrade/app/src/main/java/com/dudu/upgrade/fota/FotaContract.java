package com.dudu.upgrade.fota;

/**
 * Created by raise.yang on 2016/05/04.
 */
public class FotaContract {

    interface View<T> {
        void set_presenter(T presenter);

        void show_default_ui();

        void show_checking();

        void show_can_download();

        void show_downloading(int progress);

        void show_can_upgrade();

        void show_upgrading();

        void show_error(String error);

    }

    interface Presenter {
        void start();

        void check_version();

        void download();

        void reboot_upgrade();

        void download_cancel();

        /**
         * 解析多语言release note
         *
         * @return
         */
        String parse_release_note();

    }

}
