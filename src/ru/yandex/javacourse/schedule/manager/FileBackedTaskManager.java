package ru.yandex.javacourse.schedule.manager;

import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;
import ru.yandex.javacourse.schedule.tasks.TaskType;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

/**
 * File-backed task manager.
 *
 * @author Andrey Terzi (terzi.andrey.sergeevich@gmail.com)
 */
public class FileBackedTaskManager extends InMemoryTaskManager implements TaskManager {

    private final File storageFile;

    public FileBackedTaskManager(File storageFile) {
        this.storageFile = storageFile;
    }

    protected void save() {
        try (BufferedWriter writer = Files.newBufferedWriter(storageFile.toPath(), StandardCharsets.UTF_8)) {
            writer.write("id,type,name,status,description,epic");
            writer.newLine();

            for (Task task : getTasks()) {
                writer.write(taskToString(task));
                writer.newLine();
            }

            for (Epic epic : getEpics()) {
                writer.write(taskToString(epic));
                writer.newLine();
            }

            for (Subtask subtask : getSubtasks()) {
                writer.write(taskToString(subtask));
                writer.newLine();
            }

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения данных в файл: " + storageFile, e);
        }
    }

    private static String taskToString(Task task) {
        StringBuilder sb = new StringBuilder();

        sb.append(task.getId()).append(",");
        sb.append(task.getType()).append(",");
        sb.append(task.getName()).append(",");
        sb.append(task.getStatus()).append(",");
        sb.append(task.getDescription() == null ? "" : task.getDescription()).append(",");

        if (task.getType() == TaskType.SUBTASK) {
            Subtask sub = (Subtask) task;
            sb.append(sub.getEpicId());
        }

        return sb.toString();
    }

    private static Task taskFromString(String value) {
        String[] fields = value.split(",", -1);
        int id = Integer.parseInt(fields[0]);
        TaskType type = TaskType.valueOf(fields[1]);
        String name = fields[2];
        TaskStatus status = TaskStatus.valueOf(fields[3]);
        String description = fields[4];
        String epicField = fields.length > 5 ? fields[5] : "";

        return switch (type) {
            case TASK -> new Task(id, name, description, status);
            case EPIC -> new Epic(id, name, description);
            case SUBTASK -> {
                int epicId = Integer.parseInt(epicField);
                yield new Subtask(id, name, description, status, epicId);
            }
        };
    }

    public static FileBackedTaskManager loadFromFile(File storageFile) {
        FileBackedTaskManager manager = new FileBackedTaskManager(storageFile);

        if (!storageFile.exists()) {
            return manager;
        }

        List<String> lines;
        try {
            lines = Files.readAllLines(storageFile.toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка чтения файла: " + storageFile, e);
        }

        if (lines.isEmpty()) {
            return manager;
        }


        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) {
                continue;
            }
            Task task = taskFromString(line);
            switch (task.getType()) {
                case TASK -> manager.addNewTask(task);
                case EPIC -> manager.addNewEpic((Epic) task);
                case SUBTASK -> manager.addNewSubtask((Subtask) task);
            }
        }

        return manager;
    }

    @Override
    public int addNewTask(Task task) {
        int id = super.addNewTask(task);
        save();
        return id;
    }

    @Override
    public int addNewEpic(Epic epic) {
        int id = super.addNewEpic(epic);
        save();
        return id;
    }

    @Override
    public Integer addNewSubtask(Subtask subtask) {
        Integer id = super.addNewSubtask(subtask);
        save();
        return id;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
    }

    @Override
    public void deleteSubtask(int id) {
        super.deleteSubtask(id);
        save();
    }

    @Override
    public void deleteTasks() {
        super.deleteTasks();
        save();
    }

    @Override
    public void deleteSubtasks() {
        super.deleteSubtasks();
        save();
    }

    @Override
    public void deleteEpics() {
        super.deleteEpics();
        save();
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

