package com.lachesis.common.network;

import com.lachesis.common.network.bean.DownloaderConfigBean;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloadQueueSet;
import com.liulishuo.filedownloader.FileDownloader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by boxue.hao on 2017/9/22.
 */

public class HttpTool {
    public static void downloadFile2Path(String url, String localFilePath) {

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

    public static void downloadFiles(List<DownloaderConfigBean> downloaderConfigList, final FileDownloadListener downloadListener) {

        boolean isParallel = false; //是否并行下载

        final FileDownloadQueueSet queueSet = new FileDownloadQueueSet(downloadListener);
        final List<BaseDownloadTask> tasks = new ArrayList<>();

        for (int i = 0; i < downloaderConfigList.size(); i++) {
            DownloaderConfigBean config = downloaderConfigList.get(i);
            tasks.add(FileDownloader.getImpl()
                    .create(config.getRemotePath())
                    .setPath(config.getLocalPath())
                    .setTag(config.getTag()));
        }
        queueSet.disableCallbackProgressTimes();
        queueSet.setAutoRetryTimes(1);

        if (isParallel) {
            queueSet.downloadTogether(tasks);
        } else {
            queueSet.downloadSequentially(tasks);
        }
        queueSet.start();
    }


}
