<template>
    <view class="container">
        <heads :title="language.chooseArea.title" :border="true"></heads>
        <view class="topBox">
            <view class="searchBox">
                <view class="searchBox_left">
                    <image class='searchImg' :src="serchimg"></image>
                    <input type="text" v-model="searchKey" :placeholder="language.chooseArea.search_placeholder"/>
                    <image v-show="searchKey.length > 0" @click="cleardata" class="cancel" :src="sc_icon" mode=""></image>
                </view>
                
                <view class="searchBox_btn" @tap="search">{{language.chooseArea.search}}</view>
            </view>
            
            <view class="area">
                <view class="area_left">
                    <text>{{language.chooseArea.current_location}}：</text>
                    <view class="area_left_item" v-if="choose_area.length>0">
                        <text v-for="item,index of choose_area" :key="index">{{item.G_CName}}</text>
                    </view>
                    <view v-else>{{language.chooseArea.change_address}}</view>
                </view>
                
                <view class="area_right" @tap="seeAreaClick">
                    <text>{{language.chooseArea.Switch}}</text>
                    <image :src="jianX" :style="{transform: seeAreaFlag?'rotate(180deg)':''}"></image>
                </view>
            </view>
            
            <view class="areaItemBox" v-if="seeAreaFlag">
                <view class="areaItem" :class="{active: choose_area[1]&&choose_area[1].GroupID == item.GroupID}" v-for="item,index of area_list" :key="index" @tap="clickArea(item,1)">{{item.G_CName}}</view>
            </view>
        </view>
        <scroll-view scroll-y="true" class="content" @scroll="contentScroll" :scroll-into-view="scrollKey" :scroll-with-animation="true">
            <view v-for="items,indexs of city_list" :id="indexs" :key="indexs">
                <view v-if="!filterKey" class="listTitle">{{indexs}}</view>
                
                <template v-for="item,index of items">
                    <view v-if="!item.hidden" :key="index" class="list" @tap="clickArea(item,0)" :class="{active: choose_area.length>0&&choose_area[0].GroupID == item.GroupID}">{{item.G_CName}}</view>
                </template>
                
            </view>
            
            <view class="listRight" v-if="!filterKey">
                <view v-for="item,index of city_list" :key="index" :class="{active: index == titleKey}" @tap="changeTitle(index)">{{index}}</view>
            </view>
        </scroll-view>
    </view>
</template>

<script>
export default {
    data() {
        return {
            title: '选择地区',
            serchimg: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL+'images/icon1/searchNew.png',
            jianX: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL+'images/icon1/jianX.png',
            sc_icon: this.LaiKeTuiCommon.LKT_ROOT_VERSION_URL+'images/icon1/delete2x.png',
            city_list: {},
            area_list: [],
            choose_area: [],
            level: 0,
            GroupID: '',
            titleKey: 'A',
            scrollKey: 'A',
            
            seeAreaFlag: false,
            searchKey: '',
            filterKey: '',
            
            keyTop: [],
            boxTop: ''
        };
    },
    onBackPress() {
        if(this.choose_area.length == 0){
            uni.showToast({
                title: this.language.chooseArea.change_address,
                icon: 'none'
            })
            return true
        }
    },
    onLoad(option) {
        let region = uni.getStorageSync('region')
        this.choose_area = [
            {
                G_CName: region.city,
                GroupID: region.city_GroupID
            },
            {
                G_CName: region.district,
                GroupID: region.district_GroupID
            }
        ]
        this.GroupID = region.city_GroupID
        this.axios();
        
        this.$nextTick(()=>{
            const query = uni.createSelectorQuery()
            query.select('.content').fields({
                rect: true,
            }, data => {
                this.boxTop = data.top
                
            }).exec();
        })
    },
    methods: {
        cleardata(){
            this.searchKey = ''
        },
        search(){
            this.filterKey = this.searchKey
            
            let city_list = JSON.parse(JSON.stringify(this.city_list))
            
            for(let i in city_list){
                city_list[i].filter(item=>{
                    if(!item.G_CName.includes(this.filterKey)){
                        item.hidden = true
                    }else{
                        item.hidden = false
                    }
                })
            }
            this.city_list = city_list
        },
        changeTitle(key){
            this.titleKey = key;
            this.scrollKey = key;
        },
        contentScroll(e){
            let index = this.keyTop.findIndex(item=>{
                return item.top>e.detail.scrollTop
            })
            
            this.titleKey = this.keyTop[index-1].key
            
        },
        seeAreaClick(){
            this.seeAreaFlag = !this.seeAreaFlag
        },
        clickArea(item,index){
            if(index == 0){
                this.choose_area = []
            }
            this.choose_area.splice(index,1,item)
            this.GroupID = item.GroupID
            
            if(this.choose_area.length == 1){
                this.axios(true)
            }else{
                let region = {
                    city: this.choose_area[0].G_CName,
                    city_GroupID: this.choose_area[0].GroupID,
                    district: this.choose_area[1].G_CName,
                    district_GroupID: this.choose_area[1].GroupID,
                }
                uni.setStorageSync('region',region)
                
                this.getLocation(this.choose_area[1].GroupID)
                
                uni.navigateBack({
                    delta: 1
                })
            }
            
        },
        toUrl(url){
            uni.navigateTo({
                url
            })
        },
        getLocation(GroupID){
            let data = {
                api: 'app.index.get_Longitude_and_latitude',
                GroupID:GroupID, // 市ID
            }
            
            this.$req.post({data}).then(res=>{
                uni.setStorageSync('longitude',res.data.longitude)
                uni.setStorageSync('latitude',res.data.latitude)
            })
        },
        axios(type){
            let data = {
                api: 'app.index.get_location',
                GroupID:typeof(this.GroupID) == "undefined"?"":this.GroupID, // 市ID
            }
            this.$req.post({data}).then(res=>{
                let { code, data, message } = res
                
                if( code == 200 ){
                    this.city_list = data.list
                    this.area_list = data.xian
                    
                    if(this.keyTop.length == 0){
                        const query = uni.createSelectorQuery()
                        
                        setTimeout(()=>{
                            let keysArr = Object.keys(this.city_list)
                            
                            keysArr.filter((item,index)=>{
                                query.select('#'+item).fields({
                                    rect: true,
                                }, data => {
                                    let itemTop = data.top - this.boxTop
                                    
                                    this.keyTop.splice(index,1,{key: item,top: itemTop})
                                }).exec();
                            })
                        },200)
                        
                    }
                    
                    if(type){
                        let region = {
                            city: this.choose_area[0].G_CName,
                            city_GroupID: this.choose_area[0].GroupID,
                            district: this.area_list[0].G_CName,
                            district_GroupID: this.area_list[0].GroupID,
                        }
                        uni.setStorageSync('region',region)
                        
                        this.getLocation(this.area_list[0].GroupID)
                        
                        uni.navigateBack({
                            delta: 1
                        })
                    }
                    
                    this.search()
                }else{
                    uni.showToast({
                        title: message,
                        icon: 'none'
                    })
                }
            })
        }
    },
};
</script>

<style lang="less">
@import url("@/laike.less");
@import url('../../static/css/home/chooseArea.less');
</style>
