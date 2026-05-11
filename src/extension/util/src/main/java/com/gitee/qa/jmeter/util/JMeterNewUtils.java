package com.gitee.qa.jmeter.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static java.time.temporal.ChronoUnit.SECONDS;

public class JMeterNewUtils {

    private static final Logger log = LoggerFactory.getLogger(JMeterNewUtils.class);

    /**
     * 计算两个时间戳的差值
     * */
    public static String TimeDifference(String startTimeString, String endTimeString, String format, String durationToType){
        // 定义日期时间格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);

        // 解析开始时间和结束时间
        LocalDateTime startTime = LocalDateTime.parse(startTimeString, formatter);
        LocalDateTime endTime = LocalDateTime.parse(endTimeString, formatter);

        // 计算时间差
        Duration duration = Duration.between(startTime, endTime);

        long time;
        // 将时间差转换为分钟并返回字符串格式
        if ("mm".equals(durationToType)) { // 分钟
            time = duration.toMinutes();
        } else if ("HH".equals(durationToType)) { // 小时
            time = duration.toHours();
        } else if ("dd".equals(durationToType)) { // 天
            time = duration.toDays();
        } else if ("ss".equals(durationToType)) { // 秒
            time = duration.get(SECONDS);
        } else { // 默认是秒
            time = duration.get(SECONDS);
        }
        return String.valueOf(time);
    }

}
