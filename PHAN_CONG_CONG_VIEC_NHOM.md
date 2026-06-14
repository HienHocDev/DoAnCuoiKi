# Phân công công việc nhóm TaskFlow

## Tổng quan phân chia

Nhóm có 3 thành viên. Mỗi người phụ trách một mảng riêng để tránh sửa trùng file quá nhiều.

```text
Bạn 1: Đăng nhập, tài khoản, người dùng
Bạn 2: Dự án, chi tiết dự án, Firestore cho project/task
Bạn 3: Công việc, lịch, thông báo, báo cáo/charts
```

## Bạn 1: LoginScreen, Firebase Auth, User/Profile

## File phụ trách chính

```text
LoginScreen.java
AccountScreen.java
TaskFlowNavigator.java
MainActivity.java
```

## File cần tạo thêm

```text
model/User.java
repository/AuthRepository.java
repository/UserRepository.java
utils/Constants.java
```

## Firebase cần dùng

```text
Firebase Authentication
Cloud Firestore collection: users
```

## Database cần làm

Collection:

```text
users
```

Mỗi user có các trường:

```text
id
name
email
role
avatarUrl
createdAt
```

Ví dụ:

```text
users/u001
  id: u001
  name: Nguyễn Văn A
  email: vana@gmail.com
  role: Trưởng nhóm
  avatarUrl:
  createdAt:
```

## Nhiệm vụ chi tiết

### 1. Làm đăng nhập bằng email/password

Trong `LoginScreen.java`, hiện tại nút `Đăng nhập` đang chuyển thẳng vào trang chủ.

Cần sửa thành:

```text
1. Lấy email từ ô Email.
2. Lấy password từ ô Mật khẩu.
3. Kiểm tra không được để trống.
4. Gọi FirebaseAuth.signInWithEmailAndPassword.
5. Nếu thành công thì chuyển vào HomeScreen.
6. Nếu lỗi thì hiện thông báo lỗi.
```

Tiêu chí hoàn thành:

```text
Đăng nhập đúng tài khoản Firebase thì vào được app.
Đăng nhập sai thì hiện thông báo lỗi.
```

### 2. Làm đăng ký tài khoản

Hiện tại dòng `Đăng ký ngay` đang để "đang phát triển".

Cần làm:

```text
1. Tạo màn hình hoặc form đăng ký.
2. Người dùng nhập họ tên, email, mật khẩu.
3. Gọi FirebaseAuth.createUserWithEmailAndPassword.
4. Lấy uid của tài khoản vừa tạo.
5. Lưu thông tin user vào Firestore collection users.
```

Thông tin cần lưu:

```text
id = uid
name = họ tên người dùng nhập
email = email đăng ký
role = Thành viên
avatarUrl = rỗng
createdAt = thời gian hiện tại
```

Tiêu chí hoàn thành:

```text
Tạo được tài khoản mới trên Firebase Authentication.
Firestore có thêm document trong users.
Tài khoản mới đăng nhập được.
```

### 3. Làm quên mật khẩu

Hiện tại nút `Quên mật khẩu?` đang để "đang phát triển".

Cần làm:

```text
1. Người dùng nhập email.
2. Gọi FirebaseAuth.sendPasswordResetEmail.
3. Hiện thông báo đã gửi email đặt lại mật khẩu.
```

Tiêu chí hoàn thành:

```text
Firebase gửi được email reset password.
```

### 4. Làm đăng xuất

Trong `AccountScreen.java`, nút `Đăng xuất` hiện đang quay về màn hình login.

Cần sửa thành:

```text
1. Gọi FirebaseAuth.signOut.
2. Quay về LoginScreen.
```

Tiêu chí hoàn thành:

```text
Sau khi đăng xuất, mở lại app không tự vào trang chủ nếu chưa đăng nhập.
```

### 5. Hiển thị profile người dùng

Trong `AccountScreen.java`, hiện tại đang hiển thị cứng:

```text
Nguyễn Văn A
Trưởng nhóm
```

Cần sửa thành:

```text
1. Lấy uid user hiện tại từ FirebaseAuth.
2. Đọc document users/{uid}.
3. Hiển thị name, email, role.
```

Tiêu chí hoàn thành:

```text
Mỗi tài khoản đăng nhập sẽ thấy đúng tên/email của tài khoản đó.
```

### 6. Cập nhật thông tin cá nhân

