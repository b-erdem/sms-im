<template>
  <a href="#" class="messages__item">
    <div class="name">{{ snippet.person }}</div>
    <div class="date">1h ago</div>

    <div
      class="content"
    >{{ snippet.snippet }}</div>
  </a>
</template>

<script>
export default {
  props: {
    snippet: Object
  }
}
</script>
