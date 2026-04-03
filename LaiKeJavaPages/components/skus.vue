<template>
    <div class="mask" @touchmove.stop.prevent v-if="mask_display">
        <div class="mask_d" :class="mask_display1 ? 'goodAnimate1' : mask_display ? 'goodAnimate' : ''" v-if="mask_display">
            <div class="mask_guige">
                <div class="mask_one" @touchmove.stop.prevent>
                    <div class="mask_imgDiv">
                        <img v-if="loadImg" @load="_loadImg()" class="shangp" :src="imgurl" @error="handleErrorImg"/>
                        <img v-if="!loadImg" :src="load_img" class="img" />
                    </div>
                    <div class="mask_pric">
                        <p class="mask_price" :style="{marginBottom: pro2.commodity_type == 1?'0':''}">
                            <view v-if="integral" style="font-size: 28rpx;">{{language.my.integralNum}}{{' '}}</view>
                            <span class="font_30" v-if="integral">{{ integral }}</span>
                            <span v-if="integral">{{' '}}+{{' '}}</span>
                            {{LaiKeTuiCommon.DEFAULT_STORE_SYMBOL}}
                            <span class="font_30" >{{ LaiKeTuiCommon.formatPrice((price ? Number(price).toFixed(2) : 0)) }}</span>
                        </p>
                        <!-- 虚拟商品不显示库存 -->
                        <p v-if="pro2.commodity_type != 1" class="inventory">
                            {{language.group_end.inventory}}：{{num}}
                        </p>
                    </div>
                    <img class="cha" :src="xxxx" @tap="_mask_f()" />
                </div>
                <scroll-view scroll-y="true" class="mask_over" style="height: 430rpx;">
                    <template v-if="sku_list.result && !sku_list.result['undefined']">
                        <div class='mask_two' v-for="(item, key) in sku_list.result" :key="key" :class="{hl: highKeys[key]}">
                            <p>{{key}}</p>
                            <ul>
                                <template v-for="(value,i) in item">
                                        <li v-if="value.name!=undefined &&integral" :key="i" :class="{select: (!value.active)&&(!value.disabled),orangea: value.active, back: value.disabled}"
                                         @tap='handleActive(key, value)'>
                                            <div>{{ value.name }}</div>
                                        </li>
                                        <li v-else :key="i" :class="{select: (!value.active)&&(!value.disabled),orange: value.active, back: value.disabled}"
                                         @tap='handleActive(key, value)'>
                                            <div>{{ value.name }}</div>
                                        </li>
                                </template>
                            </ul>
                        </div>
                    </template>
                    
                    <div class="mask_num" v-if="!noNumb" style="border-bottom: 0rpx;">
                        <p v-if="integral" style="color: #333333;">{{language.skus.dhsl}}</p>
                        <p v-else>{{ nums || language.skus.num}}</p>
                        <div class="goods_mun" :style="{opacity:pro2.commodity_type == 1 && pro2.write_off_settings == 1 ? '0.5':''}">
                            <span class="goods_mun_span" @tap="_reduce"><img :src="numb == 1 ? jian_hui : jian_hei" /></span>
                            <input :disabled="pro2.commodity_type == 1 && pro2.write_off_settings == 1" @blur="changeNumb" pattern="[0-9]*" type="number" v-model="numb" style="width: 42rpx;min-width: 42rpx;text-align: center;color: #020202;font-size: 28rpx;">
                            <span v-if="type == 'MS'" class="goods_mun_add" @tap="_add"><img :src="numb < buyNums ? jia_hei : jia_hui" /></span>
                            <span v-else class="goods_mun_add" @tap="_add"><img :src="numb < num ? jia_hei : jia_hui" /></span>
                        </div>
                    </div>

                </scroll-view>
                
                <div class="mask_quren_btn_box">
                  
                    <div :class="integral?'mask_quren_btna':'mask_quren_btn'" @tap="_confirm">{{ confirm || language.skus.confirm}}</div>
                    </div>
            </div>
        

        </div>
    </div>
</template>

