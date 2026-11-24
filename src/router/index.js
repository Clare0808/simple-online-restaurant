import { createRouter, createWebHistory } from "vue-router";
import HomePage from "../views/HomePage.vue";
import MenuPage from "@/components/MenuPage.vue";

const routes = [
  {
    path: "/",
    name: "HomePage",
    component: HomePage,
  },
  {
    path: "/menu",
    name: "MenuPage",
    component: MenuPage,
  },
];

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes,
});

export default router;
