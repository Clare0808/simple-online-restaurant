import { defineStore } from "pinia";
import axios from "axios";

export const useUserStore = defineStore("user", {
  state: () => ({
    user: null,
    token: localStorage.getItem("token") || null, // 刷新頁面仍保持登入狀態
    isAuthenticated: !!localStorage.getItem("token"), // 轉化成布林值
  }),
  actions: {
    async signup(userData) {
      try {
        const response = await axios.post("/api/signup", userData);
        this.user = response.data.user;
      } catch (error) {
        // 後端回傳的錯誤訊息
        const msg = error.response?.data?.message || "註冊失敗";
        throw new Error(msg);
      }
    },
    async login(credentials) {
      try {
        const response = await axios.post("/api/login", credentials);
        this.user = response.data.user;
        this.token = response.data.token;
        this.isAuthenticated = true;

        localStorage.setItem("token", this.token);
      } catch (error) {
        const msg =
          error.response?.data?.message ||
          "無法連接後端，請確認 Java 服務已啟動";
        throw new Error(msg);
      }
    },
    async logout() {
      try {
        await axios.get("/api/logout", { withCredentials: true });
      } catch {
        // Always clear local login data, even if the server is unavailable.
      }

      this.user = null;
      this.token = null;
      this.isAuthenticated = false;
      localStorage.removeItem("token");
      localStorage.removeItem("userEmail");
      localStorage.removeItem("userName");
      localStorage.removeItem("inAdmin");
    },
    async init(loginUser) {
      try {
        const response = await axios.post("/api/now-login", {
          email: loginUser,
        });

        this.user = response.data.user;
        return this.user;
      } catch (error) {
        const msg = error.response?.data?.message || "初始化失敗";
        throw new Error(msg);
      }
    },
  },
  getters: {
    isAdmin: (state) => state.user?.role === "admin",
    isUser: (state) => state.user?.role === "user",
  },
});
