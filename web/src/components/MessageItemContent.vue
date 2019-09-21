<template>
  <div class="message-content">
    <div class="message-content__item">
      <div class="message-content-header">
        <div class="name" v-if="info.person">{{ info.person }}</div>
        <div class="phone">{{ messages[0].address }}</div>
        <img src="@/assets/img/message-more.png" alt />
      </div>
    </div>

    <div class="message-box" ref="messageBox">
      <div class="message-box__wrapper">
        <button @click="seeMore" class="message-box__more">See More</button>
        <div v-for="(message, $index) in messages" :class="['message-box__item', message.type === '1' ? 'incoming' : 'outgoing']" :key="$index">
          <div class="name" v-if="message.type === '1'">{{ info.person || message.address }}</div>
          <div class="box-text">
            {{ message.body }}
          </div>
          <div class="time">{{ new Date(Number(message.date)).toLocaleString() }}</div>
        </div>
      </div>
    </div>

    <div class="message-form">
      <input v-model="msg" type="text" placeholder="Type your message here" v-on:keyup.enter="sendMessage" />
      <img src="@/assets/img/submit.png" alt @click="sendMessage"/>
    </div>
  </div>
</template>

<script>
export default {
  props: {
    messages: Array,
    info: Object
  },
  data () {
    return {
      msg: '',
      currentScrollPosition: null,
      isMessageSended: false
    }
  },
  mounted() {
    console.log('mounted', this.isMessageSended);
  },
  methods: {
    sendMessage () {
      this.$emit('onSend', this.msg, this.messages[0].address)
      this.msg = ''
      document.querySelector('.message-box').scrollTo(0, document.querySelector('.message-box__wrapper').offsetHeight)
      this.isMessageSended = true
    },
    seeMore () {
      this.currentScrollPosition = document.querySelector('.message-box__wrapper').offsetHeight
      this.$emit('onSeeMoreMessages', this.info.thread_id, this.messages.length)
    }
  },
  watch: {
    messages () {
      this.$nextTick(() => {
      console.log('watch', this.isMessageSended);
        if (!this.isMessageSended) {
          let scrollPosition = document.querySelector('.message-box__wrapper').offsetHeight - this.currentScrollPosition
          document.querySelector('.message-box').scrollTo(0, scrollPosition)
          this.currentScrollPosition = document.querySelector('.message-box__wrapper').offsetHeight
        } else {
          document.querySelector('.message-box').scrollTo(0, document.querySelector('.message-box__wrapper').offsetHeight)
        }
        this.isMessageSended = false
      })
    }
  }
}
</script>
