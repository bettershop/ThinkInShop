<!--
 * @Description: In User Settings Edit
 * @Author: your name
 * @Date: 2019-08-12 15:31:42
 * @LastEditTime: 2019-09-05 10:07:21
 * @LastEditors: Please set LastEditors
 -->
<template>
    <div class='box_eva  ' >
        <lktauthorize ref="lktAuthorizeComp" v-on:pChangeLoginStatus="changeLoginStatus"></lktauthorize>
        <heads :title='title' ishead_w="2" :bgColor="bgColor" titleColor="#333333"></heads>

        <div class='goods' v-for='(item,index) in goods' :key='item.id'>
            <div class='heads'>
                <img :src='item.commodityIcon' />
                <div class='heads_bottom'>
                    <p class='heads_p'>{{item.product_title}}</p>
                    <p style="color: #999999;font-size: 22rpx;">
                        {{item.size}}
                    </p>
                </div>
            </div>


            <div class="evaluate"> 
                <div class='heads_evaluate' v-if='type != 1'>
                    <p class="evaluate_title">{{language.evaluate.evaluating.shop_pf}}:</p>
                    <div class='starall'>
                        <img v-for="(star,itemIndex) in item.stars" :key='itemIndex'
                            :src="item.stars[itemIndex].show ? starOnImg : starOffImg"
                            @tap="rating(index,itemIndex,item.commodityId)" />
                    </div>
                </div>
                <!-- 评价内容 -->
                <div class="input">
                    <textarea placeholder-style="font-size:28rpx;color:#999999;" class='input_text' rows="4"
                        maxlength="200" v-model="desc[index]"
                        :placeholder='language.evaluate.evaluating.evaluate_placeholder' />
                </div>
                <!-- 添加图片-->
                <div class="add_images">
                    <p class='add_img'>{{language.evaluate.evaluating.add_img}} (<span class="add_num">{{data[index].src.length}}</span>/9)</p>
                    <ul class='evaluat_ul'>
                        <li class='evaluat_li' v-for='(item_img,indexsrc) in data[index].src' :key='indexsrc'>
                            <img class='evaluat_img' :src="item_img" @tap='_previewImg(index,indexsrc)' />
                            <img class='img_dele' :src="evaluate_guanbi" @tap="_delete(index,indexsrc)" />
                        </li>

                        <li class="uploadphoto" @tap="choiceImg(index,data[index].src.length)"
                            v-if='data[index].src.length<9?true:false'>
                            <img class='xiangji' :src="xiangji_img" />
                            <p class="p eva-p">{{language.evaluate.evaluating.upload_img}}</p>
                        </li>

                    </ul>
                    <div class="eva-clear-both"></div>
                </div>
            </div>
        </div>
        <div class="user" v-if="is_show && isShow">
            <img class="anonymous_img" :src="mous ? anonymous:onanonymous" @tap="_mous" />
            <span>{{language.evaluate.evaluating.anonymous}}</span>
        </div>



        <div class="card" v-show="isShow">
            <div class='button' @click="submit">{{language.evaluate.evaluating.Submit}}</div>
        </div>
        <div v-if="show_zz" style="height: 100vh;position: absolute;top: 0;width: 100%;">

        </div>
    </div>

</template>

