package ru.yandex.javacourse.schedule.manager;

import java.util.*;
import java.time.Duration;
import java.time.LocalDateTime;

import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;
import ru.yandex.javacourse.schedule.exceptions.ManagerValidateException;

public class InMemoryTaskManager implements TaskManager {

	private final Map<Integer, Task> tasks = new HashMap<>();
	private final Map<Integer, Epic> epics = new HashMap<>();
	private final Map<Integer, Subtask> subtasks = new HashMap<>();
	private int generatorId = 0;

	private final HistoryManager historyManager = Managers.getDefaultHistory();


	private static final LocalDateTime BASE_TIME = LocalDateTime.of(2025, 1, 1, 0, 0);
	private static final int MINUTES_IN_YEAR = 365 * 24 * 60;
	private final BitSet occupiedMinutes = new BitSet(MINUTES_IN_YEAR);


	private int toMinuteIndex(LocalDateTime time) {
		return (int) Duration.between(BASE_TIME, time).toMinutes();
	}


	private boolean isIntervalFree(Task task) {
		if (task.getStartTime() == null || task.getEndTime() == null) return true;
		int start = toMinuteIndex(task.getStartTime());
		int end = toMinuteIndex(task.getEndTime());
		return occupiedMinutes.nextSetBit(start) >= end || occupiedMinutes.nextSetBit(start) == -1;
	}


	private void markIntervals(Task task, boolean occupy) {
		if (task.getStartTime() == null || task.getEndTime() == null) return;
		int start = toMinuteIndex(task.getStartTime());
		int end = toMinuteIndex(task.getEndTime());
		occupiedMinutes.set(start, end, occupy);
	}

	private Task copyTask(Task t) {
		if (t == null) return null;

		Task copy = new Task(t.getId(), t.getName(), t.getDescription(), t.getStatus());
		copy.setDuration(t.getDuration());
		copy.setStartTime(t.getStartTime());
		return copy;
	}

	private Epic copyEpic(Epic epic) {
		if (epic == null) return null;

		Epic copyEpic = new Epic(epic.getId(), epic.getName(), epic.getDescription());
		copyEpic.setStatus(epic.getStatus());

		for (Integer sid : epic.getSubtaskIds()) {
			copyEpic.addSubtaskId(sid);
		}

		copyEpic.setCalculatedFields(epic.getDuration(), epic.getStartTime(), epic.getEndTime());

		return copyEpic;
	}

	private Subtask copySubtask(Subtask subtask) {
		if (subtask == null) return null;

		Subtask copy = new Subtask(
				subtask.getId(),
				subtask.getName(),
				subtask.getDescription(),
				subtask.getStatus(),
				subtask.getEpicId()
		);
		copy.setDuration(subtask.getDuration());
		copy.setStartTime(subtask.getStartTime());
		return copy;
	}

	private final NavigableSet<Task> prioritizedTasks = new TreeSet<>(
			Comparator.comparing(Task::getStartTime)
					.thenComparingInt(Task::getId)
	);

	private void addToPrioritized(Task task) {
		if (task == null || task.getStartTime() == null) return;
		prioritizedTasks.add(task);
		markIntervals(task, true);
	}

	private void removeFromPrioritized(Task task) {
		if (task == null || task.getStartTime() == null) return;
		prioritizedTasks.remove(task);
		markIntervals(task, false);
	}

	@Override
	public ArrayList<Task> getTasks() {
		return tasks.values().stream()
				.map(this::copyTask)
				.collect(java.util.stream.Collectors.toCollection(ArrayList::new));
	}

	@Override
	public ArrayList<Subtask> getSubtasks() {
		return subtasks.values().stream()
				.map(this::copySubtask)
				.collect(java.util.stream.Collectors.toCollection(ArrayList::new));
	}

	@Override
	public ArrayList<Epic> getEpics() {
		return epics.values().stream()
				.map(this::copyEpic)
				.collect(java.util.stream.Collectors.toCollection(ArrayList::new));
	}

