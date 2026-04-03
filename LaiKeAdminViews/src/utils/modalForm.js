// import request from '@/plugins/request/index2';
import Modal from './modal'
import { Message, Spin, Notice } from 'iview'
import qs from 'qs'
import { addClass } from '../api/uploadPictures'
import ElementUI from 'element-ui';
let modalInstance

function getModalInstance(render = undefined) {
    modalInstance = modalInstance || Modal.newInstance({
        closable: true,
        maskClosable: true,
        footerHide: true,
        render: render
    })

    return modalInstance
}

function alert(options) {
    const render = ('render' in options) ? options.render : undefined
    let instance = getModalInstance(render)

    options.onRemove = function () {
        modalInstance = null
    }

    instance.show(options)
}

export default function (formRequestPromise, { width = '700' } = { width: '700' }) {
    return new Promise((resolve, reject) => {
        const msg = Message.loading({
            content: 'Loading...',
            duration: 0
        });
        formRequestPromise.then(({ data }) => {
            if (data.status === false) {
                msg();
                return Notice.warning({
                    title: data.title,
                    duration: 3,
                    desc: data.info,
                    render: h => {
                        return h('div', [
                            h('a', {
                                attrs: {
                                    href: 'http://laiketui.com'
                                }
                            }, data.info)
                        ])
                    }
                });
            }
            data.config = {};
            data.config.global = {
                upload: {
                    props: {
                        onSuccess(res, file) {
                            if (res.status === 200) {
                                file.url = res.data.src;
                            } else {
                                ElementUI.Message({
                                    message: res.msg,
                                    type: 'error',
                                    offset: 100
                                })
                            }
                        }
                    }
                }
            }
            data.config.onSubmit = function (formData, $f) {
                const names = formData
                $f.btn.loading(true);
                if (data.action === 'index.php?module=software&action=group&m=save_group') {
                    console.log(data);
                    if (!formData.name) {
                        ElementUI.Message({
                            message: '分类名称不能为空',
                            type: 'error',
                            offset: 100
                        })
                        return false;
                    }

                    if (formData.name.length > 10) {
                        ElementUI.Message({
                            message: '分类名称不能大于10个字符',
                            type: 'error',
                            offset: 100
                        })
                        return false;
                    }

                    let treeData = data.treeData.treeData;
                    for (let i = 0; i < treeData.length; i++) {
                        if (formData.name === treeData[i].name) {
                            ElementUI.Message({
                                message: '分类名称重复',
                                type: 'error',
                                offset: 100
                            })
                            return false;
                        }
                    }



                    formData = qs.stringify({
                        m: 'save_group',
                        data: JSON.stringify({
                            name: formData.name,
                            is_default: 0
                        })
                    })
                } else if (data.action === 'index.php?module=software&action=group&type=edit') {
                    formData = qs.stringify({
                        m: 'save_group',
                        data: JSON.stringify({
                            name: formData.name,
                            is_default: 0,
                            id: data.id
                        })
                    })
                }
                console.log(data)
                addClass({
                    api: 'resources.file.createCatalogue',
                    catalogueName: names.name,
                    id: data.id ? data.id : null
                }).then((res) => {
                    modalInstance.remove()
                    ElementUI.Message({
                        message: res.msg || '提交成功',
                        type: 'success',
                        offset: 100
                    })
                    resolve(res)
                }).catch(err => {
                    console.log(err.data);
                    ElementUI.Message({
                        message: err.msg || '提交失败',
                        type: 'error',
                        offset: 100
                    })
                    reject(err)
                }).finally(() => {
                    $f.btn.loading(false)
                })

                // request[data.method.toLowerCase()](data.action, formData, {
                //   headers: {
                //     'Content-Type': 'application/x-www-form-urlencoded'
                //   }
                // }).then((res) => {
                //     modalInstance.remove()
                //     Message.success(res.msg || '提交成功')
                //     resolve(res)
                // }).catch(err => {
                //   console.log(err.data);
                //   Message.error(err.msg || '提交失败')
                //     reject(err)
                // }).finally(() => {
                //     $f.btn.loading(false)
                // })
            }
            data.config.submitBtn = false
            data.config.resetBtn = false
            if (!data.config.form) data.config.form = {}
            data.config.form.labelWidth = 100
            let fApi
            alert({
                title: data.title,
                width,
                loading: false,
                render: function (h) {
                    return h('div', { class: 'common-form-create' }, [
                        h('formCreate', {
                            props: {
                                rule: data.rules,
                                option: data.config
                            },
                            on: {
                                mounted: ($f) => {
                                    fApi = $f
                                    msg()
                                }
                            }
                        }),
                        // 添加分类 提交按钮 样式以及事件设置
                        h('Button', {
                            class: 'common-form-button',
                            style: {
                                width: 'auto',
                                marginLeft: '50%',
                                transform: 'translateX(-50%)',
                            },
                            props: {
                                type: 'primary',
                                long: true
                            },
                            on: {
                                click: () => {
                                    fApi.submit()
                                }
                            }
                        }, ['提交'])
                    ])
                }
            })
        }).catch(res => {
            Spin.hide();
            msg();
            ElementUI.Message({
                message: res.msg || '表单加载失败',
                type: 'error',
                offset: 100
            })
        })
    })
}
