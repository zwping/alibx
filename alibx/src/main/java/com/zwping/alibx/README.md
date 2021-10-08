## 维护一份AOP思路的ktx框架

> 没有任何基类/组件, 单ktx文件, 复制即用
> 插件式依赖, 随时升级/遗弃
> 简易扩展, 学习&Review
> 遵循Google原生开发, 遵循原生特色
> kotlin扩展函数给予了安卓非反射实现AOP功能


### ktx

```kotlin
```

### ToolbarKtx

> Demo >>> ../ToolbarAc.java

```kotlin
T.setTitleOfCenter(title) // 居中的标题 (addTextView形式)
T.setTitleCenter(title)   // 标题居中 (childView遍历)
T.addMenu(itemId...)   // 代码中增加menu, 不再需要创建menu.layout
T.addMenuBadge(itemId...)   // 增加带角标的menu (actionProvider自定义menuView)
T.setStatusBarImmersion() // Toolbar动态marginTop
object ToolbarKtx(){....} // 兼容Java
```


### 小彩蛋

```kotlin
class DiffUtilKtx<B>(...) {
    var oldData : List<B>? // 老数据/当前数据
    fun calculateDiff(data, detectMoves) // 类对象管理oldData
    fun setOnDataDiffListener(lis) // 监听数据是否产生差异 (DiffUtil差量算法)
}
```

```kotlin
/*** 具有生命周期感知的定时器 ***/
class TimerKtx(...){
    var count           // 执行次数
    fun schedule(...)   // 执行
    fun cancel()        // 取消
}
```