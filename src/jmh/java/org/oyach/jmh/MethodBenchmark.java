package org.oyach.jmh;

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

/**
 * @author liuzhenyuan
 * @version Last modified 15/3/3
 * @since 0.0.1
 *
 * 测试基于反射，基于栈跟踪获取方法名的效率
 */
@Fork(2) // 迭代集合次数
@Warmup(iterations = 4) // 预热迭代次数
@Measurement(iterations = 3) // 迭代次数
@BenchmarkMode(value = Mode.AverageTime)
@OutputTimeUnit(value = TimeUnit.NANOSECONDS)
@Timeout(time = 60)
public class MethodBenchmark {

    private static int i;

    @Benchmark
    public void setMethodBenchMark(){
        System.out.println(i++);
        i++;
    }

    @Benchmark
    public void methodBenchMark(){
//        System.out.println(i++);
        i++;
    }
}
