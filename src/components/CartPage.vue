<template>
  <div class="cart-page">
    <transition name="fade">
      <div class="title" v-if="showFade">購物車</div>
    </transition>
    <div class="top-btn-frame">
      <div
        class="all-btn"
        @click="SelectAll"
        :class="{ active: selectAllClick }"
      >
        全部選取
      </div>
      <div class="clean-btn" @click="RemoveAllOrder">一鍵刪除</div>
    </div>
    <transition name="slide">
      <div class="box-frame" v-if="showSlide">
        <div
          class="dish-box-outframe"
          v-for="(order, index) in filteredData"
          :key="order._id"
        >
          <div class="dish-box">
            <img :src="order.image" />
            <div class="info-frame">
              <div class="dish-info">
                <div class="dish-name">{{ order.name }}</div>
                <div class="num-frame">
                  <div class="num-btn" @click="CountDishAmount('-', index)">
                    -
                  </div>
                  <div class="dish-amount">x{{ order.amount }}</div>
                  <div class="num-btn" @click="CountDishAmount('+', index)">
                    +
                  </div>
                </div>
              </div>
              <div class="dish-total-price">${{ order.total }}</div>
            </div>
            <div class="btn-frame">
              <input class="checkbox" type="checkbox" v-model="order.check" />
              <i
                class="fa-solid fa-trash-can"
                id="cancel-btn"
                @click="RemoveOrder(index)"
              ></i>
            </div>
          </div>
        </div>
      </div>
    </transition>
    <transition name="fade">
      <div class="check-frame" v-if="showFade">
        <div class="total-price">
          <span style="color: #272727">總計: </span>
          ${{ totalPrice }}
        </div>
        <div class="checkout-btn" @click="ClickBuy">結帳</div>
      </div>
    </transition>
  </div>

  <div class="overlay" v-show="showCheck" @click="showCheck = false"></div>
  <transition name="slide-check">
    <CheckOrder
      class="check-page"
      v-if="showCheck"
      :orders="buyList"
      @confirm="CompleteCheckout"
    />
  </transition>
</template>

<script>
import { ref, computed, onMounted } from "vue";
import CheckOrder from "./ele/CheckOrder.vue";
import { errorUiStore } from "@/store/error";

