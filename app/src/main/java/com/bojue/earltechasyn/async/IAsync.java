package com.bojue.earltechasyn.async;

/**
 * Created by sendi on 2018/5/5.
 *
 * 异步类的接口
 */
public interface IAsync<P,R> {

    /**
     * 前期准备
     */
    void onPre();

    /**
     * 执行任务
     * @param p:操作参数
     * @return
     */
    R doThings(P p);

    /**
     * 获得结果
     * @param r:结果
     */
    void onResult(R r);

    /**
     * 取消
     */
    void onCanceled();


    void onEnd(boolean isFinished);

}
