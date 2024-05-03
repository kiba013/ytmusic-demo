package org.example.ytmusicdemo.oldForm.request;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
public class Download {
    private static final String SAVE_DIRECTORY = "/home/legion/files/mp3";

    public File save(String url) {
        String savePath = SAVE_DIRECTORY + File.separator + "%(title)s.%(ext)s";
        Process process = null;
        File saveFile = null;
        String yt = "/home/kkibv17/yt-dlp/./yt-dlp.sh";
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    yt, "--extract-audio", "--audio-format", "best",
                    "--audio-format", "mp3",
                    "--output", savePath, url
            );
            processBuilder.redirectErrorStream(true);
            process = processBuilder.start();

            if (!process.waitFor(1, TimeUnit.MINUTES)) {
                process.destroy();
                throw new RuntimeException("Audio download failed: Process timed out");
            }
            File[] files = new File(SAVE_DIRECTORY).listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        saveFile = file;
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error while starting the download process: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Download process was interrupted: " + e.getMessage(), e);
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
        return saveFile;
    }

}
