### Android开发中个人习惯代码封装

##### 特点
1. 功能点均在单个kt文件内，可cv单独使用
> kt的扩展属性和顶层属性
2. js配合jitpack一键发版
> 自动发布jitpack脚本.js `node autojitpack.js`

##### 指令
```bash
# 合并文件夹
git status
git checkout demo alibx/src

# 脚本自动push async 升级jitpack版本
node autojitpack.js
```