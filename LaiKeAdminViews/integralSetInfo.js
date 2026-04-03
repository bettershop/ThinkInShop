import { getConfigInfo, addConfigInfo } from "@/api/plug_ins/integralMall";
import { VueEditor } from "vue2-editor";
import OSS from "ali-oss";
import Config from "@/packages/apis/Config";
import axios from "axios";
import { getStorage, setStorage } from "@/utils/storage";
export default {
    name: "mallSet",
    components: {
        VueEditor,
    },

    data() {
        return {
            isop: false,
            radio1: "3",
            actionUrl: Config.baseUrl,
            ruleForm: {
                isOpen: 1,
                integral_proportion: "",
                amsTime: "",
                issue_time: 0,
                overdue_set: "",
                slideshow: "",
                package_mail: 0,
                package_num: "",
                automatic_time: "",
                failure_time: "",
                afterSales_time: "",
                remind_day: "",
                remind_hours: "",
                auto_remind_day: "",
                autoCommentContent: "",
                content: "",
                publish_reward: [{ "num": 1, "integral": 1 }],
                like_reward: [{ "num": 1, "integral": 1 }],
                share_reward: [{ "num": 1, "integral": 1 }],
                isjfOpen: '' //积分商品兑换
            },

            rules: {
                integral_proportion: [
                    {
                        required: true,
                        message: this.$t("integralMall.mallSet.qsrgwzs"),
                        trigger: "blur",
                    },
                ],
                amsTime: [
                    {
                        required: true,
                        message: this.$t("integralMall.mallSet.qsrts"),
                        trigger: "blur",
                    },
                ],
                overdue_set: [
                    {
                        required: true,
                        message: this.$t("integralMall.mallSet.qsrjfgq"),
                        trigger: "blur",
                    },
                ],
                automatic_time: [
                    {
                        required: true,
                        message: this.$t("integralMall.mallSet.qsrzdsh"),
                        trigger: "blur",
                    },
                ],
                publish_reward: [
                    { required: true, message: '请至少添加一项发布种草作品奖励积分', trigger: 'blur' },
                    {
                        validator: this.validatePublishReward,
                        trigger: 'submit',
                    }
                ],
                like_reward: [
                    { required: true, message: '请至少添加一项点赞种草作品奖励积分', trigger: 'blur' },
                    {
                        validator: this.validatePublishReward,
                        trigger: 'submit',
                    }
                ],
                share_reward: [
                    { required: true, message: '请至少添加一项分享种草作品访问奖励积分', trigger: 'blur' },
                    {
                        validator: this.validatePublishReward,
                        trigger: 'submit',
                    }
                ],
                failure_time: [
                    {
                        required: true,
                        message: this.$t("integralMall.mallSet.qsrddsx"),
                        trigger: "blur",
                    },
                ],
                afterSales_time: [
                    {
                        required: true,
                        message: this.$t("integralMall.mallSet.qsrddsh"),
                        trigger: "blur",
                    },
                ],
                remind_day: [
                    {
                        required: true,
                        message: this.$t("integralMall.mallSet.qsrtx"),
                        trigger: "blur",
                    },
                ],
                auto_remind_day: [
                    {
                        required: true,
                        message: this.$t("integralMall.mallSet.qsrzd"),
                        trigger: "blur",
                    },
                ],
                autoCommentContent: [
                    {
                        required: true,
                        message: this.$t("integralMall.mallSet.qsrzd"),
                        trigger: "blur",
                    },
                ],
                remind_hours: [
                    {
                        required: true,
                        message: this.$t("integralMall.mallSet.qsrtx"),
                        trigger: "blur",
                    },
                ],
            },

            issueTimeList: [
                {
                    value: 0,
                    name: this.$t("integralMall.mallSet.shh"),
                },
                {
                    value: 1,
                    name: this.$t("integralMall.mallSet.fkh"),
                },
            ],
        };
    },

    created() {
        this.getConfigInfos();
    },
    beforeRouteLeave(to, from, next) {
        if (
            JSON.stringify(this.ruleForm) ==
            sessionStorage.getItem("ruleForm_integra")
        ) {
            next();
        } else {
            console.log("表单变化，询问是否保存");
            next(false);
            this.$confirm(
                this.$t("coupons.couponsSet.sjygx"),
                this.$t("coupons.ts"),
                {
                    distinguishCancelAndClose: true,
                    confirmButtonText: this.$t("coupons.okk"),
                    cancelButtonText: this.$t("coupons.ccel"),
                    type: "warning",
                }
            )
                .then(() => {
                    // this.submitForm();
                    next();
                })
                .catch(() => {
                    // next();
                    // next('/mall/plugInsSet/plugInsList')
                });
        }
    },

    methods: {
        validatePublishReward(rule, value, callback) {
            console.log(value);

            if (!value || value.length === 0) {
                return callback(new Error('请至少添加一个奖励积分'));
            }
            for (let i = 0; i < value.length; i++) {
                if (Number(value[i].integral || 0) <= 0 || Number(value[i].num || 0) <= 0) {
                    return callback(new Error('请设置内容'));
                }
            }

            callback();
        },
        addList(e) {
            console.log(this.ruleForm[e], 'this.ruleForm[e]')
            this.ruleForm[e].push({ 'num': '', 'integral': '' })
        },
        minList(e, index) {
            this.ruleForm[e].splice(index, 1)
        },
        back() {
            this.$router.push({
                path: "/mall/plugInsSet/plugInsList",
            });
        },
        agreeChange() {
            this.$message({
                message: this.$t("integralMall.mallSet.qhff"),
                type: "warning",
                offset: 100,
            });
        },
        async getConfigInfos() {
            const res = await getConfigInfo({
                api: "plugin.integral.AdminIntegral.getConfigInfo",
            });
            console.log(res);
            const Config = res.data.data.config;
            if (!Config) {
                sessionStorage.setItem(
                    "ruleForm_integra",
                    JSON.stringify(this.ruleForm)
                );
                return;
            }
            this.ruleForm.isOpen = Config.status;
            this.ruleForm.isjfOpen = Config.member_can;
            this.ruleForm.slideshow = Config.bg_img;

            this.ruleForm.integral_proportion = Config.proportion;
            this.ruleForm.amsTime = Config.ams_time;
            this.ruleForm.issue_time = Config.give_status;
            this.ruleForm.overdue_set = Config.overdue_time / 86400;

            this.ruleForm.package_mail = Config.package_settings;
            this.ruleForm.package_num = Config.same_piece;
            this.ruleForm.automatic_time = Config.auto_the_goods / 86400;
            this.ruleForm.failure_time = Config.order_failure / 3600;
            this.ruleForm.afterSales_time = Config.order_after / 86400;
            this.ruleForm.remind_day = parseInt(
                Math.floor(Config.deliver_remind / 86400)
            );
            this.ruleForm.remind_hours = Math.floor(
                ((Config.deliver_remind - this.ruleForm.remind_day * 3600 * 24) %
                    86400) /
                3600
            );
            this.ruleForm.auto_remind_day = Config.auto_good_comment_day / 86400;
            this.ruleForm.autoCommentContent = Config.auto_good_comment_content;
            this.ruleForm.content = Config.content;
            this.ruleForm.publish_reward = JSON.parse(Config.publish_reward) || [{ "num": '', "integral": '' }];
            this.ruleForm.like_reward = JSON.parse(Config.like_reward) || [{ "num": '', "integral": '' }];
            this.ruleForm.share_reward = JSON.parse(Config.share_reward) || [{ "num": '', "integral": '' }]

            this.isop = this.ruleForm.auto_remind_day > 0 ? true : false;
            sessionStorage.setItem("ruleForm_integra", JSON.stringify(this.ruleForm));
        },

        // handleImageAdded(file, Editor, cursorLocation, resetUploader) {
        //     let random_name = new Date().getTime() + '.' + file.name.split('.').pop()
        //     const client = new OSS({
        //       region: "oss-cn-shenzhen.aliyuncs.com",
        //       secure: true,
        //       endpoint: 'oss-cn-shenzhen.aliyuncs.com',
        //       accessKeyId: "LTAI4Fm8MFnadgaCdi6GGmkN",
        //       accessKeySecret: "NhBAJuGtx218pvTE4IBTZcvRzrFrH4",
        //       bucket: 'laikeds'
        //     });
        //     client.multipartUpload(random_name, file)
        //     .then(res => {
        //       console.log(res);
        //       Editor.insertEmbed(cursorLocation, 'image', res.res.requestUrls[0])
        //       Editor.setSelection(length + 1)
        //       resetUploader()
        //     })
        //     .catch(err => {
        //       console.log(err)
        //     })
        // },
        handleImageAdded(file, Editor, cursorLocation, resetUploader) {
            var formData = new FormData();
            formData.append("file", file); //第一个file 后台接收的参数名
            axios({
                url: this.actionUrl, //上传路径
                method: "POST",
                params: {
                    api: "resources.file.uploadFiles",
                    storeId: getStorage("laike_admin_userInfo").storeId,
                    groupId: -1,
                    uploadType: 2,
                    accessId: this.$store.getters.token,
                },
                data: formData,
            })
                .then((result) => {
                    let url = result.data.data.imgUrls[0]; // 返回给你的图片路径
                    Editor.insertEmbed(cursorLocation, "image", url);
                    // Editor.setSelection(length + 1)
                    resetUploader();
                })
                .catch((err) => {
                    console.log(err);
                });
        },

        submitForm(formName) {
            this.$refs[formName].validate(async (valid) => {
                console.log(this.ruleForm);
                if (valid) {
                    try {
                        addConfigInfo({
                            api: "plugin.integral.AdminIntegral.addConfigInfo",
                            status: this.ruleForm.isOpen,
                            imgUrls: this.ruleForm.slideshow,
                            proportion: this.ruleForm.integral_proportion,
                            amsTime:
                                this.ruleForm.issue_time == 0 ? this.ruleForm.amsTime : "",
                            giveStatus: this.ruleForm.issue_time,
                            overdueTime: parseInt(this.ruleForm.overdue_set) * 86400,
                            member_can: this.ruleForm.isjfOpen,
                            isFreeShipping: this.ruleForm.package_mail,
                            goodsNum: Math.round(this.ruleForm.package_num),
                            autoReceivingGoodsDay: Math.round(this.ruleForm.automatic_time),
                            orderInvalidTime: Math.round(this.ruleForm.failure_time),
                            returnDay: Math.round(this.ruleForm.afterSales_time),
                            deliverRemind: parseInt(this.ruleForm.remind_hours) * 3600,
                            // deliverRemind: parseInt(this.ruleForm.remind_day) == 0 ? parseInt(this.ruleForm.remind_hours) * 3600 : parseInt(this.ruleForm.remind_day) * 86400  + parseInt(this.ruleForm.remind_hours) * 3600,
                            autoCommentDay: Math.round(this.ruleForm.auto_remind_day),
                            autoCommentContent: this.ruleForm.autoCommentContent,
                            content: this.ruleForm.content,

                            publish_reward: JSON.stringify(this.ruleForm.publish_reward),
                            like_reward: JSON.stringify(this.ruleForm.like_reward),
                            share_reward: JSON.stringify(this.ruleForm.share_reward),

                        }).then((res) => {
                            if (res.data.code == "200") {
                                this.$message({
                                    message: this.$t("plugInsSet.czcg"),
                                    type: "success",
                                    offset: 100,
                                });
                                sessionStorage.setItem(
                                    "ruleForm_integra",
                                    JSON.stringify(this.ruleForm)
                                );
                                setTimeout(() => {
                                    this.$router.push({
                                        path: "/mall/plugInsSet/plugInsList",
                                    });
                                }, 1000);
                            }
                        });
                    } catch (error) {
                        this.$message({
                            message: error.message,
                            type: "error",
                            showClose: true,
                            offset: 100,

                        });
                    }
                } else {
                    console.log("error submit!!");
                    return false;
                }
            });
        },

        oninput2(num) {
            var str = num;
            str = str.replace(/[^\.\d]/g, "");
            str = str.replace(".", "");

            return str;
        },
        oninput3(num) {
            var str = num;
            var str = num;
            str = str.replace(/[^\.\d]/g, "");
            str = str.replace(".", "");
            if (str == 0) {
                return ''
            } else {
                return str;
            }

            // var reg = /^[1-9]\d*$/;
            // if (reg.test(str)) {
            //   return str;
            // } else {
            //   return "";
            // }
        },
        mychange(e) {
            if (e) {
                this.ruleForm.auto_remind_day = 1;
            } else {
                this.ruleForm.auto_remind_day = 0;
            }
        },
    },
};
