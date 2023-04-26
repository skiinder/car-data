package com.atguigu.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BatteryPack {
    private Integer id;
    private List<Battery> batteries;
    private Double voltage;
    private Double electricCurrent;
    private Double temperature;
    private Double soc;

    private Double maxVoltage;
    private Integer maxVoltageId;
    private Double maxElectricCurrent;
    private Integer maxElectricCurrentId;
    private Double maxTemperature;
    private Integer maxTemperatureId;

    private Double minVoltage;
    private Integer minVoltageId;
    private Double minElectricCurrent;
    private Integer minElectricCurrentId;
    private Double minTemperature;
    private Integer minTemperatureId;

    public static BatteryPack newInstance(Integer id) {
        List<Battery> batteries = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            batteries.add(Battery.newInstance(i));
        }
        BatteryPack batteryPack = new BatteryPack();
        batteryPack.setId(id);
        batteryPack.setBatteries(batteries);
        batteryPack.renewStatus();
        return batteryPack;
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private void renewStatus() {
        this.voltage = batteries.stream().map(Battery::getVoltage).reduce(Double::sum).get()/ batteries.size();
        this.electricCurrent = batteries.stream().map(Battery::getCurrent).reduce(Double::sum).get();
        this.temperature = batteries.stream().map(Battery::getTemperature).reduce(Double::sum).get() / batteries.size();
        this.soc = batteries.stream().map(Battery::getSoc).reduce(Double::sum).get() / batteries.size();

        Battery maxCurrent = batteries.stream().max(Comparator.comparing(Battery::getCurrent)).get();
        this.maxElectricCurrent = maxCurrent.getCurrent();
        this.maxElectricCurrentId = maxCurrent.getId();

        Battery maxVoltage = batteries.stream().max(Comparator.comparing(Battery::getVoltage)).get();
        this.maxVoltage = maxVoltage.getVoltage();
        this.maxVoltageId = maxVoltage.getId();

        Battery maxTemperature = batteries.stream().max(Comparator.comparing(Battery::getTemperature)).get();
        this.maxTemperature = maxTemperature.getTemperature();
        this.maxTemperatureId = maxTemperature.getId();

        Battery minCurrent = batteries.stream().min(Comparator.comparing(Battery::getCurrent)).get();
        this.minElectricCurrent = minCurrent.getCurrent();
        this.minElectricCurrentId = minCurrent.getId();

        Battery minVoltage = batteries.stream().min(Comparator.comparing(Battery::getVoltage)).get();
        this.minVoltage = minVoltage.getVoltage();
        this.minVoltageId = minVoltage.getId();

        Battery minTemperature = batteries.stream().min(Comparator.comparing(Battery::getTemperature)).get();
        this.minTemperature = minTemperature.getTemperature();
        this.minTemperatureId = minTemperature.getId();
    }

    public void charge(long seconds) {
        batteries.forEach(battery -> battery.charge(seconds));
        renewStatus();
    }

    public void discharge(long seconds) {
        batteries.forEach(battery -> battery.discharge(seconds));
        renewStatus();
    }

    public void tie() {
        batteries.forEach(Battery::tie);
        renewStatus();
    }
}
