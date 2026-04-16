# CLAUDE.md - 献立自動作成アプリ

このファイルはClaude Codeがこのプロジェクトで作業する際の指示書です。

---

## プロジェクト概要

家族の1週間分の献立を自動生成するWebアプリ。

---

## 技術スタック

| 役割 | 技術 | 備考 |
|---|---|---|
| フロントエンド | Vue 3 + Vite | Vue Router / Pinia を使用 |
| バックエンド | Java / Spring Boot | Spring Security でJWT認証 |
| データベース | MySQL | |
| フロントホスティング | Vercel | |
| バックエンド+DB | Railway | 月$5〜 |

---

## ディレクトリ構成

```
kondate-maker/
├── docker-compose.yml  # Docker全サービス定義
├── .env                # 環境変数（Git管理外）
├── .env.example        # 環境変数サンプル
├── frontend/           # Vue 3 + Vite
├── backend/            # Spring Boot (Gradle)
├── doc/                # 要件定義・設計ドキュメント
└── CLAUDE.md
```

---

## 開発ルール

- コミットメッセージは日本語でOK
- ブランチ戦略は未確定（決まり次第ここに追記）
- `doc/` 配下のドキュメントを最新に保つこと

---

## フェーズ構成

| フェーズ | 内容 | ステータス |
|---|---|---|
| Phase 1 | レシピ管理 ＋ 個人アカウント | 要件定義中 |
| Phase 2 | 献立自動作成 ＋ 家族グループ | 今後検討 |
| Phase 3 | 食材登録・冷蔵庫機能 | 今後検討 |

詳細は `doc/requirements.md` を参照。

---

## 重要ドキュメント

| ファイル | 内容 |
|---|---|
| `doc/requirements.md` | 要件定義書 |
| `doc/er-diagram.mermaid` | ERダイアグラム |
| `doc/feature-er-mapping.md` | 機能一覧 & ER図マッピング |
| `doc/progress-summary.md` | 設計進捗まとめ |
| `doc/system-architecture.md` | システム構成・フォルダ構成・環境構築手順 |
| `doc/4-7todo.md` | 要件定義ToDoリスト |
