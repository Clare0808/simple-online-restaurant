import { createRouter, createWebHistory } from "vue-router";
import HomePage from "../views/HomePage.vue";
import MenuPage from "@/components/MenuPage.vue";
import CartPage from "@/components/CartPage.vue";

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
  {
    path: "/cart",
    name: "CartPage",
    component: CartPage,
  },
];

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes,
});

export default router;
