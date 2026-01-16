package ru.yandex.javacourse.schedule.manager;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.exceptions.ManagerValidateException;
import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {

    protected abstract T createManager();

    @Test
    @DisplayName("добавление и получение Task")
    void addAndGetTask() {
        TaskManager manager = createManager();

        Task task = new Task("T1", "d", TaskStatus.NEW);
        task.setDuration(Duration.ofMinutes(30));
        task.setStartTime(LocalDateTime.of(2025, 1, 1, 10, 0));

        int id = manager.addNewTask(task);

        Optional<Task> loadedOpt = manager.getTask(id);
        assertTrue(loadedOpt.isPresent(), "task should be found by id");

        Task loaded = loadedOpt.get();
        assertEquals(id, loaded.getId());
        assertEquals("T1", loaded.getName());
        assertEquals(Duration.ofMinutes(30), loaded.getDuration());
        assertEquals(LocalDateTime.of(2025, 1, 1, 10, 0), loaded.getStartTime());
        assertEquals(LocalDateTime.of(2025, 1, 1, 10, 30), loaded.getEndTime());
    }


    @Test
    @DisplayName("подзадача создаётся только при наличии связанного эпика")
    void subtaskMustHaveEpic() {
        TaskManager manager = createManager();

        Integer subId = manager.addNewSubtask(new Subtask("S", "d", TaskStatus.NEW, 999));
        assertNull(subId);
    }

    @Test
    @DisplayName("Epic статус: все подзадачи NEW -> Epic NEW")
    void epicStatus_allNew() {
        TaskManager manager = createManager();

        int epicId = manager.addNewEpic(new Epic("E", "d"));
        manager.addNewSubtask(new Subtask("S1", "d", TaskStatus.NEW, epicId));
        manager.addNewSubtask(new Subtask("S2", "d", TaskStatus.NEW, epicId));

        Optional<Epic> loadedEpic = manager.getEpic(epicId);
        assertTrue(loadedEpic.isPresent());
        assertEquals(TaskStatus.NEW, loadedEpic.get().getStatus());
    }

    @Test
    @DisplayName("Epic статус: все подзадачи DONE -> Epic DONE")
    void epicStatus_allDone() {
        TaskManager manager = createManager();

        int epicId = manager.addNewEpic(new Epic("E", "d"));
        manager.addNewSubtask(new Subtask("S1", "d", TaskStatus.DONE, epicId));
        manager.addNewSubtask(new Subtask("S2", "d", TaskStatus.DONE, epicId));

        Optional<Epic> loadedEpic = manager.getEpic(epicId);
        assertTrue(loadedEpic.isPresent());
        assertEquals(TaskStatus.DONE, loadedEpic.get().getStatus());
    }

    @Test
    @DisplayName("Epic статус: NEW и DONE -> Epic IN_PROGRESS")
    void epicStatus_newAndDone() {
        TaskManager manager = createManager();

        int epicId = manager.addNewEpic(new Epic("E", "d"));
        manager.addNewSubtask(new Subtask("S1️1", "d", TaskStatus.NEW, epicId));
        manager.addNewSubtask(new Subtask("S2", "d", TaskStatus.DONE, epicId));

        Optional<Epic> loadedEpic = manager.getEpic(epicId);
        assertTrue(loadedEpic.isPresent());
        assertEquals(TaskStatus.IN_PROGRESS, loadedEpic.get().getStatus());
    }

    @Test
    @DisplayName("Epic статус: есть IN_PROGRESS -> Epic IN_PROGRESS")
    void epicStatus_hasInProgress() {
        TaskManager manager = createManager();

        int epicId = manager.addNewEpic(new Epic("E", "d"));
        manager.addNewSubtask(new Subtask("S1", "d", TaskStatus.IN_PROGRESS, epicId));
        manager.addNewSubtask(new Subtask("S2", "d", TaskStatus.NEW, epicId));

        Optional<Epic> loadedEpic = manager.getEpic(epicId);
        assertTrue(loadedEpic.isPresent());
        assertEquals(TaskStatus.IN_PROGRESS, loadedEpic.get().getStatus());
    }


    @Test
    @DisplayName("prioritized: сортировка по startTime + задачи без startTime не попадают")
    void prioritizedTasks_sortedAndNullStartExcluded() {
        TaskManager manager = createManager();

        Task t1 = new Task("T1", "d", TaskStatus.NEW);
        t1.setStartTime(LocalDateTime.of(2025, 1, 1, 12, 0));
        t1.setDuration(Duration.ofMinutes(10));

        Task t2 = new Task("T2", "d", TaskStatus.NEW);
        t2.setStartTime(LocalDateTime.of(2025, 1, 1, 11, 0));
        t2.setDuration(Duration.ofMinutes(10));

        Task tNoTime = new Task("T3", "d", TaskStatus.NEW); // startTime=null

        manager.addNewTask(t1);
        manager.addNewTask(t2);
        manager.addNewTask(tNoTime);

        List<Task> prioritized = manager.getPrioritizedTasks();
        assertEquals(2, prioritized.size(), "задача без startTime не должна попасть в prioritized");
        assertEquals("T2", prioritized.get(0).getName());
        assertEquals("T1", prioritized.get(1).getName());
    }

    @Test
    @DisplayName("пересечение интервалов: пересекающиеся задачи должны вызывать исключение")
    void intersections_shouldThrow() {
        TaskManager manager = createManager();

        Task t1 = new Task("A", "d", TaskStatus.NEW);
        t1.setStartTime(LocalDateTime.of(2025, 1, 1, 10, 0));
        t1.setDuration(Duration.ofMinutes(60));
        manager.addNewTask(t1);

        Task t2 = new Task("B", "d", TaskStatus.NEW);
        t2.setStartTime(LocalDateTime.of(2025, 1, 1, 10, 30));
        t2.setDuration(Duration.ofMinutes(30));

        assertThrows(ManagerValidateException.class, () -> manager.addNewTask(t2));
    }

    @Test
    @DisplayName("пересечение интервалов: стык-в-стык не считается пересечением")
    void intersections_touchingBorders_shouldNotThrow() {
        TaskManager manager = createManager();

        Task t1 = new Task("A", "d", TaskStatus.NEW);
        t1.setStartTime(LocalDateTime.of(2025, 1, 1, 10, 0));
        t1.setDuration(Duration.ofMinutes(60));
        manager.addNewTask(t1);

        Task t2 = new Task("B", "d", TaskStatus.NEW);

        t2.setStartTime(LocalDateTime.of(2025, 1, 1, 11, 0));
        t2.setDuration(Duration.ofMinutes(30));

        assertDoesNotThrow(() -> manager.addNewTask(t2));
    }
}

