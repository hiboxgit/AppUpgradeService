package com.lachesis.common.db;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmModel;
import io.realm.RealmObject;
import io.realm.RealmResults;

/**
 * realm 数据库辅助类
 *
 * Created by Robert on 2017/9/24.
 */

public class RealmDBHelper {

    private static RealmDBHelper instance = new RealmDBHelper();

    private RealmDBHelper() {
    }

    public static RealmDBHelper getInstance(){
        return instance;
    }

    public void insert(RealmObject data){
        Realm mRealm=Realm.getDefaultInstance();

        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {

                realm.copyToRealmOrUpdate(data);

            }
        });
    }

    public void delete(RealmObject data){
//        Realm  mRealm=Realm.getDefaultInstance();
//
//        final RealmResults<Dog> dogs=  mRealm.where(Dog.class).findAll();
//
//        mRealm.executeTransaction(new Realm.Transaction() {
//            @Override
//            public void execute(Realm realm) {
//
//                Dog dog=dogs.get(5);
//                dog.deleteFromRealm();
//                //删除第一个数据
//                dogs.deleteFirstFromRealm();
//                //删除最后一个数据
//                dogs.deleteLastFromRealm();
//                //删除位置为1的数据
//                dogs.deleteFromRealm(1);
//                //删除所有数据
//                dogs.deleteAllFromRealm();
//            }
//        });
    }

    public void update(RealmObject data){
        Realm mRealm=Realm.getDefaultInstance();

        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {

                realm.copyToRealmOrUpdate(data);

            }
        });
    }

    public List<RealmObject> queryAll(){
        Realm  mRealm=Realm.getDefaultInstance();

        RealmResults<RealmObject> dogs = mRealm.where(RealmObject.class).findAll();

        return mRealm.copyFromRealm(dogs);
    }

    public RealmObject queryFirst(String id){
        Realm  mRealm=Realm.getDefaultInstance();

        RealmObject dog = mRealm.where(RealmObject.class).equalTo("id", id).findFirst();
        return dog;
    }
}
