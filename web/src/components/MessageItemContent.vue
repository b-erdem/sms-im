<template>
  <div class="message-content">
    <div class="message-content__item">
      <div class="message-content-header">
        <div class="name">{{ message.person }}</div>
        <div class="phone">{{ message.address }}</div>
        <img src="@/assets/img/message-more.png" alt />
      </div>
    </div>

    <div class="message-box">
      <div v-for="message in messages" :class="['message-box__item', message.type === '1' ? 'incoming' : 'outgoing']">
        <div class="name">{{ message.person || message.address }}</div>

        <div class="box-text">
          {{ message.body }}
          <div class="time">{{ new Date(Number(message.date)).toLocaleString() }}</div>
        </div>
      </div>
    </div>

    <div class="message-form">
      <input type="text" placeholder="Type your message here" v-on:keyup.enter="send" />
      <img src="@/assets/img/submit.png" alt />
    </div>
  </div>
</template>

<script>
export default {
  props: {
    messages: Array
  },
  data() {
    return {
      message: ""
    };
  }
};
</script>