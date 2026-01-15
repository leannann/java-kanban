package ru.yandex.javacourse.schedule;

import static ru.yandex.javacourse.schedule.tasks.TaskStatus.DONE;
import static ru.yandex.javacourse.schedule.tasks.TaskStatus.NEW;

import java.util.List;

import ru.yandex.javacourse.schedule.manager.Managers;
import ru.yandex.javacourse.schedule.manager.TaskManager;
import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;
import ru.yandex.javacourse.schedule.tasks.Task;

public class Main {
	public static void main(String[] args) {

		TaskManager taskManager = Managers.getDefault();

		System.out.println("=== Создание задач и эпиков ===");

		// 2 обычные задачи
		int taskId1 = taskManager.addNewTask(new Task("Задача 1", "Описание задачи 1", NEW));
		int taskId2 = taskManager.addNewTask(new Task("Задача 2", "Описание задачи 2", DONE));

		// Эпик с тремя подзадачами
		int epicIdWithSubtasks = taskManager.addNewEpic(new Epic("Эпик с подзадачами", "Три подзадачи"));
		Integer subtaskId1 = taskManager.addNewSubtask(new Subtask("Подзадача 1.1", "Описание подзадачи", NEW, epicIdWithSubtasks));
		Integer subtaskId2 = taskManager.addNewSubtask(new Subtask("Подзадача 1.2", "Описание подзадачи", NEW, epicIdWithSubtasks));
		Integer subtaskId3 = taskManager.addNewSubtask(new Subtask("Подзадача 1.3", "Описание подзадачи", DONE, epicIdWithSubtasks));

		// Эпик без подзадач
		int emptyEpicId = taskManager.addNewEpic(new Epic("Эпик без подзадач", "Просто эпик без сабтасок"));

		printCurrentState(taskManager);

		System.out.println("\n=== Запросы задач и печать истории ===");

		System.out.println("\nПросмотр задачи #1");
		taskManager.getTask(taskId1);
		printHistory(taskManager);

		System.out.println("\nПросмотр эпика с подзадачами");
		taskManager.getEpic(epicIdWithSubtasks);
		printHistory(taskManager);

		System.out.println("\nПросмотр подзадачи 1.1");
		taskManager.getSubtask(subtaskId1);
		printHistory(taskManager);

		System.out.println("\nПовторный просмотр задачи #1 (дубликатов быть не должно)");
		taskManager.getTask(taskId1);
		printHistory(taskManager);

		System.out.println("\nПросмотр подзадачи 1.2");
		taskManager.getSubtask(subtaskId2);
		printHistory(taskManager);

		System.out.println("\nПовторный просмотр подзадачи 1.1 (дубликатов быть не должно)");
		taskManager.getSubtask(subtaskId1);
		printHistory(taskManager);

		System.out.println("\nПросмотр эпика без подзадач");
		taskManager.getEpic(emptyEpicId);
		printHistory(taskManager);

		System.out.println("\n=== Удаление задачи, присутствующей в истории ===");
		System.out.println("Удаляем задачу с id=" + taskId1);
		taskManager.deleteTask(taskId1);
		printHistory(taskManager);

		System.out.println("\n=== Удаление эпика с подзадачами ===");
		System.out.println("Удаляем эпик с id=" + epicIdWithSubtasks + " и его подзадачи");
		taskManager.deleteEpic(epicIdWithSubtasks);
		printHistory(taskManager);

		System.out.println("\n=== Финальное состояние ===");
		printCurrentState(taskManager);
	}

	private static void printCurrentState(TaskManager taskManager) {
		System.out.println("\n--- Текущее состояние системы ---");

		System.out.println("Задачи:");
		for (Task task : taskManager.getTasks()) {
			System.out.println(task);
		}

		System.out.println("\nЭпики и их подзадачи:");
		for (Epic epic : taskManager.getEpics()) {
			System.out.println(epic);
			List<Subtask> subtasks = taskManager.getEpicSubtasks(epic.getId());
			for (Subtask subtask : subtasks) {
				System.out.println("   -> " + subtask);
			}
		}

		System.out.println("\nПодзадачи:");
		for (Subtask subtask : taskManager.getSubtasks()) {
			System.out.println(subtask);
		}

		System.out.println("\nИстория просмотров:");
		printHistory(taskManager);

		System.out.println("--- Конец состояния ---\n");
	}

	private static void printHistory(TaskManager taskManager) {
		System.out.println("История просмотров (от старых к новым):");
		if (taskManager.getHistory().isEmpty()) {
			System.out.println("   [история пуста]");
			return;
		}

		int index = 1;
		for (Task task : taskManager.getHistory()) {
			System.out.println("   " + (index++) + ") " + formatTask(task));
		}
	}

	private static String formatTask(Task task) {
		String type = (task instanceof Subtask) ? "Подзадача" :
				(task instanceof Epic) ? "Эпик" : "Задача";
		return type + " {id=" + task.getId() + ", name='" + task.getName() + "', status=" + task.getStatus() + "}";
	}
}
