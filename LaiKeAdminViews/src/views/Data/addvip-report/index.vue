<template>
  <div class="iframe-content">
    <div class="box_one">
      <div class="box_two">
        <div class="box_head">
          <div>
            <span class="font_one">{{$t('addvip.hyyh')}}</span>
          </div>
          <div style="display: flex">
            <el-button :class="dep == 1 ? 'shaco_bt_dep' : 'shaco_bt_kep'" @click="changeData_one(1)">{{$t('addvip.jr')}}</el-button>
            <el-button :class="dep == 2 ? 'shaco_bt_dep' : 'shaco_bt_kep'" @click="changeData_one(2)">{{$t('addvip.zr')}}</el-button>
            <el-button :class="dep == 3 ? 'shaco_bt_dep' : 'shaco_bt_kep'" @click="changeData_one(3)">{{$t('addvip.bz')}}</el-button>
            <el-button :class="dep == 4 ? 'shaco_bt_dep' : 'shaco_bt_kep'" @click="changeData_one(4)">{{$t('addvip.by')}}</el-button>
          </div>
        </div>
        <div class="activeUser" id="activeUser"></div>
      </div>
      <div class="box_two">
        <div class="box_head">
          <div>
            <span class="font_one">{{$t('addvip.yhtj')}}</span>
          </div>
        </div>
        <div class="box_three">
          <div class="box_four">
            <div class="box_five">
              <div class="shaco_bottom">
                {{$t('addvip.yhzsl')}}
              </div>
              <div class="shaco_bottom" v-for="(item,index) in userCount" :key="index">
                {{ item.name }}
                <!-- {{$t('addvip.appd')}} -->
              </div>
              
            </div>
            <div class="box_six">
              <div class="shaco_bottom">
                {{ this.allNum }}
              </div>
              <div class="shaco_bottom" v-for="(item,index) in countNum" :key="index">
                {{item}}
              </div>
              
            </div>
          </div>
          <div style="width:50%;height:100%" id="userStatistics"></div>
        </div>
      </div>
    </div>
    <div class="box_one">
      <div class="box_two">
        <div class="box_head">
          <div>
            <span class="font_one">{{$t('addvip.xzyh')}}</span>
          </div>
          <div style="display: flex">
            <el-button :class="kep == 1?'shaco_bt_dep':'shaco_bt_kep'" @click="changeData_two(1)">{{$t('addvip.az')}}</el-button>
            <el-button :class="kep == 2?'shaco_bt_dep':'shaco_bt_kep'" @click="changeData_two(2)">{{$t('addvip.ay')}}</el-button>
            <el-button :class="kep == 3?'shaco_bt_dep':'shaco_bt_kep'" @click="changeData_two(3)">{{$t('addvip.an')}}</el-button>
          </div>
        </div>
        <div style="width: 100%;height:80%;padding: 0 24px 24px 24px;" id="newUsers"></div>
      </div>
      <div class="box_two">
        <div class="box_head">
          <div>
            <span class="font_one">{{$t('addvip.hytj')}}</span>
          </div>
          <div style="display: flex">
            <el-button :class="yep == 1 ? 'shaco_bt_dep' : 'shaco_bt_kep'" @click="changeData_three(1)">{{$t('addvip.az')}}</el-button>
            <el-button :class="yep == 2 ? 'shaco_bt_dep' : 'shaco_bt_kep'" @click="changeData_three(2)">{{$t('addvip.ay')}}</el-button>
            <el-button :class="yep == 3 ? 'shaco_bt_dep' : 'shaco_bt_kep'" @click="changeData_three(3)">{{$t('addvip.an')}}</el-button >
          </div>
        </div>
        <div style="width: 100%;height:80%;padding: 0 24px 24px 24px;" id="memberStatistics"></div>
        
      </div>
    </div>
       <div class="box_one" style="padding-bottom: 20px;">
        <div class="box_seven">
          <div class="box_head">
            <div>
              <span class="font_one">{{$t('addvip.yhxfph')}}</span>
            </div>
            <div>
              <el-select v-model="oneday" placeholder="请选择" class="shaco_select" @change="changeTime($event)">
                <el-option
                  v-for="item in dayList_one"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value">
                </el-option>
              </el-select>
            </div>
          </div>
          <div class="shaco_flex" style="padding: 0 24px 24px 24px;height: 78%;">
            <div style="width: 50%;height:100%;" id="Store1"></div>
            <div class="shaco_flex"  style="width: 42%;margin-left: 128px;height: 94%;">
              <div class="shacoe_box_fourteen">
                <div v-for="(item,index) in arrList_two.slice(0,5)" :key="index">
                    <div :class="index == 4?'shacoe_box_thirteen_two':'shacoe_box_thirteen'">
                      <div>
                        <div class="shaco_flex">
                          <div>
                            <img src="../../../assets/imgs/grade_one.png" class="grade_box_img" v-if="index == 0"/>
                            <img src="../../../assets/imgs/grade_two.png" class="grade_box_img" v-if="index == 1"/>
                            <img src="../../../assets/imgs/grade_three.png" class="grade_box_img" v-if="index == 2"/>
                            <img src="../../../assets/imgs/grade_four.png" class="grade_box_img" v-if="index == 3"/>
                            <img src="../../../assets/imgs/grade_five.png" class="grade_box_img" v-if="index == 4"/>
                          </div>
                          <div>
                            <span>{{ item.name }}</span>
                          </div>
                        </div>
                      </div>
                      <div style="margin-right: 51px;">
                        <span>{{ item.amount.toLocaleString() }}</span>
                      </div>
                    </div>
                </div>
              </div>
              <div style="width:347px">
                <div v-for="(item,index) in arrList_two.slice(5,10)" :key="index">
                  <div :class="index == 4?'shacoe_box_thirteen_two':'shacoe_box_thirteen'">
                      <div>
                        <div style="display: flex;">
                          <div>
                            <img src="../../../assets/imgs/grade_six.png" class="grade_box_img" v-if="index == 0"/>
                            <img src="../../../assets/imgs/grade_seven.png" class="grade_box_img" v-if="index == 1"/>
                            <img src="../../../assets/imgs/grade_eight.png" class="grade_box_img" v-if="index == 2"/>
                            <img src="../../../assets/imgs/grade_nine.png" class="grade_box_img" v-if="index == 3"/>
                            <img src="../../../assets/imgs/grade_ten.png" class="grade_box_img" v-if="index == 4"/>
                          </div>
                          <div>
                            <span>{{ item.name }}</span>
                          </div>
                        </div>
                      </div>
                      <div>
                        <span>{{ item.amount.toLocaleString() }}</span>
                      </div>
                    </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
  </div>
</template>

<script>
import main from '@/webManage/js/data/addvip-report/index'
export default main
</script>

<style scoped lang="less">
@import '../../../webManage/css/data/addvip-report/index.less';
.minh{
  min-width: 1px;
  min-height: 20px;
}
</style>
