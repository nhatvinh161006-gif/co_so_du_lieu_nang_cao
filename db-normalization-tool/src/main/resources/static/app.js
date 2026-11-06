'use strict';
const $ = id => document.getElementById(id);
const sampleU = 'MaSV, TenSV, NgaySinh, MaNganh, TenNganh, MaKhoa, TenKhoa, MaHP, TenHP, SoTC, MaLHP, HocKy, Thu, TietBD, MaGV, TenGV, HocVi, MaPhong, ToaNha, SucChua, Diem';
const sampleF = `MaSV -> TenSV, NgaySinh, MaNganh
MaNganh -> TenNganh, MaKhoa
MaKhoa -> TenKhoa
MaHP -> TenHP, SoTC
MaLHP -> MaHP, HocKy, MaGV, MaPhong, Thu, TietBD
MaGV -> TenGV, HocVi
MaPhong -> ToaNha, SucChua
MaSV, MaLHP -> Diem
MaPhong, HocKy, Thu, TietBD -> MaLHP
MaGV, HocKy, Thu, TietBD -> MaLHP`;
let tool = 'closure', resultText = '', toastTimer, analysisRequestVersion = 0;
const fieldIds = ['attributes', 'dependencies', 'query-x', 'query-y'];
let undoState = null;
function saveUndo() {
  if (!fieldIds.some(id => $(id).value.trim())) return;
  undoState = { values: fieldIds.map(id => $(id).value), tool };
  $('undo-bar').hidden = false;
}
function clearErrors() {
  fieldIds.forEach(id => { $(id).removeAttribute('aria-invalid'); $(`${id}-error`).hidden = true; });
  $('import-error').hidden = true;
}
const setText = values => values.length ? `{ ${values.join(', ')} }` : '∅';
function toast(message) { $('toast').textContent = message; $('toast').hidden = false; clearTimeout(toastTimer); toastTimer = setTimeout(() => { $('toast').hidden = true; }, 3500); }
function clearResult() { $('result').hidden = true; $('empty-result').hidden = false; $('error').hidden = true; resultText = ''; clearErrors(); }
function updateCounts() {
  const u = $('attributes').value.trim();
  $('attribute-count').textContent = `${u ? u.split(',').filter(x => x.trim()).length : 0} thuộc tính`;
  $('fd-count').textContent = `${$('dependencies').value.split(/\r?\n/).filter(x => x.trim() && !x.trim().startsWith('#')).length} phụ thuộc`;
  $('data-status').textContent = u ? 'Dữ liệu sẽ được kiểm tra khi phân tích' : 'Sẵn sàng nhập dữ liệu';
}
function showPage(page) {
  ['workspace', 'dataset', 'simulation', 'history', 'guide'].forEach(name => { const el = $(`page-${name}`); if (el) el.hidden = name !== page; });
  document.querySelectorAll('[data-page]').forEach(button => { button.classList.toggle('active', button.dataset.page === page); if (button.dataset.page === page) button.setAttribute('aria-current', 'page'); else button.removeAttribute('aria-current'); });
  $('breadcrumb').textContent = { workspace: 'Phân tích lược đồ', dataset: 'Dữ liệu & sơ đồ EER', simulation: 'Nghiệp vụ & Ràng buộc CSDL', history: 'Lịch sử phân tích (ObjectDB)', guide: 'Hướng dẫn sử dụng' }[page];
  if (page === 'history') loadHistory();
  if (page === 'simulation') loadSimulationData();
}
function loadSample() {
  analysisRequestVersion++;
  saveUndo();
  $('attributes').value = sampleU; $('dependencies').value = sampleF; $('query-x').value = 'MaSV'; $('query-y').value = 'TenKhoa';
  clearResult(); updateCounts(); showPage('workspace'); toast('Đã nạp dữ liệu mẫu: 21 thuộc tính, 10 phụ thuộc.');
}
function showError(error, isImport = false) {
  clearResult();
  const message = error.message;
  const apiField = error.field || '';
  const field = apiField === 'universe' || /^U:/.test(message) ? 'attributes'
    : apiField.startsWith('dependencies') || /^F[, :]/.test(message) ? 'dependencies'
      : apiField === 'x' || /^X:/.test(message) ? 'query-x'
        : apiField === 'y' || /^Y:/.test(message) ? 'query-y' : null;
  if (isImport) {
    $('import-error').textContent = message; $('import-error').hidden = false; $('import').focus();
  } else if (field) {
    $(`${field}-error`).textContent = message; $(`${field}-error`).hidden = false;
    $(field).setAttribute('aria-invalid', 'true'); $(field).focus();
  } else { $('error').textContent = message; $('error').hidden = false; }
  $('data-status').textContent = 'Cần kiểm tra lại dữ liệu';
}
function selectTool(value) {
  analysisRequestVersion++;
  tool = value; clearResult();
  document.querySelectorAll('[data-tool]').forEach(button => { button.classList.toggle('selected', button.dataset.tool === tool); button.setAttribute('aria-pressed', String(button.dataset.tool === tool)); });
  const settings = {
    closure: { label: 'Tập thuộc tính cần tính bao đóng (X)', help: 'Tìm tất cả thuộc tính có thể suy ra từ X.', button: 'Tính bao đóng →', query: true, y: false },
    implication: { label: 'Vế trái của phụ thuộc cần kiểm tra (X)', help: 'Phụ thuộc X → Y đúng khi Y ⊆ X⁺.', button: 'Kiểm tra suy diễn →', query: true, y: true },
    keys: { label: '', help: 'Tìm tất cả khóa ứng viên của U theo tập phụ thuộc F.', button: 'Tìm tất cả khóa →', query: false, y: false },
    'minimal-cover': { label: '', help: 'Tách vế phải, rút gọn vế trái và loại phụ thuộc dư thừa.', button: 'Tìm phủ tối thiểu →', query: false, y: false },
    'check-nf': { label: '', help: 'Kiểm tra dạng chuẩn cao nhất theo giả định các giá trị nguyên tử.', button: 'Kiểm tra dạng chuẩn →', query: false, y: false },
    'decompose-3nf': { label: '', help: 'Tổng hợp các quan hệ con từ phủ tối thiểu.', button: 'Phân rã 3NF →', query: false, y: false }
  }[tool];
  $('query-fields').hidden = !settings.query;
  $('query-x-field').hidden = !settings.query;
  $('y-field').hidden = !settings.y;
  $('x-label').textContent = settings.label;
  $('query-help').textContent = settings.help;
  $('run').textContent = settings.button;
}
function appendStep(step) {
  const li = document.createElement('li'); li.dataset.step = step.step;
  const strong = document.createElement('strong'); strong.textContent = `${step.step}. ${step.action}`;
  const p = document.createElement('p'); p.textContent = step.detail;
  li.append(strong, p); $('steps').append(li);
}
function renderItems(items) {
  $('result-attributes').replaceChildren();
  items.forEach(value => {
    const chip = document.createElement('span');
    chip.className = 'chip';
    chip.textContent = value;
    $('result-attributes').append(chip);
  });
}
function renderAnalysis(result, universe, activeTool) {
  let label, verdict, count, items = [], note;
  if (activeTool === 'closure' || activeTool === 'implication') {
    label = activeTool === 'closure' ? 'BAO ĐÓNG THUỘC TÍNH' : 'KẾT QUẢ SUY DIỄN';
    count = `${result.closure.length} / ${universe.length} thuộc tính`;
    verdict = activeTool === 'closure' ? `${setText(Relatio.attributes($('query-x').value, 'X', true))}⁺`
      : (result.implied ? 'Suy ra được từ F' : 'Không suy ra được từ F');
    items = result.closure;
    note = activeTool === 'closure'
      ? (result.closure.length === universe.length ? 'X⁺ = U: X là siêu khóa; cần kiểm tra tính tối tiểu để kết luận khóa ứng viên.' : 'X⁺ chưa bao phủ U: X không phải siêu khóa.')
      : `${setText(Relatio.attributes($('query-x').value, 'X', true))} → ${setText(Relatio.attributes($('query-y').value, 'Y', true))}: ${result.implied ? 'mọi thuộc tính Y đều thuộc X⁺.' : 'có thuộc tính Y không thuộc X⁺.'}`;
  } else if (activeTool === 'keys') {
    label = 'KHÓA ỨNG VIÊN'; count = `${result.keys.length} khóa`;
    verdict = result.keys.length ? `Tìm được ${result.keys.length} khóa ứng viên` : 'Không tìm thấy khóa ứng viên';
    items = result.keys.map(setText);
    note = `Nguồn: ${setText(result.source)} · Đích: ${setText(result.destination)} · Trung gian: ${setText(result.intermediate)}.`;
  } else if (activeTool === 'minimal-cover') {
    label = 'PHỦ TỐI THIỂU'; count = `${result.minimalCover.length} phụ thuộc`;
    verdict = 'Phủ tối thiểu của F';
    items = result.minimalCover.map(fd => `${setText(fd.left)} → ${setText(fd.right)}`);
    note = 'Mỗi phụ thuộc có vế phải đơn thuộc tính.';
  } else if (activeTool === 'check-nf') {
    label = 'DẠNG CHUẨN'; count = `${result.violations.length} lý do vi phạm`;
    verdict = `Dạng chuẩn cao nhất: ${result.highestNormalForm}`;
    items = result.candidateKeys.map(key => `Khóa: ${setText(key)}`);
    note = `Thuộc tính khóa: ${setText(result.primeAttributes)}. ${result.violations.map(v => `${v.normalForm}: ${v.dependency} — ${v.reason}`).join(' ') || 'Không có vi phạm.'}`;
  } else {
    label = 'PHÂN RÃ 3NF'; count = `${result.relations.length} quan hệ`;
    verdict = 'Các quan hệ sau phân rã';
    items = result.relations.map((relation, index) => `R${index + 1}(${relation.join(', ')})`);
    note = 'Phân rã được tổng hợp từ phủ tối thiểu; xem từng bước để theo dõi quá trình tạo và rút gọn lược đồ.';
  }
  $('result-label').textContent = label;
  $('result-count').textContent = count;
  $('result-verdict').textContent = verdict;
  $('closure-label').textContent = activeTool === 'closure' || activeTool === 'implication' ? 'Thuộc tính kết quả' : 'Kết quả';
  renderItems(items);
  $('result-note').textContent = note;
  $('steps').replaceChildren();
  result.steps.forEach(appendStep);
  $('steps-details').open = true;
  $('empty-result').hidden = true; $('result').hidden = false;
  $('data-status').textContent = 'Dữ liệu hợp lệ · Đã phân tích từ backend';
  resultText = `${verdict}\n${items.join('\n')}\n${note}\n\n${result.steps.map(step => `${step.step}. ${step.action}: ${step.detail}`).join('\n')}`;
}
async function run() {
  const requestVersion = ++analysisRequestVersion;
  const activeTool = tool;
  const runButton = $('run');
  const originalButtonText = runButton.textContent;
  const toolButtons = [...document.querySelectorAll('[data-tool]')];
  runButton.disabled = true;
  runButton.textContent = 'Đang phân tích…';
  toolButtons.forEach(button => { button.disabled = true; });
  $('data-status').textContent = 'Đang gửi yêu cầu đến backend…';
  clearResult();
  try {
    const { universe, dependencies } = Relatio.parse($('attributes').value, $('dependencies').value);
    const body = { universe, dependencies };
    if (activeTool === 'closure' || activeTool === 'implication') {
      body.x = Relatio.attributes($('query-x').value, 'X', true);
      Relatio.checkSubset(body.x, universe, 'X');
    }
    if (activeTool === 'implication') {
      body.y = Relatio.attributes($('query-y').value, 'Y', true);
      Relatio.checkSubset(body.y, universe, 'Y');
    }
    const response = await fetch(`/api/algorithms/${activeTool}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body)
    });
    const result = await response.json();
    if (!response.ok) {
      const error = new Error(result.message || `Backend trả về HTTP ${response.status}.`);
      error.field = result.field;
      throw error;
    }
    if (requestVersion === analysisRequestVersion) {
      renderAnalysis(result, universe, activeTool);
      saveHistory(universe, dependencies, activeTool, resultText);
    }
  } catch (error) {
    if (requestVersion === analysisRequestVersion) showError(error);
  } finally {
    runButton.disabled = false;
    runButton.textContent = originalButtonText;
    toolButtons.forEach(button => { button.disabled = false; });
  }
}
function download(text, filename) {
  const url = URL.createObjectURL(new Blob([text], { type: 'text/plain;charset=utf-8' }));
  const a = document.createElement('a'); a.href = url; a.download = filename; document.body.append(a); a.click(); a.remove(); setTimeout(() => URL.revokeObjectURL(url), 1000);
}
document.querySelectorAll('[data-page]').forEach(button => button.addEventListener('click', () => showPage(button.dataset.page)));
document.querySelectorAll('[data-tool]').forEach(button => button.addEventListener('click', () => selectTool(button.dataset.tool)));
$('load-sample').addEventListener('click', loadSample); $('use-dataset').addEventListener('click', loadSample);
$('reset').addEventListener('click', () => { analysisRequestVersion++; saveUndo(); fieldIds.forEach(id => { $(id).value = ''; }); clearResult(); updateCounts(); $('attributes').focus(); });
$('undo').addEventListener('click', () => {
  if (!undoState) return;
  undoState.values.forEach((value, index) => { $(fieldIds[index]).value = value; });
  selectTool(undoState.tool); undoState = null; $('undo-bar').hidden = true;
  updateCounts(); $('attributes').focus(); toast('Đã khôi phục dữ liệu. Bạn có thể chạy lại phân tích.');
});
['attributes', 'dependencies', 'query-x', 'query-y'].forEach(id => $(id).addEventListener('input', () => { analysisRequestVersion++; clearResult(); updateCounts(); }));
['query-x', 'query-y'].forEach(id => $(id).addEventListener('keydown', event => { if (event.key === 'Enter') run(); }));
$('run').addEventListener('click', run);
$('import').addEventListener('click', () => $('file-input').click());
$('file-input').addEventListener('change', async event => {
  const file = event.target.files[0]; if (!file) return;
  analysisRequestVersion++;
  try {
    if (file.size > 1024 * 1024) throw new Error('Chọn file văn bản nhỏ hơn 1 MB.');
    const parsed = Relatio.parseFile(await file.text());
    saveUndo();
    $('attributes').value = parsed.u; $('dependencies').value = parsed.f; $('query-x').value = ''; $('query-y').value = '';
    clearResult(); updateCounts(); toast(`Đã nhập ${file.name}.`);
  } catch (error) { showError(error, true); }
  finally { event.target.value = ''; }
});
$('export').addEventListener('click', () => {
  try {
    const { universe, dependencies } = Relatio.parse($('attributes').value, $('dependencies').value);
    download(`U: ${universe.join(', ')}\n\nF:\n${dependencies.map(fd => `${fd.left.join(', ') || '∅'} -> ${fd.right.join(', ') || '∅'}`).join('\n')}\n`, 'U_F.txt');
    toast('Đã xuất bộ U, F hiện tại.');
  } catch (error) { showError(error); }
});
$('copy-result').addEventListener('click', async () => {
  try { await navigator.clipboard.writeText(resultText); toast('Đã sao chép kết quả.'); }
  catch { download(resultText, 'ket_qua.txt'); toast('Không truy cập được clipboard; đã tải kết quả thành file.'); }
});
showPage('workspace'); updateCounts();

function saveHistory(universe, dependencies, activeTool, detail) {
  const toolNames = {
    closure: 'TINH_BAO_DONG',
    implication: 'KIEM_TRA_SUY_DIEN',
    keys: 'TIM_TAT_CA_KHOA',
    'minimal-cover': 'PHU_TOI_TIEU',
    'check-nf': 'KIEM_TRA_DANG_CHUAN',
    'decompose-3nf': 'PHAN_RA_3NF'
  };
  const extraInput = activeTool === 'closure' ? `{${$('query-x').value.trim()}}⁺`
    : (activeTool === 'implication' ? `${$('query-x').value.trim()} → ${$('query-y').value.trim()}` : '');
  fetch('/api/history', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      tapThuocTinhU: universe.join(', '),
      tapPhuThuocHamF: dependencies.map(fd => `${fd.left.join(', ')} -> ${fd.right.join(', ')}`).join('\n'),
      loaiThuatToan: toolNames[activeTool] || activeTool.toUpperCase(),
      dauVaoBoSung: extraInput,
      ketQuaChiTiet: detail,
      thoiGianThucThiMs: 5,
      thanhCong: true
    })
  }).catch(() => {});
}

async function loadHistory() {
  const container = $('history-container');
  const loading = $('history-loading');
  const empty = $('history-empty');
  if (!container) return;
  container.replaceChildren();
  loading.hidden = false;
  empty.hidden = true;
  try {
    const res = await fetch('/api/history');
    if (!res.ok) throw new Error('Không thể tải lịch sử');
    const data = await res.json();
    loading.hidden = true;
    if (!Array.isArray(data) || data.length === 0) {
      empty.hidden = false;
      return;
    }
    data.forEach(item => {
      const card = document.createElement('div');
      card.className = 'history-card';

      const header = document.createElement('div');
      header.className = 'history-card-header';
      header.innerHTML = `
        <div class="history-title">
          <span class="badge-oid">OID #${item.id}</span>
          <strong>${item.loaiThuatToan || 'PHÂN TÍCH'}</strong>
          <span class="badge-duration">${item.thoiGianThucThiMs || 0} ms</span>
        </div>
        <div class="badge-time">${item.thoiGianChay || ''}</div>
      `;

      const body = document.createElement('div');
      body.className = 'history-card-body';
      
      const meta = document.createElement('div');
      meta.className = 'history-meta';
      meta.innerHTML = `
        <div class="history-meta-item">
          <strong>Tập thuộc tính U</strong>
          <span>${item.tapThuocTinhU || '∅'}</span>
        </div>
        <div class="history-meta-item">
          <strong>Phụ thuộc hàm F</strong>
          <span>${item.tapPhuThuocHamF || '∅'}</span>
        </div>
        ${item.dauVaoBoSung ? `
        <div class="history-meta-item">
          <strong>Đầu vào phụ</strong>
          <span>${item.dauVaoBoSung}</span>
        </div>` : ''}
      `;

      const resultBox = document.createElement('div');
      resultBox.className = 'history-result-box';
      resultBox.textContent = item.ketQuaChiTiet || 'Không có chi tiết kết quả';

      body.append(meta, resultBox);
      card.append(header, body);
      container.append(card);
    });
  } catch (err) {
    loading.hidden = true;
    empty.hidden = false;
  }
}

async function clearHistory() {
  if (!confirm('Bạn có chắc chắn muốn xóa toàn bộ lịch sử trong ObjectDB không?')) return;
  try {
    const res = await fetch('/api/history', { method: 'DELETE' });
    if (res.ok) {
      toast('Đã xóa sạch lịch sử trong ObjectDB.');
      loadHistory();
    } else {
      toast('Không thể xóa lịch sử.');
    }
  } catch (err) {
    toast('Lỗi khi gửi yêu cầu xóa.');
  }
}

if ($('refresh-history')) $('refresh-history').addEventListener('click', loadHistory);
if ($('clear-history')) $('clear-history').addEventListener('click', clearHistory);


/* ==========================================================================
   MODULE: MÔ PHỎNG NGHIỆP VỤ & RÀNG BUỘC TRANSACTION OBJECTDB
   ========================================================================== */
let simStudentsCache = [];
let simCoursesCache = [];
let simQueriesCache = [];

async function loadSimulationData() {
  const loadingStudents = $('sim-students-loading');
  const loadingCourses = $('sim-courses-loading');
  const loadingQueries = $('sim-queries-loading');
  if (loadingStudents) loadingStudents.hidden = false;
  if (loadingCourses) loadingCourses.hidden = false;
  if (loadingQueries) loadingQueries.hidden = false;

  try {
    const [resSv, resHp, resQ] = await Promise.all([
      fetch('/api/simulation/students'),
      fetch('/api/simulation/courses'),
      fetch('/api/simulation/queries')
    ]);

    if (!resSv.ok) {
      toast("Backend chưa nạp API mới (HTTP " + resSv.status + "). Bạn vui lòng khởi động lại (restart) Spring Boot terminal nhé!");
      return;
    }
    if (resSv.ok) {
      simStudentsCache = await resSv.json();
      renderSimulationStudents(simStudentsCache);
      populateStudentDropdown(simStudentsCache);
    }
    if (resHp.ok) {
      simCoursesCache = await resHp.json();
      renderSimulationCourses(simCoursesCache);
      populateCourseDropdown(simCoursesCache);
    }
    if (resQ.ok) {
      simQueriesCache = await resQ.json();
      renderSimulationQueries(simQueriesCache);
    }
  } catch (err) {
    toast('Không thể kết nối đến API mô phỏng ObjectDB: ' + err.message);
  } finally {
    if (loadingStudents) loadingStudents.hidden = true;
    if (loadingCourses) loadingCourses.hidden = true;
    if (loadingQueries) loadingQueries.hidden = true;
  }
}

function populateStudentDropdown(students) {
  const select = $('sim-select-student');
  if (!select) return;
  const currentVal = select.value;
  select.innerHTML = '<option value="">-- Chọn sinh viên --</option>' +
    students.map(s => `<option value="${s.maSV}">${s.maSV} - ${s.hoTen} (${s.gioiTinh}, ĐTB: ${s.diemTB})</option>`).join('');
  if (currentVal && students.some(s => s.maSV === currentVal)) {
    select.value = currentVal;
  }
  updateStudentInfo();
}

function populateCourseDropdown(courses) {
  const select = $('sim-select-course');
  if (!select) return;
  const currentVal = select.value;
  select.innerHTML = '<option value="">-- Chọn lớp học phần --</option>' +
    courses.map(c => {
      const tq = c.maHPTienQuyet ? ` [Cần ${c.maHPTienQuyet}]` : '';
      return `<option value="${c.maLHP}">${c.maLHP} - ${c.tenHP}${tq} (Sĩ số: ${c.siSoHienTai}/${c.sucChua})</option>`;
    }).join('');
  if (currentVal && courses.some(c => c.maLHP === currentVal)) {
    select.value = currentVal;
  }
  updateCourseInfo();
}

function updateStudentInfo() {
  const select = $('sim-select-student');
  const info = $('sim-student-info');
  if (!select || !info) return;
  const sv = simStudentsCache.find(s => s.maSV === select.value);
  if (!sv) {
    info.textContent = 'Chưa chọn sinh viên';
    return;
  }
  const monDat = (sv.dsDangKy || []).filter(d => d.laMonDat).map(d => d.maHP || d.tenHP).join(', ') || 'Chưa đạt môn nào';
  info.innerHTML = `<strong>ĐTB:</strong> ${sv.diemTB} | <strong>Tích lũy:</strong> ${sv.tinChiTichLuy} TC (${sv.xepLoai})<br><strong>Môn đã đạt:</strong> ${monDat}`;
}

function updateCourseInfo() {
  const select = $('sim-select-course');
  const info = $('sim-course-info');
  if (!select || !info) return;
  const c = simCoursesCache.find(item => item.maLHP === select.value);
  if (!c) {
    info.textContent = 'Chưa chọn lớp học phần';
    return;
  }
  const tqText = c.maHPTienQuyet ? `<span style="color:#a84022;font-weight:600;">${c.tenHPTienQuyet} (${c.maHPTienQuyet})</span>` : 'Không yêu cầu';
  info.innerHTML = `<strong>GV:</strong> ${c.tenGV || 'Chưa phân công'} | <strong>Lịch:</strong> ${c.thongTinLichHoc}<br><strong>Môn tiên quyết:</strong> ${tqText}`;
}

function renderSimulationStudents(students) {
  const container = $('sim-students-list');
  if (!container) return;
  container.innerHTML = students.map(s => {
    const listDK = (s.dsDangKy || []).map(d => {
      const badgeClass = d.laMonDat ? 'color:#246039;background:#edf5e8;border:1px solid #d4e7c7;' : 'color:#9a3e26;background:#fdf1ec;border:1px solid #f2cec2;';
      return `<span style="display:inline-block;padding:2px 6px;border-radius:3px;font-size:11px;${badgeClass}">${d.tenHP} (${d.diemTongKet}) - ${d.laMonDat ? 'Đạt' : 'Rớt'}</span>`;
    }).join(' ') || '<span style="color:#74817b;font-size:11px;">Chưa đăng ký môn nào</span>';

    return `
      <div class="sim-item-card">
        <div class="sim-item-header">
          <span class="sim-item-title">${s.maSV} — ${s.hoTen} (${s.gioiTinh})</span>
          <span class="badge-oid">ĐTB: ${s.diemTB} (${s.xepLoai})</span>
        </div>
        <div style="font-size:11px;color:#617467;margin-bottom:6px;">
          Ngày sinh: ${s.ngaySinh} | Tích lũy: ${s.tinChiTichLuy} tín chỉ | Đã đăng ký: ${(s.dsDangKy || []).length} môn
        </div>
        <div style="display:flex;flex-wrap:wrap;gap:4px;">${listDK}</div>
      </div>
    `;
  }).join('');
}

function renderSimulationCourses(courses) {
  const container = $('sim-courses-list');
  if (!container) return;
  container.innerHTML = courses.map(c => {
    const tqBadge = c.maHPTienQuyet 
      ? `<span style="background:#fff3ef;color:#b03a1a;border:1px solid #f2c7b8;padding:2px 6px;border-radius:3px;font-size:11px;">Tiên quyết: ${c.tenHPTienQuyet} (${c.maHPTienQuyet})</span>`
      : `<span style="background:#f4f7ec;color:#496b3a;border:1px solid #dbe6cb;padding:2px 6px;border-radius:3px;font-size:11px;">Không có tiên quyết</span>`;

    return `
      <div class="sim-item-card">
        <div class="sim-item-header">
          <span class="sim-item-title">${c.maLHP} — ${c.tenHP} (${c.soTC} tín chỉ)</span>
          <span class="badge-oid">Sĩ số: ${c.siSoHienTai} / ${c.sucChua}</span>
        </div>
        <div style="font-size:11px;color:#617467;margin-bottom:6px;">
          GV: ${c.tenGV || 'N/A'} | Phòng: ${c.maPhong} (${c.toaNha}) | Lịch: Thứ ${c.thu}, Tiết ${c.tietBD}
        </div>
        <div>${tqBadge}</div>
      </div>
    `;
  }).join('');
}

function renderSimulationQueries(queries) {
  const container = $('sim-queries-list');
  if (!container) return;
  container.innerHTML = queries.map(q => `
    <details class="sim-item-card" style="padding:10px 14px;cursor:pointer;">
      <summary style="font-weight:650;color:#2c4036;font-size:12px;outline:none;">
        <span style="color:#20634f;margin-right:6px;">#${q.id}</span> ${q.title}
      </summary>
      <div style="margin-top:10px;font-size:12px;color:#45554d;">
        <p style="margin:0 0 8px;">${q.description}</p>
        <div style="display:grid;grid-template-columns:1fr 1fr;gap:10px;margin-bottom:8px;">
          <div>
            <strong style="font-size:10px;color:#7a896e;text-transform:uppercase;letter-spacing:1px;">Cú pháp OQL (ODMG 3.0):</strong>
            <div class="sim-query-block">${q.oql}</div>
          </div>
          <div>
            <strong style="font-size:10px;color:#7a896e;text-transform:uppercase;letter-spacing:1px;">Cú pháp JPQL (ObjectDB JPA):</strong>
            <div class="sim-query-block">${q.jpql}</div>
          </div>
        </div>
        <div style="background:#f4f7ec;padding:8px 10px;border-radius:5px;border:1px solid #e0ebd5;margin-bottom:6px;">
          <strong style="color:#2c4e24;font-size:11px;">Ưu việt hơn RDBMS:</strong> ${q.advantage}
        </div>
        <div style="font-size:11px;color:#667a6d;">
          <strong>Kết quả mẫu thực thi:</strong> ${q.resultSample}
        </div>
      </div>
    </details>
  `).join('');
}

async function executeSimulation(maSV, maLHP) {
  if (!maSV || !maLHP) {
    toast('Vui lòng chọn đầy đủ Sinh viên và Lớp học phần!');
    return;
  }

  const btn = $('sim-execute-btn');
  const container = $('sim-result-container');
  if (btn) btn.disabled = true;

  try {
    const res = await fetch('/api/simulation/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ maSV, maLHP })
    });

    if (!res.ok) {
      toast("Lỗi HTTP " + res.status + ": Backend chưa nạp Controller mới. Hãy khởi động lại Spring Boot trong terminal!");
      return;
    }
    const data = await res.json();
    renderSimulationResult(data);

    // Tự động tải lại dữ liệu đối tượng sau giao dịch
    const [resSv, resHp] = await Promise.all([
      fetch('/api/simulation/students'),
      fetch('/api/simulation/courses')
    ]);
    if (resSv.ok) {
      simStudentsCache = await resSv.json();
      renderSimulationStudents(simStudentsCache);
      populateStudentDropdown(simStudentsCache);
    }
    if (resHp.ok) {
      simCoursesCache = await resHp.json();
      renderSimulationCourses(simCoursesCache);
      populateCourseDropdown(simCoursesCache);
    }

  } catch (err) {
    toast('Lỗi khi thực thi giao dịch: ' + err.message);
  } finally {
    if (btn) btn.disabled = false;
  }
}

function renderSimulationResult(data) {
  const container = $('sim-result-container');
  if (!container) return;
  container.hidden = false;

  const isCommit = data.status === 'COMMIT';
  const cardClass = isCommit ? 'sim-res-commit' : 'sim-res-rollback';
  const badgeClass = isCommit ? 'badge-commit' : 'badge-rollback';
  const icon = isCommit ? '✓' : '✗';
  const title = isCommit ? 'TRANSACTION COMMIT THÀNH CÔNG' : 'TRANSACTION ĐÃ BỊ ROLLBACK';

  const stepsHtml = (data.steps || []).map(step => {
    let style = 'padding:4px 0;border-bottom:1px dashed #e3ebd8;';
    if (step.includes('[ROLLBACK') || step.includes('[EXCEPTION') || step.includes('[VI PHẠM]')) {
      style = 'padding:4px 0;border-bottom:1px dashed #fad8cf;color:#a8351b;font-weight:600;';
    } else if (step.includes('[COMMIT')) {
      style = 'padding:4px 0;border-bottom:1px dashed #cde3c4;color:#1c592b;font-weight:600;';
    }
    return `<div style="${style}">${step}</div>`;
  }).join('');

  container.innerHTML = `
    <div class="sim-res-card ${cardClass}">
      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:10px;flex-wrap:wrap;gap:8px;">
        <div style="display:flex;align-items:center;gap:10px;">
          <span class="sim-status-badge ${badgeClass}">${icon} ${data.status}</span>
          <strong style="font-size:14px;">${title}</strong>
        </div>
        <span class="badge-oid" style="background:#fff;">Bản ghi: ${data.countBefore} → ${data.countAfter}</span>
      </div>
      <p style="margin:6px 0 10px;font-size:13px;line-height:1.6;">
        ${data.message}
      </p>
      <div style="background:#fff;padding:12px;border-radius:6px;font-family:var(--mono);font-size:11px;line-height:1.7;margin-bottom:10px;box-shadow:inset 0 1px 2px rgba(0,0,0,0.03);">
        <strong style="display:block;margin-bottom:4px;color:#526958;font-family:inherit;">QUÁ TRÌNH THỰC THI GIAO DỊCH (TRANSACTION TRACE):</strong>
        ${stepsHtml}
      </div>
      <div style="font-size:12px;line-height:1.6;opacity:.92;">
        <strong>Phân tích tính chất ACID:</strong> ${data.explanation || ''}
      </div>
    </div>
  `;

  container.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
}

// Bắt sự kiện trên trang Simulation
if ($('sim-select-student')) $('sim-select-student').addEventListener('change', updateStudentInfo);
if ($('sim-select-course')) $('sim-select-course').addEventListener('change', updateCourseInfo);

if ($('sim-execute-btn')) {
  $('sim-execute-btn').addEventListener('click', () => {
    const sv = $('sim-select-student') ? $('sim-select-student').value : '';
    const lhp = $('sim-select-course') ? $('sim-select-course').value : '';
    executeSimulation(sv, lhp);
  });
}

if ($('quick-scenario-fail')) {
  $('quick-scenario-fail').addEventListener('click', () => {
    if ($('sim-select-student')) $('sim-select-student').value = 'SV004';
    if ($('sim-select-course')) $('sim-select-course').value = 'LHP01';
    updateStudentInfo();
    updateCourseInfo();
    executeSimulation('SV004', 'LHP01');
  });
}

if ($('quick-scenario-pass')) {
  $('quick-scenario-pass').addEventListener('click', () => {
    if ($('sim-select-student')) $('sim-select-student').value = 'SV001';
    if ($('sim-select-course')) $('sim-select-course').value = 'LHP03';
    updateStudentInfo();
    updateCourseInfo();
    executeSimulation('SV001', 'LHP03');
  });
}

if ($('sim-reset-btn')) {
  $('sim-reset-btn').addEventListener('click', async () => {
    if (!confirm('Khôi phục toàn bộ CSDL ObjectDB về trạng thái mẫu ban đầu?')) return;
    try {
      const res = await fetch('/api/simulation/reset', { method: 'POST' });
      const data = await res.json();
      toast(data.message || 'Đã nạp lại CSDL thành công!');
      loadSimulationData();
      if ($('sim-result-container')) $('sim-result-container').hidden = true;
    } catch (e) {
      toast('Lỗi đặt lại CSDL: ' + e.message);
    }
  });
}

if ($('sim-refresh-btn')) {
  $('sim-refresh-btn').addEventListener('click', () => {
    loadSimulationData();
    toast('Đã làm mới dữ liệu từ ObjectDB.');
  });
}

// Tabs chuyển đổi
function setupSimTabs() {
  const tabs = [
    { btn: 'tab-btn-students', content: 'tab-content-students' },
    { btn: 'tab-btn-courses', content: 'tab-content-courses' },
    { btn: 'tab-btn-queries', content: 'tab-content-queries' }
  ];

  tabs.forEach(tab => {
    const b = $(tab.btn);
    if (!b) return;
    b.addEventListener('click', () => {
      tabs.forEach(t => {
        const otherBtn = $(t.btn);
        const otherContent = $(t.content);
        if (otherBtn) {
          otherBtn.classList.toggle('active', t.btn === tab.btn);
          otherBtn.setAttribute('aria-selected', String(t.btn === tab.btn));
        }
        if (otherContent) otherContent.hidden = (t.btn !== tab.btn);
      });
    });
  });
}
setupSimTabs();
