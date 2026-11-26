<template>
  <div class="check-page">
    <div class="title">訂單確認</div>
    <div class="order-frame">
      <div class="sec-title">訂單標號</div>
      <div class="order-info">{{ orderCode }}</div>
    </div>
    <div class="order-frame">
      <div class="sec-title">訂餐時間</div>
      <div class="order-info">{{ orderTime }}</div>
    </div>
    <div class="dish-outframe">
      <div v-for="buy in selectedOrders" :key="buy.id">
        <div class="dish-frame">
          <div class="dish-amount">x{{ buy.amount }}</div>
          <div class="dish-info">{{ buy.name }}</div>
          <div class="dish-price">${{ buy.total }}</div>
        </div>
      </div>
    </div>
    <div class="order-frame">
      <div class="sec-title">總計</div>
      <div class="total-price">${{ totalPrice }}</div>
    </div>
    <div class="btn" @click="sendOrders">確認送出</div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from "vue";

export default {
  props: {
    orders: {
      type: Array,
      default: () => [],
    },
  },
  emits: ["confirm"],
  setup(props, { emit }) {
    const orderTime = ref("");
    const orderCode = ref("");
    const totalPrice = computed(() =>
      props.orders.reduce((total, order) => total + order.total, 0)
    );

    const GetDate = () => {
      const date = new Date();

      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, "0");
      const day = String(date.getDate()).padStart(2, "0");

      const hours = String(date.getHours()).padStart(2, "0");
      const minutes = String(date.getMinutes()).padStart(2, "0");

      orderTime.value = `${year}-${month}-${day} ${hours}:${minutes}`;
      // orderTime.value = "2026-02-02 13:56";
    };

    const CreateOrderCode = () => {
      const date = new Date();

      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, "0");
      const day = String(date.getDate()).padStart(2, "0");

      const hours = String(date.getHours()).padStart(2, "0");
      const minutes = String(date.getMinutes()).padStart(2, "0");

      orderCode.value = "LSW" + year + month + day + hours + minutes;
    };

    const sendOrders = () => {
      emit("confirm", {
        total: totalPrice.value,
        code: orderCode.value,
        date: orderTime.value,
      });
    };

    onMounted(() => {
      GetDate();
      CreateOrderCode();
    });

    return {
      selectedOrders: props.orders,
      totalPrice,
      orderTime,
      orderCode,
      sendOrders,
    };
  },
};
</script>

<style scoped>
.check-page {
  width: 65%;
  background-color: #ffffff;
  border-radius: 20px;
  border: 1px solid #f0c42d;
  padding: 20px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}
.title {
  color: #f0c42d;
  font-size: 35px;
  font-weight: bold;
  margin: 20px 0;
}
.order-frame {
  width: 80%;
  font-size: 22px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.dish-outframe {
  width: 80%;
  margin: 20px 0;
}
.dish-frame {
  font-size: 20px;
  display: grid;
  grid-template-columns: 10% 80% 10%;
  justify-items: start;
}
.dish-amount,
.total-price {
  color: #f0c42d;
}
.dish-price {
  width: 100%;
  text-align: end;
}
.btn {
  width: 120px;
  height: 40px;
  color: #ffffff;
  font-size: 20px;
  text-align: center;
  line-height: 40px;
  background-color: #f0c42d;
  border-radius: 20px;
  margin-top: 20px;
  transition: all 0.3s ease;
}
.btn:hover {
  color: #272727;
  background-color: #ffea9d;
  cursor: pointer;
  transform: scale(1.1);
}
</style>
