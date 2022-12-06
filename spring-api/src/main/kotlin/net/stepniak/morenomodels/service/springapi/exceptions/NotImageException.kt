package net.stepniak.morenomodels.service.springapi.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
class NotImageException : Exception() {
}