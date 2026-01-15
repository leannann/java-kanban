package ru.yandex.javacourse.schedule.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTaskManagerTest extends TaskManagerTest<TaskManager> {

    @Override
    protected TaskManager createManager() {
        return Managers.getDefault();
    }

    TaskManager manager;

    @BeforeEach
    public void initManager(){
        manager = Managers.getDefault();
    }

    @Test
    public void testAddTask() {
        Task task = new Task("Test 1", "Testing task 1", TaskStatus.NEW);
        int id = manager.addNewTask(task);

        assertEquals(1, manager.getTasks().size(), "task should be added");

        Task addedTask = manager.getTasks().getFirst();
        assertEquals(id, addedTask.getId());
        assertEquals("Test 1", addedTask.getName());
        assertEquals("Testing task 1", addedTask.getDescription());
        assertEquals(TaskStatus.NEW, addedTask.getStatus());

        Optional<Task> byIdTask = manager.getTask(id);
        assertTrue(byIdTask.isPresent(), "task should be retrievable by id");
        assertEquals(id, byIdTask.get().getId());
        assertEquals("Test 1", byIdTask.get().getName());
    }


    @Test
    public void testAddTaskWithId(){
        Task task = new Task(42, "Test 1", "Testing task 1", TaskStatus.NEW);
        int returnedId = manager.addNewTask(task);

        assertEquals(1, manager.getTasks().size(), "task should be added");
        Task addedTask = manager.getTasks().getFirst();
        assertEquals(task, addedTask, "predefined task should be stored");
        assertEquals(42, returnedId, "returned id should equal predefined id");
        assertEquals(42, task.getId(), "predefined id should be kept");
    }

    @Test
    public void testAddTaskWithAndWithoutId() {
        Task task0 = new Task("Test 1", "Testing task 1", TaskStatus.NEW);
        int id0 = manager.addNewTask(task0);
        assertEquals(1, id0);

        Task task1 = new Task(1, "Test 2", "Testing task 2", TaskStatus.NEW);
        int id1 = manager.addNewTask(task1);
        assertEquals(1, id1);

        assertEquals(1, manager.getTasks().size(), "should be a single record with id=1 after update strategy");

        Optional<Task> storedOpt = manager.getTask(1);
        assertTrue(storedOpt.isPresent(), "task with id=1 must exist");

        Task stored = storedOpt.get();
        assertEquals("Test 2", stored.getName(), "record with id=1 should be updated to second task");

        assertEquals(1, id0, "first returned id should be 1");
        assertEquals(1, id1, "second returned id should be 1 as well");
    }


    @Test
    public void checkTaskNotChangedAfterAddTask() {
        int id = 1;
        String name = "Test 1";
        String description = "Testing task 1";
        TaskStatus status = TaskStatus.NEW;

        Task task1before = new Task(id, name, description, status);
        manager.addNewTask(task1before);

        Optional<Task> taskOpt = manager.getTask(task1before.getId());
        assertTrue(taskOpt.isPresent(), "task should be found by id");

        Task task1after = taskOpt.get();
        assertEquals(id, task1after.getId());
        assertEquals(description, task1after.getDescription());
        assertEquals(status, task1after.getStatus());
        assertEquals(name, task1after.getName());
    }

}

