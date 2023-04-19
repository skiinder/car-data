package com.atguigu.bean;

import cn.hutool.core.util.RandomUtil;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Battery {
    private Integer id;
    private Double voltage;
    private Double electricCurrent;
    private Double temperature;
    private Double soc;

    public void charge(long seconds) {
        soc += seconds / 60D;
        temperature += (70 - temperature) * seconds / 140 / 60;
        electricCurrent = RandomUtil.randomDouble(1.3, 1.7);
        voltage = (4.2 - 2.9) * soc / 100 + 2.9;
    }

    public void tie() {
        temperature -= (temperature - 25) / 10;
        electricCurrent = 0D;
    }

    public void discharge(long seconds) {
        soc -= seconds / 600D;
        temperature += (70 - temperature) * seconds / 560 / 60;
        electricCurrent = RandomUtil.randomDouble(0.13, 0.17);
        voltage = (4.2 - 2.9) * soc / 100 + 2.9;
    }

    public static Battery newInstance(Integer id) {
        double soc = RandomUtil.randomDouble(30.0, 60.0);
        double voltage = (4.2 - 2.9) * soc / 100 + 2.9;
        return new Battery(id, voltage, 0D, RandomUtil.randomDouble(35, 60), soc);

    }

}
