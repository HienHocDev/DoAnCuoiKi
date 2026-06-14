# TaskFlow - Hướng dẫn Git và Build dự án

TaskFlow là ứng dụng Android quản lý dự án và công việc nhóm.

Dự án hiện được tổ chức theo hướng:

```text
XML: giao diện
Java: xử lý chức năng
Firebase: đăng nhập và lưu dữ liệu
```

README này chỉ tập trung vào cách lấy code, chuyển nhánh, làm việc nhóm với Git và build/chạy dự án.

---

## 1. Clone dự án từ GitHub

Mở Terminal/Git Bash tại thư mục muốn lưu dự án, chạy:

```bash
git clone <link-repository-github>
```

Ví dụ:

```bash
git clone https://github.com/ten-cua-ban/doancuoiki.git
```

Sau đó vào thư mục dự án:

```bash
cd doancuoiki
```

---

## 2. Lấy code mới nhất

Trước khi bắt đầu làm, luôn chạy:

```bash
git pull
```

Nếu muốn lấy toàn bộ thông tin nhánh mới từ GitHub:

```bash
git fetch
```

---

## 3. Làm việc theo nhánh

Không nên code trực tiếp trên `main`.

```text
main: nhánh chính, chứa code ổn định
nhánh riêng: nơi từng bạn code phần của mình
```

Kiểm tra đang ở nhánh nào:

```bash
git branch
```

Nhánh hiện tại sẽ có dấu `*`.

Ví dụ:

```text
* main
  feature-auth-profile
  feature-projects
```

Nếu đang ở `main`, cần chuyển sang nhánh của mình trước khi code.

---

## 4. Xem danh sách nhánh

Xem nhánh local:

```bash
git branch
```

Xem cả nhánh local và nhánh trên GitHub:

```bash
git branch -a
```

---

## 5. Chuyển sang nhánh của mình

Nếu nhánh đã có trên máy:

```bash
git switch ten-nhanh
```

Ví dụ:

```bash
git switch feature-auth-profile
```

Nếu nhánh đã có trên GitHub nhưng máy chưa có:

```bash
git fetch
git switch ten-nhanh
```

Nếu vẫn chưa chuyển được, dùng:

```bash
git checkout -b ten-nhanh origin/ten-nhanh
```

Ví dụ:

```bash
git checkout -b feature-auth-profile origin/feature-auth-profile
```

---

## 6. Quy trình trước khi code

Mỗi bạn nên làm theo thứ tự:

```bash
git fetch
git switch ten-nhanh-cua-minh
git pull
git branch
```

Sau đó kiểm tra dấu `*` đã nằm đúng nhánh của mình chưa.

Ví dụ:

```bash
git fetch
git switch feature-auth-profile
git pull
git branch
```

Kết quả đúng:

```text
* feature-auth-profile
  main
```

---

## 7. Quy trình sau khi code xong

Kiểm tra file đã thay đổi:

```bash
git status
```

Thêm file vào commit:

```bash
git add .
```

Commit:

```bash
git commit -m "Mo ta ngan gon phan da lam"
```

Push lên GitHub:

```bash
git push
```

Nếu lần đầu push nhánh đó và Git báo chưa có upstream:

```bash
git push -u origin ten-nhanh
```

Ví dụ:

```bash
git push -u origin feature-auth-profile
```

---

## 8. Gợi ý chia nhánh

Nhóm có thể chia nhánh như sau:

```text
main
feature-auth-profile
feature-projects
feature-tasks-reports
```

Gợi ý phân công:

```text
feature-auth-profile:
  LoginActivity, AccountActivity, Firebase Auth, users.

feature-projects:
  HomeActivity, ProjectsActivity, AddProjectActivity, ProjectDetailActivity, projects.

feature-tasks-reports:
  TasksActivity, CalendarActivity, ReportActivity, NotificationsActivity, tasks, notifications, charts.
```

Nếu nhóm đã tạo tên nhánh khác thì dùng đúng tên nhánh đã tạo.

---

## 9. Lấy code mới từ main vào nhánh cá nhân

Khi đang ở nhánh của mình, nếu muốn cập nhật code mới nhất từ `main`:

```bash
git pull origin main
```

Ví dụ:

```bash
git switch feature-projects
git pull origin main
```

Nếu có conflict:

