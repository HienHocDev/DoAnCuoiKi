package com.example.doancuoiki.repository;

import com.example.doancuoiki.model.Project;
import com.example.doancuoiki.utils.DateUtils;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ProjectRepository {
    public interface ProjectListCallback {
        void onSuccess(List<Project> projects);

        void onError(Exception exception);
    }

    public interface ProjectCallback {
        void onSuccess(Project project);

        void onError(Exception exception);
    }

    public interface SimpleCallback {
        void onSuccess();

        void onError(Exception exception);
    }

    private static final String COLLECTION_PROJECTS = "projects";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void addProject(Project project, SimpleCallback callback) {
        DocumentReference document = db.collection(COLLECTION_PROJECTS).document();
        project.setId(document.getId());
        document.set(project)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    public void getProjectsByUser(String userId, ProjectListCallback callback) {
        db.collection(COLLECTION_PROJECTS)
                .whereArrayContains("members", userId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Project> projects = new ArrayList<>();
                    for (DocumentSnapshot document : snapshot.getDocuments()) {
                        Project project = document.toObject(Project.class);
                        if (project != null) {
                            project.setId(document.getId());
                            projects.add(project);
                        }
                    }
                    callback.onSuccess(projects);
                })
                .addOnFailureListener(callback::onError);
    }

    public void getProjectById(String projectId, ProjectCallback callback) {
        db.collection(COLLECTION_PROJECTS)
                .document(projectId)
                .get()
                .addOnSuccessListener(document -> {
                    Project project = document.toObject(Project.class);
                    if (project == null) {
                        callback.onError(new IllegalStateException("Không tìm thấy dự án"));
                        return;
                    }
                    project.setId(document.getId());
                    callback.onSuccess(project);
                })
                .addOnFailureListener(callback::onError);
    }

    public void updateProject(Project project, SimpleCallback callback) {
        db.collection(COLLECTION_PROJECTS)
                .document(project.getId())
                .set(project)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    public void updateProgress(String projectId, int progress, SimpleCallback callback) {
        db.collection(COLLECTION_PROJECTS)
                .document(projectId)
                .update("progress", progress)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    public void getProjectsOwnedBy(String userId, ProjectListCallback callback) {
        db.collection(COLLECTION_PROJECTS)
                .whereEqualTo("ownerId", userId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Project> projects = new ArrayList<>();
                    for (DocumentSnapshot document : snapshot.getDocuments()) {
                        Project project = document.toObject(Project.class);
                        if (project != null) {
                            project.setId(document.getId());
                            projects.add(project);
                        }
                    }
                    callback.onSuccess(projects);
                })
                .addOnFailureListener(callback::onError);
    }

    public void addMember(String projectId, String userId, SimpleCallback callback) {
        db.collection(COLLECTION_PROJECTS)
                .document(projectId)
                .update("members", FieldValue.arrayUnion(userId))
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    public void removeMember(String projectId, String userId, SimpleCallback callback) {
        db.collection(COLLECTION_PROJECTS)
                .document(projectId)
                .update("members", FieldValue.arrayRemove(userId))
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    public void deleteProject(String projectId, SimpleCallback callback) {
        db.collection("tasks").whereEqualTo("projectId", projectId).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    com.google.firebase.firestore.WriteBatch batch = db.batch();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        batch.delete(doc.getReference());
                    }
                    batch.delete(db.collection(COLLECTION_PROJECTS).document(projectId));
                    
                    batch.commit()
                            .addOnSuccessListener(unused -> callback.onSuccess())
                            .addOnFailureListener(callback::onError);
                })
                .addOnFailureListener(e -> {
                    db.collection(COLLECTION_PROJECTS).document(projectId).delete()
                            .addOnSuccessListener(unused -> callback.onSuccess())
                            .addOnFailureListener(callback::onError);
                });
    }

    public interface ActivityListCallback {
        void onSuccess(List<com.example.doancuoiki.model.ActivityLog> logs);
        void onError(Exception exception);
    }

    public void getRecentActivities(String userId, ActivityListCallback callback) {
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("activities")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING) // Sắp xếp mới nhất lên đầu
                .limit(30) // Lấy tối đa 30 hoạt động gần nhất
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<com.example.doancuoiki.model.ActivityLog> list = new ArrayList<>();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        com.example.doancuoiki.model.ActivityLog log = doc.toObject(com.example.doancuoiki.model.ActivityLog.class);
                        if (log != null) {
                            list.add(log);
                        }
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(callback::onError);
    }

    public void addActivityLog(com.example.doancuoiki.model.ActivityLog log, com.google.android.gms.tasks.OnSuccessListener<com.google.firebase.firestore.DocumentReference> listener) {
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("activities")
                .add(log);
    }
}
