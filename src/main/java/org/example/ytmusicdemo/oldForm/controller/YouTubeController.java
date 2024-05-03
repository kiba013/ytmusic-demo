package org.example.ytmusicdemo.oldForm.controller;

import org.example.ytmusicdemo.oldForm.model.Audio;
import org.example.ytmusicdemo.oldForm.request.Ytdlp;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("youtube")
public class YouTubeController {

    private final Ytdlp ytdlp;

    public YouTubeController(Ytdlp ytdlp) {
        this.ytdlp = ytdlp;
    }

    @GetMapping("/songs")
    public ResponseEntity<List<Audio>> searchSongsByArtist(@RequestParam String artistName) {
        List<Audio> audioList = ytdlp.getAudioList(artistName);
        return ResponseEntity.ok()
                .body(audioList);

    }

    @PostMapping("songs/{id}")
    public ResponseEntity<?> getSongById(@PathVariable String id) {
        return null;
    }

}
