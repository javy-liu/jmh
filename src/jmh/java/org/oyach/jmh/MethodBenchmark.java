package org.oyach.jmh;

import org.openjdk.jmh.annotations.*;
import org.oyach.jmh.mapper.CacheReflectUntil;
import org.oyach.jmh.mapper.ReflectUntil;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * @author liuzhenyuan
 * @version Last modified 15/3/3
 * @since 0.0.1
 *
 * 测试基于反射，cache和非cache方式获取方法名的效率
 *
 * 事实证明差别不大
 */
@Fork(2) // 迭代集合次数
@Warmup(iterations = 4) // 预热迭代次数
@Measurement(iterations = 3) // 迭代次数
@BenchmarkMode(value = Mode.AverageTime)
@OutputTimeUnit(value = TimeUnit.NANOSECONDS)
@Timeout(time = 60)
@Threads(1)
public class MethodBenchmark {
    private static final String id = "org.oyach.jmh.mapper.UserMapper.findById";
    private static final String id2 = "org.oyach.jmh.mapper.UserMapper2.findById";

    @Benchmark
    public void stringBenchMark() throws ClassNotFoundException {
        int index = id.lastIndexOf(".");
        String className = id.substring(0, index);
    }

    @Benchmark
    public void classBenchMark() throws ClassNotFoundException {
        Class clazz1 = ReflectUntil.getClassById(id);
        Class clazz2 = ReflectUntil.getClassById(id);
    }

    @Benchmark
    public void methodBenchMark() throws NoSuchMethodException {
        Method method1 = ReflectUntil.getMethodById(id);
        Method method2 = ReflectUntil.getMethodById(id);
    }

    @Benchmark
    public void methodNotSameBenchMark() throws NoSuchMethodException {
        for (int i = 0; i < 10; i++) {
            Method method1 = ReflectUntil.getMethodById(id);
            Method method2 = ReflectUntil.getMethodById(id2);
        }

    }

    @Benchmark
    public void methodSameBenchMark() throws NoSuchMethodException {
        for (int i = 0; i < 1000; i++) {
            Method method1 = ReflectUntil.getMethodById(id);
            Method method2 = ReflectUntil.getMethodById(id);
        }
    }

    @Benchmark
    public void calcheMethodNotSameBenchMark() throws NoSuchMethodException {
        for (int i = 0; i < 100; i++) {
            Method method1 = CacheReflectUntil.getMethodById(id);
            Method method2 = CacheReflectUntil.getMethodById(id2);
        }

    }

    @Benchmark
    public void cacheMethodSameBenchMark() throws NoSuchMethodException {
        for (int i = 0; i < 1000; i++) {
            Method method1 = CacheReflectUntil.getMethodById(id);
            Method method2 = CacheReflectUntil.getMethodById(id);
        }
    }
}
