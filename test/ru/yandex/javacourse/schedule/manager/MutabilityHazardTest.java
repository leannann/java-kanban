package ru.yandex.javacourse.schedule.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

import static org.junit.jupiter.api.Assertions.*;

public class MutabilityHazardTest {

    private TaskManager manager;

    @BeforeEach
    void setUp() {
        manager = Managers.getDefault();
    }

    @Test
    void changingTaskIdAfterAdd_doesNotAffectManager() {
        Task t = new Task("A", "desc", TaskStatus.NEW);
        int id = manager.addNewTask(t);

        assertNotEquals(id, t.getId(), "менеджер не обязан проставлять id во внешний объект");

        t.setId(10);

        assertNull(manager.getTask(10), "по новому id во внешнем объекте задача не найдётся");
        assertNotNull(manager.getTask(id), "по старому id задача найдётся (хранится копия в менеджере)");
    }

    @Test
    void changingSubtaskStatus_updatesEpicStatus() {
        int epicId = manager.addNewEpic(new Epic("E", "d"));
        Integer s1 = manager.addNewSubtask(new Subtask("S1", "d1", TaskStatus.NEW, epicId));
        Integer s2 = manager.addNewSubtask(new Subtask("S2", "d2", TaskStatus.NEW, epicId));

        assertEquals(TaskStatus.NEW, manager.getEpic(epicId).getStatus(), "все NEW -> эпик NEW");

        Subtask st1 = manager.getSubtask(s1);
        st1.setStatus(TaskStatus.DONE);
        manager.updateSubtask(st1);

        assertEquals(TaskStatus.IN_PROGRESS, manager.getEpic(epicId).getStatus(), "смешанные -> IN_PROGRESS");

        Subtask st2 = manager.getSubtask(s2);
        st2.setStatus(TaskStatus.DONE);
        manager.updateSubtask(st2);

        assertEquals(TaskStatus.DONE, manager.getEpic(epicId).getStatus(), "все DONE -> эпик DONE");
    }

    @Test
    void reassigningSubtaskToAnotherEpic_viaManager_updatesLinks() {
        int e1 = manager.addNewEpic(new Epic("E1", "d"));
        int e2 = manager.addNewEpic(new Epic("E2", "d"));

        Integer s = manager.addNewSubtask(new Subtask("S", "d", TaskStatus.NEW, e1));
        assertTrue(manager.getEpic(e1).getSubtaskIds().contains(s));

        Subtask updated = new Subtask(s, "S", "d", TaskStatus.NEW, e2);
        manager.addNewSubtask(updated);

        assertFalse(manager.getEpic(e1).getSubtaskIds().contains(s), "из старого эпика id должен быть удалён");
        assertTrue(manager.getEpic(e2).getSubtaskIds().contains(s), "в новый эпик id должен быть добавлен");
    }
}
