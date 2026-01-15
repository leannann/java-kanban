package ru.yandex.javacourse.schedule.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTaskManagerOverlapTest {
    private InMemoryTaskManager manager;

    @BeforeEach
    public void setUp() {
        manager = new InMemoryTaskManager();
    }

    @Test
    public void shouldAddNonOverlappingTasks() {
        Task t1 = new Task("T1", "desc", TaskStatus.NEW);
        t1.setStartTime(LocalDateTime.of(2025, 1, 1, 10, 0));
        t1.setDuration(Duration.ofMinutes(30));

        Task t2 = new Task("T2", "desc", TaskStatus.NEW);
        t2.setStartTime(LocalDateTime.of(2025, 1, 1, 10, 30));
        t2.setDuration(Duration.ofMinutes(30));

        int id1 = manager.addNewTask(t1);
        int id2 = manager.addNewTask(t2);

        assertEquals(2, manager.getTasks().size());
        assertEquals(id1, manager.getTask(id1).get().getId());
        assertEquals(id2, manager.getTask(id2).get().getId());
    }

    @Test
    public void shouldThrowOnOverlappingTasks() {
        Task t1 = new Task("T1", "desc", TaskStatus.NEW);
        t1.setStartTime(LocalDateTime.of(2025, 1, 1, 10, 0));
        t1.setDuration(Duration.ofMinutes(30));

        Task t2 = new Task("T2", "desc", TaskStatus.NEW);
        t2.setStartTime(LocalDateTime.of(2025, 1, 1, 10, 15));
        t2.setDuration(Duration.ofMinutes(30));

        manager.addNewTask(t1);
        Exception ex = assertThrows(RuntimeException.class, () -> manager.addNewTask(t2));
        assertTrue(ex.getMessage().contains("пересекается"));
    }

    @Test
    public void shouldClearIntervalsAfterDelete() {
        Task t1 = new Task("T1", "desc", TaskStatus.NEW);
        t1.setStartTime(LocalDateTime.of(2025, 1, 1, 10, 0));
        t1.setDuration(Duration.ofMinutes(30));

        int id1 = manager.addNewTask(t1);
        manager.deleteTask(id1);

        Task t2 = new Task("T2", "desc", TaskStatus.NEW);
        t2.setStartTime(LocalDateTime.of(2025, 1, 1, 10, 0));
        t2.setDuration(Duration.ofMinutes(30));

        assertDoesNotThrow(() -> manager.addNewTask(t2));
    }
}

