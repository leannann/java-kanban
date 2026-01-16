package ru.yandex.javacourse.schedule.http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.yandex.javacourse.schedule.manager.TaskManager;
import ru.yandex.javacourse.schedule.tasks.Task;

import java.io.IOException;
import java.util.Map;

public class TasksHandler extends BaseHttpHandler {

    public TasksHandler(TaskManager manager, Gson gson) {
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
                        sendOk(h, manager.getTasks());
                    } else {
                        Task task = requirePresent(manager.getTask(id));
                        sendOk(h, task);
                    }
                }
                case "POST" -> {
                    String body = readBody(h);
                    Task task = gson.fromJson(body, Task.class);

                    int newId = manager.addNewTask(task);

                    sendJson(h, Map.of("id", newId), 201);
                }
                case "DELETE" -> {
                    if (id == null) {
                        manager.deleteTasks();
                        sendCreated(h);
                    } else {
                        requirePresent(manager.getTask(id));
                        manager.deleteTask(id);
                        sendCreated(h);
                    }
                }
                default -> sendJson(h, "{\"error\":\"Method not allowed\"}", 405);
            }
        } catch (Exception e) {
            handleKnownExceptions(h, e);
        }
    }
}