	@Override
	public ArrayList<Subtask> getEpicSubtasks(int epicId) {
		Epic epic = epics.get(epicId);
		if (epic == null) return new ArrayList<>();

		return epic.getSubtaskIds().stream()
				.map(subtasks::get)
				.filter(java.util.Objects::nonNull)
				.map(this::copySubtask)
				.collect(java.util.stream.Collectors.toCollection(ArrayList::new));
	}

	@Override
	public Optional<Task> getTask(int id) {
		Task task = tasks.get(id);
		if (task != null) {
			historyManager.add(task);
			return Optional.of(copyTask(task));
		}
		return Optional.empty();
	}


	@Override
	public Optional<Subtask> getSubtask(int id) {
		Subtask subtask = subtasks.get(id);
		if (subtask != null) {
			historyManager.add(subtask);
			return Optional.of(copySubtask(subtask));
		}
		return Optional.empty();
	}

	@Override
	public Optional<Epic> getEpic(int id) {
		Epic epic = epics.get(id);
		if (epic != null) {
			historyManager.add(epic);
			return Optional.of(copyEpic(epic));
		}
		return Optional.empty();
	}


	@Override
	public int addNewTask(Task task) {
		if (task == null) return -1;
		int id = ++generatorId;
		Task copy = copyTask(task);
		copy.setId(id);
		validateNoIntersections(copy);
		tasks.put(id, copy);
		addToPrioritized(copy);
		return id;
	}

	@Override
	public int addNewEpic(Epic epic) {
		if (epic == null) return -1;
		int id = ++generatorId;
		Epic copy = copyEpic(epic);
		copy.setId(id);
		epics.put(id, copy);
		return id;
	}

	@Override
	public Integer addNewSubtask(Subtask subtask) {
		if (subtask == null) return null;

		int targetEpicId = subtask.getEpicId();
		Epic targetEpic = epics.get(targetEpicId);
		if (targetEpic == null) return null;

		int id = subtask.getId();

		if (id <= 0 || !subtasks.containsKey(id)) {
			id = ++generatorId;
			Subtask copy = copySubtask(subtask);
			copy.setId(id);

			validateNoIntersections(copy);
			subtasks.put(id, copy);
			addToPrioritized(copy);
			targetEpic.addSubtaskId(id);
			updateEpicStatus(targetEpicId);
			return id;
		}

		Subtask prev = subtasks.get(id);
		if (prev != null) {
			removeFromPrioritized(prev);
			int prevEpicId = prev.getEpicId();

			if (prevEpicId != targetEpicId) {
				Epic prevEpic = epics.get(prevEpicId);
				if (prevEpic != null) {
					prevEpic.removeSubtaskId(id);
					updateEpicStatus(prevEpicId);
				}
				targetEpic.addSubtaskId(id);
			}
		}

		Subtask copy = copySubtask(subtask);
		subtasks.put(id, copy);
		validateNoIntersections(copy);
		addToPrioritized(copy);
		updateEpicStatus(targetEpicId);

		return id;
	}

	@Override
	public void updateTask(Task task) {
		if (task == null) return;

		int id = task.getId();
		Task old = tasks.get(id);
		if (old == null) return;

		Task copy = copyTask(task);

		validateNoIntersections(copy);

		removeFromPrioritized(old);
		tasks.put(id, copy);
		addToPrioritized(copy);
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

		int id = subtask.getId();
		Subtask old = subtasks.get(id);
		if (old == null) return;

		Epic epic = epics.get(subtask.getEpicId());
		if (epic == null) return;

		Subtask copy = copySubtask(subtask);

		validateNoIntersections(copy);

		removeFromPrioritized(old);
		subtasks.put(id, copy);
		addToPrioritized(copy);

		updateEpicStatus(subtask.getEpicId());
	}


	@Override
	public void deleteTask(int id) {
		Task removed = tasks.remove(id);
		removeFromPrioritized(removed);
		historyManager.remove(id);
	}

	@Override
	public void deleteEpic(int id) {
		final Epic epic = epics.remove(id);
		if (epic == null) return;
		for (Integer subtaskId : epic.getSubtaskIds()) {
			Subtask removedSub = subtasks.remove(subtaskId);
			removeFromPrioritized(removedSub);
			historyManager.remove(subtaskId);
		}
		historyManager.remove(id);
	}

