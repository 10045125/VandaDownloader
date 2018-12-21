# QuarkDownloader
重新设计夸克浏览器的下载模块，更加清晰的特性设计。

#### 下载特性 
>读写分离／非分离 </br>
>全局一个线程写文件</br>
>任意线程</br>
>网络库定制</br>
>IO可定制（网络IO和文件IO）</br>
>任务优先级 & 队列任务 & 并发任务 </br>
>静默/非静默任务</br>


#### Blog 文章

[QuarkDownloader 文章](http://wuzhonglian.com/2018/08/26/QuarkDownloader-%E8%AF%BB%E5%86%99%E5%88%86%E7%A6%BB%E8%AE%BE%E7%BD%AE%E5%8E%9F%E5%9E%8B/#more)

#### 简易流程图

{% asset_img pic/download.png %}
{% asset_img pic/callback.png %}

#### Demo

{% asset_img img/demo.jpg %}
