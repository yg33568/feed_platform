package com.feed.util;

import org.springframework.stereotype.Component;
//生成不同的id
@Component
public class SnowflakeIdUtil {
    // 起始时间戳：2024-01-01
    private final long startTimestamp = 1704067200000L;

    private long workerId = 1;      // 机器ID（配置化）
    private long dataCenterId = 1;  // 数据中心ID
    private long sequence = 0L;

    private long lastTimestamp = -1L;

    public synchronized long nextId() {
        long timestamp = System.currentTimeMillis();

        if (timestamp < lastTimestamp) {
            throw new RuntimeException("时钟回拨");
        }

        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & 4095L;  // 12位序列号
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        return ((timestamp - startTimestamp) << 22)
                | (dataCenterId << 17)
                | (workerId << 12)
                | sequence;
    }

    private long tilNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }
}