<script>
    export default {
        data() {
            return {
                load_img: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/5-121204193R7.gif',
                xxxx: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/xxxx.png',
                jian_hei: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/jian2x.png',
                jian_hui: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/jianhui2x.png',
                jia_hei: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/jia+2x.png',
                jia_hui: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon1/add+2x.png', 
                ErrorImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/Default_picture.png',
                mask_display: false,
                mask_display1: false,
                loadImg: true,
                num: '', //规格数量
                price: '', //规格价格
                imgurl: '', //规格图片
                haveSkuBean: '', //选择规则属性
                numb: 1, //规格选择的数量
                skuBeanList: [],
                
                highKeys: {},
                skuName: 'SkuID',
                skuName1: 'Price',
                skuName2: 'Pic',
                skuName3: 'Stock',
                skuName4: 'integralNum', 
                sku_list: {},
                result: [],
                pro2:{},
                integral:'',
                buyNums: ''
            }
        },
        created(){
            // this.setLang();
        },
        props: {
            "noNumb": {
                type: Boolean || String,
                default: false
            },
            "nums": {
                type: String || Number,
                default: ''
            },
            "confirm": {
                type: String || Object,
                default: ''
            },
            "stock": {
                type: String || Number,
                default: ''
            },
            "type": {
                type: String || Number,
                default: ''
            },
            "isStock": {
                type: Boolean || String,
                default: true
            },
            "buyNum": {
                type: String || Number,
                default: ''
            },
            "jfnum": {
                type: String || Number,
                default: ''
            }
        },
        watch: {
            buyNum(newVal,oldVal) {
                this.buyNums = newVal
            }
        },
        methods: {
              // 图片报错处理
              handleErrorImg(index){
                console.log('图片报错');
                setTimeout(()=>{
                    this.imgurl=this.ErrorImg
                },0)
            },
            changeNumb(e){
                console.log(e);
                this.numb = Number(e.detail.value)
                if(e.detail.value > parseInt(this.buyNums)) {
                    this.numb = parseInt(this.buyNums)
                }
                if(this.numb>200){
                    this.numb = 200
                }
                
                if(this.numb<1){
                    this.numb = 1
                    return
                }
                
                if(Number(this.numb)>Number(this.num)){
                    this.numb = Number(this.num)
                }
                
                let re = /^[0-9]+$/;
                if(!re.test(this.numb)){
                    this.numb = parseInt(this.numb)
                }
                
                
            },
            _confirm(){
                if(this.sku_list.result['undefined']){
                	this.haveSkuBean = {
                		cid: this.sku_list.items[0].sku,
                		skus: this.sku_list.items[0]
                	};
                }
                
                
                if(this.numb>200){
                    this.numb = 200
                }
                
                if(this.numb<1){
                    this.numb = 1
                    return
                }
                
                if(Number(this.numb)>Number(this.num)){
                    this.numb = Number(this.num)
                }
                
                let re = /^[0-9]+$/;
                if(!re.test(this.numb)){
                    this.numb = parseInt(this.numb)
                }
                
                this.$emit("confirm",this.$data)
            },
            //关闭遮罩层
            _mask_false() {
                this.overflow = 'scroll';
                this.mask_display1 = true;
                setTimeout(() => {
                    this.mask_display = false;
                    this.mask_display1 = false;
                }, 500);
            },
            _mask_f() {
                this.haveSkuBean = '';
                this._mask_false();
                this.overflow = 'scroll';
            },
            //打开遮罩层
            _mask_display() {
                if(this.sku_list.result){
                    this.showResult()
                }
                //加settimeout解决 加购图标 加购时带空格的部分规格错误问题
                setTimeout(()=>{
                    this.mask_display = true;
                },500)
                this.overflow = 'hidden';
            },
            _loadImg() {
                this.loadImg = true;
            },
            /* 
                增加购买量
            */
            _add() {
                //虚拟商品-需要预约时间（不能多个一起下单）
                if(this.pro2.commodity_type == 1 && this.pro2.write_off_settings == 1){
                    uni.showToast({
                        title: '一次只能购买1个',
                        duration: 1000,
                        icon: 'none'
                    });
                    return
                }
                if(this.type=='MS'){
                    let num2 = parseInt(Number(this.buyNums))
                    console.log(240240,this.buyNum,this.numb,num2);
                    // if(this.numb>=num2){ 原有判断值
                        if(Number(this.buyNum)==-1){
                        uni.showToast({
                            title: this.language.skus.maxNum,
                            duration: 1000,
                            icon: 'none'
                        });
                        return
                    }
                    console.log(this.numb);
                }
                if (this.numb < this.num && Boolean(this.haveSkuBean)) {
                    this.numb++;
                } else {
                    if (!this.haveSkuBean) {
                        uni.showToast({
                            title: this.language.skus.chooseTips,
                            duration: 1000,
                            icon: 'none'
                        });
                    } else {
                        uni.showToast({
                            title: this.language.skus.maxNum,
                            duration: 1000,
                            icon: 'none'
                        });
                    }
                }
            },
            
            /* 
                减少购买量
            */
            _reduce() {
                if (this.numb > 1 && Boolean(this.haveSkuBean)) {
                    this.numb--;
                } else {
                    if (!this.haveSkuBean) {
                        uni.showToast({
                            title: this.language.skus.chooseTips,
                            duration: 1000,
                            icon: 'none'
                        });
                    } else {
                        this.numb == 1;
                        uni.showToast({
                            title: this.language.skus.minNum,
                            duration: 1000,
                            icon: 'none'
                        });
                    }
                }
            },
            
            /*
                 ——————sku核心算法 开始——————
             */
             powerset(arr) {
                 let ps = [[]];
                 for (let i = 0; i < arr.length; i++) {
                     for (let j = 0, len = ps.length; j < len; j++) {
                         ps.push(ps[j].concat(arr[i]));
                     }
                 }
            
                 return ps;
             },
            
             /**
              * 初始化数据
              * @return
              */
             initData(isCart) {
                 this.haveSkuBean = ''
                 this.numb = 1;
                 
                 this.result = {};
                 this.keys = this.getAllKeys(); //arrKeys["颜色", "尺码", "型号"]
            
                 for (let i = 0; i < this.keys.length; i++) {
                     this.highKeys[this.keys[i]] = false; //所有的都为false
                 }
            
                 this.sku_list = this.combineAttr(this.skuBeanList, this.keys);
                console.log('isCart',isCart);
                console.log(this.sku_list);
                 this.buildResult(this.sku_list.items);
                 this.updateStatus(this.getSelectedItem(), true);
                  
                 // 初始筛选出库存为0的属性
                 let filterObj = {}
                 for(let i in this.result){
                     if((!i.includes(',')) && this.result[i].skus.Stock == 0){
                         filterObj[i] = this.result[i]
                     }
                 }
                 
                 for(let i in this.sku_list.result){
                     
                     for(let k in this.sku_list.result[i]){
                         
                         for(let j in filterObj){
                             if(k == j){
                                 this.sku_list.result[i][k].disabled = true
                             }
                         } 
                     } 
                 }
                 // 筛选结束
                 
                 if((!this.sku_list.result['undefined']) && this.haveSkuBean == '' && !isCart){
                     let price = 0;
                     let sku = ''
                     for(let i = 0;i<this.sku_list.items.length;i++){
                         let stock = Number(this.sku_list.items[i].Stock)
                         
                         if(stock>0 && price == 0){
                            price = Number(this.sku_list.items[i].price)
                            sku = this.sku_list.items[i]
                            console.log(sku);
                         }else if(stock>0 && price>Number(this.sku_list.items[i].price)){
                             console.log(sku,price,this.sku_list.items[i].price);
                            price = Number(this.sku_list.items[i].price)
                            sku = this.sku_list.items[i]
                         }
                         
                     }
                     console.log('skuskuskusku',this.sku_list);
                     if(sku != ''){
                         console.log(1,sku);
                         this.initSeleted(sku.sku);
                     } 
                     
                 } 
                 this.showResult();
             },
            
             /**
              * 正常属性点击
              */
             handleNormalClick(key, value) {
                 let list = JSON.parse(JSON.stringify(this.sku_list));
                 console.log(list.result[key])
                 console.log(value)
                  for (let i in list.result[key]) {
                      if (i != value.name) {
                          list.result[key][i].active = false;
                      } else {
                          if(value.active){
                              list.result[key][i].active = false;
                          }else{
                              list.result[key][i].active = true;
                          }
                      }
                  }
                             
                  this.sku_list = list;
             },
            
             /**
              * 无效属性点击
              */
             handleDisableClick(key, value) {
                 this.sku_list.result[key][value.name]['disabled'] = false;
                 // 清空高亮行的已选属性状态（因为更新的时候默认会跳过已选状态）
                 for (let i in this.sku_list.result) {
                     if (i != key) {
                         for (let x in this.sku_list.result[i]) {
                             this.sku_list.result[i][x].active = false;
                         }
                     }
                 }
            
                 this.updateStatus(this.getSelectedItem());
             },
            
             /**
              * 高亮行
              */
             highAttributes: function() {
                 for (let key in this.sku_list.result) {
                     this.highKeys[key] = true;
                     for (let attr in this.sku_list.result[key]) {
                         if (this.sku_list.result[key][attr].active === true) {
                             this.highKeys[key] = false;
                             break;
                         }
                     }
                 }
             },
            
             /**
              * 点击事件处理
              * @param  key   点击的行
              * @param  value 点击的按钮的数据
              */
             handleActive: function(key, value) { 
                 if(value.disabled === true){
                     uni.showToast({
                         title: this.language.skus.stockTips,
                         icon: 'none'
                     })
                     return
                 }
                 
            
                 this.handleNormalClick(key, value);
                 if (value.disabled === true) {
                     this.handleDisableClick(key, value);
                 }
            
                 setTimeout(()=>{
                     this.updateStatus(this.getSelectedItem());
                     this.highAttributes();
                     this.showResult();
                 },0)
             },
            
             /**
              * 计算属性
              * @param  {[type]} data [description]
              * @param  {[type]} keys [description]
              * @return {[type]}      [description]
              */
             combineAttr(data, keys) {
                 let allKeys = [];
                 let result = {};
                 for (let i = 0; i < data.length; i++) {
                     let item = data[i];
                     let values = [];
            
                     for (let j = 0; j < keys.length; j++) {
                         let key = keys[j];
                         if (!result[key]) {
                             result[key] = {};
                         }
            
                         if (!result[key][item[key]]) {
                             result[key][item[key]] = { name: item[key], active: false, disabled: item['Stock'] > 0 ? false : true };
                         }
            
                         values.push(item[key]);
                     }
            
                     allKeys.push({
                         path: values.join(','),
                         sku: item['SkuID'],
                         price: item['Price'],
                         Pic: item['Pic'],
                         Stock: item['Stock'],
                         integralNum: item['integralNum']
                     });
                 }
            
                 return {
                     result: result,
                     items: allKeys
                 };
             },
            /* 
                判断是否是JSON字符串
            */
             isJSON(str) {
                 if (typeof str == 'string') {
                     try {
                         var obj = JSON.parse(str);
                         return true;
                     } catch (e) {
                         console.log('error：' + str + '!!!' + e);
                         return false;
                     }
                 }
             },
            
             /**
              * 获取所有属性
              * @return {[type]} [description]
              */
             getAllKeys() {
                 let arrKeys = [];
                 for (let attribute in this.skuBeanList[0]) {
                     if (!this.skuBeanList[0].hasOwnProperty(attribute)) {
                         continue;
                     }
            
                     if (attribute !== this.skuName && attribute !== this.skuName1 && attribute !== this.skuName2 && attribute !== this.skuName3&& attribute !== this.skuName4) {
                         arrKeys.push(attribute);
                     }
                 }
                 if(arrKeys.length == 0){
                 	arrKeys = ["undefined"]
                 	this.skuBeanList[0]["undefined"] = "undefined"
                 }
                 
                 return arrKeys;
             },
            
             getAttruites(arr) {
                 let result = [];
                 for (let i = 0; i < arr.length; i++) {
                     result.push(arr[i].path);
                 }
            
                 return result;
             },
            
             /**
              * 生成所有子集是否可选、库存状态 map
              */
             buildResult(items) {
                 let allKeys = this.getAttruites(items);
            
                 let attr = {};
                 //价格 , 库存, 图片 赋值
                 for (let i = 0; i < allKeys.length; i++) {
                     let curr = allKeys[i];
                     let sku = items[i].sku;
                     let Pic = items[i].Pic;
                     let price = items[i].price;
                     let Stock = items[i].Stock;
                     let values = curr.split(',');
                     let allSets = this.powerset(values);
            
                     // 每个组合的子集
                     for (let j = 0; j < allSets.length; j++) {
                         let set = allSets[j];
                         let key = set.join(',');
                         if (key && !this.result[key]) {
                             this.result[key] = {
                                 skus: { sku, Pic, price, Stock }
                             };
            
                             if ((!key.includes(',') && !attr[key]) || (key.includes(',') && key.split(',').length < allKeys[i].split(',').length)) {
                                 attr[key] = {
                                     skus: { sku, Pic, price, Stock }
                                 };
                             }
                         }
                     }
                 }
            
                 for (let i in attr) {
                     attr[i].skus.Stock = 0;
                     for (let k in this.result) {
                         if (i != k && k.split(',').length == allKeys[0].split(',').length && k.includes(i)) {
                             attr[i].skus.Stock += Number(this.result[k].skus.Stock);
                         } else if (k.split(',').length == allKeys[0].split(',').length) {
                             let flag = [];
            
                             k.split(',').filter(item => {
                                 i.split(',').filter(it => {
                                     if (item == it) {
                                         flag.push(true);
                                     }
                                 });
                             });
            
                             if (flag.length == i.split(',').length) {
                                 attr[i].skus.Stock += Number(this.result[k].skus.Stock);
                             }
                         }
                     }
                 }
            
                 Object.assign(this.result, attr);
             },
            
             /**
              * 获取选中的信息
              * @return Array
              */
             getSelectedItem() {
                 let result = [];
                 for (let attr in this.sku_list.result) {
                     let attributeName = '';
                     for (let attribute in this.sku_list.result[attr]) {
                         if (this.sku_list.result[attr][attribute].active === true) {
                             attributeName = attribute;
                         }
                     }
                     result.push(attributeName);
                 }
                 return result;
             },
            
             /**
              * 更新所有属性状态
              */
             updateStatus(selected, type) {
                 for (let i = 0; i < this.keys.length; i++) {
                     let key = this.keys[i],
                         data = this.sku_list.result[key],
                         hasActive = !!selected[i],
                         copy = selected.slice();
            
                     for (let j in data) {
                         let item = data[j]['name'];
                         if (selected[i] == item) {
                             continue;
                         }
            
                         copy[i] = item;
                         let curr = this.trimSpliter(copy.join(','), ',');
            
                         if (type) {
                             this.sku_list.result[key][j]['disabled'] = this.result[curr] ? false : true;
                         } else {
                            this.sku_list.result[key][j]['disabled'] = this.result[curr].skus.Stock > 0 ? false : true;
                         }
                     }
                 }
             },
            
             trimSpliter(str, spliter) {
                 let reLeft = new RegExp('^' + spliter + '+', 'g');
                 let reRight = new RegExp(spliter + '+$', 'g');
                 let reSpliter = new RegExp(spliter + '+', 'g');
                 return str
                     .replace(reLeft, '')
                     .replace(reRight, '')
                     .replace(reSpliter, spliter);
             },
            
             /**
              * 初始化选中
              * @param  mixed|Int|String SkuID 需要选中的SkuID
              * @return {[type]}       [description]
              */
             initSeleted(a) {
                 for (let i in this.skuBeanList) {
                     if (this.skuBeanList[i][this.skuName] == a) {
                         for (let x in this.skuBeanList[i]) {
                             if (x !== this.skuName && x !== this.skuName1 && x !== this.skuName2 && x !== this.skuName3&& x !== this.skuName4) {
                                 this.sku_list.result[x][this.skuBeanList[i][x]].active = true;
                                 console.log(i);
                             }
                         }
                         break;
                     }
                 }
                 
                 this.updateStatus(this.getSelectedItem());
                 this.highAttributes();
                 this.showResult();
             },
            
             /**
              * 显示选中的信息
              * @return
              */
             showResult() {
                console.log('this.skuBeanList,696',this.skuBeanList);
                 let result = this.getSelectedItem();
                 let s = [];
                 
                 for (let i = 0; i < result.length; i++) {
                     let item = result[i];
                     if (!!item) {
                         s.push(item);
                     }
                 }
                 if (s.length > 0) {
                     this.num = this.result[s.join(',')].skus.Stock;
                 }
                 if (s.length == this.keys.length) {
                     let curr = this.result[s.join(',')];
                     if (curr) {
                         this.SkuID = curr.skus.sku;
                         this.Pic = curr.skus.Pic;
                         this.price = curr.skus.price;
                         console.log('this.skuBeanList',this.skuBeanList);
                         this.skuBeanList.filter(item=>{
                             if(item.SkuID == this.SkuID){
                                 this.Stock = item.Stock;
                                 this.num = item.Stock;
                                 this.imgurl = item.Pic
                                 this.integral = item.integralNum
                             }
                         })
                         
                         if(Number(this.numb)>Number(this.Stock)){
                         	this.numb = Number(this.Stock)
                         }
                     }
            
                     this.haveSkuBean = {
                         name: s.join(','),
                         cid: curr.skus.sku,
                         skus: curr.skus
                     };
                      console.log('haveSkuBean选中数据ID', this.haveSkuBean,123);
                 } else {
                     this.haveSkuBean = '';
                 }
                 
             },
            
             /* 
                 ——————sku核心算法 结束——————
            */
        },
    }