Mục `Thông tin cá nhân` đang để "đang phát triển".

Cần làm:

```text
1. Mở form sửa tên.
2. Cho phép cập nhật name.
3. Lưu lại vào users/{uid}.
4. Load lại profile sau khi sửa.
```

Tiêu chí hoàn thành:

```text
Đổi tên trong app thì Firestore cũng đổi theo.
```

## Kết quả Bạn 1 cần bàn giao

```text
1. Đăng nhập thật bằng Firebase Auth.
2. Đăng ký tài khoản thật.
3. Quên mật khẩu.
4. Đăng xuất thật.
5. Profile đọc từ Firestore.
6. Có User.java, AuthRepository.java, UserRepository.java.
```

## Bạn 2: ProjectsScreen, ProjectExtraScreens, Firestore cho Projects/Tasks

## File phụ trách chính

```text
ProjectsScreen.java
ProjectExtraScreens.java
HomeScreen.java
SampleData.java
```

## File cần tạo thêm

```text
model/Project.java
model/Task.java
repository/ProjectRepository.java
repository/TaskRepository.java
```

## Firebase cần dùng

```text
Cloud Firestore collection: projects
Cloud Firestore collection: tasks
Cloud Firestore collection: users
```

## Database cần làm

Collection:

```text
projects
```

Mỗi project có các trường:

```text
id
name
description
startDate
endDate
progress
ownerId
members
createdAt
updatedAt
```

Ví dụ:

```text
projects/p001
  id: p001
  name: Website bán hàng
  description: Xây dựng website bán hàng
  startDate: 2024-05-15
  endDate: 2024-07-15
  progress: 70
  ownerId: u001
  members: [u001, u002, u003]
```

Collection:

```text
tasks
```

Mỗi task có các trường:

```text
id
projectId
title
description
assigneeId
status
priority
startDate
dueDate
createdAt
updatedAt
```

## Nhiệm vụ chi tiết

### 1. Tạo model Project.java

Cần tạo class `Project.java` trong package `model`.

Thuộc tính:

```text
String id
String name
String description
String startDate
String endDate
int progress
String ownerId
List<String> members
```

Yêu cầu:

```text
1. Có constructor rỗng để Firebase đọc dữ liệu.
2. Có constructor đầy đủ.
3. Có getter/setter.
```

Tiêu chí hoàn thành:

```text
Firestore có thể convert document thành Project object.
```

### 2. Tạo ProjectRepository.java

Repository này chuyên xử lý Firestore cho project.

Cần có các hàm:

```text
addProject(Project project)
getProjectsByUser(String userId)
getProjectById(String projectId)
updateProject(Project project)
deleteProject(String projectId)
```

Tiêu chí hoàn thành:

```text
Code màn hình không gọi Firebase trực tiếp quá nhiều.
Các thao tác project đi qua ProjectRepository.
```

### 3. Làm thêm dự án

Trong `ProjectExtraScreens.java`, phần `addProject` hiện chỉ là giao diện.

Cần làm:

```text
1. Lấy tên dự án từ input.
2. Lấy mô tả từ input.
3. Lấy ngày bắt đầu.
4. Lấy ngày kết thúc.
5. Lấy user hiện tại làm ownerId.
6. Tạo danh sách members, ban đầu có ownerId.
7. Lưu project lên Firestore.
8. Sau khi lưu thành công, quay về ProjectsScreen.
```

Tiêu chí hoàn thành:

```text
Bấm Tạo dự án thì Firestore có document mới trong projects.
Danh sách dự án load lại thấy dự án vừa tạo.
```

### 4. Làm danh sách dự án từ Firestore

Trong `ProjectsScreen.java`, hiện đang dùng `SampleData`.

Cần sửa thành:

```text
1. Lấy uid user hiện tại.
2. Query projects where members array-contains uid.
3. Hiển thị danh sách project.
4. Nếu chưa có dự án thì hiện thông báo danh sách trống.
```

Tiêu chí hoàn thành:

```text
Dự án hiển thị theo tài khoản đăng nhập.
Không còn phụ thuộc SampleData cho danh sách thật.
```

### 5. Làm tìm kiếm dự án

Ô tìm kiếm trong `ProjectsScreen.java` hiện chỉ là TextView giả.

Cần làm:

