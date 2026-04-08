<template>
    <div>
        <el-dialog
            title="选择链接"
            :visible.sync="dialogVisible"
            width="860px" >
             
                <div style="display: flex;">
                    <div class="left_box"> 
                         <el-tree
                            :data="catalogue"
                            @node-click="handleNodeClick"
                            :props="props"
                            node-key="id"
                            :render-content="renderContent"
                            :current-node-key="selectedNodeId"
                        >
                            <!-- 自定义节点插槽 -->
                            <template #default="{ node, data }">
                                <span :class="{ 'tree-node-selected': selectedNodeId == data.id }">
                                    {{ node.name }} 
                                </span>
                            </template>
                        </el-tree>
                    </div>
                    <div style="flex: 1;" class="right_box"> 
                        <!-- 右侧模板区域 -->
                         <template v-if="temolateType == 'linkList'">
                            <div>
                                <div v-for="(item,index) in children" :key="index">
                                    <div class="title">{{ item.name }}</div>
                                    <div class="list">
                                        <div class="list-son" v-for="(sonitem,sonIndex) in item.children" :key="sonIndex">
                                            {{ sonitem.name }}
                                        </div>
                                    </div>
                                </div>
                            </div>
                         </template>
                       
                          <template v-else-if=" temolateType !='custom'" >
                            <div class="harder-box"  v-if="temolateType == 'product'">
                                 <el-input
                                    clearable
                                    class="input-name" 
                                    v-model="productTitle"
                                    :placeholder="$t('physicalgoods.qsrspmc')"
                                ></el-input>
                                <el-button class="fontColor" @click="reset">{{
                                        $t('DemoPage.tableExamplePage.reset')
                                    }}</el-button>
                                    <el-button class="bgColor" type="primary" @click="demand">{{
                                        $t('DemoPage.tableExamplePage.demand')
                                    }}</el-button>
                            </div>
                            <div>
                                 <el-table 
                                    :data="children"
                                    stripe 
                                    style="width: 100%;margin-top: 20px;"
                                    :height="tablehtight"
                                    :key="temolateType">
                                    
                                    <el-table-column  label=""  width="80">
                                        <template slot-scope="{row}">
                                            <div class="radio-none" label="#">
                                                <el-radio v-model="radio" :label="row.cid"></el-radio>
                                            </div>
                                        </template>
                                    </el-table-column>

                                    <template  v-if="temolateType == 'product_category'">
                                        <!-- 分类呈现 -->
                                        <el-table-column  prop="cid" label="ID" width="120"> </el-table-column>
                                        <el-table-column prop="pname" label="分类名称" ></el-table-column> 
                                    </template>
                                    <template  v-if="temolateType == 'product'">
                                        <!-- 商品呈现 -->
                                        <el-table-column  prop="id" label="ID" width="70"> </el-table-column>
                                        <el-table-column   label="商品图片" width="80">
                                            <template slot-scope="{row}">
                                                <div class="goods_img">
                                                    <img :src="row.imgurl" alt="" >
                                                </div>
                                            </template>
                                        </el-table-column> 
                                        <el-table-column prop="keyword" label="商品名称" ></el-table-column> 
                                    </template>
                                    <template  v-if="   ['integral','combination','seckill'].includes(temolateType)">
                                        <!-- 积分商品呈现 -->
                                        <el-table-column  prop="id" label="ID" width="70"> </el-table-column>
                                        <el-table-column   label="商品图片" width="80">
                                            <template slot-scope="{row}">
                                                <div class="goods_img">
                                                    <img :src="row.imgurl" alt="" >
                                                </div>
                                            </template>
                                        </el-table-column> 
                                        <el-table-column prop="pname" label="商品名称" ></el-table-column> 
                                    </template>

                                </el-table>
                            </div>
                            <div class="footer-box" v-if=" temolateType != 'product_category' ">
                                 <el-pagination 
                                    @current-change="handleCurrentChange"
                                    :current-page="pageNumber" 
                                    :page-size="10"
                                    layout="total, prev, pager, next, jumper"
                                    :total="total">
                                </el-pagination>
                            </div>
                         </template>
                         <template v-else>
                                <div style="display: flex;justify-content: center;align-items: center; height: 100%;">
                                    <el-form  :rules="rules" :model="formData" inline>
                                        <el-form-item label="跳转路径" prop="url" >
                                            <el-input class="input-name" v-model="formData.url" placeholder="请输入正确的跳转路径"></el-input>
                                        </el-form-item>
                                    </el-form>
                                </div>
                         </template>
                    </div>
                </div>
                <div class="footer">
                    <div>
                        <button class="footer-btn">取消</button>
                        <button class="footer-btn primary" style="margin-left: 10px;">确定</button>
                    </div>
                </div>

            </el-dialog>
    </div>
