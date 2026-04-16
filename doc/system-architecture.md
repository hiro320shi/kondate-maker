# システム構成・フォルダ構成・環境構築手順

**作成日**: 2026年4月16日

---

## 1. システム構成

### 全体アーキテクチャ

```
ブラウザ
  │
  │ http://localhost:5173
  ▼
┌─────────────────┐
│   frontend      │  Vue 3 + Vite（Tailwind CSS / Vue Router / Pinia）
│   (Docker)      │
└────────┬────────┘
         │ /api/* プロキシ
         │ http://backend:8080
         ▼
┌─────────────────┐
│   backend       │  Java 21 / Spring Boot 3.5.0（Spring Security / JPA）
│   (Docker)      │
└────────┬────────┘
         │ JDBC (MySQL Connector/J)
         │ mysql://db:3306
         ▼
┌─────────────────┐
│   db            │  MySQL 8.0
│   (Docker)      │
└─────────────────┘
```

### 技術スタック詳細

| 役割 | 技術 | バージョン |
|---|---|---|
| フロントエンド | Vue 3 + Vite | Vue 3.5 / Vite 8 |
| 状態管理 | Pinia | 3.x |
| ルーティング | Vue Router | 5.x |
| CSSフレームワーク | Tailwind CSS | 4.x |
| バックエンド | Spring Boot | 3.5.0 |
| 言語 | Java | 21 |
| ビルドツール | Gradle | （Spring Boot同梱） |
| 認証 | Spring Security + JWT | - |
| ORM | Spring Data JPA / Hibernate | - |
| データベース | MySQL | 8.0 |
| コンテナ | Docker / Docker Compose | - |

### ホスティング（本番）

| 役割 | サービス | 費用 |
|---|---|---|
| フロントエンド | Vercel | 無料枠 |
| バックエンド + DB | Railway | 月$5〜 |

---

## 2. フォルダ構成

```
kondate-maker/
├── docker-compose.yml          # 全サービスの起動定義
├── .env                        # 環境変数（Git管理外）
├── .env.example                # 環境変数のサンプル（コピー用）
├── .gitignore
├── CLAUDE.md                   # Claude Code用プロジェクト指示書
├── README.md
│
├── doc/                        # 設計・仕様ドキュメント
│   ├── requirements.md         # 要件定義書
│   ├── er-diagram.mermaid      # ERダイアグラム
│   ├── feature-er-mapping.md   # 機能一覧 & ER図マッピング
│   ├── api-design.md           # API設計書
│   ├── screen-design.md        # 画面設計書
│   ├── progress-summary.md     # 設計進捗まとめ
│   ├── system-architecture.md  # 本ファイル
│   └── 4-7todo.md              # ToDoリスト
│
├── frontend/                   # Vue 3 + Vite
│   ├── Dockerfile
│   ├── index.html
│   ├── vite.config.js          # Tailwind / /api プロキシ設定
│   ├── package.json
│   └── src/
│       ├── main.js
│       ├── App.vue
│       ├── style.css           # Tailwind CSS エントリ
│       ├── assets/
│       ├── components/
│       ├── views/              # （今後追加）
│       ├── router/             # （今後追加）
│       └── stores/             # （今後追加・Pinia）
│
└── backend/                    # Spring Boot + Gradle
    ├── Dockerfile              # マルチステージビルド（JDK→JRE）
    ├── build.gradle
    ├── settings.gradle
    ├── gradlew / gradlew.bat
    └── src/
        ├── main/
        │   ├── java/com/kondatemaker/backend/
        │   │   └── BackendApplication.java
        │   └── resources/
        │       └── application.properties
        └── test/
```

---

## 3. 環境構築手順

### 前提条件

以下がインストール済みであること。

| ツール | 推奨バージョン |
|---|---|
| Docker Desktop | 最新版 |
| Git | 最新版 |

### 手順

#### 1. リポジトリをクローン

```bash
git clone <リポジトリURL>
cd kondate-maker
```

#### 2. 環境変数ファイルを作成

```bash
cp .env.example .env
```

`.env` の内容を必要に応じて編集する（ローカル開発はデフォルトのままでOK）。

#### 3. Dockerコンテナを起動

```bash
docker compose up --build
```

初回は以下の処理が走るため数分かかる。

- Node.jsパッケージのインストール（frontend）
- GradleによるSpring Bootビルド（backend）
- MySQLの初期化（db）

#### 4. 動作確認

| サービス | URL |
|---|---|
| フロントエンド | http://localhost:5173 |
| バックエンドAPI | http://localhost:8080 |
| MySQL | localhost:3306 |

### よく使うコマンド

```bash
# 起動（バックグラウンド）
docker compose up -d

# 停止
docker compose down

# ログ確認
docker compose logs -f backend
docker compose logs -f frontend
docker compose logs -f db

# コンテナを再ビルドして起動
docker compose up --build

# DBのデータごと削除して初期化
docker compose down -v
```

---

## 4. 環境変数一覧

`.env` ファイルで管理する変数の一覧。

| 変数名 | 説明 | デフォルト値（ローカル） |
|---|---|---|
| `MYSQL_ROOT_PASSWORD` | MySQLのrootパスワード | `rootpassword` |
| `MYSQL_DATABASE` | DB名 | `kondate_db` |
| `MYSQL_USER` | DBユーザー名 | `kondate_user` |
| `MYSQL_PASSWORD` | DBパスワード | `kondate_pass` |
| `DB_HOST` | DBホスト名（Docker内） | `db` |
| `DB_PORT` | DBポート | `3306` |
| `JWT_SECRET` | JWT署名シークレット | 本番では必ず変更すること |
| `JWT_EXPIRATION_MS` | JWTの有効期限（ミリ秒） | `86400000`（24時間） |

> 本番環境（Railway）では、これらの値をRailwayの環境変数として設定する。`.env` ファイルは **絶対にGitにコミットしない**。
