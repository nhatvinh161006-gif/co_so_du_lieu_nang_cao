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
const quickDatasets = {
  chain: {
    name: 'Chuỗi phụ thuộc truyền dẫn',
    universe: 'A, B, C, D, E',
    dependencies: 'A -> B\nB -> C\nC -> D',
    queryX: 'A',
    queryY: 'D'
  },
  student: {
    name: 'Quản lý sinh viên - học phần',
    universe: 'MaSV, TenSV, MaLHP, TenHP, SoTC, Diem',
    dependencies: 'MaSV -> TenSV\nMaLHP -> TenHP, SoTC\nMaSV, MaLHP -> Diem',
    queryX: 'MaSV',
    queryY: 'TenSV'
  }
};

let tool = 'closure', resultText = '', toastTimer, analysisRequestVersion = 0;
const fieldIds = ['attributes', 'dependencies', 'query-x', 'query-y', 'dependencies2', 'custom-relations'];
let undoState = null;

function saveUndo() {
  if (!fieldIds.some(id => $(id) && $(id).value.trim())) return;
  undoState = { values: fieldIds.map(id => $(id) ? $(id).value : ''), tool };
  if ($('undo-bar')) $('undo-bar').hidden = false;
}

function clearErrors() {
  fieldIds.forEach(id => {
    const el = $(id);
    if (el) {
      el.removeAttribute('aria-invalid');
      const err = $(`${id}-error`);
      if (err) err.hidden = true;
    }
  });
  if ($('import-error')) $('import-error').hidden = true;
}

const setText = values => values && values.length ? `{ ${values.join(', ')} }` : '∅';

function toast(message) {
  const t = $('toast');
  if (!t) return;
  t.textContent = message;
  t.hidden = false;
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => { t.hidden = true; }, 3500);
}

function clearResult() {
  if ($('result')) $('result').hidden = true;
  if ($('empty-result')) $('empty-result').hidden = false;
  if ($('error')) $('error').hidden = true;
  if ($('sql-ddl-box')) $('sql-ddl-box').style.display = 'none';
  resultText = '';
  clearErrors();
}

function updateCounts() {
  const u = $('attributes') ? $('attributes').value.trim() : '';
  const f = $('dependencies') ? $('dependencies').value : '';
  if ($('attribute-count')) $('attribute-count').textContent = `${u ? u.split(',').filter(x => x.trim()).length : 0} thuộc tính`;
  if ($('fd-count')) $('fd-count').textContent = `${f.split(/\r?\n/).filter(x => x.trim() && !x.trim().startsWith('#')).length} phụ thuộc`;
  if ($('data-status')) $('data-status').textContent = u ? 'Dữ liệu sẽ được kiểm tra khi phân tích' : 'Sẵn sàng nhập dữ liệu';
}

function showPage(page) {
  ['workspace', 'dataset', 'simulation', 'history', 'guide'].forEach(name => {
    const el = $(`page-${name}`);
    if (el) el.hidden = name !== page;
  });
  document.querySelectorAll('[data-page]').forEach(button => {
    button.classList.toggle('active', button.dataset.page === page);
    if (button.dataset.page === page) button.setAttribute('aria-current', 'page');
    else button.removeAttribute('aria-current');
  });
  if ($('breadcrumb')) {
    $('breadcrumb').textContent = {
      workspace: 'Phân tích lược đồ',
      dataset: 'Dữ liệu & sơ đồ EER',
      simulation: 'Nghiệp vụ & Ràng buộc CSDL',
      history: 'Lịch sử phân tích (ObjectDB)',
      guide: 'Hướng dẫn sử dụng'
    }[page] || 'Phân tích lược đồ';
  }
  if (page === 'history') loadHistory();
  if (page === 'simulation') loadSimulationData();
}

function loadSample() {
  analysisRequestVersion++;
  saveUndo();
  if ($('attributes')) $('attributes').value = sampleU;
  if ($('dependencies')) $('dependencies').value = sampleF;
  if ($('query-x')) $('query-x').value = 'MaSV';
  if ($('query-y')) $('query-y').value = 'TenKhoa';
  clearResult();
  updateCounts();
  showPage('workspace');
  toast('Đã nạp dữ liệu mẫu: 21 thuộc tính, 10 phụ thuộc.');
}

function loadQuickDataset(datasetKey) {
  const dataset = quickDatasets[datasetKey];
  if (!dataset) return;
  analysisRequestVersion++;
  saveUndo();
  if ($('attributes')) $('attributes').value = dataset.universe;
  if ($('dependencies')) $('dependencies').value = dataset.dependencies;
  if ($('query-x')) $('query-x').value = dataset.queryX;
  if ($('query-y')) $('query-y').value = dataset.queryY;
  clearResult();
  updateCounts();
  showPage('workspace');
  toast(`Đã nạp bộ dữ liệu: ${dataset.name}`);
}

