<template>
  <div class="qr-box">
    <div class="qr-box-img">
      <div class="qr-box-img__svg" v-if="svg" v-html="svg"></div>
    </div>
    <div class="qr-box__title">Scan QR Code</div>
    <div class="qr-box__text">
      Align the QR code within the frame to scan.
      <!-- for app: "Place the code inside the frame" -->
    </div>
    <a href="https://github.com/b-erdem/sms-im/raw/master/resources/apk/app-debug.apk" download class="qr-box__button">Get APK</a>
  </div>
</template>

<script>
export default {
  name: 'QrBox',
  props: {
    svg: String
  }
}
</script>

<style lang="scss" scoped>
.qr-box {
  width: 560px;
  margin: 160px auto;
  background-image: linear-gradient(90deg, #117ee7 0%, #108ef2 100%);
  box-shadow: 0px 12px 30px 0px rgba(1, 50, 101, 0.3);
  border-radius: 10px;
  padding: 25px;
}

.qr-box-img {
  width: 315px;
  height: 315px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 10px auto 25px;
  border-radius: 10px;
  box-shadow: 0px 12px 30px 0px rgba(1, 50, 101, 0.3);
}

.qr-box__title {
  font-size: 25px;
  font-weight: 700;
  color: #fff;
  text-align: center;
}

.qr-box__text {
  color: #fff;
  font-size: 18px;
  padding: 10px 25px;
  font-weight: 500;
  opacity: 0.75;
  text-align: center;
}

.qr-box__button {
  background: #fff;
  display: flex;
  padding: 10px 25px;
  border-radius: 5px;
  box-shadow: 0px 12px 30px 0px rgba(1, 50, 101, 0.3);
  text-decoration: none;
  color: #535357;
  font-size: 18px;
  font-weight: 500;
  margin: 25px auto 10px;
  width: 260px;
  text-align: center;
  justify-content: center;
  align-items: center;
  min-height: 50px;
}
</style>
