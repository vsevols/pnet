package com.pnet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class PNSystem {
    static String promptString(String prompt) {
        System.out.print(prompt+System.lineSeparator());
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String str = "";
        try {
            str = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return str;
    }
}
