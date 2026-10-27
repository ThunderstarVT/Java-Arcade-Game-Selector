package util;

import com.google.gson.Gson;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.ptr.IntByReference;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Reference {
    public static final Gson gson = new Gson();

    public static final ExecutorService executor = Executors.newFixedThreadPool(4);



    public static class Game {
        public final int id;
        public final String name;
        public final File path;
        public final List<File> images;

        public Game(int id, String name, File path, List<File> images) {
            this.id = id;
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



    public static WinDef.HWND FindWindow(long targetPid) {
        WinDef.HWND result[] = new WinDef.HWND[1];

        User32.INSTANCE.EnumWindows((window, data) -> {
            if (!User32.INSTANCE.IsWindowVisible(window)) return true;

            IntByReference pid = new IntByReference();

            User32.INSTANCE.GetWindowThreadProcessId(window, pid);

            long window_pid = Integer.toUnsignedLong(pid.getValue());

            if (window_pid == targetPid) {
                result[0] = window;
                return false;
            }

            return true;
        }, Pointer.NULL);

        return result[0];
    }
}