```text
1. Mở file bị conflict.
2. Sửa phần bị trùng.
3. Kiểm tra app còn build được không.
4. Commit lại phần đã sửa.
```

Sau khi sửa conflict:

```bash
git add .
git commit -m "Resolve conflict"
git push
```

---

## 10. Các file dễ bị conflict

Các file nhiều người dễ sửa trùng:

```text
app/src/main/AndroidManifest.xml
app/build.gradle.kts
app/src/main/java/com/example/doancuoiki/NavigationUtils.java
app/src/main/java/com/example/doancuoiki/ViewFactory.java
app/src/main/res/values/themes.xml
```

Nếu cần sửa các file này, nên báo trước cho nhóm.

---

## 11. Mở và sync dự án

Sau khi pull code:

```text
1. Mở Android Studio.
2. Open thư mục doancuoiki.
3. Đợi Gradle Sync.
4. Nếu có nút Sync Project with Gradle Files thì bấm Sync.
```

Nếu Android Studio báo thiếu file `local.properties`, chỉ cần mở project bằng Android Studio, file này thường sẽ được tạo lại theo SDK máy của bạn.

Không nên commit file:

```text
local.properties
```

---

## 12. Firebase trong dự án

Firebase đã được cấu hình qua file:

```text
app/google-services.json
```

Nếu file này có trong project, app sẽ kết nối tới Firebase project của nhóm.

Firebase dùng cho:

```text
Firebase Authentication: đăng nhập, đăng ký, quên mật khẩu
Cloud Firestore: users, projects, tasks, notifications
```

Nếu pull code về mà thiếu file `google-services.json`, xin lại file từ trưởng nhóm và đặt vào:

```text
app/google-services.json
```

---

## 13. Build dự án

Build bằng terminal ở thư mục gốc dự án:

```powershell
.\gradlew.bat assembleDebug
```

Nếu dùng Git Bash:

```bash
./gradlew assembleDebug
```

Kết quả đúng:

```text
BUILD SUCCESSFUL
```

---

## 14. Chạy dự án

Trong Android Studio:

```text
1. Chọn thiết bị ảo hoặc thiết bị thật.
2. Chọn cấu hình app.
3. Bấm Run.
```

Nếu không thấy thiết bị, mở:

```text
Device Manager
```

và tạo hoặc chạy một máy ảo Android.

---

## 15. Lỗi thường gặp

## 15.1. Lỗi current branch has no upstream

Lỗi:

```text
fatal: The current branch main has no upstream branch
```

Cách sửa:

```bash
git push -u origin main
```

Nếu đang ở nhánh khác:

```bash
git push -u origin ten-nhanh
```

## 15.2. Lỗi thiếu google-services.json

Kiểm tra file:

```text
app/google-services.json
```

Nếu thiếu, app sẽ không xử lý Firebase đúng.

## 15.3. Lỗi package Firebase không khớp

Package hiện tại:

```text
com.example.doancuoiki
```

Package trong Firebase cũng phải là:

```text
com.example.doancuoiki
```

## 15.4. Lỗi jlink.exe does not exist

Nếu gặp lỗi:

```text
jlink.exe does not exist
```

Vào Android Studio:

```text
Settings > Build Tools > Gradle > Gradle JDK
```

Chọn JDK đầy đủ, không chọn JRE của VS Code.

## 15.5. Lỗi Gradle sync

Thử:

```text
1. Kiểm tra internet.
2. Bấm Sync Project with Gradle Files.
3. File > Invalidate Caches / Restart.
4. Kiểm tra Gradle JDK.
```

---

## 16. Cấu trúc file chính

Java:

```text
app/src/main/java/com/example/doancuoiki/
```

XML:

```text
app/src/main/res/layout/
```

Firebase:

```text
app/google-services.json
```

Manifest:

```text
app/src/main/AndroidManifest.xml
```

---

## 17. Trạng thái hiện tại

Đã có:

```text
Khung giao diện XML
Các Activity Java
Điều hướng giữa màn hình
Firebase cấu hình trong Gradle
Firebase Auth và Firestore dependency
Dữ liệu mẫu để demo
```

Chưa làm:

```text
Đăng nhập thật bằng Firebase
Đăng ký thật
Đọc/ghi projects từ Firestore
Đọc/ghi tasks từ Firestore
Thông báo thật
Báo cáo/charts thật
```
