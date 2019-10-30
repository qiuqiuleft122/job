package com.icicle.util;

import com.icicle.file.FastDFSFile;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.*;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * @Author Max
 * @Date 16:04 2019/8/27
 * @Description：文件操作的工具类
 * 功能：
 *      1.文件上传
 *      2.文件下载
 *      3.文件删除
 *      4.文件信息获取
 *      5.Tracker信息获取
 *      6.Storage信息获取
 **/
public class FastDFSClient {

    /**
     * 初始化Tracker的信息  静态代码块 只会加载一次
     */
    static {
        try {
            //1.加载Tracker的配置文件
            //获取配置文件的位置fastDFS_client.conf的位置 读取fdfs_client.conf文件   BeanFactory  ApplicationContext
            String path = new ClassPathResource("fastDFS_client.conf").getPath();

            //2.初始化加载到指定对象中  --- 肯定和FastDFS对象有关
            ClientGlobal.init(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 文件上传
     * @param fastDFSFile 包含了文件所有信息
     * @return
     * @throws Exception
     */
    public static String[] upload(FastDFSFile fastDFSFile) throws Exception {
        //自定义属性
        NameValuePair[] meta_list = new NameValuePair[1];
        meta_list[0]=new NameValuePair("设备","华为P30 Pro");

//        //上传文件的分析  文件上传 会先经过Tracker  最终存储在Storage中
//        //1.TrackerClient：Tracker客户端
//        TrackerClient trackerClient = new TrackerClient();
//        //2.通过TrackerClient客户端可以获取连接 -- >会携带storage信息 ->connection
//        TrackerServer trackerServer = trackerClient.getConnection();
//
//        //3.通过Tracker的连接对象 获取storage的服务端信息，创建一个storage客户端对象存储Storage服务端信息
//        StorageClient storageClient = new StorageClient(trackerServer,null);

        //提取的公共方法操作
        StorageClient storageClient = getStorageClient();

        /****
         * 因为Storage信息存储到了Storage的客户端对象中，所以可以通过Storage客户端实现对Storage的访问，例如文件上传
         * 参数1）要上传的文件内容提供
         * 参数2）文件的扩展名自
         * 参数3）自定义属性信息
         *
         * 返回内容
         *      参数1)文件存储的Storage组名
         *      参数2)文件的详细存储路径
         */
        String[] uploads = storageClient.upload_file(fastDFSFile.getContent(), fastDFSFile.getExt(), null);
        for (String upload : uploads) {
            System.out.println(upload);
        }

        return uploads;
    }

    /**
     * 获取文件信息  Storage服务的IP、端口信息
     * @param groupName 组名
     * @param remoteFileName 文件存储的完整名
     */
    public static FileInfo getFile(String groupName, String remoteFileName) throws Exception {
//        //1.TrackerClient：Tracker 客户端
//        TrackerClient trackerClient = new TrackerClient();
//        //2.通过TrackerClient客户端获取连接-->会携带Storage信息
//        TrackerServer trackerServer = trackerClient.getConnection();
        //3.通过Tracker的连接对象获取Storage服务端信息,创建一个Storage客户端对象存储Storage服务端信息
//        StorageClient storageClient = new StorageClient(trackerServer,null);

        //公共方法操作
        StorageClient storageClient = getStorageClient();

        //获取文件信息
        return storageClient.get_file_info(groupName,remoteFileName);
    }

    /**
     * 文件下载  任何资料的下载都是先获取输入流  读入到内存 然后写出到硬盘  记住返回一个大一点的 方便代码复用
     * @param groupName 组名
     * @param remoteFileName 文件存储的完整名
     */
    public static InputStream downFile(String groupName, String remoteFileName) throws Exception {
        //获取StorageClient
        StorageClient storageClient = getStorageClient();

        //下载文件  获取输入流
        byte[] buffer = storageClient.download_file(groupName,remoteFileName);
        //将字节数组转换成字节输入流
        return new ByteArrayInputStream(buffer);
    }

    /**
     * 文件删除
     * @param groupName  组名
     * @param remoteFileName 文件的完整路径名
     * @return 是否删除成功
     * @throws Exception
     */
    public static int deleteFile(String groupName, String remoteFileName) throws Exception {
        //获取StorageClient
        StorageClient storageClient = getStorageClient();

        /***
         * 删除文件
         * 0:success
         * !0:error
         */
        return storageClient.delete_file(groupName, remoteFileName);
    }

    /**
     * 获取storage组信息  Tracker客户端里边有StorageServer信息
     * @param groupName 组名
     * @return
     * @throws Exception
     */
    public static StorageServer getStorages(String groupName) throws Exception {
        //创建TrackerClient对象
        TrackerClient trackerClient = new TrackerClient();
        //通过TrackerClient获取TrackerServer对象
        TrackerServer trackerServer = trackerClient.getConnection();
        //通过trackerClient获取Storage组信息
        return trackerClient.getStoreStorage(trackerServer,groupName);
    }

    /**
     *  根据文件组名和文件存储路径获取Storage服务的IP、端口信息  Storage服务信息
     * @param groupName 组名
     * @param remoteFileName 文件存储完整名
     * @return
     * @throws Exception
     */
    public static ServerInfo[] getServerInfo(String groupName, String remoteFileName) throws Exception{
        //创建TrackerClient对象
        TrackerClient trackerClient = new TrackerClient();
        //通过TrackerClient获取TrackerServer对象
        TrackerServer trackerServer = trackerClient.getConnection();
        //获取服务信息
        return trackerClient.getFetchStorages(trackerServer, groupName, remoteFileName);
    }

    /**
     * 获取TrackerServer服务信息  这个里边封装了很多信息
     * @return
     * @throws Exception
     */
    public static TrackerServer getTrackerServer() throws Exception {
        //上传文件的分析  文件上传 会先经过Tracker  最终存储在Storage中
        //1.TrackerClient：Tracker客户端
        TrackerClient trackerClient = new TrackerClient();
        //2.通过TrackerClient客户端可以获取连接 -- >会携带storage信息 ->connection
        return trackerClient.getConnection();
    }

    /**
     * 获取Storage客户端
     * @return
     * @throws Exception
     */
    public static StorageClient getStorageClient() throws Exception {
        //1.TrackerClient：Tracker 客户端
        TrackerClient trackerClient = new TrackerClient();
        //2.通过TrackerClient客户端获取连接-->会携带Storage信息
        TrackerServer trackerServer = trackerClient.getConnection();
        //3.通过Tracker的连接对象获取Storage服务端信息,创建一个Storage客户端对象存储Storage服务端信息
        return new StorageClient(trackerServer,null);
    }

    /**
     * FastDFS的相关测试
     * @param args
     */
    public static void main(String[] args) throws Exception {
        String groupName = "group1";
        String remoteFileName="M00/00/00/wKjThF1k9FOAU7P9AAF9D31vB84565.png";
//        String remoteFileName="M00/00/00/wKjThF1E95SAZkDVAAnAAJuzIB4821.jpg ";

        //获取文件信息
//        FileInfo fileInfo = getFile(groupName, remoteFileName);
//        System.out.println("文件大小：" + fileInfo.getFileSize());  //文件大小：9755
//        System.out.println("文件ip：" + fileInfo.getSourceIpAddr()); //文件ip：192.168.211.132

        //文件下载
//        InputStream is = downFile(groupName, remoteFileName);
        //缓冲区
//        byte[] buffer = new byte[1024];
        //写入到E:/1.png
//        OutputStream os = new FileOutputStream("E:/1.png");
        //读文件
//        while (is.read(buffer)!=-1){
//            os.write(buffer);
//        }
//        //关闭资源
//        os.flush();
//        os.close();
//        is.close();

        //文件删除
//        int flag = deleteFile(groupName, remoteFileName);
//        System.out.println("删除成功与否的标志： " + flag);

        //获取组信息
//        StorageServer storageServer = getStorages(groupName);
//        System.out.println(storageServer.getStorePathIndex());  //第几组 索引默认从0开始  这里只有一个组

        //获取Storage服务信息
//        ServerInfo[] serverInfo = getServerInfo(groupName, remoteFileName);
//        for (ServerInfo info : serverInfo) {
//            System.out.println(info.getIpAddr()+":"+info.getPort());//192.168.211.132:23000
//        }
        TrackerServer trackerServer = getTrackerServer();
        System.out.println(trackerServer.getInetSocketAddress().getHostString()+":"+trackerServer.getInetSocketAddress().getPort()); //192.168.211.132:22122
        System.out.println(trackerServer.getInetSocketAddress().getHostName()+":"+trackerServer.getInetSocketAddress().getPort()); //192.168.211.132:22122 不推荐使用这个  返回数据很慢
        System.out.println(ClientGlobal.getG_tracker_http_port());//得到Tracker的http端口 8080
    }
}
