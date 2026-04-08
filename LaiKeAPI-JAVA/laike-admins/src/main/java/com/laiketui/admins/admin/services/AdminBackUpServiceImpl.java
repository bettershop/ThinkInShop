package com.laiketui.admins.admin.services;


import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson2.JSON;
import com.laiketui.admins.api.admin.AdminBackUpService;
import com.laiketui.common.config.DatabaseConfig;
import com.laiketui.common.mapper.BackUpConfigModelMapper;
import com.laiketui.common.mapper.BackUpRecordModelMapper;
import com.laiketui.common.mapper.ConfigModelMapper;
import com.laiketui.common.mapper.CustomerModelMapper;
import com.laiketui.core.cache.RedisUtil;
import com.laiketui.core.exception.LaiKeAPIException;
import com.laiketui.core.lktconst.DictionaryConst;
import com.laiketui.core.lktconst.ErrorCode;
import com.laiketui.core.lktconst.GloabConst;
import com.laiketui.core.utils.tool.DateUtil;
import com.laiketui.core.utils.tool.StringUtils;
import com.laiketui.domain.backUp.BackUpConfigModel;
import com.laiketui.domain.backUp.BackUpRecordModel;
import com.laiketui.domain.config.ConfigModel;
import com.laiketui.domain.mch.CustomerModel;
import com.laiketui.domain.vo.MainVo;
import org.apache.commons.collections.MapUtils;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.LockSupport;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author zhuqingyu
 * @create 2024/8/19
 */
@Service
public class AdminBackUpServiceImpl implements AdminBackUpService
{

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String BACKUP_LOCK_PREFIX            = "backup:lock:";
    private static final String RESTORE_LOCK_PREFIX           = "backup:restore:lock:";
    private static final long   BACKUP_LOCK_LEASE_SEC         = 2 * 60 * 60;
    private static final long   RESTORE_LOCK_LEASE_SEC        = 30 * 60;
    private static final int    BACKUP_MAX_RETRY              = 3;
    private static final long   RETRY_BACKOFF_MS              = 5000L;
    private static final int    DEFAULT_RETENTION_DAYS        = 7;
    private static final int    DEFAULT_MAX_CONCURRENT_BACKUP = Math.max(2, Runtime.getRuntime().availableProcessors());

    @Autowired
    private BackUpConfigModelMapper backUpConfigModelMapper;

    @Autowired
    private BackUpRecordModelMapper backUpRecordModelMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private ConfigModelMapper configModelMapper;


