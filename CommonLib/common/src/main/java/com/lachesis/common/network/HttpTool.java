package com.lachesis.common.network;

import com.lachesis.common.CommonLib;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloadQueueSet;
import com.liulishuo.filedownloader.FileDownloader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by boxue.hao on 2017/9/22.
 */

public class HttpTool {
    public static void downloadFile2Path(String url, String localFilePath){

        FileDownloader.getImpl().create(url)
                .setPath(localFilePath)
                .setListener(new FileDownloadListener() {
                    @Override
                    protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                    }

                    @Override
                    protected void started(BaseDownloadTask task) {
                    }

                    @Override
                    protected void connected(BaseDownloadTask task, String etag, boolean isContinue, int soFarBytes, int totalBytes) {
                    }

                    @Override
                    protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                    }

                    @Override
                    protected void blockComplete(BaseDownloadTask task) {
                    }

                    @Override
                    protected void retry(final BaseDownloadTask task, final Throwable ex, final int retryingTimes, final int soFarBytes) {
                    }

                    @Override
                    protected void completed(BaseDownloadTask task) {
                    }

                    @Override
                    protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                    }

                    @Override
                    protected void error(BaseDownloadTask task, Throwable e) {
                    }

                    @Override
                    protected void warn(BaseDownloadTask task) {
                    }
                }).start();
    }

    public static void downloadFiles(HashMap<String,String> downloadInfoList, final FileDownloadListener downloadListener){

        boolean isParallel = false; //是否并行下载

//        FileDownloader.setup(CommonLib.getInstance().getContext());
//        final FileDownloadListener queueTarget = new FileDownloadListener() {
//            @Override
//            protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
//            }
//
//            @Override
//            protected void connected(BaseDownloadTask task, String etag, boolean isContinue, int soFarBytes, int totalBytes) {
//            }
//
//            @Override
//            protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
//            }
//
//            @Override
//            protected void blockComplete(BaseDownloadTask task) {
//            }
//
//            @Override
//            protected void retry(final BaseDownloadTask task, final Throwable ex, final int retryingTimes, final int soFarBytes) {
//            }
//
//            @Override
//            protected void completed(BaseDownloadTask task) {
//            }
//
//            @Override
//            protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
//            }
//
//            @Override
//            protected void error(BaseDownloadTask task, Throwable e) {
//            }
//
//            @Override
//            protected void warn(BaseDownloadTask task) {
//            }
//        };

        final FileDownloadQueueSet queueSet = new FileDownloadQueueSet(downloadListener);
        final List<BaseDownloadTask> tasks = new ArrayList<>();

        downloadInfoList.forEach((url, localPath) -> {
            tasks.add(FileDownloader.getImpl()
                    .create(url)
                    .setPath(localPath)
                    .setTag(url));
        });


        queueSet.disableCallbackProgressTimes(); // Do not need for each task callback `FileDownloadListener#progress`,
// We just consider which task will complete. so in this way reduce ipc will be effective optimization.

// Each task will auto retry 1 time if download fail.
        queueSet.setAutoRetryTimes(1);



        if (isParallel) {
            // Start parallel download.
            queueSet.downloadTogether(tasks);
            // If your tasks are not a list, invoke such following will more readable:
//    queueSet.downloadTogether(
//            FileDownloader.getImpl().create(url).setPath(...),
//            FileDownloader.getImpl().create(url).setPath(...),
//            FileDownloader.getImpl().create(url).setSyncCallback(true)
//    );
        }else{
            // Start downloading in serial order.
            queueSet.downloadSequentially(tasks);
            // If your tasks are not a list, invoke such following will more readable:
//      queueSet.downloadSequentially(
//              FileDownloader.getImpl().create(url).setPath(...),
//              FileDownloader.getImpl().create(url).addHeader(...,...),
//              FileDownloader.getImpl().create(url).setPath(...)
//      );
        }
        queueSet.start();
    }
}
