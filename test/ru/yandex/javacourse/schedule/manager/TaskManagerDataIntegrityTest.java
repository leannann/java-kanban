package ru.yandex.javacourse.schedule.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TaskManagerDataIntegrityTest {

    private TaskManager manager;

    @BeforeEach
    void setUp() {
        manager = Managers.getDefault();
    }

    @Test
    void deleteSubtask_removesFromEpic_andFromHistory() {
        int epicId = manager.addNewEpic(new Epic("E1", "desc"));
        Integer s1 = manager.addNewSubtask(new Subtask("S1", "d1", TaskStatus.NEW, epicId));
        Integer s2 = manager.addNewSubtask(new Subtask("S2", "d2", TaskStatus.NEW, epicId));

        manager.getEpic(epicId);
        manager.getSubtask(s1);
        manager.getSubtask(s2);
        assertEquals(3, manager.getHistory().size());

        manager.deleteSubtask(s1);
        assertNull(manager.getSubtask(s1));
        assertEquals(List.of(epicId, s2), manager.getHistory().stream().map(Task::getId).toList());

        List<Integer> ids = manager.getEpic(epicId).getSubtaskIds();
        assertFalse(ids.contains(s1), "в эпике не должен оставаться id удалённой сабтаски");
    }

    @Test
    void deleteEpic_removesAllItsSubtasks_andHistoryClean() {
        int epicId = manager.addNewEpic(new Epic("E1", "d"));
        Integer s1 = manager.addNewSubtask(new Subtask("S1", "d1", TaskStatus.NEW, epicId));
        Integer s2 = manager.addNewSubtask(new Subtask("S2", "d2", TaskStatus.NEW, epicId));

        manager.getSubtask(s1);
        manager.getSubtask(s2);
        manager.getEpic(epicId);
        assertEquals(3, manager.getHistory().size());

        manager.deleteEpic(epicId);

        assertNull(manager.getEpic(epicId));
        assertNull(manager.getSubtask(s1));
        assertNull(manager.getSubtask(s2));

        assertTrue(manager.getHistory().isEmpty(), "удаление эпика должно убрать его и сабтаски из истории");
    }

    @Test
    void deleteSubtasks_bulk_removesIdsFromAllEpics() {
        int e1 = manager.addNewEpic(new Epic("E1", "d"));
        int e2 = manager.addNewEpic(new Epic("E2", "d"));

        Integer s11 = manager.addNewSubtask(new Subtask("S11", "x", TaskStatus.NEW, e1));
        Integer s12 = manager.addNewSubtask(new Subtask("S12", "x", TaskStatus.NEW, e1));
        Integer s21 = manager.addNewSubtask(new Subtask("S21", "x", TaskStatus.NEW, e2));

        manager.getSubtask(s11);
        manager.getSubtask(s12);
        manager.getSubtask(s21);
        assertEquals(3, manager.getHistory().size());

        manager.deleteSubtasks();

        assertTrue(manager.getHistory().isEmpty());

        assertTrue(manager.getEpic(e1).getSubtaskIds().isEmpty());
        assertTrue(manager.getEpic(e2).getSubtaskIds().isEmpty());

    }

    @Test
    void epicSubtasksGetter_neverReturnsNull() {
        ArrayList<Subtask> none = (ArrayList<Subtask>) manager.getEpicSubtasks(9999);
        assertNotNull(none);
        assertTrue(none.isEmpty());
    }
}

