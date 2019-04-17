package com.shzlw.poli.service;

import com.shzlw.poli.dao.JdbcDataSourceDao;
import com.shzlw.poli.model.JdbcDataSource;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class JdbcDataSourceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcDataSourceService.class);

    private static final Map<Long, HikariDataSource> DATA_SOURCE_CACHE = new ConcurrentHashMap<>();

    @Autowired
    JdbcDataSourceDao jdbcDataSourceDao;

    @PostConstruct
    public void init() {
    }

    @PreDestroy
    public void shutdown() {
        for (HikariDataSource hiDs : DATA_SOURCE_CACHE.values()) {
            hiDs.close();
        }
    }

    public DataSource put(JdbcDataSource dataSource) {
        LOGGER.info("HikariDataSource - put: {}", dataSource);
        HikariDataSource hiDs = DATA_SOURCE_CACHE.get(dataSource.getId());
        if (hiDs != null) {
            hiDs.close();
        }
        HikariDataSource newHiDs = new HikariDataSource();
        newHiDs.setJdbcUrl(dataSource.getConnectionUrl());
        newHiDs.setUsername(dataSource.getUsername());
        newHiDs.setPassword(dataSource.getPassword());
        if (!StringUtils.isEmpty(dataSource.getDriverClassName())) {
            newHiDs.setDriverClassName(dataSource.getDriverClassName());
        }
        newHiDs.setMaximumPoolSize(10);
        DATA_SOURCE_CACHE.put(dataSource.getId(), newHiDs);
        return newHiDs;
    }

    public void remove(JdbcDataSource dataSource) {
        DATA_SOURCE_CACHE.remove(dataSource.getId());
    }

    public DataSource getDataSource(JdbcDataSource dataSource) {
        long id = dataSource.getId();
        DataSource hiDs = DATA_SOURCE_CACHE.get(id);
        if (hiDs == null) {
            JdbcDataSource ds = jdbcDataSourceDao.findById(id);
            if (ds != null) {
                hiDs = put(dataSource);
            }
        }
        return hiDs;
    }
}