function showError(error, isImport = false) {
  clearResult();
  const message = error.message;
  const apiField = error.field || '';
  const field = apiField === 'universe' || /^U:/.test(message) ? 'attributes'
    : apiField.startsWith('dependencies2') ? 'dependencies2'
      : apiField.startsWith('dependencies') || /^F[, :]/.test(message) ? 'dependencies'
        : apiField === 'x' || /^X:/.test(message) ? 'query-x'
          : apiField === 'y' || /^Y:/.test(message) ? 'query-y' : null;

  if (isImport) {
    if ($('import-error')) { $('import-error').textContent = message; $('import-error').hidden = false; $('import').focus(); }
  } else if (field && $(field)) {
    const err = $(`${field}-error`);
    if (err) { err.textContent = message; err.hidden = false; }
    $(field).setAttribute('aria-invalid', 'true');
    $(field).focus();
  } else {
    if ($('error')) { $('error').textContent = message; $('error').hidden = false; }
  }
  if ($('data-status')) $('data-status').textContent = 'Cần kiểm tra lại dữ liệu';
}

function selectTool(value) {
  analysisRequestVersion++;
  tool = value;
  clearResult();

  document.querySelectorAll('[data-tool]').forEach(button => {
    button.classList.toggle('selected', button.dataset.tool === tool);
    button.setAttribute('aria-pressed', String(button.dataset.tool === tool));
  });

  const settings = {
    closure: { label: 'Tập thuộc tính cần tính bao đóng (X)', help: 'Tìm tất cả thuộc tính có thể suy ra từ X.', button: 'Tính bao đóng →', query: true, y: false, f2: false, rel: false, arm: false },
    implication: { label: 'Vế trái của phụ thuộc cần kiểm tra (X)', help: 'Phụ thuộc X → Y đúng khi Y ⊆ X⁺.', button: 'Kiểm tra suy diễn →', query: true, y: true, f2: false, rel: false, arm: false },
    keys: { label: '', help: 'Tìm tất cả khóa ứng viên của U theo tập phụ thuộc F.', button: 'Tìm tất cả khóa →', query: false, y: false, f2: false, rel: false, arm: false },
    superkeys: { label: '', help: 'Tính chính xác tổng số siêu khóa bằng Nguyên lý Bù trừ (PIE) & liệt kê mẫu.', button: 'Tìm tất cả siêu khóa →', query: false, y: false, f2: false, rel: false, arm: false },
    'minimal-cover': { label: '', help: 'Tách vế phải, rút gọn vế trái và loại phụ thuộc dư thừa.', button: 'Tìm phủ tối thiểu →', query: false, y: false, f2: false, rel: false, arm: false },
    'check-nf': { label: '', help: 'Kiểm tra dạng chuẩn cao nhất theo giả định các giá trị nguyên tử (1NF..BCNF).', button: 'Kiểm tra dạng chuẩn →', query: false, y: false, f2: false, rel: false, arm: false },
    'decompose-3nf': { label: '', help: 'Tổng hợp các quan hệ con từ phủ tối thiểu (bảo toàn PTH và lossless).', button: 'Phân rã 3NF →', query: false, y: false, f2: false, rel: false, arm: false },
    'decompose-bcnf': { label: '', help: 'Tách nhị phân đệ quy đạt chuẩn BCNF và đảm bảo phép nối không mất mát thông tin.', button: 'Phân rã BCNF →', query: false, y: false, f2: false, rel: false, arm: false },
    equivalence: { label: '', help: 'Kiểm tra hai tập phụ thuộc hàm F1 và F2 có tương đương hay không (F1 ≡ F2).', button: 'Kiểm tra tương đương →', query: false, y: false, f2: true, rel: false, arm: false },
    chase: { label: '', help: 'Kiểm tra bảo toàn thông tin (Lossless Join) bằng phương pháp bảng Chase.', button: 'Chạy thuật toán Chase →', query: false, y: false, f2: false, rel: true, arm: false },
    'check-preservation': { label: '', help: 'Kiểm tra xem phép phân rã có bảo toàn tất cả phụ thuộc hàm trong F hay không.', button: 'Kiểm tra bảo toàn PTH →', query: false, y: false, f2: false, rel: true, arm: false },
    'full-analysis': { label: '', help: 'Phân tích toàn diện một chạm: Khóa, Siêu khóa, Dạng chuẩn, 3NF, BCNF, Chase, Bảo toàn PTH & Cảnh báo.', button: '⚡ Phân tích toàn diện →', query: false, y: false, f2: false, rel: false, arm: false },
    'compare-3nf-bcnf': { label: '', help: 'Đối chiếu song song các tiêu chuẩn giữa phân rã 3NF và phân rã BCNF.', button: 'So sánh 3NF vs BCNF →', query: false, y: false, f2: false, rel: false, arm: false },
    armstrong: { label: 'Tập tiền đề X', help: 'Minh họa và áp dụng hệ tiên đề Armstrong tương tác.', button: 'Áp dụng tiên đề →', query: true, y: true, f2: false, rel: false, arm: true },
    benchmark: { label: '', help: 'Đo lường thời gian thực thi của các thuật toán với số lượng thuộc tính tăng dần (N = 5..20).', button: 'Đo hiệu năng thuật toán →', query: false, y: false, f2: false, rel: false, arm: false }
  }[tool] || { label: '', help: '', button: 'Phân tích →', query: false, y: false, f2: false, rel: false, arm: false };

  if ($('query-fields')) $('query-fields').hidden = !(settings.query || settings.f2 || settings.rel || settings.arm);
  if ($('query-x-field')) $('query-x-field').hidden = !settings.query;
  if ($('y-field')) $('y-field').hidden = !settings.y;
  if ($('f2-field')) $('f2-field').hidden = !settings.f2;
  if ($('relations-field')) $('relations-field').hidden = !settings.rel;
  if ($('armstrong-field')) $('armstrong-field').hidden = !settings.arm;

  if ($('x-label')) $('x-label').textContent = settings.label;
  if ($('query-help')) $('query-help').textContent = settings.help;
  if ($('run')) $('run').textContent = settings.button;
}

