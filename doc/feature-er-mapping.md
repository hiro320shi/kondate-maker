# 機能一覧 ＆ ER図マッピング表

## 1. 機能一覧

| # | グループ | 機能名 | 概要 |
|---|---|---|---|
| 1 | アカウント | 会員登録 | メール＋パスワードで登録 |
| 2 | アカウント | ログイン | 認証成功時にJWTを発行 |
| 3 | アカウント | ログアウト | フロント側でJWTを破棄（DB操作なし） |
| 4 | アカウント | パスワードリセット（リクエスト） | メールアドレスを入力してリセットメール送信 |
| 5 | アカウント | パスワードリセット（更新） | トークンを検証して新パスワードに更新 |
| 6 | レシピ | レシピ登録 | 新規レシピを登録 |
| 7 | レシピ | レシピ一覧表示 | 登録済みレシピを作成日時の降順で表示 |
| 8 | レシピ | レシピ編集 | 既存レシピの内容を更新 |
| 9 | レシピ | レシピ削除 | 論理削除（deleted_atに日時をセット） |
| 10 | レシピ | 料理名で検索 | 料理名の部分一致検索 |
| 11 | レシピ | カテゴリで絞り込み | カテゴリの単一選択で絞り込み |
| 12 | カテゴリ | カテゴリ一覧取得 | 絞り込みUIのセレクトボックスに使用 |

---

## 2. 機能 × テーブル/カラム マッピング表

### 凡例
- ◎ … 書き込み（INSERT / UPDATE / DELETE）
- ○ … 読み取り（SELECT）
- 認証 … JWTからuser_idを取得するだけ（SELECTなし）

| # | 機能名 | users | password_reset_tokens | categories | recipes |
|---|---|---|---|---|---|
| 1 | 会員登録 | ◎ email<br>◎ password_hash<br>◎ created_at<br>◎ updated_at | - | - | - |
| 2 | ログイン | ○ email<br>○ password_hash | - | - | - |
| 3 | ログアウト | - | - | - | - |
| 4 | PW リセット（リクエスト） | ○ id<br>○ email | ◎ user_id<br>◎ token<br>◎ expires_at<br>◎ created_at | - | - |
| 5 | PW リセット（更新） | ◎ password_hash<br>◎ updated_at | ○ token<br>○ expires_at<br>✕ DELETE | - | - |
| 6 | レシピ登録 | 認証 | - | ○ id | ◎ user_id<br>◎ category_id<br>◎ name<br>◎ point<br>◎ memo<br>◎ created_at<br>◎ updated_at |
| 7 | レシピ一覧表示 | 認証 | - | ○ id<br>○ name | ○ id<br>○ category_id<br>○ name<br>○ point<br>○ memo<br>○ created_at<br>条件: user_id<br>条件: deleted_at IS NULL |
| 8 | レシピ編集 | 認証 | - | ○ id | ○ id（存在確認）<br>◎ category_id<br>◎ name<br>◎ point<br>◎ memo<br>◎ updated_at<br>条件: user_id（他人のレシピを編集不可） |
| 9 | レシピ削除 | 認証 | - | - | ◎ deleted_at<br>条件: id<br>条件: user_id（他人のレシピを削除不可） |
| 10 | 料理名で検索 | 認証 | - | ○ id<br>○ name | ○ name（LIKE検索）<br>条件: user_id<br>条件: deleted_at IS NULL |
| 11 | カテゴリで絞り込み | 認証 | - | ○ id | ○ category_id<br>条件: user_id<br>条件: deleted_at IS NULL |
| 12 | カテゴリ一覧取得 | - | - | ○ id<br>○ name<br>○ sort_order | - |

---

## 3. ER図との照合結果

### ✅ 問題なし
- 全機能で必要なカラムがテーブルに存在する
- 論理削除（deleted_at）は機能7〜11すべての検索条件で使用されており、設計通り

### 💡 実装時の注意点

| 項目 | 内容 |
|---|---|
| 他人のレシピ操作防止 | 編集・削除時に `user_id = ログイン中のユーザーID` を必ずWHERE条件に含める |
| 検索＋絞り込みの同時使用 | 料理名検索とカテゴリ絞り込みは同時に使える仕様にすること（AND条件） |
| トークンの使用済み処理 | PW リセット更新後は必ずトークンをDELETEする |
| カテゴリ一覧の取得タイミング | レシピ登録・編集画面の表示時にAPIを叩いて取得する |
