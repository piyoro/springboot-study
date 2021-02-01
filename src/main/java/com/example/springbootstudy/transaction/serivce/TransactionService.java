package com.example.springbootstudy.transaction.serivce;

import com.example.springbootstudy.common.CommonDao;
import com.example.springbootstudy.common.TestException;
import com.example.springbootstudy.transaction.vo.TestVO;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.IntStream;

/**
 * 
 */
@Service
@Slf4j
public class TransactionService {

    @Autowired
    CommonDao commonDao;

    //타서비스 메서드의 @Transaction 기능 수행 확인을 위한 서비스
    @Autowired
    TransactionTestService transactionTestService;

    //this 자신을 스프링 proxy 화 하기 위한 현재 서비스
    @Autowired
    TransactionService self;

    //트랜잭션 확인 조회시 사용할 키값 상수
    public static final String SEQ_NO = "26519";

    /**
     * mysql auto_increase 정합성 확인
     * Thread 10,000개로 insert 할때 키 중복 오류없이 정상 처리
     * @return
     */
    public int insertTest() {
        IntStream.range(0, 100).forEach(i -> {
            final int f_i = i;
            IntStream.range(0, 100).forEach(j -> {
                final int f_j = j;
                Thread t = new Thread(() -> {
                    TestVO vo = new TestVO();
                    vo.setDummy(String.valueOf((f_i * 100) + f_j));
                    commonDao.insert("TEST.insertTest", vo);
                });
                t.setDaemon(true);
                t.start();
            });
        });
        return 0;
    }

    /**
     * 인서트 후, 일부러 RuntimeException 을 발생시키고
     * transaction() 메서드 내에선 rollback 안되는 것 확인
     * caller 인 controller 에선 rollack 된것을 확인
     * @return
     */
    @Transactional
    public TestVO transaction() {
        TestVO vo = new TestVO();
        vo.setDummy("1234");
        commonDao.insert("TEST.insertTest", vo);
        //transaction 확인을 위해 일부러 예외 발생.
        if(true) {
            String key = vo.getSeqNo();
            log.debug("insert key [{}]", key);
            //rollback 전이기 때문에 select 된다.
            TestVO rtnVO = commonDao.selectOne("TEST.selectTestSeq", vo);
            log.debug("rollback before vo [{}]", rtnVO);
            throw TestException.builder().code(key).build();
        }
        return vo;
    }

    /**
     * rollback 확인
     * 혹시나 커밋되지 않아 조회 안되는건 아닐까 싶어
     * Isolation.READ_UNCOMMITTED 적용 -> 마찮가지로 조회안됨 
     * @param vo 
     * @return
     */
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public int transaction_rollback_after(TestVO vo) {
        TestVO rtnVO = commonDao.selectOne("TEST.selectTestSeq", vo);
        log.debug("rollback after vo  [{}]", rtnVO);
        return 0;
    }

    /**
     * dirty read 확인을 위해 데이터 인서트후 5초 대기
     * @return
     */
    @SneakyThrows
    @Transactional
    public int dirtyRead1() {
        TestVO vo = new TestVO();
        commonDao.insert("TEST.insertTest", vo);
        Thread.sleep(5_000);
        int max = commonDao.selectOne("TEST.selectMaxSeqNo");
        return max;
    }

    /**
     * /dirtyRead1 실행후, sleep 5초 동안 인서트된 데이터를 조회할 수 있는지 확인
     * Isolation.READ_COMMITTED - dirty read 발생하지 않음.
     * Isolation.READ_UNCOMMITTED - /dirtyRead1 에서 커밋되지 않아 dirty read 발생
     * @return
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public int dirtyRead2() {
        int max = commonDao.selectOne("TEST.selectMaxSeqNo");
        return max;
    }

    /**
     * 서비스에서 트랜잭션 시작 Isolation.READ_UNCOMMITTED
     * 1. 원본 조회
     * -> max값 [SEQ_NO] 원본
     * 2. transactionTestService.dirtyRead_new_trx_sleep -> Propagation.REQUIRES_NEW
     * - 새로운 트랜잭션 - 새로운 값 인서트
     * - 새로운 트랜잭션 조회
     * -> max값 [SEQ_NO] 새로운 트랜잭션
     * 3. 기존 트랜잭션으로 재조회
     * -> 기존 트랜잭션 max값 [SEQ_NO] 기존 트랜잭션 재조회
     * @return
     */
    @SneakyThrows
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public int dirtyRead_happen() {
        int max = commonDao.selectOne("TEST.selectMaxSeqNo");;
        log.debug("max값 [{}] 원본", new Object[]{max});

        Thread t = new Thread(() -> {
            //-> 새로운 트랜잭션
            transactionTestService.dirtyRead_new_trx_sleep();
        });
        t.start();
        Thread.sleep(100);


        max = commonDao.selectOne("TEST.selectMaxSeqNo");;
        log.debug("max값 [{}] 기존 트랜잭션 재조회", new Object[]{max});
        return max;
    }


