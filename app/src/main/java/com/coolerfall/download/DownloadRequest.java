package com.coolerfall.download;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * DownloadRequest: download request, this is designed according to Request in Andoird-Volley.
 *
 * @author Vincent Cheung
 * @since Nov. 24, 2014
 */
public class DownloadRequest implements Comparable<DownloadRequest> {
	/**
	 * Bit flag for {@link #setAllowedNetworkTypes} corresponding to
	 * {@link android.net.ConnectivityManager#TYPE_MOBILE}.
	 */
	public static final int NETWORK_MOBILE = 1;
	/**
	 * Bit flag for {@link #setAllowedNetworkTypes} corresponding to
	 * {@link android.net.ConnectivityManager#TYPE_WIFI}.
	 */
	public static final int NETWORK_WIFI = 1 << 1;
	private static final String TAG = DownloadRequest.class.getSimpleName();
	/**
	 * default download directory
	 */
	private static final String DEFAULT_DIR = Environment
		.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
		.getAbsolutePath();
	/**
	 * Download id of this download request.
	 */
	private int mDownloadId = -1;

	/**
	 * Retry TIME_IN_MILLISECOND when downloading failed, default is 1.
	 */
	private AtomicInteger mRetryTime = new AtomicInteger(1);

	/**
	 * Allowed network types, default to all network types allowed.
	 */
	private int mAllowedNetworkTypes = 0;

	/**
	 * The context used in {@link DownloadDispatcher}.
	 */
	private Context mContext;

	/**
	 * The download state.
	 */
	private DownloadState mDownloadState;

	/**
	 * URL of download request.
	 */
	private String mUrl;

	/**
	 * Destination directory to save file.
	 */
	private String mDestinationDir;

	/**
	 * Destination file path.
	 */
	private String mDestinationFilePath;

	/**
	 * Progress interval, how long should {@link DownloadDispatcher}
	 * invoke {@link DownloadListener#onProgress(int, long, long)}.
	 */
	private int mProgressInterval;

	/**
	 * Download request queue.
	 */
	private DownloadRequestQueue mDownloadRequestQueue;

	/**
	 * Timestamp of this download request when created.
	 */
	private long mTimestamp = System.currentTimeMillis() / 1000;

	/**
	 * The priority of this download request, normal by default.
	 */
	private Priority mPriority = Priority.NORMAL;

	/**
	 * Whether or not this request has been canceled.
	 */
	private boolean mCanceled = false;

	/**
	 * Download listener.
	 */
	private DownloadListener mDownloadListener;

	/**
	 * Simple download listener.
	 */
	private SimpleDownloadListener mSimpleDownloadListener;

	private String mFileName;

	/**
	 * The default constructor, set the download state as pending.
	 */
	public DownloadRequest() {
		mDownloadState = DownloadState.PENDING;
	}

	public String getFileName() {
		return mFileName;
	}

	public DownloadRequest setFileName(String mFileName) {
		this.mFileName = mFileName;

		return this;
	}

	@Override
	public int compareTo(DownloadRequest other) {
		Priority left = this.getPriority();
		Priority right = other.getPriority();

		/*
		 * High-priority requests are "lesser" so they are sorted to the front.
		 * Equal priorities are sorted by timestamp to provide FIFO ordering.
		 */
		return left == right ?
			(int) (this.mTimestamp - other.mTimestamp) :
			right.ordinal() - left.ordinal();
	}

	/**
	 * Get the priority of download request.
	 *
	 * @return {@link Priority#NORMAL} by default.
	 */
	protected Priority getPriority() {
		return mPriority;
	}

	/**
	 * Set the priority of this downloader.
	 *
	 * @param priority {@link Priority}
	 * @return this Request object to allow for chaining
	 */
	public DownloadRequest setPriority(Priority priority) {
		mPriority = priority;

		return this;
	}

	/**
	 * Get the download listener of this request.
	 *
	 * @return download listener
	 */
	protected DownloadListener getDownloadListener() {
		return mDownloadListener;
	}

