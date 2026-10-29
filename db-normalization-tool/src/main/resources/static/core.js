/* Pure input helpers; production analysis is performed by the backend REST API. */
(function (root) {
  'use strict';
  function attributes(text, label = 'Tập thuộc tính', allowEmpty = false) {
    if (text.trim() === '∅' && allowEmpty) return [];
    if (!text.trim()) throw new Error(`${label}: hãy nhập thuộc tính${allowEmpty ? ' hoặc ∅ cho tập rỗng' : ''}.`);
    const values = text.split(',').map(value => value.trim());
    for (const value of values) {
      if (!/^[A-Za-z_][A-Za-z0-9_]*$/.test(value)) throw new Error(`${label}: tên “${value}” không hợp lệ. Dùng chữ không dấu, số, gạch dưới; bắt đầu bằng chữ hoặc gạch dưới.`);
    }
    if (new Set(values).size !== values.length) throw new Error(`${label}: có thuộc tính bị lặp.`);
    return values;
  }
  function checkSubset(values, universe, label) {
    const missing = values.filter(value => !universe.includes(value));
    if (missing.length) throw new Error(`${label}: ${missing.join(', ')} không thuộc U (có phân biệt hoa thường).`);
  }
  function parse(u, f) {
    const universe = attributes(u, 'U');
    const dependencies = [];
    f.split(/\r?\n/).forEach((raw, index) => {
      const line = raw.trim();
      if (!line || line.startsWith('#')) return;
      const parts = line.split(/->|→/);
      if (parts.length !== 2) throw new Error(`F, dòng ${index + 1}: cần đúng một dấu -> giữa hai vế.`);
      const left = attributes(parts[0], `F, dòng ${index + 1}, vế trái`, true);
      const right = attributes(parts[1], `F, dòng ${index + 1}, vế phải`, true);
      checkSubset([...left, ...right], universe, `F, dòng ${index + 1}`);
      dependencies.push({ left, right });
    });
    return { universe, dependencies };
  }
  function parseFile(text) {
    let u = null, inF = false, foundF = false;
    const lines = [];
    for (const raw of text.replace(/^\uFEFF/, '').split(/\r?\n/)) {
      const line = raw.trim();
      if (!line || line.startsWith('#')) continue;
      if (line.startsWith('U:')) {
        if (u !== null || inF) throw new Error('File cần một dòng U: nằm trước F:.');
        u = line.slice(2).trim();
      } else if (line === 'F:') {
        if (u === null || foundF) throw new Error('File cần một dòng F: sau U:.');
        inF = true; foundF = true;
      } else if (inF) lines.push(line);
      else throw new Error('File cần định dạng U: ... và F: trên dòng riêng.');
    }
    if (u === null || !foundF) throw new Error('File thiếu dòng U: hoặc F:.');
    const f = lines.join('\n');
    return { u, f, ...parse(u, f) };
  }
  function closure(x, dependencies) {
    const known = new Set(x), steps = [];
    let changed;
    do {
      changed = false;
      dependencies.forEach((fd, index) => {
        if (!fd.left.every(value => known.has(value))) return;
        const added = fd.right.filter(value => !known.has(value));
        if (!added.length) return;
        added.forEach(value => known.add(value));
        steps.push({ dependencyIndex: index, left: [...fd.left], right: [...fd.right], added, closure: [...known] });
        changed = true;
      });
    } while (changed);
    return { closure: [...known], steps };
  }
  const api = { attributes, checkSubset, parse, parseFile, closure };
  if (typeof module !== 'undefined' && module.exports) module.exports = api;
  else root.Relatio = api;
})(typeof globalThis !== 'undefined' ? globalThis : this);
