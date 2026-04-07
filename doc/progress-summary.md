# 献立自動作成アプリ 設計進捗まとめ

**作成日**: 2026年4月7日

---

## 1. プロジェクト概要

家族の1週間分の献立を自動生成するWebアプリ。  
Phase 1ではレシピ管理と個人アカウント機能を実装する。

---

## 2. 技術選定

| 役割 | 採用技術 | 補足 |
|---|---|---|
| フロントエンド | Vue 3 + Vite | Vue Router / Pinia を使用 |
| バックエンド | Java / Spring Boot | スキルセットに合わせて選定 |
| DB | PostgreSQL | 両技術と相性◎ |
| 認証 | JWT（自前実装） | Spring Security を使用 |
| フロントホスティング | Vercel | 無料枠で対応 |
| バックエンド+DB | Railway | 月$5〜、Java対応 |

**月額コスト目安：$5〜10（数人規模）**

---

## 3. 認証方式

### 通常認証（JWT）

- ログイン成功時にサーバーがJWTを発行
- フロント（Vue）がJWTを保持し、以降のAPIリクエストに付与
- サーバーはJWTの署名を検証してユーザーを特定
- DBを毎回参照しないステートレスな仕組み

### パスワードリセット（専用トークン）

- JWTではなく、DBに保存するランダムトークンを使用
- 理由：「1回使ったら即無効」にするため、DBから削除する必要がある
- フロー：リセット申請 → トークン生成＆メール送信 → トークン検証 → パスワード更新 → トークン削除

---

## 4. DB設計（ER図）

### 設計方針の決定事項

| 項目 | 決定内容 |
|---|---|
| カテゴリ管理 | `categories`テーブルを作成（将来の追加に対応） |
| レシピ削除 | 論理削除（`deleted_at`カラムで管理） |
| Phase 2の献立テーブル | 今回のER図には含めない（Phase 1のみ） |

### テーブル一覧

#### users
| カラム | 型 | 備考 |
|---|---|---|
| id | bigint | PK |
| email | varchar | UNIQUE / NOT NULL |
| password_hash | varchar | bcryptでハッシュ化 |
| created_at | timestamp | NOT NULL |
| updated_at | timestamp | NOT NULL |

#### password_reset_tokens
| カラム | 型 | 備考 |
|---|---|---|
| id | bigint | PK |
| user_id | bigint | FK → users.id |
| token | varchar | UNIQUE / ランダム文字列 |
| expires_at | timestamp | 有効期限（例：1時間） |
| created_at | timestamp | NOT NULL |

#### categories
| カラム | 型 | 備考 |
|---|---|---|
| id | bigint | PK |
| name | varchar | UNIQUE（主食・主菜・副菜・汁物・その他） |
| sort_order | int | 表示順の制御 |
| created_at | timestamp | NOT NULL |

#### recipes
| カラム | 型 | 備考 |
|---|---|---|
| id | bigint | PK |
| user_id | bigint | FK → users.id |
| category_id | bigint | FK → categories.id |
| name | varchar | 料理名 |
| point | decimal | 0.1〜1.0（1食の構成比） |
| memo | text | NULL許容 |
| created_at | timestamp | 一覧の並び順に使用 |
| updated_at | timestamp | NOT NULL |
| deleted_at | timestamp | NULL = 有効 / 論理削除 |

---

## 5. 機能一覧とER図照合

### 機能一覧

| # | グループ | 機能名 |
|---|---|---|
| 1 | アカウント | 会員登録 |
| 2 | アカウント | ログイン |
| 3 | アカウント | ログアウト（フロントのみ） |
| 4 | アカウント | パスワードリセット（リクエスト） |
| 5 | アカウント | パスワードリセット（更新） |
| 6 | レシピ | レシピ登録 |
| 7 | レシピ | レシピ一覧表示 |
| 8 | レシピ | レシピ編集 |
| 9 | レシピ | レシピ削除（論理削除） |
| 10 | レシピ | 料理名で検索 |
| 11 | レシピ | カテゴリで絞り込み |
| 12 | カテゴリ | カテゴリ一覧取得 |

### 照合結果

- ✅ 全機能で必要なカラムがテーブルに存在する
- ✅ 論理削除（deleted_at）は機能7〜11すべての検索条件で使用される

### 実装時の注意点

| 項目 | 内容 |
|---|---|
| 他人のレシピ操作防止 | 編集・削除時に `WHERE user_id = ログイン中のユーザーID` を必ず付ける |
| 検索＋絞り込みの同時使用 | 料理名検索とカテゴリ絞り込みはAND条件で同時に使える設計にする |
| トークンの使用済み処理 | PW リセット更新後は必ずトークンをDELETEする |
| カテゴリ一覧の取得タイミング | レシピ登録・編集画面の表示時にAPIを叩いて取得する |

---

## 6. 今後の進め方

```
✅ 技術選定
✅ 認証方式の決定
✅ ER図作成
✅ 機能一覧 × ER図照合
       ↓
🔜 API設計書
       ↓
🔜 画面設計（ワイヤーフレーム）
       ↓
🔜 環境構築・開発スタート
```
