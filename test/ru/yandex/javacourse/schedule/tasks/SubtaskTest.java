package ru.yandex.javacourse.schedule.tasks;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SubtaskTest {

    @Test
    public void testEqualityById() {
        Subtask s0 = new Subtask(1, "Test 1", "Testing task 1", TaskStatus.NEW, 10);
        Subtask s1 = new Subtask(1, "Test 2", "Testing task 2", TaskStatus.IN_PROGRESS, 20);
        assertEquals(s0, s1, "Subtasks should be equal if their ids are equal");
    }

    @Test
    public void testEpicIdIsStoredCorrectly() {
        Subtask subtask = new Subtask(10, "Subtask 1", "Testing subtask 1", TaskStatus.NEW, 99);
        assertEquals(10, subtask.getId());
        assertEquals(99, subtask.getEpicId(), "epicId must be preserved as passed to constructor");
    }

    @Test
    public void testToStringContainsEpicId() {
        Subtask subtask = new Subtask(5, "S", "D", TaskStatus.NEW, 42);
        String s = subtask.toString();
        assertTrue(s.contains("epicId=42") || s.contains("epicId=42".replace("=", "=")), "toString should include epicId");
    }
}