</template>

<script>
import {  getAdminLinkClassAll,getAdminLinkGoodAll,getAdminLinkPointsGoodAll } from "@/api/linkDialog/index";
import {catalogue} from '@/enums/DIY/catalogue' 
export default {
    name: 'linkDialog',
    data() {
        return {
            catalogue:{},
            dialogVisible:false,
            props:{
                label: 'name',
            }, 
            selectedNodeId: null, // 记录当前选中节点的ID
            temolateType:'linkList',// 模板启用类型
            children:[], //左侧数据展示区 模型
            radio:'',
            productTitle:"", // 商品搜索 商品名称 商品店铺 时使用
            pageNumber: 1,
            total:0,
            tablehtight:'',
            formData:{
                url:""
            },
            rules:{
                url: [
                    { required: true, message: '请输入跳转路径', trigger: 'blur' }
                ]
            }
        }
    },
    watch:{
        temolateType:{
            handler(newVal,lodVal){
                this.tablehtight = newVal == 'product'?330:newVal != 'product_category'?390:''
            }
        }
    },
    mounted(){
        const {data} =catalogue
        this.catalogue = data
        console.log('catalogue',this.catalogue)
    },

    methods: {
        // 
         async handleNodeClick(data) { 
            if(data.pid == 0 ){
                return
            }

            if(data.id){
                this.selectedNodeId = data.id;
            }
            this.pageNumber = 1 //重置 分页码
            this.total = 0
            this.children = []
            try{ 
                const noe = String(this.selectedNodeId || '').split('-')[0] 
                console.log(noe)
                if(noe == 0){
                    this.children = data.sonChildren
                    this.temolateType = 'linkList'
                }else{
                     this.temolateType = data.type
                    let res = null
                    // 请求对应的AIP

                    if(noe == 8){
                        // 商品分类请求
                         res = await getAdminLinkClassAll() 
                    }
                    if(noe == 9){
                        // 普通商品 请求
                        res = await getAdminLinkGoodAll({
                            productTitle: this.productTitle,
                            pageNo: this.pageNumber
                        })
                    }
                    if(noe == 13){
                        res = await getAdminLinkPointsGoodAll({
                            pageNo: this.pageNumber
                        })
                    }
                    console.log( this.temolateType,res)
                    try{
                        if(res.data.code == 200){

                            if(noe == 8){
                                this.children = res.data.data.classInfo
                            }else if(noe == 9 ){ 
                                this.children =  res.data.data.list
                            }else if(noe == 13 ){ 
                                 this.children =  res.data.data.list
                            }
                             
                            this.total  = res.data.data.total ||  res.data.data.num 
                        
                        }else{
                            this.$message.error(res.message)
                        }
                    }catch(e){
                        console.error('linkDialog Component AIP error::'+e)
                    }
                    
                }
            }catch(e){
                console.error('linkDialog Component error::'+e)
            }
        },
         renderContent(h, { node, data }) {
            // 判断当前节点是否为选中节点，是则加自定义样式类
            const isSelected = this.selectedNodeId === data.id; 
            return h(
                "span",
                {
                class: {
                    "tree-node-label": true, // 基础样式
                    "tree-node-selected": isSelected // 选中样式
                }
                },
                node.label // 节点文字
            );
        },
        reset(){
            this.productTitle = ''
            this.handleCurrentChange(1)
        },
        demand(){
            this.handleCurrentChange(1)
        },
       async handleCurrentChange(val){
            this.pageNumber = val
            let res = {}
              if(this.temolateType == 'product' ){ 
                  res =  await getAdminLinkGoodAll({
                      productTitle: this.productTitle,
                      pageNo: this.pageNumber
                    })
                }else if(this.temolateType == 'integral'){
                      res = await getAdminLinkPointsGoodAll({
                            pageNo: this.pageNumber
                        })
                }
                console.log(res)
            if(res.data.code == 200){
                 
                if(this.temolateType == 'product' ){ 
                    this.children =  res.data.data.list
                    this.total  = res.data.data.total
                }else if(this.temolateType == 'integral' ){ 
                    this.children =  res.data.data.list
                    this.total  = res.data.data.num
                }
            }
         
        }
    }
}
</script>

