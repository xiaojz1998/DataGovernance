package com.atguigu.dga.governance.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class CompletableFutureTest {
    public static void main(String[] args) {
        List<Integer> integers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        // 求平方和，并行过程
        long start = System.currentTimeMillis();
        // 任务列表
        List<CompletableFuture<Integer>> futures = new ArrayList<>();
        for (Integer integer : integers) {
            // 提交异步任务, 并不执行
            CompletableFuture<Integer> integerCompletableFuture = CompletableFuture.supplyAsync(
                    () -> {
                        try {
                            TimeUnit.SECONDS.sleep(2);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        return integer * integer;
                    }
            );
            futures.add(integerCompletableFuture);
        }
        // 异步执行，然后收集到集合
        List<Integer> collect = futures.stream().map(CompletableFuture::join).collect(Collectors.toList());
        long end = System.currentTimeMillis();
        // 集合中是结果
        System.out.println(end-start);

        //---------------------------------------------------------------------------------------
        // 线程池方法    线程只会提供两个，任务会优先往阻塞队列里面放，所以线程池只会2个2个执行，把阻塞队列改成7就能多给2个
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(2,4,10, TimeUnit.SECONDS,new LinkedBlockingDeque<>(100));



    }
}
