package com.kiwi;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

import static com.kiwi.Method.QUIT;

// This class subject to work, will be advanced and refactored with further implementation
public class ClientMain {
    public static void main(String[] args) throws IOException {

        final var socket = new Socket("localhost", 8090);
        final var flags = 0;
        final var is = socket.getInputStream();
        final var os = socket.getOutputStream();

        final var scanner = new Scanner(System.in);
        final var responseParser = new ResponseParser();

        while (true) {
            final var line = scanner.nextLine();
            final String[] tokens = line.split(" ");
            final Method method;
            try {
                method = Method.valueOf(tokens[0].trim());
            } catch (Exception ex) {
                System.out.println("Unknown method: [" + tokens[0] + "]");
                continue;
            }
            if (QUIT == method) {
                System.out.println("Good bye");
                break;
            }
            os.write(flags);
            os.write(method.ordinal());
            if (method.isKeyless()) {
                os.write(new byte[]{0, 1, 0, 0, 0, 0, 0, 0, 13, 10});
                os.flush();
            } else if (method.isMultiKey()) {
                if (method.withValue()) {
                    final var keyAmount = (tokens.length - 1) / 2;
                    os.write(new byte[]{0, (byte) keyAmount});
                    for (int i = 1; i < tokens.length; i += 2) {
                        final var key = tokens[i].trim();
                        final var value = tokens[i + 1].trim();
                        os.write(payloadLength(key, 2));
                        os.write(payloadLength(value, 4));
                        os.write(key.getBytes());
                        os.write(value.getBytes());
                    }
                } else {
                    final var keyAmount = tokens.length - 1;
                    os.write(new byte[]{0, (byte) keyAmount});
                    for (int i = 1; i < tokens.length; i++) {
                        final var key = tokens[i].trim();
                        os.write(payloadLength(key, 2));
                        os.write(new byte[]{0, 0, 0, 0});
                        os.write(key.getBytes());
                    }
                }

                os.write(new byte[]{13, 10});
                os.flush();
            } else {
                os.write(new byte[]{0,1});

                os.write(payloadLength(tokens[1].trim(), 2));
                if (method.withValue()) {
                    os.write(payloadLength(tokens[2].trim(), 4));
                } else {
                    os.write(new byte[]{0, 0, 0, 0});
                }
                os.write(tokens[1].getBytes());

                if (method.withValue()) {
                    os.write(tokens[2].getBytes());
                }
                os.write(new byte[]{13, 10});
                os.flush();
            }


            //RESPONSE
            final var response = responseParser.parse(is);
            System.out.println(response);
        }
        socket.close();
    }

    private static byte[] payloadLength(String value, int headerLength) {
        final var bytes = new byte[headerLength];
        var len = value.length();

        for (int i = headerLength - 1; i >= 0; i--) {
            final var b = len & 255;
            bytes[i] = (byte) b;
            len = len >> 8;
        }
        return bytes;
    }
}
