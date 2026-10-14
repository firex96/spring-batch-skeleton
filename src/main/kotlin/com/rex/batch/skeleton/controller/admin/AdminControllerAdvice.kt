package com.rex.batch.skeleton.controller.admin

import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ModelAttribute

/**
 * Admin 페이지 공통 Model Attribute 설정
 *
 * Thymeleaf 템플릿에서 사용할 공통 변수를 제공합니다.
 */
@ControllerAdvice(assignableTypes = [AdminController::class])
class AdminControllerAdvice {
    @ModelAttribute("currentPath")
    fun currentPath(request: HttpServletRequest): String = request.requestURI
}