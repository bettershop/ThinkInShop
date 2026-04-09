package com.laiketui.common.utils.tool;

import com.alibaba.fastjson2.JSON;
import com.aliyun.oss.ClientConfiguration;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.comm.Protocol;
import com.aliyun.oss.model.GetObjectRequest;
import com.aliyun.oss.model.LifecycleRule;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.PutObjectResult;
import com.laiketui.common.utils.tool.data.DataCheckTool;
import com.laiketui.core.common.LKTSnowflakeIdWorker;
import com.laiketui.core.common.SplitUtils;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DefaultImagesValues;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.core.utils.tool.PinyinUtils;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.log.FilesRecordModel;
import com.laiketui.domain.upload.UploadConfigModel;
import com.laiketui.domain.upload.UploadImagModel;
import com.laiketui.domain.vo.files.DelFilesVo;
import com.laiketui.root.common.BuilderIDTool;
import io.minio.*;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.FileNameMap;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.zip.Adler32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 图片上传工具类
 *
 * @author Trick
 * @date 2020/9/25 16:43
 */
public class ImgUploadUtils
{

    private static final Logger logger = LoggerFactory.getLogger(ImgUploadUtils.class);

    /**
     * oos 公共图片 - 店铺分享主图
     */
    public static final String OOS_DEFAULT_IMAGE_NAME_MCH  = DefaultImagesValues.OOS_DEFAULT_IMAGE_NAME_MCH;
    /**
     * oos 公共图片 - 默认底图logo
     */
    public static final String OOS_DEFAULT_IMAGE_NAME_LOGO = DefaultImagesValues.OOS_DEFAULT_IMAGE_NAME_LOGO_ADD;

    public static final String SYSTEM_DIY_THEME = "system_diy_theme";

    public static final String CODE_IMGS_FORLDER = "code_imgs";

    /**
     * 本地默认上传地址
     */
    public static String DEFAULT_UPLOAD_PATH;

    static
    {
        try
        {
            String path;

            // 1️⃣ 优先读取 Docker 或外部配置的上传路径
            // 支持环境变量（UPLOAD_PATH）或 JVM 启动参数（-Dupload.path=xxx）
            String envPath = System.getenv("UPLOAD_PATH");
            if (envPath == null || envPath.trim().isEmpty())
            {
                envPath = System.getProperty("upload.path");
            }

            if (envPath != null && !envPath.trim().isEmpty())
            {
                // 来自外部定义（如 Docker -e UPLOAD_PATH=/data/upload）
                path = envPath.endsWith(File.separator) ? envPath : envPath + File.separator;
            }
            else
            {
                // 2️⃣ 本地开发环境使用 classpath 下 static 目录
                try
                {
                    File classPathDir = new File(ResourceUtils.getURL("classpath:").getPath());
                    if (classPathDir.exists() && classPathDir.isDirectory())
                    {
                        path = classPathDir.getAbsolutePath() + File.separator + "static" + File.separator;
                    }
                    else
                    {
                        // 3️⃣ JAR 或 Docker 环境：默认写入容器内 /app/static
                        path = "/app/static/";
                    }
                }
                catch (Exception e)
                {
                    // classpath 在 JAR 内会报错：直接降级为容器路径
                    path = "/app/static/";
                }
            }

            // 4️⃣ 确保目录存在
            File dir = new File(path);
            if (!dir.exists())
            {
                boolean created = dir.mkdirs();
                if (!created)
                {
                    logger.warn("默认上传路径目录创建失败：{}", path);
                }
            }

            DEFAULT_UPLOAD_PATH = path;
            logger.info("默认上传路径初始化完成：{}", DEFAULT_UPLOAD_PATH);
        }
        catch (Exception e)
        {
            logger.error("初始化默认上传路径失败：{}", e.getMessage(), e);
            // 兜底路径（防止异常导致为 null）
            DEFAULT_UPLOAD_PATH = "/app/static/";
        }
    }


