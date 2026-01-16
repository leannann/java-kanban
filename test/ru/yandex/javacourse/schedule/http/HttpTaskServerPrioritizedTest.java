package ru.yandex.javacourse.schedule.http;

import org.junit.jupiter.api.*;
import ru.yandex.javacourse.schedule.manager.InMemoryTaskManager;
import ru.yandex.javacourse.schedule.manager.TaskManager;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskServerPrioritizedTest {

    private HttpTaskServer server;
    private HttpClient client;

    @BeforeEach
    void setUp() throws IOException {
        TaskManager manager = new InMemoryTaskManager();
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
    void prioritized_returns200() throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/prioritized"))
                .GET()
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
    }
}

