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
test('UI exposes fifteen unique tools with expected IDs and normalized labels', () => {
  const html = fs.readFileSync(path.join(__dirname, '../src/main/resources/static/index.html'), 'utf8');
  const expectedIds = [
    'closure', 'implication', 'keys', 'superkeys', 'minimal-cover', 'check-nf',
    'decompose-3nf', 'decompose-bcnf', 'equivalence', 'chase', 'check-preservation',
    'full-analysis', 'compare-3nf-bcnf', 'armstrong', 'benchmark'
  ];
  const htmlToolIds = [...html.matchAll(/data-tool="([^"]+)"/g)].map(match => match[1]);
  assert.equal(new Set(htmlToolIds).size, 15);
  assert.deepEqual(htmlToolIds, expectedIds);
  for (const [, label] of html.matchAll(/data-tool="[^"]+"[^>]*>[\s\S]*?<span>([^<]+)<small>/g)) {
    const trimmed = label.trim();
    assert.ok(trimmed === trimmed.normalize('NFC') && !/[\u200B-\u200D\uFEFF]|\s{2,}/u.test(trimmed));
  }
});
test('Core module exports pure input/closure helpers without network calls', () => {
  assert.deepEqual(
    Object.keys(core).sort(),
    ['attributes', 'checkSubset', 'closure', 'parse', 'parseDependenciesOnly', 'parseFile']
  );
  const coreSrc = fs.readFileSync(path.join(__dirname, '../src/main/resources/static/core.js'), 'utf8');
  assert.doesNotMatch(coreSrc, /fetch\s*\(|XMLHttpRequest|axios/i);
});
test('HTML IDs are unique', () => {
  const html = fs.readFileSync(path.join(__dirname, '../src/main/resources/static/index.html'), 'utf8');
  const ids = [...html.matchAll(/\sid="([^"]+)"/g)].map(match => match[1]);
  assert.equal(new Set(ids).size, ids.length);
});
test('App wires analysis, history and simulation UI to backend REST APIs', () => {
  const app = fs.readFileSync(path.join(__dirname, '../src/main/resources/static/app.js'), 'utf8');
  assert.match(app, /fetch\(`\/api\/algorithms\/\$\{activeTool\}`/);
  assert.match(app, /fetch\('\/api\/algorithms\/generate-ddl'/);
  assert.match(app, /fetch\('\/api\/history'/);
  assert.match(app, /fetch\('\/api\/simulation\/register'/);
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
test('UI sends every analysis tool to the backend without local algorithm fallback', () => {
  const app = fs.readFileSync(path.join(__dirname, '../src/main/resources/static/app.js'), 'utf8');
  const html = fs.readFileSync(path.join(__dirname, '../src/main/resources/static/index.html'), 'utf8');
  for (const tool of ['closure', 'implication', 'keys', 'minimal-cover', 'check-nf', 'decompose-3nf']) {
    assert.ok(html.includes(`data-tool="${tool}"`));
  }
  assert.ok(app.includes('fetch(`/api/algorithms/${activeTool}`'));
  assert.ok(!app.includes('Relatio.closure('));
});
