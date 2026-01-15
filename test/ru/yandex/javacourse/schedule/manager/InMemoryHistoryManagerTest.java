package ru.yandex.javacourse.schedule.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryHistoryManagerTest {

    private HistoryManager historyManager;

    @BeforeEach
    public void initHistoryManager() {
        historyManager = Managers.getDefaultHistory();
    }

    @Test
    public void testAddTaskAddsToHistory() {
        Task task = new Task(1, "Test 1", "Testing task 1", TaskStatus.NEW);
        historyManager.add(task);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "Задача должна быть добавлена в историю");
        assertEquals(task, history.get(0), "Добавленная задача должна присутствовать в истории");
    }

    @Test
    public void testAddSameTaskTwiceShouldNotDuplicateButMoveToEnd() {
        Task task1 = new Task(1, "Task 1", "desc", TaskStatus.NEW);
        Task task2 = new Task(2, "Task 2", "desc", TaskStatus.NEW);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task1);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "История не должна содержать дубликаты");
        assertEquals(2, history.get(0).getId(), "Первая задача в истории должна быть 2");
        assertEquals(1, history.get(1).getId(), "Повторно добавленная задача должна быть перемещена в конец");
    }

    @Test
    public void testRemoveTaskRemovesFromHistory() {
        Task t1 = new Task(1, "Task 1", "desc", TaskStatus.NEW);
        Task t2 = new Task(2, "Task 2", "desc", TaskStatus.NEW);

        historyManager.add(t1);
        historyManager.add(t2);
        assertEquals(2, historyManager.getHistory().size(), "История должна содержать 2 задачи");

        historyManager.remove(1);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "После удаления должна остаться одна запись");
        assertEquals(2, history.get(0).getId(), "В истории должна остаться только задача с id=2");
    }

    @Test
    public void testRemoveNonExistingTaskDoesNothing() {
        Task t = new Task(1, "Task 1", "desc", TaskStatus.NEW);
        historyManager.add(t);

        assertDoesNotThrow(() -> historyManager.remove(999), "Удаление несуществующего id не должно вызывать ошибок");
        assertEquals(1, historyManager.getHistory().size(), "Размер истории не должен измениться");
    }

    @Test
    public void testEmptyHistoryInitially() {
        assertTrue(historyManager.getHistory().isEmpty(), "История должна быть пустой при инициализации");
    }
}
