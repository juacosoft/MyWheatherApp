package com.mtzdev.mywheatherapp.domain.model

/**
 * Generic wrapper for operation results in the domain layer.
 * Represents the three states of an asynchronous operation: Loading, Success, Error.
 * Provides type-safe handling of operation outcomes.
 *
 * @param T The type of data held by Success state
 */
sealed class Result<out T> {

    /**
     * Represents a loading state (operation in progress).
     * Used to trigger loading UI indicators.
     */
    data object Loading : Result<Nothing>()

    /**
     * Represents a successful operation with data.
     *
     * @property data The result data of type T
     */
    data class Success<T>(val data: T) : Result<T>()

    /**
     * Represents a failed operation with error information.
     *
     * @property error The domain error that occurred
     */
    data class Error(val error: DomainError) : Result<Nothing>()

    /**
     * Maps the success data to a different type.
     * Error and Loading states are passed through unchanged.
     *
     * @param transform Function to transform success data
     * @return Result with transformed data or original Error/Loading
     */
    fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
        is Loading -> this
    }

    /**
     * Chains another Result-producing operation if this is Success.
     * Error and Loading states are passed through unchanged.
     *
     * @param transform Function that produces a new Result
     * @return Result from transform or original Error/Loading
     */
    fun <R> flatMap(transform: (T) -> Result<R>): Result<R> = when (this) {
        is Success -> transform(data)
        is Error -> this
        is Loading -> this
    }
}

/**
 * Extension to check if result is successful.
 */
fun <T> Result<T>.isSuccess(): Boolean = this is Result.Success

/**
 * Extension to check if result is error.
 */
fun <T> Result<T>.isError(): Boolean = this is Result.Error

/**
 * Extension to check if result is loading.
 */
fun <T> Result<T>.isLoading(): Boolean = this is Result.Loading

/**
 * Extension to get data or null.
 * Returns data if Success, null otherwise.
 */
fun <T> Result<T>.getOrNull(): T? = when (this) {
    is Result.Success -> data
    else -> null
}

/**
 * Extension to get error or null.
 * Returns error if Error, null otherwise.
 */
fun <T> Result<T>.exceptionOrNull(): DomainError? = when (this) {
    is Result.Error -> error
    else -> null
}

/**
 * Extension to get data or default value.
 * Returns data if Success, default value otherwise.
 */
fun <T> Result<T>.getOrDefault(default: T): T = when (this) {
    is Result.Success -> data
    else -> default
}

/**
 * Extension to execute block only if Success.
 */
inline fun <T> Result<T>.onSuccess(block: (T) -> Unit): Result<T> {
    if (this is Result.Success) {
        block(data)
    }
    return this
}

/**
 * Extension to execute block only if Error.
 */
inline fun <T> Result<T>.onError(block: (DomainError) -> Unit): Result<T> {
    if (this is Result.Error) {
        block(error)
    }
    return this
}

/**
 * Extension to execute block only if Loading.
 */
inline fun <T> Result<T>.onLoading(block: () -> Unit): Result<T> {
    if (this is Result.Loading) {
        block()
    }
    return this
}
