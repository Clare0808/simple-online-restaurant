<template>
  <div class="menu-page">
    <transition name="fade">
      <div class="title" v-if="showText">菜單 Menu</div>
    </transition>
    <div class="container">
      <transition name="slide">
        <div class="options-frame" v-if="showElement">
          <div v-for="opt in options" :key="opt.label">
            <div
              class="option"
              @click="selectCategory(opt.label)"
              :class="{ active: selectedCategory === opt.label }"
            >
              {{ opt.name }}
            </div>
          </div>
        </div>
      </transition>
      <transition name="slide">
        <div class="dish-box-frame" v-if="showElement">
          <div
            class="dish-frame"
            v-for="dish in filteredList"
            :key="dish.id"
            @click="openOrder(dish)"
          >
            <img :src="dish.image" />
            <div class="dish-info-frame">
              <div class="dish-name">{{ dish.name }}</div>
              <div class="dish-price">${{ dish.price }}</div>
            </div>
          </div>
        </div>
      </transition>
    </div>
    <transition name="fade">
      <div v-if="selectedDish" class="overlay" @click.self="closeOrder">
        <OrderDish
          :dish="selectedDish"
          @close="closeOrder"
          @add-to-cart="handleAddToCart"
        />
      </div>
    </transition>
  </div>
</template>

<script>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import optionsData from "@/assets/data/optionsData.json";
import OrderDish from "@/components/ele/OrderDish.vue";
import { useUserStore } from "@/store/user";
import { errorUiStore } from "@/store/error";

export default {
  name: "MenuPage",
  components: { OrderDish },

  setup() {
    const router = useRouter();
    const userStore = useUserStore();
    const errorStore = errorUiStore();
    const dishList = ref([]);
    const showText = ref(false);
    const showElement = ref(false);
    const options = optionsData;
    const selectedCategory = ref("all");
    const filteredList = ref([]);
    const selectedDish = ref(null);

    const GetDishData = async () => {
      const response = await fetch("/data/dishData.json");
      if (!response.ok) {
        throw new Error(`菜單載入失敗：${response.status}`);
      }
      const data = await response.json();

      dishList.value = data;
      filteredList.value = data;
    };

    const selectCategory = (category) => {
      selectedCategory.value = category;
      filteredList.value =
        category === "all"
          ? dishList.value
          : dishList.value.filter((dish) => dish.category === category);
    };

    const openOrder = (dish) => {
      selectedDish.value = dish;
    };

    const closeOrder = () => {
      selectedDish.value = null;
    };

    const handleAddToCart = async ({ dish, amount }) => {
      if (!userStore.isAuthenticated) {
        errorStore.SetError("請先登入!");
        errorStore.CloseEle();
        closeOrder();
        router.push("/login");
        return;
      }

      const email = userStore.user?.email || localStorage.getItem("userEmail");
      if (!email) {
        errorStore.SetError("請先登入!");
        errorStore.CloseEle();
        closeOrder();
        router.push("/login");
        return;
      }

      try {
        const response = await fetch("/api/send-orders", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            email,
            name: dish.name,
            amount,
            price: Number(dish.price) || 0,
            image: dish.image,
            check: false,
            total: (Number(dish.price) || 0) * amount,
          }),
        });
        const result = await response.json();
        if (!response.ok) throw new Error(result.message || "加入購物車失敗");

        errorStore.SetSuccess("已加入購物車!");
        errorStore.CloseEle();
        closeOrder();
      } catch (error) {
        errorStore.SetError(error.message || "無法連接購物車服務。");
        errorStore.CloseEle();
      }
    };

    onMounted(async () => {
      showText.value = true;
      showElement.value = true;

      try {
        await GetDishData();
      } catch (error) {
        errorStore.SetError(error.message || "菜單載入失敗。");
        errorStore.CloseEle();
      }
    });

    return {
      options,
      selectedCategory,
      filteredList,
      selectedDish,
      showText,
      showElement,
      selectCategory,
      openOrder,
      closeOrder,
      handleAddToCart,
    };
  },
};
</script>

<style scoped>
.menu-page {
  padding-bottom: 50px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  position: relative;
}
.title {
  color: #f0c42d;
  font-size: 35px;
  font-weight: bold;
  margin: 20px 0;
}
.container {
  width: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}
.options-frame {
  margin-bottom: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
}
.option {
  width: 80px;
  color: #272727;
  font-size: 20px;
  background-color: #ffffff;
  border: 1px solid #f0c42d;
  border-radius: 20px;
  margin: 10px;
  padding: 10px;
  transition: all 0.3s ease;
}
.option:hover {
  color: #ffffff;
  background-color: #f0c42d;
  cursor: pointer;
}
.active {
  color: #ffffff;
  background-color: #f0c42d;
}
.dish-box-frame {
  width: 90%;
  display: grid;
  grid-template-columns: repeat(3, 30%);
  grid-template-rows: auto;
  gap: 20px;
  justify-content: center;
  align-items: center;
}
.dish-frame {
  width: 300px;
  background-color: #ffffff;
  border: 1px solid #f0c42d;
  border-radius: 20px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  transition: all 0.3s ease;
}
.dish-frame:hover {
  transform: scale(1.05);
}
.dish-frame img {
  width: 250px;
}
.dish-info-frame {
  width: 100%;
  font-size: 24px;
  margin-top: 10px;
  padding-top: 15px;
  border-top: 1px solid #f0c42d;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.dish-price {
  color: #f0c42d;
}
.slide-enter-active,
.slide-leave-active {
  transition: all 1s ease;
}
.slide-enter-from,
.slide-leave-to {
  opacity: 0;
  transform: translateY(20px);
}
.slide-enter-to,
.slide-leave-from {
  opacity: 1;
  transform: translateY(0);
}
.fade-enter-active,
.fade-leave-active {
  transition: all 1s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
.fade-enter-to,
.fade-leave-from {
  opacity: 1;
}
</style>
