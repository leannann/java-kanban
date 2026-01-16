package ru.yandex.javacourse.schedule.http.handler;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import ru.yandex.javacourse.schedule.manager.TaskManager;
import ru.yandex.javacourse.schedule.tasks.Subtask;

import java.io.IOException;
import java.util.Optional;

public class SubtasksHandler extends BaseHttpHandler {

    public SubtasksHandler(TaskManager manager, Gson gson) {
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
                        sendJson(h, manager.getSubtasks(), 200);
                        return;
                    }
                    Subtask subtask = requirePresent(manager.getSubtask(id));
                    if (subtask == null) return;
                    sendJson(h, subtask, 200);
                }

                case "POST" -> {
                    String body = readBody(h);
                    Subtask subtask = gson.fromJson(body, Subtask.class);
                    if (subtask == null) {
                        sendBadRequest(h);
                        return;
                    }

                    int incomingId = subtask.getId();
                    Optional<Subtask> existing = (incomingId > 0) ? manager.getSubtask(incomingId) : Optional.empty();

                    Integer resultId;
                    if (incomingId > 0 && existing.isPresent()) {
                        manager.updateSubtask(subtask);
                        resultId = incomingId;
                    } else {
                        resultId = manager.addNewSubtask(subtask);
                        if (resultId == null) {
                            sendNotFound(h);
                            return;
                        }
                    }

                    sendJson(h, "{\"id\":" + resultId + "}", 201);
                }

                case "DELETE" -> {
                    if (id == null) {
                        manager.deleteSubtasks();
                        sendCreated(h);
                        return;
                    }

                    if (manager.getSubtask(id).isEmpty()) {
                        sendNotFound(h);
                        return;
                    }

                    manager.deleteSubtask(id);
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



