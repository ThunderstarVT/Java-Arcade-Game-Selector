package Util;

import com.google.gson.Gson;

import java.io.File;
import java.util.List;

public class Reference {
    public static final Gson gson = new Gson();



    public static class Game {
        public final String name;
        public final File path;
        public final List<File> images;

        public Game(String name, File path, List<File> images) {
            this.name = name;
            this.path = path;
            this.images = images;
        }

        @Override
        public String toString() {
            return "Game{" +
                    "name='" + name + '\'' +
                    ", path=" + path +
                    ", images=" + images +
                    '}';
        }
    }
}
