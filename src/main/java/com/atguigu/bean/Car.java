package com.atguigu.bean;

import cn.hutool.core.util.RandomUtil;
import com.atguigu.data.MotorData;
import com.atguigu.data.RealtimeData;
import com.atguigu.util.Util;
import lombok.Data;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Data
public class Car {
    private String vin;
    private CarStatus status;
    private ChargingStatus chargingStatus;
    private DrivingMode drivingMode;
    private Double soc;
    private Integer mileage;
    private Integer velocity;
    private Gear gear;

    public List<BatteryPack> batteryPackList;
    public List<Motor> motorList;
    public Engine engine;

    public void running(long seconds) {
        status = CarStatus.RUNNING;
        renewStatus();
        velocity = RandomUtil.randomInt(400, 1200);
        mileage += (int) (velocity * seconds / 360.0);
        switch (chargingStatus) {
            case RUNNING_CHARGING:
                batteryPackList.forEach(batteryPack -> batteryPack.charge(seconds));
                break;
            case NOT_CHARGING:
                if (drivingMode == DrivingMode.FUEL) {
                    batteryPackList.forEach(BatteryPack::tie);
                } else {
                    batteryPackList.forEach(batteryPack -> batteryPack.discharge(seconds));
                }
                break;
            default:
                throw new RuntimeException("非法充电状态！汽车行式状态：" + status + "，充电状态" + chargingStatus);
        }
        switch (drivingMode) {
            case FUEL:
                motorList.forEach(motor -> {
                    if (chargingStatus == ChargingStatus.RUNNING_CHARGING) {
                        motor.charging();
                    } else {
                        motor.stopped();
                    }
                });
                engine.run();
                break;
            case HYBRID:
                motorList.forEach(Motor::discharging);
                engine.run();
                break;
            case ELECTRICITY:
                motorList.forEach(Motor::discharging);
                engine.stop();
                break;
            default:
                throw new RuntimeException("非法驾驶状态！汽车行式状态：" + status + "，驾驶状态：" + drivingMode);
        }
    }

    public void parking(long seconds) {
        status = CarStatus.PARKING;
        renewStatus();
        velocity = 0;
        engine.stop();
        motorList.forEach(Motor::stopped);
        switch (chargingStatus) {
            case CHARGING:
                batteryPackList.forEach(batteryPack -> batteryPack.charge(seconds));
                break;
            case NOT_CHARGING:
            case CHARGING_FINISHED:
                batteryPackList.forEach(BatteryPack::tie);
                break;
            default:
                throw new RuntimeException("非法充电状态！汽车行式状态：" + status + "，充电状态：" + chargingStatus);
        }

    }

    public void malfunction() {
        status = CarStatus.MALFUNCTION;
        renewStatus();
        velocity = 0;
        engine.stop();
        motorList.forEach(Motor::stopped);
        batteryPackList.forEach(BatteryPack::tie);
    }

    public void repeat(long seconds) {
        switch (status) {
            case RUNNING:
                running(seconds);
                return;
            case PARKING:
                parking(seconds);
                return;
            case MALFUNCTION:
                malfunction();
        }
    }

