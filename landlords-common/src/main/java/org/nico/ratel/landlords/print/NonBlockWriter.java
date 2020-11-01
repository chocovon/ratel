package org.nico.ratel.landlords.print;

import org.nico.ratel.landlords.helper.TimeHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class NonBlockWriter {
    public static final String ABANDON = "ABANDON";

    private static final BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();
    private static final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

    public static String write(String message, int timeLimit) {
        System.out.println();
        System.out.print("[ratel@" + message + "]$ ");
        try {
            if (timeLimit > 0) {
                String input = messageQueue.poll(timeLimit, TimeUnit.SECONDS);
                return input == null ? ABANDON : input;
            } else {
                return messageQueue.take();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            System.out.println();
        }
        return null;
    }

    public static String write(String message) {
        return write(message, 0);
    }

    static {
        new Thread(() -> {
            try {
                while (true) {
                    if (bufferedReader.ready()) { //check whether input is finished
                        String line = bufferedReader.readLine();
                        messageQueue.put(line == null ? "" : line);
                    }
                    TimeHelper.sleep(100L); //wait a short time to prevent busy check
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            } finally {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
