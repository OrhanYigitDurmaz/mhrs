from typing import Any, Optional

from fastapi import HTTPException, status
from fastapi.requests import Request
from fastapi.responses import JSONResponse


class ErrorCode:
    # Auth errors
    INVALID_TOKEN = "INVALID_TOKEN"
    INVALID_CREDENTIALS = "INVALID_CREDENTIALS"
    USER_NOT_FOUND = "USER_NOT_FOUND"
    USER_ALREADY_EXISTS = "USER_ALREADY_EXISTS"
    INVALID_TC_NO = "INVALID_TC_NO"
    FORBIDDEN = "FORBIDDEN"

    # Resource errors
    NOT_FOUND = "NOT_FOUND"
    ALREADY_EXISTS = "ALREADY_EXISTS"

    # Validation errors
    VALIDATION_ERROR = "VALIDATION_ERROR"
    INVALID_ROLE = "INVALID_ROLE"

    # Business logic errors
    SLOT_NOT_AVAILABLE = "SLOT_NOT_AVAILABLE"
    APPOINTMENT_EXISTS = "APPOINTMENT_EXISTS"
    APPOINTMENT_ALREADY_CANCELLED = "APPOINTMENT_ALREADY_CANCELLED"
    NO_WORKING_HOURS = "NO_WORKING_HOURS"


class APIException(Exception):
    """Base API exception with structured error response"""

    def __init__(
        self,
        message: str,
        code: str = ErrorCode.VALIDATION_ERROR,
        status_code: int = status.HTTP_400_BAD_REQUEST,
        details: Optional[dict[str, Any]] = None,
    ):
        self.message = message
        self.code = code
        self.status_code = status_code
        self.details = details or {}
        super().__init__(message)


class NotFoundException(APIException):
    def __init__(self, message: str = "Resource not found", details: Optional[dict[str, Any]] = None):
        super().__init__(
            message=message,
            code=ErrorCode.NOT_FOUND,
            status_code=status.HTTP_404_NOT_FOUND,
            details=details,
        )


class BadRequestException(APIException):
    def __init__(self, message: str, code: str = ErrorCode.VALIDATION_ERROR, details: Optional[dict[str, Any]] = None):
        super().__init__(
            message=message,
            code=code,
            status_code=status.HTTP_400_BAD_REQUEST,
            details=details,
        )


class UnauthorizedException(APIException):
    def __init__(self, message: str = "Unauthorized", details: Optional[dict[str, Any]] = None):
        super().__init__(
            message=message,
            code=ErrorCode.INVALID_TOKEN,
            status_code=status.HTTP_401_UNAUTHORIZED,
            details=details,
        )


class ForbiddenException(APIException):
    def __init__(self, message: str = "Forbidden", details: Optional[dict[str, Any]] = None):
        super().__init__(
            message=message,
            code=ErrorCode.FORBIDDEN,
            status_code=status.HTTP_403_FORBIDDEN,
            details=details,
        )


async def api_exception_handler(request: Request, exc: APIException) -> JSONResponse:
    """Global exception handler for APIException"""
    return JSONResponse(
        status_code=exc.status_code,
        content={
            "success": False,
            "error": {
                "code": exc.code,
                "message": exc.message,
                "details": exc.details,
            },
        },
    )


async def http_exception_handler(request: Request, exc: HTTPException) -> JSONResponse:
    """Global exception handler for HTTPException (backward compatibility)"""
    return JSONResponse(
        status_code=exc.status_code,
        content={
            "success": False,
            "error": {
                "code": "HTTP_ERROR",
                "message": str(exc.detail),
                "details": {},
            },
        },
    )


async def validation_exception_handler(request: Request, exc: Exception) -> JSONResponse:
    """Global exception handler for validation errors"""
    return JSONResponse(
        status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
        content={
            "success": False,
            "error": {
                "code": ErrorCode.VALIDATION_ERROR,
                "message": "Validation error",
                "details": {"errors": str(exc)},
            },
        },
    )
