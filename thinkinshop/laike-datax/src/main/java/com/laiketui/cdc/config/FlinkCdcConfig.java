package com.laiketui.cdc.config;

import com.laiketui.cdc.deserializer.MysqlDeserializer;
import com.laiketui.cdc.sinks.DataChangeSink;
import com.laiketui.domain.datax.DataChangeInfo;
import com.ververica.cdc.connectors.mysql.source.MySqlSource;
import com.ververica.cdc.connectors.mysql.table.StartupOptions;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.RestOptions;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.Properties;

@org.springframework.context.annotation.Configuration
@RefreshScope
public class FlinkCdcConfig {

    @Value("${lkt-flink-cdc.db-host}")
    private String  db_host = "localhost";

    @Value("${lkt-flink-cdc.db-port}")
    private int db_port = 3306 ;

    /**
     * 可以配置多个数据库名 逗号隔开
     */
    @Value("${lkt-flink-cdc.dbs}")
    private String  dbs ;

    /**
     * 可以配置多个表必须要带schema 多个表用逗号隔开
     */
    @Value("${lkt-flink-cdc.tables}")
    private String  tables ;

    /**
     * 数据库密码
     */
    @Value("${lkt-flink-cdc.password}")
    private String  password ;

    /**
     * 数据库用户名
     */
    @Value("${lkt-flink-cdc.username}")
    private String username ;

    /**
     * 并行数:
     */
    @Value("${lkt-flink-cdc.parallelism}")
    private int parallelism ;

    /**
     * checkpoint：
     */
    @Value("${lkt-flink-cdc.checkpointing}")
    private int checkpointing ;


    /**
     * 初始化 flink - StreamExecutionEnvironment
     * @param context
     * @param rocketMQTemplate
     * @return
     */
    @Bean
    public StreamExecutionEnvironment getFlinkCDC(ApplicationContext context, RocketMQTemplate rocketMQTemplate){
        Properties jdbcProperties = new Properties() ;
        jdbcProperties.setProperty("useSSL", "false") ;
        MySqlSource<DataChangeInfo> source = MySqlSource.<DataChangeInfo>builder()
                .hostname(db_host)
                .port(db_port)
                .username(username)
                .password(password)
                // 可配置多个数据库
                .databaseList(dbs)
                // 可配置多个表
                .tableList(tables)
//                .tableList("v3_db.lkt_user,v3_db.lkt_order,v3_db.lkt_order_details")
                .jdbcProperties(jdbcProperties)
                // 反序列化设置
                .deserializer(new MysqlDeserializer())
                // 启动模式
                .startupOptions(StartupOptions.initial())
                .build() ;
        // 环境配置
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment() ;
        // 设置 6s 的 checkpoint 间隔
        env.enableCheckpointing(checkpointing) ;
        // 设置 source 节点的并行度为 4
        env.setParallelism(parallelism) ;
        DataChangeSink sink = new DataChangeSink(rocketMQTemplate);
        env.fromSource(source, WatermarkStrategy.noWatermarks(), "MySQL").addSink(sink) ;
        return env;
    }

    /**
     * 异步运行 flink - StreamExecutionEnvironment
     * @param env
     * @return
     */
    @Bean
    public CommandLineRunner flinkJobStarter(StreamExecutionEnvironment env) {
        return new CommandLineRunner() {
            @Override
            public void run(String... args) throws Exception {
                env.executeAsync();
            }
        };
    }
}