function appendStep(step) {
  const li = document.createElement('li');
  li.dataset.step = step.step;
  const strong = document.createElement('strong');
  strong.textContent = `${step.step}. ${step.action}`;
  const p = document.createElement('p');
  p.textContent = step.detail;
  li.append(strong, p);
  if ($('steps')) $('steps').append(li);
}

function renderItems(items) {
  if (!$('result-attributes')) return;
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

  } else if (activeTool === 'superkeys') {
    label = 'TẤT CẢ SIÊU KHÓA (PIE)'; count = `${result.totalCount} siêu khóa`;
    verdict = `Tổng cộng có ${result.totalCount} siêu khóa`;
    items = (result.sampleSuperkeys || []).map(setText);
    const distStr = result.countBySize ? Object.entries(result.countBySize).map(([k, v]) => `Kích thước ${k}: ${v}`).join(' · ') : '';
    note = `Áp dụng Nguyên lý Bù trừ (Inclusion-Exclusion) trên ${result.candidateKeys ? result.candidateKeys.length : 0} khóa ứng viên. ${distStr}`;

  } else if (activeTool === 'minimal-cover') {
    label = 'PHỦ TỐI THIỂU'; count = `${result.minimalCover.length} phụ thuộc`;
    verdict = 'Phủ tối thiểu của F';
    items = result.minimalCover.map(fd => `${setText(fd.left)} → ${setText(fd.right)}`);
    note = 'Mỗi phụ thuộc có vế phải đơn thuộc tính, vế trái tối tiểu và không dư thừa.';

  } else if (activeTool === 'check-nf') {
    label = 'DẠNG CHUẨN'; count = `${result.violations.length} lý do vi phạm`;
    verdict = `Dạng chuẩn cao nhất: ${result.highestNormalForm}`;
    items = result.candidateKeys.map(key => `Khóa: ${setText(key)}`);
    note = `Thuộc tính khóa: ${setText(result.primeAttributes)}. ${result.violations.map(v => `${v.normalForm}: ${v.dependency} — ${v.reason}`).join(' ') || 'Không có vi phạm.'}`;

  } else if (activeTool === 'decompose-3nf') {
    label = 'PHÂN RÃ 3NF'; count = `${result.relations.length} quan hệ con`;
    verdict = 'Các quan hệ sau phân rã 3NF';
    items = result.relations.map((relation, index) => `R${index + 1}(${relation.join(', ')})`);
    note = 'Phân rã được tổng hợp từ phủ tối thiểu; đảm bảo bảo toàn phụ thuộc hàm và nối không mất mát thông tin.';

  } else if (activeTool === 'decompose-bcnf') {
    label = 'PHÂN RÃ BCNF'; count = `${result.relations.length} quan hệ con`;
    verdict = `Phân rã thành công ${result.relations.length} lược đồ con đạt BCNF`;
    items = result.relations.map(r => `${r.name}(${r.attributes.join(', ')}) [Khóa: ${r.candidateKeys.map(k => '{' + k.join(', ') + '}').join(', ')}]`);
    note = `Số phụ thuộc vi phạm đã tách: ${result.violations ? result.violations.length : 0}. Phép phân rã BCNF đảm bảo tính chất Lossless Join.`;

  } else if (activeTool === 'equivalence') {
    label = 'KIỂM TRA TƯƠNG ĐƯƠNG'; count = result.equivalent ? 'TƯƠNG ĐƯƠNG' : 'KHÔNG TƯƠNG ĐƯƠNG';
    verdict = result.equivalent ? 'Hai tập phụ thuộc hàm TƯƠNG ĐƯƠNG: F₁ ≡ F₂' : 'Hai tập phụ thuộc hàm KHÔNG TƯƠNG ĐƯƠNG: F₁ ≢ F₂';
    items = [
      `Chiều 1 (F₁ ⊨ F₂): ${result.f1ImpliesF2 ? '✓ Thỏa mãn' : '✗ Vi phạm'}`,
      `Chiều 2 (F₂ ⊨ F₁): ${result.f2ImpliesF1 ? '✓ Thỏa mãn' : '✗ Vi phạm'}`
    ];
    note = result.equivalent
      ? 'Mọi phụ thuộc trong F2 đều được suy ra từ F1 và mọi phụ thuộc trong F1 đều được suy ra từ F2.'
      : 'Tồn tại ít nhất một phụ thuộc không thể suy diễn qua lại giữa hai tập F1 và F2.';

  } else if (activeTool === 'chase') {
    label = 'THUẬT TOÁN CHASE'; count = result.lossless ? 'LOSSLESS JOIN' : 'LOSSY JOIN';
    verdict = result.lossless ? 'Phép phân rã ĐẢM BẢO NỐI KHÔNG MẤT MÁT THÔNG TIN' : 'Phép phân rã BỊ MẤT MÁT THÔNG TIN (Lossy Join)';
    items = result.lossless
      ? [`Hàng thỏa mãn: R${result.satisfyingRow} đã đạt toàn bộ ký hiệu phân biệt (a)`]
      : ['Bảng Chase dừng cố định nhưng không có hàng nào đạt toàn bộ ký hiệu phân biệt (a)'];
    note = `Bảng Chase gồm ${result.attributes ? result.attributes.length : 0} cột đã được đồng nhất qua các bước lặp phụ thuộc hàm.`;

  } else if (activeTool === 'check-preservation') {
    label = 'BẢO TOÀN PHỤ THUỘC'; count = result.preserved ? 'BẢO TOÀN 100%' : `MẤT ${result.lostDependencies.length} PTH`;
    verdict = result.preserved ? 'Phép phân rã BẢO TOÀN TẤT CẢ PHỤ THUỘC HÀM' : `Phép phân rã KHÔNG BẢO TOÀN PHỤ THUỘC (Mất ${result.lostDependencies.length} PTH)`;
    items = (result.details || []).map(d => `${setText(d.dependency.left)} → ${setText(d.dependency.right)}: ${d.preserved ? '✓ Bảo toàn' : '✗ Bị mất'}`);
    note = result.preserved
      ? 'Mọi ràng buộc nghiệp vụ trong F đều được kiểm tra cục bộ trên các quan hệ con mà không cần join.'
      : `Các phụ thuộc bị mất: ${result.lostDependencies.map(fd => setText(fd.left) + ' → ' + setText(fd.right)).join(', ')}.`;

  } else if (activeTool === 'full-analysis') {
    label = 'PHÂN TÍCH TOÀN DIỆN'; count = `${result.decomposition3nf ? result.decomposition3nf.length : 0} bảng 3NF · ${result.decompositionBcnf ? result.decompositionBcnf.length : 0} bảng BCNF`;
    verdict = `Dạng chuẩn: ${result.highestNormalForm} | Tổng siêu khóa: ${result.totalSuperkeys}`;
    items = [
      `Khóa ứng viên: ${(result.candidateKeys || []).map(setText).join(', ')}`,
      `Phủ tối thiểu: ${result.minimalCover ? result.minimalCover.length : 0} phụ thuộc`,
      `Phân rã 3NF: ${result.decomposition3nf ? result.decomposition3nf.length : 0} bảng (Lossless: ${result.lossless3nf ? '✓' : '✗'}, Bảo toàn: ${result.preserved3nf ? '✓' : '✗'})`,
      `Phân rã BCNF: ${result.decompositionBcnf ? result.decompositionBcnf.length : 0} bảng (Lossless: ${result.losslessBcnf ? '✓' : '✗'}, Bảo toàn: ${result.preservedBcnf ? '✓' : '✗'})`
    ];
    note = result.warnings && result.warnings.length
      ? `⚠️ Cảnh báo: ${result.warnings.join(' | ')}`
      : '✓ Lược đồ chuẩn hóa đạt tính toàn vẹn cao, không phát hiện phụ thuộc tầm thường hay thuộc tính cô lập.';

    if (result.sqlDdl && $('sql-ddl-box') && $('sql-ddl-content')) {
      $('sql-ddl-content').textContent = result.sqlDdl;
      $('sql-ddl-box').style.display = 'block';
    }

  } else if (activeTool === 'compare-3nf-bcnf') {
    label = 'SO SÁNH 3NF VS BCNF'; count = `3NF (${result.relationsCount3nf} bảng) vs BCNF (${result.relationsCountBcnf} bảng)`;
    verdict = 'Đối chiếu toàn diện giữa Phân rã 3NF và Phân rã BCNF';
    items = [
      `Số quan hệ con: 3NF = ${result.relationsCount3nf} | BCNF = ${result.relationsCountBcnf}`,
      `Nối không mất mát: 3NF = ${result.lossless3nf ? '✓ Có' : '✗ Không'} | BCNF = ${result.losslessBcnf ? '✓ Có' : '✗ Không'}`,
      `Bảo toàn phụ thuộc: 3NF = ${result.preserved3nf ? '✓ Có' : '✗ Không'} | BCNF = ${result.preservedBcnf ? '✓ Có' : '✗ Bị mất (' + result.lostDependenciesBcnf.length + ' PTH)'}`
    ];
    note = result.summary;

  } else if (activeTool === 'armstrong') {
    label = 'TIÊN ĐỀ ARMSTRONG'; count = result.valid ? 'HỢP LỆ' : 'KHÔNG THỎA MÃN';
    verdict = result.valid ? `Suy diễn thành công: ${result.derivedDependency}` : 'Không thỏa mãn điều kiện tiên đề';
    items = [result.axiomName, `Quy tắc: ${result.formula}`];
    note = result.valid ? `Phụ thuộc hàm được chứng minh dựa trên ${result.axiomName}.` : 'Vui lòng kiểm tra lại điều kiện tập thuộc tính đầu vào.';

  } else if (activeTool === 'benchmark') {
    label = 'ĐÁNH GIÁ HIỆU NĂNG'; count = `${result.cacDiemThuNghiem ? result.cacDiemThuNghiem.length : 0} mốc thử nghiệm`;
    verdict = 'Thời gian thực thi (ms) khi số thuộc tính tăng (N = 5..20)';
    items = (result.cacDiemThuNghiem || []).map(p => `N=${p.soThuocTinh} attrs: Bao đóng=${p.baoDongMs.toFixed(2)}ms, Khóa=${p.timKhoaMs.toFixed(2)}ms, 3NF=${p.phanRa3nfMs.toFixed(2)}ms, BCNF=${p.phanRaBcnfMs.toFixed(2)}ms, Chase=${p.chaseMs.toFixed(2)}ms`);
    note = result.nhanXetDoPhucTap;
  }

  if ($('result-label')) $('result-label').textContent = label;
  if ($('result-count')) $('result-count').textContent = count;
  if ($('result-verdict')) $('result-verdict').textContent = verdict;
  if ($('closure-label')) $('closure-label').textContent = activeTool === 'closure' || activeTool === 'implication' ? 'Thuộc tính kết quả' : 'Kết quả phân tích';
  renderItems(items);
  if ($('result-note')) $('result-note').textContent = note;

  if ($('steps')) {
    $('steps').replaceChildren();
    (result.steps || result.allSteps || []).forEach(appendStep);
  }
  if ($('steps-details')) $('steps-details').open = true;
  if ($('empty-result')) $('empty-result').hidden = true;
  if ($('result')) $('result').hidden = false;
  if ($('data-status')) $('data-status').textContent = 'Dữ liệu hợp lệ · Đã phân tích từ backend';

  resultText = `${verdict}\n${items.join('\n')}\n${note}\n\n${(result.steps || result.allSteps || []).map(s => `${s.step}. ${s.action}: ${s.detail}`).join('\n')}`;
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
  if ($('data-status')) $('data-status').textContent = 'Đang gửi yêu cầu đến backend…';
  clearResult();

  try {
    let body = {};

    if (activeTool === 'benchmark') {
      body = {};
    } else if (activeTool === 'armstrong') {
      const xText = $('query-x') ? $('query-x').value.trim() : '';
      const yText = $('query-y') ? $('query-y').value.trim() : '';
      const zText = $('armstrong-z') ? $('armstrong-z').value.trim() : '';
      const wText = $('armstrong-w') ? $('armstrong-w').value.trim() : '';
      body = {
        axiomType: $('axiom-select') ? $('axiom-select').value : 'reflexivity',
        x: xText ? xText.split(',').map(s => s.trim()) : [],
        y: yText ? yText.split(',').map(s => s.trim()) : [],
        z: zText ? zText.split(',').map(s => s.trim()) : [],
        w: wText ? wText.split(',').map(s => s.trim()) : []
      };
    } else {
      const { universe, dependencies } = Relatio.parse($('attributes').value, $('dependencies').value);
      body = { universe, dependencies };

      if (activeTool === 'closure' || activeTool === 'implication') {
        body.x = Relatio.attributes($('query-x').value, 'X', true);
        Relatio.checkSubset(body.x, universe, 'X');
      }
      if (activeTool === 'implication') {
        body.y = Relatio.attributes($('query-y').value, 'Y', true);
        Relatio.checkSubset(body.y, universe, 'Y');
      }
      if (activeTool === 'equivalence') {
        const f2Text = $('dependencies2') ? $('dependencies2').value : '';
        if (!f2Text.trim()) throw new Error('Vui lòng nhập tập phụ thuộc hàm F2 để kiểm tra tương đương.');
        body.dependencies2 = Relatio.parseDependenciesOnly(f2Text, universe);
      }
      if (activeTool === 'chase' || activeTool === 'check-preservation') {
        const relText = $('custom-relations') ? $('custom-relations').value.trim() : '';
        if (relText) {
          // Parse e.g. (A, B), (B, C) or A, B ; B, C
          body.relations = relText.split(/;|\)\s*,\s*\(|\)\s*\(/).map(chunk => {
            const clean = chunk.replace(/[()]/g, '').trim();
            return clean ? clean.split(',').map(s => s.trim()).filter(Boolean) : [];
          }).filter(arr => arr.length > 0);
        }
      }
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
      const u = body.universe || [];
      renderAnalysis(result, u, activeTool);
      saveHistory(u, body.dependencies || [], activeTool, resultText);
    }
  } catch (error) {
    if (requestVersion === analysisRequestVersion) showError(error);
  } finally {
    runButton.disabled = false;
    runButton.textContent = originalButtonText;
    toolButtons.forEach(button => { button.disabled = false; });
  }
}

