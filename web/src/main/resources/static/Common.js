/**
 * 公共js工具类
 * */
document.write("<script type='text/javascript' src='https://cdn.bootcss.com/layer/1.8.5/layer.min.js'></script>");
var baigt={
    //版本
    version:function(){
        return "Version 1.0 create  by baigt";
    },
    //判断是否登录 未登录则弹出登录窗口
    iflogin:function(){
        $.ajax({
            url : ctx+'/iflogin' ,
            type: "get",
            async: false,
            dataType : "json",
            success: function(data){
                //未登录
                if (data.iflogin=='no') {
                    openwindow(ctx+'/login');
                    $('#uploginModal').modal('show')
                    throw "终止后续js操作";
                }
            },error:function(jqXHR, textStatus, errorThrown) {
                baigt.error(jqXHR,textStatus,errorThrown);
            }
        });
    },
    //post异步请求
    getPostHtml:function (url,data,dompos){
        $.ajax({
            url: url,
            data: data,
            method:"post",
            async: false,
            dataType:'html',
            success: function(data) {
                $(dompos).html(data);
            },error:function(jqXHR, textStatus, errorThrown) {
                baigt.error(jqXHR,textStatus,errorThrown);
            }
        });
    },
    //post-json请求
    getPostJson:function (url,data,callback){
        $.ajax({
            url: url,
            data: data,
            method:"post",
            async: false,
            dataType:'json',
            success: function(data) {
                if (data.code=='200') {
                    baigt.msg(data.msg,callback)
                }else {
                    baigt.msg(data.msg, null);
                }
            },error:function(jqXHR, textStatus, errorThrown) {
                baigt.error(jqXHR,textStatus,errorThrown);
            }
        });
    },
    //post-json请求
    getJson:function (url,data,callback){
        $.ajax({
            url: url,
            data: data,
            method:"get",
            async: false,
            dataType:'json',
            success: function(data) {
                console.info(data)
                var  func=eval(callback);
                new func(data);
            },error:function(jqXHR, textStatus, errorThrown) {
                baigt.error(jqXHR,textStatus,errorThrown);
            }
        });
    },
    //post-json请求
    getPostJsonCallBack:function (url,data,callback){
        $.ajax({
            url: url,
            data: data,
            method:"post",
            async: false,
            dataType:'json',
            success: function(data) {
                var  func=eval(callback);
                new func(data);
            },error:function(jqXHR, textStatus, errorThrown) {
                baigt.error(jqXHR,textStatus,errorThrown);
            }
        });
    },
    error:function(jqXHR, textStatus, errorThrown){
        if(jqXHR.status == 404) {
            baigt.msg("系统发生异常,请联系管理员!!(error-404)");
        }else {
            baigt.msg("系统发生异常,请联系管理员");
        }
    },
    //post提交跳转页面
    windowpost:function(url){
        var queryString="";//检索条件
        if(url.indexOf('?')){ //判断是否携带参数
            actionurl=url.substring(0,url.indexOf('?')); //基础地址
            queryString=url.substring(url.indexOf('?')+1,url.length);//携带参数
        }else{
            actionurl=url;
        }
        document.write("<form action='"+actionurl+"' method='post' name='form1'  style='display:none'>");
        if(!!queryString){
            var arr =queryString.split("&");
            if(arr.length>0){
                for(var i=0;i<arr.length;i++){
                    var tem=arr[i].split("=");
                    if(tem.length>0){
                        var name=tem[0];
                        var value=tem[1];
                        document.write("<input type='hidden' name='"+name+"' value='"+value+"' />");
                    }
                }
            }
        }
        document.write("</form>");
        document.form1.submit();
    },
    //公共弹窗
    msg:function(text,callback){
        layer.msg(text);
        setTimeout(function(){
            if (!!callback) {
                var  func=eval(callback);
                new func();
            }
        },"1000")
    },
    //公共弹窗 不关闭的错误提示
    msg:function(text){
        layer.msg(text);
    },/*
     配合img的onerror方法使用
     * obj为图片对象
     * defaultImgPath 为默认图片  需确保该图片一定存在 不然一直报错
     *
     * 示例：
     * <img src="${item.videoThumbnailUrl}" onerror="baigt.Defaultimg(this,'${hyrt}/static/images/sp_03.jpg')" alt="">
     * */
    Defaultimg:function(obj,defaultImgPath) {
        var imgsrc = $(obj).attr("src");
        //图片不存在
        if (!!!imgsrc) {
            console.info("defaultImgPath1:" + defaultImgPath)
            $(obj).attr("src", defaultImgPath);
        } else { //斜杠开头
            var reg = /^\//;
            //斜杠开头
            if (reg.test(imgsrc)) {
                //因为已经发生了错误  所以给默认图片进行展示
                $(obj).attr("src", defaultImgPath);
            } else {
                //尝试添加/ 看是否解决 如果没解决则继续出错  走默认图片
                imgsrc = "/" + imgsrc;
                console.info("imgsrc:" + imgsrc)
                $(obj).attr("src", imgsrc);
            }

        }
    }
}

