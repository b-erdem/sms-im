<template>
  <div class="message-content">
    <div class="message-content__item">
      <div class="message-content-header">
        <div class="name" v-if="person">{{ person }}</div>
        <div class="phone">{{ messages[0].address }}</div>
        <img src="@/assets/img/message-more.png" alt />
      </div>
    </div>

    <div class="message-box" ref="messageBox">
      <div class="message-box__wrapper">
        <button class="message-box__more">Daha fazla yükle</button>
        <div v-for="message in messages" :class="['message-box__item', message.type === '1' ? 'incoming' : 'outgoing']">
          <div class="name" v-if="message.type === '1'">{{ person || message.address }}</div>
          <div class="box-text">
            {{ message.body }}
          </div>
          <div class="time">{{ new Date(Number(message.date)).toLocaleString() }}</div>
        </div>
      </div>
    </div>

    <div class="message-form">
      <input v-model="msg" type="text" placeholder="Type your message here" v-on:keyup.enter="$emit('onSend', msg, messages[0].address); msg= ''" />
      <img src="@/assets/img/submit.png" alt />
    </div>
  </div>
</template>

<script>
export default {
  props: {
    messages: Array,
    person: String
  },
  data () {
    return {
      msg: ''
    }
  }
}
</script>
