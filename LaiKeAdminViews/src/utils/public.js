import { tableDelApi } from '@/api/common';
import { fileDelApi, delFile } from '@/api/uploadPictures'
export function modalSure (delfromData) {
    return new Promise((resolve, reject) => {
        let content = '';
        if (delfromData.info !== undefined) {
            content = `<p>${delfromData.title}</p><p>${delfromData.info}</p>`
        } else {
            content = `<p>确定要${delfromData.title}吗？</p><p>${delfromData.title}后将无法恢复，请谨慎操作！</p>`
        }
        this.$Modal.confirm({
            title: delfromData.title,
            content: content,
            loading: true,
            onOk: () => {
                setTimeout(() => {
                    this.$Modal.remove();
                    if (delfromData.success) {
                        delfromData.success.then(async res => {
                            resolve(res);
                        }).catch(res => {
                            reject(res)
                        })
                    } else {
                        if (delfromData.url === 'file/file/delete') {
                          console.log(delfromData)
                          delFile({
                            api: 'resources.file.delFile',
                            id: delfromData.ids.ids
                          }).then(async res => {
                            resolve(res);
                          }).catch(res => {
                            console.log(res);
                            reject(res)
                          })
                          // fileDelApi(delfromData.checkPicList).then(async res => {
                          //   resolve(res);
                          // }).catch(res => {
                          //   console.log(res);
                          //   reject(res)
                          // })
                        } else {
                          tableDelApi(delfromData).then(async res => {
                            resolve(res);
                          }).catch(res => {
                            reject(res)
                          })
                        }
                    }
                }, 300);
            },
            onCancel: () => {
               // this.$Message.info('取消成功');
            }
        });
    })
}
