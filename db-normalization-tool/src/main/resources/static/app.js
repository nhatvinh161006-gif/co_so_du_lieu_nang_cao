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
let tool = 'closure', resultText = '', toastTimer;
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
  saveUndo();
  $('attributes').value = sampleU; $('dependencies').value = sampleF; $('query-x').value = 'MaSV'; $('query-y').value = 'TenKhoa';
  clearResult(); updateCounts(); showPage('workspace'); toast('Đã nạp dữ liệu mẫu: 21 thuộc tính, 10 phụ thuộc.');
}
function showError(error, isImport = false) {
  clearResult();
  const message = error.message;
  const field = /^U:/.test(message) ? 'attributes' : /^F[, :]/.test(message) ? 'dependencies' : /^X:/.test(message) ? 'query-x' : /^Y:/.test(message) ? 'query-y' : null;
  if (isImport) {
    $('import-error').textContent = message; $('import-error').hidden = false; $('import').focus();
  } else if (field) {
    $(`${field}-error`).textContent = message; $(`${field}-error`).hidden = false;
    $(field).setAttribute('aria-invalid', 'true'); $(field).focus();
  } else { $('error').textContent = message; $('error').hidden = false; }
  $('data-status').textContent = 'Cần kiểm tra lại dữ liệu';
}
function selectTool(value) {
  tool = value; clearResult();
  document.querySelectorAll('[data-tool]').forEach(button => { button.classList.toggle('selected', button.dataset.tool === tool); button.setAttribute('aria-pressed', String(button.dataset.tool === tool)); });
  $('y-field').hidden = tool === 'closure';
  $('x-label').textContent = tool === 'closure' ? 'Tập thuộc tính cần tính bao đóng (X)' : 'Vế trái của phụ thuộc cần kiểm tra (X)';
  $('query-help').textContent = tool === 'closure' ? 'Tìm tất cả thuộc tính có thể suy ra từ X.' : 'Phụ thuộc X → Y đúng khi Y ⊆ X⁺.';
  $('run').textContent = tool === 'closure' ? 'Tính bao đóng →' : 'Kiểm tra suy diễn →';
}
function appendStep(title, detail, values, number) {
  const li = document.createElement('li'); li.dataset.step = number;
  const strong = document.createElement('strong'); strong.textContent = title;
  const p = document.createElement('p'); p.textContent = detail;
  const snapshot = document.createElement('details');
  const summary = document.createElement('summary'); summary.textContent = `Xem bao đóng sau bước này (${values.length} thuộc tính)`;
  const content = document.createElement('p'); content.textContent = `X⁺ = ${setText(values)}`;
  snapshot.append(summary, content);
  li.append(strong, p, snapshot); $('steps').append(li);
}
function run() {
  clearResult();
  try {
    const { universe, dependencies } = Relatio.parse($('attributes').value, $('dependencies').value);
    const x = Relatio.attributes($('query-x').value, 'X', true); Relatio.checkSubset(x, universe, 'X');
    const y = tool === 'implication' ? Relatio.attributes($('query-y').value, 'Y', true) : []; Relatio.checkSubset(y, universe, 'Y');
    const answer = Relatio.closure(x, dependencies);
    const missing = y.filter(value => !answer.closure.includes(value));
    $('result-label').textContent = tool === 'closure' ? 'BAO ĐÓNG THUỘC TÍNH' : 'KẾT QUẢ SUY DIỄN';
    $('result-count').textContent = `${answer.closure.length} / ${universe.length} thuộc tính`;
    $('result-verdict').textContent = tool === 'closure' ? `${setText(x)}⁺` : (missing.length ? 'Không suy ra được từ F' : 'Suy ra được từ F');
    $('result-attributes').replaceChildren();
    answer.closure.forEach(value => { const chip = document.createElement('span'); chip.className = 'chip'; chip.textContent = value; $('result-attributes').append(chip); });
    $('result-note').textContent = tool === 'implication'
      ? `${setText(x)} → ${setText(y)}: ${missing.length ? `FALSE. ${setText(missing)} không nằm trong X⁺.` : 'TRUE. Mọi thuộc tính của Y đều nằm trong X⁺.'}`
      : (answer.closure.length === universe.length ? 'X⁺ = U: X là một siêu khóa. Cần kiểm tra tính tối tiểu để kết luận X là khóa ứng viên.' : 'X⁺ chưa bao phủ U: X không phải siêu khóa.');
    $('steps').replaceChildren();
    $('steps-details').open = true;
    appendStep('Khởi tạo', 'Bắt đầu với các thuộc tính của X.', x, 0);
    answer.steps.forEach((step, index) => appendStep(`${setText(step.left)} → ${setText(step.right)}`, `Vế trái nằm trong bao đóng hiện tại. Thêm ${setText(step.added)}.`, step.closure, index + 1));
    $('empty-result').hidden = true; $('result').hidden = false; $('data-status').textContent = 'Dữ liệu hợp lệ · Đã phân tích';
    resultText = `${$('result-verdict').textContent}\nX+ = ${setText(answer.closure)}\n${$('result-note').textContent}\n\nKhởi tạo: ${setText(x)}\n${answer.steps.map((step, index) => `${index + 1}. ${setText(step.left)} → ${setText(step.right)}; thêm ${setText(step.added)}; X+ = ${setText(step.closure)}`).join('\n')}`;
  } catch (error) { showError(error); }
}
function download(text, filename) {
  const url = URL.createObjectURL(new Blob([text], { type: 'text/plain;charset=utf-8' }));
  const a = document.createElement('a'); a.href = url; a.download = filename; document.body.append(a); a.click(); a.remove(); setTimeout(() => URL.revokeObjectURL(url), 1000);
}
document.querySelectorAll('[data-page]').forEach(button => button.addEventListener('click', () => showPage(button.dataset.page)));
document.querySelectorAll('[data-tool]').forEach(button => button.addEventListener('click', () => selectTool(button.dataset.tool)));
$('load-sample').addEventListener('click', loadSample); $('use-dataset').addEventListener('click', loadSample);
$('reset').addEventListener('click', () => { saveUndo(); fieldIds.forEach(id => { $(id).value = ''; }); clearResult(); updateCounts(); $('attributes').focus(); });
$('undo').addEventListener('click', () => {
  if (!undoState) return;
  undoState.values.forEach((value, index) => { $(fieldIds[index]).value = value; });
  selectTool(undoState.tool); undoState = null; $('undo-bar').hidden = true;
  updateCounts(); $('attributes').focus(); toast('Đã khôi phục dữ liệu. Bạn có thể chạy lại phân tích.');
});
['attributes', 'dependencies', 'query-x', 'query-y'].forEach(id => $(id).addEventListener('input', () => { clearResult(); updateCounts(); }));
['query-x', 'query-y'].forEach(id => $(id).addEventListener('keydown', event => { if (event.key === 'Enter') run(); }));
$('run').addEventListener('click', run);
$('import').addEventListener('click', () => $('file-input').click());
$('file-input').addEventListener('change', async event => {
  const file = event.target.files[0]; if (!file) return;
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
