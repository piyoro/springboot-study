package com.example.springbootstudy.common;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author : piyor
 * @date : 2020. 2. 6.
 * @desc :
 *
 * <pre>
 * 마이바이트 쿼리를 위한 공통 dao
 * </pre>
 */
@Repository
@Slf4j
public class CommonDao {

    @Autowired
    private SqlSessionTemplate sqlSession;

    protected void printQueryId(String queryId) {
        log.debug("\t QueryId  \t:  " + queryId);
    }

    public int insert(String queryId, Object params) {
        printQueryId(queryId);
        return sqlSession.insert(queryId, params);
    }

    public int update(String queryId, Object params) {
        printQueryId(queryId);
        return sqlSession.update(queryId, params);
    }

    public int delete(String queryId, Object params) {
        printQueryId(queryId);
        return sqlSession.delete(queryId, params);
    }

    public <T> T selectOne(String queryId) {
        printQueryId(queryId);
        return sqlSession.selectOne(queryId);
    }

    public <T> T selectOne(String queryId, Object params) {
        printQueryId(queryId);
        return sqlSession.selectOne(queryId, params);
    }

    public List<?> selectList(String queryId) {
        printQueryId(queryId);
        return sqlSession.selectList(queryId);
    }

    @SuppressWarnings("rawtypes")
    public List selectList(String queryId, Object params) {
        printQueryId(queryId);
        return sqlSession.selectList(queryId, params);
    }
}