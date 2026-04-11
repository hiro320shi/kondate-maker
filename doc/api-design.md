# API設計書 - 献立自動作成アプリ Phase 1

**作成日**: 2026年4月11日  
**バージョン**: 1.0

---

## 1. 基本方針

| 項目 | 決定内容 |
|---|---|
| スタイル | REST API |
| URLプレフィックス | `/api/v1/` |
| データ形式 | JSON（`Content-Type: application/json`） |
| 認証方式 | JWT Bearer トークン（`Authorization: Bearer <token>`） |
| JWTの有効期限 | **24時間**（利便性とセキュリティのバランスで決定） |
| リフレッシュトークン | **Phase 1はなし**（シンプルさを優先。有効期限切れはログインし直し） |

---

## 2. 共通レスポンス形式

### 成功時（データあり）

```json
{
  "data": { ... }
}
```

### 成功時（一覧）

```json
{
  "data": [ ... ]
}
```

### 成功時（データなし: 204 No Content）

レスポンスボディなし

### エラー時

```json
{
  "error": {
    "code": "ERROR_CODE",
    "message": "エラーの説明（英語）"
  }
}
```

---

## 3. HTTPステータスコード方針

| コード | 用途 |
|---|---|
| 200 OK | GET・PUT の成功 |
| 201 Created | POST の成功（リソース作成） |
| 204 No Content | DELETE の成功 |
| 400 Bad Request | バリデーションエラー、リクエスト形式不正 |
| 401 Unauthorized | JWTなし・無効・期限切れ |
| 403 Forbidden | 他ユーザーのリソースへのアクセス |
| 404 Not Found | リソースが存在しない（論理削除済み含む） |
| 409 Conflict | 既に存在するリソース（例: メールアドレス重複） |
| 500 Internal Server Error | サーバー内部エラー |

> **404 vs 400 の使い分け**: URLに含まれるID（`/recipes/{id}`）が存在しない場合は 404。クエリパラメータや本文の値が不正な場合は 400。

---

## 4. バリデーション仕様

| フィールド | ルール |
|---|---|
| メールアドレス | フォーマット検証のみ（ドメイン制限なし）。最大255文字 |
| パスワード | 8文字以上・64文字以下。英字と数字をそれぞれ1文字以上含む |
| 料理名 | 1文字以上・100文字以下 |
| ポイント | 0.1〜1.0の範囲。小数点以下1桁まで（0.15 は不可） |
| メモ | 任意。最大500文字 |

---

## 5. エンドポイント一覧

| # | メソッド | URL | 認証 | 概要 |
|---|---|---|---|---|
| 1 | POST | `/api/v1/auth/register` | 不要 | 会員登録 |
| 2 | POST | `/api/v1/auth/login` | 不要 | ログイン |
| 3 | POST | `/api/v1/auth/password-reset/request` | 不要 | パスワードリセット申請 |
| 4 | POST | `/api/v1/auth/password-reset/confirm` | 不要 | パスワードリセット実行 |
| 5 | GET | `/api/v1/categories` | 必要 | カテゴリ一覧取得 |
| 6 | GET | `/api/v1/recipes` | 必要 | レシピ一覧取得（検索・絞り込み） |
| 7 | POST | `/api/v1/recipes` | 必要 | レシピ登録 |
| 8 | GET | `/api/v1/recipes/{id}` | 必要 | レシピ詳細取得 |
| 9 | PUT | `/api/v1/recipes/{id}` | 必要 | レシピ編集 |
| 10 | DELETE | `/api/v1/recipes/{id}` | 必要 | レシピ削除（論理削除） |

---

## 6. エンドポイント詳細

---

### 1. 会員登録

**POST** `/api/v1/auth/register`

#### リクエスト

```json
{
  "email": "user@example.com",
  "password": "Password1"
}
```

#### レスポンス: 201 Created

```json
{
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 86400
  }
}
```

#### エラー

| ステータス | code | 条件 |
|---|---|---|
| 400 | `VALIDATION_ERROR` | メールアドレス形式不正、パスワードが条件未満 |
| 409 | `EMAIL_ALREADY_EXISTS` | 既にそのメールアドレスで登録済み |

---

### 2. ログイン

