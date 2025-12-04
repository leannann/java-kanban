package ru.yandex.javacourse.schedule.manager;

import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest {

    private File createTempFile() throws IOException {
        File file = File.createTempFile("tasks", ".csv");
        file.deleteOnExit();
        return file;
    }

    @Test
    public void shouldLoadEmptyManagerFromEmptyFile() throws IOException {
        File file = createTempFile();

        FileBackedTaskManager manager = FileBackedTaskManager.loadFromFile(file);

        assertTrue(manager.getTasks().isEmpty(), "ожидаем отсутствие задач");
        assertTrue(manager.getEpics().isEmpty(), "ожидаем отсутствие эпиков");
        assertTrue(manager.getSubtasks().isEmpty(), "ожидаем отсутствие подзадач");
    }

    @Test
    public void shouldSaveAndLoadSingleTask() throws IOException {
        File file = createTempFile();

        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        Task task = new Task("Test task", "Description", TaskStatus.NEW);
        int id = manager.addNewTask(task);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);

        assertEquals(1, loaded.getTasks().size(), "должна загрузиться одна задача");
        Task loadedTask = loaded.getTask(id);
        assertNotNull(loadedTask, "задача должна быть найдена по id");
        assertEquals("Test task", loadedTask.getName());
        assertEquals("Description", loadedTask.getDescription());
        assertEquals(TaskStatus.NEW, loadedTask.getStatus());
    }

    @Test
    public void shouldSaveAndLoadMultipleTasksEpicsAndSubtasks() throws IOException {
        File file = createTempFile();

        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        Task task1 = new Task("Task 1", "Desc 1", TaskStatus.NEW);
        int taskId1 = manager.addNewTask(task1);

        Epic epic = new Epic("Epic 1", "Epic desc");
        int epicId = manager.addNewEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Sub desc 1", TaskStatus.NEW, epicId);
        Subtask subtask2 = new Subtask("Subtask 2", "Sub desc 2", TaskStatus.DONE, epicId);

        int subtaskId1 = manager.addNewSubtask(subtask1);
        int subtaskId2 = manager.addNewSubtask(subtask2);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);

        assertEquals(1, loaded.getTasks().size(), "должна загрузиться одна обычная задача");
        assertEquals(1, loaded.getEpics().size(), "должен загрузиться один эпик");
        assertEquals(2, loaded.getSubtasks().size(), "должны загрузиться две подзадачи");

        Task loadedTask1 = loaded.getTask(taskId1);
        assertNotNull(loadedTask1);
        assertEquals("Task 1", loadedTask1.getName());
        assertEquals(TaskStatus.NEW, loadedTask1.getStatus());

        Epic loadedEpic = loaded.getEpic(epicId);
        assertNotNull(loadedEpic);
        assertEquals("Epic 1", loadedEpic.getName());

        Subtask loadedSubtask1 = loaded.getSubtask(subtaskId1);
        Subtask loadedSubtask2 = loaded.getSubtask(subtaskId2);

        assertNotNull(loadedSubtask1);
        assertNotNull(loadedSubtask2);

        assertEquals("Subtask 1", loadedSubtask1.getName());
        assertEquals(TaskStatus.NEW, loadedSubtask1.getStatus());
        assertEquals(epicId, loadedSubtask1.getEpicId(), "у подзадачи должен сохраниться epicId");

        assertEquals("Subtask 2", loadedSubtask2.getName());
        assertEquals(TaskStatus.DONE, loadedSubtask2.getStatus());
        assertEquals(epicId, loadedSubtask2.getEpicId(), "у подзадачи должен сохраниться epicId");
    }

    public static void main(String[] args) {
        File storageFile = new File("tasks.csv");

        // 1. Создаём менеджер и добавляем в него задачи
        FileBackedTaskManager originalManager = new FileBackedTaskManager(storageFile);

        int taskId1 = originalManager.addNewTask(
                new Task("Задача 1", "Описание задачи 1", TaskStatus.NEW)
        );
        int taskId2 = originalManager.addNewTask(
                new Task("Задача 2", "Описание задачи 2", TaskStatus.DONE)
        );

        int epicId1 = originalManager.addNewEpic(
                new Epic("Эпик 1", "Эпик с подзадачами")
        );
        int epicId2 = originalManager.addNewEpic(
                new Epic("Эпик 2", "Эпик без подзадач")
        );

        int subtaskId1 = originalManager.addNewSubtask(
                new Subtask("Подзадача 1.1", "Описание подзадачи 1.1", TaskStatus.NEW, epicId1)
        );
        int subtaskId2 = originalManager.addNewSubtask(
                new Subtask("Подзадача 1.2", "Описание подзадачи 1.2", TaskStatus.IN_PROGRESS, epicId1)
        );

        System.out.println("=== ОРИГИНАЛЬНЫЙ МЕНЕДЖЕР ===");
        printManagerState(originalManager);

        // 2. Создаём новый менеджер из того же файла
        FileBackedTaskManager restoredManager = FileBackedTaskManager.loadFromFile(storageFile);

        System.out.println("\n=== ВОССТАНОВЛЕННЫЙ МЕНЕДЖЕР ===");
        printManagerState(restoredManager);

        // 3. Простая проверка: количество сущностей
        boolean sameTasksCount = originalManager.getTasks().size() == restoredManager.getTasks().size();
        boolean sameEpicsCount = originalManager.getEpics().size() == restoredManager.getEpics().size();
        boolean sameSubtasksCount = originalManager.getSubtasks().size() == restoredManager.getSubtasks().size();

        System.out.println("\n=== ПРОВЕРКА ===");
        System.out.println("Совпало количество задач:      " + sameTasksCount);
        System.out.println("Совпало количество эпиков:     " + sameEpicsCount);
        System.out.println("Совпало количество подзадач:   " + sameSubtasksCount);
    }

    private static void printManagerState(FileBackedTaskManager manager) {
        System.out.println("Задачи:");
        for (Task task : manager.getTasks()) {
            System.out.println("  " + task);
        }

        System.out.println("Эпики и их подзадачи:");
        for (Epic epic : manager.getEpics()) {
            System.out.println("  " + epic);
            for (Subtask subtask : manager.getEpicSubtasks(epic.getId())) {
                System.out.println("    -> " + subtask);
            }
        }

        System.out.println("Подзадачи (общий список):");
        for (Subtask subtask : manager.getSubtasks()) {
            System.out.println("  " + subtask);
        }
    }
}