<script>
    export default {
        data() {
            return {
                num: '',
                aaa: [9, 8, 7, 6, 5, 4, 3, 2, 1, 0],
                title: '发布评价',
                // order_id:'',
                order_details_id: '',
                isShow:false,
                goods: [], //带评价商品信息
                goodsId: [],
                desc: [], //评论内容
                flag: [],
                // remnant: 200 , //最多输入200个字符
                index: '',
                stars: [], //单个商品的星星状态
                starnum: '', //单个商品星级数
                evaluate_guanbi: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/evaluate_guanbi.png',
                delete_img: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/delete2x.png',
                xiangji_img: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/xiangji2x.png',
                starOffImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/xing12x.png', //灰星星
                starOnImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/xing22x.png?v=' + Math
            .random(), //红星星
                anonymous: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/xuanzehui2x.png', //匿名
                onanonymous: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/gouhei.png', //署名
                mous: true, //是否匿名
                data: [],
                imgsrc: '', //上传的·图片的地址
                show: false, //图片放大预览
                timeout: '',
                img_index: -1,
                // img_dispaly:true,
                requestTime: 0,
                requestTime1: 0,
                // cunt:0,
                resImg: [],
                add: '', //true为追加评论
                fastTap: true,
                combotab: false, //防止重复提交
                staus: true,
                show_zz: false,
                is_show: true, //匿名是否显示 
                bgColor: [{
                        item: '#ffffff'
                    },
                    {
                        item: '#ffffff'
                    }
                ],
                orderNo:'',
                type:''
            }
        },
        // 监听页面加载
        onLoad(option) {
            var that = this;
            this.order_details_id = option.order_details_id
            this.orderNo = option.orderNo
            this.add = option.add || false

            this.num = option.num
            this.type = option.type || 0  // 0是立即评价  1 追加评价

            this.getEvaluatingData()

        },
        // 监听页面显示
        onShow() {
            if (this.add) {
                this.title = this.language.evaluate.evaluating.Additional
                this.is_show = false
            } else {
                this.title = this.language.evaluate.evaluating.title
            }

            this.isLogin(() => {

            })
        },
        onReady(option) {
            if (this.title == '追加评价' || this.title == 'Additional evaluation') {
                this.title = this.language.evaluate.evaluating.Additional
            } else {
                this.title = this.language.evaluate.evaluating.title
            }
        },
        // 计算属性
        computed: {
            remnant() {
                let arr = []
                for (let i = 0; i < this.desc.length; i++) {
                    arr[i] = 200 - this.desc[i].length
                }
                return arr
            }
        },
        // 事件方法
        methods: {
            changeLoginStatus() {
                this.getEvaluatingData()
            },
            _previewImg(index, i) {
                uni.previewImage({
                    current: this.data[index].src[i],
                    urls: this.data[index].src
                })
            },
            /**
             * 获取评论
             * @return { undefind}
             */
            getEvaluatingData() {
                let data = {
                    api: 'app.product.commentList',
                    order_details_id: this.order_details_id,
                    type:this.type,
                    num: this.num,
                    orderNo: this.orderNo
                }
               if(!this.order_details_id){
                   delete data.order_details_id
               }

                if (this.add) {
                    data.type = 1
                }
                uni.showLoading({
                    title:'加载中...'
                })
                this.$req.post({
                    data
                }).then(res => {
                    if (res.code == 200) {
                        this.goods = res.data.commentList
                        this.starnum = this.goods.length
                        // 如果为空数组则判断为 暂无可评价的商品
                        
                        if(this.goods && this.goods.length == 0 ){
                            uni.showToast({
                                title:this.language.evaluate.evaluate.zwpl,
                                icon:'none', 
                            })
                            setTimeout(()=>{
                               uni.navigateBack({
                                   delta: 1,
                               });
                            },1500)
                            return
                        }
                        uni.hideLoading()
                        this.isShow = true
                        let tempArr = []
                        // if (this.stars.length > 0) {
                        //     this.stars = []
                        // } else if (this.flag.length > 0) {
                        //     this.flag = []
                        // } else { 
                            for (let i = 0; i < this.goods.length; i++) {
                                 let stars = []
                                this.flag.push(false)
                                for (let j = 0; j < 5; j++) {
                                    stars.push({
                                        show: false
                                    })
                                } 
                                // stars.push(tempArr)
                                this.$set(this.goods[i],'stars',stars)
                                this.data.push(new Object)

                                tempArr = []

                                this.data[i].commodityId = this.goods[i].commodityId
                                this.data[i].size = this.goods[i].size
                                this.data[i].attribute_id = this.goods[i].sid
                                this.data[i].cid = ''
                                this.data[i].index = ''
                                this.$set(this.data[i], 'src', [])
                                this.$set(this.data[i], 'base64_img', [])
                            // }
                        } 
                    } else {
                        uni.showToast({
                            title: res.message,
                            duration: 1500,
                            icon: 'none'
                        })
                    }
                })
            },
            /**
             *    评价星星切换
             * @param:  { pIndex:Number }
             * @param:  { sIndex:Number }
             * @param:  { id:Number }
             * @return: undefined
             */
            rating(pIndex, sIndex, id) {
                let con = 0 
                ++sIndex
                while (con < 5) {
                    if (con < sIndex) {
                        this.goods[pIndex].stars[con].show = true
                    } else {
                        this.goods[pIndex].stars[con].show = false
                    }
                    con++
                }

                this.data[pIndex].starnum = sIndex
            },
            /**
             * 是否匿名
             * @return { undefined }
             * */
            _mous() {
                this.mous = !this.mous
            },
            /**
             * 提交评论
             * @return { undefined }
             * */
            async submit() {
                if (this.combotab) {
                    return false
                } else {
                    this.combotab = true

                    setTimeout(() => {
                        this.combotab = false
                    }, 2000)
                }

                this.fastTap = false

                for (let i = 0; i < this.goods.length; i++) {
                    this.data[i].content = this.resImg[i]
                }

                this.requestTime1 = 0
                for (let a = 0; a < this.goods.length; a++) {
                    for (let j = 0; j < this.data[a].src.length; j++) {
                        this.requestTime1 += 1
                    }
                }

                // uni.showLoading({
                //     title: this.language.evaluate.evaluating.Loading
                // })
                this.show_zz = true
                
                this.newReview()
                // for (let i = 0; i < this.data.length; i++) {

                //     if (this.desc.length == 0 || this.desc[i] == '' || this.desc.length < this.data.length) {
                //         uni.showToast({
                //             title: this.language.evaluate.evaluating.shop_evaluate,
                //             duration: 1500,
                //             icon: 'none'
                //         })
                //         this.fastTap = true
                //         return
                //     } else {
                //         this.fastTap = false
                //     }
                // } 
                
                // if (this.title != '追加评价' || this.title != 'Additional evaluation') {
                //     for (let i = 0; i < this.data.length; i++) {
                //         if (!this.data[i].starnum && !this.add) {
                //             uni.showToast({
                //                 title: this.language.evaluate.evaluating.shop_evaluate,
                //                 duration: 1500,
                //                 icon: 'none'
                //             })
                //             this.fastTap = true
                //             return false
                //         } else {
                //             this.fastTap = true
                //         }
                //     }
                // } 
            },
            
            /**
             * 新评论接口
             * @param {Object} b
             * @param {Object} data
             * data
             */
            async newReview(){ 
                 
                let commentList =[]
                this.data.forEach((v,i)=>{
                    
                    commentList.push({
                        imgUrls:v.src, //图片
                        order_details_id:this.goods[i].id, 
                        comment:this.desc[i], //评论
                        start:this.data[i].starnum, // 评分
                        anonymous: this.mous ? 0 : 1, //1匿名，2不匿名
                        access_id: this.$store.state.access_id || uni.getStorageSync('access_id') || uni.getStorageSync('laiketuiAccessId'),
                    })
                })
                if(commentList.filter(v=>v.start>0).length !== this.data.length){ 
                    if(this.type == 0){
                        // 只有 评价才会判断 星级 追评不会
                        this.show_zz = false
                        uni.showToast({
                            title: this.language.evaluate.evaluating.shop_evaluate, 
                            icon: 'none'
                        })
                        return
                    }
                }
                 
                const postData ={
                    api: 'app.product.addBatch_comment',
                    commentList:JSON.stringify(commentList.filter(v=>v.start>0)) //商品对象 
                } 
                if(this.type != 0){
                    postData.commentList=JSON.stringify(commentList)
                }
                const {code ,data, message} = await this.$req.post({
                        data:postData
                    })
                     this.show_zz = false
                    if(code == 200){
                        uni.showToast({
                            title: this.language.evaluate.evaluating.Submit_success,
                            icon: 'none',
                            duration: 1000
                        })
                        setTimeout(() => {
                            uni.redirectTo({
                                url: 'evaluatEnd'
                            })
                        
                            this.fastTap = true
                        }, 1000)
                       
                    }else{
                        
                         uni.hideLoading()
                        uni.showToast({
                            title: message,
                            icon: 'none',
                            duration: 1000
                        })
                    }
            },
            /**
             *
             * */
            async _Submission(b, data) {
                try {
                    var me = this
                    let res = await this.$req.post({
                        data
                    })
                    if (res.code == 200) {
                        me.data[b].cid = res.data.cid;
                        if (me.data[b].src.length == 0) {
                            uni.hideLoading();
                            uni.showToast({
                                title: this.language.evaluate.evaluating.Submit_success,
                                icon: 'none',
                                duration: 1500
                            })
                            setTimeout(function() {
                                me.fastTap = true
                                uni.redirectTo({
                                    url: 'evaluatEnd'
                                })
                            }, 1500)
                        } else {
                            me._img(b)
                        }

                    } else if (res.code != 200) {
                        me.fastTap = true
                        uni.showToast({
                            title: res.message,
                            icon: 'none',
                            duration: 1000
                        })
                    }
                } catch (e) {
                    me.fastTap = true
                }

            },
            /**
             *
             * */
            async _img(b) {
                var me = this
                for (var i = 0; i < me.data[b].src.length; i++) {
                    var parameter = {
                        access_id: me.access_id,
                        api: 'app.product.img_comment',
                        cid: me.data[b].cid,
                        upload_z_num: me.data[b].src.length,
                        upload_num: i,
                    }
                    //判断上次上传结果成功了才能执行下一次方法
                    parameter = await this._upload(b, i, parameter)

                }
            },
            /**
             *
             * */
            _upload(b, i, parameter) {
                var me = this
                return new Promise((res1) => {
                    uni.uploadFile({
                        url: uni.getStorageSync('url'),
                        filePath: me.data[b].src[i],
                        name: 'file',
                        formData: parameter,
                        success: function(res) {
                            var res2 = JSON.parse(res.data)
                            me.fastTap = true
                            if (res2.code == 200) {
                                i++
                                if (i == me.data[b].src.length && me.data.length - b == 1) {
                                    me.fastTap = true
                                    me.staus = false
                                    uni.showToast({
                                        title: me.language.evaluate.evaluating
                                            .Submit_success,
                                        duration: 1500,
                                        icon: 'none'
                                    })
                                    setTimeout(function() {
                                        uni.redirectTo({
                                            url: 'evaluatEnd'
                                        })
                                    }, 1500)
                                } else {
                                    me.staus = true
                                }
                            } else {
                                uni.showToast({
                                    title: res2.status,
                                    duration: 1500,
                                    icon: 'none'
                                })
                                return
                            }
                            res1(res2)
                        },
                        error: function() {
                            me.fastTap = true
                        }
                    })
                })
            },
            /**
             *
             * */
            async _Timg(b) {
                var me = this
                for (var i = 0; i < me.data[b].src.length; i++) {
                    var parameter = {
                        access_id: me.access_id,
                        api: 'app.product.t_comment',
                        anonymous: me.mous ? 0 : 1, //1匿名，2不匿名
                        order_details_id: me.order_details_id,
                        start: typeof(me.data[b].starnum) == "undefined" ? "0" : me.data[b].starnum,
                        comment: me.desc[b],
                        goodsId: me.data[b].commodityId,
                        attributeId: me.data[b].attribute_id,
                        // num:me.num,
                        upload_z_num: me.data[b].src.length,
                        upload_num: i,
                    }
                    //判断上次上传结果成功了才能执行下一次方法
                    parameter = await this._upload(b, i, parameter)

                }
            },
            /**
             * 上传图片
             * @param { number } index
             * @param { number } indexsrc
             * @return { undefined }
             * */
            choiceImg(index, indexsrc) {
                let count = Number(this.aaa[indexsrc])
                uni.chooseImage({
                    count: count, // 默认9
                    sizeType: ['original', 'compressed'], // 可以指定是原图还是压缩图，默认二者都有
                    sourceType: ['album', 'camera'], // 从相册选择
                    success: async (res) => {
                        // let list = await this.getImageList(res.tempFilePaths)
                        // this.resImg = this.resImg.concat(list)
                        // this.data[index].src = this.data[index].src.concat(list)
                        let data = {
                            src_img: res.tempFilePaths[0],
                            api: "resources.file.uploadFiles",
                            coverage:1
                        }
                        
                        this.$req.upLoadl(res.tempFilePaths[0], data).then(res => {
                            let {
                                code,
                                message
                            } = res
                            if (code == 200) {
                                uni.showToast({
                                    title: this.language.myinfo.upload_seccess,
                                    duration: 1500,
                                    icon: 'none'
                                })
                                
                                // this.data[index].src = res.data.imgUrls[0]
                                // this.resImg = this.resImg.concat(res.data.imgUrls[0])
                                this.data[index].src.push(res.data.imgUrls[0])
                            } else {
                                uni.showToast({
                                    title: message,
                                    duration: 1500,
                                    icon: 'none'
                                })
                            } 
                        }).catch(({message})=>{ 
                            uni.showToast({
                                title:message,
                                icon:'error'
                            })
                        })
                        
                        
                    }
                })
            },
            async getImageList(tempFilePaths) {
                let list = tempFilePaths
                for (let key of list.keys()) {
                    let res = await uni.getImageInfo({
                        src: list[key]
                    })
                    list[key] = res[1].path

                }
                return Promise.resolve(list);
            },
            /**
             * 删除选中图片
             * @param { unmber } index 操作对象位置
             * @param { unmber } pImgIndex 选中图片下标
             * @return { undefined }
             * */
            _delete(index, pImgIndex) {
                if (this.data[index].src.length > 0) {
                    this.data[index].src.splice(pImgIndex, 1)
                    this.resImg.splice(pImgIndex, 1)
                }
            }
        }
    }
</script>

<style>
    page {
        background: #F4F5F6;
    }
</style>
<style lang="less" scoped> 
    @import url("@/laike.less");
    @import url("../../static/css/evaluate/evaluating.less");
    
</style>
