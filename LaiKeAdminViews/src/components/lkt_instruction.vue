<template>
    <div class="box" @click="selectDirection">
        <div class="heng top" data-type="Top" :class="direction =='Top' && 'active'"></div>
        <div class="shu right" data-type="Right" :class="direction =='Right' && 'active'"></div>
        <div :class="`display-box ${direction}`" >
           <span>→</span>
        </div>
        <div class="heng bottom" data-type="Bottom" :class="direction =='Bottom' && 'active'"></div>
        <div class="shu left" data-type="Left" :class="direction =='Left' && 'active'"></div>
    </div>
</template>

<!-- 方向指示 组件 -->
<script>
    export default {
        name: 'lktinstruction',
        data(){
            return{
                direction:''
            }
        },
        methods:{
            selectDirection({target }){
                const directionElement = target.closest('[data-type]');
                if (directionElement) {
                    const type = directionElement.dataset.type;
                    this.direction = type;
                }
                this.$emit('orientation',this.direction)
            }
        }
    }
</script>

<style scoped lang='less'>
*{
    user-select: none;
}
.active{
    background-color: #2c8ff5 !important;
    transition: all 0.3s ease;
}
.display-box.Top {
    span{
        transform: rotate(270deg) !important; /* 上 */
    }
}
.display-box.Right {
    span{
        transform: rotate(0deg) !important;  /* 右 */
    }

}
.display-box.Bottom {
    span{
        transform: rotate(90deg) !important; /* 下 */
    }
}
.display-box.Left {
    span{
        transform: rotate(180deg) !important; /* 左 */
    }
}
.box {
    position: relative;

    >div{
        position: absolute;
        cursor: pointer;
        // background-color: #d8dce6;
    }

        .shu {
            width: 15%;
            text-align: center;
            height: 60%;
            margin-top: 50%;
            transform: translateY(-50%);
            background-color: #d8dce6;
        }
        .heng{
            text-align: center;
            height:  15%;
            width: 60%;
            margin-left: 50%;
            transform: translateX(-50%);
            background-color: #d8dce6
        }
        .display-box{
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            font-size: 65%;
            width: 57%;
            height: 57%;
            text-align: center;
            border-radius: 10%;
            font-weight: bold;
            background-color: #d8dce6;
            line-height: normal;
            color: #fff;

            display: flex;
            justify-content: center;
            align-items: center;
        }
        .top {
            left: 0px;
            top: -2%;
        }
        .right {
            top:0px;
            right: -2%;
        }
        .bottom {
            bottom: -2%;
            left: 0px;
        }
        .left {
            top: 0px;
            left: -2%;
        }
}
</style>
