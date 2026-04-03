<template>
    <view class="container">
        <lktauthorize ref="lktAuthorizeComp" v-on:pChangeLoginStatus="changeLoginStatus"></lktauthorize>
        <heads :title="title"  ishead_w="2" :bgColor="bgColor" titleColor="#333333"></heads>
        
        <view class="content" v-if="show_type != 'see' && !isOpenCity">
            
           <ly-tree
                :ready="ready"
                :props="props"
                ref="tree"
                v-if="isReady && treeData.length > 0"
                :default-checked-keys="defaultCheckedKeys"
                :load="loadNode"
                :lazy="true"
                :showCheckbox="true"
                :renderAfterExpand="false"
                :expandOnCheckNode="true"
                node-key="id"
                @check-change="handleCheckChange"
                >
            </ly-tree>
            
        </view>
        <view class="container_btn" v-if="show_type != 'see' && area_list.length>0">
           
            <view class="container_bottomBtn"  :class="{disable:show_type == 'add' && !this.checkObj.id}"   @tap="saveOk">{{language.freight_sheng.save}}</view>
        </view>
        
        <p class="address" v-if="show_type == 'see' || isOpenCity">{{ viewCity }}</p>
    </view>
</template> 

<script>
import LyTree from '@my-miniprogram/src/components/ly-tree/ly-tree.vue'
var _self;
export default {
    data() {
        return {
            remove_id:'',//排除显示的id
            show_type:'', //see:查看 edit :编辑
            type:'', // not_delivery:不发货地址选择 
            selectAll:false,
            quan_hui: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/xuanzehui2x.png',
            quan_hei: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/icon/xuanzehei2x.png',
            title: '选择省份',
            chooseImg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL + 'images/seconds/check.png',
			area_list: [],
            shop_id: '',
            freight_id: '', //已选中的省份ID
            freight_data: {}, //已排除的省份对象{"0":{"one":"5","name":"山东省,河南省"}}
            sel_city: [],
            
            ready: false, // 这里用于自主控制loading加载状态，避免异步正在加载数据的空档显示“暂无数据”
            treeData: [],
            treeData1: [],
            
            treeDataCopy: [],
            rolelist: [],
            idList: [],
            nameList: [],
            defaultCheckedKeys: [], // 默认选择
            indeterminateList: [], // 半选
            isReady: false,
            props: function() {
                return {
                    label: 'district_name', // 指把数据中的‘personName’当做label也就是节点名称 
                    children: 'children', // 指把数据中的‘childs’当做children当做子节点数据
                    isLeaf: 'leaf'
                }
            },
            bgColor: [
                {
                    item: '#ffffff'
                },
                {
                    item: '#ffffff'
                }
            ],
            
            viewCity: '', // 城市回显
            isOpenCity: ''
        };
    },
    components: {
        LyTree
    },
    computed: {
        checkAll(){
            let flag = this.area_list.some(item=>item.status==false)
            return !flag
        },
        checkObj(){
            let checkName = []
            let checkId = []
            this.area_list.filter(item=>{
                
                //TODO ??
                if(item.status){
                    checkName.push(item.district_name)
                    checkId.push(item.id)
                }
            })
            return {
                name: checkName.join(),
                id: checkId.join()
            }
        },
        
    },
    onLoad(option) {
        _self = this;
        this.getCityInfo()
        let me = this
        if(option.freight_id && option.show_type != 'see' && !option.isOpenCity) {
            setTimeout(function() {
                if(option.indeterminateList) {
                    me.indeterminateList = option.indeterminateList.split(',')
                }
                if(option.freight_id) {
                    me.defaultCheckedKeys = option.freight_id.split(',')
                }
                me.defaultCheckedKeys = me.defaultCheckedKeys.concat(me.indeterminateList)
                me.defaultCheckedKeys = me.noRepeat(me.defaultCheckedKeys)
            },500)
        }
        if(option.isOpenCity) {
            this.isOpenCity = option.isOpenCity
        }
        if(option.viewCity) {
            this.viewCity = option.viewCity
        }
        if(option.type){
            this.type = option.type
        } 
        if(option.freight_id){
            this.freight_id = option.freight_id
        }else{
            this.show_type = "add"
        }
        if(option.show_type){
            this.show_type = option.show_type
        }
        if(option.remove_id){
            this.remove_id = option.remove_id
        }
        if(option.show_type == 'see') {
            this.indeterminateList = option.indeterminateList
        }
        
        if((uni.getStorageSync('freight_rules')||uni.getStorageSync('not_delivery_province')) && this.show_type != 'see'){
            let freights = uni.getStorageSync('freight_rules') // 发货的省
            let not_delivery_province = uni.getStorageSync('not_delivery_province') // 不发货的省
            let freight_data = {}
            let sel_city = []
            let freights_item = []
            if(freights){
               
            }
            let not_delivery_province_id = '';
            if(JSON.stringify(not_delivery_province) != "{}" && not_delivery_province){
                not_delivery_province_id = not_delivery_province.id.split(',')
                
                not_delivery_province_id.forEach((item)=>{
                    if(item==''){
                        return
                    }
                    sel_city.push(item)
                })
            }
           
            
            // 去掉编辑传来的省id
            let new_province_id = []
            if(this.freight_id){
               let freight_id_arr=  this.freight_id.split(',');
               
               
               sel_city.forEach(it=>{
                   
                   if(it!=''){
                       if(freight_id_arr.indexOf(it) == -1){
                            new_province_id.push(it)
                       }
                   }
                   
               })
            }else{
                new_province_id = sel_city
            }
           
            this.sel_city = new_province_id
       
        }else if(this.show_type == 'see'){  // 查看省份不需要过滤省份
           
             this.sel_city  = [];        
        }       
    },
    onShow() {
        this.isLogin()
        if(this.type != 'not_delivery'){
        
            if(this.show_type == 'see'){
                this.title=this.language.freight_sheng.cksf;
            }else if(this.show_type == 'edit'){
                this.title=this.language.freight_sheng.bjsf;
            }else if(this.show_type == 'add'){
                this.title=this.language.freight_sheng.tjsf;
            }
        }else{
            this.title=this.language.freight_sheng.zdbps
        }
        this.shop_id = uni.getStorageSync('shop_id') ? uni.getStorageSync('shop_id') : this.$store.state.shop_id;
        
        this.axios()
    },
    methods: {
        noRepeat(arr){
            var newArr = [...new Set(arr)]
            return newArr
        },

        getCityInfo() {
            let data = {
                api: 'admin.AdminFreight.cityInfo',
            }
            this.$req.post({data}).then(res => {
                this.treeDataCopy = res.data.list
                this.treeDataCopy.forEach(item => {
                    const newItem = {
                        "id": item.id,
                        "district_level": item.district_level,
                        "children": item.children,
                        "district_name": item.district_name
                    };
                    this.treeData.push(newItem);
                });
                this.ready = true;
                this.isReady = true
            })
        },
        nodeMap(list) {
            list.forEach(item => {
                this.setHalfCheckedNodes(item)
            })
        },
        setHalfCheckedNodes (key) {
          const node = this.$refs.tree.getNode(key)
          if (node) { // 此处应判断当前节点的checked属性是否为true，是则不必半选
            node.indeterminate = true
          }
        },
        
        // 节点懒加载
        loadNode(node, resolve) {
            let me = this
            if (node.level === 0) {
              return resolve(_self.treeData);
            }
            if (node.level > 2) return resolve([]);
            if(node.data.children && node.data.children.length) {
                node.data.children = node.data.children.map(item => {
                    return {
                        ...item,
                        district_name: node.data.district_name ? `${node.data.district_name}-${item.district_name}` : `${node.data.district_name}-${item.district_name}`
                    }
                })
                if(_self.indeterminateList.length) {
                    setTimeout(function() {
                        _self.nodeMap(_self.indeterminateList)
                    },1500)
                }
                if(node.level == 2 && _self.defaultCheckedKeys.length) {
                    setTimeout(function() {
                        _self.changeChecked(node.childNodesId)
                    },1000)
                }
                resolve(node.data.children);
            } else {
                return resolve([])
            }
            
        },
        
        changeChecked(list) {
            list.forEach(item => {
                this.changeCheckeds(item.toString())
            })
        },
        
        changeCheckeds(key) {
            const node = this.$refs.tree.getNode(key)
            if (node && !this.defaultCheckedKeys.includes(key)) { // 此处应判断当前节点的checked属性是否为true，是则不必半选
              node.checked = false
            }
        },
        
        handleCheckChange(data, checked, indeterminate) { 
            debugger
            let res = this.$refs.tree.getCheckedNodes() // 获取当前选中的节点的id数组
           
            let idList = []
            let nameList = []
            res.forEach((item) => { 
                // if(item.district_level == 4) {
                //     nameList.push(item.district_name)
                // }
                nameList.push(item.district_name)
                idList.push(item.id)
            })
            // this.indeterminateList = res3
            this.idList = idList
            this.nameList = nameList
        },
        changeLoginStatus(){
			this.axios()
		},
        saveOk(){
            
            let res3 = this.$refs.tree.getHalfCheckedKeys() // 获取当前半选中的节点的id数组
           
            this.indeterminateList = res3
            let checkObj = {
                name: this.nameList.join(),
                id: this.idList.join(),
                indeterminateList: this.indeterminateList.join()
            }
            if(this.type == 'not_delivery'){
                uni.setStorageSync('not_delivery_province',checkObj)
            }else{
                uni.setStorageSync('freight_sheng',checkObj)
            }
            uni.navigateBack({
                delta: 1
            })
        },
        clickAll(){
            let checkAll = !this.checkAll
            this.area_list.filter(item=>{
                item.status = checkAll
            })
        },
        clickMe(item){
            item.status = !item.status
        },
        toUrl(url){
            uni.navigateTo({
                url
            })
        },
        axios(){
            let data = { 
                api:"mch.App.Mch.Get_sheng",
                shop_id:this.shop_id, // 店铺ID
                data:'',
                sel_city:this.sel_city
            }
            
            this.$req.post({data}).then(res=>{
                let { code, data, message } = res
                if(code == 200){
                    if(data && data.length>0){
                        let freight_id = this.freight_id.split(',')
                        data.filter(item=>{
                            item.status = false
                            
                            freight_id.filter(items=>{
                                if(items == item.GroupID){
                                    item.status = true
                                }
                            })
                        })
                    }else{
                        uni.showToast({
                            title: this.language.freight_sheng.noShengSelected,
                            icon: 'none',
                            duration:1500,
                        })
                    }
                    this.area_list = data 
                }else{
                    uni.showToast({
                        title: message,
                        icon: 'none'
                    })
                }
            }).catch((e) => {})
        }
    }
};
</script>
<style>
	    page{
	        background-color: #f4f5f6;
	    }