```text
1. Đổi thành EditText hoặc SearchView.
2. Khi nhập từ khóa, lọc danh sách dự án theo tên.
3. Có thể lọc local sau khi đã load danh sách từ Firestore.
```

Tiêu chí hoàn thành:

```text
Gõ tên dự án thì danh sách được lọc đúng.
```

### 6. Làm chi tiết dự án

Trong `ProjectExtraScreens.java`, phần `projectDetail` đang hiển thị cứng Website bán hàng.

Cần sửa thành:

```text
1. Khi bấm vào project, truyền projectId sang màn hình chi tiết.
2. Đọc project từ Firestore theo projectId.
3. Hiển thị tên, mô tả, thành viên, ngày bắt đầu, ngày kết thúc.
4. Load danh sách task thuộc projectId.
```

Tiêu chí hoàn thành:

```text
Bấm dự án nào thì chi tiết hiện đúng dự án đó.
```

### 7. Làm sửa/xóa dự án

Nút `...` trong chi tiết dự án hiện đang để "đang phát triển".

Cần làm:

```text
1. Hiện menu Sửa dự án, Xóa dự án.
2. Sửa dự án: cập nhật name, description, date.
3. Xóa dự án: xóa document project.
4. Có hộp thoại xác nhận trước khi xóa.
```

Tiêu chí hoàn thành:

```text
Sửa dự án thì Firestore cập nhật.
Xóa dự án thì dự án biến mất khỏi danh sách.
```

### 8. Tính tiến độ dự án

Progress hiện đang là số mẫu.

Cần làm:

```text
1. Lấy tất cả task theo projectId.
2. Đếm tổng số task.
3. Đếm số task có status = Hoàn thành.
4. progress = hoàn thành / tổng * 100.
5. Cập nhật field progress vào project.
```

Tiêu chí hoàn thành:

```text
Cập nhật trạng thái task thì tiến độ dự án thay đổi đúng.
```

### 9. Cập nhật HomeScreen

Trong `HomeScreen.java`, hiện tổng số dự án, công việc, hoàn thành đang là số cứng.

Cần làm:

```text
1. Load projects của user hiện tại.
2. Tính tổng project.
3. Load tasks thuộc các project đó.
4. Tính tổng task.
5. Tính task hoàn thành.
6. Hiển thị số liệu thật.
```

Tiêu chí hoàn thành:

```text
Trang chủ hiển thị số liệu thật từ Firebase.
```

## Kết quả Bạn 2 cần bàn giao

```text
1. Thêm/sửa/xóa dự án.
2. Danh sách dự án đọc từ Firestore.
3. Chi tiết dự án đọc đúng theo projectId.
4. Tính tiến độ dự án theo task.
5. HomeScreen có số liệu thật.
6. Có Project.java, Task.java, ProjectRepository.java, TaskRepository.java.
```

## Bạn 3: TasksScreen, CalendarScreen, Notifications, Reports/Charts

## File phụ trách chính

```text
TasksScreen.java
CalendarScreen.java
ProjectExtraScreens.java
```

## File cần tạo thêm

```text
model/Task.java
model/NotificationItem.java
repository/TaskRepository.java
repository/NotificationRepository.java
utils/DateUtils.java
```

## Firebase cần dùng

```text
Cloud Firestore collection: tasks
Cloud Firestore collection: projects
Cloud Firestore collection: notifications
```

## Thư viện có thể cần

```text
MPAndroidChart
Firebase Cloud Messaging
Android NotificationManager
Google Calendar API
```

## Database cần làm

Collection:

```text
tasks
```

Mỗi task có các trường:

```text
id
projectId
title
description
assigneeId
status
priority
startDate
dueDate
createdAt
updatedAt
```

Collection:

```text
notifications
```

Mỗi notification có các trường:

```text
id
userId
title
message
type
isRead
createdAt
```

## Nhiệm vụ chi tiết

### 1. Làm danh sách công việc từ Firestore

Trong `TasksScreen.java`, hiện đang dùng dữ liệu cứng.

Cần làm:

```text
1. Lấy uid user hiện tại.
2. Lấy danh sách project mà user tham gia.
3. Lấy tasks thuộc các project đó.
4. Hiển thị task lên màn hình.
```

Tiêu chí hoàn thành:

```text
Danh sách task hiển thị từ Firestore.
Task mới thêm vào Firestore thì app hiển thị được.
```

### 2. Làm lọc task

