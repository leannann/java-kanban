package ru.yandex.javacourse.schedule.http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.yandex.javacourse.schedule.manager.TaskManager;

import java.io.IOException;

public class PrioritizedHandler extends BaseHttpHandler {

    public PrioritizedHandler(TaskManager manager, Gson gson) {
        super(manager, gson);
    }

    @Override
    public void handle(HttpExchange h) throws IOException {
        try {
            if (!"GET".equals(h.getRequestMethod())) {
                sendJson(h, "{\"error\":\"Method not allowed\"}", 405);
                return;
            }
            sendJson(h, manager.getPrioritizedTasks(), 200);
        } catch (Exception e) {
            handleKnownExceptions(h, e);
        }
    }
}



