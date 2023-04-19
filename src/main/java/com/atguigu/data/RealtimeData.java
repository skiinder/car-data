package com.atguigu.data;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class RealtimeData {

    // 车辆编码
    private String vin;
    // 数据采集时间
    private Date timestamp;

    // 整车状态
    // 车辆状态
    private Integer carStatus;
    // 充电状态
    private Integer chargeStatus;
    // 运行模式
    private Integer executionMode;
    // 车速
    private Integer velocity;
    // 里程
    private Integer mileage;
    // 总电压
    private Integer voltage;
    // 总电流
    private Integer electricCurrent;
    // SOC
    private Integer soc;
    // DC-DC状态
    private Integer dcStatus;
    // 挡位
    private Integer gear;
    // 绝缘电阻
    private Integer insulationResistance;

    // 驱动电机数据
    // 驱动电机个数
    private Integer motorCount;
    // 驱动电机列表
    private List<MotorData> motorList;

    // 燃料电池
    // 燃料电池电压
    private Integer fuelCellVoltage;
    // 燃料电池电流
    private Integer fuelCellCurrent;
    // 燃料消耗率
    private Integer fuelCellConsumeRate;
    // 燃料电池温度探针总数
    private Integer fuelCellTemperatureProbeCount;
    // 燃料电池温度值
    private Integer fuelCellTemperature;
    // 氢系统中最高温度
    private Integer fuelCellMaxTemperature;
    // 氢系统中 最高温度探针号
    private Integer fuelCellMaxTemperatureProbeId;
    // 氢气最高浓度
    private Integer fuelCellMaxHydrogenConsistency;
    // 氢气最高浓度传感器代号
    private Integer fuelCellMaxHydrogenConsistencyProbeId;
    // 氢气最高压力
    private Integer fuelCellMaxHydrogenPressure;
    // 氢气最高压力传感器代号
    private Integer fuelCellMaxHydrogenPressureProbeId;
    // 高压DC-DC状态
    private Integer fuelCellDcStatus;

    // 发动机
    // 发动机状态
    private Integer engineStatus;
    // 曲轴转速
    private Integer crankshaftSpeed;
    // 燃料消耗率
    private Integer fuelConsumeRate;

    // 极值数据
    // 最高电压电池子系统号
    private Integer MaxVoltageBatteryPackId;
    // 最高电压电池单体代号
    private Integer MaxVoltageBatteryId;
    // 电池单体电压最高值
    private Integer MaxVoltage;
    // 最低电压电池子系统号
    private Integer MinVoltageBatteryPackId;
    // 最低电压电池单体代号
    private Integer MinVoltageBatteryId;
    // 电低单体电压最高值
    private Integer MinVoltage;
    // 最高温度子系统号
    private Integer MaxTemperatureSubsystemId;
    // 最高温度探针号
    private Integer MaxTemperatureProbeId;
    // 最高温度值
    private Integer MaxTemperature;
    // 最低温度子系统号
    private Integer MinTemperatureSubsystemId;
    // 最低温度探针号
    private Integer MinTemperatureProbeId;
    // 最低温度值
    private Integer MinTemperature;

    // 报警数据
    // 最高报警等级
    private Integer alarmLevel;
    // 通用报警标志
    private Integer alarmSign;
    // 可充电储能装置故障总数N1
    private Integer customBatteryAlarmCount;
    // 可充电储能装置故障代码列表
    private List<Integer> customBatteryAlarmList;
    // 驱动电机故障总数N2
    private Integer customMotorAlarmCount;
    // 驱动电机故障代码列表
    private List<Integer> customMotorAlarmList;
    // 发动机故障总数N3
    private Integer customEngineAlarmCount;
    // 发动机故障代码列表
    private List<Integer> customEngineAlarmList;
    // 其他故障总数N4
    private Integer otherAlarmCount;
    // 其他故障代码列表
    private List<Integer> otherAlarmList;

    // 单体电池电压
    // 单体电池总数
    private Integer batteryCount;
    // 单体电池包总数
    private Integer batteryPackCount;
    // 单体电池电压值列表
    private List<Integer> batteryVoltages;

    // 单体电池温度
    // 单体电池温度探针总数
    private Integer batteryTemperatureProbeCount;
    // 单体电池包总数
    private Integer batteryPackTemperatureCount;
    // 单体电池温度值列表
    private List<Integer> batteryTemperatures;
}
