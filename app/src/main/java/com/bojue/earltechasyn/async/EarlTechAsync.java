package com.bojue.earltechasyn.async;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.ArrayDeque;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

/**
 * Created by sendi on 2018/5/5.
 * 异步类
 */

public abstract class EarlTechAsync<P, R> implements IAsync<P, R> {

    private static final int DEFAULT_THREAD_COUNT = 5;
    private static final int RESULT = 1;
    private static final int CANCELED = 0;

    enum STATE {
        FINISHED,
        RUNNING,
        CANCLEED;
    }

    /*状态*/
    private STATE mSTATE = STATE.FINISHED;

    private P parameter;
    ExecutorService mExecutorService = null;

    FutureTask mFuture;
    Callable<R> mCallable;

    public EarlTechAsync() {
        mExecutorService = Executors.newFixedThreadPool(DEFAULT_THREAD_COUNT);

        mCallable = new Callable() {
            @Override
            public R call() throws Exception {

                R result = doThings(parameter);
                Message message = new Message();
                message.obj = new AsyncEntity(EarlTechAsync.this, result);
                message.what = RESULT;
                asyncHandler.sendMessage(message);

                return result;
            }
        };

        mFuture = new FutureTask(mCallable);

    }


    private static Handler asyncHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            AsyncEntity result = (AsyncEntity) msg.obj;
            switch (msg.what) {
                case RESULT:
                    result(result);
                    break;
                case CANCELED:
                    cancel(result);
                    break;
            }
        }
    };

    /*取消操作*/
    private static void cancel(AsyncEntity result) {
        result.mAsync.onCanceled();
        result.mAsync.onEnd(false);
    }

    /*操作结果*/
    private static void result(AsyncEntity result) {
        result.mAsync.onResult(result.data);
        result.mAsync.onEnd(true);
    }

    @Override
    public void onEnd(boolean isFinished) {
        if (isFinished) {
            mSTATE = STATE.FINISHED;
        } else {
            mSTATE = STATE.CANCLEED;
        }
    }

    public void execute(P parameter) {

        onPre();

        this.parameter = parameter;

        startExecute();
    }

    public void cancel(boolean ismayInterrupted){
        mFuture.cancel(ismayInterrupted);

        Message message=new Message();
        message.what=CANCELED;
        asyncHandler.sendMessage(message);
    }
    ArrayDeque<Runnable> tasks=new ArrayDeque<>();
    private void startExecute() {

        if (mSTATE == STATE.RUNNING) {
            throw new IllegalStateException("there has task running!");
        }
        mSTATE = STATE.RUNNING;

        tasks.offer(new Runnable() {
            @Override
            public void run() {
                try {
                    mFuture.run();
                }catch (Exception e){
                }finally {
                    scheduleNext();
                }
            }
        });

        scheduleNext();
    }

    private void scheduleNext() {
        Runnable mActive;
        if ((mActive=tasks.poll())!=null)
            mExecutorService.execute(mActive);
    }


    static class AsyncEntity<P, R> {

        IAsync<P, R> mAsync;
        R data;

        public AsyncEntity(IAsync<P, R> async, R data) {
            mAsync = async;
            this.data = data;
        }
    }

}
