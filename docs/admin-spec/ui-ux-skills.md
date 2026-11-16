# Skill UI/UX cho Admin JavaFX

## 1. Nguồn và cài đặt

Theo yêu cầu của Hải ngày 16/11/2026, dùng hai skill sau để hỗ trợ thiết kế và rà soát UI/UX Admin:

| Skill | Nguồn được đối chiếu | Trạng thái tại máy Hải |
|---|---|---|
| `design-taste-frontend` (taste-skill 2.5.1) | [Leonxlnx/taste-skill](https://github.com/Leonxlnx/taste-skill/tree/f6b76575f949707b4e3c37cd86cb44b0b98643e8/skills/taste-skill) | Đã có tại thư mục skill cá nhân `.agents/skills/design-taste-frontend`; giữ bản hiện có, không cài trùng. |
| `ui-ux-pro-max` | [nextlevelbuilder/ui-ux-pro-max-skill](https://github.com/nextlevelbuilder/ui-ux-pro-max-skill/tree/902bf616a0cc590d3f89f233f0c4e00072e549f0/.claude/skills/ui-ux-pro-max) | Đã cài bằng skill-installer vào `.codex/skills/ui-ux-pro-max`. |

- Đường dẫn trong bảng tính từ thư mục người dùng; không phải thư mục trong repository. Skill mới có thể dùng từ lượt tiếp theo.
- Đã kiểm tra taste-skill có đủ 1.631 file theo cây nguồn; `SKILL.md` khớp sau chuẩn hóa CRLF/LF. Không tuyên bố mọi file đã được đối chiếu nội dung sau chuẩn hóa.
- UI UX Pro Max đã có `SKILL.md`, `scripts/` và `data/` thực, không phụ thuộc symlink hỏng. Đã chạy thành công tìm kiếm UX và stack JavaFX.
- Cài skill trên máy không tự cài cho Minh hoặc các máy khác. Repository chỉ lưu hướng dẫn và nguồn, không sao chép toàn bộ asset/script của bên thứ ba vào `docs/`, không thêm dependency vào POM.

## 2. Áp dụng cho JavaFX 21

[Chuẩn thiết kế Admin](admin-design-system.md) tiếp tục là nguồn quyết định màu, chữ, khoảng cách và thành phần. [Spec màn hình](admin-screens.md) quyết định hành vi; skill hỗ trợ lựa chọn và rà soát, không thay yêu cầu đã chốt.

| Nguồn | Tiếp nhận cho Admin | Phần ngoài phạm vi native |
|---|---|---|
| Taste | Căn lề nhất quán, phân cấp rõ, nội dung cụ thể, một hệ thống thành phần; tránh trang trí rườm rà và giao diện mẫu chung chung. | Hero trang quảng bá, ảnh sản phẩm, shader/WebGL, HTML/CSS trình duyệt, hoạt ảnh cuộn và browser gate. Không đưa những thành phần này vào app chỉ để đáp ứng quy tắc web. |
| UI UX Pro Max | Luồng tác vụ, nhãn nhập liệu, lỗi dễ hiểu, focus bàn phím, mật độ bảng, tương phản hai theme và biểu đồ đọc được. | Không áp máy móc cỡ chữ/kích thước chạm mobile, Tailwind/React hoặc animation làm chậm thao tác desktop. |

- Bộ dữ liệu `--stack javafx` vừa kiểm tra ghi **JavaFX 26**. Mọi API/CSS phải được xác minh với JavaFX **21.0.5** trong POM, tài liệu [Node JavaFX 21](https://openjfx.io/javadoc/21/javafx.graphics/javafx/scene/Node.html) và [CSS JavaFX 21](https://openjfx.io/javadoc/21/javafx.graphics/javafx/scene/doc-files/cssref.html).
- Không tự nâng JavaFX, thêm framework, đổi palette/font hoặc dựng một hệ thống thiết kế thứ hai. Chỉ thay chuẩn đã chốt khi Hải đồng ý; thay phần dùng chung phải phối hợp Minh.
- Browser gate của taste không kiểm tra JavaFX. Ghi rõ không áp dụng cho native; không ghi PASS nếu chỉ chạy script kiểm tra HTML hoặc chưa xem app thật.

## 3. Quy trình cho các màn hình tiếp theo

1. Đọc đúng task/spec và chuẩn Admin; xem màn hình hiện có để tái sử dụng.
2. Đối chiếu nguyên tắc phù hợp của taste. Với UX cụ thể, dùng truy vấn nhỏ, một mục tiêu mỗi lần; chỉ tìm stack khi cần chi tiết triển khai.
3. Kiểm tra kết quả trả về có đúng nền tảng và vấn đề không. Nếu lệch, thu hẹp một lần; không lưu kết quả chưa kiểm chứng làm quy tắc dự án.
4. Chuyển đề xuất phù hợp sang JavaFX 21 và CSS `-fx-*`; dùng `TableView`, control/dialog hiện có, Allman cho code mới.
5. Build và chạy kiểm tra chức năng; xem trực quan app sáng/tối, focus, popup, trạng thái rỗng/lỗi và nội dung dài. Nếu Computer Use lỗi, ghi phần trực quan còn chờ, không dùng kiểm tra node thay bằng chứng nhìn app.
6. Ghi kết quả vào [checklist](implementation-tasks.md); sửa chuẩn thiết kế chỉ khi quyết định giao diện thay đổi. Không sinh `MASTER.md` song song với chuẩn Admin hiện có.

## 4. Lệnh tra cứu tại máy đã cài

Chạy PowerShell; đường dẫn được suy ra từ thư mục người dùng, không ghi đường dẫn máy cá nhân vào repo:

```powershell
$uiSkillRoot = Join-Path $env:USERPROFILE '.codex/skills/ui-ux-pro-max'
$uiSearch = Join-Path $uiSkillRoot 'scripts/search.py'
python $uiSearch 'keyboard focus contrast' --domain ux
python $uiSearch 'tableview keyboard focus' --stack javafx
```

Hai lệnh đã chạy thành công ngày 16/11/2026. Kết quả focus/bảng phù hợp để tham khảo; các ví dụ web và liên kết JavaFX 26 chưa phải bằng chứng tương thích JavaFX 21.

## 5. Cài trên máy khác

Nếu Codex có skill-installer chuẩn, dùng helper sau khi kiểm tra thư mục đích chưa tồn tại. Installer không ghi đè skill đã có:

```powershell
$uiInstaller = Join-Path $env:USERPROFILE '.codex/skills/.system/skill-installer/scripts/install-skill-from-github.py'
$tasteDestination = Join-Path $env:USERPROFILE '.agents/skills'
$codexDestination = Join-Path $env:USERPROFILE '.codex/skills'
python $uiInstaller --repo Leonxlnx/taste-skill --ref f6b76575f949707b4e3c37cd86cb44b0b98643e8 --path skills/taste-skill --name design-taste-frontend --dest $tasteDestination
python $uiInstaller --repo nextlevelbuilder/ui-ux-pro-max-skill --ref 902bf616a0cc590d3f89f233f0c4e00072e549f0 --path .claude/skills/ui-ux-pro-max --dest $codexDestination
```

Các lệnh cài lại trên máy khác là hướng dẫn, chưa được chạy nguyên bộ tại máy Hải vì taste đã tồn tại. Agent khác có thể dùng cơ chế cài riêng của nó từ cùng nguồn; không coi công cụ cá nhân của Hải là dependency sản phẩm.
