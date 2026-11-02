# Chuẩn thiết kế giao diện Admin

Chuẩn dùng chung cho A1/A2 và các màn hình Admin tiếp theo. Mã định nghĩa giao diện nằm tại [`src/admin/admin.css`](../../src/admin/admin.css); khi đổi màu hoặc trạng thái, cập nhật CSS và tài liệu này cùng nhau. Yêu cầu nghiệp vụ và lịch công việc tiếp tục theo [`docs/plan.md`](../plan.md), không lấy tài liệu thiết kế này làm bằng chứng hoàn thành các task khác.

## 1. Phạm vi và nền tảng

- Java 21 LTS, JavaFX **21.0.5** theo POM hiện hành; dùng CSS JavaFX, package `admin`, nguồn `src/admin/` và tài nguyên classpath `/admin/admin.css`.
- Giữ khung `AdminApplication` → `AdminMainView` → `AdminNavigation` và các trang được giữ trong bộ nhớ khi chuyển mục.
- Thiết kế gọn, dễ đọc bảng dữ liệu, giữ nhận diện xanh dương và sidebar xanh đậm của A1. Không thêm thư viện, font tải qua mạng hoặc hệ thống giao diện song song.
- A2 dùng `AdminUserStore` trong bộ nhớ và 10 hồ sơ mẫu hiện có. Việc làm đẹp không thay đổi dữ liệu, điều kiện lọc, xác nhận, quyền thao tác hoặc cơ chế lưu trữ.
- Các trang ngoài quản lý người dùng hiện là bản xem trước; không trình bày chúng như chức năng đã triển khai.

## 2. Cách áp dụng ui-ux-pro-max

Đã dùng danh sách rà soát của skill và tra cứu các chủ đề `keyboard focus contrast`, `enterprise tableview density permission`. Giữ các khuyến nghị phù hợp: focus rõ ràng, bảng có lọc/sắp xếp, nhãn đầy đủ, dữ liệu dài có cách xem toàn bộ, trạng thái có chữ và kiểm tra đồng thời hai theme.

Tra cứu thiết kế tổng thể lần đầu trả về trang quảng bá có hiệu ứng kính; lần thu hẹp trả về phong cách tối giản phù hợp nhưng bố cục quảng bá vẫn không phù hợp ứng dụng Admin desktop. Không dùng các bố cục quảng bá, màu/font mới hoặc mã web từ kết quả đó; lấy giao diện hiện có làm nền và chỉ tiếp nhận nguyên tắc tối giản.

Hướng dẫn stack trong skill có mục áp dụng cho JavaFX 26. Mọi API, CSS và thành phần phải đối chiếu với JavaFX 21 và chạy kiểm tra trên phiên bản của POM. Không nâng JavaFX, thêm AtlantaFX/TestFX hoặc dùng cú pháp CSS của trình duyệt chỉ vì skill gợi ý.

