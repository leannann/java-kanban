package ru.yandex.javacourse.schedule.tasks;

import static ru.yandex.javacourse.schedule.tasks.TaskStatus.NEW;

import java.util.ArrayList;
import java.util.List;
import java.time.Duration;
import java.time.LocalDateTime;

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
	public TaskType getType() {
		return TaskType.EPIC;
	}

	private LocalDateTime endTime;

	@Override
	public LocalDateTime getEndTime() {
		return endTime;
	}

	public void setCalculatedFields(Duration duration, LocalDateTime startTime, LocalDateTime endTime) {
		this.duration = duration;
		this.startTime = startTime;
		this.endTime = endTime;
	}

	@Override
	public String toString() {
		return "Epic{" +
				"id=" + id +
				", name='" + name + '\'' +
				", status=" + status +
				", description='" + description + '\'' +
				", duration=" + (duration != null ? duration.toMinutes() + "min" : "null") +
				", startTime=" + startTime +
				", endTime=" + getEndTime() +
				", subtaskIds=" + subtaskIds +
				'}';
	}
}

