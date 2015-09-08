
      <h3>
<a id="简介" class="anchor" href="#%E7%AE%80%E4%BB%8B" aria-hidden="true"><span class="octicon octicon-link"></span></a><strong>简介</strong>
</h3>

<p>AdjustImageView是一个开源的安卓UI控件，支持手势移动、缩放、旋转、单击和双击事件功能，并且支持角度、边界、以及大小校正。<br></p>

<h3>
<a id="描述" class="anchor" href="#%E6%8F%8F%E8%BF%B0" aria-hidden="true"><span class="octicon octicon-link"></span></a><strong>描述</strong>
</h3>

<p>继承的是ImageView，使用简单快捷。最初的图片显示是以 centerInside 的形式显示的，也就是图片按比例，填充在ImageView里，直到宽或高铺满整个ImageView。<br></p>

<h3>
<a id="使用方法" class="anchor" href="#%E4%BD%BF%E7%94%A8%E6%96%B9%E6%B3%95" aria-hidden="true"><span class="octicon octicon-link"></span></a><strong>使用方法</strong>
</h3>

<p>1、导入adjust_imageview.jar 或者 导入AdjustImageView.java<br>
2、在相应的布局文件中引用 <br>
    <code>&lt;com.ddxce.open.AdjustImageView</code>
         <code>android:id="@+id/imageview"</code>
         <code>android:layout_width="fill_parent"</code>
         <code>android:layout_height="fill_parent" /&gt;</code> <br>
3、在代码Activity 或者 Fragment 中调用setImageBitmap() <br></p>

<p>protected void onCreate(Bundle savedInstanceState) { <br>
        super.onCreate(savedInstanceState); <br>
        setContentView(R.layout.activity_main); <br>
        AdjustImageView imageView = (AdjustImageView) findViewById(R.id.imageview); <br>
        imageView.setImageBitmap(((BitmapDrawable) <br>getResources().getDrawable(R.mipmap.ic_launcher)).getBitmap()); <br>
    }<br></p>

<h3>
<a id="可用的方法" class="anchor" href="#%E5%8F%AF%E7%94%A8%E7%9A%84%E6%96%B9%E6%B3%95" aria-hidden="true"><span class="octicon octicon-link"></span></a><strong>可用的方法</strong>
</h3>

<p>1、监听点击事情<br>
imageView.setAction(new AdjustImageView.Action() {<br>
            public void onClick() {<br>
                //do something<br>
            }<br>
        });<br>
2、恢复到最初<br>
不带过渡动画：  imageView.toInit();<br>
带过渡动画：imageView.toInit(true);<br></p>

<p>3、锁定手势<br>
imageView.setLock(true);<br></p>
