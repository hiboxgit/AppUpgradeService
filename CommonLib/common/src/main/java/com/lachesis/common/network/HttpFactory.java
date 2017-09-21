package com.lachesis.common.network;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

/**
 * http 工厂类
 * Created by Robert on 2017/9/21.
 */

public class HttpFactory {

    public IHttpClient getClient(String type){

        if(type.equalsIgnoreCase("okhttp")){

            return null;

        }else{

            OkGo.<String>get("")
                    .tag(this)
                    .execute(new StringCallback() {
                        @Override
                        public void onSuccess(Response<String> response) {

                        }
                    });

            return null;

        }
    }
}
