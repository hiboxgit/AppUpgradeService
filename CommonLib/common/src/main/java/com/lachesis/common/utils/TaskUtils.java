package com.lachesis.common.utils;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by boxue.hao on 2017/9/26.
 */

public class TaskUtils {

    /* 倒计时任务(s) */
    public static Observable<Integer> countdown(int time) {
        if (time < 0) time = 0;

        final int countTime = time;
        return Observable.interval(0, 1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .map(new Func1<Long, Integer>() {
                    @Override
                    public Integer call(Long increaseTime) {
                        return countTime - increaseTime.intValue();
                    }
                })
                .take(countTime + 1);
    }

    /* 定时执行任务 */
    public static Observable<Long> interval(long delay,long minutes){
        return Observable.interval(delay,minutes, TimeUnit.MINUTES)
                .subscribeOn(Schedulers.newThread());
    }

    /* 主线程执行任务*/
    public static Observable<Integer> runMainThread(){
        return Observable.just(1)
                .observeOn(AndroidSchedulers.mainThread());
    }

    /* 子线程执行任务 */
    public static Observable<Integer> runSubThread(){
        return Observable.just(1)
                .observeOn(Schedulers.newThread());
    }

    /* 延时任务 */
    public static Observable<Long> delay2Do(int delaySeconds){
        return Observable.timer(delaySeconds, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread());
    }
}