**POST** `/api/v1/auth/login`

#### リクエスト

```json
{
  "email": "user@example.com",
  "password": "Password1"
}
```

#### レスポンス: 200 OK

```json
{
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 86400
  }
}
```

#### エラー

| ステータス | code | 条件 |
|---|---|---|
| 400 | `VALIDATION_ERROR` | 必須項目が空 |
| 401 | `INVALID_CREDENTIALS` | メールアドレスまたはパスワードが不一致（どちらが誤りかは伝えない） |

---

### 3. パスワードリセット申請

**POST** `/api/v1/auth/password-reset/request`

#### リクエスト

```json
{
  "email": "user@example.com"
}
```

#### レスポンス: 200 OK

```json
{
  "data": {
    "message": "If the email address is registered, a reset email has been sent."
  }
}
```

> **セキュリティ上の注意**: メールアドレスが登録済みかどうかを問わず、必ず同じ成功レスポンスを返す（ユーザー列挙攻撃の防止）。

#### リセットトークンの仕様
- 有効期限: **1時間**
- 形式: UUID（ランダム文字列）
- `password_reset_tokens` テーブルに保存し、使用後は削除

#### エラー

| ステータス | code | 条件 |
|---|---|---|
| 400 | `VALIDATION_ERROR` | メールアドレス形式不正 |

---

### 4. パスワードリセット実行

**POST** `/api/v1/auth/password-reset/confirm`

#### リクエスト

```json
{
  "token": "reset-token-uuid",
  "newPassword": "NewPassword1"
}
```

#### レスポンス: 200 OK

```json
{
  "data": {
    "message": "Password has been reset successfully."
  }
}
```

#### エラー

| ステータス | code | 条件 |
|---|---|---|
| 400 | `VALIDATION_ERROR` | 新パスワードが条件未満 |
| 400 | `INVALID_OR_EXPIRED_TOKEN` | トークンが無効または期限切れ |

---

### 5. カテゴリ一覧取得

**GET** `/api/v1/categories`

**認証**: 必要（`Authorization: Bearer <token>`）

#### レスポンス: 200 OK

```json
{
  "data": [
    { "id": 1, "name": "主食", "sortOrder": 1 },
    { "id": 2, "name": "主菜", "sortOrder": 2 },
    { "id": 3, "name": "副菜", "sortOrder": 3 },
    { "id": 4, "name": "汁物", "sortOrder": 4 },
    { "id": 5, "name": "その他", "sortOrder": 5 }
  ]
}
```

---

### 6. レシピ一覧取得

**GET** `/api/v1/recipes`

**認証**: 必要

#### クエリパラメータ

| パラメータ | 型 | 必須 | 説明 |
|---|---|---|---|
| `q` | string | 任意 | 料理名で部分一致検索 |
| `categoryId` | number | 任意 | カテゴリIDで絞り込み |

例: `GET /api/v1/recipes?q=から揚げ&categoryId=2`

> 両方指定した場合は AND 条件で絞り込む。

#### レスポンス: 200 OK

```json
{
  "data": [
    {
      "id": 1,
      "name": "から揚げ",
      "category": {
        "id": 2,
        "name": "主菜"
      },
      "point": 0.5,
      "memo": "下味は一晩漬けると美味しい",
      "createdAt": "2026-04-10T12:00:00Z",
      "updatedAt": "2026-04-10T12:00:00Z"
    }
  ]
}
```

> - 並び順: `created_at` 降順（新しい順）
> - `deleted_at` が NULL のレコードのみ返す
> - ログインユーザー自身のレシピのみ返す

---

### 7. レシピ登録

**POST** `/api/v1/recipes`

**認証**: 必要

#### リクエスト

```json
{
  "name": "から揚げ",
  "categoryId": 2,
  "point": 0.5,
  "memo": "下味は一晩漬けると美味しい"
}
```

#### レスポンス: 201 Created

```json
{
  "data": {
    "id": 1,
    "name": "から揚げ",
    "category": {
      "id": 2,
      "name": "主菜"
    },
    "point": 0.5,
    "memo": "下味は一晩漬けると美味しい",
    "createdAt": "2026-04-10T12:00:00Z",
    "updatedAt": "2026-04-10T12:00:00Z"
  }
}
```

