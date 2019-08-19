<template>
  <div id="app">
    <side-bar />
    <div class="messages">
      <search-bar />
      <message-list-item v-for="snippet in conversationSnippets" :snippet="snippet" :key="snippet.sender" />
    </div>
    <message-item-content :messages="activeConversation" />
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
    const socket = new Socket("ws://localhost:4000/socket", {})
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
          return {sender: conv, snippet: conversations[conv][0].body}
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
        console.log("last_10_messages")
      })
    },
    send () {
      console.log("send message: ", this.msg)
      this.channel.push("new_msg", { message: this.msg, to: "6505551212" }, 10000)
      .receive("ok", (msg) => {
        console.log("created message ", msg)
        this.messages.push(msg.message)
      })
      .receive("error", ({reason}) => console.log("failed join", reason) )
      .receive("timeout", () => console.log("Networking issue. Still waiting..."))
    }
  }
}
</script>

<style>
@import'~bootstrap/dist/css/bootstrap.css';
@import "./assets/css/style.css";
#app {
  font-family: 'Avenir', Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  margin-top: 60px;
}
</style>
