package com.atguigu.bean;

import cn.hutool.core.util.RandomUtil;
import com.atguigu.data.MotorData;
import lombok.Data;

import java.util.List;

@Data
public class Motor {
    public Integer id;
    public Status status;
    public Integer temperature;
    public Integer rotatingSpeed;
    public Integer torque;
    public Integer controllerTemperature;
    public Double controllerVoltage;
    public Double controllerCurrent;
    public List<BatteryPack> batteryPackList;

    public void charging() {
        this.status = Status.CHARGING;
        this.rotatingSpeed = RandomUtil.randomInt(0, 20000);
        this.torque = RandomUtil.randomInt(0, 20000);
        this.controllerTemperature = RandomUtil.randomInt(20, 140);
        this.temperature = RandomUtil.randomInt(20, 140);
        renewStatus();

    }

    public void discharging() {
        this.status = Status.DISCHARGING;
        this.rotatingSpeed = RandomUtil.randomInt(20000, 40000);
        this.torque = RandomUtil.randomInt(20000, 40000);
        this.controllerTemperature = RandomUtil.randomInt(20, 140);
        this.temperature = RandomUtil.randomInt(20, 140);
        renewStatus();
    }

    public void stopped() {
        this.status = Status.STOPPING;
        this.rotatingSpeed = 0;
        this.torque = 0;
        this.controllerTemperature = RandomUtil.randomInt(20, 140);
        this.temperature = RandomUtil.randomInt(20, 140);
        renewStatus();
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private void renewStatus() {
        this.controllerCurrent = batteryPackList.stream().map(BatteryPack::getElectricCurrent).reduce(Double::sum).get();
        this.controllerVoltage = batteryPackList.stream().map(BatteryPack::getVoltage).reduce(Double::sum).get() / batteryPackList.size();
    }

    public static Motor newInstance(Integer id, List<BatteryPack> batteryPackList) {
        Motor motor = new Motor();
        motor.setId(id);
        motor.setBatteryPackList(batteryPackList);
        return motor;
    }

    public MotorData drawData() {
        MotorData motorData = new MotorData();
        motorData.setId(id);
        motorData.setStatus(status.getValue());
        motorData.setControllerTemperature(controllerTemperature);
        motorData.setRev(rotatingSpeed);
        motorData.setTorque(torque);
        motorData.setTemperature(temperature);
        motorData.setVoltage((int) (controllerVoltage * 10));
        motorData.setElectricCurrent((int) (controllerCurrent * 10));
        return motorData;
    }


    public enum Status {
        DISCHARGING(1),
        CHARGING(2),
        STOPPING(3),
        PREPARING(4);
        private final Integer value;

        Status(Integer value) {
            this.value = value;
        }

        public Integer getValue() {
            return value;
        }
    }

}
