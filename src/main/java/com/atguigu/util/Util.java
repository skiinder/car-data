package com.atguigu.util;

import cn.hutool.core.util.RandomUtil;

public class Util {

    /**
     * 根据挡位状态获取编码
     *
     * @param hasDrivingForce 是否有驱动力
     * @param hasBrakingForce 是否有制动力
     * @param gear            挡位：0.空挡 1-12.1档到12档 13.倒挡 14.D档 15.P档
     * @return 挡位编码
     */
    public static Integer getGear(Boolean hasDrivingForce, Boolean hasBrakingForce, Integer gear) {
        return gear + (hasBrakingForce ? 16 : 0) + (hasDrivingForce ? 32 : 0);
    }

    /**
     * 获取报警标指
     *
     * @return 随机有效的报警数值
     */
    public static Integer getAlarmSign() {
        return RandomUtil.randomDouble() < 0.95 ? 0 : RandomUtil.randomInt(1, 512 * 1024);
    }
}
