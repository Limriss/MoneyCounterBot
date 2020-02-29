package ru.some.company.service;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.some.company.service.interfaces.Command;
import ru.some.company.utils.PropertyLoader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class BotMain extends TelegramLongPollingBot {

    private static final Map<String, Method> availableCommands = new HashMap<>();
    private static final CommandListener listener = new CommandListener();
    private ReplyKeyboardMarkup keyboard;
    private boolean isAwait;
    private String command;

    static{
        for (Method method : listener.getClass().getDeclaredMethods())
        {
            if (method.isAnnotationPresent(Command.class))
            {
                Command cmd = method.getAnnotation(Command.class);
                availableCommands.put(cmd.name(), method);
            }
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        if (message.getText().equals("/start")){

        }

        if (!isAwait && availableCommands.containsKey(message.getText())) {
            command = message.getText();
            reply("enter arguments", message.getChatId());
            isAwait = true;
        }

        else if (!isAwait && !availableCommands.containsKey(message.getText()))
            reply("Such command doesn't exist!", message.getChatId());

        else if (isAwait){
            String text = message.getText() + " " + message.getFrom().getId();
            String[] args = text.split(" ");
            Method method = availableCommands.get(command);

            Command com = method.getAnnotation(Command.class);
            if (com.argsCount() != args.length)
                reply("Invalid arguments count", message.getChatId());



            else try {
                System.out.println("step1");
                method.invoke(listener, (Object) args);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                reply("Ups, Something went wrong.", message.getChatId());
            } finally {
                isAwait = false;
            }
        }
    }

    @Override
    public String getBotUsername() {
        return PropertyLoader.loadProperty("bot.username");
    }

    @Override
    public String getBotToken() {
        return PropertyLoader.loadProperty("bot.token");
    }

    private void reply(String text, long chatId){
        SendMessage response = new SendMessage()
                .setText(text)
                .setChatId(chatId);

        try{
            execute(response);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
