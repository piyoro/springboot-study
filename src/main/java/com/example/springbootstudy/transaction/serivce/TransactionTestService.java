package com.example.springbootstudy.transaction.serivce;

import com.example.springbootstudy.common.CommonDao;
import com.example.springbootstudy.transaction.vo.TestVO;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class TransactionTestService {

    @Autowired
    CommonDao commonDao;


    /**
     * Propagation.REQUIRES_NEW 로 새로운 트랜잭션의
     * dirty read 를 확인하려 했지만.
     * 서비스가 절차적으로 실행되면서 무조건 commit 된 데이터를
     * 조회하기 때문에 무용지물
     * @return 
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int dirtyRead_new_trx() {
        //기본 인서트
        commonDao.insert("TEST.insertTest", new TestVO());
        //새로운 트랜잭션 조회
        int max = commonDao.selectOne("TEST.selectMaxSeqNo");
        log.debug("max값 [{}] 새로운 트랜잭션", new Object[]{max});
        return max;
    }

    @SneakyThrows
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void dirtyRead_new_trx_sleep() {
        //기본 인서트
        commonDao.insert("TEST.insertTest", new TestVO());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //새로운 트랜잭션 조회
        int max = commonDao.selectOne("TEST.selectMaxSeqNo");
        log.debug("max값 [{}] 새로운 트랜잭션", new Object[]{max});
    }

    /**
     * 서비스에서 this를 이용해 자신의 메서드를 호출할 때
     * 해당 메서드의 @Transactional 기능이 수행되지 않기 때문에
     * 스프링 proxy 사용을 위한 메서드
     * 항상 새로운 트랜잭션을 생성하여 처리. Propagation.REQUIRES_NEW
     * @param vo
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void selectTestSeq_new_transaction(TestVO vo) {
        vo.setDummy("test_dummy");
        commonDao.update("TEST.updateTestDummy", vo);
        //새로운 트랜잭션 조회
        TestVO rtnVo = commonDao.selectOne("TEST.selectTestSeq", vo);
        log.debug("dummy값 [{}] 새로운 트랜잭션", new Object[]{rtnVo.getDummy()});
    }

}