	@Override
	public void deleteSubtask(int id) {
		Subtask removed = subtasks.remove(id);
		if (removed == null) return;

		removeFromPrioritized(removed);

		Epic epic = epics.get(removed.getEpicId());
		if (epic != null) {
			epic.removeSubtaskId(id);
			updateEpicStatus(epic.getId());
		}

		historyManager.remove(id);
	}

	@Override
	public void deleteTasks() {
		for (Task t : tasks.values()) {
			removeFromPrioritized(t);
			historyManager.remove(t.getId());
		}
		tasks.clear();
	}

	@Override
	public void deleteSubtasks() {
		for (Epic epic : epics.values()) {
			epic.clearSubtaskIds();
			updateEpicStatus(epic.getId());
		}
		for (Subtask st : subtasks.values()) {
			removeFromPrioritized(st);
			historyManager.remove(st.getId());
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
		for (Subtask st : subtasks.values()) {
			removeFromPrioritized(st);
			historyManager.remove(st.getId());
		}
		epics.clear();
		subtasks.clear();
	}

	@Override
	public List<Task> getHistory() {
		return historyManager.getHistory().stream()
				.map(task -> switch (task.getType()) {
					case SUBTASK -> copySubtask((Subtask) task);
					case EPIC -> copyEpic((Epic) task);
					case TASK -> copyTask(task);
				})
				.collect(java.util.stream.Collectors.toList());
	}

	private void updateEpicStatus(int epicId) {
		Epic epic = epics.get(epicId);
		if (epic == null) return;

		List<Integer> subtaskIds = epic.getSubtaskIds();
		if (subtaskIds.isEmpty()) {
			epic.setStatus(TaskStatus.NEW);
			updateEpicTime(epicId);
			return;
		}

		List<Subtask> epicSubtasks = subtaskIds.stream()
				.map(subtasks::get)
				.filter(java.util.Objects::nonNull)
				.toList();

		if (epicSubtasks.isEmpty()) {
			epic.setStatus(TaskStatus.NEW);
			updateEpicTime(epicId);
			return;
		}

		boolean allNew = epicSubtasks.stream()
				.allMatch(st -> st.getStatus() == TaskStatus.NEW);

		boolean allDone = epicSubtasks.stream()
				.allMatch(st -> st.getStatus() == TaskStatus.DONE);

		if (allNew) {
			epic.setStatus(TaskStatus.NEW);
		} else if (allDone) {
			epic.setStatus(TaskStatus.DONE);
		} else {
			epic.setStatus(TaskStatus.IN_PROGRESS);
		}

		updateEpicTime(epicId);
	}

	private void updateEpicTime(int epicId) {
		Epic epic = epics.get(epicId);
		if (epic == null) return;

		List<Integer> subtaskIds = epic.getSubtaskIds();
		if (subtaskIds.isEmpty()) {
			epic.setCalculatedFields(Duration.ZERO, null, null);
			return;
		}

		var epicSubtasks = subtaskIds.stream()
				.map(subtasks::get)
				.filter(java.util.Objects::nonNull)
				.toList();

		Duration totalDuration = epicSubtasks.stream()
				.map(Subtask::getDuration)
				.filter(java.util.Objects::nonNull)
				.reduce(Duration.ZERO, Duration::plus);

		LocalDateTime earliestStart = epicSubtasks.stream()
				.map(Subtask::getStartTime)
				.filter(java.util.Objects::nonNull)
				.min(LocalDateTime::compareTo)
				.orElse(null);

		LocalDateTime latestEnd = epicSubtasks.stream()
				.map(Subtask::getEndTime)
				.filter(java.util.Objects::nonNull)
				.max(LocalDateTime::compareTo)
				.orElse(null);

		epic.setCalculatedFields(totalDuration, earliestStart, latestEnd);
	}


	@Override
	public List<Task> getPrioritizedTasks() {
		return new ArrayList<>(prioritizedTasks);
	}

	private void validateNoIntersections(Task candidate) {
		if (!isIntervalFree(candidate)) {
			throw new ManagerValidateException("Задача пересекается по времени с уже существующей: id=" + candidate.getId());
		}
	}
}


