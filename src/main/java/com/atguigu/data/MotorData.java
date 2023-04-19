package com.atguigu.data;

import lombok.Data;

@Data
public class MotorData {
    // 驱动电机序号
    private Integer id;
    // 驱动电机状态
    private Integer status;
    // 驱动电机控制器温度
    private Integer controllerTemperature;
    // 驱动电机转速
    private Integer rev;
    // 驱动电机转矩
    private Integer torque;
    // 驱动电机温度
    private Integer temperature;
    // 电机控制器输入电压
    private Integer voltage;
    // 电机控制器直流母线电流
    private Integer electricCurrent;
}