    /**
     * 获取文件完整路径
     *
     * @param filesRecordModel   -
     * @param uploadConfigModels -
     * @return String
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/10/10 13:26
     */
    public static String getImgPath(FilesRecordModel filesRecordModel, List<UploadConfigModel> uploadConfigModels) throws LaiKeAPIException
    {
        StringBuilder fileUrl      = new StringBuilder();
        String        uploadDomain = "";
        String        imgName      = "";
        //域名(第三方使用)
        StringBuilder domainUrl   = new StringBuilder();
        String        endpoint    = "";
        String        diyEndpoint = "";
        //2021-09-29 16:57:14 oss图片增加日期分组(按天)
        String fileDateDay = "";
        switch (filesRecordModel.getUpload_mode())
        {
            case GloabConst.UploadConfigConst.IMG_UPLOAD_LOCALHOST + "":
                //本地上传
                for (UploadConfigModel uploadConfig : uploadConfigModels)
                {
                    //根据配置拼接url
                    switch (uploadConfig.getAttr())
                    {
                        case GloabConst.UploadConfigConst.UPLOADIMG_DOMAIN:
                            //图片上传域名
                            uploadDomain = uploadConfig.getAttrvalue();
                            break;
                        case GloabConst.UploadConfigConst.UPLOADIMG:
                            //图片上传位置
                            fileUrl.append(uploadConfig.getAttrvalue());
                            break;
                        default:
                            break;
                    }
                    //文件夹路径
                    fileUrl.append(GloabConst.UploadConfigConst.IMG_PATH);
                    fileUrl.append(filesRecordModel.getStore_id());
                    fileUrl.insert(0, uploadDomain);
                    fileUrl.append(filesRecordModel.getImage_name());
                }
                break;
            case GloabConst.UploadConfigConst.IMG_UPLOAD_OSS + "":
                Date sysDate = DateUtil.dateFormateToDate("2021-09-29 00:00:00", GloabConst.TimePattern.YMDHMS);
                if (DateUtil.dateCompare(filesRecordModel.getAdd_time(), sysDate))
                {
                    //上传时间,图片上传按照天分组了
                    fileDateDay = DateUtil.dateFormate(filesRecordModel.getAdd_time(), GloabConst.TimePattern.YMD1);
                }
                //是否开启自定义域名
                boolean isOpenDiyUrl = false;
                for (UploadConfigModel uploadConfig : uploadConfigModels)
                {
                    //根据配置拼接url
                    switch (uploadConfig.getAttr())
                    {
                        case GloabConst.UploadConfigConst.BUCKET:
                            //oos仓库
                            uploadDomain = uploadConfig.getAttrvalue();
                            break;
                        case GloabConst.UploadConfigConst.ENDPOINT:
                            //域名
                            endpoint = uploadConfig.getAttrvalue();
                            break;
                        case GloabConst.UploadConfigConst.ISOPENZDY:
                            //自定义域名开关
                            if ("1".equals(uploadConfig.getAttrvalue()))
                            {
                                isOpenDiyUrl = true;
                            }
                            break;
                        case GloabConst.UploadConfigConst.MYENDPOINT:
                            //自定义域名
                            diyEndpoint = uploadConfig.getAttrvalue();
                            break;
                        default:
                            break;
                    }
                }
                if (isOpenDiyUrl)
                {
                    //自定义域名
                    domainUrl.append(diyEndpoint);
                }
                else
                {
                    domainUrl
                            //仓库
                            .append(uploadDomain).append(SplitUtils.XSD)
                            //域名
                            .append(endpoint);
                }
                if (StringUtils.isNotEmpty(fileDateDay))
                {
                    //新增日期文件夹
                    imgName = fileDateDay + SplitUtils.FXG + filesRecordModel.getImage_name();
                }
                //请求协议
                fileUrl.append(GloabConst.UploadConfigConst.OOS_HTTPS_HEADER)
                        .append(domainUrl);

                if (Objects.nonNull(filesRecordModel.getDiy_img_type()) && filesRecordModel.getDiy_img_type() == 0) {
                    fileUrl.append(SplitUtils.FXG).append(SYSTEM_DIY_THEME).append(SplitUtils.FXG).append(filesRecordModel.getDiy_id());
                } else {
                    fileUrl.append(SplitUtils.FXG).append(filesRecordModel.getStore_id());
                }
                fileUrl.append(SplitUtils.FXG).append(filesRecordModel.getStore_type())
                        .append(SplitUtils.FXG).append(imgName);
                break;
            case GloabConst.UploadConfigConst.IMG_UPLOAD_MINIO + "":
                fileDateDay = DateUtil.dateFormate(filesRecordModel.getAdd_time(), GloabConst.TimePattern.YMD1);
                for (UploadConfigModel uploadConfig : uploadConfigModels)
                {
                    //根据配置拼接url
                    switch (uploadConfig.getAttr())
                    {
                        case GloabConst.UploadConfigConst.BUCKET:
                            //MinIo桶名称
                            uploadDomain = uploadConfig.getAttrvalue();
                            break;
                        case GloabConst.UploadConfigConst.ENDPOINT:
                            //域名
                            endpoint = uploadConfig.getAttrvalue();
                            //如果配置不带http/https,则默认https://
                            if (!(endpoint.startsWith(GloabConst.UploadConfigConst.OOS_HTTPS_HEADER) || endpoint.startsWith(GloabConst.UploadConfigConst.OOS_HTTP_HEADER)))
                            {
                                endpoint = GloabConst.UploadConfigConst.OOS_HTTPS_HEADER + endpoint;
                            }
                            break;
                        default:
                            break;
                    }
                }
                domainUrl
                        //域名
                        .append(endpoint).append(SplitUtils.FXG)
                        //MinIo桶名称
                        .append(uploadDomain);
                if (StringUtils.isNotEmpty(fileDateDay))
                {
                    //新增日期文件夹
                    imgName = fileDateDay + SplitUtils.FXG + filesRecordModel.getImage_name();
                }
                logger.info("domainUrl：{}", domainUrl);

                //请求协议
                fileUrl.append(domainUrl);

                if (Objects.nonNull(filesRecordModel.getDiy_img_type())
                        && filesRecordModel.getDiy_img_type() == 0)
                {
                    fileUrl.append(SplitUtils.FXG).append(SYSTEM_DIY_THEME).append(SplitUtils.FXG).append(filesRecordModel.getDiy_id());
                } else
                {
                    fileUrl.append(SplitUtils.FXG).append(filesRecordModel.getStore_id());
                }
                fileUrl.append(SplitUtils.FXG).append(filesRecordModel.getStore_type())
                        .append(SplitUtils.FXG).append(imgName);

                break;
            case GloabConst.UploadConfigConst.IMG_UPLOAD_TXY + "":
                //腾讯云上传
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.PARAMATER_ERROR, "暂不支持腾讯云", "uploadLocalhost");
            case GloabConst.UploadConfigConst.IMG_UPLOAD_QNY + "":
                //七牛云上传
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.PARAMATER_ERROR, "暂不支持七牛云", "uploadLocalhost");
            default:
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.PARAMATER_ERROR, "参数错误", "uploadLocalhost");
        }
