package ru.yandex.javacourse.schedule.tasks;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class EpicTest {

    @Test
    public void testEqualityById() {
        Epic e0 = new Epic(1, "Test 1", "Testing task 1");
        Epic e1 = new Epic(1, "Test 2", "Testing task 2");
        assertEquals(e0, e1, "Epics should be compared by id only");
    }

    @Test
    public void testSubtaskIdsDoNotAllowDuplicates() {
        Epic epic = new Epic(0, "Epic 1", "Testing epic 1");
        epic.addSubtaskId(1);
        epic.addSubtaskId(2);
        epic.addSubtaskId(1);

        assertEquals(2, epic.getSubtaskIds().size(), "дубликаты сабтасок не должны добавляться повторно");
    }
}

