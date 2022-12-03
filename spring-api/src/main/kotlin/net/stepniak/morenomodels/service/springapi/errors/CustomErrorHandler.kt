package net.stepniak.morenomodels.service.springapi.errors

import net.stepniak.morenomodels.service.generated.model.Error
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import javax.validation.ConstraintViolationException

@ControllerAdvice
class CustomErrorHandler {

    @ExceptionHandler
    fun handleConstraintViolation(cve: ConstraintViolationException): ResponseEntity<Error> {
        return ResponseEntity
            .badRequest()
            .body(Error(cve.message))
    }
}