export default {
  name: "CartPage",
  components: {
    CheckOrder,
  },
  setup() {
    const errorStore = errorUiStore();
    const showFade = ref(false);
    const showSlide = ref(false);
    const filteredData = ref([]);
    const buyList = ref([]);
    const showCheck = ref(false);
    const totalPrice = computed(() =>
      filteredData.value
        .filter((order) => order.check)
        .reduce((total, order) => total + order.total, 0)
    );
    const selectAllClick = computed(
      () =>
        filteredData.value.length > 0 &&
        filteredData.value.every((order) => order.check)
    );

    const CountDishAmount = (operator, index) => {
      const order = filteredData.value[index];

      if (operator === "+") {
        order.amount += 1;
      } else if (operator === "-" && order.amount > 1) {
        order.amount -= 1;
      }

      order.total = order.price * order.amount;
      updateOrder(order);
    };

    const email = () => localStorage.getItem("userEmail");

    const request = async (url, options = {}) => {
      const response = await fetch(url, options);
      const result = await response.json();
      if (!response.ok) {
        throw new Error(result.message || "伺服器請求失敗。");
      }
      return result;
    };

    const GetOrderData = async () => {
      const userEmail = email();
      if (!userEmail) {
        filteredData.value = [];
        return;
      }
      try {
        const orders = await request(
          `/api/get-orders?email=${encodeURIComponent(userEmail)}`
        );
        filteredData.value = orders.map((order) => ({
          ...order,
          amount: Math.max(1, Number(order.amount) || 1),
          price: Number(order.price) || 0,
          check: false,
          total:
            Number(order.price || 0) * Math.max(1, Number(order.amount) || 1),
        }));
      } catch (error) {
        errorStore.SetError(error.message || "購物車載入失敗。");
        errorStore.CloseEle();
        filteredData.value = [];
      }
    };

    const updateOrder = async (order) => {
      try {
        await request("/api/update-order", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            email: email(),
            id: order._id,
            amount: order.amount,
          }),
        });
      } catch (error) {
        errorStore.SetError(error.message || "餐點數量更新失敗。");
        errorStore.CloseEle();
      }
    };

    const ClickBuy = () => {
      buyList.value = filteredData.value.filter(
        (order) => order.check === true
      );

      if (!email()) {
        errorStore.SetError("請先登入，再進行結帳。");
        errorStore.CloseEle();
      } else if (buyList.value.length > 0) {
        showCheck.value = true;
      } else {
        errorStore.SetError("請先選擇要結帳的餐點。");
        errorStore.CloseEle();
      }
    };

    const RemoveOrder = async (index) => {
      const order = filteredData.value[index];
      try {
        await request("/api/remove-orders", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ email: email(), list: [order._id] }),
        });
        filteredData.value.splice(index, 1);
      } catch (error) {
        errorStore.SetError(error.message || "餐點刪除失敗。");
        errorStore.CloseEle();
      }
    };

    const RemoveAllOrder = async () => {
      if (!filteredData.value.length) return;
      try {
        await request("/api/remove-orders", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            email: email(),
            list: filteredData.value.map((order) => order._id),
          }),
        });
        filteredData.value = [];
        buyList.value = [];
      } catch (error) {
        errorStore.SetError(error.message || "清空購物車失敗。");
        errorStore.CloseEle();
      }
    };

    const SelectAll = () => {
      const shouldSelectAll = !selectAllClick.value;
      filteredData.value.forEach((item) => (item.check = shouldSelectAll));
    };

    const CompleteCheckout = async (checkout) => {
      const purchasedIds = new Set(buyList.value.map((order) => order._id));
      const orderIds = buyList.value.map((order) => order._id);
      try {
        await request("/api/send-buy-orders", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            email: email(),
            list: buyList.value,
            total: checkout.total,
            code: checkout.code,
            date: checkout.date,
            orderIds,
          }),
        });
        filteredData.value = filteredData.value.filter(
          (order) => !purchasedIds.has(order._id)
        );
        buyList.value = [];
        showCheck.value = false;
        errorStore.SetSuccess("訂單已送出!");
        errorStore.CloseEle();
      } catch (error) {
        errorStore.SetError(error.message || "訂單送出失敗。");
        errorStore.CloseEle();
      }
    };

    onMounted(() => {
      showFade.value = true;
      showSlide.value = true;

      GetOrderData();
    });

    return {
      showCheck,
      showFade,
      showSlide,
      filteredData,
      buyList,
      totalPrice,
      selectAllClick,
      CountDishAmount,
      GetOrderData,
      ClickBuy,
      CompleteCheckout,
      RemoveOrder,
      RemoveAllOrder,
      SelectAll,
    };
  },
};
</script>

<style scoped>
.cart-page {
  height: 100%;
  padding-bottom: 100px;
  display: flex;
  flex-direction: column;
  justify-content: start;
  align-items: center;
  position: relative;
}
.title {
  color: #f0c42d;
  font-size: 35px;
  font-weight: bold;
  margin: 20px 0;
  margin-bottom: 50px;
}
.top-btn-frame {
  display: flex;
  justify-content: center;
  align-items: center;
  position: absolute;
  top: 80px;
  right: calc(10% - 20px);
}
.all-btn,
.clean-btn {
  width: 100px;
  height: 40px;
  color: #ffffff;
  font-size: 20px;
  text-align: center;
  line-height: 40px;
  background-color: #f0c42d;
  border-radius: 20px;
  transition: all 0.3s ease;
}
.all-btn {
  margin-right: 20px;
}
.all-btn:hover,
.clean-btn:hover {
  color: #272727;
  background-color: #ffea9d;
  cursor: pointer;
}
.active {
  color: #272727;
  background-color: #ffea9d;
}
.box-frame,
.dish-box-outframe {
  width: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}
