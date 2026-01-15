package ru.yandex.javacourse.schedule.manager;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HistoryManagerTest {

    private HistoryManager createHistory() {
        return Managers.getDefaultHistory();
    }

    @Test
    @DisplayName("пустая история")
    void emptyHistory() {
        HistoryManager history = createHistory();
        assertTrue(history.getHistory().isEmpty());
    }

    @Test
    @DisplayName("дублирование: в истории остаётся только последний просмотр")
    void duplication_keepsOnlyLast() {
        HistoryManager history = createHistory();

        Task t1 = new Task(1, "T1", "d", TaskStatus.NEW);
        history.add(t1);
        history.add(t1);
        history.add(t1);

        List<Task> result = history.getHistory();
        assertEquals(1, result.size());
        assertEquals(1, result.getFirst().getId());
    }

    @Test
    @DisplayName("удаление из истории: начало")
    void removeFromBeginning() {
        HistoryManager history = createHistory();

        history.add(new Task(1, "T1", "d", TaskStatus.NEW));
        history.add(new Task(2, "T2", "d", TaskStatus.NEW));
        history.add(new Task(3, "T3", "d", TaskStatus.NEW));

        history.remove(1);

        assertEquals(List.of(2, 3), history.getHistory().stream().map(Task::getId).toList());
    }

    @Test
    @DisplayName("удаление из истории: середина")
    void removeFromMiddle() {
        HistoryManager history = createHistory();

        history.add(new Task(1, "T1", "d", TaskStatus.NEW));
        history.add(new Task(2, "T2", "d", TaskStatus.NEW));
        history.add(new Task(3, "T3", "d", TaskStatus.NEW));

        history.remove(2);

        assertEquals(List.of(1, 3), history.getHistory().stream().map(Task::getId).toList());
    }

    @Test
    @DisplayName("удаление из истории: конец")
    void removeFromEnd() {
        HistoryManager history = createHistory();

        history.add(new Task(1, "T1", "d", TaskStatus.NEW));
        history.add(new Task(2, "T2", "d", TaskStatus.NEW));
        history.add(new Task(3, "T3", "d", TaskStatus.NEW));

        history.remove(3);

        assertEquals(List.of(1, 2), history.getHistory().stream().map(Task::getId).toList());
    }
}

