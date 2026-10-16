package com.donnations.backend.error

import com.donnations.backend.domain.exception.CampaignEndedException
import com.donnations.backend.domain.exception.CampaignNotFoundException
import com.donnations.backend.domain.exception.DonationConflictException
import com.donnations.backend.domain.exception.DonationNotFoundException
import com.donnations.backend.domain.exception.ErrorCode
import com.donnations.backend.domain.exception.InvalidDeviceTokenException
import com.donnations.backend.domain.exception.InvalidDonationAmountException
import com.donnations.backend.http.dto.ErrorResponseDto
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.resource.NoResourceFoundException

// Every failure maps to ErrorResponseDto so stack traces never reach clients.
@RestControllerAdvice
class GlobalExceptionHandler {
    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(CampaignNotFoundException::class)
    fun handleNotFound(e: CampaignNotFoundException): ResponseEntity<ErrorResponseDto> =
        error(HttpStatus.NOT_FOUND, e.message.orEmpty(), e.errorCode)

    @ExceptionHandler(DonationNotFoundException::class)
    fun handleDonationNotFound(e: DonationNotFoundException): ResponseEntity<ErrorResponseDto> =
        error(HttpStatus.NOT_FOUND, e.message.orEmpty(), e.errorCode)

    @ExceptionHandler(InvalidDonationAmountException::class)
    fun handleInvalidDonationAmount(e: InvalidDonationAmountException): ResponseEntity<ErrorResponseDto> =
        error(HttpStatus.BAD_REQUEST, e.message.orEmpty(), e.errorCode)

    @ExceptionHandler(InvalidDeviceTokenException::class)
    fun handleInvalidDeviceToken(e: InvalidDeviceTokenException): ResponseEntity<ErrorResponseDto> =
        error(HttpStatus.BAD_REQUEST, e.message.orEmpty(), e.errorCode)

    @ExceptionHandler(CampaignEndedException::class)
    fun handleCampaignEnded(e: CampaignEndedException): ResponseEntity<ErrorResponseDto> =
        error(HttpStatus.CONFLICT, e.message.orEmpty(), e.errorCode)

    @ExceptionHandler(DonationConflictException::class)
    fun handleDonationConflict(e: DonationConflictException): ResponseEntity<ErrorResponseDto> =
        error(HttpStatus.CONFLICT, e.message.orEmpty(), e.errorCode)

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleUnreadableBody(e: HttpMessageNotReadableException): ResponseEntity<ErrorResponseDto> {
        log.debug("Unreadable request body", e)
        return error(HttpStatus.BAD_REQUEST, "Malformed request body", ErrorCode.INVALID_REQUEST)
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(e: MethodArgumentTypeMismatchException): ResponseEntity<ErrorResponseDto> =
        error(HttpStatus.BAD_REQUEST, "Invalid value for '${e.name}'", ErrorCode.INVALID_REQUEST)

    @ExceptionHandler(NoResourceFoundException::class)
    fun handleRouteNotFound(e: NoResourceFoundException): ResponseEntity<ErrorResponseDto> =
        error(HttpStatus.NOT_FOUND, "No such route", ErrorCode.ROUTE_NOT_FOUND)

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(e: Exception): ResponseEntity<ErrorResponseDto> {
        log.error("Unhandled exception", e)
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong. Please try again later.", ErrorCode.INTERNAL_ERROR)
    }

    private fun error(status: HttpStatus, message: String, errorCode: ErrorCode): ResponseEntity<ErrorResponseDto> =
        ResponseEntity.status(status).body(ErrorResponseDto(message = message, errorCode = errorCode.value))
}
