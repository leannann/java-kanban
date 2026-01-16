package ru.yandex.javacourse.schedule.http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.yandex.javacourse.schedule.exceptions.ManagerSaveException;
import ru.yandex.javacourse.schedule.exceptions.ManagerValidateException;
import ru.yandex.javacourse.schedule.manager.TaskManager;
import ru.yandex.javacourse.schedule.exceptions.NotFoundException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public abstract class BaseHttpHandler implements HttpHandler {
    protected final TaskManager manager;
    protected final Gson gson;

    protected BaseHttpHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
        this.gson = gson;
    }

    protected String readBody(HttpExchange h) throws IOException {
        return new String(h.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    protected Integer getIdFromQuery(HttpExchange h) {
        String query = h.getRequestURI().getQuery();
        if (query == null || query.isBlank()) return null;

        for (String part : query.split("&")) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2 && "id".equals(kv[0])) {
                try {
                    return Integer.parseInt(kv[1]);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Bad id: " + kv[1]);
                }
            }
        }
        return null;
    }

    protected void sendOk(HttpExchange h, Object data) throws IOException {
        sendJson(h, data, 200);
    }

    protected void sendCreated(HttpExchange h) throws IOException {
        sendJson(h, "", 201);
    }

    protected void sendNotFound(HttpExchange h) throws IOException {
        sendJson(h, "{\"error\":\"Not Found\"}", 404);
    }

    protected void sendHasInteractions(HttpExchange h) throws IOException {
        sendJson(h, "{\"error\":\"Task intersects\"}", 406);
    }

    protected void sendError(HttpExchange h) throws IOException {
        sendJson(h, "{\"error\":\"Internal Server Error\"}", 500);
    }

    protected void sendBadRequest(HttpExchange h) throws IOException {
        sendJson(h, "{\"error\":\"Bad request\"}", 400);
    }

    protected void sendJson(HttpExchange h, Object data, int code) throws IOException {
        String json = (data instanceof String s) ? s : gson.toJson(data);
        byte[] resp = json.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(code, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    protected <T> T requirePresent(Optional<T> opt) {
        return opt.orElseThrow(NotFoundException::new);
    }

    protected void handleKnownExceptions(HttpExchange h, Exception e) throws IOException {
        if (e instanceof NotFoundException) {
            sendNotFound(h);
        } else if (e instanceof ManagerValidateException) {
            sendHasInteractions(h);
        } else if (e instanceof ManagerSaveException) {
            sendError(h);
        } else if (e instanceof IllegalArgumentException) {
            sendBadRequest(h);
        } else {
            sendError(h);
        }
    }
}


