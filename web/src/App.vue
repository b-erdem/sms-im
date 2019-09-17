<template>
  <div id="app">
    <QrBox :svg="svg" v-if="!isLoggedIn"/>
    <div class="wrapper" v-if="isLoggedIn">
      <side-bar />
      <div class="messages">
        <search-bar />
        <div class="messages__list">
          <message-list-item
            :class="{ '-active' : activeConversationIndex === $index }"
            v-on:click.native="setActiveConversation($index)" v-for="(snippet, $index) in conversationSnippets" :snippet="snippet" :key="snippet.person" />
          <button @click="recentConversations(conversations.length)" class="message-box__more">See More</button>
        </div>
      </div>
      <message-item-content
        :is-reach-top="isReachTop"
        v-if="conversations.length > 0"
        ref="messageContent"
        @onSend="send"
        @onSeeMoreMessages="seeMoreMessages"
        :messages="conversations[activeConversationIndex].messages"
        :info="conversations[activeConversationIndex].info"
      />
    </div>
    <a href="https://github.com/b-erdem/sms-im" target="_blank" class="github-btn">
      <img src="@/assets/img/github.svg" alt="" class="github-btn__icon">
        See on GitHub
    </a>
  </div>
</template>

<script>
import { Socket, Presence } from 'phoenix'
import SearchBar from '@/components/SearchBar'
import SideBar from '@/components/SideBar'
import MessageListItem from '@/components/MessageListItem'
import MessageItemContent from '@/components/MessageItemContent'
import QrBox from '@/components/QrBox'

import Bowser from 'bowser'

export default {
  name: 'app',
  components: {
    SideBar,
    SearchBar,
    MessageListItem,
    MessageItemContent,
    QrBox
  },
  data () {
    return {
      socket: null,
      channel: null,
      conversations: [],
      activeConversationIndex: 0,
      svg: '',
      isLoggedIn: false,
      isReachTop: false
    }
  },

  created () {
    fetch('https://websms-backend.erdem.dev:8443/api/auth/generate_qr_code')
      .then(resp => resp.json())
      .then(data => {
        this.svg = data.qr_code_svg
        this.connect(data)
      })

    Notification.requestPermission().then(function(result) {
      console.log(result);
    });
  },
  watch: {
    activeConversation () {
      this.setScrollPosition()
    }
  },

  computed: {
    conversationSnippets () {
      if (!this.conversations) return []
      this.conversations.sort((a, b) => b.info.date - a.info.date)
      return this.conversations.map(c => c.info)
    }
  },

  methods: {
    getBrowserInfo () {
      const browser = Bowser.parse(window.navigator.userAgent)
      console.log('browser ', `${browser.browser.name} ${browser.browser.version} ${browser.os.name} ${browser.os.versionName} ${browser.platform.type}`)
      return `${browser.browser.name} ${browser.browser.version} ${browser.os.name} ${browser.os.versionName} ${browser.platform.type}`
    },
    connect (params) {
      const device = this.getBrowserInfo()

      const socket = new Socket('wss://websms-backend.erdem.dev:8443/socket', {})
      socket.onOpen(event => console.log('connected'))
      socket.onError(event => console.log('cannot connect'))
      socket.onClose(event => console.log('socket closed'))
      socket.connect()

      const channel = socket.channel('room:' + params.channel_id, { device: device })
      const presence = new Presence(channel)
      presence.onSync(() => {
        console.log('presence sync ', presence.list())
      })
      presence.onJoin((id, current, newPres) => {
        if (!current) {
          console.log('user has entered for the first time', newPres)
        } else {
          console.log('user additional presence', newPres)
        }
      })

      // detect if user has left from all tabs/devices, or is still present
      presence.onLeave((id, current, leftPres) => {
        if (current.metas.length === 0) {
          console.log('user has left from all devices', leftPres)
        } else {
          console.log('user left from a device', leftPres)
        }
      })

      channel.on('new_msg', msg => {
        console.log('got message ', msg)
        this.sendNotification(msg.body, msg.from)
        let conversationIndex = this.conversations.findIndex(conversation => msg.from === conversation.messages[0].address)
        if (conversationIndex === -1) {
          let newConversation = { info: { date: msg.timestamp, snippet: msg.body }, messages: [] }
          newConversation.messages.push({ body: msg.body, type: '1', address: msg.from, date: msg.timestamp })
        }
        this.conversations[conversationIndex].messages.push({ body: msg.body, type: '1', address: msg.from, date: msg.timestamp })
      })
      channel.on('recent_conversations', data => {
        let conversations = data.conversations
        console.log('received recent_conversations ', conversations)
        this.conversations = this.conversations.concat(conversations)
        this.setActiveConversation(this.activeConversationIndex)
      })
      channel.on("more_messages", messages => {
        let threadId = Object.keys(messages)[0]
        let conversationIndex = this.conversations.findIndex(conversation => conversation.info.thread_id === threadId)
        this.conversations[conversationIndex].messages = messages[threadId].concat(this.conversations[conversationIndex].messages)
        this.conversations[conversationIndex].messages.sort((a, b) => a.date - b.date)
      })
      channel.on('user_entered', data => {
        console.log('user_entered', data)
        if (data.mobile) {
          this.isLoggedIn = true
          this.recentConversations()
        }
      })

      channel.join()
        .receive('ok', ({ messages }) => {
          console.log('joined')
        })
        .receive('error', ({ reason }) => console.log('failed join', reason))
        .receive('timeout', () => console.log('networking issue'))
      this.socket = socket
      this.channel = channel
    },
    recentConversations (position = 0) {
      this.channel.push('recent_conversations', {position: position}, 10000)
        .receive('ok', (msg) => console.log('push recent_conversations', msg))
    },
    send (message, address) {
      this.channel.push('send_sms', { message: message, to: address }, 10000)
        .receive('ok', ({ messages }) => {
          console.log('push send_sms ', messages)
          let now = Date.now().toString()
          this.conversations[this.activeConversationIndex].info.date = now
          this.conversations[this.activeConversationIndex].messages.push({ address: address, body: message, date: now, read: '0' })
          this.setScrollPosition()
        })
        .receive('error', ({ reason }) => console.log('failed send', reason))
        .receive('timeout', () => console.log('Networking issue. Still waiting...'))
    },
    setActiveConversation (index) {
      this.conversations[index].messages.sort((a, b) => a.date - b.date)
      this.activeConversationIndex = index
      this.setScrollPosition()
    },
    setScrollPosition () {
      this.$nextTick(() => {
        if (document.querySelector('.message-box__wrapper')) {
          document.querySelector('.message-box').scrollTo(0, document.querySelector('.message-box__wrapper').offsetHeight)
        }
      });
    },
    seeMoreMessages (threadId, position) {
      this.channel.push("more_messages", { threadId: threadId, position: position }, 10000)
        .receive('ok', (messages) => {
          console.log("more_messages ", "ok")
        })
    },
    sendNotification(body, title) {
      if (!("Notification" in window)) {
        alert("This browser does not support desktop notification");
      }
      else if (Notification.permission === "granted") {
        this.spawnNotification(body, title)
      }
      else if (Notification.permission !== "denied") {
        Notification.requestPermission().then(function (permission) {
          if (permission === "granted") {
            this.spawnNotification(body, title)
          }
        });
      }
    },
    spawnNotification(body, title) {
      var options = {
          body: body,
          // icon: icon
      };
      var n = new Notification(title, options);
    }
  }
}
</script>

<style lang="scss">
@import "./assets/scss/style.scss";
</style>
