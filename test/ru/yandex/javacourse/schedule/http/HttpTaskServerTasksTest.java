package ru.yandex.javacourse.schedule.http;

import com.google.gson.Gson;
import org.junit.jupiter.api.*;
import ru.yandex.javacourse.schedule.manager.InMemoryTaskManager;
import ru.yandex.javacourse.schedule.manager.TaskManager;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskServerTasksTest {

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
    void postTask_returns201_andTaskSaved() throws IOException, InterruptedException {
        Task task = new Task("T1", "D1", TaskStatus.NEW);
        task.setDuration(Duration.ofMinutes(15));
        task.setStartTime(LocalDateTime.of(2025, 1, 1, 10, 0));

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(task)))
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, resp.statusCode());
        assertEquals(1, manager.getTasks().size());
        assertEquals("T1", manager.getTasks().getFirst().getName());
    }

    @Test
    void getTaskById_notFound_returns404() throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks?id=999"))
                .GET()
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, resp.statusCode());
    }

    @Test
    void postOverlappingTask_returns406() throws IOException, InterruptedException {
        Task t1 = new Task("A", "d", TaskStatus.NEW);
        t1.setDuration(Duration.ofMinutes(30));
        t1.setStartTime(LocalDateTime.of(2025, 1, 1, 10, 0));

        Task t2 = new Task("B", "d", TaskStatus.NEW);
        t2.setDuration(Duration.ofMinutes(30));
        t2.setStartTime(LocalDateTime.of(2025, 1, 1, 10, 15)); // пересекается с t1

        HttpRequest req1 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(t1)))
                .build();

        HttpRequest req2 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(t2)))
                .build();

        HttpResponse<String> r1 = client.send(req1, HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> r2 = client.send(req2, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, r1.statusCode());
        assertEquals(406, r2.statusCode());
    }
}