async function generateSqlDdl() {
  const u = $('attributes') ? $('attributes').value.trim() : '';
  const f = $('dependencies') ? $('dependencies').value.trim() : '';
  if (!u || !f) {
    toast('Vui lòng nhập tập thuộc tính U và phụ thuộc hàm F trước!');
    return;
  }
  const btn = $('btn-generate-ddl');
  if (btn) btn.disabled = true;

  try {
    const { universe, dependencies } = Relatio.parse(u, f);
    const res = await fetch('/api/algorithms/generate-ddl', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ universe, dependencies })
    });
    if (!res.ok) throw new Error('Không thể sinh mã SQL');
    const data = await res.json();

    if ($('sql-ddl-box') && $('sql-ddl-content')) {
      $('sql-ddl-content').textContent = data.sqlDdl;
      $('sql-ddl-box').style.display = 'block';
      $('empty-result').hidden = true;
      $('result').hidden = false;
      $('sql-ddl-box').scrollIntoView({ behavior: 'smooth', block: 'nearest' });
      toast('Đã sinh mã SQL DDL thành công từ cấu trúc chuẩn hóa!');
    }
  } catch (err) {
    showError(err);
  } finally {
    if (btn) btn.disabled = false;
  }
}

function download(text, filename) {
  const url = URL.createObjectURL(new Blob([text], { type: 'text/plain;charset=utf-8' }));
  const a = document.createElement('a'); a.href = url; a.download = filename; document.body.append(a); a.click(); a.remove(); setTimeout(() => URL.revokeObjectURL(url), 1000);
}

