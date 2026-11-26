# simple-online-restaurant

## Project setup
```
npm install
```

### Compiles and hot-reloads for development
```
npm run serve
```

### Compiles and minifies for production
```
npm run build
```

### Lints and fixes files
```
npm run lint
```

### Customize configuration
See [Configuration Reference](https://cli.vuejs.org/config/).

## Docker 全端部署到 Render

這個專案以單一 Docker image 部署：Docker 先建置 Vue，再將前端檔案包進 Spring Boot jar，由同一個 Java 服務提供網頁與 `/api`。

### 部署到 Render

1. 準備一個 MongoDB Atlas 資料庫，取得包含資料庫名稱的連線 URI。
2. 將專案推送到 GitHub，並在 Render 選擇 **New + → Blueprint**，連接此 repository。Render 會讀取根目錄的 `render.yaml` 並使用 Docker 建置。
3. 在 Render 的服務環境變數中設定 `MONGO_URL` 為 Atlas URI。`JWT_SECRET` 由 Blueprint 自動產生，`PORT` 由 Render 提供。
4. 等待部署完成，確認 `/api/health` 回傳 `{"status":"ok"}`。

Render 服務不會在容器內啟動 MongoDB；請使用 Atlas 或其他可從 Render 連線的 MongoDB。MongoDB 密碼只放在 Render 的環境變數，不要提交到 Git。

### 本機 Docker Compose

```powershell
docker compose up --build
```

網站會在 `http://localhost:3001` 提供服務，MongoDB 資料保存在 Compose volume。需要更換本機 JWT 密鑰時，可在執行前設定 `$env:JWT_SECRET`。