	/**
	 * Set the download listener.
	 *
	 * @param l download listener
	 * @return this Request object to allow for chaining
	 */
	public DownloadRequest setDownloadListener(DownloadListener l) {
		mDownloadListener = l;

		return this;
	}

	/**
	 * Get the simple download listener of this request.
	 *
	 * @return simple download listener
	 */
	protected SimpleDownloadListener getSimpleDownloadListener() {
		return mSimpleDownloadListener;
	}

	/**
	 * Set simple download listener.
	 *
	 * @param sl simple download listener
	 * @return this Request object to allow for chaining
	 */
	public DownloadRequest setSimpleDownloadListener(SimpleDownloadListener sl) {
		mSimpleDownloadListener = sl;

		return this;
	}

	/**
	 * Associates this request with the given queue. The request queue will be
	 * notified when this request has finished.
	 *
	 * @param queue download request queue
	 * @return this Request object to allow for chaining
	 */
	protected DownloadRequest setDownloadQueue(DownloadRequestQueue queue) {
		mDownloadRequestQueue = queue;

		return this;
	}

	/**
	 * Get download state of current request.
	 *
	 * @return download state
	 */
	protected DownloadState getDownloadState() {
		return mDownloadState;
	}

	/**
	 * Set download state of this request.
	 *
	 * @param state download state
	 */
	protected void setDownloadState(DownloadState state) {
		mDownloadState = state;
	}

	/**
	 * Get the download id of this download request.
	 *
	 * @return download id
	 */
	protected int getDownloadId() {
		return mDownloadId;
	}

	/**
	 * Set download id of this download request.
	 *
	 * @param downloadId download id
	 * @return download request
	 */
	public DownloadRequest setDownloadId(int downloadId) {
		mDownloadId = downloadId;

		return this;
	}

	/**
	 * Get retry TIME_IN_MILLISECOND, the retry TIME_IN_MILLISECOND will decrease automatically
	 * after invoking this method.
	 *
	 * @return retry TIME_IN_MILLISECOND
	 */
	protected int getRetryTime() {
		return mRetryTime.decrementAndGet();
	}

	/**
	 * Set retry TIME_IN_MILLISECOND, the manager will re-download with retry TIME_IN_MILLISECOND.
	 *
	 * @param retryTime retry TIME_IN_MILLISECOND
	 * @return this Request object to allow for chaining
	 */
	public DownloadRequest setRetryTime(int retryTime) {
		mRetryTime = new AtomicInteger(retryTime);

		return this;
	}

	/**
	 * Get progress interval, used in {@link DownloadDispatcher}.
	 *
	 * @return progress interval
	 */
	protected int getProgressInterval() {
		return mProgressInterval;
	}

	/**
	 * Set progress interval for this download request.
	 *
	 * @param millisec interval in millisecond
	 * @return this Request object to allow for chaining
	 */
	public DownloadRequest setProgressInterval(int millisec) {
		mProgressInterval = millisec;
		return this;
	}

	/**
	 * Restrict the types of networks over which this download may proceed.
	 * By default, all network types are allowed.
	 * Be sure to add permission android.permission.ACCESS_NETWORK_STATE.
	 *
	 * @param context the context to use
	 * @param types   any network type
	 * @return this Request object to allow for chaining
	 */
	public DownloadRequest setAllowedNetworkTypes(Context context, int types) {
		mContext = context;
		mAllowedNetworkTypes = types;

		return this;
	}

	/**
	 * Get the types of allowed network.
	 *
	 * @return all the types
	 */
	protected int getAllowedNetworkTypes() {
		return mAllowedNetworkTypes;
	}

	/**
	 * Get the context.
	 *
	 * @return context
	 */
	protected Context getContext() {
		return mContext;
	}

	/**
	 * Get the URL of this request.
	 *
	 * @return the URL of this request
	 */
	protected String getUrl() {
		return mUrl;
	}

