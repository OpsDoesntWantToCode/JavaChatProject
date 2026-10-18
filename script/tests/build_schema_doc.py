"""Build the schema reference from catalog.sql JSON; Python stdlib only.

Run from repo root: python script/tests/build_schema_doc.py
This reads exported test metadata and writes documentation, never connects to DB.
"""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
data = json.loads((ROOT / 'docs/database/validation_catalog.json').read_text(encoding='utf-8-sig'))
tables = data['tables']
assert len(tables) == 13 and sum(len(t['columns']) for t in tables) == 65


def code(value):
    return '`' + str(value).replace('|', '&#124;').replace('\n', ' ') + '`'


out = [
    '# Database Schema — Java Chat System', '',
    'Thiết kế chính thức dùng chung cho A/B, được nhóm chấp thuận và kiểm thử trên PostgreSQL ' + data['server_version'] + '.', '',
    'Nguồn thực thi: [database.sql](../../script/database.sql). Tài liệu này được tạo từ [catalog PostgreSQL đã xuất](validation_catalog.json), '
    'không suy diễn từ hình ERD. [Quyết định](Database_Decisions.md), [ERD](ERD.mmd), [kết quả kiểm thử](TEST_RESULTS.md).', '',
    '**13 bảng, 65 cột, 58 NOT NULL, 13 PK, 21 FK, 4 UNIQUE, 31 CHECK; 16 index tường minh và 17 backing index = 33 index; 2 trigger nghiệp vụ.**', '',
    'Server-side AES-GCM vẫn **PROVISIONAL**, không phải E2EE. Test schema không chứng minh mã hóa, concurrency hoặc phân quyền Server.', '',
    'Chạy DDL bằng owner trên database mới/rỗng, không dùng như migration. Không có password/key/seed Admin. Runtime Server dùng role riêng, '
    'không phải owner/superuser; cấu hình privilege và kiểm thử quyền thực tế là việc vận hành chưa hoàn thành.', '',
    'Tám cột identity dùng GENERATED ALWAYS, START 1, INCREMENT 1, CACHE 1, NO CYCLE. Mọi PK/FK immediate; '
    'FK ON UPDATE mặc định NO ACTION. NOT NULL được trình bày trong bảng cột, không liệt kê lại constraint loại n của PostgreSQL 18.', '',
    '## Danh mục bảng', '', '| Bảng | Số cột |', '|---|---:|',
]
out += [f"| {code(t['name'])} | {len(t['columns'])} |" for t in tables]
for table in tables:
    out += ['', '## ' + table['name'], '', '| Cột | Kiểu PostgreSQL | NOT NULL | Default / identity |', '|---|---|---|---|']
    for column in table['columns']:
        default = 'GENERATED ALWAYS AS IDENTITY' if column['identity'] == 'a' else column['default']
        out.append(f"| {code(column['name'])} | {code(column['type'])} | {'Có' if column['not_null'] else 'Không'} | {code(default) if default is not None else '—'} |")
    out += ['', 'Ràng buộc:', '']
    for constraint in table['constraints']:
        if constraint['kind'] != 'n':
            out.append('- ' + code(constraint['name']) + ': ' + code(constraint['definition']))
    out += ['', 'Index (gồm cả backing PK/UNIQUE):', '']
    for index in table['indexes']:
        out.append('- ' + code(index['definition']))
    for trigger in table['triggers'] or []:
        out += ['', '- Trigger ' + code(trigger['name']) + ': ' + code(trigger['definition'])]
out += [
    '', '## Ngữ nghĩa bắt buộc ở Server', '',
    '- UUID session là idempotency của OPEN_APP, không phải bearer token; reconnect không tạo lượt mở mới.',
    '- Watermark không là FK, cho phép 0. Clear chỉ một phía; đọc/tìm kiếm lọc cả watermark và message_hidden. Khóa cặp trước cấp ID là điều kiện correctness chưa kiểm thử concurrency.',
    '- Mỗi reporter chỉ báo cáo một reported user một lần trong toàn bộ thời gian. UNIQUE ghép không làm riêng hai cột thành UNIQUE; Server kiểm bằng chứng DM chiều reported → reporter.',
    '- Soft delete cập nhật status, giữ tin và FK; Server áp dụng DELETED terminal, thu hồi session, bảo vệ Admin cuối và kế nhiệm nhóm.',
    '- Các FK sender/group không chứng minh membership hoặc quyền. Quyền Admin hệ thống và Admin nhóm độc lập.',
    '- CHECK IV/mode và unique nonce chỉ kiểm schema; payload/tag/AAD, quản lý DEK/KEK và TLS cần Server triển khai. Không có khóa thô trong DB.',
    '- Canonical username/email, BCrypt và validation đầu vào nằm tại Server bên cạnh CHECK của DB.', '',
    '## Tái tạo tài liệu', '',
    'Xuất lại catalog bằng `script/tests/catalog.sql` trên database test vừa PASS, sau đó chạy '
    '`python script/tests/build_schema_doc.py`. Không xuất schema từ database công việc khác. '
    'Khi DDL thay đổi, phải test lại và đồng bộ catalog, tài liệu, ERD trong cùng thay đổi.', '',
]
(ROOT / 'docs/database/Database_Schema.md').write_text('\n'.join(out), encoding='utf-8')