    /**
     * 서비스에서 트랜잭션 시작 Isolation.READ_COMMITTED
     * 1. 원본 조회
     * -> max값 [SEQ_NO] 원본
     * 2. transactionTestService.dirtyRead_new_trx_sleep -> Propagation.REQUIRES_NEW
     * - 새로운 트랜잭션 - 새로운 값 인서트
     * - 새로운 트랜잭션 조회
     * -> max값 [SEQ_NO] 새로운 트랜잭션
     * 3. 기존 트랜잭션으로 재조회
     * -> 기존 트랜잭션 max값 [SEQ_NO] 기존 트랜잭션 재조회
     * Isolation.READ_COMMITTED (or 그 이상) 적용
     * @return
     */
    @SneakyThrows
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public int dirtyRead_prevent() {
        int max = commonDao.selectOne("TEST.selectMaxSeqNo");;
        log.debug("max값 [{}] 원본", new Object[]{max});

        Thread t = new Thread(() -> {
            //-> 새로운 트랜잭션
            transactionTestService.dirtyRead_new_trx_sleep();
        });
        t.start();
        Thread.sleep(100);

        max = commonDao.selectOne("TEST.selectMaxSeqNo");;
        log.debug("max값 [{}] 기존 트랜잭션 재조회", new Object[]{max});
        return max;
    }


    /**
     * Non-Repeatable Read 발생 확인
     * 1. 원본 조회
     * 2. commonDao.updateTestDummy => Propagation.REQUIRES_NEW 새로운 트랜잭션 생성 => dummy 값 변경
     * 3. 2번 트랜잭션에서 변경된값 확인
     * 4. 기존 트랜잭션으로 재조회
     * -> dummy값 [test] 원본
     * -> 새로운 트랜잭션 변경
     * -> dummy값 [test_dummy] 새로운 트랜잭션
     * -> dummy값 [test_dummy] 기존 트랜잭션 재조회
     * 기존 트랜잭션에서 재조회한 최종 dummy 값이 test_dummy로 변경됨
     * Isolation.Isolation.REPEATABLE_READ 이상에서는 발생하지 않음. Isolation.READ_COMMITTED 적용
     * @return
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public String selectTestSeq_repeatableread_happen() {

        TestVO vo = new TestVO();
        vo.setSeqNo("SEQ_NO");
        TestVO rtnVO = commonDao.selectOne("TEST.selectTestSeq", vo);;
        log.debug("dummy값 [{}] 원본", new Object[]{rtnVO.getDummy()});
        //새로운 트랜잭션 업데이트
        transactionTestService.selectTestSeq_new_transaction(vo);

        rtnVO = commonDao.selectOne("TEST.selectTestSeq", vo);;
        log.debug("dummy값 [{}] 기존 트랜잭션 재조회", new Object[]{rtnVO.getDummy()});
        //테스트를 위해 기존값 원복
        vo.setDummy("test");
        commonDao.update("TEST.updateTestDummy", vo);
        return rtnVO.getDummy();
    }


    /**
     * Non-Repeatable Read 발생 확인
     * 1. 원본 조회
     * 2. commonDao.updateTestDummy => Propagation.REQUIRES_NEW 새로운 트랜잭션 생성 => dummy 값 변경
     * 3. 2번 트랜잭션에서 변경된값 확인
     * 4. 기존 트랜잭션으로 재조회
     * -> dummy값 [test] 원본
     * -> 새로운 트랜잭션 변경
     * -> dummy값 [test_dummy] 새로운 트랜잭션
     * -> dummy값 [test] 기존 트랜잭션 재조회
     * 기존 트랜잭션에서 재조회한 최종 dummy 변경되지 않음. [일관성 유지]
     * Mysql default Isolation.REPEATABLE_READ 이상에서 방지됨
     * @return
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public String selectTestSeq_repeatableread() {

        TestVO vo = new TestVO();
        vo.setSeqNo("SEQ_NO");
        TestVO rtnVO = commonDao.selectOne("TEST.selectTestSeq", vo);;
        log.debug("dummy값 [{}] 원본", new Object[]{rtnVO.getDummy()});

        //새로운 트랜잭션 업데이트
        self.selectTestSeq_new_transaction(vo);

        rtnVO = commonDao.selectOne("TEST.selectTestSeq", vo);;
        log.debug("dummy값 [{}] 기존 트랜잭션 재조회", new Object[]{rtnVO.getDummy()});
        //테스트를 위해 기존값 원복
        vo.setDummy("test");
        commonDao.update("TEST.updateTestDummy", vo);
        return rtnVO.getDummy();
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void selectTestSeq_new_transaction(TestVO vo) {
        vo.setDummy("test_dummy");
        commonDao.update("TEST.updateTestDummy", vo);
        //새로운 트랜잭션 조회
        String dummy = commonDao.selectOne("TEST.selectTestSeq", vo);;
        log.debug("dummy값 [{}] 새로운 트랜잭션", new Object[]{dummy});
    }
}