// Bắt sự kiện
document.querySelectorAll('[data-page]').forEach(button => button.addEventListener('click', () => showPage(button.dataset.page)));
document.querySelectorAll('[data-tool]').forEach(button => button.addEventListener('click', () => selectTool(button.dataset.tool)));

function openSampleChooser() {
  const chooser = $('sample-chooser');
  if (!chooser) return;
  chooser.hidden = false;
  document.body.classList.add('chooser-open');
  const closeButton = chooser.querySelector('.sample-chooser-close');
  if (closeButton) closeButton.focus();
}

function closeSampleChooser() {
  const chooser = $('sample-chooser');
  if (!chooser) return;
  chooser.hidden = true;
  document.body.classList.remove('chooser-open');
  if ($('load-sample')) $('load-sample').focus();
}

if ($('load-sample')) $('load-sample').addEventListener('click', openSampleChooser);
document.querySelectorAll('[data-close-sample-chooser]').forEach(element => {
  element.addEventListener('click', closeSampleChooser);
});
document.querySelectorAll('[data-sample-choice]').forEach(button => {
  button.addEventListener('click', () => {
    const choice = button.dataset.sampleChoice;
    closeSampleChooser();
    if (choice === 'full') loadSample();
    else loadQuickDataset(choice);
  });
});
document.addEventListener('keydown', event => {
  if (event.key === 'Escape' && $('sample-chooser') && !$('sample-chooser').hidden) {
    closeSampleChooser();
  }
});
if ($('use-dataset')) $('use-dataset').addEventListener('click', loadSample);
document.querySelectorAll('.quick-dataset-load').forEach(button => {
  button.addEventListener('click', () => loadQuickDataset(button.dataset.dataset));
});
if ($('btn-generate-ddl')) $('btn-generate-ddl').addEventListener('click', generateSqlDdl);

