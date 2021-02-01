package com.example.springbootstudy.test.service;

import com.example.springbootstudy.common.CommonDao;
import com.example.springbootstudy.test.mapper.MapperTestMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TestService {

    @Autowired
    CommonDao commonDao;

    @Autowired
    MapperTestMapper mapperTestMapper;

    @Value("${app.dbms}")
    String dbms;

    @Value("${app.config}")
    String appConfig;

    public int selectTestCnt() {
        int cnt = commonDao.selectOne("TEST.selectTestCnt");
        int mapper_cnt = mapperTestMapper.selectTestCnt();
        log.debug("cnt commonDao [{}] Mapper [{}]", new Object[]{cnt, mapper_cnt});
        log.debug("appDbms [{}] appConfig [{}]", new Object[]{dbms, appConfig});
        return cnt;
    }
}