.dish-box {
  width: 80%;
  background-color: #ffffff;
  border: 1px solid #f0c42d;
  border-radius: 20px;
  margin: 20px;
  padding: 20px;
  display: flex;
  justify-content: start;
  align-items: center;
}
img {
  width: 120px;
}
.info-frame {
  width: 100%;
  border-right: 1px solid #f0c42d;
  border-left: 1px solid #f0c42d;
  margin: 0 20px;
  padding: 0 20px;
  display: flex;
  flex-direction: column;
  justify-content: start;
  align-items: center;
}
.dish-info {
  width: 100%;
  font-size: 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.num-frame {
  font-size: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
}
.num-btn {
  width: 30px;
  height: 30px;
  text-align: center;
  line-height: 30px;
  border: 1px solid #f0c42d;
  border-radius: 20px;
  transition: all 0.3s ease;
}
.num-btn:hover {
  background-color: #f0c42d;
  cursor: pointer;
}
.dish-amount {
  margin: 0 20px;
}
.dish-total-price {
  width: 100%;
  color: #f0c42d;
  font-size: 26px;
  font-weight: bold;
  text-align: end;
  margin-top: 20px;
}
.checkbox {
  appearance: none; /* 移除預設樣式 */
  width: 25px;
  height: 25px;
  border: 1px solid #f0c42d;
  border-radius: 5px;
  cursor: pointer;
}
.checkbox:checked {
  color: #ffffff;
  background-color: #f0c42d;
}
#cancel-btn {
  color: #f0c42d;
  font-size: 26px;
  margin-top: 15px;
}
#cancel-btn:hover {
  cursor: pointer;
}
.check-frame {
  width: 100%;
  background-color: #ffffff;
  border-top: 4px solid #ffea9d;
  padding: 20px;
  display: flex;
  justify-content: end;
  align-items: center;
  position: fixed;
  bottom: 0;
}
.total-price {
  height: 40px;
  color: #f0c42d;
  font-size: 28px;
  font-weight: bold;
  line-height: 40px;
  margin-right: 40px;
}
.checkout-btn {
  width: 100px;
  height: 40px;
  color: #ffffff;
  font-size: 20px;
  text-align: center;
  line-height: 40px;
  background-color: #f0c42d;
  border-radius: 20px;
  margin-right: 50px;
  transition: all 0.3s ease;
}
.checkout-btn:hover {
  color: #272727;
  background-color: #ffea9d;
  cursor: pointer;
  transform: scale(1.1);
}
#help-btn {
  font-size: 26px;
  color: #f0c42d;
  position: fixed;
  bottom: 20px;
  left: 20px;
  z-index: 1;
  cursor: pointer;
}

.check-page,
.order-guide,
.cart-guide {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 99;
}
.overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-color: rgba(0, 0, 0, 0.4);
  z-index: 98;
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
.slide-check-enter-active,
.slide-check-leave-active {
  transition: all 1s ease;
}
.slide-check-enter-from,
.slide-check-leave-to {
  opacity: 0;
  transform: translate(-50%, -50%) translateY(20px);
}
.slide-check-enter-to,
.slide-check-leave-from {
  opacity: 1;
  transform: translate(-50%, -50%) translateY(0);
}
.slide-guide-enter-active,
.slide-guide-leave-active {
  transition: all 1s ease;
}
.slide-guide-enter-from,
.slide-guide-leave-to {
  opacity: 0;
  transform: translateX(-100%) translate(-50%, -50%);
}
.slide-guide-enter-to,
.slide-guide-leave-from {
  opacity: 1;
  transform: translateX(0) translate(-50%, -50%);
}
</style>