</style>
<style scoped lang="less">
    @import url("@/laike.less");
    .container_btn{
        z-index: 998;
        position: fixed;
        bottom: 0;
        width: 100%;
        height: 124rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: #ffffff;
        box-shadow: 0 0 0 0 rgba(0,0,0,0.2);
        padding-bottom: constant(safe-area-inset-bottom); /* 兼容 iOS < 11.2 */
        padding-bottom: env(safe-area-inset-bottom);
    }
    .container_bottomBtn{
        width: 686rpx;
        height: 92rpx;
        font-size: 32rpx;
        color: #fff;
        text-align: center;
        line-height: 92rpx;
        border-radius: 52rpx;
        margin: 0 auto;
        padding:0;
        .solidBtn()
    }
    .container{
        min-height: 100vh;
        
        .content{
            display: flex;
           
            padding-bottom: 150rpx;
            width: 100%;
            &-item{
                background-color: #FFFFFF;
                width: 100%;
                margin: 0 0px 53px 0px;
            }
            
            .list:nth-last-child(1)
            {
              border-bottom: none;
            }
            
            .list{
                display: flex;
                align-items: center;
                justify-content: space-between;
                height: 80rpx;
                border-bottom: 1px solid #EDEDED;
                margin: 0 16px;
                font-size: 36rpx;
                color: #010101;
                
                image{
                    width:40rpx;
                    height:40rpx;
                }
            }
        }
        
   
    }
    
    .address {
        margin-top: 24rpx;
        padding: 30rpx;
        font-size: 32rpx;
    }
</style>