//        logger.info("fileUrl::::{}", fileUrl.toString());
        return fileUrl.toString();
    }

    /**
     * 图片上传
     *
     * @param imageModel - 图片配置参数
     * @return Map
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/9/28 14:26
     */
    public List<String> imgUpload(UploadImagModel imageModel, int storeId, int storeType) throws LaiKeAPIException
    {
        //图片路径集合
        List<String> urls;
        imageModel = getConfigValue(imageModel);
        String path = "";
        //代表是diy操作系统主题
        if (Objects.nonNull(imageModel.getDiy_img_type()) && imageModel.getDiy_img_type() == 0)
        {
            //系统主题文件夹/主题id/来源
            path = SYSTEM_DIY_THEME + SplitUtils.FXG + imageModel.getDiyId() + SplitUtils.FXG + storeType;
        }
        else if (imageModel.getIsCode())
        {
            //code_img 所有的验证码类型的都放这个文件夹下面 便于oss维护
            path = CODE_IMGS_FORLDER + SplitUtils.FXG + storeType;
        }
        else
        {
            path = storeId + SplitUtils.FXG + storeType;
        }
        switch (imageModel.getUploadType())
        {
            case GloabConst.UploadConfigConst.IMG_UPLOAD_LOCALHOST:
                urls = uploadLocalhost(imageModel);
                break;
            case GloabConst.UploadConfigConst.IMG_UPLOAD_LOCALHOST1:
                imageModel.setUploadPath(imageModel.getUploadPath());
                urls = uploadLocalhost(imageModel);
                break;
            case GloabConst.UploadConfigConst.IMG_UPLOAD_OSS:
                urls = uploadOss(imageModel, path);
                break;
            case GloabConst.UploadConfigConst.IMG_UPLOAD_TXY:
                urls = uploadTxy(imageModel);
                break;
            case GloabConst.UploadConfigConst.IMG_UPLOAD_QNY:
                urls = uploadQny(imageModel);
                break;
            case GloabConst.UploadConfigConst.IMG_UPLOAD_MINIO:
                urls = uploadMinIo(imageModel, path);
                break;
            default:
                logger.error("上传类型不存在{}", imageModel.getUploadType());
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.PARAMATER_ERROR, "参数错误", "imgUpload");
        }

        return urls;
    }

    /**
     * 批量下载图片
     *
     * @param imageModel -
     * @param imgMaps    -
     * @param storeId    -
     * @param storeType  -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/22 10:33
     */
    public void downImages(UploadImagModel imageModel, List<Map<String, Object>> imgMaps, HttpServletResponse response, int storeId, int storeType) throws LaiKeAPIException
    {
        imageModel = getConfigValue(imageModel);
        switch (imageModel.getUploadType())
        {
            case GloabConst.UploadConfigConst.IMG_UPLOAD_LOCALHOST:
            case GloabConst.UploadConfigConst.IMG_UPLOAD_TXY:
            case GloabConst.UploadConfigConst.IMG_UPLOAD_QNY:
                break;
            case GloabConst.UploadConfigConst.IMG_UPLOAD_OSS:
                downImagesOss(imageModel, imgMaps, response);
                break;
            default:
                logger.error("上传类型不存在{}", imageModel.getUploadType());
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.PARAMATER_ERROR, "参数错误", "downImages");
        }
    }


    /**
     * 删除图片
     *
     * @param vo -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/8 17:54
     */
    public void imgDel(DelFilesVo vo) throws LaiKeAPIException
    {
        switch (vo.getUploadType())
        {
            case GloabConst.UploadConfigConst.IMG_UPLOAD_LOCALHOST:
                imgDelLocalhost(vo.getCatalogue(), vo.getFileName());
                break;
            case GloabConst.UploadConfigConst.IMG_UPLOAD_OSS:
                imgDelOss(getConfigValue(vo.getUploadImagModel()), vo.getStoreId() + SplitUtils.FXG
                        + vo.getStoreType() + SplitUtils.FXG + DateUtil.dateFormate(vo.getAddTime(), GloabConst.TimePattern.YMD1) + SplitUtils.FXG + vo.getFileName());
                break;
            case GloabConst.UploadConfigConst.IMG_UPLOAD_MINIO:
                imgDelMinIo(getConfigValue(vo.getUploadImagModel()), vo.getStoreId() + SplitUtils.FXG
                        + vo.getStoreType() + SplitUtils.FXG + DateUtil.dateFormate(vo.getAddTime(), GloabConst.TimePattern.YMD1) + SplitUtils.FXG + vo.getFileName());
                break;
            default:
                logger.error("上传类型不存在{}", vo.getUploadType());
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.PARAMATER_ERROR, "参数错误", "imgDel");
        }
    }

    /**
     * 批量下载oss
     *
     * @param imageModel -
     * @param imgMaps    -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/22 10:39
     */
    private void downImagesOss(UploadImagModel imageModel, List<Map<String, Object>> imgMaps, HttpServletResponse response) throws LaiKeAPIException
    {
        try
        {
            if (imgMaps != null && imgMaps.size() > 0)
            {
                //连接oss
                ClientConfiguration conf = new ClientConfiguration();
                conf.setMaxConnections(200);
                conf.setSocketTimeout(10000);
                conf.setConnectionTimeout(50000);
                conf.setRequestTimeoutEnabled(true);
                conf.setRequestTimeout(100000);
                conf.setMaxErrorRetry(15);
                // 需要开启，默认不开启
                // 设置请求超时，单位毫秒，默认值300秒
                conf.setRequestTimeout(3000);
                OSSClient ossClient = new OSSClient(imageModel.getEndpoint(), imageModel.getAccessKeyId(), imageModel.getAccessKeySecret());

                //创建临时文件
//                File zipFile = File.createTempFile(BuilderIDTool.getGuid(), ".zip", new File(imageModel.getUploadPath()));
//                FileOutputStream f = new FileOutputStream(zipFile);
                // 对于每一个要被存放到压缩包的文件，都必须调用ZipOutputStream对象的putNextEntry()方法，确保压缩包里面文件不同名
                CheckedOutputStream cos      = new CheckedOutputStream(response.getOutputStream(), new Adler32());
                List<String>        imgNames = new ArrayList<>();
                //提示信息
                List<String> info = new ArrayList<>();
                try (ZipOutputStream zip = new ZipOutputStream(cos))
                {
                    for (Map<String, Object> map : imgMaps)
                    {
                        String imgUrl = MapUtils.getString(map, "image_name");
                        //publiceService.getImgPath(MapUtils.getString(map, "image_name"), vo.getStoreId())
                        String imgName   = ImgUploadUtils.getUrlImgByName(imgUrl, true);
                        String storeId   = MapUtils.getString(map, "store_id");
                        String storeType = MapUtils.getString(map, "store_type");
                        String time      = MapUtils.getString(map, "add_time");
                        String url       = storeId + SplitUtils.FXG + storeType + SplitUtils.FXG + time + SplitUtils.FXG + imgName;
                        //https://laikeds.oss-cn-shenzhen.aliyuncs.com/1/8/20240520/1792452562009153536.jpeg"
                        GetObjectRequest getObjectRequest = new GetObjectRequest(imageModel.getBucket(), url);
                        //获取图片
                        try (OSSObject ossObject = ossClient.getObject(getObjectRequest))
                        {
                            //获取图片流
                            try (InputStream inputStream = ossObject.getObjectContent())
                            {
                                //处理图片同名
                                if (imgNames.contains(imgName))
                                {
                                    imgName += BuilderIDTool.getNext(BuilderIDTool.Type.NUMBER, 3);
                                }
                                imgNames.add(imgName);
                                zip.putNextEntry(new ZipEntry(imgName));
                                byte[] buff = new byte[4 * 1024];
                                int    bytesRead;
                                // 向压缩文件中输出数据
                                logger.info("当前下载=>" + imgName);
                                while ((bytesRead = inputStream.read(buff)) != -1)
                                {
                                    zip.write(buff, 0, bytesRead);
                                }
                                inputStream.close();
                                // 当前文件写完，定位为写入下一条项目
                                zip.closeEntry();

                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                                logger.error("图片下载失败 ", e);
                            }
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                            logger.error("图片不存在 {}", imgUrl);
                            //创建一个错误日志文件,如果失败了则生成一个文件到压缩包里 (xx图下载失败)
                            info.add(String.format("【%s】图片下载失败", imgUrl));
                        }

                    }
                    //写入日志
                    /*if (info.size() > 0) {
                        String infoFilePath = builderFile(StringUtils.stringImplode(info, "\n"), ".txt");
                        if (infoFilePath != null) {
                            File tempFile = File.createTempFile(BuilderIDTool.getGuid(), ".txt", new File(infoFilePath));
                            try (FileInputStream fis = new FileInputStream(tempFile)) {
                                try (BufferedInputStream is = new BufferedInputStream(fis)) {
                                    zip.putNextEntry(new ZipEntry("下载失败列表.txt"));
                                    int bytesRead;
                                    while ((bytesRead = is.read()) != -1) {
                                        zip.write(bytesRead);
                                    }
                                    zip.closeEntry();
                                    zip.finish();
                                } catch (Exception ignored) {
                                    //Who cares?
                                }
                            } catch (Exception ignored) {
                                //Who cares?
                            } finally {
                                logger.debug("临时文件删除状态 :" + tempFile.delete());
                            }
                        }
                    }*/
                    ossClient.shutdown();
                }
                catch (Exception e)
                {
                    //Who cares?
                    logger.error("图片下载失败 ", e);
                }
                finally
                {
                    logger.debug("文件下载完成");
                }
            }
        }
        catch (Exception e)
        {
            logger.error("文件下载 - 阿里云oss下载 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.PARAMATER_ERROR, "文件不能为空", "downImagesOss");
        }
    }

    /**
     * 创建一个文件
     *
     * @param context -  文件名
     * @param suffix  - 后缀名
     * @return String - 反回一个绝对路径
     * @throws IOException-
     * @author Trick
     * @date 2021/7/22 17:40
     */
    public static String builderFile(String context, String suffix) throws IOException
    {
        File                 file = null;
        FileInputStream      fis  = null;
        BufferedInputStream  buff = null;
        BufferedOutputStream out  = null;
        try
        {
            //创建一个文件
            file = File.createTempFile(BuilderIDTool.getGuid(), suffix, new File("E:\\test"));
            fis = new FileInputStream(file);
            buff = new BufferedInputStream(fis);
            out = new BufferedOutputStream(new FileOutputStream(file));
            out.write(context.getBytes());

            return file.getCanonicalPath();
        }
        catch (Exception e)
        {
            logger.error("文件创建失败 异常:", e);
        }
        finally
        {
            if (fis != null)
            {
                fis.close();
            }
            if (buff != null)
            {
                buff.close();
            }
            if (out != null)
            {
                out.close();
            }
        }
        return null;
    }


    /**
     * 图片删除 - 阿里云oss删除
     *
     * @param imageModel -
     * @param imgName    -
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/8 17:56
     */
    private void imgDelOss(UploadImagModel imageModel, String imgName) throws LaiKeAPIException
    {
        try
        {
            if (StringUtils.isEmpty(imgName))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.PARAMATER_ERROR, "文件不能为空");
            }
            OSSClient ossClient = new OSSClient(imageModel.getEndpoint(), imageModel.getAccessKeyId(), imageModel.getAccessKeySecret());
            ossClient.deleteObject(imageModel.getBucket(), imgName);
        }
        catch (Exception e)
        {
            logger.error("图片删除 - 阿里云oss删除 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_OOSWSCQX, "oss无删除权限", "imgDelOss");
        }
    }

    /**
     * 图片删除 - MinIo删除
     *
     * @param imageModel
     * @param imgName
     * @throws LaiKeAPIException
     */
    public static void imgDelMinIo(UploadImagModel imageModel, String imgName) throws LaiKeAPIException
    {
        try
        {
            //创建MinIo对象
            MinioClient minioClient =
                    MinioClient.builder()
                            // api地址
                            .endpoint(imageModel.getServeruri())
                            // 设置的账号密码
                            .credentials(imageModel.getAccessKeyId(), imageModel.getAccessKeySecret())
                            .build();
//            imgName = imageModel.getBucket() + SplitUtils.FXG + imgName;
            RemoveObjectArgs objectArgs = RemoveObjectArgs.builder().object(imgName).bucket(imageModel.getBucket())
                    .build();
            minioClient.removeObject(objectArgs);
        }
        catch (Exception e)
        {
            logger.error("图片删除 - MinIo删除 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.PARAMATER_ERROR, "MinIo无删除权限", "imgDelMinIo");
        }
    }

    /**
     * 图片删除 - 本地删除
     *
     * @param catalogue - 目录*
     * @param fileName  - 文件名称
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/7/8 17:56
     */
    private void imgDelLocalhost(String catalogue, String fileName) throws LaiKeAPIException
    {
        try
        {
            //服务器资源路径
            StringBuilder realPath = new StringBuilder(ImgUploadUtils.DEFAULT_UPLOAD_PATH);
            //是否有目录
            if (StringUtils.isNotEmpty(catalogue))
            {
                realPath.append(File.separator).append(PinyinUtils.getPinYin(catalogue));
            }
            realPath.append(File.separator).append(fileName);
            //判断文件是否存在
            File diyFile = new File(realPath.toString());
            //删除本地图片
            if (diyFile.exists())
            {
                boolean flag = diyFile.delete();
                logger.debug("路径:{} 文件存在,删除状态:{}", diyFile.getPath(), flag);
                if (!flag)
                {
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "文件删除失败");
                }
            }
            else
            {
                logger.debug("路径:{} 文件不存在,删除失败", diyFile.getPath());
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.DATA_NOT_EXIST, "文件不存在");
            }
        }
        catch (Exception e)
        {
            logger.error("图片删除 - 本地删除 异常", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.PARAMATER_ERROR, "文件不能为空", "imgDelLocalhost");
        }
    }

    /**
     * 图片上传 - 阿里oss上传
     *
     * @param imageModel - 图片上传配置,imgName - 图片名称,file -文件流
     * @return List<String>
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/9/29 16:42
     */
    private List<String> uploadOss(UploadImagModel imageModel, String path) throws LaiKeAPIException
    {
        try
        {
            List<String> urls = new ArrayList<>();
            //转过来的文件不为空
            if (imageModel.getMultipartFiles() != null && imageModel.getMultipartFiles().size() > 0)
            {
                for (MultipartFile file : imageModel.getMultipartFiles())
                {
                    StringBuilder pathBuilder = new StringBuilder(path);
                    String        url         = "";
                    //取到文件名
                    String fileName = file.getOriginalFilename();
                    //校验图片
                    if (DataCheckTool.checkUploadImgageFormate(fileName))
                    {
                        //处理图片重名
                        LKTSnowflakeIdWorker lktSnowflakeIdWorker = new LKTSnowflakeIdWorker();
                        String               imgName              = lktSnowflakeIdWorker.nextId() + getUrlImgBySuffix(fileName);
                        //增加时间文件夹
                        pathBuilder.append(SplitUtils.FXG).append(DateUtil.dateFormate(new Date(), GloabConst.TimePattern.YMD1));
                        pathBuilder.append(SplitUtils.FXG).append(imgName);

                        //设置https协议
                        ClientConfiguration clientConfiguration = new ClientConfiguration();
                        clientConfiguration.setProtocol(Protocol.HTTPS);
                        //创建oss对象
                        OSSClient       ossClient = new OSSClient(imageModel.getEndpoint(), imageModel.getAccessKeyId(), imageModel.getAccessKeySecret(), clientConfiguration);
                        PutObjectResult resultOss;
                        try
                        {
                            LifecycleRule rule = new LifecycleRule();
                            //url = 店铺id/来源/时间(yyyymmdd)/图片名称
                            resultOss = ossClient.putObject(imageModel.getBucket(), pathBuilder.toString(), file.getInputStream());
                        }
                        catch (IOException e)
                        {
                            logger.error(fileName + "图片上传失败 异常", e);
                            throw new LaiKeAPIException(ErrorCode.SysErrorCode.UPLOAD_FAIL, "图片上传失败", "uploadLocalhost");
                        }
                        System.out.println(JSON.toJSONString(resultOss));
                        //获取url链接
                        url = this.getUrl(pathBuilder.toString(), ossClient, imageModel.getBucket());
                        ossClient.shutdown();
                    }

                    urls.add(url);
                }
                return urls;
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.PARAMATER_ERROR, "文件不能为空", "uploadOss");
            }
        }
        catch (LaiKeAPIException l)
        {
            logger.error("图片上传 异常失败 ", l);
            throw l;
        }
        catch (Exception e)
        {
            logger.error("图片上传 异常失败 ", e);
            throw new LaiKeAPIException(ErrorCode.SysErrorCode.UPLOAD_FAIL, "图片上传失败", "uploadLocalhost");
        }
    }

    /**
     * 图片上传 - MinIo上传
     *
     * @param imageModel - 图片上传配置,imgName - 图片名称,file -文件流
     * @return List<String>
     * @throws LaiKeAPIException -
     * @author gp
     * @date 2023-07-20
     */
    private List<String> uploadMinIo(UploadImagModel imageModel, String path) throws LaiKeAPIException
    {
        try
        {
            List<String> urls = new ArrayList<>();
            //转过来的文件不为空
            if (imageModel.getMultipartFiles() != null && imageModel.getMultipartFiles().size() > 0)
            {
                for (MultipartFile file : imageModel.getMultipartFiles())
                {
                    StringBuilder pathBuilder = new StringBuilder(path);
                    String        url         = "";
                    //取到文件名
                    String fileName = file.getOriginalFilename();
                    //校验图片
                    if (DataCheckTool.checkUploadImgageFormate(fileName))
                    {
                        //处理图片重名
                        LKTSnowflakeIdWorker lktSnowflakeIdWorker = new LKTSnowflakeIdWorker();
                        String               imgName              = lktSnowflakeIdWorker.nextId() + getUrlImgBySuffix(fileName);
                        //增加时间文件夹
                        pathBuilder.append(SplitUtils.FXG).append(DateUtil.dateFormate(new Date(), GloabConst.TimePattern.YMD1));
                        pathBuilder.append(SplitUtils.FXG).append(imgName);
                        //获取文件流
                        InputStream stream = file.getInputStream();
                        //创建MinIo对象
                        MinioClient minioClient =
                                MinioClient.builder()
                                        // api地址 //协议端口用户自己配置2023-12-06
                                        .endpoint(imageModel.getServeruri())
                                        // 设置的账号密码
                                        .credentials(imageModel.getAccessKeyId(), imageModel.getAccessKeySecret())
                                        .build();
                        try
                        {
                            // 检查桶是否存在
                            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(imageModel.getBucket()).build());
                            if (!found)
                            {
                                // 创建桶
                                minioClient.makeBucket(MakeBucketArgs.builder().bucket(imageModel.getBucket()).build());
                            }
                            //上传文件到存储桶中
                            PutObjectArgs objectArgs = PutObjectArgs.builder().object(pathBuilder.toString())
                                    .bucket(imageModel.getBucket())
//                                    .contentType("application/octet-stream") //上传格式(默认)
                                    .stream(stream, stream.available(), -1).build();
                            minioClient.putObject(objectArgs);
                            //关闭流
                            stream.close();
                        }
                        catch (IOException e)
                        {
                            logger.error(fileName + "图片上传失败 异常", e);
                            throw new LaiKeAPIException(ErrorCode.SysErrorCode.UPLOAD_FAIL, "图片上传失败", "uploadLocalhost");
                        }
                        //获取url链接
                        url = imageModel.getEndpoint() + SplitUtils.FXG + imageModel.getBucket() + SplitUtils.FXG + pathBuilder;
                    }

                    urls.add(url);
                }
                return urls;
            }
            else
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.PARAMATER_ERROR, "文件不能为空", "uploadOss");
            }
        }
        catch (LaiKeAPIException l)
        {
            l.printStackTrace();
            logger.error("图片上传 异常失败 ", l);
            throw l;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("图片上传 异常失败 ", e);
            throw new LaiKeAPIException(ErrorCode.SysErrorCode.UPLOAD_FAIL, "图片上传失败", "uploadLocalhost");
        }
    }

    /**
     * 图片上传 - 本地上传
     *
     * @param imageModel - 图片上传配置,imgName - 图片名称,file -文件流
     * @return List<String>
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/9/29 16:42
     */
    private List<String> uploadLocalhost(UploadImagModel imageModel) throws LaiKeAPIException
    {
        List<String> urls = new ArrayList<>();
        //转过来的文件不为空
        if (imageModel.getMultipartFiles() != null && imageModel.getMultipartFiles().size() > 0)
        {
            for (MultipartFile file : imageModel.getMultipartFiles())
            {
                String url = "";
                //取到文件名
                String fileName = file.getOriginalFilename();
                if (DataCheckTool.checkUploadImgageFormate(fileName))
                {
                    //处理图片重名
                    String imgName = BuilderIDTool.getSnowflakeId() + ImgUploadUtils.getUrlImgBySuffix(fileName).toLowerCase();
                    try
                    {
                        //服务器资源路径
                        String realPath = DEFAULT_UPLOAD_PATH;
                        //自定义目录
                        StringBuilder diyPath = new StringBuilder(File.separator);
                        if (StringUtils.isNotEmpty(imageModel.getUploadPath()))
                        {
                            realPath = imageModel.getUploadPath();
                        }
                        if (StringUtils.isNotEmpty(imageModel.getPath()))
                        {
                            diyPath.append(File.separator);
                            diyPath.append(imageModel.getPath());
                            diyPath.append(File.separator);
                        }
                        //判断文件夹是否存在,不存在则创建
                        logger.info("文件夹 {}", realPath + diyPath);
                        if (!this.isDirectory(realPath + diyPath))
                        {
                            throw new LaiKeAPIException(ErrorCode.SysErrorCode.UPLOAD_FAIL, "图片上传失败", "uploadLocalhost");
                        }
                        url = imgName;
                        //拼接上传路径
                        String path = realPath + diyPath + imgName;
                        logger.info("【本地上传】上传路径{}", path);
                        file.transferTo(new File(SplitUtils.FXG + path));
                        url = path;

                        ImgUploadUtils.givePower(GloabConst.LinuxPower.RW_XR_ALL, path);
                    }
                    catch (IOException e)
                    {
                        logger.error(fileName + "图片上传失败", e);
                        throw new LaiKeAPIException(ErrorCode.SysErrorCode.UPLOAD_FAIL, "图片上传失败", "uploadLocalhost");
                    }
                }

                urls.add(url);
            }

        }
        else
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.PARAMATER_ERROR, "参数错误", "uploadLocalhost");
        }
        return urls;
    }

    /**
     * 图片上传 - 腾讯云
     *
     * @param imageModel - 图片上传配置,imgName - 图片名称,file -文件流
     * @return List<String>
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/9/29 16:42
     */
    private List<String> uploadTxy(UploadImagModel imageModel) throws LaiKeAPIException
    {

        throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "暂不支持腾讯云", "uploadLocalhost");
    }

    /**
     * 判断文件夹是否存在,不存在则创建
     * [win环境请不要使用GloabConst.UploadConfigConst.IMG_UPLOAD_LOCALHOST 请使用 win环境请不要使用GloabConst.UploadConfigConst.IMG_UPLOAD_LOCALHOST1]
     *
     * @param path -
     * @return boolean
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/12/6 11:42
     */
    public boolean isDirectory(String path) throws LaiKeAPIException
    {
        boolean flag = true;
        try
        {
            //判断操作系统
            boolean isWin = !System.getProperty("os.name").toLowerCase().startsWith("linux");
            if (isWin)
            {
                isWin = !System.getProperty("os.name").toLowerCase().startsWith("mac");
            }
            logger.debug("当前环境是否为win:{}", isWin);
            if (StringUtils.isEmpty(path))
            {
                flag = false;
            }
            //win环境盘符
            String panF = "";
            //拆分文件夹
            String[] fileNames;
            if (isWin)
            {
                path = StringUtils.trim(path, "/").replace("/", "\\");
                if (path.contains(":"))
                {
                    //去除本地盘符
                    int index = path.indexOf(":/") + 2;
                    panF = path.substring(0, index + 2);
                    path = path.substring(index + 2);
                }
                fileNames = path.split(File.separator + File.separator);
            }
            else
            {
                fileNames = path.split(File.separator);
                if (fileNames.length > 0)
                {
                    //linux 目录/xx/xx
                    fileNames[0] = File.separator + fileNames[0];
                }
            }
            StringBuilder filePath = new StringBuilder(panF);
            int           one      = 0;
            for (String fileName : fileNames)
            {
                if (StringUtils.isEmpty(fileName))
                {
                    continue;
                }
                if (one == 0)
                {
                    filePath.append(File.separator);
                }
                filePath.append(fileName).append(File.separator);
                logger.debug("检查目录是否存在 filePath:{}", filePath);
                //判断文件夹是否存在,不存在则创建
                File    diyFile = new File(filePath.toString());
                boolean isDir   = diyFile.exists() && diyFile.isDirectory();
                logger.debug("检查目录是否存在 filePath:{}", filePath);
                if (!isDir)
                {
                    logger.debug("目录:{} 不存在,创建状态:{}", diyFile.getPath(), flag = diyFile.mkdir());
                    if (!flag)
                    {
                        break;
                    }
                    ImgUploadUtils.givePower(GloabConst.LinuxPower.RW_XR_ALL, filePath.append(SplitUtils.FXG).toString());
                }
                else
                {
                    filePath.append(SplitUtils.FXG);
                }
                one++;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            flag = false;
        }
        return flag;
    }

    /**
     * 图片上传 - 七牛云
     *
     * @param imageModel - 图片上传配置,imgName - 图片名称,file -文件流
     * @return List<String>
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/9/29 16:42
     */
    private List<String> uploadQny(UploadImagModel imageModel) throws LaiKeAPIException
    {


        throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "暂不支持七牛云", "uploadLocalhost");
    }


    /**
     * 获取配置数据
     *
     * @param uploadImagModel 文件配置参数
     * @return UploadImagModel
     * @author Trick
     * @date 2020/9/30 13:52
     */
    private UploadImagModel getConfigValue(UploadImagModel uploadImagModel)
    {
        if (uploadImagModel == null || uploadImagModel.getUploadConfigs() == null)
        {
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.PARAMATER_ERROR, "参数错误", "getConfigValue");
        }
        //装载参数
        for (UploadConfigModel confg : uploadImagModel.getUploadConfigs())
        {
            String attr      = confg.getAttr();
            String attrVlaue = confg.getAttrvalue();
            switch (attr)
            {
                case GloabConst.UploadConfigConst.ACCESSKEYID:
                    uploadImagModel.setAccessKeyId(attrVlaue);
                    break;
                case GloabConst.UploadConfigConst.ACCESSKEYSECRET:
                    uploadImagModel.setAccessKeySecret(attrVlaue);
                    break;
                case GloabConst.UploadConfigConst.BUCKET:
                    uploadImagModel.setBucket(attrVlaue);
                    break;
                case GloabConst.UploadConfigConst.ENDPOINT:
                    uploadImagModel.setEndpoint(attrVlaue);
                    break;
                case GloabConst.UploadConfigConst.ISOPENZDY:
                    uploadImagModel.setIsopenzdy(attrVlaue);
                    break;
                case GloabConst.UploadConfigConst.IMAGESTYLE:
                    uploadImagModel.setImagestyle(attrVlaue);
                    break;
                case GloabConst.UploadConfigConst.UPLOADIMG_DOMAIN:
                    uploadImagModel.setUploadImgDomain(attrVlaue);
                    break;
                case GloabConst.UploadConfigConst.UPLOADIMG:
                    uploadImagModel.setUploadImg(attrVlaue);
                    break;
                case GloabConst.UploadConfigConst.MINIOSERVERURI:
                    uploadImagModel.setServeruri(attrVlaue);
                    break;
                default:
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.PARAMATER_ERROR, "参数错误", "getConfigValue");
            }
        }
        return uploadImagModel;
    }

    /**
     * 获得url链接
     *
     * @param key        图片名称
     * @param ossClient  -
     * @param bucketName - 仓库名称
     * @return String
     */
    private String getUrl(String key, OSSClient ossClient, String bucketName)
    {
        // 设置URL过期时间为10年 3600l* 1000*24*365*10
        Date expiration = new Date(System.currentTimeMillis() + 3600L * 1000 * 24 * 365 * 10);
        // 生成URL
        URL url = ossClient.generatePresignedUrl(bucketName, key, expiration);
        if (url != null)
        {
            return url.toString();
        }
        return null;
    }


    /**
     * 获取url中的图片名称
     *
     * @param imgUrl -
     * @return String
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/11/18 15:44
     */
    @Deprecated
    public static String getUrlImgByName(String imgUrl) throws LaiKeAPIException
    {
        try
        {
            return getUrlImgByName(imgUrl, true);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "获取图片名称失败", "getUrlImgByName");
        }
    }

    /**
     * 获取图片名称
     *
     * @param imgUrl  -
     * @param isParma -  是否去掉url参数
     * @return String
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021/5/20 14:04
     */
    public static String getUrlImgByName(String imgUrl, boolean isParma) throws LaiKeAPIException
    {
        StringBuilder sb = new StringBuilder(imgUrl);
        try
        {
            if (StringUtils.isNotEmpty(imgUrl))
            {
                int paramIndex    = sb.indexOf("?");
                int nameLastIndex = sb.lastIndexOf("/");
                if (isParma)
                {
                    if (paramIndex > 0)
                    {
                        //去掉多余的参数
                        sb.replace(paramIndex, sb.length(), "");
                        nameLastIndex = sb.lastIndexOf("/");
                    }
                }
                else
                {
                    int maxNum = 50;
                    int i      = 0;
                    while (paramIndex > 0 && nameLastIndex > paramIndex)
                    {
                        System.out.println(i);
                        //去掉参数中的 '/'
                        sb.replace(nameLastIndex, sb.length(), "");
                        nameLastIndex = sb.lastIndexOf("/");
                        if (i > maxNum)
                        {
                            break;
                        }
                        i++;
                    }
                }
                imgUrl = sb.substring(nameLastIndex + 1, sb.length());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "获取图片名称失败", "getUrlImgByName");
        }
        return imgUrl;
    }

    /**
     * 获取路径中的文件名字
     *
     * @param imgPath -
     * @return String
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2021/12/6 15:59
     */
    public static String getPathImgByName(String imgPath) throws LaiKeAPIException
    {
        StringBuilder sb = new StringBuilder(imgPath);
        try
        {
            if (StringUtils.isNotEmpty(imgPath))
            {
                int index = sb.lastIndexOf(File.separator) + 1;
                imgPath = sb.substring(index);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "获取图片名称失败", "getUrlImgByName");
        }
        return imgPath;
    }


    /**
     * 净化url
     *
     * @param imgUrl  -
     * @param isParma -  是否去掉url参数
     * @return String
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2021-07-01 10:53:14
     */
    public static String getUrlPure(String imgUrl, boolean isParma) throws LaiKeAPIException
    {
        StringBuilder sb = new StringBuilder(imgUrl);
        try
        {
            if (!StringUtils.isEmpty(imgUrl))
            {
                if (isParma)
                {
                    if (imgUrl.indexOf("?") > 0)
                    {
                        //去掉多余的参数
                        sb.replace(imgUrl.lastIndexOf("?"), imgUrl.length(), "");
                    }
                }
                imgUrl = sb.toString();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "获取图片名称失败", "getUrlImgByName");
        }
        return imgUrl;
    }


    /**
     * 获取url中图片后缀
     *
     * @param imgUrl -
     * @return String
     * @throws LaiKeAPIException -
     * @author Trick
     * @date 2020/12/22 16:30
     */
    public static String getUrlImgBySuffix(String imgUrl) throws LaiKeAPIException
    {
        try
        {
            if (!StringUtils.isEmpty(imgUrl))
            {
                String imageName = getUrlImgByName(imgUrl);
                return imageName.substring(imageName.lastIndexOf("."));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "获取图片后缀失败", "getUrlImgBySuffix");
        }
        return null;
    }

    /**
     * 根据后缀获取 Mime
     *
     * @param suffix -
     * @return String
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2020/12/22 16:36
     */
    public static String getUrlImgByMimeType(String suffix) throws LaiKeAPIException
    {
        try
        {
            if (!StringUtils.isEmpty(suffix))
            {
                FileNameMap fileNameMap = URLConnection.getFileNameMap();
                return fileNameMap.getContentTypeFor(suffix);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "根据后缀获取Mime失败", "getUrlImgBySuffix");
        }
        return null;
    }


    /**
     * base64转Multipart
     *
     * @param base64string -
     * @param localPath    -
     * @return MultipartFile
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/4/29 13:59
     */
    public MultipartFile base64ToMultipart(String base64string, String localPath) throws LaiKeAPIException
    {
        MultipartFile resultFile;
        try
        {
            //将byte转换为Multipart
            File file = base64ToFile(base64string, localPath);
            try (InputStream inputStream = new FileInputStream(file))
            {
                resultFile = new MockMultipartFile("file", file.getName(), getBase64MediaType(base64string), inputStream);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "图片转换失败", "base64ToMultipart");
        }

        return resultFile;
    }

    /**
     * base64转 File
     *
     * @param base64string -
     * @param localPath    - 图片路径,不要带文件名
     * @return MultipartFile
     * @throws LaiKeAPIException-
     * @author Trick
     * @date 2022/4/29 13:59
     */
    public File base64ToFile(String base64string, String localPath) throws LaiKeAPIException
    {
        File resultFile;
        try
        {
            //图片文件夹按时间分组
            localPath += File.separator + DateUtil.dateFormate(new Date(), GloabConst.TimePattern.YMD1);
            if (!this.isDirectory(localPath))
            {
                throw new LaiKeAPIException(ErrorCode.SysErrorCode.UPLOAD_FAIL, "图片上传失败", "uploadLocalhost");
            }
            // data:image/gif;base64, 获取到前缀的定位
            String fileName = File.separator + +BuilderIDTool.getSnowflakeId() + SplitUtils.XSD + "%s";
            int    prefix   = base64string.indexOf("base64,");
            if (prefix > 0)
            {
                int index = prefix + 7;
                //去除前缀
                base64string = base64string.substring(index);
            }
            localPath += String.format(fileName, getBase64Suffix(base64string));

            //将去除前缀的图片文本转化为byte流
            Base64.Decoder decoder = Base64.getDecoder();

            // Base64解码
            byte[] b = decoder.decode(base64string);
            for (int i = 0; i < b.length; ++i)
            {
                if (b[i] < 0)
                {
                    b[i] += 256;
                }
            }
            //将byte转换为文件
            try (FileOutputStream out = new FileOutputStream(localPath))
            {
                out.write(b);
                resultFile = new File(localPath);
                ImgUploadUtils.givePower(GloabConst.LinuxPower.RW_XR_ALL, resultFile.getPath());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.BUSY_NETWORK, "图片转换失败", "base64ToFile");
        }

        return resultFile;
    }

    /**
     * 获取base64后缀
     *
     * @param base64string -
     * @return String
     * @author Trick
     * @date 2022/4/29 15:38
     */
    public String getBase64Suffix(String base64string)
    {
        //获取图片后缀
        int    suffixIndex = base64string.indexOf(SplitUtils.FXG) + 1;
        String suffix      = GloabConst.UploadConfigConst.IMG_PNG;
        if (suffixIndex > 0)
        {
            int suffixLastIndex = base64string.indexOf(";base64,");
            suffix = base64string.substring(suffixIndex, suffixLastIndex);
        }
        return suffix;
    }

    /**
     * 获取base64文件媒体类型
     *
     * @param base64string -
     * @return String
     * @author Trick
     * @date 2022/4/29 15:38
     */
    public String getBase64MediaType(String base64string)
    {
        String mediaType = "image/png";

        if (base64string.indexOf(MediaType.IMAGE_PNG_VALUE) > 0)
        {
            return MediaType.IMAGE_PNG_VALUE;
        }
        else if (base64string.indexOf(MediaType.IMAGE_JPEG_VALUE) > 0 || base64string.indexOf("image/jpg") > 0)
        {
            return MediaType.IMAGE_JPEG_VALUE;
        }
        else if (base64string.indexOf(MediaType.IMAGE_GIF_VALUE) > 0)
        {
            return MediaType.IMAGE_GIF_VALUE;
        }

        return mediaType;
    }

    /**
     * 赋予权限
     *
     * @param roleCode - 权限代码
     * @param path     - 路径
     * @throws IOException-
     * @author Trick
     * @date 2023/2/8 15:47
     */
    public static void givePower(String roleCode, String path) throws IOException
    {
        // 参数验证
        if ( path == null || path.isEmpty()) {
            logger.error("路径不能为空");
            return;
        }
        //判断操作系统
        boolean isWin = !System.getProperty("os.name").toLowerCase().startsWith("linux");
        if (isWin)
        {
            isWin = !System.getProperty("os.name").toLowerCase().startsWith("mac");
        }
        if (!isWin)
        {
            File target = new File(path);
            if (!target.exists()) {
                logger.error("指定的路径不存在: {}", path);
                return;
            }

            // 根据目标是文件夹还是文件决定是否使用 -R 选项
            String option = target.isDirectory() ? "-R" : "";
            String cmd = String.format("chmod %s %s %s", option, roleCode, path);
            logger.info("执行权限命令: {}", cmd);
            BufferedReader reader = null;
            BufferedReader errorReader = null;
            try
            {
                Process process = Runtime.getRuntime().exec(cmd);

                // 获取命令执行的标准输出流
                reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                System.out.println("命令执行的标准输出：");
                while ((line = reader.readLine()) != null)
                {
                    logger.info(line);
                }

                // 获取命令执行的错误输出流
                errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                System.out.println("命令执行的错误输出：");
                while ((line = errorReader.readLine()) != null)
                {
                    logger.error("错误信息:"+line);
                }

            }
            finally
            {
                if(reader!=null)
                {
                    reader.close();
                }

                if(errorReader!=null)
                {
                    errorReader.close();
                }
            }
        }
    }
}