Tài liệu đối chiếu: [JavaFX 21 CSS Reference](https://openjfx.io/javadoc/21/javafx.graphics/javafx/scene/doc-files/cssref.html), [JavaFX 21 Node: accessibleText và accessibleHelp](https://openjfx.io/javadoc/21/javafx.graphics/javafx/scene/Node.html). Các selector lịch và thanh cuộn được đối chiếu thêm với stylesheet Modena trong JAR JavaFX 21.0.5 đang dùng; cần kiểm tra lại khi nhóm đổi phiên bản hoặc skin.

## 3. Màu sắc và theme

JavaFX dùng màu tra cứu theo tên (`-admin-*`), không dùng `var(--token)`. Khai báo gốc ở `.admin-root, .admin-dialog`; theme tối ghi đè tại `.admin-root.theme-dark, .admin-dialog.theme-dark`.

| Tên màu | Theme sáng | Theme tối | Mục đích |
|---|---|---|---|
| `-admin-bg` | `#f3f5f9` | `#161d2b` | Nền không gian làm việc |
| `-admin-surface` | `#ffffff` | `#202b3d` | Header, dialog, ô nhập, bảng |
| `-admin-soft` | `#f7f9fc` | `#28354a` | Nền phụ, tiêu đề bảng, ghi chú |
| `-admin-text` | `#19263d` | `#edf2fa` | Chữ chính |
| `-admin-muted` | `#637087` | `#b3bfd1` | Chữ phụ và nhãn |
| `-admin-border` | `#dce3ed` | `#3a4960` | Đường phân cách trang/bảng |
| `-admin-control-border` | `#7b8798` | `#7d8ba3` | Viền control và nút cuộn |
| `-admin-accent` | `#2558c9` | `#9dbdff` | Focus và nội dung nhấn mạnh |
| `-admin-primary` | `#2558c9` | `#9dbdff` | Nút hành động chính |
| `-admin-primary-hover` | `#1e48a6` | `#bfd3ff` | Nút chính khi trỏ chuột |
| `-admin-on-primary` | `#ffffff` | `#142444` | Chữ trên nút chính |
| `-admin-selection` | `#2558c9` | `#b8ceff` | Dòng/mục đang chọn |
| `-admin-on-selection` | `#ffffff` | `#142444` | Chữ trên vùng chọn |
| `-admin-danger` | `#b02a20` | `#ffb4ab` | Lỗi và chữ hành động xóa |
| `-admin-success-bg` / `-admin-success-text` | `#def3e8` / `#175b3c` | `#203e35` / `#a2e5c1` | Hoạt động |
| `-admin-warning-bg` / `-admin-warning-text` | `#fff0cc` / `#805211` | `#493a22` / `#ffdb92` | Đã khóa |
| `-admin-danger-bg` / `-admin-danger-text` | `#fce5e1` / `#984136` | `#482c32` / `#ffb4ab` | Đã xóa |

Sidebar giữ cùng bảng màu ở cả hai theme: `-admin-sidebar=#12213b`, `-admin-nav-hover=#213452`, `-admin-nav-selected=#2e5cc4`, `-admin-nav-text=#d5deec`, `-admin-nav-muted=#adbbd2`, `-admin-nav-focus=#b8d0ff`. Mục được chọn có nền xanh và chữ đậm; focus bàn phím có viền riêng.

- Mục tiêu chữ thông thường: tương phản ít nhất 4,5:1; viền control/focus so với nền bên cạnh: ít nhất 3:1. Đây là tiêu chí thiết kế, không phải chứng nhận khả năng tiếp cận toàn ứng dụng.
- Không dùng màu làm tín hiệu duy nhất. Nhãn trạng thái luôn ghi “Hoạt động”, “Đã khóa”, “Đã xóa”; lỗi phải có thông báo bằng chữ.
- Dialog được mở qua `AdminDialogs.prepare` để nhận owner, stylesheet và theme hiện tại. Hộp xác nhận đăng xuất trong `AdminMainView` cũng nhận cùng cấu hình giao diện.
- Theme chỉ tồn tại trong phiên chạy. Không tự bổ sung lưu cấu hình hoặc thay đổi thiết lập Windows.

## 4. Bố cục, chữ và khoảng cách

| Thành phần | Quy ước |
|---|---|
| Cửa sổ | Mốc chuẩn khoảng 1000×700; kiểm tra thêm khi phóng to |
| Khung chính | `BorderPane`, sidebar `VBox` rộng 230, vùng trang `StackPane` |
| Header | Danh tính mẫu, đường dẫn ngắn, nút đăng xuất; padding 18×24 |
| Tiêu đề trang | Tên chức năng + mô tả ngắn; nút đổi theme bên phải; padding 24 |
| Nội dung trang | Padding hai bên và dưới 24; phần chính ưu tiên diện tích cho bảng |
| Khoảng cách | Thang ưu tiên 4/8/12/16/24; nhóm liên quan gần nhau |
| Font | `Segoe UI` hệ thống; thân bài 13, nhãn/chữ phụ 12, chú thích nhỏ 11 |
| Phân cấp | Tiêu đề trang 25 đậm, tiêu đề khối 21, header dialog 17 đậm |
| Control | Nút cao tối thiểu 36, ô nhập tối thiểu 32; viền focus 2, bo góc 5–8 |
| Bảng | Dòng và tiêu đề cao 38; padding ô 8; nền xen kẽ nhẹ; cột tự giãn theo vùng bảng |
| Dialog hồ sơ | Rộng khoảng 520; `GridPane` hai cột, nhãn 130, khoảng cột 16, hàng 12 |

Không ép toàn bộ hướng dẫn font 16 hoặc kích thước chạm của web/mobile vào desktop này. Khi tăng cỡ chữ hoặc thu hẹp cửa sổ, phải kiểm tra lại nội dung và thao tác; chưa khẳng định hỗ trợ mọi mức DPI/cỡ chữ Windows.

## 5. Quy ước thành phần

| Thành phần | Class/CSS tái sử dụng | Hành vi và yêu cầu |
|---|---|---|
| Điều hướng | `AdminNavigation`, `.nav-button`, `.nav-number` | Một mục đang chọn; số 01–09 và nhãn rõ ràng; giữ focus riêng với selected |
| Trang và bản xem trước | `.page-heading`, `.page-title`, `.page-description`, `.preview-card` | Dùng chung phần tiêu đề; bản chưa triển khai phải có nhãn “BẢN XEM TRƯỚC” |
| Nút | `.secondary-button`, thêm `.primary-button` hoặc `.danger-button` | Một hành động chính nổi bật; giữ trạng thái hover/pressed/focused/disabled, không thay đổi kích thước khi focus |
| Bộ lọc | `TextField`, `ComboBox`, `.filter-label` | Nhãn nhìn thấy và `Label.setLabelFor`; placeholder chỉ là gợi ý; có “Xóa bộ lọc” |
| Bảng | `TableView`, `TableColumn`, `TableCell` | Giữ dữ liệu thời gian/số đúng kiểu khi sort; tên/email bị cắt có tooltip; nút Chi tiết và Enter cho phép xem đầy đủ bằng bàn phím |
| Trạng thái | `.status-badge` + `.status-active/.status-locked/.status-deleted` | Luôn có chữ; dùng cặp màu riêng từng theme |
| Không có kết quả | `.empty-state`, `.empty-state-title` | Giải thích ngắn và nút xóa bộ lọc ngay trong bảng; không tự tạo dữ liệu thay thế |
| Hồ sơ | `UserEditDialog`, `.admin-dialog` | Owner rõ ràng, nhãn bắt buộc có `*`, ghi rõ ý nghĩa dấu `*`, có Hủy/Đóng |
| Lỗi nhập | `.validation-message` | Hiện ngay trong dialog, xuống dòng; khi lưu không hợp lệ chuyển focus đến thông báo lỗi; giữ dữ liệu vừa nhập và cửa sổ mở |
| Thao tác bị cấm | Disabled + dòng gợi ý dưới bảng | Giữ nguyên bảo vệ Admin cuối cùng và tài khoản đã xóa; giải thích bằng chữ, không chỉ dựa tooltip của nút disabled |
| Phản hồi mẫu | `.demo-note` | Thông báo kết quả trong trang, phân biệt dữ liệu trong bộ nhớ với thay đổi trên hệ thống |
| Biểu đồ tương lai | JavaFX `BarChart` | Dùng bảng màu chung, trục/đơn vị rõ ràng, có cách đọc số liệu bằng chữ; chưa triển khai trong lượt chuẩn hóa A1/A2 |

Không thay toàn bộ bảng bằng các thẻ. Thanh thao tác dùng `FlowPane` để tự xuống hàng khi cần; `TableView` nhận phần chiều cao còn lại và cuộn nội dung. Dữ liệu dài không được mất khả năng xem toàn bộ vì bị cắt ở ô bảng.

A2 dùng `TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS` có sẵn trong JAR JavaFX 21.0.5; giữ chiều rộng tối thiểu theo nội dung và giới hạn ID/trạng thái/ngày tạo lần lượt 56/112/160 để phần rộng thêm ưu tiên username, họ tên, email. Khi thêm cột, phải tính lại tổng chiều rộng tối thiểu ở cửa sổ chuẩn.

## 6. Bàn phím và phản hồi

- Giữ Tab/Shift+Tab của JavaFX; mọi nút, ô nhập, ComboBox và bảng phải có focus nhìn thấy.
- Bảng có tên và hướng dẫn qua `accessibleText`/`accessibleHelp`; phím mũi tên chọn dòng, Enter mở Chi tiết. Có nút Chi tiết tương ứng để không phụ thuộc nhấp đúp.
- Các ô bảng tùy biến phải xóa text, tooltip và accessible text khi cell được tái sử dụng cho dòng trống; không để lộ giá trị của dòng cũ.
- Lỗi hồ sơ/mật khẩu là nhãn có thể nhận focus, không chỉ đổi màu viền. Khi sửa tiếp, dùng Tab/Shift+Tab hoặc chọn trực tiếp trường cần sửa.
- Giữ nguyên Hủy là mặc định an toàn trong xác nhận khóa/xóa. Không biến Enter thành tự động xác nhận thao tác phá hủy.
- Không thêm hiệu ứng chuyển động cho tác vụ tức thời; không dùng CSS transition, backdrop-filter hoặc thuộc tính chỉ có trong trình duyệt.
- Chưa kiểm thử bằng trình đọc màn hình thực tế; accessible text và cây UI Automation là bằng chứng hỗ trợ, không phải chứng nhận Narrator.

## 7. Áp dụng cho màn hình Admin tiếp theo

1. Đọc chuẩn này và yêu cầu task trong kế hoạch; tái sử dụng khung chính và `admin.css`.
2. Dùng lại thang chữ, khoảng cách, màu và bộ lọc/bảng/dialog. Chỉ thêm selector theo phạm vi màn hình khi thật sự cần.
3. Đặt tên/id ổn định cho control; tách dữ liệu mẫu khỏi trình bày, không kết nối JDBC trực tiếp từ màn hình.
4. Kiểm tra sáng/tối ngay khi dựng màn hình, bao gồm popup, vùng chọn, trạng thái rỗng và lỗi.
5. Chỉ ghi PASS sau khi chạy đúng phiên bản JavaFX của nhóm. Nếu chưa chạy được, ghi rõ chưa kiểm tra.

## 8. Kiểm chứng tại máy hiện tại

Ngày rà soát: **02/11/2026**. Phạm vi là A1/A2, không phải kiểm thử toàn hệ thống.

- Maven compile: **BUILD SUCCESS**; JDK 21, JavaFX 21.0.5, dùng nguyên POM.
- `src/admin/check-users.ps1`: **32 PASS**, kiểm tra store mẫu, tính hợp lệ và bảo vệ thao tác trong bộ nhớ.
- Bộ kiểm tra JavaFX tạm tại `target/admin-checks/AdminUiCheck.java`: **20 PASS ở theme sáng và 20 PASS ở theme tối**, kiểm tra lọc, trạng thái nút, thêm/sửa, lỗi ngày sinh, xác nhận khóa/xóa, form mật khẩu và đóng hồ sơ chỉ xem không trả kết quả sai kiểu. Bộ này nằm trong `target/`, không phải bộ test được quản lý trong Git.
- Kiểm tra giá trị màu CSS: **14 cặp chữ/nền mỗi theme đạt ≥4,5:1**; viền control so với nền surface đạt **3,65:1 sáng / 4,13:1 tối**. Không bao gồm chữ disabled, mọi tổ hợp hover hoặc mọi pixel do hệ điều hành dựng.
- Đã chạy và kiểm tra trực quan A1/A2 ở cả theme sáng/tối: bảng, nhãn trạng thái, điều hướng, trang xem trước, form hồ sơ và focus. Kiểm tra thêm ở cửa sổ chuẩn và khi phóng to 1920×1032; cột tên/email giãn theo phần rộng thêm.
- Đã kiểm tra trạng thái rỗng và nút xóa bộ lọc, lỗi form trống, ComboBox trạng thái bằng bàn phím, lịch DatePicker và thanh cuộn ở theme tối. Mũi tên lịch/thanh cuộn tối được sửa sau khi phát hiện khó nhìn; không ghi rằng mọi popup/trạng thái đều đã được thử ở cả hai theme.
- Enter mở Chi tiết từ bảng; nút Đóng quay lại danh sách 10 hồ sơ bình thường. Kiểm tra thực tế phát hiện `ClassCastException` khi dialog chỉ xem trả `ButtonType` thay vì `UserRow`; đã đặt bộ chuyển kết quả mặc định trả `null`, kiểm tra hồi quy và chạy lại thành công.
- `git diff --check`: PASS; các liên kết tương đối trong tài liệu này trỏ tới tệp đang tồn tại. Hash của store, model/dữ liệu mẫu, POM và SQL khớp trước/sau lượt chuẩn hóa giao diện.

Chưa kiểm thử Server, JDBC, PostgreSQL, đóng gói JAR hoặc trình đọc màn hình. Không coi các kiểm tra bản mẫu này là bằng chứng tích hợp hệ thống.

Lệnh hiện có, chạy tại gốc repository với Maven/JDK đã có trên PATH:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File src/admin/check-users.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File src/admin/run-admin.ps1
git diff --check
```

Nếu Maven chưa có trên PATH của tiến trình hiện tại, truyền `-MavenCommand` tới script bằng vị trí Maven trên máy; không ghi đường dẫn máy cá nhân vào tài liệu hoặc mã nguồn. Không sửa POM chỉ để đổi entrypoint User sang Admin.
