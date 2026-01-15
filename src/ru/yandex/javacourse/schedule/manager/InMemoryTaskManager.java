package ru.yandex.javacourse.schedule.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

public class InMemoryTaskManager implements TaskManager {

	private final Map<Integer, Task> tasks = new HashMap<>();
	private final Map<Integer, Epic> epics = new HashMap<>();
	private final Map<Integer, Subtask> subtasks = new HashMap<>();
	private int generatorId = 0;

	private final HistoryManager historyManager = Managers.getDefaultHistory();

	private Task copyTask(Task t) {
		if (t == null) return null;
		return new Task(t.getId(), t.getName(), t.getDescription(), t.getStatus());
	}

	private Epic copyEpic(Epic epic) {
		if (epic == null) return null;
		Epic copyEpic = new Epic(epic.getId(), epic.getName(), epic.getDescription());
		copyEpic.setStatus(epic.getStatus());
		for (Integer sid : epic.getSubtaskIds()) {
			copyEpic.addSubtaskId(sid);
		}
		return copyEpic;
	}

	private Subtask copySubtask(Subtask subtask) {
		if (subtask == null) return null;
		return new Subtask(subtask.getId(), subtask.getName(), subtask.getDescription(), subtask.getStatus(), subtask.getEpicId());
	}

	@Override
	public ArrayList<Task> getTasks() {
		ArrayList<Task> list = new ArrayList<>();
		for (Task task : tasks.values()) list.add(copyTask(task));
		return list;
	}

	@Override
	public ArrayList<Subtask> getSubtasks() {
		ArrayList<Subtask> list = new ArrayList<>();
		for (Subtask subtask : subtasks.values()) list.add(copySubtask(subtask));
		return list;
	}

	@Override
	public ArrayList<Epic> getEpics() {
		ArrayList<Epic> list = new ArrayList<>();
		for (Epic epic : epics.values()) list.add(copyEpic(epic));
		return list;
	}

	@Override
	public ArrayList<Subtask> getEpicSubtasks(int epicId) {
		ArrayList<Subtask> epicSubtasks = new ArrayList<>();
		Epic currentEpic = epics.get(epicId);
		if (currentEpic == null) return epicSubtasks;

		for (int subtaskId : currentEpic.getSubtaskIds()) {
			Subtask subtask = subtasks.get(subtaskId);
			if (subtask != null) {
				epicSubtasks.add(copySubtask(subtask));
			}
		}
		return epicSubtasks;
	}

	@Override
	public Task getTask(int id) {
		final Task task = tasks.get(id);
		if (task != null) historyManager.add(task);
		return copyTask(task);
	}

	@Override
	public Subtask getSubtask(int id) {
		final Subtask subtask = subtasks.get(id);
		if (subtask != null) historyManager.add(subtask);
		return copySubtask(subtask);
	}

	@Override
	public Epic getEpic(int id) {
		final Epic epic = epics.get(id);
		if (epic != null) historyManager.add(epic);
		return copyEpic(epic);
	}

	@Override
	public int addNewTask(Task task) {
		if (task == null) return -1;
		int id = task.getId();
		if (id <= 0) {
			id = ++generatorId;
			Task copy = copyTask(task);
			copy.setId(id);
			tasks.put(id, copy);
			return id;
		}
		if (tasks.containsKey(id)) {
			tasks.put(id, copyTask(task));
		} else {
			tasks.put(id, copyTask(task));
			if (id > generatorId) generatorId = id;
		}
		return id;
	}

	@Override
	public int addNewEpic(Epic epic) {
		if (epic == null) return -1;
		int id = epic.getId();
		if (id <= 0) {
			id = ++generatorId;
			Epic copy = copyEpic(epic);
			copy.setId(id);
			epics.put(id, copy);
			return id;
		}
		if (epics.containsKey(id)) {
			epics.put(id, copyEpic(epic));
		} else {
			epics.put(id, copyEpic(epic));
			if (id > generatorId) generatorId = id;
		}
		return id;
	}

	@Override
	public Integer addNewSubtask(Subtask subtask) {
		if (subtask == null) return null;

		final int targetEpicId = subtask.getEpicId();
		Epic targetEpic = epics.get(targetEpicId);
		if (targetEpic == null) return null;

		int id = subtask.getId();
		if (id <= 0) {
			id = ++generatorId;
			Subtask copy = copySubtask(subtask);
			copy.setId(id);
			subtasks.put(id, copy);
			targetEpic.addSubtaskId(id);
			updateEpicStatus(targetEpicId);
			return id;
		}

		if (subtasks.containsKey(id)) {
			Subtask prev = subtasks.get(id);
			int prevEpicId = prev.getEpicId();

			if (prevEpicId != targetEpicId) {
				Epic prevEpic = epics.get(prevEpicId);
				if (prevEpic != null) {
					prevEpic.removeSubtaskId(id);
					updateEpicStatus(prevEpicId);
				}
				targetEpic.addSubtaskId(id);
			}
			subtasks.put(id, copySubtask(subtask));
			if (id > generatorId) generatorId = id;
			updateEpicStatus(targetEpicId);
		} else {
			subtasks.put(id, copySubtask(subtask));
			if (id > generatorId) generatorId = id;
			targetEpic.addSubtaskId(id);
			updateEpicStatus(targetEpicId);
		}
		return id;
	}

