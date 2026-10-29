# Algorithm REST API

The Spring Boot application exposes JSON endpoints under `/api/algorithms`.
All requests use `Content-Type: application/json`. Attribute names are
case-sensitive strings, and functional dependencies use `left` and `right`
arrays. Empty `left` is allowed for dependencies such as `∅ -> A`; `right`
must contain at least one attribute.

## Shared request

```json
{
  "universe": ["MaSV", "TenSV", "MaLHP", "Diem"],
  "dependencies": [
    {"left": ["MaSV"], "right": ["TenSV"]},
    {"left": ["MaSV", "MaLHP"], "right": ["Diem"]}
  ]
}
```

Every attribute in a dependency must occur in `universe`. Duplicate or
malformed names are rejected. `atomicValues` is optional and defaults to
`true`; set it to `false` when the values are not atomic to report that the
relation does not satisfy 1NF.

## Endpoints

| Method | Path | Purpose |
|---|---|---|
| POST | `/api/algorithms/closure` | Requires `x`; returns `closure` and solution `steps`. |
| POST | `/api/algorithms/implication` | Requires `x` and `y`; returns `closure`, `implied`, and `steps`. |
| POST | `/api/algorithms/keys` | Returns all candidate `keys`, attribute categories, and `steps`. |
| POST | `/api/algorithms/minimal-cover` | Returns `minimalCover` and reduction `steps`. |
| POST | `/api/algorithms/check-nf` | Returns `highestNormalForm`, candidate keys, prime attributes, violations, and `steps`. |
| POST | `/api/algorithms/decompose-3nf` | Returns synthesized `relations`, candidate keys, and `steps`. |

Closure and implication request example:

```json
{
  "universe": ["MaSV", "TenSV", "MaLHP", "Diem"],
  "dependencies": [
    {"left": ["MaSV"], "right": ["TenSV"]},
    {"left": ["MaSV", "MaLHP"], "right": ["Diem"]}
  ],
  "x": ["MaSV"],
  "y": ["TenSV"]
}
```

The response for `/implication` has this shape:

```json
{
  "closure": ["MaSV", "TenSV"],
  "implied": true,
  "steps": [
    {
      "step": 1,
      "action": "Áp dụng phụ thuộc hàm",
      "detail": "Vòng 1: MaSV -> TenSV có vế trái nằm trong bao đóng; thêm [TenSV], bao đóng hiện tại là [MaSV, TenSV]."
    },
    {
      "step": 2,
      "action": "Đạt điểm cố định",
      "detail": "Không còn phụ thuộc hàm nào thêm được thuộc tính mới. Bao đóng cuối cùng: [MaSV, TenSV]."
    }
  ]
}
```

For `/keys`, a response includes `keys`, `source`, `destination`,
`intermediate`, and `steps`. `/minimal-cover` returns dependencies in the same
`left`/`right` form, with a single attribute on every `right`. Normal-form
violations include the form, dependency, and reason. A 3NF decomposition is
an array of attribute arrays; relation names are not assigned.

Invalid input returns HTTP 400:

```json
{
  "message": "[B] không thuộc universe.",
  "field": "dependencies[0].right"
}
```

CORS is enabled for all origins during development. For deployment, replace
the wildcard origin on `AlgorithmController` with the frontend's actual origin.
