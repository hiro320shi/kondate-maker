# Phase 1 実装サマリー

**実装日**: 2026年5月2日

---

## 概要

Phase 1（レシピ管理 + 個人アカウント）のバックエンドAPI・フロントエンド全画面を実装した。  
`docker compose up --build` で全サービスが起動し、ブラウザから操作できる状態。

---

## バックエンド実装内容

### 追加ファイル一覧

| ファイル | 役割 |
|---|---|
| `dto/recipe/RecipeRequest.java` | レシピ登録・更新のリクエストDTO（バリデーション付き） |
| `dto/recipe/RecipeResponse.java` | レシピ一覧・詳細のレスポンスDTO |
| `dto/category/CategoryResponse.java` | カテゴリ一覧のレスポンスDTO |
| `service/RecipeService.java` | レシピCRUDのビジネスロジック |
| `service/CategoryService.java` | カテゴリ一覧取得のビジネスロジック |
| `controller/RecipeController.java` | レシピAPIエンドポイント |
| `controller/CategoryController.java` | カテゴリAPIエンドポイント |
| `config/DataInitializer.java` | カテゴリ初期データの自動投入 |

### 実装済みAPIエンドポイント

| メソッド | URL | 認証 | 説明 |
|---|---|---|---|
| `GET` | `/api/v1/recipes` | 必要 | レシピ一覧（名前検索・カテゴリ絞り込み対応） |
| `POST` | `/api/v1/recipes` | 必要 | レシピ登録 |
| `PUT` | `/api/v1/recipes/{id}` | 必要 | レシピ更新 |
| `DELETE` | `/api/v1/recipes/{id}` | 必要 | レシピ削除（論理削除） |
| `GET` | `/api/v1/categories` | 必要 | カテゴリ一覧 |

### 実装上のポイント

- **所有者チェック**: レシピの更新・削除時は `WHERE user_id = ログインユーザーID` で他人のレシピを操作できないようにしている
- **論理削除**: レシピ削除は `deleted_at` に日時をセットする方式（物理削除なし）
- **カテゴリ初期データ**: `DataInitializer`（`CommandLineRunner`）が起動時に categories テーブルが空の場合のみ「主食・主菜・副菜・汁物・その他」を投入する
- **404ハンドラ**: `GlobalExceptionHandler` に `NoSuchElementException` のハンドラを追加（レシピが見つからない場合に HTTP 404 を返す）

---

## フロントエンド実装内容

### 追加ファイル一覧

| ファイル | 役割 |
|---|---|
| `src/main.js` | Pinia・Vue Router を createApp に登録 |
| `src/App.vue` | ナビバー（メールアドレス表示・ログアウト）+ `<RouterView>` |
| `src/router/index.js` | ルート定義・認証ガード |
| `src/stores/auth.js` | JWT・メールアドレスの Pinia ストア |
| `src/api/index.js` | fetch ベースのAPIクライアント |
| `src/pages/LoginPage.vue` | ログイン画面 |
| `src/pages/RegisterPage.vue` | 会員登録画面 |
| `src/pages/RecipeListPage.vue` | レシピ一覧・検索・削除画面 |
| `src/pages/RecipeFormPage.vue` | レシピ登録・編集画面（共通コンポーネント） |

### 画面・ルート対応

| URL | 画面 | 認証要否 |
|---|---|---|
| `/login` | ログイン | 不要（ログイン済みは `/recipes` へリダイレクト） |
| `/register` | 会員登録 | 不要（ログイン済みは `/recipes` へリダイレクト） |
| `/recipes` | レシピ一覧 | 必要 |
| `/recipes/new` | レシピ新規登録 | 必要 |
| `/recipes/:id/edit` | レシピ編集 | 必要 |

### 実装上のポイント

- **APIクライアント** (`src/api/index.js`): ネイティブ `fetch` を使用。`localStorage` からJWTを取得して `Authorization: Bearer` ヘッダーに付与する
- **認証状態管理** (`src/stores/auth.js`): JWTとメールを `localStorage` に永続化。ページリロード後も認証状態が維持される
- **ルートガード**: 未認証ユーザーが保護ルートにアクセスすると `/login` にリダイレクト。認証済みユーザーがログイン画面にアクセスすると `/recipes` にリダイレクト
- **レシピフォーム**: 新規登録・編集で同一コンポーネントを使用。`route.params.id` の有無で分岐
- **ポイント入力**: 0.1〜1.0 を `<select>` で選択する形式（バリデーション仕様に合わせて0.1刻み）
- **Viteプロキシ**: `/api` へのリクエストは `http://backend:8080` に転送（Docker内通信）

---

## 起動手順

```bash
# 環境変数ファイルをコピーして必要な値を設定
cp .env.example .env

# 全サービスを起動（初回はビルドに時間がかかる）
docker compose up --build
```

起動後、`http://localhost:5173` にアクセスする。

---

## 残タスク

| タスク | 優先度 | 備考 |
|---|---|---|
| Vercel へフロントをデプロイ | 高 | CORS許可オリジンの追加が必要 |
| Railway へバックエンド+DBをデプロイ | 高 | 環境変数の設定が必要 |
| パスワードリセット機能 | 中 | メール送信サービスの選定が先決 |
| Phase 2（献立自動作成） | 低 | 要件は `doc/4-7todo.md` 参照 |
