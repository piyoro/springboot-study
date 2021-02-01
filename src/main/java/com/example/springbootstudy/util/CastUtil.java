package com.example.springbootstudy.util;

import java.util.Map;

public class CastUtil {

    /**
     * 파라미터가 Object 를 담는 Map 이기 때문에 key를 꺼낸 Object 를
     * 해당 클래스에 맞춰 반환했을 때 컴파일시에는 형변환 체크를 할 수 없고
     * 런타임시 형변환에 안전하지는 않음.
     * 명시적인 형변환 소스를 줄여줄 목적으로 사용
     * @param map
     * @param key
     * @param <T>
     * @param <R>
     * @return
     */
    public static <T, R> R cast(Map<T, Object> map, T key) {
        R r = (R) cast(map, key, null);
        return r;
    }

    @SuppressWarnings("unchecked")
    public static <T, R> R cast(Map<T, Object> map, T key, Class<R> clz) {
        Object obj = map.get(key);
        if(obj == null) return null;
        if(clz == null) clz = (Class<R>) map.getClass();
        return clz.cast(obj);
    }

}
