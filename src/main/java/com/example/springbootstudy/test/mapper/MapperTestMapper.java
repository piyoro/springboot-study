package com.example.springbootstudy.test.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

/**
 * Mybatis @Mapper 인터페이스 를 이용해 매퍼 설정을 확인하기 위해 만들었지만,
 * 난 이 방식을 선호하지 않아서 앞으론 사용하지 않는다.
 */
@Mapper
@Component
@Deprecated
public interface MapperTestMapper {

    int selectTestCnt();

}
