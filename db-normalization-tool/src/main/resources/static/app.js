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
  ['workspace', 'dataset', 'guide'].forEach(name => { $(`page-${name}`).hidden = name !== page; });
  document.querySelectorAll('[data-page]').forEach(button => { button.classList.toggle('active', button.dataset.page === page); if (button.dataset.page === page) button.setAttribute('aria-current', 'page'); else button.removeAttribute('aria-current'); });
  $('breadcrumb').textContent = { workspace: 'Phân tích lược đồ', dataset: 'Dữ liệu & sơ đồ EER', guide: 'Hướng dẫn sử dụng' }[page];
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
    if (requestVersion === analysisRequestVersion) renderAnalysis(result, universe, activeTool);
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
