viewbase 基础控件包，该包里面不存放资源图片资源文件等，所有存放到该位置的控件都应该能通过xml配置进行配置里面所有属性

2017/5/15
增加LetterSlideBar，字母滑动条
自定义属性：
viewbase:textSize 字体大小
viewbase:textColor 字体颜色
viewbase:textLightColor 字体高亮颜色

2017/12/1
抽象uibase代码，部分与逻辑相关的代码放到viewbase中，使代码逻辑与UI分离

2018/06/21
封装限制表情和特殊符号输入的LimitEditText，使用的时候只需要在xml中使用com.roadrover.viewbase.widget.LimitEditText标签，
该对象中封装了一个LimitTextCallback，在代码中注册该callback，使用者可以根据错误码实现相应的输入异常的提示功能
