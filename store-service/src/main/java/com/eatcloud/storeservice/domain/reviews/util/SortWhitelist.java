package com.eatcloud.storeservice.domain.reviews.util;

import org.springframework.data.domain.*;
import java.util.Set;

public final class SortWhitelist {
    /**
 * 인스턴스화를 방지하기 위한 private 생성자.
 *
 * 유틸리티 클래스이므로 외부에서 객체를 생성할 수 없도록 설정되어 있다.
 */
private SortWhitelist() {}
    /**
     * Pageable의 정렬 속성 화이트리스트를 적용한다.
     *
     * <p>pageable에 정렬이 없으면 원본을 그대로 반환한다. 정렬이 있으면 첫 번째 {@link Sort.Order}의
     * property를 허용된 집합(allowed)과 비교한다. 허용되지 않은 속성일 경우 같은 페이지 번호와 크기를
     * 유지하되 `createdAt`에 대해 내림차순으로 정렬되는 새로운 {@link PageRequest}를 반환한다.
     * 허용된 속성인 경우 원본 pageable을 그대로 반환한다.
     *
     * <p>참고: 본 메서드는 전달된 pageable의 첫 번째 정렬 기준만 검사하며, pageable 또는 allowed가
     * null이거나 예상치 못한 정렬 형태일 경우 런타임 예외(NPE 등)가 발생할 수 있다.
     *
     * @param pageable 검사할 Pageable (null이 아님을 가정)
     * @param allowed  허용된 정렬 속성 이름들의 집합
     * @return 허용되지 않은 정렬 속성이 사용된 경우 createdAt 내림차순으로 대체된 Pageable, 그렇지 않으면 원본 pageable
     */
    public static Pageable enforce(Pageable pageable, Set<String> allowed) {
        if (pageable.getSort().isEmpty()) return pageable;
        Sort.Order o = pageable.getSort().stream().findFirst().get();
        if (!allowed.contains(o.getProperty())) {
            return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                    Sort.by(Sort.Order.desc("createdAt")));
        }
        return pageable;
    }
}
