package com.example.springbootstudy.common;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TestException extends RuntimeException {
    private String code;
    private String mes;
}