if ($('copy-sql-btn')) {
  $('copy-sql-btn').addEventListener('click', async () => {
    const content = $('sql-ddl-content') ? $('sql-ddl-content').textContent : '';
    try {
      await navigator.clipboard.writeText(content);
      toast('Đã sao chép mã nguồn SQL DDL vào bộ nhớ tạm.');
    } catch {
      download(content, 'schema_ddl.sql');
      toast('Đã tải mã SQL DDL thành file .sql');
    }
  });
}

if ($('reset')) {
  $('reset').addEventListener('click', () => {
    analysisRequestVersion++;
    saveUndo();
    fieldIds.forEach(id => { if ($(id)) $(id).value = ''; });
    clearResult();
    updateCounts();
    if ($('attributes')) $('attributes').focus();
  });
}

if ($('undo')) {
  $('undo').addEventListener('click', () => {
    if (!undoState) return;
    undoState.values.forEach((value, index) => { if ($(fieldIds[index])) $(fieldIds[index]).value = value; });
    selectTool(undoState.tool);
    undoState = null;
    if ($('undo-bar')) $('undo-bar').hidden = true;
    updateCounts();
    if ($('attributes')) $('attributes').focus();
    toast('Đã khôi phục dữ liệu.');
  });
}

fieldIds.forEach(id => {
  const el = $(id);
  if (el) el.addEventListener('input', () => { analysisRequestVersion++; clearResult(); updateCounts(); });
});

['query-x', 'query-y'].forEach(id => {
  const el = $(id);
  if (el) el.addEventListener('keydown', event => { if (event.key === 'Enter') run(); });
});

if ($('run')) $('run').addEventListener('click', run);
if ($('import')) $('import').addEventListener('click', () => $('file-input').click());

if ($('file-input')) {
  $('file-input').addEventListener('change', async event => {
    const file = event.target.files[0];
    if (!file) return;
    analysisRequestVersion++;
    try {
      if (file.size > 1024 * 1024) throw new Error('Chọn file văn bản nhỏ hơn 1 MB.');
      const parsed = Relatio.parseFile(await file.text());
      saveUndo();
      if ($('attributes')) $('attributes').value = parsed.u;
      if ($('dependencies')) $('dependencies').value = parsed.f;
      if ($('query-x')) $('query-x').value = '';
      if ($('query-y')) $('query-y').value = '';
      clearResult();
      updateCounts();
      toast(`Đã nhập ${file.name}.`);
    } catch (error) { showError(error, true); }
    finally { event.target.value = ''; }
  });
}

