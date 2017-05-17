package com.sdk.dyq.mapmodule.common;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import java.lang.reflect.Field;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class ThreadManager {

    /**
     * UI线程 handler
     **/
    private static Handler mUiHandler;
    private static final Object mMainHandlerLock = new Object();

    /**
     * AsyncTask的默认线程池Executor. 负责长时间的任务(网络访问) 默认3个线程
     */
    public static final Executor NETWORK_EXECUTOR;

    /**
     * 副线程的Handle, 只有一个线程 可以执行比较快但不能在ui线程执行的操作. 文件读写不建议在此线程执行,
     * 请使用FILE_THREAD_HANDLER 此线程禁止进行网络操作.如果需要进行网络操作. 请使用NETWORK_EXECUTOR
     */
    private static Handler SUB_THREAD1_HANDLER;
    private static Handler SUB_THREAD2_HANDLER;

    /**
     * 副线程1
     */
    private static HandlerThread SUB_THREAD1;
    /**
     * 副线程2
     */
    private static HandlerThread SUB_THREAD2;

    /**
     * 文件读写线程的Handle, 只有一个线程 可以执行文件读写操作, 如图片解码等 此线程禁止进行网络操作.如果需要进行网络操作.
     * 请使用NETWORK_EXECUTOR
     */
    private static Handler FILE_THREAD_HANDLER;
    /**
     * 文件读写用的线程
     */
    private static HandlerThread FILE_THREAD;

    static {
        NETWORK_EXECUTOR = initNetworkExecutor();
    }

    //region ================================== network thread 相关 ==================================

    private static Executor initNetworkExecutor() {
        Executor result;
        // 3.0以上
        if (Build.VERSION.SDK_INT >= 11) {
            //result = AsyncTask.THREAD_POOL_EXECUTOR;
            result = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<Runnable>());
        }
        // 3.0以下, 反射获取
        else {
            Executor tmp;
            try {
                Field field = AsyncTask.class.getDeclaredField("sExecutor");
                field.setAccessible(true);
                tmp = (Executor) field.get(null);
            } catch (Exception e) {
                tmp = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS,
                        new LinkedBlockingQueue<Runnable>());
            }
            result = tmp;
        }

        if (result instanceof ThreadPoolExecutor) {
            //线程数改为CPU 核数 +1
            ((ThreadPoolExecutor) result).setCorePoolSize(getPhoneCpuCoreNum() + 1);
        }

        return result;
    }

    /**
     * 获取手机Cpu核数
     *
     * @return 手机cpu核数
     * */
    public static int getPhoneCpuCoreNum(){
        if(Build.VERSION.SDK_INT >= 17) {
            int num = Runtime.getRuntime().availableProcessors();
            if(num <= 0){
                return 2;
            }
            return num;
        }else {
            return 2;
        }
    }

    /**
     * 取得UI线程Handler
     *
     * @return
     */
    public static Handler getMainHandler() {
        if (mUiHandler == null) {
            synchronized (mMainHandlerLock) {
//                if (mUiHandler == null) {
                mUiHandler = new Handler(Looper.getMainLooper());
//                }
            }
        }
        return mUiHandler;
    }

    /**
     * 在网络线程上执行异步操作. 该线程池负责网络请求等操作 长时间的执行(如网络请求使用此方法执行) 当然也可以执行其他 线程和AsyncTask公用
     *
     * @param run
     */
    public static void executeOnNetWorkThread(Runnable run) {
        try {
            NETWORK_EXECUTOR.execute(run);
        } catch (RejectedExecutionException e) {
        }
    }

    //endregion ================================== network thread 相关 ==================================

    //region ================================== file thread 相关 ==================================

    /**
     * 获得文件线程的Handler.<br>
     * 副线程可以执行本地文件读写等比较快但不能在ui线程执行的操作.<br>
     * <b>此线程禁止进行网络操作.如果需要进行网络操作. 请使用NETWORK_EXECUTOR</b>
     *
     * @return handler
     */
    public static Handler getFileThreadHandler() {
        if (FILE_THREAD_HANDLER == null) {
            synchronized (ThreadManager.class) {
                FILE_THREAD = new HandlerThread("FILE_RW");
                FILE_THREAD.setPriority(Thread.MIN_PRIORITY);//降低线程优先级
                FILE_THREAD.start();
                FILE_THREAD_HANDLER = new Handler(FILE_THREAD.getLooper());
            }
        }
        return FILE_THREAD_HANDLER;
    }

    /**
     * 在文件读写线程执行. <br>
     * 可以执行本地文件读写等比较快但不能在ui线程执行的操作.<br>
     * <b>此线程禁止进行网络操作.如果需要进行网络操作. 请使用NETWORK_EXECUTOR</b>
     *
     * @param run
     */
    public static void executeOnFileThread(Runnable run) {
        try {
            getFileThreadHandler().post(run);
        } catch (RejectedExecutionException e) {
        }
    }

    public static Looper getFileThreadLooper() {
        return getFileThreadHandler().getLooper();
    }

    //endregion ================================== file thread 相关 ==================================

    //region ================================== SUB_THREAD1 相关 ==================================
    public static Thread getSubThread1() {
        if (SUB_THREAD1 == null) {
            getSubThread1Handler();
        }
        return SUB_THREAD1;
    }

    /**
     * 获得副线程1的Handler.<br>
     * 副线程可以执行比较快但不能在ui线程执行的操作.<br>
     * 另外, 文件读写建议放到FileThread中执行 <b>此线程禁止进行网络操作.如果需要进行网络操作.
     * 请使用NETWORK_EXECUTOR</b>
     *
     * @return handler
     */
    public static Handler getSubThread1Handler() {
        if (SUB_THREAD1_HANDLER == null) {
            synchronized (ThreadManager.class) {
                SUB_THREAD1 = new HandlerThread("SUB1");
                SUB_THREAD1.setPriority(Thread.MIN_PRIORITY);//降低线程优先级
                SUB_THREAD1.start();
                SUB_THREAD1_HANDLER = new Handler(SUB_THREAD1.getLooper());
            }
        }
        return SUB_THREAD1_HANDLER;
    }

    public static Looper getSubThread1Looper() {
        return getSubThread1Handler().getLooper();
    }

    /**
     * 在副线程1执行. <br>
     * 可以执行本地文件读写等比较快但不能在ui线程执行的操作.<br>
     * <b>此线程禁止进行网络操作.如果需要进行网络操作. 请使用NETWORK_EXECUTOR</b>
     *
     * @return
     */
    public static void executeOnSubThread1(Runnable run) {
        getSubThread1Handler().post(run);
    }

    //endregion ================================== SUB_THREAD1 相关 ==================================

    //region ================================== SUB_THREAD2 相关 ==================================
    public static Thread getSubThread2() {
        if (SUB_THREAD2 == null) {
            getSubThread2Handler();
        }
        return SUB_THREAD2;
    }

    /**
     * 获得副线程2的Handler.<br>
     * 副线程可以执行比较快但不能在ui线程执行的操作.<br>
     * 另外, 文件读写建议放到FileThread中执行 <b>此线程禁止进行网络操作.如果需要进行网络操作.
     * 请使用NETWORK_EXECUTOR</b>
     *
     * @return handler
     */
    public static Handler getSubThread2Handler() {
        if (SUB_THREAD2_HANDLER == null) {
            synchronized (ThreadManager.class) {
                SUB_THREAD2 = new HandlerThread("SUB2");
                SUB_THREAD2.setPriority(Thread.MIN_PRIORITY);//降低线程优先级
                SUB_THREAD2.start();
                SUB_THREAD2_HANDLER = new Handler(SUB_THREAD2.getLooper());
            }
        }
        return SUB_THREAD2_HANDLER;
    }

    public static Looper getSubThread2Looper() {
        return getSubThread2Handler().getLooper();
    }

    /**
     * 在副线程2执行. <br>
     * 可以执行本地文件读写等比较快但不能在ui线程执行的操作.<br>
     * <b>此线程禁止进行网络操作.如果需要进行网络操作. 请使用NETWORK_EXECUTOR</b>
     *
     * @return
     */
    public static void executeOnSubThread2(Runnable run) {
        getSubThread2Handler().post(run);
    }
    //endregion ================================== SUB_THREAD2 相关 ==================================
}
