<template>
	<view class="">
		<heads :title="language.login.page2.xzgjordq" :ishead_w="2" :bgColor="bgColor" :titleColor="'#333333'"></heads>
		<view class="box">
			<view class="select-input">
				<input v-model="selectValue" placeholder="" @input="getCountey" :placeholder="language.login.page2.ssgjordq"/>
			</view>
		</view>
		<view class="">
			<view style="width: 98%;" v-for="(items,itemToUp) in classifiedWords" :key="itemToUp">
				<view class="man region-title">{{items.letter}}</view>
				<view  style="border-bottom: 1rpx solid #F4F5F6;" >
					<view class="region man" v-for="(item,index) in items.list" :key="index" @tap="queryItem(item)">
						<text>{{item.name}}</text> 
						<text>+{{item.code2}}</text> 
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	export default {
	    data () {
	        return {
				bgColor:[{
				        item: '#ffffff'
				    },
				    {
				        item: '#ffffff'
				    }
				],
				selectValue:'', 
                classifiedWords: {} // 这里初始化空的classifiedWords，稍后赋值
			}
		},
		 created() { 
		 	this.queryList()
            const pages = getCurrentPages();
		 },
		 
		 mounted() {
            const pages = getCurrentPages();
		 },
		methods:{
                queryList() {
                    try {
                        const stored = uni.getStorageSync('regionAndCountry');
                        if (stored && !this.selectValue) {  // ← 关键：只有无搜索词时才用全量缓存
                            let parsed = JSON.parse(stored);
                            if (Array.isArray(parsed)) {
                                this.classifiedWords = parsed;
                                return;
                            }
                            if (typeof parsed === 'object' && !Array.isArray(parsed)) {
                                this.classifiedWords = Object.keys(parsed)
                                    .sort()
                                    .map(letter => ({ letter, list: parsed[letter] }));
                                uni.setStorageSync('regionAndCountry', JSON.stringify(this.classifiedWords));
                                return;
                            }
                        }
                    } catch (e) {
                        console.error(e);
                    }
                    this.getCountey(); 
                },
				async getCountey(){
                    if(!this.selectValue && uni.getStorageSync('regionAndCountry')){
                        this.classifiedWords = JSON.parse(uni.getStorageSync('regionAndCountry'))
                        return
                    }
					const data = {
						api:'app.user.getItuList',
						keyword:this.selectValue
					}
					await this.$req.post({data}).then(res => {
					    if(res.code == 200){
					        const list = res.data 
                            
                            
                            const sortedList = list.sort((a, b) => {
                              const aLetter = a.name.charAt(0).toUpperCase();
                              const bLetter = b.name.charAt(0).toUpperCase();
                              return aLetter.localeCompare(bLetter); // 字母正序，localeCompare兼容特殊字母
                            });
                            
                            // 步骤2：归并成分组对象
                            const groupObj = sortedList.reduce((acc, word) => {
                              const firstLetter = word.name.charAt(0).toUpperCase();
                              if (!acc[firstLetter]) {
                                acc[firstLetter] = [];
                              }
                              acc[firstLetter].push(word);
                              return acc;
                            }, {});
                            
                            // 步骤3：将对象的字母键排序，转成有序数组（核心：保证A→Z顺序，不受对象属性限制）
                            this.classifiedWords = Object.keys(groupObj)
                              .sort() // 对字母键再次正序排序（A→Z）
                              .map(letter => ({
                                letter, // 首字母（A/B/C...）
                                list: groupObj[letter] // 对应字母的单词数组
                              }));
                              
					        // 排序并分类
					     //    this.classifiedWords = list.sort().reduce((acc, word) => {
					     //    	// 获取单词的首字母（大写）
					     //    	let firstLetter = word.name.charAt(0).toUpperCase();
					     //    	// 如果 acc 中还没有这个首字母的数组，则创建一个新的空数组
					     //    	if (!acc[firstLetter]) { 
								  // this.$set(acc,firstLetter,[])
					     //    	}
					     //    	// 将当前单词添加到对应首字母的数组中
					     //    	acc[firstLetter].push(word);
					     //    	return acc;
					     //    }, {}); 
                            
                            if(!this.selectValue){
                                uni.setStorageSync('regionAndCountry',JSON.stringify(this.classifiedWords))
                            }
					    }else{
							uni.showToast({
							    title: res.message,
							    duration: 1000,
							    icon: 'none'
							})
						}
					}) 
				},
				queryItem(item){
					uni.setStorageSync('diqu',JSON.stringify(item)) 
					uni.navigateBack()
				}
		}
		
	}
</script>

<style scoped lang="less">
	.box{
		display: flex;
		justify-content: center;
	}
	.select-input{
		background: #F4F5F6;
		border-radius: 100rpx;
		height: 60rpx;
		width: 88%;
		display: flex;
		align-items: center;
		margin-top: 26rpx;
			
		input {
			margin-left: 20rpx;
			text-indent: 30rpx;
			width: 95%;
		}
	}
	.region-title {
		font-size: 40rpx;
	}
	.region{
	
		color: #333333;
		display: flex;
		justify-content: space-between;
		text:nth-child(2){
			color: #FE6667;
		}
	}
	.man{
		margin: 30rpx;
	}
</style>
