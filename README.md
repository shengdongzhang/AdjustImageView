# AdjustImageView
<!DOCTYPE html>
<html lang="en-us">
  <head>
    <meta charset="UTF-8">
    <title>Adjustimageview by shengdongzhang</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="stylesheet" type="text/css" href="stylesheets/normalize.css" media="screen">
    <link href='https://fonts.googleapis.com/css?family=Open+Sans:400,700' rel='stylesheet' type='text/css'>
    <link rel="stylesheet" type="text/css" href="stylesheets/stylesheet.css" media="screen">
    <link rel="stylesheet" type="text/css" href="stylesheets/github-light.css" media="screen">
  </head>
  <body>
    <section class="page-header">
      <h1 class="project-name">Adjustimageview</h1>
      <h2 class="project-tagline">easy to use imageview for android</h2>
      <a href="https://github.com/shengdongzhang/AdjustImageView" class="btn">View on GitHub</a>
      <a href="https://github.com/shengdongzhang/AdjustImageView/zipball/master" class="btn">Download .zip</a>
      <a href="https://github.com/shengdongzhang/AdjustImageView/tarball/master" class="btn">Download .tar.gz</a>
    </section>

    <section class="main-content">
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

      <footer class="site-footer">
        <span class="site-footer-owner"><a href="https://github.com/shengdongzhang/AdjustImageView">Adjustimageview</a> is maintained by <a href="https://github.com/shengdongzhang">shengdongzhang</a>.</span>

        <span class="site-footer-credits">This page was generated by <a href="https://pages.github.com">GitHub Pages</a> using the <a href="https://github.com/jasonlong/cayman-theme">Cayman theme</a> by <a href="https://twitter.com/jasonlong">Jason Long</a>.</span>
      </footer>

    </section>

  
  </body>
</html>