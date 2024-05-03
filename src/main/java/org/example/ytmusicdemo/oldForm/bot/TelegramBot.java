package org.example.ytmusicdemo.oldForm.bot;

import org.example.ytmusicdemo.oldForm.config.BotConfig;
import org.example.ytmusicdemo.oldForm.model.Audio;
import org.example.ytmusicdemo.oldForm.request.Download;
import org.example.ytmusicdemo.oldForm.request.Ytdlp;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.methods.send.SendAudio;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TelegramBot extends TelegramLongPollingBot {

    private final BotConfig botConfig;
    private final Ytdlp ytdlp;
    private final Download download;
    private final Map<Long, Integer> searchResultsMessages = new HashMap<>();


    public TelegramBot(BotConfig botConfig, Ytdlp ytdlp, Download download) {
        this.botConfig = botConfig;
        this.ytdlp = ytdlp;
        this.download = download;
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }


    @Override
    public String getBotToken() {
        return botConfig.getBotToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String message = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            if (message.equals("/start")) {
                String text = "Привет, Я бот";
                sendTextMessage(chatId, text);
            } else {
                List<Audio> audioList = ytdlp.getAudioList(message);
                if (!audioList.isEmpty()) {
                    InlineKeyboardMarkup inlineKeyboardMarkup = createInlineKeyboard(audioList);
                    sendTextMessageWithKeyboard(chatId, "Выберите трек: ", inlineKeyboardMarkup);
                } else {
                    sendTextMessage(chatId, "По вашему запросу ничего не найдено.");
                }
            }
        }
        if (update.hasCallbackQuery()) {
            String data = update.getCallbackQuery().getData();
            Long chatId = update.getCallbackQuery().getMessage().getChatId();
            sendStatusMessage(chatId);
            deleteMessage(chatId, searchResultsMessages.get(chatId));
            downloadAudioFileAndSend(chatId, data);
        }
    }

    private void sendTextMessage(Long chatId, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId.toString());
        sendMessage.setText(text);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private InlineKeyboardMarkup createInlineKeyboard(List<Audio> audioList) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (Audio audio : audioList) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(audio.getDuration() + " | " + audio.getTitle());
            button.setCallbackData(audio.getId());
            row.add(button);
            rows.add(row);
        }

        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }

    private void sendTextMessageWithKeyboard(Long chatId, String text, InlineKeyboardMarkup keyboardMarkup) {
        try {
            SendMessage message = new SendMessage();
            message.setChatId(chatId.toString());
            message.setText(text);
            message.setReplyMarkup(keyboardMarkup);
            Message sendMessage = execute(message);
            searchResultsMessages.put(chatId, sendMessage.getMessageId());
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void downloadAudioFileAndSend(Long chatId, String audioId) {

        try {
            File file = download.save(audioId);
            SendAudio sendAudio = new SendAudio();
            AudioFile audioFile = AudioFileIO.read(file);
            Tag tag = audioFile.getTagOrCreateDefault();
            String[] title = (file.getName().split("-"));
            tag.setField(FieldKey.ARTIST, title[0].trim());
            tag.setField(FieldKey.TITLE, title[1].trim());
            audioFile.setTag(tag);
            audioFile.commit();
            sendAudio.setChatId(chatId.toString());
            sendAudio.setAudio(new InputFile(file));
            execute(sendAudio);
            Files.delete(Path.of(file.getAbsolutePath()));
        } catch (TelegramApiException | CannotWriteException | CannotReadException | TagException |
                 InvalidAudioFrameException | ReadOnlyFileException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void deleteMessage(Long chatId, Integer messageId) {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setMessageId(messageId);
        deleteMessage.setChatId(chatId.toString());
        try {
            execute(deleteMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
    private void sendStatusMessage(Long chatId) {
        SendChatAction sendMessage = new SendChatAction();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setAction(ActionType.UPLOADVOICE);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
