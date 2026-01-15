package ru.yandex.javacourse.schedule.tasks;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class TaskTest {

    @Test
    public void testEqualityById(){
        Task t0 = new Task(1, "Test 1", "Testing task 1", TaskStatus.NEW);
        Task t1 = new Task(1, "Test 2", "Testing task 2", TaskStatus.IN_PROGRESS);
        assertEquals(t0, t1, "task entities should be compared by id");
    }

    @Test
    public void testDifferentIdsNotEqual() {
        Task t0 = new Task(1, "Task A", "desc", TaskStatus.NEW);
        Task t1 = new Task(2, "Task A", "desc", TaskStatus.NEW);
        assertNotEquals(t0, t1, "tasks with different ids must not be equal");
    }

}
