package com.kiwi.tests.multithread;

import com.kiwi.tests.multithread.clients.Client;

import java.net.Socket;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentMap;

public class ThreadTask implements Runnable {
    private final Random rand = new Random();

    private final List<Client> clients;
    private final ConcurrentMap<String, ResponsePerf> responses;

    public ThreadTask(ConcurrentMap<String, ResponsePerf> responses, List<Client> clients) {
        this.responses = responses;
        this.clients = clients;
    }

    @Override
    public void run() {
        final Socket socket;
        long start = 0;
        ResponseReader responseReader = null;
        final byte flags = 0;
        final byte ext = 3;
        try {
            socket = new Socket("localhost", 8090);
            final var is = socket.getInputStream();
            final var os = socket.getOutputStream();
            responseReader = new ResponseReader(is);
            final var responseThread = new Thread(responseReader);
            responseThread.start();

            start = System.currentTimeMillis();
            for (int i = 0; i < 100000; i++) {
                os.write(flags);
                os.flush();
                final var methodIndex = rand.nextInt(4);
                clients.get(methodIndex).execute(os);
            }

            os.write(flags);
            os.write(ext);
            os.write(new byte[]{0, 1, 0, 0, 0, 0, 0, 0, 13, 10});
            os.flush();

            responseThread.join();
        } catch (Exception ex) {
            System.out.println("Exception in thread: [" + Thread.currentThread().getName() +
                    "]. Message: " + ex.getMessage());
        }

        final var time = System.currentTimeMillis() - start;
        final var responseNumber = responseReader.getResponseNumber();
        responses.put(Thread.currentThread().getName(),
                new ResponsePerf(responseNumber, time, (double) time / responseNumber));
    }

    public record ResponsePerf(int responseNumber, long time, double averageTime) {}
}

