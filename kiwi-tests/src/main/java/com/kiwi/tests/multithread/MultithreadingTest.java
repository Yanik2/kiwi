package com.kiwi.tests.multithread;

import com.kiwi.tests.multithread.clients.DelClient;
import com.kiwi.tests.multithread.clients.GetClient;
import com.kiwi.tests.multithread.clients.IncrClient;
import com.kiwi.tests.multithread.clients.SetClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class MultithreadingTest {
    private static final String KEY_BASE = "ADSFASDFADSFASDFASDFASDFASDFASDFASDFASDFASDFASDFASDFASDFASDFASDFASDFASDFASDF" +
            "ASDFASDFASDFASDFASDFASDFASDFASDFASDFASDFASDFASDFASDFASDFASDFASDFASDFASDFASDFASDFASDFASDFAFDADFASDFADFASDF" +
            "ASDFASDFASDFASDFADAFSDFASDFADFASDFADFADFASDFASDFASDFASDFASDFASDFAFDASDFAASDFASDFADFASDFADSFASDFADFASDFADF" +
            "ASDFADFSADSFADSASDFASFD";

    public static void main(String[] args) throws InterruptedException {
        final var keys = new ArrayList<String>();
        for (int i = 0; i < 1000000; i++) {
            keys.add(KEY_BASE + i);
        }
        final var setKeys = keys.subList(0, 750000);
        final var incrKeys = keys.subList(750000, 1000000);
        final var rand = new Random();
        final var setIndexes = new ArrayList<Integer>();
        for (int i = 0; i < 3000000; i++) {
            setIndexes.add(rand.nextInt(750000));
        }
        final var incrIndexes = new ArrayList<Integer>();
        for (int i = 0; i < 3000000; i++) {
            incrIndexes.add(rand.nextInt(250000));
        }
        final var commonIndexes = new ArrayList<Integer>();
        for (int i = 0; i < 3000000; i++) {
            commonIndexes.add(rand.nextInt(1000000));
        }

        final var clients = List.of(new GetClient(keys, commonIndexes),
                new SetClient(setKeys, setIndexes),
                new IncrClient(incrKeys, incrIndexes),
                new DelClient(keys, commonIndexes));

        final var threads = new ArrayList<Thread>();
        final var map = new ConcurrentHashMap<String, ThreadTask.ResponsePerf>();

        for (int i = 0; i < 100; i++) {
            threads.add(new Thread(new ThreadTask(map, clients)));
        }

        for (Thread t : threads) {
            t.start();
        }

        for (Thread t : threads) {
            t.join();
        }

        for (Map.Entry<String, ThreadTask.ResponsePerf> e : map.entrySet()) {
            System.out.println(e.getKey() + ": " + e.getValue());
        }
    }
}
