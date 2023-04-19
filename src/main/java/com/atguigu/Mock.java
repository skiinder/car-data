package com.atguigu;

import cn.hutool.core.util.RandomUtil;
import com.atguigu.bean.Car;
import com.atguigu.data.RealtimeData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class Mock {
    private Date startDate;
    private Integer count;
    private Integer carCount;
    private String dataPath;
    private String jdbcUrl;
    private String jdbcUsername;
    private String jdbcPassword;
    private Logger LOGGER;
    private List<Car> cars;

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        new Mock().run(args);
        long stop = System.currentTimeMillis();
        System.err.println("共用时" + (stop - start) + "ms");
    }

    private void run(String[] args) {
        parseArgs(args);
        getLogger();
        getCars();
        writeData();
    }

    private void getCars() {
        Properties properties = new Properties();
        properties.setProperty("user", jdbcUsername);
        properties.setProperty("password", jdbcPassword);
        properties.setProperty("useSSL", "false");
        properties.setProperty("rewriteBatchedStatements", "true");
        properties.setProperty("allowPublicKeyRetrieval", "true");
        try (Connection connection = DriverManager.getConnection(jdbcUrl, properties)) {
            connection.setAutoCommit(false);
            // 检查维度表是否存在
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet tables = metaData.getTables(null, null, "car_info", new String[]{"TABLE"});
            if (!tables.first()) {
                Statement statement = connection.createStatement();
                statement.execute("create table car_info(" +
                        "id varchar(20) primary key," +
                        "type_id varchar(20)," +
                        "type varchar(20)," +
                        "sale_type varchar(20)," +
                        "trademark varchar(20)," +
                        "company varchar(20)," +
                        "seating_capacity int," +
                        "power_type varchar(20)," +
                        "charge_type varchar(20)," +
                        "category varchar(20)," +
                        "weight_kg int," +
                        "warranty varchar(20)" +
                        ")");
                statement.close();
            }
            PreparedStatement preparedStatement = connection.prepareStatement("select id from car_info");
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next()) {
                PreparedStatement insert = connection.prepareStatement("insert into car_info values (?,?,?,?,?,?,?,?,?,?,?,?)");
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(Mock.class.getResourceAsStream("/cars.csv")));
                String line = bufferedReader.readLine();
                while (line != null && line.length() != 0) {
                    String[] fields = line.split(",");
                    insert.setString(1, fields[0]);
                    insert.setString(2, fields[1]);
                    insert.setString(3, fields[2]);
                    insert.setString(4, fields[3]);
                    insert.setString(5, fields[4]);
                    insert.setString(6, fields[5]);
                    insert.setInt(7, Integer.parseInt(fields[6]));
                    insert.setString(8, fields[7]);
                    insert.setString(9, fields[8]);
                    insert.setString(10, fields[9]);
                    insert.setInt(11, Integer.parseInt(fields[10]));
                    insert.setString(12, fields[11]);
                    insert.addBatch();
                    line = bufferedReader.readLine();
                }
                insert.executeBatch();
                connection.commit();
                insert.close();
            }
            resultSet.close();
            resultSet = preparedStatement.executeQuery();
            cars = new ArrayList<>(carCount);
            for (int i = 0; i < carCount && resultSet.next(); i++) {
                cars.add(Car.newInstance(resultSet.getString(1)));
            }
            resultSet.close();
            preparedStatement.close();

        } catch (SQLException e) {
            System.err.println("维度数据的JDBC URL格式错误");
            throw new RuntimeException(e);
        } catch (IOException | ArrayIndexOutOfBoundsException e) {
            System.err.println("读取维度数据错误，请检查源码");
            throw new RuntimeException(e);
        }
    }

    private void getLogger() {
        System.setProperty("dataPath", dataPath);
        LOGGER = LoggerFactory.getLogger(Mock.class);
    }

    private void writeData() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        int intervalSeconds = 30;
        for (int i = 0; ; i++) {
            if (count > 0 && i >= count * 2880) {
                break;
            }
            if (i > 0 && i % 2880 == 0) {
                System.err.println();
            }
            System.err.print("\r正在模拟" + format.format(startDate));
            cars.forEach(car -> {
                if (RandomUtil.randomDouble() < 0.95) {
                    car.repeat(30);
                } else {
                    car.change(30);
                }
                RealtimeData realtimeData = car.drawData(startDate);
                try {
                    String value = objectMapper.writeValueAsString(realtimeData);
                    LOGGER.info(value);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            });
            startDate = new Date(startDate.getTime() + intervalSeconds * 1000);
        }
        System.err.println();
    }

    private void parseArgs(String[] args) {
        Option startDateOpt = new Option("d", "date", true, "模拟开始日期, 格式为'yyyy-MM-dd', 必须指定");
        startDateOpt.setRequired(true);
        Option jdbcUrlOpt = new Option("u", "url", true, "维度数据的JDBC URL, 必须指定");
        jdbcUrlOpt.setRequired(true);
        Option jdbcUsernameOpt = new Option("n", "username", true, "维度数据的JDBC用户名, 必须指定");
        jdbcUsernameOpt.setRequired(true);
        Option jdbcPasswordOpt = new Option("p", "password", true, "维度数据的JDBC密码, 必须指定");
        jdbcPasswordOpt.setRequired(true);
        Option countOpt = new Option("c", "count", true, "模拟天数，-1表示无限模拟，默认为无限模拟");
        countOpt.setRequired(false);
        Option carCountOpt = new Option(null, "cars", true, "模拟车辆数目，1-10000，默认为100");
        carCountOpt.setRequired(false);
        Option dataPathOpt = new Option("o", "output", true, "模拟数据输出目录，默认为./data");
        dataPathOpt.setRequired(false);


        Options options = new Options();
        options.addOption(startDateOpt);
        options.addOption(countOpt);
        options.addOption(carCountOpt);
        options.addOption(jdbcUrlOpt);
        options.addOption(jdbcUsernameOpt);
        options.addOption(jdbcPasswordOpt);
        options.addOption(dataPathOpt);

        CommandLine cli;
        CommandLineParser cliParser = new DefaultParser();
        HelpFormatter helpFormatter = new HelpFormatter();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");


        try {
            cli = cliParser.parse(options, args);
            startDate = dateFormat.parse(cli.getOptionValue(startDateOpt));
            count = Integer.parseInt(cli.getOptionValue(countOpt, "-1"));
            carCount = Integer.parseInt(cli.getOptionValue(carCountOpt, "100"));
            dataPath = cli.getOptionValue(dataPathOpt, "data");
            jdbcUrl = cli.getOptionValue(jdbcUrlOpt);
            jdbcUsername = cli.getOptionValue(jdbcUsernameOpt);
            jdbcPassword = cli.getOptionValue(jdbcPasswordOpt);
        } catch (NumberFormatException e) {
            System.err.println("条目数量要求整数！");
        } catch (java.text.ParseException e) {
            System.err.println("日期格式错误！");
            System.exit(1);
        } catch (ParseException e) {
            helpFormatter.printHelp("参数错误，可用参数列表如下", options);
            System.exit(1);
        }
    }
}