    @Autowired
    private CustomerModelMapper customerModelMapper;
    private final ExecutorService executor = new ThreadPoolExecutor(
            DEFAULT_MAX_CONCURRENT_BACKUP,
            Math.max(DEFAULT_MAX_CONCURRENT_BACKUP * 2, 4),
            60L,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1000),
            new ThreadPoolExecutor.CallerRunsPolicy()
    );

    @Override
    public Map<String, Object> queryConfig(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            BackUpConfigModel backUpConfigModel = new BackUpConfigModel();
            backUpConfigModel.setStore_id(vo.getStoreId());
            backUpConfigModel = backUpConfigModelMapper.selectOne(backUpConfigModel);
            if (backUpConfigModel != null)
            {
                resultMap.put("backUpConfigModel", backUpConfigModel);
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("查询备份配置 异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "queryConfig");
        }
        return resultMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addConfig(MainVo vo, Integer is_open, String execute_cycle, String url, Integer selectDate) throws LaiKeAPIException
    {
        try
        {

            if (is_open == 1 && StringUtils.isEmpty(execute_cycle))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSYC, "参数异常", "addConfig");
            }
            if (is_open == 1 && StringUtils.isEmpty(url))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_LJBNWK, "参数异常", "addConfig");
            }
            BackUpConfigModel backUpConfigModel = new BackUpConfigModel();
            backUpConfigModel.setStore_id(vo.getStoreId());
            backUpConfigModel.setRecycle(0);
            backUpConfigModel = backUpConfigModelMapper.selectOne(backUpConfigModel);
            BackUpConfigModel saveBackUpConfigModel = new BackUpConfigModel();
            int               i                     = 0;
            //不为空修改，为空新增
            if (backUpConfigModel != null)
            {
                backUpConfigModel.setIs_open(is_open);
                backUpConfigModel.setExecute_cycle(execute_cycle);
                backUpConfigModel.setUrl(url);
                backUpConfigModel.setAdd_time(new Date());
                backUpConfigModel.setQuery_data(selectDate);
                i = backUpConfigModelMapper.updateByPrimaryKeySelective(backUpConfigModel);
            }
            else
            {
                saveBackUpConfigModel.setStore_id(vo.getStoreId());
                saveBackUpConfigModel.setIs_open(is_open);
                saveBackUpConfigModel.setExecute_cycle(execute_cycle);
                saveBackUpConfigModel.setUrl(url);
                saveBackUpConfigModel.setAdd_time(new Date());
                saveBackUpConfigModel.setRecycle(0);
                saveBackUpConfigModel.setQuery_data(selectDate);
                //backUpConfigModel.setQuery_data(selectDate);
                i = backUpConfigModelMapper.insertSelective(saveBackUpConfigModel);
            }
            if (i < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addConfig");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加数据备份 异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "addConfig");
        }
    }

    @Override
    public Map<String, Object> backUpRecord(MainVo vo) throws LaiKeAPIException
    {
        Map<String, Object> resultMap = new HashMap<>(16);
        try
        {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("store_id", vo.getStoreId());
            paramMap.put("pageStart", vo.getPageNo());
            paramMap.put("pageEnd", vo.getPageSize());
            int                       total = backUpRecordModelMapper.countRecord(paramMap);
            List<Map<String, Object>> list  = new ArrayList<>();
            if (total > 0)
            {
                list = backUpRecordModelMapper.recordList(paramMap);
                for (Map<String, Object> map : list)
                {
                    map.put("add_time", DateUtil.dateFormate(MapUtils.getString(map, "add_time"), GloabConst.TimePattern.YMDHMS));

                    if (StringUtils.isNotEmpty(map.get("file_url")))
                    {
                        // 找到"/sql"在字符串中第一次（或最后一次，因为在这个例子中"/sql"只出现一次）出现的位置
                        int index = MapUtils.getString(map, "file_url").indexOf("/sql");

                        // 检查是否找到了"/sql"
                        if (index != -1)
                        {
                            // 截取从"/sql"开始到字符串末尾的所有内容
                            String subPath = MapUtils.getString(map, "file_url").substring(index);
                            map.put("xz_url", subPath);
                        }
                        else
                        {
                            map.put("xz_url", MapUtils.getString(map, "file_url"));
                        }
                    }
                    else
                    {
                        map.put("xz_url", "");
                    }
                }
            }
            resultMap.put("total", total);
            resultMap.put("list", list);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("查询备份记录 异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "backUpRecord");
        }
        return resultMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delBackUpRecord(MainVo vo, Integer id) throws LaiKeAPIException
    {
        try
        {
            if (StringUtils.isEmpty(id))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSYC, "参数异常", "delBackUpRecord");
            }
            BackUpRecordModel backUpRecordModel = backUpRecordModelMapper.selectByPrimaryKey(id);
            if (backUpRecordModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSYC, "参数异常", "delBackUpRecord");
            }
            int i = 0;
            backUpRecordModel.setRecycle(1);
            i = backUpRecordModelMapper.updateByPrimaryKeySelective(backUpRecordModel);
            if (i < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delBackUpRecord");
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("删除备份记录 异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "delBackUpRecord");
        }
    }
    private final Semaphore backupSemaphore = new Semaphore(DEFAULT_MAX_CONCURRENT_BACKUP);
    @Autowired
    private RedissonClient redissonClient;

    @Override
    public void reduction(MainVo vo, Integer id) throws LaiKeAPIException
    {
        BackUpRecordModel savebackUpRecordModel = new BackUpRecordModel();
        RLock             lock                  = null;
        try
        {
            if (StringUtils.isEmpty(id))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSYC, "参数异常", "delBackUpRecord");
            }
            BackUpRecordModel backUpRecordModel = backUpRecordModelMapper.selectByPrimaryKey(id);
            if (backUpRecordModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSYC, "参数异常", "delBackUpRecord");
            }
            //获取数据库配置
            String         jdbcUrl        = databaseConfig.getUrl();
            //还原完之后修改数据
            savebackUpRecordModel.setFile_url(backUpRecordModel.getFile_url());
            savebackUpRecordModel.setFile_name(backUpRecordModel.getFile_name());
            savebackUpRecordModel.setFile_size(backUpRecordModel.getFile_size());
            savebackUpRecordModel.setId(backUpRecordModel.getId());
            // 去除查询参数部分
            int    index    = jdbcUrl.indexOf('?');
            String mainPart = index == -1 ? jdbcUrl : jdbcUrl.substring(0, index);
            // 分割URL以获取数据库名
            String[] parts = mainPart.split("//");
            if (parts.length > 2)
            {
                logger.info("获取数据库信息失败，请检查数据库配置");
                // 如果URL格式不正确，则抛出异常或返回错误
                throw new IllegalArgumentException("Unexpected URL format");
            }
            parts = parts[parts.length - 1].split("/");
            //数据库配置
            String username = databaseConfig.getUsername();
            String password = databaseConfig.getPassword();
            String db_name  = "";
            String db_ip    = "";
            String db_port  = "";
            db_name = parts[parts.length - 1];
            // 分割URL以获取主机名和端口号（如果有的话）
            String hostPortPart = parts[0];
            if (hostPortPart.contains(":"))
            {
                String[] hostPort = hostPortPart.split(":");
                db_ip = hostPort[0];
                db_port = hostPort[1];
            }
            else
            {
                // 没有指定端口号的情况
                db_ip = hostPortPart;
                db_port = "3306";
            }
            // 数据库恢复接口：同一商城同一时间只允许一个恢复任务执行
            lock = tryLock(RESTORE_LOCK_PREFIX + backUpRecordModel.getStore_id(), 0, RESTORE_LOCK_LEASE_SEC);
            if (lock == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QSHZS, "恢复任务正在执行，请稍后再试");
            }
            importDbFile(db_ip, db_port, username, password, db_name, backUpRecordModel.getFile_url());
            //导入成功后更新状态
            savebackUpRecordModel.setStatus(BackUpRecordModel.BACKUP_RECORD_STATUS_COMPLETED);
            backUpRecordModelMapper.updateByPrimaryKeySelective(savebackUpRecordModel);
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            if (savebackUpRecordModel.getId() != null)
            {
                //导入失败标记失败状态，避免误判为成功
                savebackUpRecordModel.setStatus(BackUpRecordModel.BACKUP_RECORD_STATUS_FAIL);
                backUpRecordModelMapper.updateByPrimaryKeySelective(savebackUpRecordModel);
            }
            logger.error("还原备份 异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "reduction");
        }
        finally
        {
            if (lock != null && lock.isHeldByCurrentThread())
            {
                lock.unlock();
            }
        }
    }

    @Override
    public void immediately(MainVo vo) throws LaiKeAPIException
    {
        String msg = null;
        RLock   lock           = null;
        boolean permitAcquired = false;
        try
        {
            BackUpConfigModel backUpConfigModel = new BackUpConfigModel();
            backUpConfigModel.setStore_id(vo.getStoreId());
            backUpConfigModel = backUpConfigModelMapper.selectOne(backUpConfigModel);
            if (Objects.isNull(backUpConfigModel))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QXSZSJKBFPZ, "数据库备份配置为空");
            }
            if (!Objects.equals(backUpConfigModel.getIs_open(), 1))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_SJCW, "数据库备份未开启");
            }
            if (StringUtils.isEmpty(backUpConfigModel.getUrl()))
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_LJBNWK, "备份路径为空");
            }

            // 分布式锁：同一商城只允许一个备份任务执行
            lock = tryLock(BACKUP_LOCK_PREFIX + vo.getStoreId(), 5, BACKUP_LOCK_LEASE_SEC);
            if (lock == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QSHZS, "备份任务正在执行，请稍后再试");
            }
            permitAcquired = backupSemaphore.tryAcquire();
            if (!permitAcquired)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_QSHZS, "备份任务繁忙，请稍后再试");
            }

            Future<Integer> future = executor.submit(new Callable<Integer>()
            {
                @Override
                public Integer call() throws LaiKeAPIException
                {
                    try
                    {
                        // 调用insertRecord方法
                        return insertRecord(vo, "人工");
                    }
                    catch (LaiKeAPIException e)
                    {
                        e.printStackTrace();
                        return null; // 或者使用Optional<Integer>来避免null
                    }
                }
            });
            try
            {
                Integer id = future.get(); // 这会阻塞直到Callable任务完成
                if (id != null)
                {
                    // 使用返回的ID调用exportV3DB
                    msg = exportV3DB(backUpConfigModel.getUrl(), id);
                }
                else
                {
                    logger.info("添加记录异常");
                }
                if (org.apache.commons.lang3.StringUtils.isNotEmpty(msg))
                {
                    throw new LaiKeAPIException(ErrorCode.SysErrorCode.ALL_CODE,msg);
                }
            }
            catch (InterruptedException | ExecutionException e)
            {
                e.printStackTrace();
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("立即备份 异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "immediately");
        }
        finally
        {
            if (lock != null && lock.isHeldByCurrentThread())
            {
                lock.unlock();
            }
            if (permitAcquired)
            {
                backupSemaphore.release();
            }
        }
    }

    @Override
    public Runnable immediatelyTask(Integer storeId) throws LaiKeAPIException
    {
        return new Runnable()
        {
            @Override
            public void run()
            {
                RLock   lock           = null;
                boolean permitAcquired = false;
                try
                {
                    BackUpConfigModel backUpConfigModel = new BackUpConfigModel();
                    backUpConfigModel.setStore_id(storeId);
                    backUpConfigModel = backUpConfigModelMapper.selectOne(backUpConfigModel);
                    if (backUpConfigModel == null || !Objects.equals(backUpConfigModel.getIs_open(), 1))
                    {
                        return;
                    }
                    if (StringUtils.isEmpty(backUpConfigModel.getUrl()))
                    {
                        return;
                    }
                    // 分布式锁：避免多节点对同一商城重复执行
                    lock = tryLock(BACKUP_LOCK_PREFIX + storeId, 0, BACKUP_LOCK_LEASE_SEC);
                    if (lock == null)
                    {
                        return;
                    }
                    permitAcquired = backupSemaphore.tryAcquire();
                    if (!permitAcquired)
                    {
                        return;
                    }

                    Future<Integer> future = executor.submit(new Callable<Integer>()
                    {
                        @Override
                        public Integer call() throws LaiKeAPIException
                        {
                            try
                            {
                                // 调用insertRecord方法
                                MainVo vo = new MainVo();
                                vo.setStoreId(storeId);
                                return insertRecord(vo, "定时");
                            }
                            catch (LaiKeAPIException e)
                            {
                                e.printStackTrace();
                                return null; // 或者使用Optional<Integer>来避免null
                            }
                        }
                    });
                    try
                    {
                        Integer id = future.get(); // 这会阻塞直到Callable任务完成
                        if (id != null)
                        {
                            // 使用返回的ID调用exportV3DB
                            exportV3DB(backUpConfigModel.getUrl(), id);
                        }
                        else
                        {
                            logger.info("添加记录异常");
                        }
                    }
                    catch (InterruptedException | ExecutionException e)
                    {
                        e.printStackTrace();
                    }
                }
                catch (LaiKeAPIException l)
                {
                    throw l;
                }
                catch (Exception e)
                {
                    logger.error("立即备份 异常 ", e);
                    throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "immediately");
                }
                finally
                {
                    if (lock != null && lock.isHeldByCurrentThread())
                    {
                        lock.unlock();
                    }
                    if (permitAcquired)
                    {
                        backupSemaphore.release();
                    }
                }
            }
        };
    }

    @Transactional(rollbackFor = Exception.class)
    public Integer insertRecord(MainVo vo) throws LaiKeAPIException
    {
        return insertRecord(vo, "人工");
    }

    @Transactional(rollbackFor = Exception.class)
    public Integer insertRecord(MainVo vo, String fileType) throws LaiKeAPIException
    {
        Integer id = 0;
        try
        {
            BackUpRecordModel backUpRecordModel = new BackUpRecordModel();
            backUpRecordModel.setStore_id(vo.getStoreId());
            backUpRecordModel.setFile_type(fileType);
            backUpRecordModel.setStatus(BackUpRecordModel.BACKUP_RECORD_STATUS_AFOOT);
            backUpRecordModel.setAdd_time(new Date());
            backUpRecordModel.setRecycle(0);
            int i = 0;
            i = backUpRecordModelMapper.insertSelective(backUpRecordModel);
            if (i < 1)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "insertRecord");
            }
            id = backUpRecordModel.getId();

        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("添加记录 异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "insertRecord");
        }
