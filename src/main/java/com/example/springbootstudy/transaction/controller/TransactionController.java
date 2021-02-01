package com.example.springbootstudy.transaction.controller;

import com.example.springbootstudy.common.TestException;
import com.example.springbootstudy.transaction.serivce.TransactionService;
import com.example.springbootstudy.transaction.vo.TestVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@Slf4j
public class TransactionController {

    @Autowired
    TransactionService transactionService;

    /**
     * Thread 10,000건, Mysql auto_increase 로
     * 키 중복없이 인서트 정상처리 확인
     * @param req
     * @param res
     * @return
     */
    @GetMapping("/stress")
    public ResponseEntity<?> stress(HttpServletRequest req, HttpServletResponse res) {
        transactionService.insertTest();
        return ResponseEntity.ok("hello!!!");
    }

    /**
     * 선언적 트랜잭션 @Transactionl 기능 확인
     * @param req
     * @param res
     * @return
     */
    @GetMapping("/transaction")
    public ResponseEntity<?> transaction(HttpServletRequest req, HttpServletResponse res) {
        TestVO vo = null;
        String key = null;
        try {
            vo = transactionService.transaction();
        } catch (TestException e) {
            //service 의 exceptio을 controller 에서 catch 해도 rollback 은 된다.
            //로그에 찍힌 키값으로 db tool에서 조회하면 조회되지 않는다.
            key = e.getCode();
            log.debug("first service rollback key[{}] vo [{}]", new Object[]{key, vo});
            if (vo == null) vo = new TestVO();
            vo.setSeqNo(key);
        }
        transactionService.transaction_rollback_after(vo);
        return ResponseEntity.ok("transaction");
    }

    /**
     * Dirty Read 확인용 서비스 호출
     * tb_seq 테이블에 데이터 인서트
     * sleep 5초
     * max 키 값 반환
     * @param req
     * @param res
     * @return
     */
    @GetMapping("/dirtyRead1")
    public ResponseEntity<?> dirtyRead1(HttpServletRequest req, HttpServletResponse res) {
        int rtn = transactionService.dirtyRead1();
        return ResponseEntity.ok(rtn);
    }

    /**
     * Dirty Read 확인용 서비스 호출
     * tb_seq 테이블 max 키 값 반환
     * @param req
     * @param res
     * @return
     */
    @GetMapping("/dirtyRead2")
    public ResponseEntity<?> dirtyRead2(HttpServletRequest req, HttpServletResponse res) {
        int rtn = transactionService.dirtyRead2();
        return ResponseEntity.ok(rtn);
    }

    /**
     * Dirty Read uncommit 확인용 서비스 호출
     * @@dirty read 발생
     * @param req
     * @param res
     * @return
     */
    @GetMapping("/dirtyRead_happen")
    public ResponseEntity<?> dirtyRead_happen(HttpServletRequest req, HttpServletResponse res) {
        int rtn = transactionService.dirtyRead_happen();
        return ResponseEntity.ok(rtn);
    }
    
    /**
     * Dirty Read 확인용 서비스 호출
     * @@dirty read 방지
     * @param req
     * @param res
     * @return
     */
    @GetMapping("/dirtyRead_prevent")
    public ResponseEntity<?> dirtyRead_prevent(HttpServletRequest req, HttpServletResponse res) {
        int rtn = transactionService.dirtyRead_prevent();
        return ResponseEntity.ok(rtn);
    }

    /**
     * Non-Repeatable Read 발생 확인용 서비스 호출
     * @@Non-Repeatable Read 발생
     * @param req
     * @param res
     * @return
     */
    @GetMapping("/nonrepeatableread_happen")
    public ResponseEntity<?> nonRepeatableRead_happen(HttpServletRequest req, HttpServletResponse res) {
        String rtn = transactionService.selectTestSeq_repeatableread_happen();
        return ResponseEntity.ok(rtn);
    }
    /**
     * Non-Repeatable Read 방지 서비스 호출
     * @@Non-Repeatable Read 발생
     * @param req
     * @param res
     * @return
     */
    @GetMapping("/nonrepeatableread")
    public ResponseEntity<?> nonRepeatableRead(HttpServletRequest req, HttpServletResponse res) {
        String rtn = transactionService.selectTestSeq_repeatableread();
        return ResponseEntity.ok(rtn);
    }
}
