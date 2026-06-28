package com.utakatalp.donebot.domain.usecase

interface FetchTasksUseCase {
    operator fun invoke(force: Boolean = false)
}
