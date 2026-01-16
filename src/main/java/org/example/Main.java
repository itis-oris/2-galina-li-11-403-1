package org.example;


import org.example.client.ui.MainView;

public class Main {
    public static void main(String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("server")) {
            main(new String[]{});
        } else {
            MainView.main(new String[]{});
        }
    }
}
