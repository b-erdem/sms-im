<template>
  <div id="app">
    <div class="wrapper">
      <side-bar />
      <div class="messages">
        <search-bar />
        <message-list-item v-on:click.native="setActiveConversation(snippet)" v-for="snippet in conversationSnippets" :snippet="snippet" :key="snippet.sender" />
      </div>
      <message-item-content @onSend="send" :messages="activeConversation" />
    </div>
  </div>
</template>

<script>
import { Socket } from "phoenix"
import SearchBar from "@/components/SearchBar"
import SideBar from "@/components/SideBar"
import MessageListItem from "@/components/MessageListItem"
import MessageItemContent from "@/components/MessageItemContent"

export default {
  name: 'app',
  components: {
    SideBar,
    SearchBar,
    MessageListItem,
    MessageItemContent
  },
  data () {
    return {
      messages: [],
      msg: "",
      socket: null,
      channels: [],
      channel: null,
      conversations: {},
      conversationSnippets: [],
      activeConversation: null,
    }
  },

  created () {
    const socket = new Socket("ws://104.248.20.26:4000/socket", {})
    socket.onOpen(event => console.log("connected"))
    socket.onError(event => console.log("cannot connect"))
    socket.onClose(event => console.log("socket closed"))
    socket.connect({})

    const channel = socket.channel("room:lobby")
    channel.on("new_msg", msg => {
      console.log("got message ", msg)
      this.messages.push(msg.message)
    })
    channel.on("last_10", conversations => {
      console.log("received last 10 messages ", conversations)
        this.conversations = conversations
        this.activeConversation = this.conversations[Object.keys(this.conversations)[0]].sort((a, b) => a.date - b.date)
        this.conversationSnippets = Object.keys(conversations).map(conv => {
          return {sender: conversations[conv][0].person, address: conversations[conv][0].address, snippet: conversations[conv][0].body}
        })
    })
    channel.join()
    .receive("ok", ({ messages }) => {
       console.log("catching up ", messages)
       this.last10Messages()
    })
    .receive("error", ({ reason }) => console.log("failed join", reason))
    .receive("timeout", () => console.log("networking issue"))
    this.socket = socket
    this.channel = channel
  },

  methods: {
    last10Messages () {
      this.channel.push("last_10_messages", {}, 10000)
      .receive("ok", (conversations) => {
        console.log("last_10_messages ", conversations)
      })
    },
    send (message, address) {
      console.log("send message: ", message, address)
      this.channel.push("send_sms", { message: message, to: address }, 10000)
      .receive("ok", (msg) => {
        console.log("created message ", msg)
        this.messages.push(msg.message)
      })
      .receive("error", ({reason}) => console.log("failed join", reason) )
      .receive("timeout", () => console.log("Networking issue. Still waiting..."))
    },

    setActiveConversation(snippet) {
      console.log("snippet ", snippet)
      this.activeConversation = this.conversations[snippet.address].sort((a, b) => a.date - b.date)
    }
  }
}
</script>

<style lang="scss">
@import'~bootstrap/dist/css/bootstrap.css';
@import "./assets/css/style.scss";
#app {
  font-family: 'Avenir', Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  margin-top: 60px;
}
</style>
