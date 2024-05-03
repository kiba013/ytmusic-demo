package org.example.ytmusicdemo.oldForm.request;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.ytmusicdemo.oldForm.model.Audio;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class Ytdlp {

    private static final Logger log = LogManager.getLogger(Ytdlp.class);

    public List<Audio> getAudioList(String query) {
        List<Audio> audioList = new ArrayList<>();
        try (InputStream inputStream = executeYtDlp(query);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            List<String> jsonBlocks = new ArrayList<>();

            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                // Собираем JSON-ответ в строку
                if (line.trim().startsWith("{")) {
                    if (!jsonBuilder.isEmpty()) {
                        jsonBlocks.add(jsonBuilder.toString());
                        jsonBuilder = new StringBuilder();
                    }
                }
                jsonBuilder.append(line.trim());
            }
            if (!jsonBuilder.isEmpty()) {
                jsonBlocks.add(jsonBuilder.toString());
            }

            // Обработка JSON-ответа
            for (String json : jsonBlocks) {
                try {
                    Audio audio = parseJson(json);
                    if (audio != null) {
                        audioList.add(audio);
                    }
                } catch (Exception e) {
                    log.error("Error parsing JSON: {}", json, e);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return audioList;
    }

    private InputStream executeYtDlp(String query) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(
                "yt-dlp", "ytsearch" + 25 + ":" + query,
                "--dump-json", "--default-search", "ytsearch",
                "--format", "bestaudio/best", "--no-playlist",
                "--flat-playlist", "--skip-download", "--quiet",
                "--ignore-errors", "--get-duration");
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        return process.getInputStream();
    }

    private Audio parseJson(String jsonBlock) {
        if (jsonBlock == null || jsonBlock.isEmpty()) {
            return null;
        }

        try {
            // Создаем JsonReader и устанавливаем флаг Lenient
            JsonReader reader = new JsonReader(new StringReader(jsonBlock));
            reader.setLenient(true);

            // Парсим JSON с помощью Gson
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();

            // Проверяем, что объект является валидным JSON объектом
            if (jsonObject.isJsonObject()) {
                String id = jsonObject.get("id").getAsString();
                String title = extractValue(jsonObject, "title");
                String url = extractValue(jsonObject, "webpage_url");
                String duration = extractValue(jsonObject, "duration");
                return new Audio(id, title, url, formatDuration(Double.parseDouble(duration)));
            } else {
                log.error(jsonBlock);
                return null;
            }
        } catch (Exception e) {
            // Обработка ошибок парсинга JSON
            log.info(e.getMessage());
            return null;
        }
    }


    private String extractValue(JsonObject jsonObject, String key) {
        if (jsonObject != null && jsonObject.has(key)) {
            return jsonObject.get(key).getAsString();
        }
        return null;
    }

    private static String formatDuration(double durationInSeconds) {
        int minutes = (int) (durationInSeconds / 60);
        int seconds = (int) (durationInSeconds % 60);
        return String.format("%d:%02d", minutes, seconds);
    }
}