#### エラー

| ステータス | code | 条件 |
|---|---|---|
| 400 | `VALIDATION_ERROR` | バリデーション違反 |
| 404 | `CATEGORY_NOT_FOUND` | 指定した `categoryId` が存在しない |

---

### 8. レシピ詳細取得

**GET** `/api/v1/recipes/{id}`

**認証**: 必要

#### レスポンス: 200 OK

```json
{
  "data": {
    "id": 1,
    "name": "から揚げ",
    "category": {
      "id": 2,
      "name": "主菜"
    },
    "point": 0.5,
    "memo": "下味は一晩漬けると美味しい",
    "createdAt": "2026-04-10T12:00:00Z",
    "updatedAt": "2026-04-10T12:00:00Z"
  }
}
```

#### エラー

| ステータス | code | 条件 |
|---|---|---|
| 403 | `FORBIDDEN` | 他ユーザーのレシピ |
| 404 | `RECIPE_NOT_FOUND` | 存在しない・論理削除済み |

---

### 9. レシピ編集

**PUT** `/api/v1/recipes/{id}`

**認証**: 必要

#### リクエスト

```json
{
  "name": "から揚げ（改）",
  "categoryId": 2,
  "point": 0.6,
  "memo": "レモンを添えると美味しい"
}
```

> すべてのフィールドを送る（部分更新ではなく全置換）。

#### レスポンス: 200 OK

```json
{
  "data": {
    "id": 1,
    "name": "から揚げ（改）",
    "category": {
      "id": 2,
      "name": "主菜"
    },
    "point": 0.6,
    "memo": "レモンを添えると美味しい",
    "createdAt": "2026-04-10T12:00:00Z",
    "updatedAt": "2026-04-11T09:00:00Z"
  }
}
```

#### エラー

| ステータス | code | 条件 |
|---|---|---|
| 400 | `VALIDATION_ERROR` | バリデーション違反 |
| 403 | `FORBIDDEN` | 他ユーザーのレシピ |
| 404 | `RECIPE_NOT_FOUND` | 存在しない・論理削除済み |
| 404 | `CATEGORY_NOT_FOUND` | 指定した `categoryId` が存在しない |

---

### 10. レシピ削除（論理削除）

**DELETE** `/api/v1/recipes/{id}`

**認証**: 必要

#### レスポンス: 204 No Content

レスポンスボディなし

#### エラー

| ステータス | code | 条件 |
|---|---|---|
| 403 | `FORBIDDEN` | 他ユーザーのレシピ |
| 404 | `RECIPE_NOT_FOUND` | 存在しない・論理削除済み |

---

## 7. エラーコード一覧

| code | 説明 |
|---|---|
| `VALIDATION_ERROR` | バリデーション違反（詳細は `message` に含む） |
| `INVALID_CREDENTIALS` | ログイン失敗 |
| `EMAIL_ALREADY_EXISTS` | メールアドレス重複 |
| `INVALID_OR_EXPIRED_TOKEN` | パスワードリセットトークンが無効または期限切れ |
| `UNAUTHORIZED` | JWTなし・無効・期限切れ |
| `FORBIDDEN` | 他ユーザーのリソースへのアクセス |
| `RECIPE_NOT_FOUND` | レシピが存在しない |
| `CATEGORY_NOT_FOUND` | カテゴリが存在しない |
| `INTERNAL_SERVER_ERROR` | サーバー内部エラー |

---

## 8. CORS設定方針

| 環境 | 許可オリジン |
|---|---|
| 開発 | `http://localhost:5173`（Vite のデフォルトポート） |
| 本番 | Vercel のデプロイURL（決定後に記載） |

---

## 9. 未確定事項（実装前に要確認）

| 項目 | 内容 |
|---|---|
| メール送信サービス | SendGrid / Resend など選定が必要。多くは月100〜200通まで無料枠あり |
| 本番ドメイン（CORS） | Vercel のデプロイURL確定後に設定 |
| レートリミット | 今回は設けない方針（Phase 1 のスコープ外として保留） |
| HTTPS | Railway・Vercel ともにデフォルトで HTTPS 対応のため問題なし |
