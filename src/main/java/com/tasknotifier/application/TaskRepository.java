package com.tasknotifier.application;

import com.tasknotifier.domain.Task;

import java.util.List;
import java.util.Optional;

public interface TaskRepository {
    Task save(Task task);
    List<Task> findAll();
    Optional<Task> findById(long id);
    void deleteById(long id);
}
