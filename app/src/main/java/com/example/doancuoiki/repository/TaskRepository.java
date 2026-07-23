package com.example.doancuoiki.repository;

import com.example.doancuoiki.model.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TaskRepository {
    public interface TaskListCallback {
        void onSuccess(List<Task> tasks);

        void onError(Exception exception);
    }

    public interface SimpleCallback {
        void onSuccess();

        void onError(Exception exception);
    }

    public interface TaskCallback {
        void onSuccess(Task task);

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
        DocumentReference document = db.collection(COLLECTION_TASKS).document();
        task.setId(document.getId());
        document.set(task)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    public void getTaskById(String taskId, TaskCallback callback) {
        db.collection(COLLECTION_TASKS)
                .document(taskId)
                .get()
                .addOnSuccessListener(document -> {
                    Task task = document.toObject(Task.class);
                    if (task == null) {
                        callback.onError(new IllegalStateException("Không tìm thấy công việc"));
                        return;
                    }
                    task.setId(document.getId());
                    callback.onSuccess(task);
                })
                .addOnFailureListener(callback::onError);
    }

    public void getTasksForUser(String userId, TaskListCallback callback) {
        Tasks.whenAllSuccess(
                        db.collection(COLLECTION_TASKS).whereEqualTo("assigneeId", userId).get(),
                        db.collection(COLLECTION_TASKS).whereEqualTo("creatorId", userId).get()
                )
                .addOnSuccessListener(results -> {
                    Map<String, Task> taskMap = new LinkedHashMap<>();
                    for (Object result : results) {
                        com.google.firebase.firestore.QuerySnapshot snapshot =
                                (com.google.firebase.firestore.QuerySnapshot) result;
                        for (DocumentSnapshot document : snapshot.getDocuments()) {
                            Task task = document.toObject(Task.class);
                            if (task != null) {
                                task.setId(document.getId());
                                taskMap.put(document.getId(), task);
                            }
                        }
                    }
                    callback.onSuccess(new ArrayList<>(taskMap.values()));
                })
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

    public void getAssignedTasks(String userId, TaskListCallback callback) {
        db.collection(COLLECTION_TASKS)
                .whereEqualTo("assigneeId", userId)
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

    public void updateTask(Task task, SimpleCallback callback) {
        db.collection(COLLECTION_TASKS)
                .document(task.getId())
                .set(task)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    public void deleteTask(String taskId, SimpleCallback callback) {
        db.collection(COLLECTION_TASKS)
                .document(taskId)
                .delete()
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    public void addComment(String taskId, String comment, SimpleCallback callback) {
        db.collection(COLLECTION_TASKS)
                .document(taskId)
                .update("comments", com.google.firebase.firestore.FieldValue.arrayUnion(comment))
                .addOnSuccessListener(unused -> { if (callback != null) callback.onSuccess(); })
                .addOnFailureListener(e -> { if (callback != null) callback.onError(e); });
    }
}