	/**
	 * Set the URL of this download request.
	 *
	 * @param url the url
	 * @return this Request object to allow for chaining.
	 */
	public DownloadRequest setUrl(String url) {
		if (TextUtils.isEmpty(url)) {
			throw new IllegalArgumentException("url cannot be null");
		}

		if (!url.startsWith("http") && !url.startsWith("https")) {
			throw new IllegalArgumentException("can only download 'HTTP/HTTPS' url");
		}

		mUrl = url;

		return this;
	}

	/* get absolute file path according to the directory */
	private String getFilePath() {
		String dir = TextUtils.isEmpty(mDestinationDir) ? DEFAULT_DIR : mDestinationDir;
		return dir + File.separator + mFileName;
//		return dir + File.separator + DownloadUtils.getFilenameFromHeader(mUrl);
	}

	/**
	 * Set absolute destination directory for this download request.
	 * If {@link #setDestFilePath(String)} was used, then destination directory will
	 * be ignored. The directory will be created if not existed.
	 * The name of file will be generated from url or http header.
	 *
	 * @param dir destination directory
	 * @return this Request object to allow for chaining
	 * @see #setDestFilePath(String)
	 */
	public DownloadRequest setDestDirectory(String dir) {
		mDestinationDir = dir;
		return this;
	}

	/**
	 * Get destination file path of this download request.
	 *
	 * @return destination file path
	 */
	@SuppressWarnings("ResultOfMethodCallIgnored")
	protected String getDestFilePath() {
		/* if the destination file path is empty, use default file path */
		if (TextUtils.isEmpty(mDestinationFilePath)) {
			mDestinationFilePath = getFilePath();
		}

		/* if the destination path is directory */
		File file = new File(mDestinationFilePath);
		if (file.isDirectory()) {
			Log.w(TAG, "the destination file path cannot be directory");
			return getFilePath();
		} else if (!file.getParentFile().exists()) {
			/* make dirs in case */
			file.getParentFile().mkdirs();
		}

		return mDestinationFilePath;
	}

	/**
	 * Set destination file path of this download request. The file will be createad
	 * according to the file path. This file path must be absolute file
	 * path(such as: /sdcard/test.txt). If the filename is not certain, then use
	 * {@link #setDestDirectory(String)}, the download manager will genrate filename from url.
	 *
	 * @param filePath destination file path
	 * @return this Request object to allow for chaining
	 * @see #setDestDirectory(String)
	 */
	public DownloadRequest setDestFilePath(String filePath) {
		mDestinationFilePath = filePath;
		return this;
	}

	/**
	 * Get temporary destination file path of this download request.
	 *
	 * @return temporary destination file path
	 */
	protected String getTmpDestinationPath() {
		return getDestFilePath() + ".tmp";
	}

	/**
	 * Mark this download request as canceled.  No callback will be delivered.
	 */
	protected void cancel() {
		mCanceled = true;
	}

	/**
	 * To check if current request has canceled.
	 *
	 * @return Returns true if this request has been canceled.
	 */
	protected boolean isCanceled() {
		return mCanceled;
	}

	/**
	 * Notifies the download request queue that this request has finished(succesfully or fail)
	 */
	protected void finish() {
		if (mDownloadRequestQueue != null) {
			mDownloadRequestQueue.finish(this);
		}
	}

	/**
	 * Priority values: download request will be processed from
	 * higher priorities to lower priorities.
	 */
	public enum Priority {
		/**
		 * The lowest priority.
		 */
		LOW,
		/**
		 * Normal priority(default).
		 */
		NORMAL,
		/**
		 * The highest priority.
		 */
		HIGH,
	}

	/**
	 * State valuse: this will used to mark the state of download request.
	 */
	protected enum DownloadState {
		/**
		 * State invalid(the request is not in queue).
		 */
		INVALID,
		/**
		 * State when the download is currently pending.
		 */
		PENDING,
		/**
		 * State when the download is currently running.
		 */
		RUNNING,
		/**
		 * State when the download is successful.
		 */
		SUCCESSFUL,
		/**
		 * State when the download is failed.
		 */
		FAILURE,
	}
}