Các tab hiện có:

```text
Tất cả
Của tôi
Đã giao
Theo dự án
```

Cần làm:

```text
Tất cả:
  Hiển thị toàn bộ task trong các project user tham gia.

Của tôi:
  Hiển thị task có assigneeId là uid hiện tại.

Đã giao:
  Hiển thị task do user hiện tại tạo hoặc giao cho người khác.

Theo dự án:
  Cho chọn project rồi hiển thị task thuộc project đó.
```

Tiêu chí hoàn thành:

```text
Bấm từng tab thì danh sách task thay đổi đúng.
```

### 3. Làm thêm công việc

Nút `+` trong `TasksScreen.java` đang để "đang phát triển".

Cần làm:

```text
1. Tạo form thêm task.
2. Chọn project.
3. Nhập tiêu đề task.
4. Nhập mô tả.
5. Chọn người thực hiện.
6. Chọn ngày bắt đầu.
7. Chọn hạn hoàn thành.
8. Chọn độ ưu tiên.
9. Lưu task vào Firestore.
```

Tiêu chí hoàn thành:

```text
Tạo task mới thành công.
Task xuất hiện trong danh sách task.
Task xuất hiện trong chi tiết dự án tương ứng.
```

### 4. Làm chi tiết công việc

Hiện tại bấm vào task chỉ hiện "đang phát triển".

Cần làm:

```text
1. Khi bấm task, truyền taskId sang màn hình chi tiết.
2. Đọc task từ Firestore.
3. Hiển thị title, description, project, assignee, dueDate, priority, status.
4. Cho phép chỉnh sửa task.
```

Tiêu chí hoàn thành:

```text
Bấm task nào thì chi tiết hiện đúng task đó.
```

### 5. Cập nhật trạng thái công việc

Status cần thống nhất:

```text
Chưa bắt đầu
Đang làm
Hoàn thành
```

Cần làm:

```text
1. Trong chi tiết task, cho chọn status.
2. Update status lên Firestore.
3. Sau khi update status, gọi tính lại progress của project.
```

Tiêu chí hoàn thành:

```text
Đổi trạng thái task thành Hoàn thành thì tiến độ dự án tăng.
```

### 6. Làm lịch theo deadline

Trong `CalendarScreen.java`, hiện lịch và task trong ngày đang là dữ liệu cứng.

Cần làm:

```text
1. Hiển thị lịch tháng hiện tại.
2. Cho phép chọn ngày.
3. Khi chọn ngày, query tasks có dueDate bằng ngày đó.
4. Hiển thị danh sách task trong ngày.
```

Tiêu chí hoàn thành:

```text
Chọn ngày nào thì hiện task có deadline ngày đó.
```

### 7. Tích hợp Calendar API

Phần này có thể làm sau nếu còn thời gian.

Cần làm nâng cao:

```text
1. Xin quyền truy cập Calendar.
2. Khi tạo task có dueDate, cho phép thêm vào Google Calendar.
3. Tạo event trong calendar của máy hoặc Google Calendar.
```

Tiêu chí hoàn thành:

```text
Task deadline có thể được thêm vào lịch.
```

### 8. Làm thông báo trong app

Trong `ProjectExtraScreens.java`, phần `notifications` đang là dữ liệu cứng.

Cần làm:

```text
1. Tạo NotificationItem.java.
2. Tạo NotificationRepository.java.
3. Khi user được giao task, tạo document trong notifications.
4. Màn hình thông báo đọc danh sách notifications theo userId.
5. Cho phép đánh dấu đã đọc.
```

Tiêu chí hoàn thành:

```text
Khi giao task cho user, user đó thấy thông báo trong app.
```

### 9. Làm thông báo hệ thống Android

Cần làm:

```text
1. Tạo notification channel.
2. Dùng NotificationManager để hiện thông báo.
3. Thông báo khi task gần đến hạn.
```

Tiêu chí hoàn thành:

```text
Máy Android hiện notification khi có task sắp đến hạn.
```

### 10. Làm báo cáo tiến độ

Trong `ProjectExtraScreens.java`, phần `report` hiện là dữ liệu cứng.

Cần làm:

```text
1. Đọc danh sách projects.
2. Đọc danh sách tasks.
3. Tính task hoàn thành.
4. Tính task đang làm.
5. Tính task chưa bắt đầu.
6. Hiển thị phần trăm tiến độ thật.
```

