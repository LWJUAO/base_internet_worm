package com.myframe.base_internet_worm.frame_core.util;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * 有回调的定时器任务
 */
public class TimerJobCallBack implements Job {

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        //操作对象
        InternetWormOption option = (InternetWormOption)jobExecutionContext.getJobDetail().getJobDataMap().get("InternetWormOption");
        //获取回到参数
        IWDataCallBackIF callBackIF = (IWDataCallBackIF)jobExecutionContext.getJobDetail().getJobDataMap().get("IWDataCallBackIF");
        try {
            option.runOnce(callBackIF);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