<style lang="less" scoped>
    

    /deep/ .el-tree-node >.el-tree-node__content{ 
            .el-tree-node__label {
                font-size: 20px;
            }
    }
    /deep/ .el-tree-node:click>.el-tree-node__content {
        color:#1890ff;
        background-color: unset;
    }
     /deep/ .tree-node-selected {
        color: #409eff; /* Element UI 主题色，可替换成你想要的颜色，比如 red */
        font-weight: 600; /* 可选：加粗突出 */
    }
    /deep/ .el-dialog__body{
        padding:0px;
    }
    /deep/ .tree-node-label{
        display: inline-block;
        margin: 0;
        padding: 0 4px;
        border-radius: 3px;
        cursor: pointer;
        vertical-align: top;
        color: #515a6e;
        transition: all .2s ease-in-out;
    }
    /deep/ .cell {
        text-align: center;
    }
    /deep/.radio-none .el-radio__label{
        display: none;
    }
    /deep/ .el-table th.el-table__cell{
        background-color: #f3f8fe !important; 
    }
    .left_box{
        width: 171px;
        height: 470px;
        border-right: 1px solid #eee;
        overflow-x: hidden;
        overflow-y: auto
        }
    .right_box {
        margin-left: 23px;
        font-size: 13px;
        font-family: PingFang SC;
        width: 645px;
        height: 470px;
        overflow-x: hidden;
        overflow-y: auto;
    }
    .title {
        color: #000;
        font-weight: 700;
    }
    .list {
        margin-top: 19px;
        display: flex;
        flex-wrap: wrap;
        &-son {
            font-weight: 400;
            color: rgba(0, 0, 0, .85);
            background: #fafafa;
            border-radius: 3px;
            text-align: center;
            padding: 7px 30px;
            margin-right: 10px;
            margin-bottom: 18px;
            cursor: pointer;
        }
    }
    .footer{
        border-top: 1px solid #e8eaec;
        padding: 12px 18px 12px 18px;
        text-align: right;
        &-btn{
            display: inline-block;
            margin-bottom: 0;
            font-weight: 400;
            text-align: center;
            vertical-align: middle;
            touch-action: manipulation;
            cursor: pointer;
            background-image: none;
            border: 1px solid transparent;
            white-space: nowrap;
            user-select: none;
            height: 32px;
            padding: 0 15px;
            border-radius: 4px;
            transition: color .2s linear, background-color .2s linear, border .2s linear, box-shadow .2s linear;
            color: #515a6e;
            background-color: #fff;
            border-color: #dcdee2;
        }
        .primary{
            color: #fff;
            background-color: #2d8cf0;
            border-color: #2d8cf0;
        }
    }
    .goods_img{
        width: 36px;
        height: 36px;
        border-radius: 4px;
        cursor: pointer; 
        img {
            width: 100%;
            height: 100%;
        }
    }
    /deep/ .harder-box{
        display: flex;
         .input-name {
          width: 200px;
          height: 40px;
          margin-right: 8px;
          input {
            width: 200px;
            height: 40px;
          }
        }
    }
    .input-name {
          width: 200px;
          height: 40px;
          margin-right: 8px;
          input {
            width: 200px;
            height: 40px;
          }
        }
    .footer-box{
        margin-top: 22px;
        text-align: end;
        padding-right: 20px;
    }
</style>