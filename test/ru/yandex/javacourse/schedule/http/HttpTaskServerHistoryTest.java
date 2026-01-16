package ru.yandex.javacourse.schedule.http;

import org.junit.jupiter.api.*;
import ru.yandex.javacourse.schedule.manager.InMemoryTaskManager;
import ru.yandex.javacourse.schedule.manager.TaskManager;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskServerHistoryTest {

    private TaskManager manager;
    private HttpTaskServer server;
    private HttpClient client;

    @BeforeEach
    void setUp() throws IOException {
        manager = new InMemoryTaskManager();
        server = new HttpTaskServer(manager, HttpTaskServer.getGson());
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
    void historyInitiallyEmpty_returns200_andEmptyList() throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/history"))
                .GET()
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
        // тело можешь дополнительно распарсить gson-ом, но достаточно проверить manager:
        assertTrue(manager.getHistory().isEmpty());
    }

    @Test
    void historyAfterGets_returns200_andNotEmpty() throws IOException, InterruptedException {
        int id = manager.addNewTask(new Task("T", "d", TaskStatus.NEW));

        // дергаем GET /tasks?id=... чтобы попало в историю
        HttpRequest get = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks?id=" + id))
                .GET()
                .build();
        client.send(get, HttpResponse.BodyHandlers.ofString());

        HttpRequest history = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/history"))
                .GET()
                .build();

        HttpResponse<String> resp = client.send(history, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
        assertFalse(manager.getHistory().isEmpty());
    }
}

