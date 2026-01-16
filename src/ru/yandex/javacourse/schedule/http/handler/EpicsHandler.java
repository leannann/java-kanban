package ru.yandex.javacourse.schedule.http.handler;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import ru.yandex.javacourse.schedule.manager.TaskManager;
import ru.yandex.javacourse.schedule.tasks.Epic;

import java.io.IOException;
import java.util.Optional;

public class EpicsHandler extends BaseHttpHandler {

    public EpicsHandler(TaskManager manager, Gson gson) {
        super(manager, gson);
    }

    @Override
    public void handle(HttpExchange h) throws IOException {
        try {
            String method = h.getRequestMethod();
            Integer id = getIdFromQuery(h);

            switch (method) {
                case "GET" -> {
                    if (id == null) {
                        sendJson(h, manager.getEpics(), 200);
                        return;
                    }
                    Epic epic = requirePresent(manager.getEpic(id));
                    if (epic == null) return;
                    sendJson(h, epic, 200);
                }

                case "POST" -> {
                    String body = readBody(h);
                    Epic epic = gson.fromJson(body, Epic.class);
                    if (epic == null) {
                        sendBadRequest(h);
                        return;
                    }

                    int incomingId = epic.getId();
                    Optional<Epic> existing = (incomingId > 0) ? manager.getEpic(incomingId) : Optional.empty();

                    int resultId;
                    if (incomingId > 0 && existing.isPresent()) {
                        manager.updateEpic(epic);
                        resultId = incomingId;
                    } else {
                        resultId = manager.addNewEpic(epic);
                    }

                    sendJson(h, "{\"id\":" + resultId + "}", 201);
                }

                case "DELETE" -> {
                    if (id == null) {
                        manager.deleteEpics();
                        sendCreated(h);
                        return;
                    }

                    if (manager.getEpic(id).isEmpty()) {
                        sendNotFound(h);
                        return;
                    }

                    manager.deleteEpic(id);
                    sendCreated(h);
                }

                default -> sendJson(h, "{\"error\":\"Method not allowed\"}", 405);
            }

        } catch (JsonSyntaxException e) {
            sendBadRequest(h);
        } catch (Exception e) {
            handleKnownExceptions(h, e);
        }
    }
}