</script>

<style lang="less">
    @import url("@/laike.less");
    /* 遮罩层规格样式 */
    .mask {
        height: 100vh;
        width: 100%;
        background-color: rgba(000, 000, 000, 0.5);

        position: fixed;
        // position: relative;
        top: 0;
        left: 0;
        z-index: 999 !important;
    }
    .mask_imgDiv{
        width: 164rpx !important;
        height: 164rpx !important;
        .shangp{
            width: 164rpx !important;
            height: 164rpx !important;
        }
    }
    .mask_d {
        z-index: 999;
    }
    
    .mask_two li {
        float: left;
        width: auto;
        min-width: 60rpx;
        margin: 24rpx 24rpx 0 0;
        padding: 0 10rpx;
    }
    
    .numb ._img {
        padding: 0px;
    }

    .mask_quren_btn_box{
        // position: fixed;
        // left: 0;
        // bottom: 0;
        width: 100%;
        display: flex;
        margin-top: 460rpx;
        // height: 124rpx;
        display: flex;
        justify-content: center;
        align-items: center;
		padding-bottom: constant(safe-area-inset-bottom); /* 兼容 iOS < 11.2 */
		padding-bottom: env(safe-area-inset-bottom);
    }
    .mask_quren_btn{
       width: 686rpx;
       line-height: 92rpx;
       text-align: center;
       height: 92rpx;
       background: linear-gradient(270deg, #FF6F6F 0%, #FA5151 100%);
       border-radius: 52rpx;
        font-size: 32rpx;
        
        font-weight: 500;
        color: #FFFFFF;
        z-index: 1000;

    }
    .mask_quren_btna{
       width: 686rpx;
       line-height: 92rpx;
       text-align: center;
       height: 92rpx;
       background: #FFC300;
       border-radius: 52rpx;
        font-size: 32rpx;
        
        font-weight: 500;
        color: #333333;
        z-index: 1000;  
    }
    .cha {
        width: 48rpx;
        height: 48rpx;
        position: absolute;
        top: -10rpx;
        right: -10rpx;
        padding: 10rpx;
        background-size: 50% 50% !important;
    }
    
    .back {
        width: 144rpx;
        height: 48rpx;
        background: #CCCCCC;
        border-radius: 24rpx;
        font-size: 24rpx;
        
        font-weight: 400;
        color: #FFFFFF;
    }
    .inventory{
        font-size: 28rpx;
         
        font-weight: normal;
        color: #666666;
    }
    .orange {
        background-color: #FFFFFF;
        color: #020202;
        height: 48rpx;
        width: 144rpx;
        font-size: 24rpx;
        // border: 2rpx solid @btnBackground !important;
        background: rgba(250,81,81,0.1);
        border-radius: 24rpx;
        color: @btnBackground !important;
    }
    .orangea {
        width: 144rpx;
        height: 48rpx;
        background-color: #FFF7CC;
        font-size: 24rpx;
        border-radius: 52rpx;
        border: 2rpx solid #FFC300;
        color: #FFC300;
    }
    .orange div,.select div,.back div {
        margin: 0px 16rpx;
    }
    
    .select {
        color: #666666;
        font-size: 24rpx;
        height: 48rpx;
        border: 0rpx solid rgba(2, 2, 2, 1) !important;
        background:#F4F5F6;
        border-radius: 24rpx;
    }
    
    .goods_spec li {
        padding: 30rpx;
        color: #020202;
        font-size: 24rpx;
        padding-left: 170rpx;
        position: relative;
        border-bottom: 1rpx solid #eee;
        overflow: hidden;
        justify-content: flex-start;
    }
    
    .goods_spec li > span {
        font-size: 28rpx;
        color: #888;
    }
    
    .goods_spec li .g_div {
        position: absolute;
        top: 0;
        left: 30rpx;
        width: 160rpx;
        height: 100%;
        text-align: center;
        display: flex;
    }
    
    .goods_spec li .g_div div {
        display: flex;
        align-items: center;
    }
    
    .goods_spec li > p {
        width: 80%;
        overflow: hidden;
        height: 40rpx;
        line-height: 40rpx;
    }
    
    .active {
        color: #9D9D9D !important;
        font-size: 26rpx !important;
    }
    
    .load {
        height: 750rpx;
        width: 100%;
        background-color: #9D9D9D;
        opacity: 0.7;
        position: absolute;
        top: 0;
        left: 0;
        text-align: center;
    }
    
    .load > p {
        font-size: 20rpx;
        color: #eee;
        margin-top: 1rpx;
    }
    .mask_two,.mask_num{
        border-bottom: 0.5px solid #eee;
    }
    .mask_two{
        p{
            color: #333;
        }
    }
    .mask_num {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding-bottom: 120rpx;
    }
    
    .goods_mun {
        color: #9d9d9d;
        padding: 0rpx;
        display: inline-block;
        width: auto;
        box-sizing: border-box;
        display: flex;
        align-items: center;
        justify-content: space-around;
    }
    .goods_mun {
        display: flex;
        align-items: center;
    border: 2rpx solid rgba(0,0,0,0.1);
        border-radius: 27rpx;
        height: 56rpx;
        width: 124rpx;
    }
    
    .goods_mun span {
        display: flex;
        align-items: center;
        justify-content: center;
        height: 100%;
        width: 100%;
    }
    
    .goods_mun span img {
        width: 16rpx;
        height: 16rpx;
        padding: 10rpx;
        /* #ifdef MP-ALIPAY */
        background-size: 25rpx 25rpx !important;
        width: 0.1rem;
        height: 0.07rem;
        /* #endif */
    }

    
    .mask_pric_num {
        font-size: 28rpx;
        color: #666666;
    }
    .mun {
        margin: 0 10rpx;
        font-size: 30rpx;
        border: none !important;
        color: #020202
    }
    
    .goodAnimate {
        animation: myGood 0.5s 1;
        animation-timing-function: ease;
        border-top-left-radius: 24rpx;
        border-top-right-radius: 24rpx;
        animation-fill-mode: forwards;
    }

    @keyframes myGood {
        from {
            bottom: -1300rpx;
        }
    
        to {
            bottom: 000rpx;
        }
    }
    
    .goodAnimate1 {
        animation: myGood1 0.5s 1;
        animation-timing-function: ease;
        animation-fill-mode: forwards;
    }
    
    @keyframes myGood1 {
        from {
            bottom: 0rpx;
        }
    
        to {
            bottom: -1300rpx;
        }
    }
    .mask_querenDiv{
        
    }
    .mask_price {
        font-size: 24rpx;
        color: #FA5151;
        margin-bottom: 0;
        display: flex;
        align-items: baseline;
        .font_30 {
            font-size: 40rpx;
            font-family: DIN-Bold, DIN;
            
        }
    }
</style>
