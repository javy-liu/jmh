package org.oyach.jmh;

import org.openjdk.jmh.annotations.*;
import org.oyach.jmh.sql.parser.DruidParser;
import org.oyach.jmh.sql.parser.FDBParser;
import org.oyach.jmh.sql.parser.JsqlParser;

import java.util.concurrent.TimeUnit;

/**
 * @author liuzhenyuan
 * @version Last modified 15/3/4
 * @since 0.0.1
 *
 * sql解析基准测试
 *
 * 测试了3个sql解析框架  druid fdb-sql-parser JSqlParser
 *
 * 速度
 * druid 大约是 JSqlParser 的10倍
 * JSqlParser 大约是 fdb-sql-parser 的两倍
 * 全部框架使用cache后都有明显改善
 *
 */
@Fork(2) // 迭代集合次数
@Warmup(iterations = 2) // 预热迭代次数
@Measurement(iterations = 3) // 迭代次数
@BenchmarkMode(value = Mode.AverageTime)
@OutputTimeUnit(value = TimeUnit.MILLISECONDS)
@Timeout(time = 60)
@Threads(1)
public class SqlParserBenchmark {

    private static final String select = "select" +
            "\t\t\tid,wm_order_id,wm_food_id,food_price,unit,count,box_num,box_price,food_name,order_time,origin_food_price\n" +
            "\t\tfrom \n" +
            "\t\t\twm_order_detail\n" +
            "\t\twhere\n" +
            "			wm_order_id in (?,?,?) ORDER BY id DESC limit 0,1";


    @Benchmark
    public void druidParserBenchMark() throws ClassNotFoundException {
        int i = 1;
        do {
            DruidParser.select(select, "user" + i); // 0.005 0.013 0.481 0.121
            i++;
        }while (i < 100);

    }

    @Benchmark
    public void FDBParserBenchMark() throws ClassNotFoundException {
        int i = 1;
        do {
            FDBParser.select(select + i, "user" + i); // 0.105  0.213 10.187 2.114
            i++;
        }while (i < 100);

    }

    @Benchmark
    public void JsqlParserParserBenchMark() throws ClassNotFoundException {
        int i = 1;
        do {
            JsqlParser.select(select + i, "user" + i); // 0.053 0.109 4.457 0.450
            i++;
        }while (i < 100);

    }
}
