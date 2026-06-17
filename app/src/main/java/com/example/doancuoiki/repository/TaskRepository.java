package com.example.doancuoiki.repository;

import com.example.doancuoiki.model.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class TaskRepository {
    public interface TaskListCallback {
        void onSuccess(List<Task> tasks);

        void onError(Exception exception);
    }

    public interface SimpleCallback {
        void onSuccess();

        void onError(Exception exception);
    }

    private static final String COLLECTION_TASKS = "tasks";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void getAllTasks(TaskListCallback callback) {
        db.collection(COLLECTION_TASKS)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Task> tasks = new ArrayList<>();
                    for (DocumentSnapshot document : snapshot.getDocuments()) {
                        Task task = document.toObject(Task.class);
                        if (task != null) {
                            task.setId(document.getId());
                            tasks.add(task);
                        }
                    }
                    callback.onSuccess(tasks);
                })
                .addOnFailureListener(callback::onError);
    }

    public void addTask(Task task, SimpleCallback callback) {
        db.collection(COLLECTION_TASKS)
                .add(task)
                .addOnSuccessListener(documentReference -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    public void getTasksByProject(String projectId, TaskListCallback callback) {
        db.collection(COLLECTION_TASKS)
                .whereEqualTo("projectId", projectId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Task> tasks = new ArrayList<>();
                    for (DocumentSnapshot document : snapshot.getDocuments()) {
                        Task task = document.toObject(Task.class);
                        if (task != null) {
                            task.setId(document.getId());
                            tasks.add(task);
                        }
                    }
                    callback.onSuccess(tasks);
                })
                .addOnFailureListener(callback::onError);
    }

    public void updateTaskStatus(String taskId, String status, SimpleCallback callback) {
        db.collection(COLLECTION_TASKS)
                .document(taskId)
                .update("status", status)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }
}
