<template>
  <div class="check-ele" role="dialog" aria-modal="true">
    <div class="title">是否確定登出?</div>
    <div class="btn-frame">
      <div class="no-btn" @click="$emit('cancel')">取消</div>
      <div class="yes-btn" @click="$emit('confirm')">確定</div>
    </div>
  </div>
</template>

<script>
export default {
  emits: ["cancel", "confirm"],
};
</script>

<style scoped>
.check-ele {
  width: 65%;
  background-color: #ffffff;
  border: 1px solid #f0c42d;
  border-radius: 20px;
  padding: 20px;
}
.title {
  font-size: 26px;
  margin-bottom: 10px;
}
.btn-frame {
  display: flex;
  justify-content: center;
  align-items: center;
}
.no-btn,
.yes-btn {
  width: 150px;
  height: 40px;
  font-size: 20px;
  border-radius: 20px;
  line-height: 40px;
  text-align: center;
  margin: 20px 20px 0px 20px;
  transition: all 0.3s ease;
}
.no-btn {
  background-color: #ffffff;
  border: 1px solid #f0c42d;
}
.yes-btn {
  color: #ffffff;
  background-color: #f0c42d;
}
.no-btn:hover,
.yes-btn:hover {
  color: #272727;
  background-color: #ffea9d;
  cursor: pointer;
  transform: scale(1.1);
}
.no-btn:hover {
  border: 1px solid #ffea9d;
}
</style>