    public void change(long seconds) {
        switch (status) {
            case RUNNING:
                if (RandomUtil.randomDouble() < 0.95) {
                    parking(seconds);
                } else {
                    malfunction();
                }
                return;
            case PARKING:
                if (RandomUtil.randomDouble() < 0.95) {
                    running(seconds);
                } else {
                    malfunction();
                }
                return;
            case MALFUNCTION:
                if (RandomUtil.randomBoolean()) {
                    running(seconds);
                } else {
                    parking(seconds);
                }
        }
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private void renewStatus() {
        soc = batteryPackList.stream().map(BatteryPack::getSoc).reduce(Double::sum).get() / batteryPackList.size();

        switch (status) {
            case RUNNING:
                if (soc <= 10) {
                    chargingStatus = ChargingStatus.RUNNING_CHARGING;
                    drivingMode = DrivingMode.FUEL;
                } else if (soc >= 70 || chargingStatus == ChargingStatus.CHARGING || chargingStatus == ChargingStatus.CHARGING_FINISHED || chargingStatus == null) {
                    chargingStatus = ChargingStatus.NOT_CHARGING;
                }
                if (drivingMode == null) {
                    drivingMode = RandomUtil.randomBoolean() ? DrivingMode.HYBRID : DrivingMode.ELECTRICITY;
                }
                gear = Gear.DRIVE;
                break;
            case PARKING:
                if (soc <= 10) {
                    chargingStatus = ChargingStatus.CHARGING;
                } else if (soc >= 90) {
                    chargingStatus = ChargingStatus.CHARGING_FINISHED;
                } else if (chargingStatus == ChargingStatus.RUNNING_CHARGING || chargingStatus == null) {
                    chargingStatus = ChargingStatus.NOT_CHARGING;
                }
                drivingMode = null;
                gear = Gear.PARK;
                break;
            case MALFUNCTION:
                chargingStatus = ChargingStatus.NOT_CHARGING;
                drivingMode = null;
                gear = Gear.PARK;
                break;
        }
    }

    public static Car newInstance(String vin) {
        Car car = new Car();
        car.setVin(vin);
        car.setMileage(RandomUtil.randomInt(2000, 1000000));
        car.setEngine(Engine.newInstance());
        List<BatteryPack> batteryPackList = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            batteryPackList.add(BatteryPack.newInstance(i));
        }
        car.setBatteryPackList(batteryPackList);
        List<Motor> motorList = new ArrayList<>();
        for (int i = 1; i < 3; i++) {
            motorList.add(Motor.newInstance(i, batteryPackList));
        }
        car.setMotorList(motorList);
        if (RandomUtil.randomBoolean()) {
            car.running(30);
        } else {
            car.parking(30);
        }
        return car;
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public RealtimeData drawData(Date timestamp) {
        RealtimeData realtimeData = new RealtimeData();
        realtimeData.setVin(this.getVin());
        realtimeData.setTimestamp(timestamp);
        realtimeData.setCarStatus(status.getValue());
        realtimeData.setChargeStatus(chargingStatus.getValue());
        if (drivingMode == null) {
            realtimeData.setExecutionMode(null);
        } else {
            realtimeData.setExecutionMode(drivingMode.getValue());
        }
        realtimeData.setVelocity(this.velocity);
        realtimeData.setMileage(this.mileage);
        realtimeData.setVoltage((int) (this.batteryPackList.stream().map(BatteryPack::getVoltage).reduce(Double::sum).get() * 10));
        realtimeData.setElectricCurrent((int) (this.batteryPackList.stream().map(BatteryPack::getElectricCurrent).reduce(Double::sum).get() * 10));
        realtimeData.setSoc(soc.intValue());
        realtimeData.setDcStatus(RandomUtil.randomInt(1, 3));
        realtimeData.setGear(gear.getValue());
        realtimeData.setInsulationResistance(RandomUtil.randomInt(10000, 40000));
        realtimeData.setMotorCount(motorList.size());

        List<MotorData> motorDataList = new ArrayList<>();
        motorList.forEach(motor -> motorDataList.add(motor.drawData()));
        realtimeData.setMotorList(motorDataList);

        realtimeData.setEngineStatus(engine.getStatus().getValue());
        realtimeData.setCrankshaftSpeed(engine.getCrankshaftSpeed());
        realtimeData.setFuelConsumeRate(engine.getConsumingRate());

        BatteryPack maxBatteryPack = batteryPackList.stream().max(Comparator.comparing(BatteryPack::getMaxVoltage)).get();
        realtimeData.setMaxVoltageBatteryPackId(maxBatteryPack.getId());
        realtimeData.setMaxVoltageBatteryId(maxBatteryPack.getMaxVoltageId());
        realtimeData.setMaxVoltage((int) (maxBatteryPack.getMaxVoltage() * 10));
        BatteryPack minBatteryPack = batteryPackList.stream().min(Comparator.comparing(BatteryPack::getMinVoltage)).get();
        realtimeData.setMinVoltageBatteryPackId(minBatteryPack.getId());
        realtimeData.setMinVoltageBatteryId(minBatteryPack.getMinVoltageId());
        realtimeData.setMinVoltage((int) (minBatteryPack.getMinVoltage() * 10));

        BatteryPack maxTemp = batteryPackList.stream().max(Comparator.comparing(BatteryPack::getMaxTemperature)).get();
        realtimeData.setMaxTemperatureSubsystemId(maxTemp.getId());
        realtimeData.setMaxTemperatureProbeId(maxTemp.getMaxTemperatureId());
        realtimeData.setMaxTemperature((int) (maxTemp.getMaxTemperature() * 10));
        BatteryPack minTemp = batteryPackList.stream().min(Comparator.comparing(BatteryPack::getMinTemperature)).get();
        realtimeData.setMinTemperatureSubsystemId(minTemp.getId());
        realtimeData.setMinTemperatureProbeId(minTemp.getMinTemperatureId());
        realtimeData.setMinTemperature((int) (minTemp.getMinTemperature() * 10));

        Integer alarmSign = Util.getAlarmSign();
        realtimeData.setAlarmLevel(alarmSign == 0 ? 0 : RandomUtil.randomInt(1, 4));
        realtimeData.setAlarmSign(alarmSign);
        realtimeData.setCustomBatteryAlarmCount(3);
        realtimeData.setCustomBatteryAlarmList(Arrays.asList(1, 2, 3));
        realtimeData.setCustomEngineAlarmCount(4);
        realtimeData.setCustomEngineAlarmList(Arrays.asList(1, 2, 3, 4));
        realtimeData.setCustomMotorAlarmCount(5);
        realtimeData.setCustomMotorAlarmList(Arrays.asList(1, 2, 3, 4, 5));
        realtimeData.setOtherAlarmCount(0);
        realtimeData.setOtherAlarmList(Collections.EMPTY_LIST);

        realtimeData.setBatteryPackCount(batteryPackList.size());
        realtimeData.setBatteryCount(batteryPackList.stream().map(batteryPack -> batteryPack.getBatteries().size()).reduce(Integer::sum).get());
        realtimeData.setBatteryVoltages(batteryPackList.stream().flatMap(batteryPack -> batteryPack.getBatteries().stream().map(Battery::getVoltage)).map(v -> (int) (v * 10)).collect(Collectors.toList()));
        realtimeData.setBatteryPackTemperatureCount(realtimeData.getBatteryPackCount());
        realtimeData.setBatteryTemperatureProbeCount(realtimeData.getBatteryCount());
        realtimeData.setBatteryTemperatures(batteryPackList.stream().flatMap(batteryPack -> batteryPack.getBatteries().stream().map(Battery::getTemperature)).map(Double::intValue).collect(Collectors.toList()));


        return realtimeData;
    }


    public enum CarStatus {
        RUNNING(1),
        PARKING(2),
        MALFUNCTION(3);
        private final int value;

        CarStatus(Integer value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public enum ChargingStatus {
        CHARGING(1),
        RUNNING_CHARGING(2),
        NOT_CHARGING(3),
        CHARGING_FINISHED(4);
        private final int value;

        ChargingStatus(Integer value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public enum DrivingMode {
        ELECTRICITY(1),
        HYBRID(2),
        FUEL(3);
        private final int value;

        DrivingMode(Integer value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public enum Gear {
        NONE(0),
        ONE(1),
        TWO(2),
        THREE(3),
        FOUR(4),
        FIVE(5),
        REVERSE(13),
        DRIVE(14),
        PARK(15);
        private final int value;

        Gear(Integer value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

}
