package ru.yandex.javacourse.schedule.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import ru.yandex.javacourse.schedule.tasks.Task;

/**
 * In memory history manager.
 *
 * @author Andrey Terzi (terzi.andrey.sergeevich@gmail.com)
 */
public class InMemoryHistoryManager implements HistoryManager {

	private static class Node {
		Task task;
		Node prev;
		Node next;

		Node(Task task) {
			this.task = task;
		}
	}

	private final Map<Integer, Node> index = new HashMap<>();

	private Node head;
	private Node tail;

	@Override
	public void add(Task task) {
		if (task == null) {
			return;
		}
		int id = task.getId();

		Node existed = index.remove(id);
		if (existed != null) {
			removeNode(existed);
		}

		Node node = new Node(task);
		linkLast(node);
		index.put(id, node);
	}

	@Override
	public void remove(int id) {
		Node node = index.remove(id);
		if (node != null) {
			removeNode(node);
		}
	}

	@Override
	public List<Task> getHistory() {
		List<Task> result = new ArrayList<>();
		for (Node current = head; current != null; current = current.next) {
			result.add(current.task);
		}
		return result;
	}

	private void linkLast(Node node) {
		if (tail == null) {
			head = tail = node;
			return;
		}
		tail.next = node;
		node.prev = tail;
		tail = node;
	}

	private void removeNode(Node node) {
		if (node == null) {
			return;
		}

		Node previousNode = node.prev;
		Node nextNode = node.next;

		if (previousNode != null) {
			previousNode.next = nextNode;
		} else {
			head = nextNode;
		}

		if (nextNode != null) {
			nextNode.prev = previousNode;
		} else {
			tail = previousNode;
		}

		node.prev = null;
		node.next = null;
	}

	private List<Task> getTasks() {
		List<Task> tasksInOrder = new ArrayList<>();

		for (Node currentNode = head; currentNode != null; currentNode = currentNode.next) {
			tasksInOrder.add(currentNode.task);
		}

		return tasksInOrder;
	}
}
