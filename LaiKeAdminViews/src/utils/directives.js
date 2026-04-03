import Vue from 'vue'
import axios from "axios";

//普通分页查询
// v-load-more.method="loadMore"

//远程搜索走else
// filterable
// :filter-method="()=>{}"
// v-load-more="{
//   url:'url',
//   options: options,
//   model: id,
//   modelField: 'id',
//   searchField: 'name'}"


const loadMore = Vue.directive('loadMore', {
    bind(el, binding) {
        // debugger
      // 如果有method由调用方实现，没有则在这里实现加载和远程搜索的功能
      if (binding.modifiers.method) {
        // 节流
        let timer
        // 滚动监听
        el.querySelector('.el-select-dropdown .el-select-dropdown__wrap').addEventListener('scroll', function() {
          const condition = this.scrollHeight - this.scrollTop <= this.clientHeight + 100
          if (!timer && condition) {
            // 滚动加载（调用自定义的加载方法）
            binding.value()
            timer = setTimeout(() => {
              clearTimeout(timer)
              timer = null
            }, 500)
          }
        })
      } else {
        // 传入的对象
        let value = binding.value
        // 节流
        let timer
        // 无搜索内容变量
        let pageNo = 1
        let pages = 1
        // 远程搜索内容变量
        let searchPageNo = 1
        let searchPages = 1
        // 每次加载的条数
        let pageSize = isNaN(value.pageSize) ? 10 : parseInt(value.pageSize)
        // 远程搜索变量
        let searchField = value.searchField
        // 接口地址
        let url = value.url
        // 下拉数组，这个options在本方法中必须永远指向value.options，否则整个功能都将失效
        let options = value.options
        // 无搜索拷贝数组，此处是为了在加载的基础上加一些默认的下拉项
        let optionsCopy = JSON.parse(JSON.stringify(value.options))
        // 远程搜索拷贝数组
        let optionsSearch = []
        // 远程搜索内容
        let searchValue = ''
        // 加载逻辑
        const loadOptions = (searchField, search) => {
          let params = {
            pageSize: pageSize
          }
          // 这里不能改变options的指向，否则会使整个功能失效（不能用options = []）
          options.length = 0
          // 判断是否为远程搜索，true-是
          if (searchField && search) {
            // 当到最大页数时不再查询
            if (searchPages >= searchPageNo) {
              params.pageNo = searchPageNo++
              params[searchField] = search
              // 如需远程搜索需要改以下接口请求
              axios[url](params).then(res => {
                if (res) {
                  searchPages = Math.ceil(res.data.total / pageSize)
                  optionsSearch = optionsSearch.concat(res.data.data)
                  dataProcessing(optionsSearch)
                }
              })
            }
          } else {
            // 当到最大页数时不再查询
            if (pages >= pageNo) {
              params.pageNo = pageNo++
              // 如需远程搜索需要改以下接口请求
              axios[url](params).then(res => {
                if (res) {
                  pages = Math.ceil(res.data.total / pageSize)
                  optionsCopy = optionsCopy.concat(res.data.data)
                  dataProcessing(optionsCopy)
                }
              })
            }
          }
        }
        // 返回数据处理
        let dataProcessing = (optionsCopy) => {
          // 这里不能改变options的指向，否则会使整个功能失效
          optionsCopy.forEach(item => {
            let check = options.find(t => {
              return t[value.modelField] === item[value.modelField]
            })
            if (!check) {
              options.push(item)
            }
          })
        }
        // 首次加载
        loadOptions()
        // 判断是否需要回显
        if (value.model && value.modelField) {
          // 回显方法
          let echo = (model, modelField) => {
            let params = {}
            params[modelField] = model
            // todo 请求方法是我们自己封装的，需要换成自己项目的方法
            axios[url](params).then(res => {
              if (res) {
                optionsCopy = optionsCopy.concat(res.data.data)
                dataProcessing(optionsCopy)
              }
            })
          }
          if (optionsCopy.length > 0) {
            let check = optionsCopy.find((item) => {
              return item[value.modelField] === value.model
            })
            if (check) {
              echo(value.model, value.modelField)
            }
          } else {
            echo(value.model, value.modelField)
          }
        }
        // 滚动监听（无限滚动）
        el.querySelector('.el-select-dropdown .el-select-dropdown__wrap').addEventListener('scroll', function() {
          const condition = this.scrollHeight - this.scrollTop <= this.clientHeight + 100
          if (!timer && condition) {
            // 滚动加载
            loadOptions(searchField, searchValue)
            timer = setTimeout(() => {
              clearTimeout(timer)
              timer = null
            }, 200)
          }
        })
        // 输入监听（远程搜索）
        if (searchField) {
          el.getElementsByTagName('input')[0].addEventListener('input', function() {
            if (this.value) {
              searchPageNo = 1
              searchPages = 1
              optionsSearch = []
              searchValue = this.value
              loadOptions(searchField, searchValue)
            } else {
              searchValue = ''
              dataProcessing(optionsCopy)
            }
          })
        }
      }
    }
})

export default {
    loadMore
  }