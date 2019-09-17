<template>
  <div class="message-item" :class="{ '-unread' : Number(snippet.read) === 0 }">
    <div class="message-item__header">
      <div class="name">{{ snippet.person }}</div>
      <div class="date">1h ago</div>
    </div>
    <div class="content" >{{ snippet.snippet }}</div>
  </div>
</template>

<script>
export default {
  props: {
    snippet: Object
  }
}
</script>
