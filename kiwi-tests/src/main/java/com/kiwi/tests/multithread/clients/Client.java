package com.kiwi.tests.multithread.clients;

import java.io.IOException;
import java.io.OutputStream;

public interface Client {
    void execute(OutputStream os) throws IOException;
}
