const { test } = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const core = require('../src/main/resources/static/core.js');
const data = core.parseFile(fs.readFileSync(path.join(__dirname, '../src/main/resources/static/U_F.txt'), 'utf8'));
const split = text => text.split(',').map(s => s.trim());
const closureCases = [
  ['MaSV', 'MaSV,TenSV,NgaySinh,MaNganh,TenNganh,MaKhoa,TenKhoa'],
  ['MaNganh', 'MaNganh,TenNganh,MaKhoa,TenKhoa'],
  ['MaGV', 'MaGV,TenGV,HocVi'],
  ['MaLHP', 'MaLHP,MaHP,TenHP,SoTC,HocKy,Thu,TietBD,MaGV,TenGV,HocVi,MaPhong,ToaNha,SucChua'],
  ['MaPhong,HocKy,Thu,TietBD', 'MaPhong,HocKy,Thu,TietBD,MaLHP,MaHP,TenHP,SoTC,MaGV,TenGV,HocVi,ToaNha,SucChua'],
  ['MaSV,MaLHP', data.universe.join(',')],
  ['MaSV,MaHP,HocKy', 'MaSV,MaHP,HocKy,TenSV,NgaySinh,MaNganh,TenNganh,MaKhoa,TenKhoa,TenHP,SoTC']
];
test('Official file: 21 attributes and 10 dependencies, optional FD excluded', () => {
  assert.equal(data.universe.length, 21); assert.equal(data.dependencies.length, 10);
});
test('UI exposes eight unique tools with expected labels, fields and execution modes', () => {
  const html = fs.readFileSync(path.join(__dirname, '../src/main/resources/static/index.html'), 'utf8');
  const app = fs.readFileSync(path.join(__dirname, '../src/main/resources/static/app.js'), 'utf8');
  assert.deepEqual(core.tools.map(tool => tool.id), ['closure', 'implication', 'keys', 'superkeys', 'minimal-cover', 'normal-form', 'decompose-3nf', 'decompose-bcnf']);
  assert.deepEqual(core.tools.map(tool => tool.name), ['Bao đóng', 'Suy diễn', 'Tất cả khóa', 'Tất cả siêu khóa', 'Phủ tối thiểu', 'Dạng chuẩn', 'Phân rã 3NF', 'Phân rã BCNF']);
  assert.ok(core.tools.every(tool => tool.name === tool.name.normalize('NFC') && !/[\u200B-\u200D\uFEFF]|\s{2,}/u.test(tool.name)));
  assert.deepEqual(core.tools.map(tool => tool.buttonLabel), ['Tính bao đóng', 'Kiểm tra suy diễn', 'Tìm tất cả khóa', 'Tìm tất cả siêu khóa', 'Tìm phủ tối thiểu', 'Kiểm tra dạng chuẩn', 'Phân rã 3NF', 'Phân rã BCNF']);
  assert.deepEqual(core.tools.map(tool => [tool.needsX, tool.needsY]), [[true, false], [true, true], [false, false], [false, false], [false, false], [false, false], [false, false], [false, false]]);
  assert.deepEqual(core.tools.map(tool => tool.execution), ['local', 'local', 'backend', 'backend', 'backend', 'backend', 'backend', 'backend']);
  const configuredIds = core.tools.map(tool => tool.id);
  const htmlToolIds = [...html.matchAll(/data-tool="([^"]+)"/g)].map(match => match[1]);
  assert.equal(new Set(configuredIds).size, 8);
  assert.equal(new Set(htmlToolIds).size, 8);
  assert.deepEqual(htmlToolIds, configuredIds);
  assert.match(app, /query-fields'\)\.hidden = !config\.needsX && !config\.needsY/);
  assert.match(app, /run'\)\.textContent = `\$\{config\.buttonLabel\}/);
});
test('Superkeys and BCNF decomposition are backend-only and have empty prepared result regions', () => {
  const html = fs.readFileSync(path.join(__dirname, '../src/main/resources/static/index.html'), 'utf8');
  for (const id of ['superkeys', 'decompose-bcnf']) {
    const tool = core.toolById(id);
    assert.deepEqual([tool.needsX, tool.needsY, tool.execution, tool.localHandler], [false, false, 'backend', undefined]);
  }
  assert.equal(core.toolById('superkeys').description, 'Liệt kê các tập thuộc tính xác định U');
  assert.equal(core.toolById('decompose-bcnf').description, 'Phân rã theo phụ thuộc vi phạm BCNF');
  for (const id of ['superkeys-result', 'superkeys-count', 'superkeys-list', 'superkeys-candidate-legend', 'superkeys-warning', 'superkeys-evidence', 'bcnf-result', 'bcnf-relations', 'bcnf-violations', 'bcnf-lossless-result', 'bcnf-steps']) {
    assert.match(html, new RegExp(`id="${id}"`));
  }
  assert.match(html, /id="superkeys-list" class="result-list"><\/div>/);
  assert.match(html, /id="bcnf-relations" class="result-list"><\/div>/);
});
test('HTML IDs are unique', () => {
  const html = fs.readFileSync(path.join(__dirname, '../src/main/resources/static/index.html'), 'utf8');
  const ids = [...html.matchAll(/\sid="([^"]+)"/g)].map(match => match[1]);
  assert.equal(new Set(ids).size, ids.length);
});
test('Tools waiting for backend do not create local results or call an API', () => {
  for (const tool of core.tools.filter(tool => tool.execution === 'backend')) assert.equal(tool.localHandler, undefined);
  const app = fs.readFileSync(path.join(__dirname, '../src/main/resources/static/app.js'), 'utf8');
  const html = fs.readFileSync(path.join(__dirname, '../src/main/resources/static/index.html'), 'utf8');
  assert.match(html, /Chức năng này đang chờ API từ backend\./);
  assert.match(app, /config\.execution === 'backend'/);
  assert.doesNotMatch(app, /fetch\s*\(|XMLHttpRequest|axios/i);
});
for (const [x, expected] of closureCases) test(`Expected closure: ${x}`, () => {
  const result = core.closure(split(x), data.dependencies);
  assert.deepEqual(new Set(result.closure), new Set(split(expected)));
  let current = new Set(split(x));
  for (const step of result.steps) {
    assert.ok(step.left.every(a => current.has(a)));
    assert.ok(step.added.every(a => !current.has(a)));
    step.added.forEach(a => current.add(a));
    assert.deepEqual(current, new Set(step.closure));
  }
});
for (const [x, y, expected] of [
  ['MaSV', 'TenSV', true], ['MaSV', 'TenKhoa', true], ['MaLHP', 'ToaNha', true],
  ['MaSV,MaLHP', 'TenGV,SoTC,TenKhoa,Diem', true], ['MaSV', 'TenHP', false],
  ['MaLHP', 'Diem', false], ['MaKhoa', 'TenNganh', false]
]) test(`Expected implication: ${x} -> ${y}`, () => {
  assert.equal(split(y).every(a => core.closure(split(x), data.dependencies).closure.includes(a)), expected);
});
test('Invalid inputs are rejected', () => {
  for (const [u, f] of [['A,A', ''], ['A,B', 'A -> C'], ['A,B', 'A -> B -> A'], ['A,,B', ''], ['A,B', 'a -> B'], ['A B', '']]) assert.throws(() => core.parse(u, f));
  for (const text of ['F:\nA -> B', 'U: A\nF:\nF:', 'U: A\nU: B\nF:']) assert.throws(() => core.parseFile(text));
});
test('Empty F, empty sets, Unicode arrow, BOM, CRLF and cycles', () => {
  assert.deepEqual(core.closure(['A'], core.parse('A', '').dependencies).closure, ['A']);
  const parsed = core.parseFile('\uFEFF# comment\r\nU: A, B\r\nF:\r\n∅ → A\r\nA -> B\r\nB -> A');
  assert.deepEqual(new Set(core.closure([], parsed.dependencies).closure), new Set(['A', 'B']));
});
test('UI sample stays identical to original file', () => {
  const script = fs.readFileSync(path.join(__dirname, '../src/main/resources/static/app.js'), 'utf8');
  const u = script.match(/const sampleU = '([^']+)'/)[1];
  const f = script.match(/const sampleF = `([^`]+)`/)[1];
  assert.deepEqual(core.parse(u, f), { universe: data.universe, dependencies: data.dependencies });
});
