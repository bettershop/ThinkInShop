<template>
  <div class="container">
    <div class="main">
      <div class="header">
        <img src="../assets/imgs/log2x.png" alt="">
      </div>
      <p class="title">系统维护</p>
      <p class="date">{{ systemMsgStartDate }} ~ {{ systemMsgEndDate }}</p>
      <div class="content">
        <div v-html="content"></div>
      </div>
      <div class="footer">
        <img src="../assets/imgs/weihu_bj.jpg" alt="">
      </div>
    </div>
    <el-button class="bgColor to-login" @click="toLogin">登录</el-button>
  </div>
</template>

<script>
import {getStorage} from '@/utils/storage'

export default {
  name: 'systemMaintenance',

  data() {
    return {

    }
  },

  computed: {
    content() {
      return getStorage('laike_admin_userInfo').systemMsg
    },
    systemMsgStartDate() {
      return getStorage('laike_admin_userInfo').systemMsgStartDate
    },
    systemMsgEndDate() {
      return getStorage('laike_admin_userInfo').systemMsgEndDate
    }
  },

  methods: {
    toLogin() {
      let type = getStorage('rolesInfo').type
      if(type == 0) {
        window.sessionStorage.clear()
        this.$router.push({
          path: '/login'
        })
        // window.location.href = '/login'
        // location.reload();
      } else {
        window.sessionStorage.clear()
        this.$router.push({
          path: '/mallLogin'
        })
        // window.location.href = '/mallLogin'
        // location.reload();
      }
    }
  }
}
</script>

<style scoped lang="less">
.container {
  width: 100%;
  height: 100%;
  background: #ffffff;
  position: relative;
  .main {
    width: 800px;
    height: 100%;
    margin: 0 auto;
    padding-top: 2%;
    display: flex;
    flex-direction: column;
    align-items: center;
    .header {
      display: flex;
      justify-content: center;
      margin-bottom: 5%;
      img {
        width: 145px;
        height: 48px;
      }
    }
    .title {
      text-align: center;
      font-size: 24px;
      color: #414658;
    }
    .date {
      font-size: 16px;
      color: red;
    }
    .content {
      height: 50%;
      width: 80%;
      margin: 0 auto;
      overflow: hidden;
      /deep/p {
        img {
          width: 100px !important;
          height: 100px !important;
        }
      }

    }
    .footer {
      width: 100%;
      flex: 1;
      display: flex;
      justify-content: center;
      align-items: center;
      img {
        width: 80%;
        height: 80%;
      }
    }
  }
  .to-login {
    position: absolute;
    top: 2%;
    right: 2%;
  }
}
</style>
