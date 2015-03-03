package org.oyach.jmh;

import org.openjdk.jmh.annotations.*;
import org.oyach.jmh.mapper.ReflectUntil;

import java.lang.reflect.Method;
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
@Threads(10)
public class MethodBenchmark {
    private static final String id = "org.oyach.jmh.mapper.UserMapper.findById";
    private static final String id2 = "org.oyach.jmh.mapper.UserMapper2.findById";

    @Benchmark
    public void classBenchMark() throws ClassNotFoundException {
        Class clazz = ReflectUntil.getClassById(id);
    }

    @Benchmark
    public void methodBenchMark() throws NoSuchMethodException {
        Method method = ReflectUntil.getMethodById(id);
    }

}