Tiêu chí hoàn thành:

```text
Báo cáo tiến độ thay đổi theo dữ liệu task thật.
```

### 11. Làm biểu đồ charts

Thư viện đề xuất:

```text
MPAndroidChart
```

Cần làm:

```text
1. Thêm thư viện MPAndroidChart vào Gradle.
2. Biểu đồ tròn: tỉ lệ task theo trạng thái.
3. Biểu đồ cột: số task hoàn thành theo thành viên.
4. Biểu đồ thanh ngang: tiến độ từng project.
```

Tiêu chí hoàn thành:

```text
Báo cáo có biểu đồ thật, không chỉ là text/progress bar.
```

### 12. Làm hiệu suất thành viên

Trong `ProjectExtraScreens.java`, phần `performance` hiện là dữ liệu cứng.

Cần làm:

```text
1. Lấy danh sách thành viên trong project.
2. Với mỗi thành viên, đếm task có assigneeId là userId.
3. Đếm task đã hoàn thành.
4. Hiển thị số task hoàn thành của từng người.
```

Tiêu chí hoàn thành:

```text
Màn hình hiệu suất thành viên hiển thị đúng dữ liệu từ Firestore.
```

## Kết quả Bạn 3 cần bàn giao

```text
1. Danh sách task thật từ Firestore.
2. Thêm/sửa/cập nhật trạng thái task.
3. Lọc task theo tab.
4. Lịch hiển thị task theo deadline.
5. Thông báo trong app.
6. Báo cáo tiến độ thật.
7. Biểu đồ cơ bản.
8. Có NotificationItem.java, NotificationRepository.java, DateUtils.java.
```

## Quy tắc phối hợp giữa 3 bạn

## 1. Không sửa trùng file nếu chưa thống nhất

Các file dễ bị sửa trùng:

```text
ProjectExtraScreens.java
TaskFlowNavigator.java
MainActivity.java
UiKit.java
```

Nếu cần sửa các file này, nên báo trước cho nhóm.

## 2. Thống nhất tên status task

Chỉ dùng 3 status:

```text
Chưa bắt đầu
Đang làm
Hoàn thành
```

Không tự đặt thêm kiểu:

```text
Done
Processing
Todo
```

## 3. Thống nhất collection Firebase

Chỉ dùng các collection:

```text
users
projects
tasks
notifications
```

Không tạo tên khác như:

```text
Users
project
task_list
notification
```

## 4. Thống nhất id liên kết

Liên kết dữ liệu như sau:

```text
users.id = FirebaseAuth uid
projects.members chứa users.id
tasks.projectId = projects.id
tasks.assigneeId = users.id
notifications.userId = users.id
```

## 5. Thứ tự làm chung

```text
1. Bạn 1 làm đăng nhập trước.
2. Bạn 2 làm project sau khi có uid user.
3. Bạn 3 làm task sau khi có projectId.
4. Bạn 3 làm lịch/báo cáo sau khi task đã có dữ liệu thật.
5. Cả nhóm test lại luồng cuối.
```

## Luồng app cần chạy được khi hoàn thành

```text
1. Người dùng đăng ký tài khoản.
2. Người dùng đăng nhập.
3. Người dùng tạo dự án.
4. Người dùng thêm task vào dự án.
5. Người dùng giao task cho thành viên.
6. Thành viên xem task của mình.
7. Thành viên cập nhật trạng thái task.
8. App tính lại tiến độ dự án.
9. Lịch hiển thị deadline task.
10. Báo cáo hiển thị tiến độ và hiệu suất.
11. Thông báo hiển thị khi được giao task.
```

## Mức tối thiểu để nộp bài

Nếu thời gian gấp, cần hoàn thành ít nhất:

```text
Bạn 1:
  Đăng nhập, đăng ký, đăng xuất.

Bạn 2:
  Thêm dự án, danh sách dự án, chi tiết dự án.

Bạn 3:
  Thêm task, danh sách task, cập nhật trạng thái, báo cáo cơ bản.
```

## Mức nâng cao nếu còn thời gian

```text
1. Google Login.
2. Google Calendar API.
3. Firebase Cloud Messaging.
4. Upload avatar.
5. Upload file dự án.
6. Phân quyền trưởng nhóm/thành viên.
7. Biểu đồ đẹp bằng MPAndroidChart.
```
