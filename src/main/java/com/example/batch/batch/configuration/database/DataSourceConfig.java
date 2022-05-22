//package com.example.batch.batch.configuration.database;
//
//
//import org.apache.commons.dbcp2.BasicDataSource;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Primary;
//import org.springframework.transaction.annotation.EnableTransactionManagement;
//
//import javax.sql.DataSource;
//
//
//
//@EnableTransactionManagement(proxyTargetClass = true)
//@Configuration
//public class DataSourceConfig {
//
//
////    @Value("${external.jdbc.driver.class.name}")
//public String externalDriverClassName = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
////    @Value("${external.jdbc.url}")
//public String externalJdbcUrl ="jdbc:sqlserver://localhost:1433;databaseName=account";
////    @Value("${external.jdbc.username}")
//public String externalJdbcUsername = "sa";
////    @Value("${external.jdbc.password}")
//public String externalJdbcPassword = "1234";
//
//
//    @Primary
//    @Bean
//    public DataSource externalDataSource() {
//        return getDataSource(externalDriverClassName, externalJdbcUrl, externalJdbcUsername, externalJdbcPassword);
//    }
//
//
//
////    @Primary
////    @Bean
////    public DataSource defaultDataSource() {
////        return getDataSource(defaultDriverClassName, defaultJdbcUrl, defaultJdbcUsername, defaultJdbcPassword);
////    }
//
//
//
//
//    private DataSource getDataSource(String externalDriverClassName, String externalJdbcUrl, String externalJdbcUsername, String externalJdbcPassword) {
//        BasicDataSource dataSource = new BasicDataSource();
//        dataSource.setDriverClassName(externalDriverClassName);
//        dataSource.setUrl(externalJdbcUrl);
//        dataSource.setUsername(externalJdbcUsername);
//        dataSource.setPassword(externalJdbcPassword);
//
//        return dataSource;
//    }
//
//
//}
