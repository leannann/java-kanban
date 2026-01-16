package ru.yandex.javacourse.schedule.http;

import com.google.gson.Gson;
import org.junit.jupiter.api.*;
import ru.yandex.javacourse.schedule.manager.InMemoryTaskManager;
import ru.yandex.javacourse.schedule.manager.TaskManager;
import ru.yandex.javacourse.schedule.tasks.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskServerSubtasksTest {

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
    void postSubtask_withoutEpic_returns404() throws IOException, InterruptedException {
        Subtask st = new Subtask("S1", "d", TaskStatus.NEW, 999);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(st)))
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, resp.statusCode());
    }

    @Test
    void postSubtask_withEpic_returns201_andSaved() throws IOException, InterruptedException {
        // сперва создаём эпик через менеджер (или через API — но это отдельный тест)
        int epicId = manager.addNewEpic(new Epic("E1", "d"));

        Subtask st = new Subtask("S1", "d", TaskStatus.NEW, epicId);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(st)))
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, resp.statusCode());
        assertEquals(1, manager.getSubtasks().size());
        assertEquals(epicId, manager.getSubtasks().getFirst().getEpicId());
    }
}

