<template>
  <div class="-align-self-stretch">
    <div class="overlay" :class="{ '-active' : isMenuShow }" @click="closeMenu"></div>
    <div class="sidebar" :class="{ '-active' : isMenuShow }">
      <!-- <div class="profile-img">
      <img src="@/assets/img/profile.jpg" alt />
      </div>-->
      <div class="sidebar-toggle" :class="{ '-active' : isMenuShow }" @click="toggleMenu">
        <span></span>
        <span></span>
        <span></span>
      </div>
      <ul class="sidebar-menu">
        <li class="sidebar-menu__item">
          <a href="#" class="sidebar-menu__link active">
            <img src="@/assets/img/inbox.png" alt />
            <span class="sidebar-menu__text">Menu 1</span>
          </a>
        </li>
        <li class="sidebar-menu__item">
          <a href="#" class="sidebar-menu__link">
            <img src="@/assets/img/send.png" alt />
            <span class="sidebar-menu__text">Menu 2</span>
          </a>
        </li>
        <li class="sidebar-menu__item">
          <a href="#" class="sidebar-menu__link">
            <img src="@/assets/img/draft.png" alt />
            <span class="sidebar-menu__text">Menu 3</span>
          </a>
        </li>
        <li class="sidebar-menu__item">
          <a href="#" class="sidebar-menu__link">
            <img src="@/assets/img/trash.png" alt />
            <span class="sidebar-menu__text">Menu 4</span>
          </a>
        </li>
      </ul>

      <a href="#" class="btn-menu">
        <img src="@/assets/img/menu.png" alt />
      </a>
    </div>
  </div>
</template>
<script>
export default {
  name: 'Sidebar',
  data () {
    return {
      isMenuShow: false
    }
  },
  methods: {
    toggleMenu () {
      this.isMenuShow = !this.isMenuShow
    },
    closeMenu () {
      this.isMenuShow = false
    }
  }
}
</script>
<style lang="scss" scoped>
.overlay {
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  background: rgba(186, 193, 212, 0.65);
  z-index: 3;
  opacity: 0;
  pointer-events: none;
  transition: all .3s ease-in-out;

  &.-active {
    opacity: 1;
    pointer-events: all;
  }
}
</style>
