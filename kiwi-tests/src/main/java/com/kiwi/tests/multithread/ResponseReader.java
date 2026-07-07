package com.kiwi.tests.multithread;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.LongAdder;

import static com.kiwi.tests.utils.ResponseParser.parse;

public class ResponseReader implements Runnable {
    private final InputStream is;

    private final LongAdder responseNumber = new LongAdder();

    public ResponseReader(InputStream is) {
        this.is = is;
    }

    @Override
    public void run() {
        try {
            while (true) {
                final var response = parse(is);
                responseNumber.increment();
                if (!response.isEmpty()) {
//                    System.out.println("Response No: " + response.getBytes()[0] + ", Thread: " + Thread.currentThread().getName());
                    if (response.equals("end of stream") || response.equals("error in response")) {
                        System.out.println("Response: " + response + " for thread: " + Thread.currentThread().getName());
                        break;
                    }
                }
                if (Thread.currentThread().isInterrupted()) {
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("Exception is response parser: " + e.getMessage());
        }
    }

    public long getResponseNumber() {
        return responseNumber.sum();
    }
}