if ($('export')) {
  $('export').addEventListener('click', () => {
    try {
      const { universe, dependencies } = Relatio.parse($('attributes').value, $('dependencies').value);
      download(`U: ${universe.join(', ')}\n\nF:\n${dependencies.map(fd => `${fd.left.join(', ') || '∅'} -> ${fd.right.join(', ') || '∅'}`).join('\n')}\n`, 'U_F.txt');
      toast('Đã xuất bộ U, F hiện tại.');
    } catch (error) { showError(error); }
  });
}

if ($('copy-result')) {
  $('copy-result').addEventListener('click', async () => {
    try { await navigator.clipboard.writeText(resultText); toast('Đã sao chép kết quả.'); }
    catch { download(resultText, 'ket_qua.txt'); toast('Đã tải kết quả thành file.'); }
  });
}

// Dark Mode Toggle
if ($('theme-toggle')) {
  // Load saved theme
  const savedTheme = localStorage.getItem('theme');
  if (savedTheme === 'dark') {
    document.body.classList.add('dark-mode');
    $('theme-toggle').textContent = '☀️ Chế độ sáng';
  }

  $('theme-toggle').addEventListener('click', () => {
    const isDark = document.body.classList.toggle('dark-mode');
    localStorage.setItem('theme', isDark ? 'dark' : 'light');
    $('theme-toggle').textContent = isDark ? '☀️ Chế độ sáng' : '🌙 Chế độ tối';
    toast(isDark ? 'Đã bật giao diện tối.' : 'Đã bật giao diện sáng.');
  });
}

