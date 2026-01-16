package ru.yandex.javacourse.schedule.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import ru.yandex.javacourse.schedule.http.handler.*;
import ru.yandex.javacourse.schedule.manager.Managers;
import ru.yandex.javacourse.schedule.manager.TaskManager;
import com.google.gson.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {

    private static final int PORT = 8080;

    private final HttpServer server;
    private final TaskManager manager;
    private final Gson gson;

    public static Gson getGson() {
        DateTimeFormatter dtf = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        JsonSerializer<Duration> durSer = (src, typeOfSrc, context) ->
                src == null ? JsonNull.INSTANCE : new JsonPrimitive(src.toMinutes());

        JsonDeserializer<Duration> durDes = (json, typeOfT, context) ->
                (json == null || json.isJsonNull() || json.getAsString().isBlank())
                        ? null
                        : Duration.ofMinutes(json.getAsLong());

        JsonSerializer<LocalDateTime> ldtSer = (src, typeOfSrc, context) ->
                src == null ? JsonNull.INSTANCE : new JsonPrimitive(src.format(dtf));

        JsonDeserializer<LocalDateTime> ldtDes = (json, typeOfT, context) ->
                (json == null || json.isJsonNull() || json.getAsString().isBlank())
                        ? null
                        : LocalDateTime.parse(json.getAsString(), dtf);

        return new GsonBuilder()
                .registerTypeAdapter(Duration.class, durSer)
                .registerTypeAdapter(Duration.class, durDes)
                .registerTypeAdapter(LocalDateTime.class, ldtSer)
                .registerTypeAdapter(LocalDateTime.class, ldtDes)
                .create();
    }

    public HttpTaskServer() throws IOException {
        this(Managers.getDefault(), getGson());
    }

    public HttpTaskServer(TaskManager manager, Gson gson) throws IOException {
        this.manager = manager;
        this.gson = gson;

        server = HttpServer.create(new InetSocketAddress(PORT), 0);

        TasksHandler tasksHandler = new TasksHandler(manager, gson);
        SubtasksHandler subtasksHandler = new SubtasksHandler(manager, gson);
        EpicsHandler epicsHandler = new EpicsHandler(manager, gson);
        HistoryHandler historyHandler = new HistoryHandler(manager, gson);
        PrioritizedHandler prioritizedHandler = new PrioritizedHandler(manager, gson);

        server.createContext("/tasks", tasksHandler);

        server.createContext("/subtasks", subtasksHandler);
        server.createContext("/subtasks/epic", subtasksHandler);

        server.createContext("/epics", epicsHandler);
        server.createContext("/epics/subtasks", epicsHandler);

        server.createContext("/history", historyHandler);
        server.createContext("/prioritized", prioritizedHandler);
    }

    public void start() {
        server.start();
        System.out.println("HTTP server started on port " + PORT);
    }

    public void stop() {
        server.stop(0);
        System.out.println("HTTP server stopped");
    }

    public TaskManager getManager() {
        return manager;
    }

    public Gson gson() {
        return gson;
    }

    public static void main(String[] args) throws IOException {
        HttpTaskServer server = new HttpTaskServer();
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        server.start();
    }
}