	@Override
	public void updateTask(Task task) {
		if (task == null) return;
		final int id = task.getId();
		if (!tasks.containsKey(id)) return;
		tasks.put(id, copyTask(task));
	}

	@Override
	public void updateEpic(Epic epic) {
		if (epic == null) return;
		final int id = epic.getId();
		Epic saved = epics.get(id);
		if (saved == null) return;
		saved.setName(epic.getName());
		saved.setDescription(epic.getDescription());
	}

	@Override
	public void updateSubtask(Subtask subtask) {
		if (subtask == null) return;
		final int id = subtask.getId();
		final int epicId = subtask.getEpicId();

		Subtask saved = subtasks.get(id);
		if (saved == null) return;

		Epic epic = epics.get(epicId);
		if (epic == null) return;

		subtasks.put(id, copySubtask(subtask));
		updateEpicStatus(epicId);
	}

	@Override
	public void deleteTask(int id) {
		tasks.remove(id);
		historyManager.remove(id);
	}

	@Override
	public void deleteEpic(int id) {
		final Epic epic = epics.remove(id);
		if (epic == null) return;
		for (Integer subtaskId : epic.getSubtaskIds()) {
			subtasks.remove(subtaskId);
			historyManager.remove(subtaskId);
		}
		historyManager.remove(id);
	}

	@Override
	public void deleteSubtask(int id) {
		Subtask subtask = subtasks.remove(id);
		if (subtask == null) return;
		Epic epic = epics.get(subtask.getEpicId());
		if (epic != null) {
			epic.removeSubtaskId(id);
			updateEpicStatus(epic.getId());
		}
		historyManager.remove(id);
	}

	@Override
	public void deleteTasks() {
		for (Integer taskId : new ArrayList<>(tasks.keySet())) {
			historyManager.remove(taskId);
		}
		tasks.clear();
	}

	@Override
	public void deleteSubtasks() {
		for (Integer subId : new ArrayList<>(subtasks.keySet())) {
			historyManager.remove(subId);
		}
		for (Epic epic : epics.values()) {
			epic.clearSubtaskIds();
			updateEpicStatus(epic.getId());
		}
		subtasks.clear();
	}

	@Override
	public void deleteEpics() {
		for (Epic epic : epics.values()) {
			for (Integer subId : epic.getSubtaskIds()) {
				historyManager.remove(subId);
			}
			historyManager.remove(epic.getId());
		}
		epics.clear();
		subtasks.clear();
	}

	@Override
	public List<Task> getHistory() {
		List<Task> raw = historyManager.getHistory();
		List<Task> copies = new ArrayList<>(raw.size());

		for (Task task : raw) {
			switch (task.getType()) {
				case SUBTASK -> copies.add(copySubtask((Subtask) task));
				case EPIC    -> copies.add(copyEpic((Epic) task));
				case TASK    -> copies.add(copyTask(task));
			}
		}

		return copies;
	}


	private void updateEpicStatus(int epicId) {
		Epic currentEpic = epics.get(epicId);
		if (currentEpic == null) return;

		List<Integer> subtaskIds = currentEpic.getSubtaskIds();
		if (subtaskIds.isEmpty()) {
			currentEpic.setStatus(TaskStatus.NEW);
			return;
		}

		boolean allSubtasksNew = true;
		boolean allSubtasksDone = true;

		for (int subtaskId : subtaskIds) {
			final Subtask subtask = subtasks.get(subtaskId);
			if (subtask == null) continue;

			TaskStatus subtaskStatus = subtask.getStatus();

			if (subtaskStatus != TaskStatus.NEW)  allSubtasksNew = false;
			if (subtaskStatus != TaskStatus.DONE) allSubtasksDone = false;

			if (!allSubtasksNew && !allSubtasksDone) {
				currentEpic.setStatus(TaskStatus.IN_PROGRESS);
				return;
			}
		}

		if (allSubtasksDone) {
			currentEpic.setStatus(TaskStatus.DONE);
		} else if (allSubtasksNew) {
			currentEpic.setStatus(TaskStatus.NEW);
		} else {
			currentEpic.setStatus(TaskStatus.IN_PROGRESS);
		}
	}
}


