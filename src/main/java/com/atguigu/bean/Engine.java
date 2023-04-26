package com.atguigu.bean;

import cn.hutool.core.util.RandomUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Engine {

    private Status status;
    private Integer crankshaftSpeed;
    private Integer ConsumingRate;

    public enum Status {
        RUNNING(1),
        STOPPING(2);
        private final Integer value;

        Status(Integer value) {
            this.value = value;
        }

        public Integer getValue() {
            return value;
        }
    }

    public static Engine newInstance() {
        Engine engine = new Engine();
        engine.stop();
        return engine;
    }

    public void run() {
        this.status = Status.RUNNING;
        this.crankshaftSpeed = RandomUtil.randomInt(1000, 6000);
        this.ConsumingRate = RandomUtil.randomInt(200, 800);
    }

    public void stop() {
        this.status = Status.STOPPING;
        this.crankshaftSpeed = 0;
        this.ConsumingRate = 0;
    }
}

