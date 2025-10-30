package ru.yandex.javacourse.schedule.tasks;

import static ru.yandex.javacourse.schedule.tasks.TaskStatus.NEW;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {

	private final List<Integer> subtaskIds = new ArrayList<>();

	public Epic(int id, String name, String description) {
		super(id, name, description, NEW);
	}

	public Epic(String name, String description) {
		super(name, description, NEW);
	}

	public void addSubtaskId(int id) {
		if (id == this.id) return;
		if (subtaskIds.contains(id)) return;
		subtaskIds.add(id);
	}

	public List<Integer> getSubtaskIds() {
		return new ArrayList<>(subtaskIds);
	}

	public void clearSubtaskIds() {
		subtaskIds.clear();
	}

	public void removeSubtaskId(int id) {
		subtaskIds.remove(Integer.valueOf(id));
	}

	@Override
	public String toString() {
		return "Epic{" +
				"id=" + id +
				", name='" + name + '\'' +
				", status=" + status +
				", description='" + description + '\'' +
				", subtaskIds=" + subtaskIds +
				'}';
	}
}

