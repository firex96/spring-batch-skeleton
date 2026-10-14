package com.rex.batch.skeleton.controller.admin

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

/**
 * Admin 페이지 컨트롤러
 *
 * Thymeleaf 페이지 라우팅을 담당합니다.
 * API Key 인증 없이 접근 가능합니다 — 막으면 키를 입력할 화면 자체에 도달할 수 없다.
 *
 * ## 이 대시보드는 샘플이다 — 인증은 파생 프로젝트가 붙인다
 *
 * HTML 셸은 열려 있고 데이터는 `AdminApiController` 가 API Key 로 지킨다. 그래서 키가
 * 브라우저(`sessionStorage`)에 있고, 그 화면에 XSS 가 생기면 키가 샌다.
 *
 * **스켈레톤은 여기에 운영급 인증을 넣지 않는다.** 관리자 콘솔의 인증 요건은 프로젝트마다
 * 다르고, 스켈레톤이 고른 방식은 **모든 파생으로 복제**되기 때문이다.
 *
 * 이 대시보드를 운영에 쓰는 프로젝트는 **자기 인증 뒤에 둔다** — 사내망 한정 노출,
 * 리버스 프록시 인증, 또는 그 프로젝트의 세션. 그러면 키를 브라우저에 둘 필요도 없어진다.
 *
 * 남는 위험: 운영에서 인증 없이 이 화면을 노출하면 API Key 가 브라우저 저장소에 머문다 — 위 배치 중 하나를 반드시 고른다.
 */
@Controller
@RequestMapping("/admin")
class AdminController {
    /**
     * 대시보드 메인 페이지
     */
    @GetMapping
    fun dashboard(): String = "admin/dashboard"

    /**
     * Job 실행 목록 페이지
     */
    @GetMapping("/executions")
    fun executions(): String = "admin/executions"

    /**
     * Job 실행 상세 페이지
     */
    @GetMapping("/executions/{id}")
    fun executionDetail(
        @PathVariable id: Long,
        model: Model,
    ): String {
        model.addAttribute("executionId", id)
        return "admin/execution-detail"
    }

    /**
     * 에러 로그 페이지
     */
    @GetMapping("/errors")
    fun errors(): String = "admin/errors"

    /**
     * 스케줄 설정 페이지
     */
    @GetMapping("/schedules")
    fun schedules(): String = "admin/schedules"

    /**
     * Webhook 트리거 이력 페이지
     */
    @GetMapping("/webhooks")
    fun webhooks(): String = "admin/webhooks"
}