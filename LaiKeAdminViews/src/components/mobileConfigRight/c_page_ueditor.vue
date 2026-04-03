<template>
  <div class="box" v-if="configData"> 
         <vue-editor
                v-model="configData.val"
                useCustomImageHandler
                @image-added="handleImageAdded" 
                  style="width: 100%; height: 60%"
              ></vue-editor>
  </div>
</template>

<script> 
import { VueEditor } from 'vue2-editor'
import Config from "@/packages/apis/Config";
let actionUrl = Config.baseUrl;
import axios from "axios";
import { getStorage } from "@/utils/storage";
    export default {
        name: 'c_page_ueditor',
        props: {
            configObj: {
                type: Object
            },
            configNme: {
                type: String
            }
        },
        components: {   VueEditor },
        data () {
            return { 
                actionUrl,
                description: '',
                defaults: {},
                configData: {}
            }
        },
        created () {
            this.defaults = this.configObj 
            if( this.configObj[this.configNme]){
                this.configData = this.configObj[this.configNme]
            }else{
                this.configData = this.defaults[this.defaults.link_key][this.configNme]
            }
        },
        watch: {
            configObj: {
                handler (nVal, oVal) {
                    this.defaults = nVal
                    console.log('this.defaults',this.defaults)
                    if( this.configObj[this.configNme]){
                      this.configData = nVal[this.configNme]
                    }else{
                     this.configData = nVal[nVal.link_key][this.configNme]
                    }
                },
                immediate: true,
                deep: true
            }
        },
        methods: { 
             handleImageAdded(file, Editor, cursorLocation, resetUploader) {
                    var formData = new FormData()
                    formData.append('file', file) //第一个file 后台接收的参数名
                   axios({
                        url: this.actionUrl, //上传路径
                        method: "POST",
                        params: {
                            api: "admin.resources.uploadFiles",
                            storeId: getStorage("laike_admin_userInfo").storeId,
                            accessId: this.$store.getters.token,
                            groupId: '-1',
                            coverage: 1,
                        },
                        data:formData
                        })
                        .then(result => {
                        
                        let url = result.data.data.imgUrls[0] // 返回给你的图片路径
                        Editor.insertEmbed(cursorLocation, 'image', url) 
                        })
                        .catch(err => {
                        console.error(err)
                        })
                        .finally(() => {
                        // 【关键】重置 input，让同一张图可以再次触发
                        resetUploader()
                        this.clearImageUploadCache()

                        })
                },
                    /**
                    * 清空图片上传缓存，允许重复选择同一张图片
                    */
                clearImageUploadCache() {
                const fileInput = document.querySelector('.ql-image + input[type="file"]'); 
                if (fileInput) {
                    // 清空缓存（关键）
                    fileInput.value = "";
                    // 触发 change 事件，同步编辑器状态（2.10.2 版本需要）
                    fileInput.dispatchEvent(new Event("change", { bubbles: true }));
                } else {
                    // 兜底：遍历查找编辑器内的图片上传 input（防止选择器偏差）
                    const allFileInputs = document.querySelectorAll('input[type="file"][accept="image/*"]'); 
                    Array.from(allFileInputs).forEach((input,index) => {
                    input.value = ""; 
                    }); 
                } 
            },
        }
    }
</script>

<style scoped lang="less">
  
</style>
