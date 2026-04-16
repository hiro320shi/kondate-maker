# 献立自動作成アプリ 設計進捗まとめ

**作成日**: 2026年4月7日 / **最終更新**: 2026年4月16日

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
| DB | MySQL | 両技術と相性◎ |
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

## 6. 進捗サマリー（2026年4月16日時点）

```
✅ 技術選定
✅ 認証方式の決定
✅ ER図作成
✅ 機能一覧 × ER図照合
✅ API設計書（doc/api-design.md）
✅ 画面設計（doc/screen-design.md）
✅ Docker環境構築（frontend / backend / db）
       ↓
🔜 バックエンド実装スタート（会員登録・ログインAPIから）
```

---

## 7. 環境構築について

- 手順・構成は `doc/system-architecture.md` を参照
- 起動コマンド: `docker compose up --build`
- 詳細な環境変数一覧・よく使うコマンドも同ファイルに記載

---

## 8. 次にやること（開発再開時のエントリポイント）

### ステップ1：バックエンド実装（Spring Boot）

以下の順番で実装を進める。

| 順番 | 内容 | 対応APIエンドポイント |
|---|---|---|
| 1 | Entityクラス作成（User / Recipe / Category / PasswordResetToken） | - |
| 2 | Repositoryクラス作成（Spring Data JPA） | - |
| 3 | JWTユーティリティ実装 | - |
| 4 | 会員登録API | `POST /api/v1/auth/register` |
| 5 | ログインAPI（JWT発行） | `POST /api/v1/auth/login` |
| 6 | Spring Security設定（JWT認証フィルター） | - |
| 7 | レシピCRUD API | `GET/POST/PUT/DELETE /api/v1/recipes` |
| 8 | カテゴリ一覧API | `GET /api/v1/categories` |

### ステップ2：フロントエンド実装（Vue 3）

バックエンドAPIが一部できたタイミングで並行開始。

| 順番 | 内容 |
|---|---|
| 1 | Vue Routerのルート定義（ログイン・会員登録・レシピ一覧） |
| 2 | Piniaストア設計（認証状態管理） |
| 3 | ログイン画面・会員登録画面 |
| 4 | レシピ一覧・登録・編集画面 |

### 未決定事項（実装前に決めると良いもの）

| 項目 | 優先度 |
|---|---|
| バリデーション仕様（パスワード要件・文字数上限） | 高 |
| CORS許可オリジンの定義 | 高 |
| テスト方針（JUnit5 / Vitestを書くか） | 中 |
| メール送信サービスの選定（SendGrid等） | 中（PW リセット実装前まででOK） |
