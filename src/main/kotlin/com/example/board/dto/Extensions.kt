package com.example.board.dto

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

fun PostDto.PageRequest.toPageable(): Pageable {
    val sortOrder = when (direction) {
        PostDto.SortDirection.DESCENDING -> Sort.by(sortBy).descending()
        PostDto.SortDirection.ASCENDING -> Sort.by(sortBy).ascending()
    }
    return PageRequest.of(page, size, sortOrder)
}