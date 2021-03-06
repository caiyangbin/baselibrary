package com.nice.baselibrary.base.entity.vo

/**
 * 下载实体类
 * @author JPlus
 * @date 2019/3/1.
 */
data class DownloadInfo(var id:Int,
                        var name:String,
                        var url:String,
                        var path:String,
                        var start_time:String,
                        var end_time:String,
                        var read:Long,
                        var count:Long,
                        var status:String,
                        var ext:String)
