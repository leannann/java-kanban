package ru.yandex.javacourse.schedule.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HistoryStructureTest {

    private HistoryManager history;

    @BeforeEach
    void setUp() {
        history = Managers.getDefaultHistory();
    }

    @Test
    void add_preservesOrder_and_noDuplicates_onRevisit() {
        Task t1 = new Task(1, "A", "a", TaskStatus.NEW);
        Task t2 = new Task(2, "B", "b", TaskStatus.NEW);
        Task t3 = new Task(3, "C", "c", TaskStatus.NEW);

        history.add(t1);
        history.add(t2);
        history.add(t3);
        assertEquals(List.of(1,2,3), history.getHistory().stream().map(Task::getId).toList());

        history.add(t2);
        assertEquals(List.of(1,3,2), history.getHistory().stream().map(Task::getId).toList());
        assertEquals(3, history.getHistory().size(), "повторный просмотр не создаёт дубликаты");
    }

    @Test
    void remove_head_middle_tail() {
        Task t1 = new Task(1, "A", "a", TaskStatus.NEW);
        Task t2 = new Task(2, "B", "b", TaskStatus.NEW);
        Task t3 = new Task(3, "C", "c", TaskStatus.NEW);

        history.add(t1); history.add(t2); history.add(t3);
        history.remove(1);
        assertEquals(List.of(2,3), history.getHistory().stream().map(Task::getId).toList());

        history.remove(2);
        assertEquals(List.of(3), history.getHistory().stream().map(Task::getId).toList());

        history.remove(3);
        assertTrue(history.getHistory().isEmpty());
    }

    @Test
    void remove_nonExisting_safe() {
        Task t1 = new Task(1, "A", "a", TaskStatus.NEW);
        history.add(t1);
        assertDoesNotThrow(() -> history.remove(999));
        assertEquals(1, history.getHistory().size());
    }
}
