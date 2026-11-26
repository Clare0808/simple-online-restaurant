<template>
  <div class="user-page">
    <transition name="fade">
      <div class="title" v-if="showFade">使用者中心</div>
    </transition>
    <transition name="slide">
      <div class="info-frame" v-if="showSlide">
        <div class="text-frame">
          <div class="func-outframe">
            <div class="user-info-frame">
              <div class="img-frame">
                <img src="@/assets/images/user.jpg" />
              </div>
              <div class="user-info">{{ userName }} 您好!</div>
              <div class="user-info">
                {{ userNumber }}
              </div>
            </div>
            <div class="func-frame">
              <div class="sec-title">訂餐紀錄</div>
              <div class="non-content" v-if="showNonContent">尚無訂單紀錄</div>
              <div class="order-outframe">
                <div v-for="order in filteredData" :key="order._id">
                  <div class="order-frame">
                    <div class="order-code">{{ order.code }}</div>
                    <div class="order-date">{{ order.date }}</div>
                    <div
                      class="order-info-frame"
                      v-for="dish in order.list"
                      :key="dish._id || dish.name"
                    >
                      <div class="order-info">
                        <div class="order-dish">{{ dish.name }}</div>
                        <div class="order-amount">x{{ dish.amount }}</div>
                      </div>
                    </div>
                    <div class="order-price">${{ order.total }}</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script>
import { ref, onMounted } from "vue";
import { errorUiStore } from "@/store/error";

export default {
  name: "UserPage",
  setup() {
    const errorStore = errorUiStore();
    const showFade = ref(false);
    const showSlide = ref(false);
    const filteredData = ref([]);
    const userName = ref("");
    const userNumber = ref("");
    const showNonContent = ref(false);
    const isAdmin = ref(false);

    const GetOrderData = async () => {
      const userMail = localStorage.getItem("userEmail");
      if (!userMail) {
        filteredData.value = [];
        showNonContent.value = true;
        return;
      }

      const response = await fetch(
        `/api/get-buy-orders?email=${encodeURIComponent(userMail)}`
      );
      const data = await response.json();
      if (!response.ok) throw new Error(data.message || "訂單紀錄載入失敗。");

      filteredData.value = data;

      if (filteredData.value.length === 0) {
        showNonContent.value = true;
      }
    };

    const GetUserInfo = async () => {
      const userMail = localStorage.getItem("userEmail");
      if (!userMail) {
        userName.value = "訪客";
        userNumber.value = "請先登入";
        isAdmin.value = false;
        return;
      }

      const response = await fetch(
        `/api/send-data?email=${encodeURIComponent(userMail)}`
      );
      const data = await response.json();
      if (!response.ok) throw new Error(data.message || "使用者資料載入失敗。");

      userName.value = data.name;
      userNumber.value = data.number;
      isAdmin.value = data.role === "admin";

      if (!userNumber.value) {
        userNumber.value = "電話尚未填入";
      }
    };

    onMounted(async () => {
      showFade.value = true;
      showSlide.value = true;

      try {
        await Promise.all([GetOrderData(), GetUserInfo()]);
      } catch (error) {
        errorStore.SetError(error.message || "使用者資料載入失敗。");
        errorStore.CloseEle();
      }
    });

    return {
      showFade,
      showSlide,
      filteredData,
      userName,
      userNumber,
      showNonContent,
      isAdmin,
      GetOrderData,
      GetUserInfo,
    };
  },
};
</script>

<style scoped>
.user-page {
  height: 80vh;
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
.info-frame {
  width: 80%;
  text-align: start;
  display: flex;
  justify-content: center;
  align-items: center;
}
.img-frame {
  position: relative;
}
img {
  width: 200px;
  height: 200px;
  border-bottom: 1px solid #f0c42d;
  margin-bottom: 10px;
  padding-bottom: 10px;
}
#pencil {
  width: 30px;
  height: 30px;
  color: #f0c42d;
  font-size: 16px;
  background-color: #ffffff;
  border-radius: 50%;
  text-align: center;
  line-height: 30px;
  position: absolute;
  bottom: 50px;
  right: 10px;
}
#pencil:hover {
  color: #272727;
  background-color: #ffea9d;
  cursor: pointer;
}
.user-info-frame {
  font-size: 22px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}
.user-info {
  width: 100%;
  margin: 10px 0;
  text-align: center;
}
.user-info i {
  color: #f0c42d;
  font-size: 18px;
  margin-left: 20px;
}
.user-info i:hover {
  color: #272727;
  cursor: pointer;
}
.btn {
  width: 150px;
  height: 40px;
  color: #ffffff;
  background-color: #f0c42d;
  font-size: 20px;
  border-radius: 20px;
  line-height: 40px;
  text-align: center;
  margin-top: 10px;
  transition: all 0.3s ease;
}
.btn:hover {
  color: #272727;
  background-color: #ffea9d;
  cursor: pointer;
}
.text-frame {
  width: 100%;
  background-color: #ffffff;
  border: 1px solid #f0c42d;
  border-radius: 20px;
  padding: 20px;
}
.func-outframe {
  width: 100%;
  display: grid;
  grid-template-columns: 30% 70%;
  gap: 20px;
  justify-content: center;
  align-items: start;
}
.sec-title {
  color: #f0c42d;
  font-size: 22px;
  margin-bottom: 10px;
}
.non-content {
  font-size: 20px;
  text-align: center;
  margin-top: 50px;
}
.order-outframe {
  max-height: 370px;
  overflow-y: auto;
}
.order-frame {
  margin-top: 10px;
  padding: 10px;
}
.order-date {
  color: #9d9d9d;
}
.order-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.order-price {
  color: #f0c42d;
}
.order-price {
  text-align: end;
}

.overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-color: rgba(0, 0, 0, 0.4);
  z-index: 1;
}
.modify-page {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 2;
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
.slide-modify-enter-active,
.slide-modify-leave-active {
  transition: all 1s ease;
}
.slide-modify-enter-from,
.slide-modify-leave-to {
  opacity: 0;
  transform: translate(-50%, -50%) translateY(20px);
}
.slide-modify-enter-to,
.slide-modify-leave-from {
  opacity: 1;
  transform: translate(-50%, -50%) translateY(0);
}
</style>
