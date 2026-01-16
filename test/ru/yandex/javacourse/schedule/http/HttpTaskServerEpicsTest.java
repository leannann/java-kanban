package ru.yandex.javacourse.schedule.http;

import com.google.gson.Gson;
import org.junit.jupiter.api.*;
import ru.yandex.javacourse.schedule.manager.InMemoryTaskManager;
import ru.yandex.javacourse.schedule.manager.TaskManager;
import ru.yandex.javacourse.schedule.tasks.Epic;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskServerEpicsTest {

    private TaskManager manager;
    private HttpTaskServer server;
    private Gson gson;
    private HttpClient client;

    @BeforeEach
    void setUp() throws IOException {
        manager = new InMemoryTaskManager();
        server = new HttpTaskServer(manager, HttpTaskServer.getGson());
        gson = HttpTaskServer.getGson();
        client = HttpClient.newHttpClient();

        manager.deleteTasks();
        manager.deleteSubtasks();
        manager.deleteEpics();

        server.start();
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    @Test
    void postEpic_returns201_andEpicSaved() throws IOException, InterruptedException {
        Epic epic = new Epic("E1", "desc");

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(epic)))
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, resp.statusCode());
        assertEquals(1, manager.getEpics().size());
    }

    @Test
    void getEpic_notFound_returns404() throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics?id=999"))
                .GET()
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, resp.statusCode());
    }
}

