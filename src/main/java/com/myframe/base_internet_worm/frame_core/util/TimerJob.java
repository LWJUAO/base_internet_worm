package com.myframe.base_internet_worm.frame_core.util;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * 无回调的定时器任务
 */
public class TimerJob implements Job {

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        //操作对象
        InternetWormOption option = (InternetWormOption)jobExecutionContext.getJobDetail().getJobDataMap().get("InternetWormOption");
        try {
            option.runOnce();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