//        executor.shutdown();
        return id;
    }

    @Autowired
    private DatabaseConfig databaseConfig;

    @Transactional(rollbackFor = Exception.class)
    public String exportV3DB(String url, Integer id)
    {
        String msg = null;
        try
        {
            BackUpRecordModel backUpRecordModel = backUpRecordModelMapper.selectByPrimaryKey(id);
            if (backUpRecordModel == null)
            {
                throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_CSYC, "备份记录不存在", "exportV3DB");
            }
            BackUpConfigModel backUpConfigModel = new BackUpConfigModel();
            backUpConfigModel.setStore_id(backUpRecordModel.getStore_id());
            backUpConfigModel = backUpConfigModelMapper.selectOne(backUpConfigModel);

            //获取数据库配置
            String         jdbcUrl        = databaseConfig.getUrl();
            // 去除查询参数部分
            int    index    = jdbcUrl.indexOf('?');
            String mainPart = index == -1 ? jdbcUrl : jdbcUrl.substring(0, index);
            // 分割URL以获取数据库名
            String[] parts = mainPart.split("//");
            if (parts.length > 2)
            {
                msg = "获取数据库信息失败，请检查数据库配置";
                logger.info("获取数据库信息失败，请检查数据库配置");
                // 如果URL格式不正确，则抛出异常或返回错误
                throw new IllegalArgumentException("Unexpected URL format");
            }
            parts = parts[parts.length - 1].split("/");
            //数据库配置
            String username = databaseConfig.getUsername();
            String password = databaseConfig.getPassword();
            String db_name  = "";
            String db_ip    = "";
            String db_port  = "";
            //文件备份的路径是根据备份设置的路径来的
            String resourcePath = url;//备份文件路径

            db_name = parts[parts.length - 1];
            // 分割URL以获取主机名和端口号（如果有的话）
            String hostPortPart = parts[0];
            if (hostPortPart.contains(":"))
            {
                String[] hostPort = hostPortPart.split(":");
                db_ip = hostPort[0];
                db_port = hostPort[1];
            }
            else
            {
                // 没有指定端口号的情况
                db_ip = hostPortPart;
                db_port = "3306";
            }
            if ("localhost".equalsIgnoreCase(db_ip))
            {
                // 避免 mysqldump 走 socket 导致 root@localhost 权限问题，强制走 TCP
                db_ip = "127.0.0.1";
            }
//          String targetFile = this.backUpDb( db_ip, db_port, username, password, db_name, resourcePath,"lkt",id);
            logger.info("======执行定时器:定时备份【" + db_name + "】数据库=======");
            String            backUpPath        = resourcePath + "/sql/" + java.sql.Date.valueOf(LocalDate.now());
            File              backUpFile        = new File(backUpPath);
            if (!backUpFile.exists())
            {
                backUpFile.mkdirs();
            }
            String targetFile;
            FileUtil.mkdir(backUpPath);
            targetFile = backUpPath + "/" + "lkt" + "_" + id + ".sql";
            String    gzTargetFile = targetFile + ".gz";
            boolean   success      = false;
            Exception lastEx       = null;
            for (int attempt = 1; attempt <= BACKUP_MAX_RETRY; attempt++)
            {
                try
                {
                    FileUtil.del(targetFile);
                    FileUtil.del(gzTargetFile);
                    FileUtil.newFile(targetFile);
                    // 使用ProcessBuilder避免命令注入
                    java.util.List<String> cmdArgs = new java.util.ArrayList<>();
                    cmdArgs.add("mysqldump");
                    cmdArgs.add("--protocol=TCP");
                    cmdArgs.add("-h" + db_ip);
                    cmdArgs.add("--port=" + db_port);
                    cmdArgs.add("-u" + username);
                    cmdArgs.add("--password=" + password);
                    cmdArgs.add("--single-transaction");
                    cmdArgs.add("--skip-lock-tables");
                    cmdArgs.add("--routines");
                    cmdArgs.add("--events");
                    cmdArgs.add("--triggers");
                    cmdArgs.add(db_name);
                    cmdArgs.add("-q");
                    cmdArgs.add("-r");
                    cmdArgs.add(targetFile);
                    logger.info("======数据库备份cmd命令为：" + String.join(" ", cmdArgs) + "=======");
                    ProcessBuilder processBuilder = new ProcessBuilder(cmdArgs);
                    Process process = processBuilder.start();
                    process.waitFor();
                    if (process.exitValue() != 0)
                    {
                        // 获取错误信息
                        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                        String         line;
                        while ((line = errorReader.readLine()) != null)
                        {
                            logger.info("错误信息：" + line);
                        }
                        errorReader.close();
                        throw new RuntimeException("mysqldump执行失败");
                    }
                    // GZIP压缩（减少空间占用）
                    gzipFile(new File(targetFile), new File(gzTargetFile));
                    FileUtil.del(targetFile);

                    File gzFile = new File(gzTargetFile);
                    backUpRecordModel.setFile_name(gzFile.getName());
                    backUpRecordModel.setFile_url(gzTargetFile);
                    backUpRecordModel.setFile_size(getFileSize(gzFile));
                    backUpRecordModel.setStatus(BackUpRecordModel.BACKUP_RECORD_STATUS_COMPLETED);
                    logger.info("======备份数据库【" + db_name + "】成功======");
                    success = true;
                    break;
                }
                catch (Exception e)
                {
                    lastEx = e;
                    logger.info("======数据库备份失败(第{}次)，异常为：{}=======", attempt, e.getMessage());
                    LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(RETRY_BACKOFF_MS * attempt));
                }
            }
            if (!success)
            {
                msg = "数据库备份失败，已重试" + BACKUP_MAX_RETRY + "次";
                backUpRecordModel.setStatus(BackUpRecordModel.BACKUP_RECORD_STATUS_FAIL);
                if (lastEx != null)
                {
                    logger.info("======数据库备份失败，异常为：{}=======", lastEx.getMessage());
                }
            }
            backUpRecordModelMapper.updateByPrimaryKeySelective(backUpRecordModel);
            if (success)
            {
                cleanupHistory(backUpConfigModel, backUpRecordModel.getStore_id());
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("立即备份 异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "exportV3DB");
        }
        return msg;
    }


    public void importDbFile(String db_ip, String db_port, String username, String password, String db_name, String backupFilePath) throws Exception
    {
        if (StringUtils.isEmpty(backupFilePath))
        {
            throw new FileNotFoundException("备份文件路径为空");
        }
        File backupFile = new File(backupFilePath);
        if (!backupFile.isFile())
        {
            throw new FileNotFoundException("备份文件不存在: " + backupFilePath);
        }
        //直接执行SQL文件，出错时抛异常给上层处理
        String url = "jdbc:mysql://" + db_ip + ":" + db_port + "/" + db_name;
        try (Connection conn = DriverManager.getConnection(url, username, password);
             InputStream inputStream = backupFilePath.endsWith(".gz")
                     ? new GZIPInputStream(new FileInputStream(backupFile))
                     : new FileInputStream(backupFile);
             Reader reader = new BufferedReader(new InputStreamReader(inputStream, java.nio.charset.StandardCharsets.UTF_8)))
        {
            ScriptRunner runner = new ScriptRunner(conn);
            runner.setStopOnError(true);
            runner.runScript(reader);
        }
    }

    private void cleanupHistory(BackUpConfigModel backUpConfigModel, Integer storeId)
    {
        try
        {
            int retentionDays = DEFAULT_RETENTION_DAYS;
            if (backUpConfigModel != null && backUpConfigModel.getQuery_data() != null && backUpConfigModel.getQuery_data() > 0)
            {
                retentionDays = backUpConfigModel.getQuery_data();
            }
            LocalDate         cutoffDate = LocalDate.now().minusDays(retentionDays);
            Date              cutoff     = java.sql.Date.valueOf(cutoffDate);
            BackUpRecordModel query      = new BackUpRecordModel();
            query.setStore_id(storeId);
            query.setRecycle(0);
            List<BackUpRecordModel> list = backUpRecordModelMapper.select(query);
            for (BackUpRecordModel record : list)
            {
                if (record.getAdd_time() == null || !record.getAdd_time().before(cutoff))
                {
                    continue;
                }
                String fileUrl = record.getFile_url();
                if (StringUtils.isNotEmpty(fileUrl))
                {
                    FileUtil.del(fileUrl);
                }
                record.setRecycle(1);
                backUpRecordModelMapper.updateByPrimaryKeySelective(record);
            }
        }
        catch (Exception e)
        {
            logger.info("历史备份清理异常: {}", e.getMessage());
        }
    }

    private void gzipFile(File source, File target) throws IOException
    {
        if (source == null || !source.isFile())
        {
            throw new FileNotFoundException("源文件不存在: " + (source == null ? "null" : source.getAbsolutePath()));
        }
        try (InputStream in = new FileInputStream(source);
             GZIPOutputStream out = new GZIPOutputStream(new FileOutputStream(target)))
        {
            byte[] buffer = new byte[8192];
            int    len;
            while ((len = in.read(buffer)) != -1)
            {
                out.write(buffer, 0, len);
            }
        }
    }

    private String getFileSize(File file)
    {
        if (file == null || !file.exists())
        {
            return "";
        }
        long fileSize = file.length();
        return String.format("%.2f MB", fileSize / (1024.0 * 1024.0));
    }

    private RLock tryLock(String key, long waitSeconds, long leaseSeconds)
    {
        try
        {
            RLock lock = redissonClient.getLock(key);
            if (!lock.tryLock(waitSeconds, leaseSeconds, TimeUnit.SECONDS))
            {
                return null;
            }
            return lock;
        }
        catch (Exception e)
        {
            logger.info("获取分布式锁失败: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public String getNewCronByStoreId(Integer storeId) throws LaiKeAPIException
    {
        String cron = "";
        try
        {
            BackUpConfigModel backUpConfigModel = new BackUpConfigModel();
            backUpConfigModel.setStore_id(storeId);
            backUpConfigModel = backUpConfigModelMapper.selectOne(backUpConfigModel);
            if (backUpConfigModel != null && backUpConfigModel.getIs_open() == 1)
            {
                cron = backUpConfigModel.getExecute_cycle();
            }
        }
        catch (LaiKeAPIException l)
        {
            throw l;
        }
        catch (Exception e)
        {
            logger.error("查询备份配置 异常 ", e);
            throw new LaiKeAPIException(ErrorCode.BizErrorCode.ERROR_CODE_WLYC, "网络异常", "queryConfig");
        }
        return cron;
    }

    @Override
    public void taskStoreAll() throws LaiKeAPIException
    {
        List<ConfigModel> configModel   = null;
        CustomerModel     customerModel = null;
        try
        {
            logger.info("缓存商城id 开始执行!");
            if (redisUtil.hasKey(GloabConst.RedisHeaderKey.LKT_STOREID_EFFECTIVE_LIST))
            {
                logger.info("商城id已存在缓存 获取缓存 下次刷新商城id剩余:{}!", redisUtil.getExpire(GloabConst.RedisHeaderKey.LKT_STOREID_EFFECTIVE_LIST));
            }
            else
            {
                logger.info("商城id已存在缓存 开始初始化缓存!");
                //获取所有商城
                configModel = configModelMapper.getConfigAllInfo();
                Set<Integer> storeIdList = new HashSet<>();
                for (ConfigModel config : configModel)
                {
                    //只有正常的商城才走流程
                    customerModel = new CustomerModel();
                    customerModel.setId(config.getStore_id());
                    customerModel.setRecycle(DictionaryConst.ProductRecycle.NOT_STATUS);
                    customerModel.setStatus(0);
                    customerModel.setStatus(CustomerModel.CUSTOMER_STATUS_OPEN);
                    customerModel = customerModelMapper.selectOne(customerModel);
                    if (customerModel != null)
                    {
                        storeIdList.add(customerModel.getId());
                    }
                    else
                    {
                        logger.info("商城被锁定或者已到期 商城id:{}", config.getId());
                    }
                }
                //缓存storeId集
                logger.info(GloabConst.RedisHeaderKey.LKT_STOREID_EFFECTIVE_LIST, JSON.toJSONString(storeIdList), GloabConst.LktConfig.LOGIN_STORE_ID_EXISTENCE_TIME);
            }

            logger.info("缓存商城id 执行完毕!");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.info("商城定时任务 异常: " + e.getMessage());
        }
        finally
        {
            configModel = null;
            customerModel = null;
        }
    }
}