function saveHistory(universe, dependencies, activeTool, detail) {
  const toolNames = {
    closure: 'TINH_BAO_DONG',
    implication: 'KIEM_TRA_SUY_DIEN',
    keys: 'TIM_TAT_CA_KHOA',
    superkeys: 'TIM_SIEU_KHOA_PIE',
    'minimal-cover': 'PHU_TOI_TIEU',
    'check-nf': 'KIEM_TRA_DANG_CHUAN',
    'decompose-3nf': 'PHAN_RA_3NF',
    'decompose-bcnf': 'PHAN_RA_BCNF',
    equivalence: 'KIEM_TRA_TUONG_DUONG',
    chase: 'THUAT_TOAN_CHASE',
    'check-preservation': 'BAO_TOAN_PHU_THUOC',
    'full-analysis': 'PHAN_TICH_TONG_HOP',
    'compare-3nf-bcnf': 'SO_SANH_3NF_BCNF',
    armstrong: 'TIEN_DE_ARMSTRONG',
    benchmark: 'DANH_GIA_HIEU_NANG'
  };

  const extraInput = activeTool === 'closure' ? `{${$('query-x').value.trim()}}⁺`
    : (activeTool === 'implication' ? `${$('query-x').value.trim()} → ${$('query-y').value.trim()}`
      : (activeTool === 'equivalence' ? `F2: ${($('dependencies2') ? $('dependencies2').value : '').split('\n').length} PTH`
        : (activeTool === 'armstrong' ? $('axiom-select').value : '')));

  fetch('/api/history', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      tapThuocTinhU: universe.join(', '),
      tapPhuThuocHamF: (dependencies || []).map(fd => `${fd.left.join(', ')} -> ${fd.right.join(', ')}`).join('\n'),
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
  const search = $('history-search');
  const filter = $('history-filter');
  const count = $('history-count');
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
      if (count) count.textContent = '0 bản ghi';
      return;
    }
    const escapeHtml = value => String(value ?? '')
      .replaceAll('&', '&amp;').replaceAll('<', '&lt;').replaceAll('>', '&gt;')
      .replaceAll('"', '&quot;').replaceAll("'", '&#039;');
    const summarize = text => {
      const clean = String(text || '').replace(/\s+/g, ' ').trim();
      return clean.length > 150 ? `${clean.slice(0, 147)}...` : clean;
    };
    const algorithmLabel = value => {
      const labels = {
        closure: 'Bao đóng', implication: 'Suy diễn', keys: 'Tìm khóa',
        superkeys: 'Siêu khóa', 'minimal-cover': 'Phủ tối thiểu', 'check-nf': 'Dạng chuẩn',
        'decompose-3nf': 'Phân rã 3NF', 'decompose-bcnf': 'Phân rã BCNF',
        equivalence: 'Tương đương', chase: 'Chase', 'check-preservation': 'Bảo toàn PTH',
        'full-analysis': 'Phân tích tổng hợp', 'compare-3nf-bcnf': 'So sánh 3NF / BCNF',
        armstrong: 'Tiên đề Armstrong', benchmark: 'Đo hiệu năng',
        TINH_BAO_DONG: 'Bao đóng', KIEM_TRA_SUY_DIEN: 'Suy diễn',
        TIM_KHOA: 'Tìm khóa', TIM_SIEU_KHOA: 'Siêu khóa',
        TIM_PHU_TOI_THIEU: 'Phủ tối thiểu', KIEM_TRA_DANG_CHUAN: 'Dạng chuẩn',
        PHAN_RA_3NF: 'Phân rã 3NF', PHAN_RA_BCNF: 'Phân rã BCNF',
        KIEM_TRA_TUONG_DUONG: 'Tương đương', KIEM_TRA_CHASE: 'Chase',
        KIEM_TRA_BAO_TOAN_PTH: 'Bảo toàn phụ thuộc',
        PHAN_TICH_TONG_HOP: 'Phân tích tổng hợp',
        SO_SANH_3NF_BCNF: 'So sánh 3NF / BCNF',
        TIEN_DE_ARMSTRONG: 'Tiên đề Armstrong', DANH_GIA_HIEU_NANG: 'Đo hiệu năng'
      };
      return labels[value] || value || 'Phân tích';
    };

    if (filter) {
      const current = filter.value;
      const algorithms = [...new Set(data.map(item => item.loaiThuatToan).filter(Boolean))]
        .sort((a, b) => algorithmLabel(a).localeCompare(algorithmLabel(b), 'vi'));
      filter.replaceChildren(new Option('Tất cả thuật toán', ''));
      algorithms.forEach(value => filter.add(new Option(algorithmLabel(value), value)));
      if (algorithms.includes(current)) filter.value = current;
    }

    const render = () => {
      container.replaceChildren();
      const query = (search?.value || '').trim().toLowerCase();
      const selected = filter?.value || '';
      const filtered = data.filter(item => {
        const haystack = `${item.loaiThuatToan || ''} ${item.ketQuaChiTiet || ''} ${item.tapThuocTinhU || ''} ${item.tapPhuThuocHamF || ''}`.toLowerCase();
        return (!selected || item.loaiThuatToan === selected) && (!query || haystack.includes(query));
      });
      if (count) count.textContent = `${filtered.length}/${data.length} bản ghi`;
      if (filtered.length === 0) {
        const noMatch = document.createElement('div');
        noMatch.className = 'history-no-match';
        noMatch.textContent = 'Không tìm thấy bản ghi phù hợp.';
        container.append(noMatch);
        return;
      }
      filtered.forEach(item => {
        const algorithm = escapeHtml(algorithmLabel(item.loaiThuatToan));
        const rawAlgorithm = item.loaiThuatToan || '';
        const result = summarize(item.ketQuaChiTiet);
        const card = document.createElement('details');
        card.className = 'history-card history-card-row';
        card.innerHTML = `
          <summary>
            <div class="history-summary-main">
              <span class="history-algorithm-icon">${escapeHtml(rawAlgorithm === 'closure' ? 'X⁺' : rawAlgorithm === 'keys' ? 'K' : rawAlgorithm === 'check-nf' ? 'NF' : '◆')}</span>
              <div class="history-summary-copy">
                <strong>${algorithm}</strong>
                <span>${escapeHtml(result || 'Không có tóm tắt kết quả')}</span>
              </div>
            </div>
            <div class="history-summary-meta">
              <span class="badge-duration">${Number(item.thoiGianThucThiMs || 0)} ms</span>
              <span class="badge-time">${escapeHtml(item.thoiGianChay || '')}</span>
              <span class="history-expand" aria-hidden="true">Chi tiết <b>›</b></span>
            </div>
          </summary>
          <div class="history-card-body">
            <div class="history-meta">
              <div class="history-meta-item">
                <strong>Tập thuộc tính U</strong>
                <span>${escapeHtml(item.tapThuocTinhU || '∅')}</span>
              </div>
              <div class="history-meta-item">
                <strong>Phụ thuộc hàm F</strong>
                <span>${escapeHtml(item.tapPhuThuocHamF || '∅')}</span>
              </div>
              ${item.dauVaoBoSung ? `
              <div class="history-meta-item">
                <strong>Đầu vào phụ</strong>
                <span>${escapeHtml(item.dauVaoBoSung)}</span>
              </div>` : ''}
              <div class="history-meta-item">
                <strong>Mã bản ghi</strong>
                <span>#${escapeHtml(item.id)}</span>
              </div>
            </div>
            <div class="history-result-box">${escapeHtml(item.ketQuaChiTiet || 'Không có chi tiết kết quả')}</div>
          </div>
        `;
        container.append(card);
      });
    };
    if (search && !search.dataset.historyBound) {
      search.addEventListener('input', render);
      search.dataset.historyBound = 'true';
    }
    if (filter && !filter.dataset.historyBound) {
      filter.addEventListener('change', render);
      filter.dataset.historyBound = 'true';
    }
    render();
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
      toast("Backend chưa nạp API mới (HTTP " + resSv.status + "). Bạn vui lòng khởi động lại Spring Boot nhé!");
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
      toast("Lỗi HTTP " + res.status + ": Backend chưa nạp Controller. Hãy khởi động lại Spring Boot!");
      return;
    }
    const data = await res.json();
    renderSimulationResult(data);

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
      <p style="margin:6px 0 10px;font-size:13px;line-height:1.6;">${data.message}</p>
      <div style="background:#fff;padding:12px;border-radius:6px;font-family:var(--mono);font-size:11px;line-height:1.7;margin-bottom:10px;">
        <strong style="display:block;margin-bottom:4px;color:#526958;">QUÁ TRÌNH THỰC THI GIAO DỊCH (TRANSACTION TRACE):</strong>
        ${stepsHtml}
      </div>
      <div style="font-size:12px;line-height:1.6;opacity:.92;">
        <strong>Phân tích tính chất ACID:</strong> ${data.explanation || ''}
      </div>
    </div>
  `;
  container.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
}

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

// Khởi động trang mặc định
showPage('workspace');
updateCounts();
selectTool('closure');
