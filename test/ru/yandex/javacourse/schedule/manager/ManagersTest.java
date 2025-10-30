package ru.yandex.javacourse.schedule.manager;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ManagersTest {

    @Test
    public void testDefaultManagersNotNull() {
        TaskManager taskManager = Managers.getDefault();
        HistoryManager historyManager = Managers.getDefaultHistory();

        assertNotNull(taskManager, "Managers.getDefault() должен возвращать не null");
        assertNotNull(historyManager, "Managers.getDefaultHistory() должен возвращать не null");
    }

    @Test
    public void testDefaultManagersReturnCorrectTypes() {
        TaskManager tm = Managers.getDefault();
        HistoryManager hm = Managers.getDefaultHistory();

        assertTrue(tm instanceof InMemoryTaskManager,
                "Managers.getDefault() должен возвращать InMemoryTaskManager");
        assertTrue(hm instanceof InMemoryHistoryManager,
                "Managers.getDefaultHistory() должен возвращать InMemoryHistoryManager");
    }

    @Test
    public void testDefaultManagersReturnNewInstances() {
        TaskManager tm1 = Managers.getDefault();
        TaskManager tm2 = Managers.getDefault();

        assertNotSame(tm1, tm2, "Каждый вызов Managers.getDefault() должен возвращать новый экземпляр TaskManager");
    }
}
